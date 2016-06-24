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
package nl.xillio.xill.plugins.exiftool.services;

import com.google.inject.Singleton;
import nl.xillio.exiftool.query.Projection;
import nl.xillio.xill.api.components.ExpressionDataType;
import nl.xillio.xill.api.components.MetaExpression;

import java.util.Map;

/**
 * This class is responsible for building a projection object from a MetaExpression.
 *
 * @author Thomas Biesaart
 */
@Singleton
public class ProjectionFactory {

    public Projection build(MetaExpression object) {
        if (object.getType() != ExpressionDataType.OBJECT) {
            throw new IllegalArgumentException("A projection must be an OBJECT");
        }

        Projection result = new Projection();

        Map<String, MetaExpression> map = object.getValue();

        map.forEach((key, value) -> result.put(key, value.getBooleanValue()));

        return result;
    }
}
