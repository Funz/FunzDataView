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
package org.asnr.funz.data.model;

import java.util.Collection;
import java.util.List;

import org.funz.doeplugin.Design;

import javafx.beans.value.ObservableValue;

/**
 * Holds all the data for the Data Miners.
 *
 * @author Arnaud TROUCHE - Artenum SARL
 */
public interface ResultModel extends ObservableValue<Collection<DiscreteCaseResults>> {

    /**
     * @return the variables
     */
    Collection<String> getVariables();

    /**
     * @return the variables
     */
    Collection<String> getDiscreteVariables();

    /**
     * @return all the cases
     */
    Collection<DiscreteCaseResults> getDiscreteResults();

    /**
     * @return the DOE cases
     */
    Collection<DoeCaseResults> getDoeResults();

    /**
     * @return the resultName
     */
    String getResultName();

    /**
     * Represents whether these results have been obtained using a {@link Design} or not.
     *
     * @return {@code true} if these results have been obtained using a {@link Design}, {@code false} otherwise
     */
    boolean hasDesign();

    /**
     * @return the list of DOE output names
     */
    List<String> getDoeOutputs();

}