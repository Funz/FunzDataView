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

import org.asnr.funz.data.model.ResultModel;

import javafx.scene.Node;

/**
 * Represents a Data Miner that given a {@link ResultModel} can display/or not the results in its own way.
 *
 * @author Arnaud TROUCHE - Artenum SARL
 */
public interface DataMiner {

    /**
     * Clear the renderer from all its content.
     */
    void clear();

    /**
     * Refresh the variables (the columns for a Table renderer for example)
     */
    void refreshVariables();

    /**
     * Refresh the cases (the lines for a Table renderer for example)
     */
    void refreshCases();

    /**
     * @return {@code true} if this {@link DataMiner} can display the current data or {@code false} otherwise
     */
    boolean canDisplayData();

    /**
     * @return the name of this {@link DataMiner} to be displayed to the user
     */
    String getName();

    /**
     * @return the {@link Node} holding the renderer.
     */
    Node getView();
}
