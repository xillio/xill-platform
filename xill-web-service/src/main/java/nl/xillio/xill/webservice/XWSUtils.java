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

/**
 * Utility class for the Xill Web Service module.
 *
 * @author andrea.parrilli
 */
public class XWSUtils {
    public XWSUtils() throws IllegalAccessException {
        throw new IllegalAccessException("Please do not instantiate utility classes");
    }

    public static String getPresentWorkingDirectory() {
        return System.getProperty("user.dir");
    }
}
