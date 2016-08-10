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
package nl.xillio.xill.api;

import nl.xillio.xill.api.components.RobotID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.event.Level;

/**
 * Logger class for
 * <p>
 * Created by Anwar on 2/18/2016.
 */
public class LogUtil {
    private LogUtil() {
        // No LogUtil for you
    }

    public static final String ROBOT_LOGGER_PREFIX = "robot.";

    /**
     * Gets a new logger instance associated with a specific robot ID.
     *
     * @param robotID the ID of a robot
     * @return an instance of the logger
     */
    public static Logger getLogger(final RobotID robotID, OutputHandler outputHandler) {
        Logger logger = LoggerFactory.getLogger(ROBOT_LOGGER_PREFIX + robotID.toString());

        return new WrappingLogger(logger, outputHandler);
    }

    private static class WrappingLogger implements Logger {
        private final Logger logger;
        private final OutputHandler outputHandler;

        public WrappingLogger(Logger logger, OutputHandler outputHandler) {
            this.logger = logger;
            this.outputHandler = outputHandler;
        }

        @Override
        public String getName() {
            return logger.getName();
        }

        @Override
        public boolean isTraceEnabled() {
            return logger.isTraceEnabled();
        }

        @Override
        public void trace(String s) {
            logger.trace(s);
            outputHandler.handleLog(Level.TRACE, s);
        }

        @Override
        public void trace(String s, Object o) {
            logger.trace(s, o);
            outputHandler.handleLog(Level.TRACE, s, o);
        }

        @Override
        public void trace(String s, Object o, Object o1) {
            logger.trace(s, o, o1);
            outputHandler.handleLog(Level.TRACE, s, o, o1);
        }

        @Override
        public void trace(String s, Object... objects) {
            logger.trace(s, objects);
            outputHandler.handleLog(Level.TRACE, s, objects);
        }

        @Override
        public void trace(String s, Throwable throwable) {
            logger.trace(s, throwable);
            outputHandler.handleLog(Level.TRACE, s, throwable);
        }

        @Override
        public boolean isTraceEnabled(Marker marker) {
            return logger.isTraceEnabled(marker);
        }

        @Override
        public void trace(Marker marker, String s) {
            logger.trace(marker, s);
            outputHandler.handleLog(Level.TRACE, s);
        }

        @Override
        public void trace(Marker marker, String s, Object o) {
            logger.trace(marker, s, o);
            outputHandler.handleLog(Level.TRACE, s, o);
        }

        @Override
        public void trace(Marker marker, String s, Object o, Object o1) {
            logger.trace(marker, s, o, o1);
            outputHandler.handleLog(Level.TRACE, s, o, o1);
        }

        @Override
        public void trace(Marker marker, String s, Object... objects) {
            logger.trace(marker, s, objects);
            outputHandler.handleLog(Level.TRACE, s, objects);
        }

        @Override
        public void trace(Marker marker, String s, Throwable throwable) {
            logger.trace(marker, s, throwable);
            outputHandler.handleLog(Level.TRACE, s, throwable);
        }

        @Override
        public boolean isDebugEnabled() {
            return logger.isDebugEnabled();
        }

        @Override
        public void debug(String s) {
            logger.debug(s);
            outputHandler.handleLog(Level.DEBUG, s);
        }

        @Override
        public void debug(String s, Object o) {
            logger.debug(s, o);
            outputHandler.handleLog(Level.DEBUG, s, o);
        }

        @Override
        public void debug(String s, Object o, Object o1) {
            logger.debug(s, o, o1);
            outputHandler.handleLog(Level.DEBUG, s, o, o1);
        }

        @Override
        public void debug(String s, Object... objects) {
            logger.debug(s, objects);
            outputHandler.handleLog(Level.DEBUG, s, objects);
        }

        @Override
        public void debug(String s, Throwable throwable) {
            logger.debug(s, throwable);
            outputHandler.handleLog(Level.DEBUG, s, throwable);
        }

        @Override
        public boolean isDebugEnabled(Marker marker) {
            return logger.isDebugEnabled(marker);
        }

        @Override
        public void debug(Marker marker, String s) {
            logger.debug(marker, s);
            outputHandler.handleLog(Level.DEBUG, s);
        }

        @Override
        public void debug(Marker marker, String s, Object o) {
            logger.debug(marker, s, o);
            outputHandler.handleLog(Level.DEBUG, s, o);
        }

        @Override
        public void debug(Marker marker, String s, Object o, Object o1) {
            logger.debug(marker, s, o, o1);
            outputHandler.handleLog(Level.DEBUG, s, o, o1);
        }

        @Override
        public void debug(Marker marker, String s, Object... objects) {
            logger.debug(marker, s, objects);
            outputHandler.handleLog(Level.DEBUG, s, objects);
        }

        @Override
        public void debug(Marker marker, String s, Throwable throwable) {
            logger.debug(marker, s, throwable);
            outputHandler.handleLog(Level.DEBUG, s, throwable);
        }

        @Override
        public boolean isInfoEnabled() {
            return logger.isInfoEnabled();
        }

        @Override
        public void info(String s) {
            logger.info(s);
            outputHandler.handleLog(Level.INFO, s);
        }

        @Override
        public void info(String s, Object o) {
            logger.info(s, o);
            outputHandler.handleLog(Level.INFO, s, o);
        }

        @Override
        public void info(String s, Object o, Object o1) {
            logger.info(s, o, o1);
            outputHandler.handleLog(Level.INFO, s, o, o1);
        }

        @Override
        public void info(String s, Object... objects) {
            logger.info(s, objects);
            outputHandler.handleLog(Level.INFO, s, objects);
        }

        @Override
        public void info(String s, Throwable throwable) {
            logger.info(s, throwable);
            outputHandler.handleLog(Level.INFO, s, throwable);
        }

        @Override
        public boolean isInfoEnabled(Marker marker) {
            return logger.isInfoEnabled(marker);
        }

        @Override
        public void info(Marker marker, String s) {
            logger.info(marker, s);
            outputHandler.handleLog(Level.INFO, s);
        }

        @Override
        public void info(Marker marker, String s, Object o) {
            logger.info(marker, s, o);
            outputHandler.handleLog(Level.INFO, s, o);
        }

        @Override
        public void info(Marker marker, String s, Object o, Object o1) {
            logger.info(marker, s, o, o1);
            outputHandler.handleLog(Level.INFO, s, o, o1);
        }

        @Override
        public void info(Marker marker, String s, Object... objects) {
            logger.info(marker, s, objects);
            outputHandler.handleLog(Level.INFO, s, objects);
        }

        @Override
        public void info(Marker marker, String s, Throwable throwable) {
            logger.info(marker, s, throwable);
            outputHandler.handleLog(Level.INFO, s, throwable);
        }

        @Override
        public boolean isWarnEnabled() {
            return logger.isWarnEnabled();
        }

        @Override
        public void warn(String s) {
            logger.warn(s);
            outputHandler.handleLog(Level.WARN, s);
        }

        @Override
        public void warn(String s, Object o) {
            logger.warn(s, o);
            outputHandler.handleLog(Level.WARN, s, o);
        }

        @Override
        public void warn(String s, Object... objects) {
            logger.warn(s, objects);
            outputHandler.handleLog(Level.WARN, s, objects);
        }

        @Override
        public void warn(String s, Object o, Object o1) {
            logger.warn(s, o, o1);
            outputHandler.handleLog(Level.WARN, s, o, o1);
        }

        @Override
        public void warn(String s, Throwable throwable) {
            logger.warn(s, throwable);
            outputHandler.handleLog(Level.WARN, s, throwable);
        }

        @Override
        public boolean isWarnEnabled(Marker marker) {
            return logger.isWarnEnabled(marker);
        }

        @Override
        public void warn(Marker marker, String s) {
            logger.warn(marker, s);
            outputHandler.handleLog(Level.WARN, s);
        }

        @Override
        public void warn(Marker marker, String s, Object o) {
            logger.warn(marker, s, o);
            outputHandler.handleLog(Level.WARN, s, o);
        }

        @Override
        public void warn(Marker marker, String s, Object o, Object o1) {
            logger.warn(marker, s, o, o1);
            outputHandler.handleLog(Level.WARN, s, o, o1);
        }

        @Override
        public void warn(Marker marker, String s, Object... objects) {
            logger.warn(marker, s, objects);
            outputHandler.handleLog(Level.WARN, s, objects);
        }

        @Override
        public void warn(Marker marker, String s, Throwable throwable) {
            logger.warn(marker, s, throwable);
            outputHandler.handleLog(Level.WARN, s, throwable);
        }

        @Override
        public boolean isErrorEnabled() {
            return logger.isErrorEnabled();
        }

        @Override
        public void error(String s) {
            logger.error(s);
            outputHandler.handleLog(Level.ERROR, s);
        }

        @Override
        public void error(String s, Object o) {
            logger.error(s, o);
            outputHandler.handleLog(Level.ERROR, s, o);
        }

        @Override
        public void error(String s, Object o, Object o1) {
            logger.error(s, o, o1);
            outputHandler.handleLog(Level.ERROR, s, o, o1);
        }

        @Override
        public void error(String s, Object... objects) {
            logger.error(s, objects);
            outputHandler.handleLog(Level.ERROR, s, objects);
        }

        @Override
        public void error(String s, Throwable throwable) {
            logger.error(s, throwable);
            outputHandler.handleLog(Level.ERROR, s, throwable);
        }

        @Override
        public boolean isErrorEnabled(Marker marker) {
            return logger.isErrorEnabled(marker);
        }

        @Override
        public void error(Marker marker, String s) {
            logger.error(marker, s);
            outputHandler.handleLog(Level.ERROR, s);
        }

        @Override
        public void error(Marker marker, String s, Object o) {
            logger.error(marker, s, o);
            outputHandler.handleLog(Level.ERROR, s, o);
        }

        @Override
        public void error(Marker marker, String s, Object o, Object o1) {
            logger.error(marker, s, o, o1);
            outputHandler.handleLog(Level.ERROR, s, o, o1);
        }

        @Override
        public void error(Marker marker, String s, Object... objects) {
            logger.error(marker, s, objects);
            outputHandler.handleLog(Level.ERROR, s, objects);
        }

        @Override
        public void error(Marker marker, String s, Throwable throwable) {
            logger.error(marker, s, throwable);
            outputHandler.handleLog(Level.ERROR, s, throwable);
        }
    }
}
