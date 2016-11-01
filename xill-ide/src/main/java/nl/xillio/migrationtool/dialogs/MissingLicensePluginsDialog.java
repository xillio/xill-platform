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
import nl.xillio.plugins.PluginLoadFailure;
import org.w3c.dom.NodeList;
import org.w3c.dom.events.EventTarget;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * An {@link FXMLDialog} for displaying the plugins with missing license dialog.
 *
 * @author Ivor
 */
public class MissingLicensePluginsDialog extends FXMLDialog {
    @FXML
    private WebView webChangeLogDialog;

    @FXML
    private Button closeButton;

    public MissingLicensePluginsDialog(List<PluginLoadFailure> content) {
        super("/fxml/dialogs/MissingLicensePluginsDialog.fxml");
        setTitle("Unloaded plugins");
        Platform.runLater(() -> webChangeLogDialog.getEngine().loadContent(createHTML(content)));
    }

    /**
     * Creates an HTML string containing a HTML body.
     * Adding a head and styling.
     *
     * @param title   The title of the page
     * @param content The body of the page
     * @return the HTML string
     */
    private String createHTML(List<PluginLoadFailure> content) {
        StringBuilder html = new StringBuilder();

        //Start the header
        html.append("<html>").append("<head>");
        html.append("<title>Plugins not loaded due to invalid or expired license</title>");
        //add the styling
        addCssLink("/docgen/resources/_assets/css/style.css", html);
        addCssLink("/fxml/dialogs/changelog.css", html);

        //add the content
        html.append("</head>").append("<body>");
        html.append("<h2>The following plugins could not be loaded:</h2>");
        html.append("<table>");
        html.append("<thead><tr><td>Plugin</td><td>Cause</td></tr></thead>");
        html.append("<tbody>");
        content.forEach(plf -> html
                .append("<tr>")
                .append("<td>").append(plf.getModuleName()).append("</td>")
                .append("<td>").append(plf.getCause()).append("</td>")
                .append("</tr>")
        );
        html.append("</tbody></table><p></p>");
        html.append("<h2>How to solve this</h2>");
        html.append("<ul>");
        html.append("<li>If you would like to enable one or more of these plugins, contact the vendor of these plugins.</li>");
        html.append("<li>If you do not intend to use these plugins anymore, you can remove these from your file system.</li>");
        html.append("</ul>");
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

}
