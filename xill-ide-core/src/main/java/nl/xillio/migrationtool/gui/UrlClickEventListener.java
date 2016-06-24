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

import me.biesaart.utils.Log;
import org.slf4j.Logger;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.html.HTMLAnchorElement;

import java.awt.*;
import java.net.URI;

/**
 * Adds an event listener to link clicks to open them in a browser.
 */
public class UrlClickEventListener implements EventListener {
    private static final Logger LOGGER = Log.get();

    @Override
    public void handleEvent(Event evt) {
        EventTarget target = evt.getCurrentTarget();
        HTMLAnchorElement anchorElement = (HTMLAnchorElement) target;
        String href = anchorElement.getHref();
        try {
            URI requestedPage = new URI(href);

            // Check if page is a local path or external resource
            if (requestedPage.getHost() != null) {
                if (Desktop.isDesktopSupported()) {
                    // Prevent default behaviour and open browser
                    evt.preventDefault();
                    Desktop.getDesktop().browse(requestedPage);
                } else {
                    LOGGER.info("Could not open a browser (Desktop API is not supported)");
                }
            }
        } catch (Exception e) {
            LOGGER.error("Could not load requested help file into browser (" + href + ")", e);
        }
    }
}
