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

import org.funz.Project;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.beans.value.ObservableStringValue;

/**
 * Represent elements able to compute the readiness state of a given {@link Project} or sub-part of project input
 * elements.
 *
 * @author Arnaud Trouche - Artenum SARL
 */
public sealed interface ProjectReadyObserver permits AbstractReadyObserver, WholeProjectReady {

    /**
     * @return the {@link BooleanProperty} indicating the status of the model
     */
    ObservableBooleanValue readyProperty();

    /**
     * @return {@code true} if the model is ready, {@code false} otherwise
     */
    default boolean isReady() {
        return this.readyProperty().get();
    }

    /**
     * @return the {@link StringProperty} containing the message about the model status
     */
    ObservableStringValue messageProperty();

    /**
     * @return the message of this {@link ProjectReadyObserver}
     */
    default String getMessage() {
        return this.messageProperty().get();
    }

    /**
     * Update the readiness and the message of this {@link ProjectReadyObserver}.
     */
    void updateStatus();

}
