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
package nl.xillio.xill.plugins.httpserver.constructs;

import com.google.inject.Singleton;
import nl.xillio.xill.api.components.ExpressionDataType;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.construct.Argument;
import nl.xillio.xill.api.construct.Construct;
import nl.xillio.xill.api.construct.ConstructContext;
import nl.xillio.xill.api.construct.ConstructProcessor;
import nl.xillio.xill.api.errors.OperationFailedException;
import nl.xillio.xill.plugins.httpserver.Request;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@Singleton
public class RespondConstruct extends Construct {
    @Override
    public ConstructProcessor prepareProcess(ConstructContext context) {
        return new ConstructProcessor(
                this::process,
                new Argument("request", ExpressionDataType.OBJECT),
                new Argument("status", NULL, ExpressionDataType.ATOMIC),
                new Argument("headers", NULL),
                new Argument("body", NULL)
        );
    }

    private MetaExpression process(MetaExpression requestVar, MetaExpression statusVar, MetaExpression headersVar, MetaExpression bodyVar) {
        Request request = requestVar.getMeta(Request.class);
        try {
            Map<String, Object> headers = headersVar.getType() == OBJECT ? (Map) extractValue(headersVar) : new HashMap<>();
            InputStream body = bodyVar.isNull() ? null : bodyVar.getBinaryValue().getInputStream();
            int defaultStatus = body == null ? 204 : 200;
            int status = statusVar.isNull() ?
                    defaultStatus :
                    statusVar.getNumberValue().intValue();
            request.sendResponse(
                    status,
                    headers,
                    body
            );
            return TRUE;
        } catch (IOException e) {
            throw new OperationFailedException("write response", e.getMessage());
        }
    }
}
