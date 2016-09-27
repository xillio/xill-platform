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
package nl.xillio.xill;

import nl.xillio.xill.api.XillThreadFactory;

/**
 * Default implementation of a {@link XillThreadFactory}. Only creates threads
 * and does not keep track of them.
 *
 * @author Geert Konijnendijk
 */
public class XillThreadFactoryImpl implements XillThreadFactory {
    @Override
    public Thread create(Runnable target, String name) {
        return new Thread(target, name);
    }

    @Override
    public void close() {
        // No cleanup needs to happen here since this implementation does not keep track of created threads
    }
}
