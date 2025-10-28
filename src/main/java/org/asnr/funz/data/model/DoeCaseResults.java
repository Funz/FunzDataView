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

/**
 * Represents a case with DOE.
 *
 * @author Arnaud Trouche - ARTENUM SARL
 */
public non-sealed interface DoeCaseResults extends CaseResults {

    /**
     * @return the HTML of the case
     */
    String getHtml();

    @Override
    default Object getResult(final String outputFunction) {
        return this.getStringResult(outputFunction);
    }

}
