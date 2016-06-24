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
package nl.xillio.xill.cli.services;

import nl.xillio.xill.api.components.RobotID;
import nl.xillio.xill.api.errors.XillParsingException;

import java.io.IOException;

/**
 * This exception represents a wrapper for the {@link nl.xillio.xill.api.errors.XillParsingException}.
 *
 * @author Thomas Biesaart
 */
public class CompileException extends IOException {

    private final int line;
    private final RobotID robot;

    public CompileException(XillParsingException e) {
        super(e.getMessage(), e);
        this.line = e.getLine();
        this.robot = e.getRobot();
    }

    public int getLine() {
        return line;
    }

    public RobotID getRobot() {
        return robot;
    }
}
