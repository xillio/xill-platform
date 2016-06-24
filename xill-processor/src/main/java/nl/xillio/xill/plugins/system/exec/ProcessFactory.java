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
package nl.xillio.xill.plugins.system.exec;

import nl.xillio.xill.services.inject.FactoryBuilderException;

import java.io.IOException;
import java.util.function.Function;

/**
 * This class builds {@link Process} it is in here to improve testability
 */
public class ProcessFactory implements Function<ProcessDescription, Process> {

    @Override
    public Process apply(final ProcessDescription description) {
        ProcessBuilder builder = new ProcessBuilder(description.getCommands());

        if (description.getWorkdingDirectory() != null) {
            builder.directory(description.getWorkdingDirectory());
        }
        try {
            return builder.start();
        } catch (IOException e) {
            throw new FactoryBuilderException("Could not build a " + Process.class.getName(), e);
        }
    }
}
