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
package org.asnr.funz.data.controller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.asnr.funz.data.dataminer.DataMiner;
import org.asnr.funz.data.dataminer.TableDoeDataMiner;
import org.asnr.funz.data.model.CaseResults;
import org.asnr.funz.data.model.ResultModel;
import org.asnr.funz.data.view.HtmlVariablesUtils;
import org.asnr.funz.model.ExtendedProject;
import org.funz.Project;
import org.funz.util.Data;

import javafx.beans.binding.BooleanExpression;
import javafx.beans.property.SimpleBooleanProperty;

/**
 * Controller of the tab displaying several results for Algorithms/DOE.
 *
 * @author Arnaud TROUCHE - Artenum SARL
 */
final class AnalysisTabController extends AbstractDataMinerController {

    private final SimpleBooleanProperty hasContent = new SimpleBooleanProperty(false);

    private final TableDoeDataMiner tableDataMiner;

    /**
     * Creates a {@link AnalysisTabController} for the given {@link ExtendedProject}.
     *
     * @param project
     *         the input project
     */
    public AnalysisTabController(final ExtendedProject project) {
        super(project, HtmlVariablesUtils.Location.ANALYSIS);

        this.tableDataMiner = new TableDoeDataMiner(project);
        this.addDataMiner(this.tableDataMiner);
    }

    /**
     * Refresh all the {@link DataMiner}s.
     *
     * @param liveUpdate
     *         if {@code true} this refresh is a "live" refresh and should only be executed if the checkbox is
     *         checked
     */
    @Override
    protected void refresh(final boolean liveUpdate) {
        super.refresh(liveUpdate);
        this.hasContent.set(this.tableDataMiner.canDisplayData());
    }

    /**
     * @return the hasContent
     */
    public BooleanExpression hasContentProperty() {
        return this.hasContent;
    }

    @Override
    protected String getParameters() {
        final ResultModel resultModel = this.getProject().getResultModel();
        Collection<String> variables = resultModel.getDiscreteVariables();
        if (variables.contains(Project.SINGLE_PARAM_NAME)) {
            variables = Collections.emptyList();
        }
        final List<CaseResults> cases = new ArrayList<>(resultModel.getDoeResults());

        return this.getParameters(variables, cases);
    }

    @Override
    protected String getValuesAsString() {
        final List<String> outputNames = this.getProject().getResultModel().getDoeOutputs();

        final Map<String, Object[]> results = HashMap.newHashMap(outputNames.size());

        for (final String outputName : outputNames) {
            if (!outputName.contains(" name=\"")) {
                final Object[] array = this.getProject().getResultModel().getDoeResults().stream()
                        .map(r -> r.getStringResult(outputName)).toArray(Object[]::new);
                results.put(outputName, array);
            }
        }

        return Data.asString(results);
    }

}
