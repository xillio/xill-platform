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
package nl.xillio.xill.plugins.xurl.services;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import me.biesaart.utils.IOUtils;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.construct.ConstructContext;
import nl.xillio.xill.api.data.XmlNode;
import nl.xillio.xill.api.errors.NotImplementedException;
import nl.xillio.xill.api.errors.RobotRuntimeException;
import nl.xillio.xill.api.io.IOStream;
import nl.xillio.xill.plugins.xurl.data.Options;
import nl.xillio.xill.services.files.FileResolver;
import org.apache.http.HttpEntity;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import static nl.xillio.xill.api.components.ExpressionDataType.*;

/**
 * This class is responsible for converting a MetaExpression to a HttpEntity.
 *
 * @author Thomas Biesaart
 */
@Singleton
public class BodyFactory {
    private static final String FIELD_NAME = "name";
    private static final String FIELD_CONTENT = "content";
    private static final String FIELD_TYPE = "type";
    private static final String FIELD_CONTENT_TYPE = "contentType";
    private final FileResolver fileResolver;

    @Inject
    BodyFactory(FileResolver fileResolver) {
        this.fileResolver = fileResolver;
    }

    /**
     * This method will extract a body from the expression and set it to the request.
     *
     * @param request    the request
     * @param expression the body expression
     * @param options    the request options
     * @param context    the construct context
     */
    public void applyBody(Request request, MetaExpression expression, Options options, ConstructContext context) {
        if (options.isMultipart()) {
            // This is a multi part request
            request.body(buildMultiPart(expression, context));

        } else {
            // This is a single part request
            Optional<ContentType> type = options.getBodyContentType();
            BodyType bodyType = BodyType.of(expression);
            bodyType.apply(request, type.orElse(bodyType.getDefaultType()), expression);
        }
    }

    /**
     * This method will build a multipart body from an expression. That expression must be an object containing objects
     * that represent multipart body parts.
     *
     * @param expression the expression
     * @param context    the construct context
     * @return the body
     */
    private HttpEntity buildMultiPart(MetaExpression expression, ConstructContext context) {
        if (expression.getType() != OBJECT && expression.getType() != LIST) {
            throw new RobotRuntimeException("A multipart body must always be an OBJECT or a LIST. Please check the documentation.");
        }
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();

        if (expression.getType() == OBJECT) {
            expression.<Map<String, MetaExpression>>getValue()
                    .forEach((name, bodyPart) -> buildPart(name, bodyPart, context, builder));
        } else if (expression.getType() == LIST) {
            expression.<List<MetaExpression>>getValue()
                    .forEach(bodyPart -> buildPart(null, bodyPart, context, builder));
        }

        return builder.build();
    }

    /**
     * This method will build a single body part from an expression and add it to a builder.
     *
     * @param name     the name of the part
     * @param bodyPart the expression
     * @param context  the construct context
     * @param builder  the builder
     */
    private void buildPart(String objectName, MetaExpression bodyPart, ConstructContext context, MultipartEntityBuilder builder) {
        if (bodyPart.getType() != OBJECT) {
            throw new RobotRuntimeException("A multipart body part must always be an OBJECT. Please check the documentation.");
        }

        Map<String, MetaExpression> fields = bodyPart.getValue();
        // Check if there are unknown options
        fields.keySet()
                .stream()
                .filter(or(negate(FIELD_NAME::equals), objectName != null))
                .filter(negate(FIELD_CONTENT::equals))
                .filter(negate(FIELD_TYPE::equals))
                .filter(negate(FIELD_CONTENT_TYPE::equals))
                .findAny()
                .ifPresent(key -> {
                    throw new RobotRuntimeException("Unknown option '" + key + "' in body part.");
                });

        String name = objectName == null ? getNameField(fields).getStringValue() : objectName;

        // Extract the required information
        MetaExpression content = getRequiredField(fields, FIELD_CONTENT, name);
        MetaExpression type = getRequiredField(fields, FIELD_TYPE, name);
        Optional<ContentType> contentType = get(fields, FIELD_CONTENT_TYPE)
                .map(MetaExpression::getStringValue)
                .map(OptionsFactory::parseContentType);

        // Parse the body
        switch (type.getStringValue().toLowerCase()) {
            case "text":
                buildTextPart(name, content, contentType.orElse(BodyType.TEXT.getDefaultType()), builder);
                break;
            case "stream":
                buildBinaryPart(name, content, contentType.orElse(BodyType.STREAM.getDefaultType()), builder);
                break;
            case "file":
                buildFilePart(name, content, contentType.orElse(BodyType.STREAM.getDefaultType()), context, builder);
                break;
            default:
                throw new RobotRuntimeException("Invalid type '" + type.getStringValue() + " for body part " + name + "\n" +
                        "Available types: text, stream and file"
                );
        }
    }

    private void buildFilePart(String name, MetaExpression content, ContentType contentType, ConstructContext context, MultipartEntityBuilder builder) {
        File file = fileResolver.buildPath(context, content).toFile();
        builder.addBinaryBody(
                name,
                file,
                contentType,
                file.getName()
        );
    }

    private void buildBinaryPart(String name, MetaExpression content, ContentType contentType, MultipartEntityBuilder builder) {

        try {
            builder.addBinaryBody(
                    name,
                    content.getBinaryValue().getInputStream(),
                    contentType,
                    null
            );
        } catch (IOException e) {
            throw new RobotRuntimeException("Could not read stream: " + e.getMessage(), e);
        }
    }

    private void buildTextPart(String name, MetaExpression content, ContentType contentType, MultipartEntityBuilder builder) {
        BodyType bodyType = BodyType.of(content);
        String text = getText(bodyType, content);
        builder.addTextBody(name, text, contentType);
    }

    private String getText(BodyType type, MetaExpression content) {
        switch (type) {
            case STREAM:
                return readStream(content.getBinaryValue());
            case XML:
                XmlNode xmlNode = content.getMeta(XmlNode.class);
                return xmlNode.getXmlContent();
            case TEXT:
            case JSON:
                return content.getStringValue();
            default:
                throw new NotImplementedException("No implementation found for " + type);
        }
    }

    private String readStream(IOStream ioStream) {
        try {
            return IOUtils.toString(ioStream.getInputStream(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RobotRuntimeException("Could not read text from stream: " + e.getMessage(), e);
        }
    }

    private static <T> Predicate<T> negate(Predicate<T> predicate) {
        return e -> !predicate.test(e);
    }

    private static <T> Predicate<T> or(Predicate<T> predicate, boolean check) {
        return e -> check || predicate.test(e);
    }

    private MetaExpression getRequiredField(Map<String, MetaExpression> map, String fieldName, String name) {
        return get(map, fieldName).orElseThrow(() ->
                new RobotRuntimeException("Invalid body part " + name + ".\n" +
                        "Every body part must contain a field called '" + fieldName + "'")
        );
    }

    private MetaExpression getNameField(Map<String, MetaExpression> map) {
        return get(map, FIELD_NAME).orElseThrow(() ->
                new RobotRuntimeException("Invalid body part.\n" +
                        "Every body part from a LIST must contain a field called '" + FIELD_NAME + "'")
        );
    }

    private Optional<MetaExpression> get(Map<String, MetaExpression> map, String fieldName) {
        return Optional.ofNullable(map.get(fieldName));
    }

    private static InputStream getInputStream(MetaExpression expression) {
        try {
            return expression.getBinaryValue().getInputStream();
        } catch (IOException e) {
            throw new RobotRuntimeException("Could not get stream from " + expression, e);
        }
    }

    private enum BodyType {
        STREAM(ContentType.DEFAULT_BINARY) {
            @Override
            void apply(Request request, ContentType type, MetaExpression value) {
                request.bodyStream(getInputStream(value), type);
            }
        },
        TEXT(ContentType.DEFAULT_TEXT) {
            @Override
            void apply(Request request, ContentType contentType, MetaExpression value) {
                request.bodyString(value.getStringValue(), contentType);
            }
        },
        XML(ContentType.APPLICATION_XML) {
            @Override
            void apply(Request request, ContentType contentType, MetaExpression value) {
                XmlNode xmlNode = value.getMeta(XmlNode.class);
                request.bodyString(xmlNode.getXmlContent(), contentType);
            }
        },
        JSON(ContentType.APPLICATION_JSON) {
            @Override
            void apply(Request request, ContentType contentType, MetaExpression value) {
                request.bodyString(value.getStringValue(), contentType);
            }
        };

        private final ContentType defaultType;

        BodyType(ContentType defaultType) {
            this.defaultType = defaultType;
        }

        abstract void apply(Request request, ContentType contentType, MetaExpression value);

        public ContentType getDefaultType() {
            return defaultType;
        }

        public static BodyType of(MetaExpression expression) {
            if (expression.getBinaryValue().hasInputStream()) {
                return STREAM;
            }
            if (expression.hasMeta(XmlNode.class)) {
                return XML;
            }
            if (expression.getType() == ATOMIC) {
                return TEXT;
            }
            return JSON;
        }
    }
}

