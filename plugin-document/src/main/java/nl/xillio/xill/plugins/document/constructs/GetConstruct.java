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
package nl.xillio.xill.plugins.document.constructs;

import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.construct.Argument;
import nl.xillio.xill.api.construct.ConstructContext;
import nl.xillio.xill.api.construct.ConstructProcessor;
import nl.xillio.xill.api.errors.RobotRuntimeException;
import org.bson.Document;

import static nl.xillio.xill.plugins.document.DocumentXillPlugin.DEFAULT_IDENTITY;

/**
 * This construct will fetch a document from the udm.
 *
 * @author Thomas Biesaart
 */
public class GetConstruct extends AbstractUDMConstruct {

    @Override
    public ConstructProcessor prepareProcess(ConstructContext context) {
        return new ConstructProcessor(
                (documentId, identity) -> process(documentId, identity, context),
                new Argument("documentId", ATOMIC),
                new Argument("identity", fromValue(DEFAULT_IDENTITY), ATOMIC)
        );
    }

    private MetaExpression process(MetaExpression documentId, MetaExpression identity, ConstructContext context) {
        Document result = performSafe(
                () -> getDocuments(context, identity.getStringValue())
                        .find(new Document("_id", toBsonValue(documentId)))
                        .first()
        );

        if (result == null) {
            throw new RobotRuntimeException("No document with id " + documentId + " was found");
        }

        return fromValue(result);
    }
}
