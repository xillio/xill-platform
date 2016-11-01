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
package nl.xillio.xill.docgen.impl;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import nl.xillio.xill.docgen.DocumentationEntity;
import nl.xillio.xill.docgen.exceptions.ParsingException;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;

/**
 * Unit test the {@link FreeMarkerDocumentationGenerator}
 *
 * @author Thomas Biesaart
 * @since 14-8-2015
 */
public class FreeMarkerDocumentationGeneratorTest {
    private static final File ROOT_FOLDER = new File(".");

    @Test
    public void testGenerateSuccess() throws Exception {
        FreeMarkerDocumentationGenerator generator = spyGenerator();

        // Entity
        DocumentationEntity entity = mock(ConstructDocumentationEntity.class);
        when(entity.getIdentity()).thenReturn("__TEST__");

        // Template
        Template template = mock(Template.class);
        doReturn(template).when(generator).getTemplate(anyString());

        // Model
        Map<String, Object> model = new HashMap<>();
        doReturn(model).when(generator).getModel(entity);

        //Generator
        doNothing().when(generator).doGenerate(any(Template.class), any(File.class), same(model));
        doNothing().when(generator).assertNotClosed();

        // Call the method
        generator.generate(entity);

        // Verify call
        verify(generator, times(1)).doGenerate(same(template), any(File.class), same(model));

    }

    @Test(expectedExceptions = IllegalStateException.class, expectedExceptionsMessageRegExp = "CLOSED")
    public void testGenerateClosed() throws ParsingException, IOException, TemplateException {
        FreeMarkerDocumentationGenerator generator = spyGenerator();
        doThrow(new IllegalStateException("CLOSED")).when(generator).assertNotClosed();

        // Call method
        generator.generate(null);

        // Verify that no calls are made
        verify(generator, times(0)).getTemplate(anyString());
        verify(generator, times(0)).getModel(any());
        verify(generator, times(0)).doGenerate(any(), any(), any());

    }

    @Test(expectedExceptions = ParsingException.class, expectedExceptionsMessageRegExp = "Failed to write to .*")
    public void testGenerateIOException() throws ParsingException, IOException, TemplateException {
        FreeMarkerDocumentationGenerator generator = spyGenerator();
        doReturn(null).when(generator).getTemplate(anyString());
        doReturn(null).when(generator).getModel(any());
        doThrow(new IOException("")).when(generator).doGenerate(any(), any(), any());

        // Entity
        DocumentationEntity entity = mock(ConstructDocumentationEntity.class);

        // Call method
        generator.generate(entity);
    }

    @Test(expectedExceptions = ParsingException.class, expectedExceptionsMessageRegExp = "Error in template __TEMPLATE__")
    public void testGenerateTemplateException() throws ParsingException, IOException, TemplateException {
        FreeMarkerDocumentationGenerator generator = spyGenerator();
        doReturn(null).when(generator).getModel(any());
        doThrow(new TemplateException(null)).when(generator).doGenerate(any(), any(), any());

        // Entity
        DocumentationEntity entity = mock(ConstructDocumentationEntity.class);

        // Template
        Template template = mock(Template.class);
        doReturn("__TEMPLATE__").when(template).getName();
        doReturn(template).when(generator).getTemplate(anyString());

        // Call method
        generator.generate(entity);
    }

    @Test
    public void testAssertNotClosedSuccess() throws Exception {
        FreeMarkerDocumentationGenerator generator = spyGenerator();
        generator.assertNotClosed();
    }

    @Test(expectedExceptions = IllegalStateException.class, expectedExceptionsMessageRegExp = "This generator has already been closed")
    public void testAssertNotClosedFailure() throws Exception {
        FreeMarkerDocumentationGenerator generator = spyGenerator();
        doNothing().when(generator).generatePackageIndex();
        doNothing().when(generator).saveJsonSummary();

        generator.close();
        generator.assertNotClosed();
    }

    @Test
    public void testGenerateIndexSuccess() throws Exception {
        FreeMarkerDocumentationGenerator generator = spyGenerator();

        // getPackagesFromJson
        List<PackageDocumentationEntity> packages = Collections.emptyList();
        doReturn(packages).when(generator).getPackagesFromJson();

        // getTemplate
        Template template = mock(Template.class);
        doReturn(template).when(generator).getTemplate(anyString());

        // goGenerate
        doNothing().when(generator).doGenerate(any(), any(), any());

        // Call the method
        generator.generateIndex();

        // Verify calls
        verify(generator, times(1)).doGenerate(same(template), any(File.class), any());
    }

    @Test(expectedExceptions = ParsingException.class, expectedExceptionsMessageRegExp = "Failed to write to .*")
    void testGenerateIndexIOException() throws ParsingException, IOException, TemplateException {
        FreeMarkerDocumentationGenerator generator = spyGenerator();
        doReturn(Collections.emptyList()).when(generator).getPackagesFromJson();
        doReturn(null).when(generator).getTemplate(anyString());
        doThrow(new IOException()).when(generator).doGenerate(any(), any(), any());

        // Call the method
        generator.generateIndex();
    }

    @Test(expectedExceptions = ParsingException.class, expectedExceptionsMessageRegExp = "Syntax error")
    void testGenerateIndexTemplateException() throws ParsingException, IOException, TemplateException {
        FreeMarkerDocumentationGenerator generator = spyGenerator();
        doReturn(Collections.emptyList()).when(generator).getPackagesFromJson();
        doReturn(null).when(generator).getTemplate(anyString());
        doThrow(new TemplateException(null)).when(generator).doGenerate(any(), any(), any());

        // Call the method
        generator.generateIndex();
    }

    @Test
    public void testGetPackagesFromJsonSuccess() throws Exception {
        FreeMarkerDocumentationGenerator generator = spyGenerator();

        // Subfolder
        File[] folders = new File[]{null, null};
        doReturn(folders).when(generator).getDocumentationSubFolders();

        // getJsonFile
        File jsonFile = mock(File.class);
        doReturn(null).doReturn(jsonFile).when(generator).getJsonFile(null);

        // getJsonFromFile
        String json = "{}";
        doReturn(json).when(generator).getJsonFromFile(jsonFile);

        // Call the method
        List<PackageDocumentationEntity> result = generator.getPackagesFromJson();

        assertEquals(result.size(), 1);
    }

    @Test
    public void testGetPackagesFromJsonIOException() throws IOException {
        FreeMarkerDocumentationGenerator generator = spyGenerator();

        // Subfolder
        File[] folders = new File[]{null};
        doReturn(folders).when(generator).getDocumentationSubFolders();

        // getJsonFile
        File jsonFile = mock(File.class);
        doReturn(jsonFile).when(generator).getJsonFile(null);

        // getJsonFromFile
        IOException exception = new IOException();
        doThrow(exception).when(generator).getJsonFromFile(jsonFile);

        // Call the method
        generator.getPackagesFromJson();

        // Assert success, exception was caught

    }

    @Test
    public void testCloseSuccess() throws Exception {
        FreeMarkerDocumentationGenerator generator = spyGenerator();
        doNothing().when(generator).generatePackageIndex();
        doNothing().when(generator).saveJsonSummary();

        // Call the method
        generator.close();

        // Verify calls
        verify(generator).assertNotClosed();
        verify(generator).generatePackageIndex();
        verify(generator).saveJsonSummary();
    }

    @Test(expectedExceptions = ParsingException.class, expectedExceptionsMessageRegExp = "__ERROR__")
    public void testCloseParsingException() throws Exception {
        FreeMarkerDocumentationGenerator generator = spyGenerator();
        doThrow(new ParsingException("__ERROR__")).when(generator).generatePackageIndex();

        // Call the method
        generator.close();
    }

    @Test
    public void testGeneratePackageIndex() throws Exception {
        FreeMarkerDocumentationGenerator generator = spyGenerator();
        doReturn(null).when(generator).getTemplate(anyString());
        doReturn(null).when(generator).getModel(any());
        doNothing().when(generator).doGenerate(any(), any(), any());

        generator.generatePackageIndex();

        verify(generator).doGenerate(any(), any(), any());
    }

    @Test
    public void testGetTemplate() throws IOException, ParsingException {
        Configuration config = mock(Configuration.class);
        FreeMarkerDocumentationGenerator generator = spy(new FreeMarkerDocumentationGenerator("__UNIT_TEST__", config, ROOT_FOLDER));
        Template template = mock(Template.class);
        when(config.getTemplate("name.html")).thenReturn(template);

        // Call the method
        Template result = generator.getTemplate("name");

        assertSame(template, result);
    }

    @Test(expectedExceptions = ParsingException.class)
    public void testGetTemplateIOException() throws IOException, ParsingException {
        Configuration config = mock(Configuration.class);
        FreeMarkerDocumentationGenerator generator = spy(new FreeMarkerDocumentationGenerator("__UNIT_TEST__", config, ROOT_FOLDER));
        when(config.getTemplate("name.html")).thenThrow(new IOException());

        // Call the method
        generator.getTemplate("name");
    }

    @Test
    public void testGetModelValues() throws IOException {
        FreeMarkerDocumentationGenerator generator = spyGenerator();
        DocumentationEntity entity = mock(DocumentationEntity.class);
        Map<String, Object> props = new HashMap<>();
        props.put("testKey", "testValue");
        when(entity.getProperties()).thenReturn(props);

        Map<String, Object> result = generator.getModel(entity);

        assertSame(result.get("testKey"), props.get("testKey"));
        assertEquals(result.get("packageName"), "__UNIT_TEST__");

    }

    private FreeMarkerDocumentationGenerator spyGenerator() throws IOException {
        FreeMarkerDocumentationGenerator generator = spy(new FreeMarkerDocumentationGenerator("__UNIT_TEST__", mockConfig(), ROOT_FOLDER));
        doNothing().when(generator).touch(any(File.class));
        return generator;
    }

    private Configuration mockConfig() {

        return mock(Configuration.class);
    }
}