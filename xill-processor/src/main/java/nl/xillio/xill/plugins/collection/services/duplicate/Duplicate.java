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
package nl.xillio.xill.plugins.collection.services.duplicate;

import com.google.inject.ImplementedBy;
import nl.xillio.xill.plugins.collection.CollectionXillPlugin;
import nl.xillio.xill.services.XillService;

/**
 * This interface represents the duplicate operation for the {@link CollectionXillPlugin}
 *
 * @author Sander Visser
 */
@ImplementedBy(DuplicateImpl.class)
public interface Duplicate extends XillService {

    /**
     * Receives either a LIST or an OBJECT and returns a deepcopy of it.
     *
     * @param input the list
     * @return the copy of the list.
     */
    public Object duplicate(Object input);

}
