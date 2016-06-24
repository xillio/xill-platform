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
package nl.xillio.xill.plugins.rest.constructs;

import com.google.inject.Inject;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.construct.Argument;
import nl.xillio.xill.api.construct.Construct;
import nl.xillio.xill.api.construct.ConstructContext;
import nl.xillio.xill.api.construct.ConstructProcessor;
import nl.xillio.xill.plugins.rest.data.MultipartBody;
import nl.xillio.xill.plugins.rest.services.RestService;

/**
 * Add file to multipart REST body
 *
 * @author Zbynek Hochmann
 */
@Deprecated
public class BodyAddFileConstruct extends Construct {

    @Inject
    private RestService restService;

    @Override
    public ConstructProcessor prepareProcess(ConstructContext context) {
        return new ConstructProcessor(
                (body, name, fileName) -> process(body, name, fileName, restService),
                new Argument("body", ATOMIC),
                new Argument("name", ATOMIC),
                new Argument("fileName", ATOMIC)
        );
    }

    static MetaExpression process(final MetaExpression bodyVar, final MetaExpression nameVar, final MetaExpression fileNameVar, final RestService service) {
        MultipartBody body = assertMeta(bodyVar, "body", MultipartBody.class, "REST multipart body");
        String name = nameVar.getStringValue();
        String fileName = fileNameVar.getStringValue();
        service.bodyAddFile(body, name, fileName);
        return NULL;
    }
}
