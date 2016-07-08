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
package nl.xillio.plugins;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;


public class XillPluginTest {

    @Test
    public void testVendorOverride() {
        assertEquals(new VendorOverridePlugin().getVendor().get(), "UnitTest");
    }

    @Test
    public void testVendorUrlOverride() {
        assertEquals(new VendorUrlOverridePlugin().getVendorUrl().get(), "https://en.wikipedia.org/wiki/Unit_testing");
    }

    @Test
    public void testVersionOverride() {
        assertEquals(new VersionOverridePlugin().getVersion().get(), "1.x.x");
    }

    @PluginVendor("UnitTest")
    private static class VendorOverridePlugin extends XillPlugin {
    }

    @PluginVendor(value = "UnitTest", url = "https://en.wikipedia.org/wiki/Unit_testing")
    private static class VendorUrlOverridePlugin extends XillPlugin {
    }

    @PluginVersion("1.x.x")
    private static class VersionOverridePlugin extends XillPlugin {
    }
}
