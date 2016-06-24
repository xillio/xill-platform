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
package nl.xillio.xill.plugins.date;

import com.google.inject.Provides;
import nl.xillio.plugins.XillPlugin;
import nl.xillio.xill.api.data.DateFactory;
import nl.xillio.xill.plugins.date.services.DateServiceImpl;

/**
 * This package includes all date constructs.
 */
public class DateXillPlugin extends XillPlugin {

    @Provides
    DateFactory dateFactory() {
        return new DateServiceImpl();
    }
}
