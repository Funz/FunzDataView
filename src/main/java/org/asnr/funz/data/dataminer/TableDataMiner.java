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
package org.asnr.funz.data.dataminer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.asnr.funz.data.model.CaseResults;
import org.asnr.funz.data.model.DiscreteCaseResults;
import org.asnr.funz.data.model.ResultModel;
import org.asnr.funz.data.view.MultipleResultTableCell;
import org.asnr.funz.data.view.utils.TableViewUtils;
import org.asnr.funz.model.ExtendedProject;

import javafx.scene.control.TableColumn;

/**
 * Data miner that displays the result values in a table.
 *
 * @author Arnaud TROUCHE - Artenum SARL
 */
public class TableDataMiner extends AbstractTableDataMiner<DiscreteCaseResults, DiscreteCaseResults> {

    /** The results of the other cases with the same variable values, by case. */
    private final Map<DiscreteCaseResults, List<DiscreteCaseResults>> similarResults = new ConcurrentHashMap<>();

    /**
     * Default constructor.
     *
     * @param currentProject
     *         the current {@link ExtendedProject}
     */
    public TableDataMiner(final ExtendedProject currentProject) {
        super(currentProject);
    }

    @Override
    protected Collection<DiscreteCaseResults> getResults() {
        return this.getResultModel().getDiscreteResults();
    }

    private Collection<DiscreteCaseResults> getAllResults() {
        final Collection<DiscreteCaseResults> discreteCaseResults = new ArrayList<>();
        for (final ResultModel resultModel : this.getResultModels()) {
            discreteCaseResults.addAll(resultModel.getDiscreteResults());
        }
        return discreteCaseResults;
    }

    @Override
    public void addExtendedProject(final ExtendedProject project) {
        super.addExtendedProject(project);
        // null when the super constructor adds the first project
        if (this.similarResults != null) {
            this.similarResults.clear();
        }
    }

    @Override
    public void refreshCases() {
        this.similarResults.clear();
        super.refreshCases();
    }

    /**
     * The value of a column is taken from another project, which has the same variable values, when the original
     * project has no valid value.
     */
    @Override
    protected Object getResultForColumn(final CaseResults originalResult, final String columnName) {
        final DiscreteCaseResults original = (DiscreteCaseResults) originalResult;
        final Object result = original.getResult(columnName);
        if (TableDataMiner.isResultValid(result)) {
            return result;
        }
        return this.getSimilarResults(original).stream()
                .map(other -> other.getResult(columnName))
                .filter(TableDataMiner::isResultValid)
                .findFirst()
                .orElse(result);
    }

    private static boolean isResultValid(final Object result) {
        return (result != null) && !"?".equals(result);
    }

    /**
     * @param originalResult
     *         the results of a case
     * @return the results of the other cases, of all the projects, that have the same variable values. They are
     *         searched once for each case, until the cases are refreshed.
     */
    public List<DiscreteCaseResults> getSimilarResults(final DiscreteCaseResults originalResult) {
        return this.similarResults.computeIfAbsent(originalResult,
                original -> TableDataMiner.findSimilarResults(original, this.getAllResults()));
    }

    private static List<DiscreteCaseResults> findSimilarResults(final DiscreteCaseResults originalResult,
            final Collection<DiscreteCaseResults> allResults) {
        final Set<String> variables = originalResult.getCase().getInputValues().keySet();
        return allResults.stream()
                .filter(other -> !other.equals(originalResult))
                .filter(other -> variables.stream().allMatch(
                        variable -> Objects.equals(other.getValueForVariable(variable),
                                originalResult.getValueForVariable(variable))))
                .toList();
    }

    @Override
    protected Collection<String> getVariables() {
        return this.getResultModels().getFirst().getVariables();
    }

    @Override
    protected TableColumn<CaseResults, DiscreteCaseResults> createColumn(final String name) {
        final TableColumn<CaseResults, DiscreteCaseResults> resultsColumn = new TableColumn<>(name);
        resultsColumn.setCellValueFactory(c -> (DiscreteCaseResults) c.getValue());
        final Map<String, String> columnsFormat = this.getColumnsFormat();
        resultsColumn.setCellFactory(
                param -> new MultipleResultTableCell(name, this::getSimilarResults, columnsFormat));
        resultsColumn.setComparator(TableViewUtils.createDiscretCaseResultsColumnComparator(name));
        return resultsColumn;
    }

    @Override
    protected Map<TableColumn<CaseResults, DiscreteCaseResults>, Boolean> outputColumns() {
        final Map<TableColumn<CaseResults, DiscreteCaseResults>, Boolean> results = new HashMap<>();
        final String mainOutputName = this.getMainOutputName();

        final List<String> outputNames = this.getOutputNames();
        for (final String name : outputNames) {
            final boolean isMainOutput = name.equals(mainOutputName);

            final TableColumn<CaseResults, DiscreteCaseResults> column = this.createColumn(name);
            column.getStyleClass().add("align-center");
            if (isMainOutput) {
                column.getStyleClass().add("bold");
            }

            results.put(column, isMainOutput);
        }
        return results;
    }

}
