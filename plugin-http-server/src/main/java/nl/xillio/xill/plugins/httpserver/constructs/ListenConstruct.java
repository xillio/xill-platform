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

import com.google.inject.Inject;
import com.google.inject.Singleton;
import nl.xillio.xill.api.components.ExpressionDataType;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.construct.Argument;
import nl.xillio.xill.api.construct.Construct;
import nl.xillio.xill.api.construct.ConstructContext;
import nl.xillio.xill.api.construct.ConstructProcessor;
import nl.xillio.xill.api.errors.OperationFailedException;
import nl.xillio.xill.api.io.SimpleIOStream;
import nl.xillio.xill.plugins.httpserver.Request;
import nl.xillio.xill.plugins.httpserver.RobotStatusTracker;
import nl.xillio.xill.plugins.httpserver.Server;
import nl.xillio.xill.plugins.httpserver.ServerPool;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Optional;

@Singleton
public class ListenConstruct extends Construct {
    private final ServerPool serverPool;

    @Inject
    public ListenConstruct(ServerPool serverPool) {
        this.serverPool = serverPool;
    }

    @Override
    public ConstructProcessor prepareProcess(ConstructContext context) {
        return new ConstructProcessor(
                port -> process(port, context),
                new Argument("port", fromValue(8765), ExpressionDataType.ATOMIC)
        );
    }

    private MetaExpression process(MetaExpression port, ConstructContext context) {
        try {
            Server server = serverPool.getServer(port.getNumberValue().intValue());

            // wait for the request or robot stop
            Optional<Request> getRequest = Optional.empty();
            RobotStatusTracker status = new RobotStatusTracker();
            context.addRobotInterruptListener(status);

            while (!getRequest.isPresent() && !status.isStopped()) {
                getRequest = server.waitForRequest();
            }
            context.removeRobotInterruptListener(status);
            if (getRequest.isPresent()) {
                Request request = getRequest.get();
                LinkedHashMap<String, MetaExpression> resultMap = new LinkedHashMap<>();
                resultMap.put("method", fromValue(request.getMethod()));
                resultMap.put("path", fromValue(request.getPath()));
                resultMap.put("query", fromValue(request.getQuery()));
                resultMap.put("headers", parseObject(request.getHeaders()));
                resultMap.put("body", fromValue(new SimpleIOStream(
                        request.getBody(),
                        "Request Body"
                )));
                MetaExpression result = fromValue(resultMap);
                result.storeMeta(request);
                return result;
            } else {
                return NULL; // no need to return a request when the robot has stopped.
            }
        } catch (IOException e) {
            throw new OperationFailedException("start server", e.getMessage());
        }
    }
}
