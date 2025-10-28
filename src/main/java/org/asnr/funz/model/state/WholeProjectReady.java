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
package org.asnr.funz.model.state;

import org.asnr.funz.model.ExtendedProject;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.StringExpression;
import javafx.beans.value.ObservableBooleanValue;
import javafx.beans.value.ObservableStringValue;

/**
 * Represents a project is correctly set (= ready for calculations) or not.
 *
 * @author Arnaud TROUCHE - Artenum SARL
 */
public final class WholeProjectReady implements ProjectReadyObserver {

    private final InputVariablesObserver input;
    private final OutputFunctionStatus output;
    private final AlgorithmStatus algorithm;

    private final BooleanBinding readyProperty;
    private final StringExpression messageProperty;

    /**
     * @param project
     *         {@link ExtendedProject}
     */
    public WholeProjectReady(final ExtendedProject project) {
        this.input = new InputVariablesObserver(project);
        this.output = new OutputFunctionStatus(project);
        this.algorithm = new AlgorithmStatus(project);

        this.readyProperty = Bindings.and(this.input.readyProperty(), this.output.readyProperty())
                .and(this.algorithm.readyProperty());
        this.messageProperty = Bindings.concat(this.input.messageProperty(), this.output.messageProperty(),
                this.algorithm.messageProperty());
    }

    @Override
    public ObservableBooleanValue readyProperty() {
        return this.readyProperty;
    }

    @Override
    public ObservableStringValue messageProperty() {
        return this.messageProperty;
    }

    /**
     * Force the refresh of the status.
     */
    @Override
    public void updateStatus() {
        this.input.updateStatus();
        this.output.updateStatus();
        this.algorithm.updateStatus();
    }

    /**
     * @return the sub-observer for the input variables
     */
    public ProjectReadyObserver input() {
        return this.input;
    }

    /**
     * @return the sub-observer for the output functions
     */
    public ProjectReadyObserver output() {
        return this.output;
    }

    /**
     * @return the sub-observer for the algorihtm
     */
    public ProjectReadyObserver algorithm() {
        return this.algorithm;
    }
}
