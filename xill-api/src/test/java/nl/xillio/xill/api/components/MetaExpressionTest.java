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
import nl.xillio.xill.services.json.JacksonParser;
import org.testng.annotations.Test;

import java.lang.reflect.Field;
import java.sql.Date;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.LinkedHashMap;

import static org.testng.Assert.assertEquals;


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
                        instant -> () -> ZonedDateTime.ofInstant(instant, ZoneId.systemDefault())
                );
            }
        });
    }

    @Test
    public void testUsePrettyJson() throws NoSuchFieldException, IllegalAccessException {
        Field injectorField = MetaExpression.class.getDeclaredField("prettyJsonParser");
        injectorField.setAccessible(true);
        Object currentValue = injectorField.get(null);

        try {
            injectorField.set(null, new JacksonParser(true));

            LinkedHashMap<String, MetaExpression> map = new LinkedHashMap<>();
            map.put("test", ExpressionBuilderHelper.fromValue("value"));
            MetaExpression me = ExpressionBuilderHelper.fromValue(map);
            me.usePrettyJson(true);
            String result = me.getStringValue();

            assertEquals(result, String.format("{%1$s" +
                    "  \"test\" : \"value\"%1$s" +
                    "}", System.getProperty("line.separator")));
        } finally {
            injectorField.set(null, currentValue);
        }
    }
}
