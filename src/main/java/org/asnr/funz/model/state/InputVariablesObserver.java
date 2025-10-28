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
import org.funz.parameter.Variable;

/**
 * {@link AbstractReadyObserver} that indicates the status of the input variables.
 *
 * @author Arnaud TROUCHE - Artenum SARL
 */
final class InputVariablesObserver extends AbstractReadyObserver {

    /**
     * @param project
     *         the project to observe
     */
    InputVariablesObserver(final ExtendedProject project) {
        super(project);
    }

    @Override
    public void updateStatus() {
        if (this.getProject().getNmOfDiscreteCases(true) <= 0) {
            this.setReady(false);
        }

        if (this.getProject().checkVariablesAreValid() != null) {
            this.setReady(false);
            final StringBuilder message = new StringBuilder(
                    ResultsDictionary.INPUT_VARIABLES_STATUS_INVALID_VARIABLES.getString());
            for (final Variable v : this.getProject().getVariables()) {
                if (v.checkValid() != null) {
                    message.append(" ");
                    message.append(v.getName());
                }
            }
            this.setMessage(message.toString());
        } else {
            this.setReady(true);
            if (this.getProject().getNmOfDiscreteCases(true) > 1) {
                this.setMessage(this.getProject().getNmOfDiscreteCases(true)
                        + ResultsDictionary.INPUT_VARIABLES_STATUS_INDEPENDENT_CASES.getString());
            } else {
                this.setMessage(ResultsDictionary.MODEL_STATUS_OK.getString());
            }
        }
    }

}
