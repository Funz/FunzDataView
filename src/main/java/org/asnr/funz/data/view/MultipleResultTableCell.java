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

import java.util.Collection;
import java.util.Locale;
import java.util.Map;

import org.asnr.funz.data.dataminer.TableDataMiner;
import org.asnr.funz.data.model.CaseResults;
import org.asnr.funz.data.model.DiscreteCaseResults;
import org.asnr.funz.data.view.common.ResultCaseTableCell;

/**
 * This table cell display result value, but if the result is not found it will search in other
 * DiscreteCaseResults if the value of the given output name can be found
 *
 * @author Nicolas Chabalier
 */
public class MultipleResultTableCell extends ResultCaseTableCell<CaseResults> {

    private final Collection<DiscreteCaseResults> allResults;
    private final Map<String, String> columnsFormat;

    /**
     * @param outputFunctionName
     *         name of the output function of the column
     * @param results
     * @param columnsFormat
     */
    public MultipleResultTableCell(final String outputFunctionName, final Collection<DiscreteCaseResults> results,
            final Map<String, String> columnsFormat) {
        super(() -> outputFunctionName);
        this.allResults = results;
        this.columnsFormat = columnsFormat;
    }

    @Override
    protected String computeTextToShow(final String outputResult) {
        return outputResult;
    }

    @Override
    protected String computeTextToShow(String outputResult, final String outputFunction) {
        if (this.columnsFormat.containsKey(outputFunction)) {
            try {
                final Double valueDouble = Double.parseDouble(outputResult);
                outputResult = String.format(Locale.ENGLISH, this.columnsFormat.get(outputFunction), valueDouble);
            } catch (final NumberFormatException e) {
                //not a double
            }
        }
        return outputResult;
    }

    @Override
    protected void computeAdditionalElements(final DiscreteCaseResults results) {
        // Nothing to do
    }

    @Override
    public void updateItem(final DiscreteCaseResults result, final boolean empty) {
        super.updateItem(result, empty);
        if (!empty && (this.getText() == null || this.getText().equals("?")) && this.allResults != null) {
            this.getStyleClass().add("highlighted-table-cell2");
            for (final DiscreteCaseResults otherResults : TableDataMiner.getSimilarResults(result, this.allResults)) {
                super.updateItem(otherResults, empty);
                if (this.getText() != null && !this.getText().contentEquals("?")) {
                    break;
                }
            }
        } else {
            this.getStyleClass().remove("highlighted-table-cell2");
        }
    }
}
