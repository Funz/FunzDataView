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
package org.asnr.funz.data.view.common;

import org.asnr.funz.data.i18n.ResultsDictionary;
import org.keridwen.modelling.ace.text.editor.SuperTextEditor;

import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;

/**
 * Displays a {@link TextField} and {@link Button}s to search inside a {@link SuperTextEditor} element.
 *
 * @author Arnaud TROUCHE - Artenum SARL
 */
public final class SearchBar extends HBox {

    /**
     * {@link SuperTextEditor} in which the search should be done.
     */
    private final SuperTextEditor textEditor;

    /**
     * The text field to search text.
     */
    private final TextField searchTextField;

    /**
     * Represents the last searched text.
     */
    private String lastSearch;

    /**
     * Creates an {@link HBox} with a {@link TextField} and previous and next {@link Button}s.
     *
     * @param editor
     *         {@link SuperTextEditor} element in which execute the search.
     */
    public SearchBar(final SuperTextEditor editor) {
        this.textEditor = editor;

        this.searchTextField = new TextField();
        this.searchTextField.setOnKeyReleased(e -> {
            if (e.getCode().equals(KeyCode.ENTER)) {
                this.search();
            }
        });

        final Button prev = new Button(null, new ImageView(ResultsDictionary.LEFT_16.getPicture()));
        prev.setTooltip(new Tooltip(ResultsDictionary.SEARCH_BAR_NEXT_TOOLTIP.getString()));
        prev.setOnAction(e -> this.previous());

        final Button next = new Button(null, new ImageView(ResultsDictionary.RIGHT_16.getPicture()));
        next.setTooltip(new Tooltip(ResultsDictionary.SEARCH_BAR_PREVIOUS_TOOLTIP.getString()));
        next.setOnAction(e -> this.next());

        this.setSpacing(5);
        this.getChildren().addAll(this.searchTextField, prev, next);
    }

    /**
     * Searches for the given text inside the editor.
     */
    public void search() {
        this.search(this.getSearchText());
    }

    /**
     * Searches for the given text inside the editor.
     *
     * @param textToSearch
     *         the text to search
     */
    public void search(final String textToSearch) {
        this.search(textToSearch, false);
    }

    /**
     * Searches for the given text inside the editor.
     *
     * @param textToSearch
     *         the text to search
     * @param boolOp
     *         if <code>true</code> search using reg-exp and regular text if <code>false</code>
     */
    public void search(final String textToSearch, final boolean boolOp) {
        this.lastSearch = textToSearch;
        this.textEditor.find(this.lastSearch, boolOp, false, true, false, false);
    }

    /**
     * @return the {@link String} we are looking for.
     */
    public String getSearchText() {
        return this.searchTextField.getText();
    }

    /**
     * If there is already a result and a search, searches for the next occurrence of the search.
     */
    public void next() {
        final String searchText = this.getSearchText();
        if (searchText.equals(this.lastSearch)) {
            this.textEditor.findNext();
        } else {
            this.search();
        }
    }

    /**
     * If there is already a result and a search, searches for the previous occurrence of the search.
     */
    public void previous() {
        final String searchText = this.getSearchText();
        if (searchText.equals(this.lastSearch)) {
            this.textEditor.findPrevious();
        } else {
            this.search();
        }
    }

}
