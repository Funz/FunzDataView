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

/**
 * {@link AbstractReadyObserver} that indicates the status of the output function.
 *
 * @author Arnaud TROUCHE - Artenum SARL
 */
final class OutputFunctionStatus extends AbstractReadyObserver {

    /**
     * @param project
     *         the project to observe
     */
    OutputFunctionStatus(final ExtendedProject project) {
        super(project);
    }

    @Override
    public void updateStatus() {
        if (this.getProject().checkOutputFunctionIsValid() != null) {
            this.setReady(false);

            if (this.getProject().getMainOutputFunction() == null) {
                this.setMessage(ResultsDictionary.OUTPUT_FUNCTION_STATUS_NO_OUTPUT_FUNCTION_SELECTED.getString());
            } else {
                final StringBuilder message = new StringBuilder(this.getProject().checkOutputFunctionIsValid());
                final String functionName = this.getProject().getMainOutputFunction().toNiceSymbolicString();
                final StringBuilder paramNotSet = new StringBuilder();
                for (int i = 0; i < this.getProject().getMainOutputFunction().parametersExpression.length; i++) {
                    if (this.getProject().getMainOutputFunction().parametersExpression[i].isEmpty()) {
                        paramNotSet.append(" ");
                        paramNotSet.append(this.getProject().getMainOutputFunction().parametersNames[i]);
                    }
                }
                message.append(ResultsDictionary.OUTPUT_FUNCTION_STATUS_INVALID_OUTPUT_FUNCTION.getString(functionName,
                        paramNotSet.toString()));
                this.setMessage(message.toString());
            }
        } else {
            this.setReady(true);
            this.setMessage(ResultsDictionary.MODEL_STATUS_OK.getString());
        }
    }

}
