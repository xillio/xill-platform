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
package nl.xillio.xill.plugins.jdbc.data;

import nl.xillio.xill.api.data.Date;
import nl.xillio.xill.api.data.MetadataExpression;

import java.time.temporal.Temporal;

/**
 * {@link MetadataExpression} that can hold a {@link Temporal} value.
 *
 * This can be used to associate a specific Java type to a {@link Date} for usage with the JDBC layer.
 *
 * @author Andrea Parrilli
 */
public class TemporalMetadataExpression implements MetadataExpression {
    private final Temporal data;

    public TemporalMetadataExpression(Temporal data) {
        this.data = data;
    }

    public Temporal getData() {
        return data;
    }
}
