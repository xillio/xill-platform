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
package nl.xillio.migrationtool;

import nl.xillio.events.Event;
import nl.xillio.events.EventHost;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.layout.PatternLayout;

import java.io.Serializable;
import java.util.Objects;

/**
 * This class is a wrapper which is used to get loggers for robots.
 */
@Plugin(name = "RobotAppender", category = "Core", elementType = "appender", printObject = true)
public class RobotAppender extends AbstractAppender {
    private static final long serialVersionUID = -4849648276752490865L;
    private static final EventHost<LogEvent> messageLogged = new EventHost<>();

    /**
     * Create a {@link RobotAppender}
     *
     * @param name   the name
     * @param filter the filter
     * @param layout the layout
     */
    protected RobotAppender(final String name, final Filter filter, final Layout<? extends Serializable> layout) {
        super(name, filter, layout, true);
    }

    @Override
    public void append(final LogEvent event) {
        messageLogged.invoke(event);
    }

    /**
     * @return the messagelogged
     */
    public static Event<LogEvent> getMessagelogged() {
        return messageLogged.getEvent();
    }

    /**
     * Create a new {@link RobotAppender}
     *
     * @param name   the name
     * @param layout the layout
     * @param filter the filters
     * @return the {@link RobotAppender}
     */
    @PluginFactory
    public static RobotAppender createAppender(@PluginAttribute("name") final String name,
                                               @PluginElement("Layout") Layout<? extends Serializable> layout,
                                               @PluginElement("Filters") final Filter filter) {

        Objects.requireNonNull(name);

        if (layout == null) {
            layout = PatternLayout.createDefaultLayout();
        }

        return new RobotAppender(name, filter, layout);
    }

}