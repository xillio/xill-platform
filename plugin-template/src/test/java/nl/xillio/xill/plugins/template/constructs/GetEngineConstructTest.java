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
import nl.xillio.xill.plugins.template.data.EngineMetadata;
import nl.xillio.xill.plugins.template.services.ConfigurationFactory;
import org.testng.annotations.Test;

import static org.mockito.Mockito.*;
import static org.testng.Assert.assertSame;

/**
 * The unit tests for the {@link GetEngineConstruct}.
 *
 * @author Pieter Soels
 */
public class GetEngineConstructTest extends TestUtils {

    @Test
    public void testNormalUsage() {
        // Mock
        Configuration configuration = mock(Configuration.class);
        ConfigurationFactory configurationFactory = mock(ConfigurationFactory.class);
        when(configurationFactory.parseConfiguration(any(), any())).thenReturn(configuration);

        // Instantiate
        GetEngineConstruct getEngineConstruct = new GetEngineConstruct(configurationFactory);

        // Run
        MetaExpression result = process(getEngineConstruct, emptyObject());

        // Assert
        Configuration resultConfiguration = result.getMeta(EngineMetadata.class).getConfiguration();
        assertSame(resultConfiguration, configuration);
    }
}