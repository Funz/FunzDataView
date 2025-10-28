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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.asnr.funz.data.i18n.ResultsDictionary;
import org.asnr.funz.data.model.CaseResults;
import org.asnr.funz.data.model.DoeCaseResults;
import org.asnr.funz.model.ExtendedProject;

import com.artenum.tk.ui.control.table.fx.WebViewTableCell;

import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;

/**
 * Data miner that displays the result values in a table.
 *
 * @author Arnaud TROUCHE - Artenum SARL
 */
public class TableDoeDataMiner extends AbstractTableDataMiner<DoeCaseResults, String> implements DataMiner {

    /**
     * Default constructor.
     *
     * @param currentProject
     *         the current {@link ExtendedProject}
     */
    public TableDoeDataMiner(final ExtendedProject currentProject) {
        super(currentProject);

        this.getTable().setRowFactory(param -> {
            final TableRow<CaseResults> tableRow = new TableRow<>();
            tableRow.setMinHeight(400d);
            return tableRow;
        });

    }

    @Override
    protected Collection<DoeCaseResults> getResults() {
        return this.getResultModel().getDoeResults();
    }

    @Override
    protected Collection<String> getVariables() {
        return this.getResultModel().getDiscreteVariables();
    }

    @Override
    protected TableColumn<CaseResults, String> createColumn(final String name) {
        final TableColumn<CaseResults, String> column = new TableColumn<>(name);
        column.setCellValueFactory(p -> {
            final String valueForVariable = p.getValue().getValueForVariable(name);
            if (valueForVariable != null) {
                return new SimpleStringProperty(valueForVariable);
            } else {
                return new SimpleStringProperty();
            }
        });
        return column;
    }

    @Override
    protected Map<TableColumn<CaseResults, String>, Boolean> outputColumns() {
        final Map<TableColumn<CaseResults, String>, Boolean> results = new HashMap<>();

        // We add a last column for the result
        final TableColumn<CaseResults, String> resultColumn = new TableColumn<>(ResultsDictionary.HTML.getString());
        resultColumn.setCellValueFactory(p -> new SimpleStringProperty(((DoeCaseResults) p.getValue()).getHtml()));
        resultColumn.setCellFactory(param -> new WebViewTableCell<>());
        results.put(resultColumn, Boolean.TRUE);

        return results;
    }

}
