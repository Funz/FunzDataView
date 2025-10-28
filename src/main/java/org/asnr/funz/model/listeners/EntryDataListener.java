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

/**
 * Listener interface to be notified of a change on entry data (files, variables, design, ...).
 *
 * @author Arnaud TROUCHE - Artenum SARL
 */
@FunctionalInterface
public interface EntryDataListener {

    /**
     * Inside the given {@link ExtendedProject}, the entry data (files, variables, design, ...) has changed.
     *
     * @param modifiedProject
     *         the modified {@link ExtendedProject}
     */
    void entryDataModified(ExtendedProject modifiedProject);

}