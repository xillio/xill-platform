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

package nl.xillio.xill.util;

import me.biesaart.utils.Log;

import java.awt.*;
import java.io.IOException;
import java.net.URI;

/**
 * Utility class to open a URI in a browser window.
 * @author Daan Knoope
 */
public class BrowserOpener {

    // Class is a library and should not be initialized
    private BrowserOpener(){}


    /**
     * Checks if the currently used platform supports opening a browser window from xill.
     * @return a {@link Boolean} which is {@code true} if opening a browswer is supported on the current platform and {@code false} otherwise.
     */
    public static boolean browserIsSupported(){
        return Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE);
    }

    /**
     * Opens a browser window in a new thread.
     * @param page a {@link URI} to the page which should be opened in a browser window.
     */
    public static void openBrowser(URI page){
        Thread browseURIThread = new Thread(() -> {
            try {
                Desktop.getDesktop().browse(page);
            } catch (IOException e) {
                Log.get().error("Unable to open link.", e);
            }
        });
        browseURIThread.start();
    }


}
