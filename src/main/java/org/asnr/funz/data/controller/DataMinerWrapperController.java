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
package org.asnr.funz.data.controller;

import java.net.URL;
import java.util.ResourceBundle;

import org.asnr.funz.data.dataminer.DataMiner;
import org.asnr.funz.data.i18n.ResultsDictionary;

import com.artenum.tk.ui.util.OsgiFxmlLoader;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.layout.StackPane;

/**
 * Result graphical renderer interface.
 *
 * @author Arnaud TROUCHE - Artenum SARL
 */
final class DataMinerWrapperController extends AbstractDynamicUpdateController implements Initializable {

    /**
     * {@link DataMiner}
     */
    private final DataMiner dataMiner;

    /**
     * The complete {@link Tab} view of this {@link DataMiner}.
     */
    private final Tab view;

    /**
     * Placeholder indicating that there is no data to show.
     */
    private final Label placeHolder;

    @FXML
    private StackPane rendererContainer;
    @FXML
    private CheckBox alwaysUpdateCheckbox;

    /**
     * Creates a new {@link DataMinerWrapperController} that controls and interacts with the given {@link DataMiner}.
     *
     * @param dataMiner
     *         {@link DataMiner}
     */
    public DataMinerWrapperController(final DataMiner dataMiner) {
        this.dataMiner = dataMiner;
        this.view = new Tab(this.dataMiner.getName());
        this.placeHolder = new Label(ResultsDictionary.NO_RESULT_MESSAGE.getString());
        this.view.setContent(OsgiFxmlLoader.getRoot(this, "DataMinerWrapper", ResultsDictionary.class));

        super.setCallback(() -> this.refresh(false));
        // Show TAB content only when selected, should improve performances
        this.view.selectedProperty().addListener((o, wasSelected, isSelected) -> {
            this.rendererContainer.getChildren().clear();
            if (!wasSelected.booleanValue() && isSelected.booleanValue()) {
                this.refresh(false);
            }
        });
    }

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        this.refresh(false);
    }

    /**
     * @return the dataMiner
     */
    DataMiner getDataMiner() {
        return this.dataMiner;
    }

    /**
     * @return the tab hosting the data miner
     */
    Tab getTab() {
        return this.view;
    }

    /**
     * Refresh the content of the Result Renderer.
     *
     * @param liveUpdate
     *         if {@code true} this refresh is a "live" refresh and should only be executed if the checkbox is
     *         checked
     */
    void refresh(final boolean liveUpdate) {
        if (this.view.isSelected() && this.isShowing()) {
            if (!this.dataMiner.canDisplayData()) {
                this.dataMiner.clear();
                Platform.runLater(() -> this.rendererContainer.getChildren().setAll(this.placeHolder));
            } else if (!liveUpdate || this.alwaysUpdateCheckbox.isSelected()) {
                this.dataMiner.refreshVariables();
                this.dataMiner.refreshCases();
                Platform.runLater(() -> this.rendererContainer.getChildren().setAll(this.dataMiner.getView()));
            }
        } else {
            this.needUpdate();
        }
    }

    /**
     * Called when the user clicks on the update button
     */
    @FXML
    private void updateNowButtonPressed() {
        this.refresh(false);
    }

}
