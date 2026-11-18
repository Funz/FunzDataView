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
package org.asnr.funz.data.view.common;

import java.util.List;
import java.util.function.Supplier;

import org.asnr.funz.data.model.DiscreteCaseResults;
import org.funz.parameter.Case;

import com.artenum.tk.ui.chart.Sparkline;

import javafx.scene.control.ProgressBar;
import javafx.scene.control.cell.TextFieldTableCell;

/**
 * {@link TextFieldTableCell} overridden to apply CSS classes to enlighten the status.
 *
 * @param <T>
 *         the type of data in the list, usually {@link Case}
 * @author Arnaud TROUCHE - Artenum SARL
 */
public abstract class ResultCaseTableCell<T> extends TextFieldTableCell<T, DiscreteCaseResults> {

    private final ProgressBar progressBar;

    private final Supplier<String> outputFunction;

    /**
     * Default constructor.
     *
     * @param outputFunctionSupplier
     *         the supplier indicating the output function to show
     */
    protected ResultCaseTableCell(final Supplier<String> outputFunctionSupplier) {
        this.outputFunction = outputFunctionSupplier;
        this.progressBar = new ProgressBar();
    }

    @Override
    public void updateItem(final DiscreteCaseResults result, final boolean empty) {
        super.updateItem(result, empty);
        // the cell is reused for other rows: nothing of the previous one must stay
        this.setGraphic(null);
        final String selectedOutputFunction = this.outputFunction.get();
        final String toShow = (empty || (result == null)) ? null : result.getStringResult(selectedOutputFunction);
        if (toShow == null) {
            this.setText(null);
            return;
        }
        this.setText(this.computeTextToShow(toShow, selectedOutputFunction));
        if (result.hasPercentProgress()) {
            this.progressBar.setProgress(result.getPercentProgress());
            this.setGraphic(this.progressBar);
        }
        if (result.hasCountProgress()) {
            this.progressBar.setProgress(result.getCountProgress());
            this.setGraphic(this.progressBar);
        }
        if (result.hasSparkLine(selectedOutputFunction)) {
            this.sparkline(result);
        }
        this.computeAdditionalElements(result);
    }

    private void sparkline(final DiscreteCaseResults result) {
        this.setText(null);
        final String output = this.outputFunction.get();
        final List<Number> abscissa = result.getSparkAbscissa(output);
        if (abscissa.isEmpty()) {
            final Sparkline spark = new Sparkline(result.getSparkValues(output));
            this.setGraphic(spark);
        } else {
            final Sparkline spark = new Sparkline(abscissa, result.getSparkValues(output));
            this.setGraphic(spark);
        }
    }

    /**
     * @return the main output fucntion for this cell
     */
    protected final Supplier<String> getMainOutput() {
        return this.outputFunction;
    }

    /**
     * @param outputResult
     *         the result for this cell
     * @return the text to display extracted from the result
     */
    protected abstract String computeTextToShow(String outputResult);

    /**
     * @param outputResult
     *         the result for this cell
     * @param outputFunction
     *         the output function of the associated result
     * @return the text to display extracted from the result
     */
    protected abstract String computeTextToShow(String outputResult, String outputFunction);

    /**
     * @param result
     *         all the results to display additional elements
     */
    protected abstract void computeAdditionalElements(DiscreteCaseResults result);
}