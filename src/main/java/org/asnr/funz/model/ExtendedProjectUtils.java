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

import java.util.LinkedList;
import java.util.List;

import org.funz.Project;
import org.funz.doeplugin.DesignConstants;
import org.funz.doeplugin.DesignPluginsLoader;
import org.funz.parameter.Parameter;
import org.funz.parameter.VarGroup;
import org.funz.parameter.Variable;

/**
 * Utiliy methods for {@link ExtendedProject}.
 *
 * @author Arnaud Trouche - Artenum SARL
 */
public final class ExtendedProjectUtils {

    /**
     * @param project
     *         the Funz project
     * @return the name of the result.
     */
    public static String getResultName(final Project project) {
        if (project.getDesignerId().equals(DesignConstants.NODESIGNER_ID)) {
            if (project.getMainOutputFunction() != null) {
                return project.getMainOutputFunction().toNiceSymbolicString();
            } else {
                return project.getMainOutputFunctionName();
            }
        } else {
            if (project.getDesigner() == null) {
                project.setDesigner(DesignPluginsLoader.newInstance(project.getDesignerId()));
            }
            return project.getDesigner().getDesignOutputTitle();
        }
    }

    /**
     * Ungroup the discrete variables from the given {@link ExtendedProject}.
     *
     * @param project
     *         {@link Project} of which we want the discrete variables
     * @return the {@link List} of discrete {@link Variable}s from the given {@link Project}
     */
    public static List<Variable> getDiscreteVariablesUngrouped(final Project project) {
        final List<Variable> vars = new LinkedList<>();
        for (final Parameter parameter : project.getDiscreteParameters()) {
            if (parameter.isGroup()) {
                vars.addAll(((VarGroup) parameter).getVariables());
            } else {
                vars.add((Variable) parameter);
            }
        }
        return vars;
    }

    /**
     * Ungroup the continuous variables from the given {@link ExtendedProject}.
     *
     * @param project
     *         {@link Project} of which we want the continuous variables
     * @return the {@link List} of discrete {@link Variable}s from the given {@link Project}
     */
    public static List<Variable> getContinuousVariablesUngrouped(final Project project) {
        final List<Variable> vars = new LinkedList<>();
        for (final Parameter parameter : project.getContinuousParameters()) {
            if (parameter.isGroup()) {
                vars.addAll(((VarGroup) parameter).getVariables());
            } else {
                vars.add((Variable) parameter);
            }
        }
        return vars;
    }

    private ExtendedProjectUtils() {
        throw new AssertionError();
    }

}
