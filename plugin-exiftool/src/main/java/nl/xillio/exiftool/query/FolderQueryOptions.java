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

/**
 * This interface represents the base interface for all queries that relate to folders.
 *
 * @author Thomas Biesaart
 */
public interface FolderQueryOptions extends QueryOptions {

    boolean isRecursive();

    void setRecursive(boolean recursive);

    Projection getExtensionFilter();

    void setExtensionFilter(Projection filter);
}
