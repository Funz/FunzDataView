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
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

import org.asnr.funz.data.dataminer.TableDataMiner;
import org.asnr.funz.data.model.CaseResults;
import org.asnr.funz.data.model.DiscreteCaseResults;
import org.asnr.funz.data.view.common.ResultCaseTableCell;
import org.asnr.funz.data.view.utils.TableViewUtils;

/**
 * This table cell display result value, but if the result is not found it will search in other
 * DiscreteCaseResults if the value of the given output name can be found
 *
 * @author Nicolas Chabalier
 */
public class MultipleResultTableCell extends ResultCaseTableCell<CaseResults> {

    private final Function<DiscreteCaseResults, List<DiscreteCaseResults>> similarResults;
    private final Map<String, String> columnsFormat;

    /**
     * @param outputFunctionName
     *         name of the output function of the column
     * @param results
     *         the results where a missing value is searched, in the cases with the same variable values
     * @param columnsFormat
     *         the formats of the values, by column
     * @deprecated searches at each display of the cell: give the search of {@link TableDataMiner}
     */
    @Deprecated
    public MultipleResultTableCell(final String outputFunctionName, final Collection<DiscreteCaseResults> results,
            final Map<String, String> columnsFormat) {
        this(outputFunctionName, result -> results == null ?
                List.of() :
                List.copyOf(TableDataMiner.getSimilarResults(result, results)), columnsFormat);
    }

    /**
     * @param outputFunctionName
     *         name of the output function of the column
     * @param similarResults
     *         the results of the other cases with the same variable values, where a missing value is searched
     * @param columnsFormat
     *         the formats of the values, by column
     */
    public MultipleResultTableCell(final String outputFunctionName,
            final Function<DiscreteCaseResults, List<DiscreteCaseResults>> similarResults,
            final Map<String, String> columnsFormat) {
        super(() -> outputFunctionName);
        this.similarResults = similarResults;
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
        final boolean missing = !empty && (result != null) && MultipleResultTableCell.isMissing(this.getText());
        TableViewUtils.setStyleClass(this, "highlighted-table-cell2", missing);
        if (missing) {
            // the value of another project, with the same variable values
            for (final DiscreteCaseResults otherResults : this.similarResults.apply(result)) {
                super.updateItem(otherResults, false);
                if (!MultipleResultTableCell.isMissing(this.getText())) {
                    return;
                }
            }
            super.updateItem(result, false);
        }
    }

    private static boolean isMissing(final String text) {
        return (text == null) || text.equals("?");
    }
}
