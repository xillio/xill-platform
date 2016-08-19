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
package nl.xillio.xill.plugins.system.constructs;


import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.construct.Argument;
import nl.xillio.xill.api.construct.Construct;
import nl.xillio.xill.api.construct.ConstructContext;
import nl.xillio.xill.api.construct.ConstructProcessor;
import nl.xillio.xill.api.errors.InvalidUserInputException;
import nl.xillio.xill.debugging.ProgressInfo;

import java.util.Map;

/**
 Set current robot progress
 */
public class SetProgressConstruct extends Construct {

    private static final String EXAMPLE = "use System;\n\n" +
            "var i = 0;\n" +
            "System.setProgress(0, {\"onStop\" : \"leave\"}); // Set progress bar visible and set the onStop behavior to leaving progress bar as it is when robot is stopped\n\n" +
            "while (i < 1000) {\n" +
            "    System.print(i);\n" +
            "    System.wait(10);\n" +
            "    System.setProgress(i++/1000); // Set progress value\n" +
            "}";

    @Override
    public ConstructProcessor prepareProcess(final ConstructContext context) {
        return new ConstructProcessor(
                (progress, options) -> process(progress, options, context),
                new Argument("progress", ATOMIC),
                new Argument("options", NULL, OBJECT));
    }

    static MetaExpression process(final MetaExpression progressVar, final MetaExpression optionsVar, final ConstructContext context) {
        if (context.getDebugger() == null) {
            return fromValue(false);
        }

        Double progress = progressVar.getNumberValue().doubleValue();
        if (progress.equals(Double.NaN)) {
            throw new InvalidUserInputException("Invalid progress value type.", progressVar.getStringValue(), "The valid number from 0-1 or any negative number for hiding the progress bar.", EXAMPLE);
        }

        final ProgressInfo progressInfo = new ProgressInfo(progress);
        if (!optionsVar.isNull()) {
            final Map<String, MetaExpression> options = optionsVar.getValue();
            options.forEach((option, value) -> processOption(option, value, progressInfo));
        }
        progressInfo.setProgress(progress);

        context.getDebugger().setProgressInfo(progressInfo);
        return fromValue(true);
    }

    private static void processOption(final String option, final MetaExpression metaValue, final ProgressInfo progressInfo) {
        if ("onStop".equals(option)) {
            final String value = metaValue.getStringValue();
            if (metaValue.getType() != ATOMIC) {
                throw new InvalidUserInputException("Invalid option value type.", value, "One of these values: leave, hide, zero", EXAMPLE);
            }

            switch (value) {
                case "leave":
                    progressInfo.setOnStopBehaviour(nl.xillio.xill.api.ProgressInfo.OnStopBehavior.LEAVE);
                    break;
                case "hide":
                    progressInfo.setOnStopBehaviour(nl.xillio.xill.api.ProgressInfo.OnStopBehavior.HIDE);
                    break;
                case "zero":
                    progressInfo.setOnStopBehaviour(nl.xillio.xill.api.ProgressInfo.OnStopBehavior.ZERO);
                    break;
                default:
                    throw new InvalidUserInputException("Invalid option value.", value, "One of these values: leave, hide, zero", EXAMPLE);
            }
        } else {
            throw new InvalidUserInputException("Invalid option.", option, "Supported option.", EXAMPLE);
        }
    }
}
