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

import com.google.inject.Inject;
import com.mongodb.client.model.UpdateOptions;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.construct.Argument;
import nl.xillio.xill.api.construct.ConstructContext;
import nl.xillio.xill.api.construct.ConstructProcessor;
import nl.xillio.xill.plugins.document.services.DocumentValidator;
import org.bson.Document;

import java.util.Map;

import static nl.xillio.xill.plugins.document.DocumentXillPlugin.DEFAULT_IDENTITY;

/**
 * This construct will save a document to the database.
 *
 * @author Thomas Biesaart
 */
public class SaveConstruct extends AbstractUDMConstruct {
    private final DocumentValidator documentValidator;

    @Inject
    public SaveConstruct(DocumentValidator documentValidator) {
        this.documentValidator = documentValidator;
    }

    @Override
    public ConstructProcessor prepareProcess(ConstructContext context) {
        return new ConstructProcessor(
                (doc, identity) -> process(doc, identity, context),
                new Argument("document", OBJECT),
                new Argument("identity", fromValue(DEFAULT_IDENTITY), ATOMIC)
        );
    }

    public MetaExpression process(MetaExpression document, MetaExpression identity, ConstructContext context) {
        Document doc = documentValidator.validateDocument(context, identity.getStringValue(), document);

        // Perform insertion or update?
        if (doc.get("_id") == null) {
            performInsert(doc, context, identity);
        } else {
            performUpdate(doc, context, identity);
        }

        MetaExpression id = fromObject(doc.get("_id"));
        id.registerReference(); // Adding it to an object
        Map<String, MetaExpression> values = document.getValue();
        values.put("_id", id);

        /*
         Return a COPY for the scoping administration.
         This is to prevent the garbage collector to collect the same expression as if they
         are two different expressions.
          */
        return fromObject(doc.get("_id"));
    }

    private void performUpdate(Document mongoDoc, ConstructContext context, MetaExpression identity) {
        performSafe(() ->
                getDocuments(context, identity.getStringValue())
                        .replaceOne(
                                new Document("_id", mongoDoc.get("_id")),
                                mongoDoc,
                                new UpdateOptions().upsert(true)
                        )
        );
    }

    private void performInsert(Document mongoDoc, ConstructContext context, MetaExpression identity) {
        performSafe(() ->
                getDocuments(context, identity.getStringValue())
                        .insertOne(
                                mongoDoc
                        )
        );
    }
}
