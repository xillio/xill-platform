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
package nl.xillio.xill.plugins.jdbc.constructs;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import nl.xillio.xill.api.data.Date;
import nl.xillio.xill.plugins.jdbc.services.TemporalConversionService;

import java.net.URL;

/**
 * Converts a {@link Date} as created by the {@code Date} plugin into a java type suitable to be bound to a {@code TIMESTAMP} column.
 *
 * @author Andrea Parrilli
 */
public class ToTimestampConstruct extends BaseTemporalConversionConstruct {

    @Inject
    public ToTimestampConstruct(TemporalConversionService temporalConversionService, @Named("docRoot") String docRoot) {
        super(temporalConversionService::toTimestamp, docRoot);
    }

    @Override
    public URL getDocumentationResource() {
        if (docRoot != null) {
            String stringUrl = docRoot + getClass().getSimpleName() + ".xml";
            URL url = getClass().getResource(stringUrl);
            if (url != null) {
                return url;
            }
        }

        return super.getDocumentationResource();
    }
}
