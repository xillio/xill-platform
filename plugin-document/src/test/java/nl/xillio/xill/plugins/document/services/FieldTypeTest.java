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
import nl.xillio.xill.api.data.Date;
import nl.xillio.xill.api.errors.RobotRuntimeException;
import org.testng.annotations.Test;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertEqualsNoOrder;

/**
 * Created by andrea.parrilli on 2016-05-19.
 */
public class FieldTypeTest extends TestUtils {
    @Test
    public void extractValueValid() {
        //Prepare data that could be in a decorator definition
        List<MetaExpression> listSpecimenEmpty = new LinkedList<>();
        List<MetaExpression> listSpecimen = new LinkedList<>();
        listSpecimen.add(fromValue("V1"));
        listSpecimen.add(fromValue(33));
        Date dateSpecimen = () -> ZonedDateTime.of(2000, 1, 1, 1, 1, 1, 1, ZoneId.of("CET"));
        MetaExpression dateSpecimenME = fromValue("a date");
        dateSpecimenME.storeMeta(dateSpecimen);

        assertEquals(FieldType.BOOLEAN.extractValue(fromValue(true)), true);
        assertEquals(FieldType.BOOLEAN.extractValue(fromValue(false)), false);

        assertEqualsNoOrder(((List<MetaExpression>) FieldType.LIST.extractValue(fromValue(listSpecimen))).toArray(), new Object[]{"V1", 33});
        assertEqualsNoOrder(((List<MetaExpression>) FieldType.LIST.extractValue(fromValue(listSpecimenEmpty))).toArray(), listSpecimenEmpty.toArray());

        assertEquals(FieldType.NUMBER.extractValue(fromValue(33)), 33);
        assertEquals(FieldType.NUMBER.extractValue(fromValue(-33.3333)), -33.3333);

        assertEquals(FieldType.STRING.extractValue(fromValue("")), "");
        assertEquals(FieldType.STRING.extractValue(fromValue("The final datatype: STUFF")), "The final datatype: STUFF");

        assertEquals(FieldType.DATE.extractValue(dateSpecimenME), java.util.Date.from(dateSpecimen.getZoned().toInstant()));
    }

    @Test(expectedExceptions = RobotRuntimeException.class)
    public void extractValueInvalidBOOLEAN() {
        MetaExpression list = fromValue(new LinkedList<MetaExpression>());
        FieldType.BOOLEAN.extractValue(list);
    }

    @Test(expectedExceptions = RobotRuntimeException.class)
    public void extractValueInvalidLIST() {
        MetaExpression atomic = fromValue("kitties");
        FieldType.LIST.extractValue(atomic);
    }

    @Test(expectedExceptions = RobotRuntimeException.class)
    public void extractValueInvalidNUMBER() {
        MetaExpression object = fromValue(new LinkedHashMap());
        FieldType.NUMBER.extractValue(object);
    }

    @Test(expectedExceptions = RobotRuntimeException.class)
    public void extractValueInvalidSTRING() {
        MetaExpression list = fromValue(new LinkedList<MetaExpression>());
        FieldType.STRING.extractValue(list);
    }

    @Test(expectedExceptions = RobotRuntimeException.class)
    public void extractValueInvalidDATE1() {
        MetaExpression atomic = fromValue("kitties");
        FieldType.DATE.extractValue(atomic);
    }

    @Test(expectedExceptions = RobotRuntimeException.class)
    public void extractValueInvalidDATE2() {
        MetaExpression object = fromValue(new LinkedHashMap());
        FieldType.DATE.extractValue(object);
    }
}
