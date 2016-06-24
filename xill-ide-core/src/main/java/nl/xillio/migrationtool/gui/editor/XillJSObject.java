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
package nl.xillio.migrationtool.gui.editor;


import netscape.javascript.JSObject;
import nl.xillio.xill.api.XillProcessor;
import me.biesaart.utils.Log;
import org.slf4j.Logger;

import java.util.Collection;
import java.util.Objects;

public class XillJSObject {
    private static final Logger LOGGER = Log.get();
    private final XillProcessor processor;

    public XillJSObject(XillProcessor processor) {
        Objects.requireNonNull(processor);
        this.processor = processor;
    }

    public void info(String message) {
        LOGGER.info(message);
    }

    public void getCompletions(JSObject query) {

        try {
            String prefix = query.getMember("prefix").toString();
            String currentLine = query.getMember("currentLine").toString();
            int column = ((Number) query.getMember("column")).intValue();
            int row = ((Number) query.getMember("row")).intValue();

            processor.getCompletions(currentLine, prefix, column, row).forEach(
                    (key, value) -> value.forEach(
                            item -> {
                                int parPos = item.lastIndexOf("(");
                                String caption = parPos > 0 ? item.substring(0, parPos) : item;
                                query.call("callback", caption, item, 1000, key);
                            }
                    )
            );
        } catch (Exception e) {
            LOGGER.error("ugh", e);
            throw e;
        }
    }

    public String[] getPluginNames() {
        Collection<String> names = processor.listPackages();
        return names.toArray(new String[names.size()]);
    }
}
