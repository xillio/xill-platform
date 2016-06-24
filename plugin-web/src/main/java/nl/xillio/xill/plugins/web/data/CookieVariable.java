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
package nl.xillio.xill.plugins.web.data;

import java.util.Date;

/**
 * This is an adapter which represents a webcookie.
 */
public class CookieVariable {

    private String name;
    private String domain = "";
    private String path = "";
    private String value = "";
    private Date expireDate = null;

    /**
     * @return the name of the cookie.
     */
    public String getName() {
        return name;
    }

    /**
     * @return the domain of the cookie.
     */
    public String getDomain() {
        return domain;
    }

    /**
     * @return the path of the cookie.
     */
    public String getPath() {
        return path;
    }

    /**
     * @return the value of the cookie.
     */
    public String getValue() {
        return value;
    }

    /**
     * @return the expiration date of the cookie.
     */
    public Date getExpireDate() {
        return expireDate;
    }

    /**
     * Set the name of the cookie.
     *
     * @param name The name.
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * Set the domain of the cookie.
     *
     * @param name The name of the domain.
     */
    public void setDomain(final String name) {
        domain = name;
    }

    /**
     * Set the path of the cookie.
     *
     * @param name The name of the path.
     */
    public void setPath(final String name) {
        path = name;
    }

    /**
     * Set the value of the cookie.
     *
     * @param name The name of the value.
     */
    public void setValue(final String name) {
        value = name;
    }

    /**
     * Set the expiration date of the cookie.
     *
     * @param date The expiration date.
     */
    public void setExpireDate(final Date date) {
        expireDate = date;
    }
}
