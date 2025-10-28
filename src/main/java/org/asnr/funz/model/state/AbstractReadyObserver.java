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
import org.asnr.funz.model.listeners.EntryDataListener;
import org.funz.Project;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Abstract class for calculating the status of a given {@link Project}.
 *
 * @author Yann Richet - IRSN
 * @author Arnaud TROUCHE - Artenum SARL
 */
abstract sealed class AbstractReadyObserver implements EntryDataListener, ProjectReadyObserver
        permits AlgorithmStatus, InputVariablesObserver, OutputFunctionStatus {

    /**
     * The {@link ExtendedProject} listened by this {@link AbstractReadyObserver}.
     */
    private final ExtendedProject project;

    /**
     * Property indicating if the model is ready or not.
     */
    private final BooleanProperty ready;

    /**
     * Property storing the message about the model status.
     */
    private final StringProperty message;

    /**
     * Creates a new {@link AbstractReadyObserver} that listen to the changes of the given {@link Project}.
     *
     * @param project
     *         {@link Project} to listen
     */
    protected AbstractReadyObserver(final ExtendedProject project) {
        this.project = project;
        this.project.addEntryDataListener(this);

        this.ready = new SimpleBooleanProperty(this, null);
        this.message = new SimpleStringProperty(this, null);
    }

    @Override
    public final void entryDataModified(final ExtendedProject modifiedProject) {
        this.updateStatus();
    }

    @Override
    public final BooleanProperty readyProperty() {
        return this.ready;
    }

    @Override
    public final StringProperty messageProperty() {
        return this.message;
    }

    /**
     * @return the project
     */
    protected final ExtendedProject getProject() {
        return this.project;
    }

    /**
     * @param newReadiness
     *         whether the model is ready or not
     */
    protected final void setReady(final boolean newReadiness) {
        this.ready.set(newReadiness);
    }

    /**
     * @param message
     *         the new message of this {@link AbstractReadyObserver}
     */
    protected final void setMessage(final String message) {
        this.message.set(message);
    }
}
