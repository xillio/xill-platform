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


/**
 * This class represents a wrapper around all options regarding NTLM authentication.
 *
 * @author Ernst van Rheenen
 */
public class NTLMOptions {
    private final Credentials credentials;
    private final String workstation;
    private final String domain;

    public NTLMOptions(Credentials credentials, String workstation, String domain) {
        this.credentials = credentials;
        this.workstation = workstation;
        this.domain = domain;
    }

    public Credentials getCredentials() {
        return credentials;
    }

    public String getWorkstation() {
        return workstation;
    }

    public String getDomain() {
        return domain;
    }

    public String getUsername() {
        return credentials.getUsername();
    }

    public String getPassword() {
        return credentials.getPassword();
    }
}
