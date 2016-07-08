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
package nl.xillio.exiftool.query;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * This interface represents a filter of tags that should be fetched.
 *
 * @author Thomas Biesaart
 */
public class Projection extends HashMap<String, Boolean> {

    /**
     * Get the tag arguments for this projection.
     *
     * @return the arguments
     */
    public List<String> buildArguments() {
        List<String> result = new ArrayList<>();

        forEach((key, value) -> {
            String prefix = value ? "-" : "--";
            result.add(prefix + key);
        });

        return result;
    }
}
