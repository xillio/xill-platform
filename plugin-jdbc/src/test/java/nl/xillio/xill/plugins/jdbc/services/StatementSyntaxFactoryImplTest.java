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
package nl.xillio.xill.plugins.jdbc.services;

import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.testng.Assert.assertEquals;


public class StatementSyntaxFactoryImplTest {
    private final StatementSyntaxFactory factory = new StatementSyntaxFactoryImpl();

    @Test
    public void testSelectOne() {
        String table = "HelloWorld";
        Map<String, Object> constraints = new HashMap<>();
        constraints.put("id", 3462);
        constraints.put("string", "Hello\\World");

        String sql = factory.selectOne(table, constraints);

        assertEquals(sql, "SELECT TOP 1 * FROM \"HelloWorld\" WHERE \"string\"=? AND \"id\"=?");
    }

    @Test
    public void testSelectOneNoConstraints() {
        String table = "HelloWorld";

        String sql = factory.selectOne(table, new HashMap<>());

        assertEquals(sql, "SELECT TOP 1 * FROM \"HelloWorld\"");
    }

    @Test
    public void testInsert() {
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("id", 5245);
        values.put("name", "Manners");

        String sql = factory.insert("MyTable", values);

        assertEquals(sql, "INSERT INTO \"MyTable\" (\"id\", \"name\") VALUES (?, ?)");
    }

    @Test
    public void update() {
        Map<String, Object> constraints = new LinkedHashMap<>();
        constraints.put("id", 252462);

        Map<String, Object> values = new LinkedHashMap<>();
        values.put("name", "Manners");

        String sql = factory.update("MyTable", values, constraints);

        assertEquals(sql, "UPDATE \"MyTable\" SET \"name\"=? WHERE \"id\"=?");
    }
}