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
package nl.xillio.exiftool;

import nl.xillio.exiftool.query.FolderQueryOptions;
import nl.xillio.exiftool.query.Projection;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents the options needed for a query on a folder.
 *
 * @author Thomas Biesaart
 */
public class FolderQueryOptionsImpl extends AbstractQueryOptions implements FolderQueryOptions {

    private boolean recursive = true;
    private Projection extensionFilter = new Projection();

    @Override
    public boolean isRecursive() {
        return recursive;
    }

    @Override
    public void setRecursive(boolean recursive) {
        this.recursive = recursive;
    }

    @Override
    public Projection getExtensionFilter() {
        return extensionFilter;
    }

    @Override
    public void setExtensionFilter(Projection filter) {
        this.extensionFilter = filter;
    }

    @Override
    public List<String> buildArguments() {
        List<String> result = new ArrayList<>();
        if (isRecursive()) {
            result.add("-r");
        }

        if (extensionFilter.isEmpty()) {
            result.add("-ext");
            result.add("*");
        } else {
            extensionFilter.forEach(
                    (extension, include) -> {
                        if (include) {
                            result.add("-ext");
                        } else {
                            result.add("--ext");
                        }
                        result.add(extension);
                    }
            );
        }
        return result;
    }
}
