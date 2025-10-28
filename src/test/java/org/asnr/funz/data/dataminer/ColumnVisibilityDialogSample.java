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

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.artenum.tk.ui.util.FxHelper;

import javafx.application.Platform;

class ColumnVisibilityDialogSample {

    private static final Logger log = LoggerFactory.getLogger(ColumnVisibilityDialogSample.class);

    public static void main(final String[] args) {

        final List<ColumnVisibility> values = List.of( //
                new ColumnVisibility("1", false), //
                new ColumnVisibility("2", true), //
                new ColumnVisibility("3", false), //
                new ColumnVisibility("4", true), //
                new ColumnVisibility("5", false) //

        );

        Platform.startup(() -> {
            // Not here
        });

        final List<ColumnVisibility> result = FxHelper.runLaterAndGet(() -> ColumnVisibilityDialog.show(null, values));
        ColumnVisibilityDialogSample.log.info("Obtained results:\n {}", result);
    }
}