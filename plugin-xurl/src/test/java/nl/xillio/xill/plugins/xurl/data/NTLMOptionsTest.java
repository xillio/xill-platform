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
package nl.xillio.xill.plugins.xurl.data;

import org.testng.Assert;
import org.testng.annotations.Test;

public class NTLMOptionsTest {

    private String USERNAME = "user";
    private String PASSWORD = "pass";
    private String WORKSTATION = "workstation";
    private String DOMAIN = "domain.com";
    private final Credentials credentials = new Credentials(USERNAME, PASSWORD);
    private final NTLMOptions options = new NTLMOptions(credentials, WORKSTATION, DOMAIN);


    @Test
    public void testGetCredentials() {
        Assert.assertEquals(credentials, options.getCredentials());
    }

    @Test
    public void testGetUsername() {
        Assert.assertEquals(USERNAME, options.getUsername());
    }

    @Test
    public void testGetPassword() {
        Assert.assertEquals(PASSWORD, options.getPassword());
    }

    @Test
    public void testGetWorkstation() {
        Assert.assertEquals(WORKSTATION, options.getWorkstation());
    }

    @Test
    public void testGetDomain() {
        Assert.assertEquals(DOMAIN, options.getDomain());
    }

}