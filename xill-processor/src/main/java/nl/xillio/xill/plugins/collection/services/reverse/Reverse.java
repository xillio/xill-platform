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
package nl.xillio.xill.plugins.collection.services.reverse;

import com.google.inject.ImplementedBy;
import nl.xillio.xill.plugins.collection.CollectionXillPlugin;
import nl.xillio.xill.services.XillService;

/**
 * This interface represents the reverse operation for the {@link CollectionXillPlugin}
 *
 * @author Sander
 */
@ImplementedBy(ReverseImpl.class)
public interface Reverse extends XillService {

    /**
     * Receives either a LIST or an OBJECT and returns the reversed version.
     *
     * @param input     the list
     * @param recursive whether lists inside the list should be reversed too.
     * @return the reversed list.
     */
    public Object asReversed(Object input, boolean recursive);

}
