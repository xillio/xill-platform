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
package nl.xillio.xill.plugins.system.constructs;

import nl.xillio.xill.TestUtils;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.errors.RobotRuntimeException;
import nl.xillio.xill.plugins.system.services.version.VersionProvider;
import org.slf4j.Logger;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Test {@link VersionConstruct}
 */
public class VersionConstructTest extends TestUtils {

    /**
     * Test getting the version
     */
    @Test
    public void testProcessGet() {
        // Mock context
        String version = "I am a version";
        VersionProvider provider = mock(VersionProvider.class);
        when(provider.getVersion()).thenReturn(version);

        // Run
        MetaExpression result = VersionConstruct.process(NULL, null, provider);

        // Verify

        // Assert
        Assert.assertSame(result.getStringValue(), version);
    }

    /**
     * Test required version under normal conditions
     */
    @Test
    public void testProcessNormal() {
        // Mock context
        String version = "1.2.3";
        VersionProvider provider = mock(VersionProvider.class);
        when(provider.getVersion()).thenReturn(version);

        MetaExpression expression = mockExpression(ATOMIC);
        when(expression.getStringValue()).thenReturn("1");

        Logger log = mock(Logger.class);

        // Run
        MetaExpression result = VersionConstruct.process(expression, log, provider);

        // Verify
        verify(log, times(0)).error(anyString());

        // Assert
        Assert.assertSame(result.getStringValue(), version);
    }

    /**
     * Test required version under normal conditions with a not supported version number
     */
    @Test(expectedExceptions = RobotRuntimeException.class)
    public void testProcessError() {
        // Mock context
        String version = "1.2.3";
        VersionProvider provider = mock(VersionProvider.class);
        when(provider.getVersion()).thenReturn(version);

        MetaExpression expression = mockExpression(ATOMIC);
        when(expression.getStringValue()).thenReturn("2");

        Logger log = mock(Logger.class);

        // Run
        VersionConstruct.process(expression, log, provider);
    }

    /**
     * Test required version with invalid version string
     */
    @Test
    public void testProcessInvalid() {
        // Mock context
        String version = "1.2.3";
        VersionProvider provider = mock(VersionProvider.class);
        when(provider.getVersion()).thenReturn(version);

        MetaExpression expression = mockExpression(ATOMIC);
        when(expression.getStringValue()).thenReturn("not a version");

        Logger log = mock(Logger.class);

        // Run
        MetaExpression result = VersionConstruct.process(expression, log, provider);

        // Verify
        verify(log).error(anyString(), any(Throwable.class));

        // Assert
        Assert.assertSame(result.getStringValue(), version);
    }

    /**
     * Test required version with invalid version string
     */
    @Test
    public void testProcessDevelop() {
        // Mock context
        String version = VersionProvider.DEVELOP;
        VersionProvider provider = mock(VersionProvider.class);
        when(provider.getVersion()).thenReturn(version);

        MetaExpression expression = mockExpression(ATOMIC);
        when(expression.getStringValue()).thenReturn("not a version");

        Logger log = mock(Logger.class);

        // Run
        MetaExpression result = VersionConstruct.process(expression, log, provider);

        // Verify
        verify(log).warn(anyString());

        // Assert
        Assert.assertSame(result.getStringValue(), version);
    }
}
