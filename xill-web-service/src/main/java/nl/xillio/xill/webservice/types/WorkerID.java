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
package nl.xillio.xill.webservice.types;

import me.biesaart.utils.Log;
import me.biesaart.utils.RandomUtils;
import org.slf4j.Logger;

/**
 * Unique identifier of the Worker in the WorkerPool.
 */
public class WorkerID {
    private final int id;
    private static final Logger LOGGER = Log.get();

    public WorkerID(int id) {
        this.id = id;
    }

    public WorkerID() {
        this.id = RandomUtils.nextInt(0, Integer.MAX_VALUE);
    }

    public int getId() {
        return id;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof WorkerID && ((WorkerID)obj).id == id;
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public String toString() {
        return Integer.toString(id);
    }
}
