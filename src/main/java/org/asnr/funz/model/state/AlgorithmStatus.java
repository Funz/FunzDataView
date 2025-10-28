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

import org.asnr.funz.data.i18n.ResultsDictionary;
import org.asnr.funz.model.ExtendedProject;
import org.funz.doeplugin.DesignConstants;
import org.funz.doeplugin.Designer;

/**
 * {@link AbstractReadyObserver} that indicates the status of the algorithm.
 *
 * @author Arnaud TROUCHE - Artenum SARL
 */
final class AlgorithmStatus extends AbstractReadyObserver {

    /**
     * @param project
     *         the project to observe
     */
    AlgorithmStatus(final ExtendedProject project) {
        super(project);
    }

    @Override
    public void updateStatus() {
        if (this.getProject().getDesigner() == null) {
            // If no designer chosen

            this.setReady(false);
            this.setMessage(ResultsDictionary.ALGORITHM_STATUS_NO_ISSUE_DEFINED.getString());
        } else if (!this.getProject().getDesignerId().equals(DesignConstants.NODESIGNER_ID)) {
            // If an existing designer is chosen, we check the validity

            String isValid;
            try {
                isValid = this.getProject().getDesigner().isValid(this.getProject().getContinuousParameters(),
                        this.getProject().getMainOutputFunction());
            } catch (final Exception e) {
                isValid = e.getMessage();
            }

            if (isValid.equals(Designer.VALID)) {
                this.setReady(true);
                this.setMessage(ResultsDictionary.MODEL_STATUS_OK.getString());
            } else {
                this.setReady(false);
                this.setMessage(this.getProject().getDesignerId() + " " + isValid);
            }

        } else {
            // If the no designer option is chosen, we check that there is no continuous variables

            if ((this.getProject().getContinuousParameters() != null) && (!this.getProject().getContinuousParameters()
                    .isEmpty())) {
                this.setReady(false);
                this.setMessage(ResultsDictionary.ALGORITHM_STATUS_NO_DESIGN_NEEDS_VARIABLES.getString());
            } else {
                this.setReady(true);
                this.setMessage(ResultsDictionary.MODEL_STATUS_OK.getString());
            }
        }
    }

}
