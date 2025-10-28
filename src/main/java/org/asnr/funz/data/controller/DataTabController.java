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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.asnr.funz.data.dataminer.TableDataMiner;
import org.asnr.funz.data.model.CaseResults;
import org.asnr.funz.data.model.ResultModel;
import org.asnr.funz.data.view.HtmlVariablesUtils;
import org.asnr.funz.model.ExtendedProject;
import org.funz.util.Data;

/**
 * Controller of the tab displaying several results renderers that will display the results in different ways.
 *
 * @author Arnaud TROUCHE - Artenum SARL
 */
final class DataTabController extends AbstractDataMinerController {

    private final TableDataMiner tableDataMiner;

    /**
     * Creates a {@link DataTabController} for the given {@link ExtendedProject}.
     *
     * @param project
     *         the input project
     */
    public DataTabController(final ExtendedProject project) {
        super(project, HtmlVariablesUtils.Location.VIEWS);

        // Add table
        this.tableDataMiner = new TableDataMiner(project);
        this.addDataMiner(this.tableDataMiner);
    }

    @Override
    protected String getParameters() {
        final ResultModel resultModel = this.getProject().getResultModel();
        final Collection<String> variables = resultModel.getVariables();
        final List<CaseResults> cases = new ArrayList<>(resultModel.getDiscreteResults());

        return this.getParameters(variables, cases);
    }

    @Override
    protected String getValuesAsString() {
        final String[] outputNames = this.getProject().getSyncOutputNames();
        final Map<String, Object[]> results = HashMap.newHashMap(outputNames.length);

        for (final String outputName : outputNames) {
            final Object[] array = this.getProject().getResultModel().getDiscreteResults().stream()
                    .map(r -> r.getResult(outputName)).toArray(Object[]::new);

            // We want only numeric value here for web viewer (parcoord, scatterplot)
            if (array != null && array.length > 0 && array[0] instanceof Double) {
                results.put(outputName, array);
            }
        }

        return Data.asString(results);
    }

    /**
     * Set output data to display by default (checkbox to select by default in views)
     */
    public void setDefaultOutputSelection(final List<String> outputs) {
        this.tableDataMiner.setDefaultOutputSelection(outputs);
    }

    /**
     * Get list of all column name selected
     */
    public List<String> getSelectedColumnsNames() {
        return this.tableDataMiner.getSelectedColumnsNames();
    }

    public javafx.scene.control.ContextMenu getTableContextMenu() {
        return this.tableDataMiner.getContextMenu();
    }

    public TableDataMiner getTableDataMiner() {
        return this.tableDataMiner;
    }

}
