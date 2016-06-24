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

/**
 * The output of a processor
 */
public class ProcessOutput {
    private final String output;
    private final String errors;

    /**
     * Create a new {@link ProcessOutput}
     *
     * @param output the output from stdout
     * @param errors the output from stderr
     */
    public ProcessOutput(final String output, final String errors) {
        this.output = output;
        this.errors = errors;
    }

    /**
     * @return the output
     */
    public String getOutput() {
        return output;
    }

    /**
     * @return the errors
     */
    public String getErrors() {
        return errors;
    }
}
