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
package nl.xillio.xill.webservice;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

import java.net.URI;

/**
 * This matcher will check if the input is a valid url.
 *
 * @author Thomas Biesaart
 */
public class IsValidUrlMatcher extends BaseMatcher<String> {

    private IsValidUrlMatcher() {

    }

    @Override
    public boolean matches(Object item) {
        if (item == null) {
            return false;
        }
        try {
            URI.create(item.toString());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("is a valid URL");
    }

    public static Matcher<String> isValidUrl() {
        return new IsValidUrlMatcher();
    }
}
