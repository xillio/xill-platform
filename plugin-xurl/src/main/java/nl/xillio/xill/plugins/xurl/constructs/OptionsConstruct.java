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
package nl.xillio.xill.plugins.xurl.constructs;

import org.apache.http.client.fluent.Request;

import java.net.URI;

/**
 * This construct will perform options requests using the abstract request framework.
 *
 * @author Thomas Biesaart
 */
@NoBody
public class OptionsConstruct extends AbstractRequestConstruct {

    @Override
    protected Request buildRequest(URI uri) {
        return Request.Options(uri);
    }
}
