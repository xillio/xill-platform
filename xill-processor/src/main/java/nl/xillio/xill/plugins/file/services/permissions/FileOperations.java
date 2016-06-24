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
package nl.xillio.xill.plugins.file.services.permissions;

import java.util.HashMap;
import java.util.Map;

/**
 * This class represents a collection of file permission flags.
 *
 * @author Thomas Biesaart
 */
public class FileOperations {
    private final boolean readable;
    private final boolean writable;
    private final boolean executable;

    public FileOperations(boolean readable, boolean writable, boolean executable) {
        this.readable = readable;
        this.writable = writable;
        this.executable = executable;
    }

    public boolean isReadable() {
        return readable;
    }

    public boolean isWritable() {
        return writable;
    }

    public boolean isExecutable() {
        return executable;
    }

    public Map<String, Boolean> toMap() {
        Map<String, Boolean> result = new HashMap<>();

        result.put("read", readable);
        result.put("write", writable);
        result.put("execute", executable);

        return result;
    }
}
