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

import java.util.List;
import java.util.Map;

import org.funz.parameter.Case;

import javafx.beans.value.ObservableValue;

/**
 * Holder of a Case result value.
 *
 * @author Arnaud TROUCHE - Artenum SARL
 */
public non-sealed interface DiscreteCaseResults extends ObservableValue<DiscreteCaseResults>, CaseResults {

    /**
     * @return the wrapped {@link Case}
     */
    Case getCase();

    /**
     * @param outputFunction
     *         the output function to filter
     * @return all the Object results except the one for the given custom output function
     */
    Map<String, Object> getOtherResults(final String outputFunction);

    /**
     * @return whether the current case has a count progress
     */
    boolean hasCountProgress();

    /**
     * @return whether the current case has a FTP address
     */
    boolean hasFtp();

    /**
     * @return whether the current case has a percent progress
     */
    boolean hasPercentProgress();

    /**
     * @return the FTP address if {@link #hasFtp()}, or "" if no FTP address
     */
    String getFtpAddress();

    /**
     * @return the current count progress for the case if {@link #hasCountProgress()}, or {@code -1} otherwise
     */
    double getCountProgress();

    /**
     * @return the current progress for the case if {@link #hasPercentProgress()}, or {@code -1} otherwise
     */
    double getPercentProgress();

    /**
     * @param outputFunction
     *         the output function for which we want the information
     * @return whether the case has arrays values that can be displayed as sparline
     */
    boolean hasSparkLine(final String outputFunction);

    /**
     * @param outputFunction
     *         the output function for which we want the information
     * @return the complete list of values for sparkline
     */
    List<List<Number>> getSparkValues(final String outputFunction);

    /**
     * @param outputFunction
     *         the output function for which we want the information
     * @return the abscissa values for complex sparkline
     */
    List<Number> getSparkAbscissa(final String outputFunction);

}