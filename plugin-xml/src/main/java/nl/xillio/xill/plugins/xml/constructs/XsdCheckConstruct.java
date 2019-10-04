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
package nl.xillio.xill.plugins.xml.constructs;

import com.google.inject.Inject;
import nl.xillio.xill.api.components.ExpressionBuilderHelper;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.construct.Argument;
import nl.xillio.xill.api.construct.Construct;
import nl.xillio.xill.api.construct.ConstructContext;
import nl.xillio.xill.api.construct.ConstructProcessor;
import nl.xillio.xill.plugins.xml.services.XsdService;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.stream.Collectors;

/**
 * Returns true if XML file is valid according to XSD specification
 * In case of validation errors, it will show each problem as warning message
 *
 * @author Zbynek Hochmann
 * @author @Deprecated
 */
public class XsdCheckConstruct extends Construct {
    @Inject
    private XsdService xsdService;

    static MetaExpression process(final ConstructContext context,
                                  MetaExpression xmlFileName,
                                  MetaExpression xsdFileName,
                                  MetaExpression outputAslist,
                                  XsdService service,
                                  Logger logger) {
        Path xmlFile = getPath(context, xmlFileName);
        Path xsdFile = getPath(context, xsdFileName);
        boolean isOutputList = outputAslist.getBooleanValue();

        if (isOutputList) {
            return fromValue(
                    service.xsdCheckGetIssueList(xmlFile, xsdFile).stream()
                            .map(ExpressionBuilderHelper::fromValue)
                            .collect(Collectors.toList())
            );
        } else {
            return fromValue(service.xsdCheck(xmlFile, xsdFile, logger));
        }
    }

    @Override
    public ConstructProcessor prepareProcess(ConstructContext context) {
        return new ConstructProcessor(
                (xmlFile, xsdFile, outputAsList) -> process(context, xmlFile, xsdFile, outputAsList, xsdService, context.getRootLogger()),
                new Argument("xmlFile", ATOMIC),
                new Argument("xsdFile", ATOMIC),
                new Argument("outputAsList", fromValue(false), ATOMIC)
        );
    }

}
