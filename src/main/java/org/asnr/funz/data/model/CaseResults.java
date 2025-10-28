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

import org.funz.parameter.Variable;

/**
 * Represents common behaviour between {@link DoeCaseResults} and {@link DiscreteCaseResults}.
 *
 * @author Arnaud Trouche - ARTENUM SARL
 */
public sealed interface CaseResults permits DiscreteCaseResults, DoeCaseResults {

    /**
     * @param variableName
     *         the name of the {@link Variable}
     * @return the value for the given discrete variable.
     */
    String getValueForVariable(String variableName);

    /**
     * @param outputFunction
     *         the name of the output function for which we want to get the result
     * @return the Object result for the given custom output function
     */
    Object getResult(final String outputFunction);

    /**
     * @param selectedOutput
     *         the selected output
     * @return the string result to show on the table
     */
    String getStringResult(final String selectedOutput);
}
