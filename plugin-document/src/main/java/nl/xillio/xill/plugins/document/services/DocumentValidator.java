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
package nl.xillio.xill.plugins.document.services;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import nl.xillio.xill.api.components.ExpressionDataType;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.construct.ConstructContext;
import nl.xillio.xill.api.errors.RobotRuntimeException;
import nl.xillio.xill.plugins.mongodb.services.MongoConverter;
import nl.xillio.xill.plugins.mongodb.services.serializers.ObjectIdSerializer;
import org.bson.Document;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static nl.xillio.xill.api.components.MetaExpression.extractValue;

/**
 * This class is responsible for validating documents.
 * It provides a serializer that will parse a {@link MetaExpression} to the correct types according to the
 * decorator definitions.
 *
 * @author Thomas Biesaart
 */
@Singleton
public class DocumentValidator {
    private static final String FIELD_SOURCE = "source";
    private static final String FIELD_TARGET = "target";
    private static final String FIELD_CONTENT_TYPE = "contentType";
    private static final String FIELD_VERSIONS = "versions";
    private static final String FIELD_VERSION = "version";
    private static final String FIELD_ID = "_id";
    private static final String FIELD_CURRENT = "current";
    private static final int DEFAULT_VERSION = 1;
    private final ContentTypeService contentTypeService;
    private final ObjectIdSerializer objectIdSerializer;
    private final MongoConverter mongoConverter;

    @Inject
    public DocumentValidator(ContentTypeService contentTypeService, ObjectIdSerializer objectIdSerializer, MongoConverter mongoConverter) {
        this.contentTypeService = contentTypeService;
        this.objectIdSerializer = objectIdSerializer;
        this.mongoConverter = mongoConverter;
    }

    /**
     * Check if a document conforms to all udm requirements.
     *
     * @param context        the execution context
     * @param identity       the persistence identity
     * @param metaExpression the expression
     * @return the document that can be inserted into MongoDB
     */
    public Document validateDocument(ConstructContext context, String identity, MetaExpression metaExpression) {
        Document result = new Document();

        // Check the structure
        Map<String, MetaExpression> fullDocument = toMap(metaExpression, () -> "A document must be an object");
        mustHaveFields(fullDocument, field -> "You must provide a `" + field + "` field for this document to be valid",
                FIELD_SOURCE, FIELD_TARGET, FIELD_CONTENT_TYPE
        );
        canHaveFields(fullDocument, field -> "`" + field + "` is not a valid field for a document",
                FIELD_SOURCE, FIELD_TARGET, FIELD_CONTENT_TYPE, FIELD_ID
        );

        // Fetch the fields
        if (fullDocument.containsKey(FIELD_ID)) {
            result.put(FIELD_ID, value(fullDocument, FIELD_ID));
        }

        MetaExpression contentTypeName = fullDocument.get(FIELD_CONTENT_TYPE);
        if (contentTypeName.isNull()) {
            throw new RobotRuntimeException("The content type of a document cannot be null");
        }
        List<String> contentType = contentTypeService.getContentType(context, identity, contentTypeName.getStringValue())
                .orElseThrow(() -> new RobotRuntimeException("No content type `" + contentTypeName.getStringValue() + "` was found!"));

        result.put(FIELD_SOURCE, parseHistory(fullDocument.get(FIELD_SOURCE), contentType, context, identity));
        result.put(FIELD_TARGET, parseHistory(fullDocument.get(FIELD_TARGET), contentType, context, identity));
        result.put(FIELD_CONTENT_TYPE, contentTypeName.getStringValue());

        return result;
    }

    /**
     * This method will parse the source and target sections of a document.
     *
     * @param expression  the source/target
     * @param contentType the list of required decorators
     * @param context     the execution context
     * @param identity    the persistence identity
     * @return the document that can be inserted into mongo
     */
    private Document parseHistory(MetaExpression expression, List<String> contentType, ConstructContext context, String identity) {
        Document result = new Document();
        // Check the structure
        Map<String, MetaExpression> fullHistory = toMap(expression, () -> "A source or target value must be an object");
        mustHaveFields(fullHistory, field -> "You must provide a `" + field + "` field for this source or target value to be valid",
                FIELD_CURRENT,
                FIELD_VERSIONS
        );
        canHaveFields(fullHistory, field -> "`" + field + "` is not a valid field for a source or target value",
                FIELD_CURRENT,
                FIELD_VERSIONS,
                "timestamp",
                "modifiedBy",
                "action"
        );
        if (fullHistory.get(FIELD_VERSIONS).getType() != ExpressionDataType.LIST) {
            throw new RobotRuntimeException("The versions field must contains a list of revisions");
        }

        result.put(FIELD_CURRENT, parseRevision(fullHistory.get(FIELD_CURRENT), contentType, context, identity));
        result.put(
                FIELD_VERSIONS,
                fullHistory.get(FIELD_VERSIONS).<List<MetaExpression>>getValue()
                        .stream()
                        .map(rev -> parseRevision(rev, contentType, context, identity))
                        .collect(Collectors.toList())
        );
        result.put("timestamp", new Date());
        return result;
    }

    /**
     * This method will parse a single revision of a document.
     *
     * @param expression  the document
     * @param contentType the list of required decorators
     * @param context     the execution context
     * @param identity    the identity
     * @return the document
     */
    private Document parseRevision(MetaExpression expression, List<String> contentType, ConstructContext context, String identity) {
        Document result = new Document();

        // Check the structure
        Map<String, MetaExpression> document = toMap(expression, () -> "A revision must be an object.\nInput: " + expression.toString());

        // Check the required decorators
        mustHaveFields(document, field -> "Missing required decorator `" + field + "`", contentType.toArray(new String[contentType.size()]));

        contentType.forEach(decoratorName ->
                result.put(decoratorName, parseRequiredDecorator(document.get(decoratorName), decoratorName, context, identity))
        );

        // Set the unvalidated decorators
        document.forEach((key, value) -> {
            if (key.equals(FIELD_VERSION) || contentType.contains(key)) {
                // This is not an unvalidated decorator
                return;
            }

            if (value.getType() != ExpressionDataType.OBJECT) {
                throw new RobotRuntimeException(key + " is not a valid decorator. Every decorator must be an object.");
            }

            result.put(key, mongoConverter.parse(value));
        });

        // Add a default version
        if (!document.containsKey(FIELD_VERSION)) {
            result.put(FIELD_VERSION, DEFAULT_VERSION);
        } else {
            result.put(FIELD_VERSION, value(document, FIELD_VERSION));
        }

        return result;
    }

    private Document parseRequiredDecorator(MetaExpression expression, String name, ConstructContext context, String identity) {
        Document result = new Document();
        Document decoratorDefinition = contentTypeService.getDecorator(context, identity, name)
                .orElseThrow(() -> new RobotRuntimeException("No definition was found for required decorator `" + name + "`"));

        if (expression == null) {
            throw new RobotRuntimeException("Required decorator " + name + " is not present");
        }

        Map<String, MetaExpression> decorator = toMap(expression, () -> "Every decorator must be an OBJECT");

        decoratorDefinition.forEach(
                (fieldName, fieldDefinition) ->
                        parseField(result, decorator.get(fieldName), (Document) fieldDefinition, fieldName)
        );

        return result;
    }

    private void parseField(Document document, MetaExpression expression, Document fieldDefinition, String fieldName) {
        boolean required = fieldDefinition.getBoolean("required", true);
        FieldType fieldType = FieldType.forName(fieldDefinition.getString("type"))
                .orElseThrow(() ->
                        new RobotRuntimeException("Invalid field type " + fieldDefinition.getString("type") + " for field `" + fieldName + "`")
                );

        // Throw error if no value is given for required field
        if ((expression == null || expression.isNull()) && required) {
            throw new RobotRuntimeException("Required field `" + fieldName + "` is not present");
        }

        if (expression != null) {
            if (expression.isNull()) {
                document.put(fieldName, null); // Field exists but has a null value
            } else {
                document.put(fieldName, fieldType.extractValue(expression));
            }
        }
    }

    private Object value(Map<String, MetaExpression> map, String field) {
        return extractValue(map.get(field), objectIdSerializer);
    }

    private void canHaveFields(Map<String, ?> map, Function<String, String> errorSupplier, String... fields) {
        map.keySet()
                .forEach(key -> {
                    for (String field : fields) {
                        if (field.equals(key)) {
                            return;
                        }
                    }
                    throw new RobotRuntimeException(errorSupplier.apply(key));
                });
    }

    private void mustHaveFields(Map<String, ?> map, Function<String, String> errorSupplier, String... fields) {
        for (String field : fields) {
            if (!map.containsKey(field)) {
                throw new RobotRuntimeException(errorSupplier.apply(field));
            }
        }
    }

    private Map<String, MetaExpression> toMap(MetaExpression expression, Supplier<String> errorSupplier) {
        if (expression.getType() != ExpressionDataType.OBJECT) {
            throw new RobotRuntimeException(errorSupplier.get());
        }

        return expression.getValue();
    }
}
