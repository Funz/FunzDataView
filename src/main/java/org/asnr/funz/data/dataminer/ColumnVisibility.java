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

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

/**
 * Represents the visibility for a column: its name and its visibility status
 *
 * @param name
 *         the name of the column
 * @param visible
 *         the visibility property
 * @author Arnaud Trouche - Artenum SARL
 */
record ColumnVisibility(String name, BooleanProperty visible) {

    /**
     * @param name
     *         the column name
     * @param visibility
     *         the column visibility
     */
    ColumnVisibility(final String name, final boolean visibility) {
        this(name, new SimpleBooleanProperty(visibility));
    }

}
