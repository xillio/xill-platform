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

import nl.xillio.xill.TestUtils;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.errors.RobotRuntimeException;
import nl.xillio.xill.plugins.mongodb.services.MongoConverter;
import nl.xillio.xill.plugins.mongodb.services.ObjectIdSerializer;
import nl.xillio.xill.services.json.JsonException;
import org.apache.commons.io.IOUtils;
import org.bson.Document;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.LinkedHashMap;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;


public class DocumentValidatorTest extends TestUtils {

    @Test
    public void testValidateDocumentNormalPath() throws JsonException, IOException {
        ContentTypeService contentTypeService = mock(ContentTypeService.class);
        DocumentValidator documentValidator = mockValidator(contentTypeService);

        MetaExpression expression = makeMetaExpressionDocument("/example-valid-document.json");

        Document document = documentValidator.validateDocument(context(), "default", expression);

        // Test type conversion to string
        assertEquals(
                ((Document) ((List<Document>) ((Document) document.get("target")).get("versions")).get(0).get("user")).getString("name"),
                "5"
        );

        // Test the unvalidated decorator
        assertEquals(
                ((Document) ((Document) ((Document) document.get("target")).get("current")).get("extra")).getString("data"),
                "MORE INFO"
        );
    }

    @Test(expectedExceptions = RobotRuntimeException.class)
    public void testValidateDocumentNonExistentContentType() throws JsonException, IOException {
        ContentTypeService contentTypeService = mock(ContentTypeService.class);
        // get a content type service which will never find anything
        DocumentValidator documentValidator = mockValidatorNoContentType(contentTypeService);

        MetaExpression expression = makeMetaExpressionDocument("/example-invalid-document-malformed-decorator.json");
        documentValidator.validateDocument(context(), "default", expression);
    }

    @Test(expectedExceptions = RobotRuntimeException.class)
    public void testValidateDocumentMalformedDecorator() throws JsonException, IOException {
        ContentTypeService contentTypeService = mock(ContentTypeService.class);
        DocumentValidator documentValidator = mockValidator(contentTypeService);

        MetaExpression expression = parseJson(IOUtils.toString(getClass().getResourceAsStream("/example-invalid-document-malformed-decorator.json")));
        documentValidator.validateDocument(context(), "default", expression);
    }

    @Test(expectedExceptions = RobotRuntimeException.class)
    public void testValidateDocumentMissingContentType() throws JsonException, IOException {
        ContentTypeService contentTypeService = mock(ContentTypeService.class);
        DocumentValidator documentValidator = mockValidator(contentTypeService);

        MetaExpression expression = parseJson(IOUtils.toString(getClass().getResourceAsStream("/example-invalid-document-missing-content-type.json")));
        documentValidator.validateDocument(context(), "default", expression);
    }

    @Test(expectedExceptions = RobotRuntimeException.class)
    public void testValidateDocumentMissingDecorator() throws JsonException, IOException {
        ContentTypeService contentTypeService = mock(ContentTypeService.class);
        DocumentValidator documentValidator = mockValidator(contentTypeService);

        MetaExpression expression = parseJson(IOUtils.toString(getClass().getResourceAsStream("/example-invalid-document-missing-decorator.json")));
        documentValidator.validateDocument(context(), "default", expression);
    }

    @Test(expectedExceptions = RobotRuntimeException.class)
    public void testValidateDocumentEmptyDecorator() throws JsonException, IOException {
        ContentTypeService contentTypeService = mock(ContentTypeService.class);
        DocumentValidator documentValidator = mockValidator(contentTypeService);

        MetaExpression expression = parseJson(IOUtils.toString(getClass().getResourceAsStream("/example-invalid-document-empty-decorator.json")));
        documentValidator.validateDocument(context(), "default", expression);
    }

    @Test(expectedExceptions = RobotRuntimeException.class)
    public void testValidateDocumentMissingVersions() throws JsonException, IOException {
        ContentTypeService contentTypeService = mock(ContentTypeService.class);
        DocumentValidator documentValidator = mockValidator(contentTypeService);

        MetaExpression expression = parseJson(IOUtils.toString(getClass().getResourceAsStream("/example-invalid-document-missing-versions.json")));
        documentValidator.validateDocument(context(), "default", expression);
    }

    @Test(expectedExceptions = RobotRuntimeException.class)
    public void testValidateDocumentMissingTarget() throws JsonException, IOException {
        ContentTypeService contentTypeService = mock(ContentTypeService.class);
        DocumentValidator documentValidator = mockValidator(contentTypeService);

        MetaExpression expression = parseJson(IOUtils.toString(getClass().getResourceAsStream("/example-invalid-document-missing-target.json")));
        documentValidator.validateDocument(context(), "default", expression);
    }

    @Test(expectedExceptions = RobotRuntimeException.class)
    public void testValidateDocumentMissingSource() throws JsonException, IOException {
        ContentTypeService contentTypeService = mock(ContentTypeService.class);
        DocumentValidator documentValidator = mockValidator(contentTypeService);

        MetaExpression expression = parseJson(IOUtils.toString(getClass().getResourceAsStream("/example-invalid-document-missing-source.json")));
        documentValidator.validateDocument(context(), "default", expression);
    }

    @Test
    public void testValidateDocumentMissingVersion() throws JsonException, IOException {
        ContentTypeService contentTypeService = mock(ContentTypeService.class);
        DocumentValidator documentValidator = mockValidator(contentTypeService);

        MetaExpression expression = parseJson(IOUtils.toString(getClass().getResourceAsStream("/example-invalid-document-missing-version.json")));
        Document result = documentValidator.validateDocument(context(), "default", expression);
        assertEquals(((Document)((Document)result.get("source")).get("current")).get("version").toString(), "1");
    }

    @Test
    public void testInsertOptionalFieldNotSpecified() throws JsonException, IOException {
        ContentTypeService contentTypeService = mock(ContentTypeService.class);
        DocumentValidator documentValidator = mockValidatorOptionalField(contentTypeService);

        MetaExpression expression = parseJson(IOUtils.toString(getClass().getResourceAsStream("/example-incomplete-document.json")));
        Document result = documentValidator.validateDocument(context(), "default", expression);

        assertEquals(((Document)((Document)((Document)result.get("source")).get("current")).get("user")).size(), 0);
    }

    @Test
    public void testInsertFieldNullValue() throws JsonException, IOException {
        ContentTypeService contentTypeService = mock(ContentTypeService.class);
        DocumentValidator documentValidator = mockValidatorOptionalField(contentTypeService);

        MetaExpression expression = parseJson(IOUtils.toString(getClass().getResourceAsStream("/example-document-null-value.json")));
        Document result = documentValidator.validateDocument(context(), "default", expression);

        assertEquals(((Document)((Document)((Document)result.get("source")).get("current")).get("user")).size(), 1);
    }


    private DocumentValidator mockValidator(ContentTypeService contentTypeService) {
        when(contentTypeService.getContentType(any(), anyString(), anyString()))
                .thenReturn(Optional.of(Collections.singletonList("user")));

        when(contentTypeService.getDecorator(any(), anyString(), anyString()))
                .thenReturn(Optional.of(new Document("name", new Document("type", "STRING"))));

        return new DocumentValidator(
                contentTypeService,
                new ObjectIdSerializer(),
                new MongoConverter(new ObjectIdSerializer())
        );
    }

    private DocumentValidator mockValidatorNoContentType(ContentTypeService contentTypeService) {
        when(contentTypeService.getContentType(any(), anyString(), anyString()))
                .thenReturn(Optional.empty());

        when(contentTypeService.getDecorator(any(), anyString(), anyString()))
                .thenReturn(Optional.empty());

        return new DocumentValidator(
                contentTypeService,
                new ObjectIdSerializer(),
                new MongoConverter(new ObjectIdSerializer())
        );
    }

    private DocumentValidator mockValidatorOptionalField(ContentTypeService contentTypeService) {
        when(contentTypeService.getContentType(any(), anyString(), anyString()))
                .thenReturn(Optional.of(Collections.singletonList("user")));

        Map<String, Object> map = new LinkedHashMap<>();
        map.put("type", "STRING");
        map.put("required", false);

        when(contentTypeService.getDecorator(any(), anyString(), anyString()))
                .thenReturn(Optional.of(new Document("name", new Document(map))));

        return new DocumentValidator(
                contentTypeService,
                new ObjectIdSerializer(),
                new MongoConverter(new ObjectIdSerializer())
        );
    }


    private MetaExpression makeMetaExpressionDocument(String jsonDocument) throws JsonException, IOException {
        return parseJson(IOUtils.toString(getClass().getResourceAsStream(jsonDocument)));
    }

}