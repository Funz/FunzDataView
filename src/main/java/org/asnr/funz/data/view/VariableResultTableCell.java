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
package org.asnr.funz.data.view;

import org.asnr.funz.data.model.CaseResults;

import javafx.scene.control.cell.TextFieldTableCell;

/**
 * TableCell for parameter/variable results.
 *
 * @author Nicolas Chabalier - Artenum SARL
 */
public class VariableResultTableCell extends TextFieldTableCell<CaseResults, String> {

    @Override
    public void updateItem(final String item, final boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
            this.setText(null);
            this.setGraphic(null);
            this.getStyleClass().remove("highlighted-table-cell");
        } else {
            this.getStyleClass().add("highlighted-table-cell");
        }

    }

}
