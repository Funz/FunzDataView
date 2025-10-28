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
    protected Object getResultForColumn(final CaseResults originalResult, final String columnName) {
        return TableDataMiner.getResultForColumnInAllProjects((DiscreteCaseResults) originalResult, columnName,
                this.getAllResults());
    }

    /**
     * Get result for the given project
     * If there is another ExtendedProject containing the column we return the result value of this project
     * Otherwise we return the result value of the original ExtendedProject
     *
     * @param originalResult
     *         case results associated to the original project
     * @param columnName
     *         column name to extract
     * @param allResults
     *         results of all projects
     * @return an Object result
     */
    private static Object getResultForColumnInAllProjects(final DiscreteCaseResults originalResult,
            final String columnName, final Collection<DiscreteCaseResults> allResults) {
        final Object resultObj = originalResult.getResult(columnName);
        if (TableDataMiner.isResultValid(resultObj)) {
            return resultObj;
        }

        final Map<String, Object> originalInputValues = originalResult.getCase().getInputValues();
        for (final DiscreteCaseResults otherResults : allResults) {
            if (!otherResults.equals(originalResult)) {
                boolean same = true;
                for (final Map.Entry<String, Object> entry : originalInputValues.entrySet()) {
                    if (!otherResults.getValueForVariable(entry.getKey())
                            .equals(originalResult.getValueForVariable(entry.getKey()))) {
                        same = false;
                        break;
                    }
                }
                if (same) {
                    final Object result = otherResults.getResult(columnName);
                    if (TableDataMiner.isResultValid(result)) {
                        return result;
                    }
                }
            }
        }
        return originalResult.getResult(columnName);
    }

    private static boolean isResultValid(final Object result) {
        return !result.equals("?");
    }

    /**
     * Get results that has same variables values than the originalResult
     *
     * @param originalResult
     * @return
     */
    public static Collection<DiscreteCaseResults> getSimilarResults(final DiscreteCaseResults originalResult,
            final Collection<DiscreteCaseResults> allResults) {
        final List<DiscreteCaseResults> similarResults = new ArrayList<>();
        final Map<String, Object> originalInputValues = originalResult.getCase().getInputValues();
        for (final DiscreteCaseResults otherResults : allResults) {
            if (!otherResults.equals(originalResult)) {
                boolean same = true;
                for (final Map.Entry<String, Object> entry : originalInputValues.entrySet()) {
                    if (!otherResults.getValueForVariable(entry.getKey())
                            .equals(originalResult.getValueForVariable(entry.getKey()))) {
                        same = false;
                        break;
                    }
                }
                if (same) {
                    similarResults.add(otherResults);
                }
            }
        }
        return similarResults;
    }

    @Override
    protected Collection<String> getVariables() {
        return this.getResultModels().getFirst().getVariables();
    }

    @Override
    protected TableColumn<CaseResults, DiscreteCaseResults> createColumn(final String name) {
        final TableColumn<CaseResults, DiscreteCaseResults> resultsColumn = new TableColumn<>(name);
        resultsColumn.setCellValueFactory(c -> (DiscreteCaseResults) c.getValue());
        final Collection<DiscreteCaseResults> allResults = this.getAllResults();
        resultsColumn.setCellFactory(param -> new MultipleResultTableCell(name, allResults, this.getColumnsFormat()));
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
