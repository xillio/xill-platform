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
package nl.xillio.xill.plugins.template.services;

import freemarker.template.Configuration;
import freemarker.template.MalformedTemplateNameException;
import freemarker.template.Template;
import nl.xillio.xill.TestUtils;
import nl.xillio.xill.api.errors.InvalidUserInputException;
import nl.xillio.xill.api.errors.OperationFailedException;
import org.mockito.Mockito;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import static org.mockito.Mockito.*;

/**
 * The unit tests for the {@link TemplateProcessor}.
 *
 * @author Pieter Soels
 */
public class TemplateProcessorTest extends TestUtils {

    @Test
    public void TestNormalUsage() throws IOException {
        // Mock
        Configuration configuration = mock(Configuration.class);
        Template template = mock(Template.class);
        when(configuration.getTemplate(anyString())).thenReturn(template);
        OutputStream outputStream = mock(OutputStream.class);

        // Instantiate
        TemplateProcessor templateProcessor = new TemplateProcessor();

        // Run
        templateProcessor.generate("", outputStream, null, configuration);

        // Verify
        verify(outputStream, times(1)).flush();
        verify(outputStream, times(0)).close();
    }

    @Test(expectedExceptions = InvalidUserInputException.class)
    public void TestEncodingException() throws Exception {
        // Mock
        OutputStream outputStream = mock(OutputStream.class);
        Configuration configuration = mock(Configuration.class);
        Template template = mock(Template.class);
        when(configuration.getTemplate(anyString())).thenReturn(template);
        Mockito.doThrow(new UnsupportedEncodingException("")).when(template).process(any(), any());

        // Instantiate
        TemplateProcessor templateProcessor = new TemplateProcessor();

        // Run
        templateProcessor.generate("", outputStream, null, configuration);
    }

    @Test(expectedExceptions = OperationFailedException.class)
    public void TestIOException() throws Exception {
        // Mock
        OutputStream outputStream = mock(OutputStream.class);
        Configuration configuration = mock(Configuration.class);
        Template template = mock(Template.class);
        when(configuration.getTemplate(anyString())).thenReturn(template);
        Mockito.doThrow(new IOException(""))
                .when(template).process(any(), any());

        // Instantiate
        TemplateProcessor templateProcessor = new TemplateProcessor();

        // Run
        templateProcessor.generate("", outputStream, null, configuration);
    }

    @Test(expectedExceptions = InvalidUserInputException.class, expectedExceptionsMessageRegExp = ".*The given template could not be found.*")
    public void TestTemplateException() throws IOException {
        // Mock
        OutputStream outputStream = mock(OutputStream.class);

        // Instantiate
        TemplateProcessor templateProcessor = new TemplateProcessor();
        Configuration configuration = new Configuration(Configuration.VERSION_2_3_25);

        // Run
        templateProcessor.generate("", outputStream, null, configuration);
    }

    @Test(expectedExceptions = InvalidUserInputException.class, expectedExceptionsMessageRegExp = ".*invalid name.*")
    public void TestMalformedTemplateNameException() throws Exception {
        // Mock
        OutputStream outputStream = mock(OutputStream.class);
        Configuration configuration = mock(Configuration.class);
        Template template = mock(Template.class);
        when(configuration.getTemplate(anyString())).thenReturn(template);
        Mockito.doThrow(new MalformedTemplateNameException("name", ""))
                .when(template).process(any(), any());

        // Instantiate
        TemplateProcessor templateProcessor = new TemplateProcessor();

        // Run
        templateProcessor.generate("", outputStream, null, configuration);
    }
}