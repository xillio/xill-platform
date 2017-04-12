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
package nl.xillio.xill.plugins.xurl.services;

import nl.xillio.xill.api.errors.RobotRuntimeException;
import nl.xillio.xill.plugins.xurl.data.Options;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.slf4j.Logger;

import java.io.IOException;

/**
 * This class is responsible for logging request and response information.
 *
 * @author Thomas Biesaart
 */
public class ActivityLogger {

    public void handle(Request request, Options options, Logger rootLogger) {
        log(options, rootLogger, request.toString());
    }

    public void handle(Request request, HttpResponse response, Options options, Logger rootLogger) {
        log(options, rootLogger, "Response from {}: {} {}", request, response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase());
    }

    private void log(Options options, Logger logger, String format, Object... parameters) {
        String level = options.getLogging();

        if (level == null) {
            return;
        }

        if (level.contains("info")) {
            logger.info(format, parameters);
        } else if (level.contains("error")) {
            logger.error(format, parameters);
        } else if (level.contains("warn")) {
            logger.warn(format, parameters);
        } else if (level.contains("debug")) {
            logger.debug(format, parameters);
        } else {
            throw new RobotRuntimeException("Unknown logging level [" + level + "]. Use debug, info, warn or error");
        }
    }
}
