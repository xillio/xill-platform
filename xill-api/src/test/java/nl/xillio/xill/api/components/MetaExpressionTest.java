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
package nl.xillio.xill.api.components;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import nl.xillio.xill.api.data.DateFactory;
import nl.xillio.xill.api.data.MetadataExpression;
import nl.xillio.xill.api.io.IOStream;
import org.testng.annotations.Test;

import java.lang.reflect.Field;
import java.sql.Date;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collection;

import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;


public class MetaExpressionTest {

    // Sadly we have to test this using injection because of the static fields
    @Test
    public void testSqlDateExtraction() throws NoSuchFieldException, IllegalAccessException {
        // Inject factory
        Injector testInjector = getDateInjector();
        Field injectorField = MetaExpression.class.getDeclaredField("injector");
        injectorField.setAccessible(true);
        Object currentValue = injectorField.get(null);

        try {
            // Place the test injector for testing
            injectorField.set(null, testInjector);
            long time = 1465897665;

            Date inputDate = new Date(time);

            MetaExpression result = MetaExpression.parseObject(inputDate);
            nl.xillio.xill.api.data.Date date = result.getMeta(nl.xillio.xill.api.data.Date.class);
            long resultTime = date.getZoned().toEpochSecond();

            assertEquals(resultTime, time / 1000);

        } finally {
            // Recover the injector
            injectorField.set(null, currentValue);
        }

    }

    public Injector getDateInjector() {
        return Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bind(DateFactory.class).toInstance(
                        instant -> new nl.xillio.xill.api.data.Date() {
                            @Override
                            public ZonedDateTime getZoned() {
                                return ZonedDateTime.ofInstant(instant, ZoneId.systemDefault());
                            }

                            @Override
                            public nl.xillio.xill.api.data.Date copy() {
                                return null;
                            }
                        }
                );
            }
        });
    }

    /**
     * Test {@link MetaExpression#copy()}
     */
    @Test
    public void testCopyWithMetadata() {
        // Mock
        MetaExpression copy = mock(MetaExpression.class);
        MetaExpression test = new TestingMetadataExpression(copy);
        MetadataExpression notCopyable = mock(MetadataExpression.class);
        test.storeMeta(notCopyable);
        CopyableMetadataExpression copyable = mock(CopyableMetadataExpression.class);
        CopyableMetadataExpression copied =mock(CopyableMetadataExpression.class);
        when(copyable.copy()).thenReturn(copied);
        test.storeMeta(copyable);

        // Run
        MetaExpression result = test.copy();

        // Verify
        verify(copy).storeMeta(copied);
        verifyNoMoreInteractions(copied, notCopyable);

        // Assert
        assertSame(result, copy);
    }

    /**
     * Test {@link MetaExpression#copy()}
     */
    @Test
    public void testCopy() {
        // Mock
        MetaExpression copy = mock(MetaExpression.class);
        MetaExpression test = new TestingMetadataExpression(copy);
        MetadataExpression notCopyable = mock(MetadataExpression.class);
        test.storeMeta(notCopyable);

        // Run
        MetaExpression result = test.copy();

        // Verify
        verifyNoMoreInteractions(notCopyable);

        // Assert
        assertSame(result, copy);
    }

    /**
     * Stub implementation of MetaExpression for testing
     */
    private class TestingMetadataExpression extends MetaExpression {

        private MetaExpression copy;

        /**
         *
         * @param copy Returned when {@link MetaExpression#copyExpression()} is called
         */
        public TestingMetadataExpression(MetaExpression copy) {
            this.copy = copy;
        }

        @Override
        public Number getNumberValue() {
            return null;
        }

        @Override
        public String getStringValue() {
            return null;
        }

        @Override
        public boolean getBooleanValue() {
            return false;
        }

        @Override
        public boolean isNull() {
            return false;
        }

        @Override
        public IOStream getBinaryValue() {
            return null;
        }

        @Override
        public Collection<Processable> getChildren() {
            return null;
        }

        @Override
        protected MetaExpression copyExpression() {
            return copy;
        }
    }
}
