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
package nl.xillio.xill.plugins.document.util;

import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Test the {@link Require} functionality
 */
public class RequireTest {
    @Test
    public void notNullTestOk() {
        Require.notNull(new Object(), "", 33, new Long(33));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void notNullTestFail() {
        Object nullO = null;
        Require.notNull(nullO);
    }

    @Test
    public void notEmptyTestOk() {
        Collection<Object> c = new ArrayList();
        c.add(new Object());
        Require.notEmpty("asdf", c);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void notEmptyTestFail1() {
        Require.notEmpty("", "asdfg");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void notEmptyTestFail2() {
        ArrayList<Object> cEmpty = new ArrayList();
        Collection<Object> cFull = new ArrayList();
        cFull.add(new Object());

        Require.notEmpty(cEmpty, cFull);
    }
}
