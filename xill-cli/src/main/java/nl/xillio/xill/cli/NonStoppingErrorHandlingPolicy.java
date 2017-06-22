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
package nl.xillio.xill.cli;

import nl.xillio.xill.api.errors.ErrorHandlingPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This Error handling policy allows robots to keep running even when errors are thrown.
 *
 * Created by Dwight.Peters on 20-Jun-17.
 */
public class NonStoppingErrorHandlingPolicy implements ErrorHandlingPolicy {

    private final Logger logger = LoggerFactory.getLogger(NonStoppingErrorHandlingPolicy.class);

    public void handle(final Throwable e){
        if (e.getMessage() != null)
        {
            logger.error(e.getMessage(), e);
            return;
        }
        logger.error("An error occured in one of the robots.", e);
    }
}
