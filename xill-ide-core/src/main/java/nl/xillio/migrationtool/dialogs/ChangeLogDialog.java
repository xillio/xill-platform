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
package nl.xillio.migrationtool.dialogs;


import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.web.WebView;
import nl.xillio.migrationtool.gui.UrlClickEventListener;
import org.w3c.dom.NodeList;
import org.w3c.dom.events.EventTarget;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * An {@link FXMLDialog} for displaying the ChangeLog dialog.
 *
 * @author Ivor
 */
public class ChangeLogDialog extends FXMLDialog implements Initializable {
    @FXML
    private WebView webChangeLogDialog;

    @FXML
    private Button closeButton;

    public ChangeLogDialog(String title, String header, String content) {
        super("/fxml/dialogs/ChangeLogDialog.fxml");
        setTitle(title);
        Platform.runLater(() -> webChangeLogDialog.getEngine().loadContent(createHTML(header, content)));
    }

    /**
     * Creates an HTML string containing a HTML body.
     * Adding a head and styling.
     *
     * @param title   The title of the page
     * @param content The body of the page
     * @return the HTML string
     */
    private String createHTML(String title, String content) {
        StringBuilder html = new StringBuilder();

        //Start the header
        html.append("<html>").append("<head>");
        html.append("<title>").append(title).append("</title>");
        //add the styling
        addCssLink("/docgen/resources/_assets/css/style.css", html);
        addCssLink("/fxml/dialogs/changelog.css", html);

        //add the content
        html.append("</head>").append("<body>");
        html.append(content);
        html.append("</body></html>");
        return html.toString();
    }

    /**
     * Adds a css link to the HTML.
     *
     * @param resource The location of the css file
     * @param target   The HTML we're adding the css to
     */
    private void addCssLink(String resource, StringBuilder target) {
        target.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"")
                .append(getClass().getResource(resource).toExternalForm())
                .append("\">");
    }

    @FXML
    private void closeButtonPress() {
        this.close();
    }

    @Override
    // Add an event listener to <a> tags to load external urls in a browser
    public void initialize(URL location, ResourceBundle resources) {
        webChangeLogDialog.getEngine().getLoadWorker().stateProperty().addListener((observable, o, n) -> {
            if (Worker.State.SUCCEEDED.equals(n)) {
                NodeList nodeList = webChangeLogDialog.getEngine().getDocument().getElementsByTagName("a");
                for (int i = 0; i < nodeList.getLength(); i++) {
                    org.w3c.dom.Node node = nodeList.item(i);
                    EventTarget eventTarget = (EventTarget) node;

                    // Add the click event listener
                    eventTarget.addEventListener("click", new UrlClickEventListener(), false);
                }
            }
        });
    }
}
