/*
 * Project        : FunzDataView
 * Website        : https://github.com/Funz/FunzDataView
 * Copyright      : © ASNR
 *                  31 Avenue de la Division Leclerc
 *                  92260 Fontenay-aux-Roses, France
 *                  https://www.asnr.fr
 * Licence        : cf. LICENSE
 * Developed By   : Artenum SARL
 * Authors        : Arnaud Trouche
 *                  Nicolas Chabalier
 *                  Julien Forest
 * Contract       : contract N°50000976 - order N°34007261
 */
package org.asnr.funz.data.model.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;

import org.asnr.funz.data.model.DiscreteCaseResults;
import org.asnr.funz.data.model.DoeCaseResults;
import org.asnr.funz.data.model.ResultModel;
import org.asnr.funz.model.ExtendedProject;
import org.asnr.funz.model.ExtendedProjectUtils;
import org.funz.Project;
import org.funz.doeplugin.Design;
import org.funz.doeplugin.DesignSession;
import org.funz.parameter.Case;
import org.funz.parameter.CaseList;
import org.funz.parameter.Variable;

import javafx.beans.value.ObservableValueBase;

/**
 * Holds all the data for the Data Miners.
 *
 * @author Arnaud TROUCHE - Artenum SARL
 */
public class DefaultResultModel extends ObservableValueBase<Collection<DiscreteCaseResults>> implements ResultModel {

    /**
     * Current project.
     */
    private final ExtendedProject project;

    /**
     * All the variables for the current project.
     */
    private final Set<String> variables;

    /**
     * All the discrete variables for the current project.
     */
    private final Set<String> discreteVariables;

    /**
     * All the cases for the current project, stored by index.
     */
    private Map<Integer, DiscreteCaseResults> cases;

    /**
     * All the original cases for the current project, stored by index.
     */
    private final Map<Integer, DiscreteCaseResults> originalCases;

    /**
     * DOE cases.
     */
    private final List<DoeCaseResults> doeCases;

    /**
     * Represents the name of the result.
     */
    private String resultName;

    /**
     * Represents whether the results have been obtained with a {@link Design}.
     */
    private boolean haveDesign = false;

    private final List<String> doeOutputs;

    /**
     * Create the result renderer model for the given project.
     *
     * @param project
     */
    public DefaultResultModel(final ExtendedProject project) {
        this.variables = new HashSet<>();
        this.discreteVariables = new HashSet<>();
        this.cases = new HashMap<>();
        this.originalCases = new HashMap<>();
        this.doeCases = new ArrayList<>();
        this.doeOutputs = new ArrayList<>();

        this.project = project;

        // Listen to status
        this.project.addStatusListener((modifiedProject, newStatus) -> {
            switch (newStatus) {
            case RUNNING:
                this.clearModel();
                //$FALL-THROUGH$
            case RESULTS:
                this.fillModel();
                break;
            case INITIAL, FAILED:
                break;
            }
        });

        this.project.addCaseModifiedListener((index, what) -> {
            switch (what) {
            case Case.MODIFIED_CALC, Case.MODIFIED_STATE ->
                    Executors.defaultThreadFactory().newThread(() -> this.refreshCase(index)).start();
            case Case.MODIFIED_INFO, Case.MODIFIED_TIME -> this.fireValueChangedEvent();
            }
        });

        // When design updated, we refresh the corresponding line (using the design index)
        this.project.addDesignUpdatedListener(
                index -> Executors.defaultThreadFactory().newThread(() -> this.refreshCase(index)).start());
    }

    @Override
    public Collection<DiscreteCaseResults> getValue() {
        return this.cases.values();
    }

    /**
     * @return the variables
     */
    @Override
    public Collection<String> getVariables() {
        return this.variables;
    }

    @Override
    public Collection<String> getDiscreteVariables() {
        return this.discreteVariables;
    }

    /**
     * @return the cases
     */
    @Override
    public Collection<DiscreteCaseResults> getDiscreteResults() {
        return this.cases.values();
    }

    @Override
    public Collection<DoeCaseResults> getDoeResults() {
        return this.doeCases;
    }

    @Override
    public List<String> getDoeOutputs() {
        return this.doeOutputs;
    }

    /**
     * @return the resultName
     */
    @Override
    public String getResultName() {
        return this.resultName;
    }

    @Override
    public boolean hasDesign() {
        return this.haveDesign;
    }

    @Override
    protected void fireValueChangedEvent() {
        // Recreate a map to force fire value changed event
        this.cases = new HashMap<>(this.cases);
        super.fireValueChangedEvent();
    }

    /**
     * Clear the model.
     */
    private void clearModel() {
        this.variables.clear();
        this.discreteVariables.clear();
        this.cases.clear();
        this.doeCases.clear();
        this.doeOutputs.clear();
        this.haveDesign = false;
        this.fireValueChangedEvent();
    }

    /**
     * Fill the model with cases and variables.
     */
    private void fillModel() {
        Executors.defaultThreadFactory().newThread(() -> {
            this.resultName = ExtendedProjectUtils.getResultName(this.project);

            // Create variables
            this.fillVariables();

            // Create discrete cases
            if (this.project.getCases() != null) {
                this.fillValues();
                this.haveDesign = !this.project.getContinuousParameters().isEmpty();
            }

            // Create DOE cases
            this.doeOutputs.clear();
            this.doeCases.clear();
            if ((this.project.getDesignSessions() != null) && !this.project.getDesignSessions().isEmpty()) {
                for (final DesignSession ds : this.project.getDesignSessions()) {
                    final DefaultDoeCase doe = new DefaultDoeCase(ds);
                    if (!doe.getOutputs().isEmpty()) {
                        this.doeOutputs.addAll(doe.getOutputs().keySet());
                    }
                    this.doeCases.add(doe);

                }
            }
            this.fireValueChangedEvent();
        }).start();
    }

    /**
     * Create the variables for the current project.
     */
    private void fillVariables() {
        // Discrete variables
        final List<String> discretes = ExtendedProjectUtils.getDiscreteVariablesUngrouped(this.project).stream()
                .map(Variable::getName).toList();
        this.variables.addAll(discretes);
        this.discreteVariables.addAll(discretes);

        // Continuous variables
        final List<String> continuous = ExtendedProjectUtils.getContinuousVariablesUngrouped(this.project).stream()
                .map(Variable::getName).toList();
        this.variables.addAll(continuous);

        // If there is no variables, we add a "all-in-one"
        if (this.variables.isEmpty()) {
            this.variables.add(Project.SINGLE_PARAM_NAME);
        }
        if (this.discreteVariables.isEmpty()) {
            this.discreteVariables.add(Project.SINGLE_PARAM_NAME);
        }
    }

    /**
     * Create the cases for the current project.
     *
     * @throws Exception
     *         if unable to get main result.
     */
    private void fillValues() {
        for (final Case currentCase : this.project.getCases()) {
            this.refreshCase(currentCase.getIndex());
        }
    }

    private synchronized void refreshCase(final int index) {
        if (this.variables.isEmpty()) {
            this.fillVariables();
        }

        final CaseList projectCases = this.project.getCases();
        if ((projectCases != null) && (projectCases.size() > index)) {
            if (this.cases.size() != projectCases.size()) {
                this.createExtractors();
            }

            ((CaseExtractor) this.cases.get(index)).update();
        }
    }

    private void createExtractors() {
        final CaseList projectCases = this.project.getCases();
        for (int index = 0; index < projectCases.size(); index++) {
            this.originalCases.computeIfAbsent(index, i -> new CaseExtractor(this.project, projectCases.get(i)));
        }
        this.cases = new HashMap<>(this.originalCases);

        this.fireValueChangedEvent();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + ((this.cases == null) ? 0 : this.cases.hashCode());
        result = (prime * result) + (this.haveDesign ? 1231 : 1237);
        result = (prime * result) + this.doeOutputs.hashCode();
        result = (prime * result) + ((this.resultName == null) ? 0 : this.resultName.hashCode());
        result = (prime * result) + this.variables.hashCode();
        result = (prime * result) + this.discreteVariables.hashCode();
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        final DefaultResultModel other = (DefaultResultModel) obj;
        if (!Objects.equals(this.cases, other.cases)) {
            return false;
        }
        if (this.haveDesign != other.haveDesign) {
            return false;
        }
        if (!this.doeOutputs.equals(other.doeOutputs)) {
            return false;
        }
        if (!Objects.equals(this.resultName, other.resultName)) {
            return false;
        }
        if (!this.discreteVariables.equals(other.discreteVariables)) {
            return false;
        }
        return this.variables.equals(other.variables);
    }

    public void groupVariable(final String selectedParameter) {
        if (selectedParameter.equals("None")) {
            this.cases = new HashMap<>(this.originalCases);
        } else {
            final Collection<String> variablesToGroup = new ArrayList<>(this.getDiscreteVariables());
            variablesToGroup.remove(selectedParameter);

            this.cases.clear();
            final List<List<DiscreteCaseResults>> caseResultsGroups = DefaultResultModel.groupByVariableValues(
                    this.originalCases.values(), variablesToGroup);
            final BinaryOperator<String> variableMerger = (v1, v2) -> (v1 != null && v1.equals(v2)) ?
                    v1 :
                    v1 + "," + v2;
            final BinaryOperator<String> valuesMerger = (v1, v2) -> {
                try {
                    // Attempt to parse the strings as doubles
                    final double d1 = Double.parseDouble(v1);
                    final double d2 = Double.parseDouble(v2);
                    // Calculate the mean and return it as a string
                    return String.valueOf((d1 + d2) / 2);
                } catch (final NumberFormatException e) {
                    // Return "?" if either of the values cannot be parsed as a double
                    return "?";
                }
            };

            final AtomicInteger key = new AtomicInteger();
            caseResultsGroups.forEach(caseResultsGroup -> {
                final List<CaseExtractor> caseExtractorsList = caseResultsGroup.stream() //
                        .map(CaseExtractor.class::cast) //
                        .toList();
                final MergeCaseExtractor mergeCaseExtractor = new MergeCaseExtractor(caseExtractorsList,
                        this.getDiscreteVariables(), variableMerger, valuesMerger);
                this.cases.put(key.getAndIncrement(), mergeCaseExtractor);
            });
        }
        super.fireValueChangedEvent();
    }

    private static List<List<DiscreteCaseResults>> groupByVariableValues(
            final Collection<DiscreteCaseResults> caseResults, final Collection<String> variableNames) {
        final Map<String, List<DiscreteCaseResults>> groupedMap = new HashMap<>();

        for (final DiscreteCaseResults caseResult : caseResults) {
            final String key = variableNames.stream().map(caseResult::getValueForVariable)
                    .collect(Collectors.joining("-"));

            groupedMap.computeIfAbsent(key, k -> new ArrayList<>()).add(caseResult);
        }

        return new ArrayList<>(groupedMap.values());
    }
}
