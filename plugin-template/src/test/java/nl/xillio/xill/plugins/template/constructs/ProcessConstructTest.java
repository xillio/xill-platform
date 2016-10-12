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
package nl.xillio.xill.plugins.template.constructs;

import freemarker.template.Configuration;
import nl.xillio.xill.TestUtils;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.construct.ConstructContext;
import nl.xillio.xill.api.errors.InvalidUserInputException;
import nl.xillio.xill.api.errors.OperationFailedException;
import nl.xillio.xill.api.io.SimpleIOStream;
import nl.xillio.xill.plugins.template.data.EngineMetadata;
import nl.xillio.xill.plugins.template.services.ConfigurationFactory;
import nl.xillio.xill.plugins.template.services.TemplateProcessor;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.mockito.Mockito.*;

/**
 * The unit tests for the {@link ProcessConstruct}.
 *
 * @author Pieter Soels
 */
public class ProcessConstructTest extends TestUtils {
    @Test
    public void testNormalUsage() {
        // Mock
        Configuration configuration = mock(Configuration.class);
        ConfigurationFactory configurationFactory = mock(ConfigurationFactory.class);
        EngineMetadata metadata = mock(EngineMetadata.class);
        TemplateProcessor templateProcessor = mock(TemplateProcessor.class);
        when(metadata.getConfiguration()).thenReturn(configuration);

        // Instantiate
        ProcessConstruct processConstruct = new ProcessConstruct(templateProcessor, configurationFactory);
        MetaExpression engine = fromValue("engine");
        engine.storeMeta(metadata);

        // Run
        MetaExpression result = process(
                processConstruct,
                fromValue(""),
                fromValue(new SimpleIOStream(new ByteArrayOutputStream(), "description")),
                emptyObject(),
                engine);

        // Assert
        verify(metadata, times(1)).getConfiguration();
        verify(templateProcessor, times(1)).generate(anyString(), any(), any(), any());
        verify(configurationFactory, times(0)).buildDefaultConfiguration(any(ConstructContext.class));
        Assert.assertTrue(result.isNull());
    }

    @Test
    public void testWithoutEngine() {
        // Mock
        Configuration configuration = mock(Configuration.class);
        ConfigurationFactory configurationFactory = mock(ConfigurationFactory.class);
        TemplateProcessor templateProcessor = mock(TemplateProcessor.class);
        when(configurationFactory.buildDefaultConfiguration(any(ConstructContext.class))).thenReturn(configuration);

        // Instantiate
        ProcessConstruct processConstruct = new ProcessConstruct(templateProcessor, configurationFactory);

        // Run
        MetaExpression result = process(
                processConstruct,
                fromValue(""),
                fromValue(new SimpleIOStream(new ByteArrayOutputStream(), "description")),
                emptyObject(),
                NULL);

        // Assert
        verify(templateProcessor, times(1)).generate(anyString(), any(), any(), any());
        verify(configurationFactory, times(1)).buildDefaultConfiguration(any(ConstructContext.class));
        Assert.assertTrue(result.isNull());
    }

    @Test(expectedExceptions = InvalidUserInputException.class)
    public void testInvalidEngine() {
        // Mock
        Configuration configuration = mock(Configuration.class);
        ConfigurationFactory configurationFactory = mock(ConfigurationFactory.class);
        TemplateProcessor templateProcessor = mock(TemplateProcessor.class);

        // Instantiate
        ProcessConstruct processConstruct = new ProcessConstruct(templateProcessor, configurationFactory);

        // Run
        process(
                processConstruct,
                fromValue(""),
                fromValue(new SimpleIOStream(new ByteArrayOutputStream(), "description")),
                emptyObject(),
                fromValue(true));
    }

    @Test(expectedExceptions = OperationFailedException.class)
    public void testNoOutputStreamGiven() {
        // Mock
        ConfigurationFactory configurationFactory = mock(ConfigurationFactory.class);
        TemplateProcessor templateProcessor = mock(TemplateProcessor.class);

        // Instantiate
        ProcessConstruct processConstruct = new ProcessConstruct(templateProcessor, configurationFactory);

        // Run
        process(processConstruct, fromValue(""), fromValue(""), emptyObject(), NULL);
    }
}