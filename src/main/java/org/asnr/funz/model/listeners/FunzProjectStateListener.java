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
package org.asnr.funz.model.listeners;

import org.asnr.funz.model.ExtendedProject;
import org.asnr.funz.model.ExtendedProjectState;

/**
 * Listener interface to be notified of project status change.
 *
 * @author Arnaud TROUCHE - Artenum SARL
 */
@FunctionalInterface
public interface FunzProjectStateListener {

    /**
     * The status of the given project has changed.
     *
     * @param modifiedProject
     *         the modified {@link ExtendedProject}
     * @param newStatus
     *         the new {@link ExtendedProjectState} of the project
     */
    void statusChanged(ExtendedProject modifiedProject, ExtendedProjectState newStatus);

}