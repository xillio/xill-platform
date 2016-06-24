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
package nl.xillio.xill.services.json;

import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.services.XillService;

/**
 * This service is capable of parsing objects to and from json.
 *
 * @author Thomas Biesaart
 */
public interface JsonParser extends XillService {
    /**
     * Parses an object to json.
     *
     * @param object the object to parse
     * @return a json string
     * @throws JsonException when parsing the json failed
     */
    String toJson(Object object) throws JsonException;

    /**
     * Parses a {@link MetaExpression} to a json string.
     *
     * @param metaExpression the expression
     * @return a json string
     * @throws JsonException when parsing the json failed
     */
    String toJson(MetaExpression metaExpression) throws JsonException;

    /**
     * Parses a json string to an object.
     *
     * @param <T>  the type of object to build
     * @param json the json string
     * @param type the type of object to build
     * @return the object
     * @throws JsonException when parsing the json failed
     */
    <T> T fromJson(String json, Class<T> type) throws JsonException;
}
