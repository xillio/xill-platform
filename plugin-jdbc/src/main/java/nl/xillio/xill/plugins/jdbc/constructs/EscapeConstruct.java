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
package nl.xillio.xill.plugins.jdbc.constructs;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.construct.Argument;
import nl.xillio.xill.api.construct.Construct;
import nl.xillio.xill.api.construct.ConstructContext;
import nl.xillio.xill.api.construct.ConstructProcessor;
import nl.xillio.xill.plugins.jdbc.services.StatementSyntaxFactory;

import java.net.URL;

/**
 * This construct will escape a string constant for the specific database technology.
 * <p>
 * You can optionally provide a docRoot. If you do then the documentation for this construct will be fetched from
 * <code>docRoot + getClass().getSimpleName() + ".xml"</code> instead of the default documentation location.
 *
 * @author Thomas Biesaart
 */
public class EscapeConstruct extends Construct {
    private final StatementSyntaxFactory statementSyntaxFactory;
    private final String docRoot;

    @Inject
    public EscapeConstruct(StatementSyntaxFactory statementSyntaxFactory, @Named("docRoot") String docRoot) {
        this.statementSyntaxFactory = statementSyntaxFactory;
        this.docRoot = docRoot;
    }

    @Override
    public ConstructProcessor prepareProcess(ConstructContext context) {
        return new ConstructProcessor(
                this::process,
                new Argument("input", ATOMIC)
        );
    }

    @SuppressWarnings("squid:UnusedPrivateMethod") // Sonar does not do lambdas
    private MetaExpression process(MetaExpression input) {
        assertNotNull(input, "input");
        String result = statementSyntaxFactory.escapeString(input.getStringValue());
        return fromValue(result);
    }

    @Override
    public URL getDocumentationResource() {
        if (docRoot != null) {
            String stringUrl = docRoot + getClass().getSimpleName() + ".xml";
            URL url = getClass().getResource(stringUrl);
            if (url != null) {
                return url;
            }
        }

        return super.getDocumentationResource();
    }
}
