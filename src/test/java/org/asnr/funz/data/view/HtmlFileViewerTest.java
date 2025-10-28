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
package org.asnr.funz.data.view;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for {@link HtmlFileViewer}.
 *
 * @author Arnaud Trouche - Artenum SARL
 **/
public class HtmlFileViewerTest {

    /**
     * Test various file content load, and especially the check of binary or not
     *
     * @throws IOException
     *         if error
     * @throws URISyntaxException
     *         if error
     */
    @Test
    public void loadFileContent() throws URISyntaxException, IOException {
        final URL textUrl = this.getClass().getResource("/text.txt");
        Assert.assertNotNull(textUrl);

        final String textContent = HtmlFileViewer.loadFileContent(new File(textUrl.toURI()));
        Assert.assertEquals("This is a test file.", textContent);

        final URL binaryUrl = this.getClass().getResource("/icon.png");
        Assert.assertNotNull(binaryUrl);

        // Check no exception is thrown
        HtmlFileViewer.loadFileContent(new File(binaryUrl.toURI()));
    }
}