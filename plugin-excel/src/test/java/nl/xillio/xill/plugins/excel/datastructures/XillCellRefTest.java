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
package nl.xillio.xill.plugins.excel.datastructures;

import org.testng.annotations.Test;

/**
 * Unit tests for the XillCellRef data structure
 *
 * @author Daan Knoope
 */
public class XillCellRefTest {

    /**
     * Throws IllegalArgumentException when column < 1
     *
     * @throws Exception
     */
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void constructorTestInvalidRow() throws Exception {
        new XillCellRef(1, 0);
    }

    /**
     * Throws IllegalArgumentException when row < 1
     *
     * @throws Exception
     */
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void constructorTestInvalidColumn() throws Exception {
        new XillCellRef(0, 1);
    }

    /**
     * Tests if constructor accepts correct values
     *
     * @throws Exception
     */
    @Test
    public void constructorXillCellRef() throws Exception {
        new XillCellRef(1, 1);
        new XillCellRef("A", 1);
        new XillCellRef("ZA", 92);
    }
}
