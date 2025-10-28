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
package org.asnr.funz.data.model.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.asnr.funz.data.model.DoeCaseResults;
import org.funz.doeplugin.DesignSession;

/**
 * Stores results from a DOE case
 *
 * @author Arnaud Trouche - ARTENUM SARL
 */
public class DefaultDoeCase implements DoeCaseResults {

    private final DesignSession designSession;
    private final Map<String, String> outputs;

    /**
     * Constructs a new {@link DoeCaseResults}
     *
     * @param session
     *         the design session
     */
    public DefaultDoeCase(final DesignSession session) {
        super();
        this.designSession = session;
        this.outputs = DefaultDoeCase.extractResults(session.getAnalysis());
    }

    @Override
    public String getValueForVariable(final String variableName) {
        final Object value = this.designSession.getFixedParameters().get(variableName);
        if (value != null) {
            return String.valueOf(value);
        }
        return "?";
    }

    @Override
    public String getHtml() {
        String result = "";
        for (final Entry<String, String> e : this.outputs.entrySet()) {
            if (e.getKey().startsWith("HTML")) {
                result = e.getValue();
            }
        }
        return result;
    }

    @Override
    public String getStringResult(final String selectedOutput) {
        return this.outputs.get(selectedOutput);
    }

    private static Map<String, String> extractResults(final String caseResult) {
        final Map<String, String> results = new HashMap<>();
        if (caseResult == null || caseResult.isEmpty()) {
            return results;
        }

        // The following map associates each type (for example <HTML name='ABC'>content</HTML>) to a list of the
        // content for this type for each case
        final Pattern splitter = Pattern.compile("^<(.+?)>(.++)$");

        final String[] tags = caseResult.replace("\n", "").split("</\\w++>");

        for (final String typeString : tags) {
            final Matcher matcher = splitter.matcher(typeString.trim());
            if (matcher.find()) {
                results.put(matcher.group(1), matcher.group(2));
            }
        }
        return results;
    }

    /**
     * @return the whole DOE outputs
     */
    public Map<String, String> getOutputs() {
        return this.outputs;
    }
}
