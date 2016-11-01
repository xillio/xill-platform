/**
 * Copyright (C) 2014 Xillio (support@xillio.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.xillio.migrationtool.gui;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;
import javafx.scene.web.WebView;
import me.biesaart.utils.Log;
import netscape.javascript.JSObject;
import nl.xillio.migrationtool.Loader;
import nl.xillio.migrationtool.XillInitializer;
import nl.xillio.migrationtool.gui.editor.XillJSObject;
import nl.xillio.xill.docgen.DocGenConfiguration;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.w3c.dom.NodeList;
import org.w3c.dom.events.EventTarget;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * This pane contains the documentation information.
 *
 * @author Thomas Biesaart
 * @author Pieter Dirk Soels
 */
public class HelpPane extends AnchorPane {
    @FXML
    private WebView webFunctionDoc;

    @FXML
    private HelpSearchBar helpSearchBar;

    // the logger
    private static final Logger LOGGER = Log.get();

    private File helpDirectory;

    /**
     * Instantiate the HelpPane and load the home page.
     */
    public HelpPane() {

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/HelpPane.fxml"));
        loader.setClassLoader(getClass().getClassLoader());
        loader.setController(this);

        try {
            Node ui = loader.load();
            getChildren().add(ui);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
        helpSearchBar.setHelpPane(this);

        XillInitializer initializer = Loader.getInitializer();
        initializer.getOnLoadComplete().addListener(init -> {
            helpDirectory = initializer.getHelpDirectory();
            displayHome();
            webFunctionDoc.getEngine().getHistory().setMaxSize(0);
            webFunctionDoc.getEngine().getHistory().setMaxSize(100);
            helpSearchBar.setSearcher(init.getSearcher());

        });

        // Add an event listener to <a> tags to load external urls in a browser
        webFunctionDoc.getEngine().getLoadWorker().stateProperty().addListener((observable, o, n) -> {
            if (Worker.State.SUCCEEDED.equals(n)) {
                NodeList nodeList = webFunctionDoc.getEngine().getDocument().getElementsByTagName("a");
                for (int i = 0; i < nodeList.getLength(); i++) {
                    org.w3c.dom.Node node = nodeList.item(i);
                    EventTarget eventTarget = (EventTarget) node;

                    // Add the click event listener
                    eventTarget.addEventListener("click", new UrlClickEventListener(), false);
                }
            }
        });

        // Fill the highlight settings with keywords and built-ins
        webFunctionDoc.getEngine().documentProperty().addListener((observable, o, n) -> {
            // Set the highlight settings
            JSObject window = (JSObject) webFunctionDoc.getEngine().executeScript("window");
            try {
                window.setMember(
                        "xillCoreOverride",
                        new XillJSObject(
                                Loader.getXill().buildProcessor(
                                        Paths.get("."),
                                        Paths.get(".")
                                )
                        )
                );
            } catch (IOException e) {
                LOGGER.error("Failed to create processor for highlighting", e);
            }
            // Load the Ace editors if present
            webFunctionDoc.getEngine().executeScript("if (typeof loadEditors !== 'undefined') {loadEditors()}");

            // Disable context menu
            webFunctionDoc.setContextMenuEnabled(false);
        });

        heightProperty().addListener((observable, oldValue, newValue) -> helpSearchBar.handleHeightChange());

        // load the splash
        File splashFile = null;

        // Create a temporary file for the splash file and register it for deletion
        try {
            splashFile = File.createTempFile("xill_splash", ".html");
        } catch (IOException e) {
            LOGGER.error("Could not create help splash file");
        }
        if (splashFile != null) {
            try {
                FileUtils.forceDeleteOnExit(splashFile);
            } catch (IOException e) {
                LOGGER.error("could not register help splash file for deletion");
            }

            SplashGenerator generator = new SplashGenerator(splashFile);
            generator.generateSplash();
            try {
                this.display(splashFile.toURI().toURL());
            } catch (MalformedURLException e) {
                LOGGER.error("Malformed url when displaying splash", e);
            }
        }

        // Disable drag-and-drop, set the cursor graphic when dragging.
        webFunctionDoc.setOnDragDropped(null);
        webFunctionDoc.setOnDragOver(e -> getScene().setCursor(Cursor.DISAPPEAR));
    }

    /**
     * Displays the home page
     */
    public void displayHome() {
        displayFile(new File(helpDirectory, "index.html"));
    }

    /**
     * Display the page corresponding to a keyword.
     *
     * @param pluginPackage The package the function we want to display comes from
     * @param keyword       The name of the function in the package
     */
    public void display(final String pluginPackage, final String keyword) {
        File file = new File(helpDirectory, pluginPackage + "/" + keyword + ".html");

        Platform.runLater(() -> displayFile(file));
    }

    /**
     * Display the page corresponding to a HTML file
     *
     * @param file the file to display
     */
    private void displayFile(File file) {
        try {
            display(file.toURI().toURL());
        } catch (MalformedURLException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    /**
     * Load the passed resource.
     *
     * @param resource the resource to display
     */
    public void display(final URL resource) {
        Platform.runLater(() -> webFunctionDoc.getEngine().load(resource.toExternalForm()));
    }

    private void back() {
        webFunctionDoc.getEngine().executeScript("history.back()");
    }

    private void forward() {
        webFunctionDoc.getEngine().executeScript("history.forward()");
    }

    @FXML
    private void buttonHelpHome() {
        displayHome();
    }

    @FXML
    private void buttonHelpBack() {
        back();
    }

    @FXML
    private void buttonHelpForward() {
        forward();
    }

    @Override
    public void requestFocus() {
        webFunctionDoc.requestFocus();
    }

    /**
     * Getter for the helpSearchBar
     *
     * @return the helpSearchBar
     */
    public HelpSearchBar getHelpSearchBar() {
        return helpSearchBar;
    }

    /**
     * The generator of the splash file.
     */
    private class SplashGenerator {
        private final File file;

        /**
         * Creates a SplashGenerator which will generate a splash html at a given file
         *
         * @param file the file to generate the splash to
         */
        public SplashGenerator(final File file) {
            this.file = file;
        }

        /**
         * Generates the splash
         */
        public void generateSplash() {
            Map<String, String> substitutions = new HashMap<>();
            substitutions.put("style", getClass().getResource("/docgen/resources/_assets/css/style.css").toExternalForm());
            substitutions.put("splash", getClass().getResource("/docgen/resources/_assets/img/splash.png").toExternalForm());

            try {
                touch(file);
                FileWriter writer = new FileWriter(file);
                Template template = configureHTMLGenerator().getTemplate("splash.html");
                template.process(substitutions, writer);
                writer.close();
            } catch (IOException e) {
                LOGGER.error("IO exception when generating splash", e);
            } catch (TemplateException e) {
                LOGGER.error("Template exception when generating splash", e);
            }
        }

        private Configuration configureHTMLGenerator() {
            DocGenConfiguration docConfig = new DocGenConfiguration();
            Configuration config = new Configuration(Configuration.VERSION_2_3_23);
            config.setClassForTemplateLoading(getClass(), docConfig.getTemplateUrl());
            return config;
        }

        /**
         * Checks if a file exists, else creates that file.
         *
         * @param file The file we want to touch.
         * @throws IOException
         */
        private void touch(final File file) throws IOException {
            FileUtils.touch(file);
        }
    }
}
