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

import nl.xillio.xill.XillProcessor;

import java.io.PrintStream;

/**
 * This class is responsible for finding the version number of the current {@link nl.xillio.xill.api.XillProcessor} and
 * printing it to a {@link PrintStream}.
 *
 * @author Thomas Biesaart
 */
public class VersionPrinter {

    private final PrintStream output;

    public VersionPrinter(PrintStream output) {
        this.output = output;
    }

    /**
     * Print the version numbers of the processor and the java runtime environment to the {@link PrintStream} provided
     * through the constructor.
     */
    public void print() {
        output.print("Xill Processor: ");
        output.println(getVersion());

        output.print("Java Version: ");
        output.println(System.getProperty("java.version"));
    }

    private String getVersion() {
        String version = XillProcessor.class.getPackage().getImplementationVersion();
        if (version == null) {
            version = "Development Build";
        }
        return version;
    }
}
