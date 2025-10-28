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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FileUtils;
import org.asnr.funz.data.i18n.ResultsDictionary;
import org.asnr.funz.data.model.ResultModel;
import org.asnr.funz.data.model.impl.DefaultResultModel;
import org.asnr.funz.model.listeners.EntryDataListener;
import org.asnr.funz.model.listeners.FunzProjectStateListener;
import org.asnr.funz.model.listeners.ProjectSavedListener;
import org.asnr.funz.model.state.WholeProjectReady;
import org.funz.Project;
import org.funz.doeplugin.Design;
import org.funz.parameter.Case;
import org.funz.parameter.CaseList;
import org.funz.parameter.VarGroup;
import org.funz.parameter.Variable;
import org.funz.parameter.VariableMethods.Value;
import org.funz.util.Format;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javafx.application.Platform;

/**
 * Overrides the default {@link Project} to improve the listener mechanism.
 *
 * @author Arnaud TROUCHE - Artenum SARL
 */
public class ExtendedProject extends Project implements Case.Observer, Design.Observer {

    private static final Logger log = LoggerFactory.getLogger(ExtendedProject.class);

    /**
     * Represents if the entry data of the project is correctly set.
     */
    private final WholeProjectReady currentReadiness;

    /**
     * Represents the {@link ExtendedProjectState} of the project.
     */
    private ExtendedProjectState currentStatus = ExtendedProjectState.INITIAL;

    /**
     * Listeners on entry data (model/design).
     */
    private final List<EntryDataListener> entryDataListeners = new CopyOnWriteArrayList<>();

    /**
     * Listeners on project results.
     */
    private final List<FunzProjectStateListener> statusListeners = new CopyOnWriteArrayList<>();

    /**
     * Listeners on save action.
     */
    private final List<ProjectSavedListener> saveListeners = new CopyOnWriteArrayList<>();

    /**
     * Listeners on the design (useful for DOE).
     */
    private final List<Design.Observer> designListeners = new CopyOnWriteArrayList<>();

    /**
     * Listeners on the project cases.
     */
    private final List<Case.Observer> caseListeners = new CopyOnWriteArrayList<>();

    private final ResultModel resultModel;

    /**
     * @param file
     *         the input file to use to create the project
     */
    public ExtendedProject(final File file) {
        super(file);
        this.currentReadiness = new WholeProjectReady(this);
        this.resultModel = new DefaultResultModel(this);
    }

    /**
     * @param name
     *         the name of the plugin to associate to the project
     */
    public ExtendedProject(final String name) {
        super(name);
        this.currentReadiness = new WholeProjectReady(this);
        this.resultModel = new DefaultResultModel(this);
    }

    /**
     * @param prj
     *         the existing project
     */
    public ExtendedProject(final Project prj) {
        super(new File(Project.getDirectoryForProject(prj.getName()) + File.separator + Project.PROJECT_FILE));
        this.currentReadiness = new WholeProjectReady(this);
        this.resultModel = new DefaultResultModel(this);
    }

    /**
     * @deprecated use {@link #fireEntryDataModified()}
     */
    @Override
    @Deprecated(since = "V1")
    public void modified() {
        this.fireEntryDataModified();
    }

    /**
     * @return {@code true} if the project is ready for calculations, {@code false otherwise}
     */
    public WholeProjectReady projectReadyObserver() {
        return this.currentReadiness;
    }

    /**
     * @return the current {@link ExtendedProjectState} of this {@link ExtendedProject}.
     */
    public ExtendedProjectState getProjectState() {
        return this.currentStatus;
    }

    /**
     * @return the resultModel {@link ResultModel} for this {@link Project}.
     */
    public ResultModel getResultModel() {
        return this.resultModel;
    }

    @Override
    public void saveInSpool() {
        try {
            super.saveInSpool();
            for (final ProjectSavedListener listeners : this.saveListeners) {
                listeners.projectSaved(this);
            }
        } catch (final Exception e) {
            ExtendedProject.log.error(e.getMessage(), e);
        }

    }

    /**
     * Adds a
     *
     * @param listener
     *         {@link EntryDataListener}
     */
    public void addEntryDataListener(final EntryDataListener listener) {
        this.entryDataListeners.add(listener);
    }

    /**
     * Removes a
     *
     * @param listener
     *         {@link EntryDataListener}
     */
    public void removeEntryDataListener(final EntryDataListener listener) {
        this.entryDataListeners.remove(listener);
    }

    /**
     * @return the output names synchronised with the actual output functions list and not the output of the plugin
     */
    public String[] getSyncOutputNames() {
        return this.getOutputFunctionsList().stream().map(ofe -> ofe.toNiceSymbolicString().replace("'", ""))
                .toArray(String[]::new);
    }

    /**
     * Adds a
     *
     * @param listener
     *         {@link EntryDataListener}
     */
    public void addStatusListener(final FunzProjectStateListener listener) {
        this.statusListeners.add(listener);
    }

    /**
     * Removes a
     *
     * @param listener
     *         {@link EntryDataListener}
     */
    public void removeStatusListener(final FunzProjectStateListener listener) {
        this.statusListeners.remove(listener);
    }

    /**
     * Adds a
     *
     * @param listener
     *         {@link ProjectSavedListener}
     */
    public void addProjectSavedListener(final ProjectSavedListener listener) {
        this.saveListeners.add(listener);
    }

    /**
     * Removes a
     *
     * @param listener
     *         {@link ProjectSavedListener}
     */
    public void removeProjectSavedListener(final ProjectSavedListener listener) {
        this.saveListeners.remove(listener);
    }

    /**
     * Adds a
     *
     * @param listener
     *         {@link Design.Observer}
     */
    public void addDesignUpdatedListener(final Design.Observer listener) {
        this.designListeners.add(listener);
    }

    /**
     * Removes a
     *
     * @param listener
     *         {@link Design.Observer}
     */
    public void removeDesignUpdatedListener(final Design.Observer listener) {
        this.designListeners.remove(listener);
    }

    /**
     * Adds a
     *
     * @param listener
     *         {@link Case.Observer}
     */
    public void addCaseModifiedListener(final Case.Observer listener) {
        this.caseListeners.add(listener);
    }

    /**
     * Removes a
     *
     * @param listener
     *         {@link Case.Observer}
     */
    public void removeCaseModifiedListener(final Case.Observer listener) {
        this.caseListeners.remove(listener);
    }

    @Override
    public void designUpdated(final int numCases) {
        for (final Design.Observer listener : this.designListeners) {
            listener.designUpdated(numCases);
        }
    }

    @Override
    public void caseModified(final int index, final int what) {
        for (final Case.Observer listener : this.caseListeners) {
            listener.caseModified(index, what);
        }
    }

    @Override
    public void cleanParameters() {
        super.cleanParameters();

        // Add the default value to the list of values if it is empty
        for (final Variable variable : this.getVariables()) {
            if (variable.getValues().isEmpty() && (variable.getDefaultValue() != null)) {
                final Value value = new Value(variable.getDefaultValue());
                variable.getValues().add(value);
            }
        }
    }

    /**
     * Parse for output functions, <b>Beware, it will remove all user-defined output functions and change the main
     * output function</b>.
     */
    public void parseOutputFunctions() {
        this.cleanOutputs();
        this.cleanOutputFunctions();
    }

    /**
     * Automatically select the first output function.
     */
    public void autoSelectOutputFunction() {
        // We set the first current OutputFunctions from project as the mainOutputFunction to have one line selected at
        // initialization, if there not already a main output selected
        if (!this.getOutputFunctionsList().isEmpty() && (this.getMainOutputFunction() == null)) {
            this.setMainOutputFunction(this.getOutputFunctionsList().get(0));
        }
    }

    /**
     * Notify listeners that the entry data has been modified.
     */
    public void fireEntryDataModified() {
        if (!this.currentStatus.equals(ExtendedProjectState.INITIAL)) {
            this.fireInitialState();
        }

        this.autoSelectOutputFunction();

        Platform.runLater(() -> {
            for (final EntryDataListener listener : this.entryDataListeners) {
                listener.entryDataModified(this);
            }
        });

        // As data is modified, we save the project
        try {
            this.saveInSpool();
        } catch (final Exception e) {
            ExtendedProject.log.error(ResultsDictionary.ERROR_WHILE_SAVING_PROJECT.getString(e.getMessage()), e);
        }
    }

    /**
     * Notify listeners that the project is not ready for calculations.
     */
    public void fireInitialState() {
        this.changeStatus(ExtendedProjectState.INITIAL);
    }

    /**
     * Notify listeners that the calculations are currently running
     */
    public void fireCalculationsRunning() {
        this.changeStatus(ExtendedProjectState.RUNNING);
    }

    /**
     * Notify listeners that the calculations has failed.
     */
    public void fireCalculationsFailed() {
        this.changeStatus(ExtendedProjectState.FAILED);
    }

    /**
     * Notify listeners that the project has results.
     */
    public void fireResultsSet() {
        this.changeStatus(ExtendedProjectState.RESULTS);
    }

    /**
     * Clear the results of the project, and go back to "ready" state.
     */
    public void clearResults() {
        final File resultsDir = this.getResultsDir();
        if (resultsDir.exists() && (resultsDir.listFiles() != null) && (this.getCases() != null)) {
            final Instant oldCalcInstant;
            if (!this.getCases().isEmpty()) {
                oldCalcInstant = Instant.ofEpochMilli(this.getCases().get(0).getStart());
            } else {
                oldCalcInstant = Instant.now();
            }
            final ZonedDateTime zonedDateTime = oldCalcInstant.atZone(ZoneId.systemDefault());
            final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss-SSS");
            final String oldDirname = formatter.format(zonedDateTime.toLocalDateTime());

            final File targetDirectory = new File(this.getOldResultsDir(), oldDirname);
            targetDirectory.mkdir();
            Arrays.stream(resultsDir.listFiles()).forEach(file -> {
                if (!file.renameTo(new File(targetDirectory, file.getName()))) {
                    ExtendedProject.log.debug("Unable to rename file '{}'", file);
                }
            });

            // Remove cases from project
            this.getCases().clear();

            // we notify that there are no more results
            this.changeStatus(ExtendedProjectState.INITIAL);
        }
    }

    @Override
    public void removeGroup(final VarGroup g) {
        // Re-write removeGroup function that has a bug in Funz: all variable are parsed and remove from their group, so
        // if there are several groups defined, once a group is deleted, al other groups are quietly almost deleted, at
        // least from the variables

        // Here, we do not list all variables but only the one inside the project
        for (final Variable groupVariable : g.getVariables()) {
            groupVariable.setGroup(null);
        }

        this.getGroups().remove(g);
        this.removeParameter(g);
    }

    @Override
    public void loadCases() throws Exception {

        // Re-written loadCases from Project to avoid issue on empty cases
        final File caseFile = new File(this.getResultsDir(), "cases.xml");
        if (caseFile.exists()) {
            final CaseList cases = this.correctlyLoadResults(caseFile);
            this.setCases(cases, this);
            if (!this.getCases().isEmpty()) {
                this.getVoidIntermediate().putAll(this.getCases().get(0).getIntermediateValues());
            }
        }

        // Update the status if necessary
        if ((this.getCases() != null) && !this.getCases().isEmpty() && !this.currentStatus.equals(
                ExtendedProjectState.RESULTS)) {
            this.changeStatus(ExtendedProjectState.RESULTS);
        }
    }

    @Override
    public void loadDesignSessions() throws Exception {
        final File resDsFile = new File(this.getResultsDir(), Project.DESIGN_SESSIONS_FILE);
        final File rootDsFile = new File(this.getDirectory(), Project.DESIGN_SESSIONS_FILE);

        if (resDsFile.exists()) {
            if (!resDsFile.equals(rootDsFile)) {
                // Copy the design file to root
                FileUtils.copyFile(resDsFile, rootDsFile);
            }

            // Now load the sessions
            super.loadDesignSessions();

            // Delete the design file from root
            Files.delete(resDsFile.toPath());
        }
    }

    private CaseList correctlyLoadResults(final File casesFile)
            throws SAXException, IOException, ParserConfigurationException {
        final CaseList cases = new CaseList();

        final DocumentBuilderFactory df = DocumentBuilderFactory.newInstance();
        df.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        df.setExpandEntityReferences(false);
        final Document d = df.newDocumentBuilder().parse(casesFile);
        final Element e = d.getDocumentElement();
        if (!e.getTagName().equals("CASES")) {
            throw new IllegalArgumentException("wrong XML element " + e.getTagName() + " in file " + casesFile);
        }

        final NodeList fileCases = e.getElementsByTagName("CASE");
        for (int caseIndex = 0; caseIndex < fileCases.getLength(); caseIndex++) {
            final Element caseElement = (Element) fileCases.item(caseIndex);
            final Case newCase = new Case(caseElement, this);

            // Get the output results
            final NodeList outputNodes = caseElement.getElementsByTagName("OUTPUT");
            final Map<String, Object> map = new HashMap<>();
            for (int index = 0; index < outputNodes.getLength(); index++) {
                final Element n = (Element) outputNodes.item(index);
                final Object o = org.funz.util.Data.asObject(Format.fromHTML(n.getTextContent()));
                map.put(n.getAttribute("name"), o);
            }

            // Put the result into the correct fields
            newCase.setOutputValues(map);
            newCase.setResult(map);

            cases.add(newCase);
        }

        return cases;
    }

    private void changeStatus(final ExtendedProjectState newStatus) {
        if (!this.currentStatus.equals(newStatus)) {
            this.currentStatus = newStatus;
            Platform.runLater(() -> {
                for (final FunzProjectStateListener listener : this.statusListeners) {
                    listener.statusChanged(this, newStatus);
                }
            });
        }
    }
}
