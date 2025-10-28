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
package org.asnr.funz.model;

/**
 * Enumeration indicating the status of the project.
 *
 * @author Arnaud TROUCHE - Artenum SARL
 */
public enum ExtendedProjectState {

    /**
     * Project is set up but has not run.
     */
    INITIAL,

    /**
     * Project is currently running.
     */
    RUNNING,

    /**
     * Project run has failed.
     */
    FAILED,

    /**
     * Project has run and has results.
     */
    RESULTS

}