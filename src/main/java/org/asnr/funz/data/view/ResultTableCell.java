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
import org.asnr.funz.data.model.DiscreteCaseResults;
import org.asnr.funz.data.view.common.ResultCaseTableCell;

/**
 * TableCell for results.
 *
 * @author Arnaud Trouche - Artenum SARL
 */
public class ResultTableCell extends ResultCaseTableCell<CaseResults> {

    /**
     * @param outputFunctionName
     *         name of the output function of the column
     */
    public ResultTableCell(final String outputFunctionName) {
        super(() -> outputFunctionName);
    }

    @Override
    protected String computeTextToShow(final String outputResult) {
        // No filter, display directly
        return outputResult;
    }

    @Override
    protected void computeAdditionalElements(final DiscreteCaseResults results) {
        // Nothing to do
    }

    @Override
    protected String computeTextToShow(final String outputResult, final String outputFunction) {
        return this.computeTextToShow(outputResult);
    }

}
