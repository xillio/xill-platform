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
package nl.xillio.xill.plugins.httpserver;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import nl.xillio.xill.api.errors.OperationFailedException;

import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class Server {
    private final HttpServer httpServer;
    private BlockingQueue<Request> requests = new ArrayBlockingQueue<>(500);

    public Server(HttpServer httpServer) {
        this.httpServer = httpServer;
        httpServer.createContext("/", this::handle);
        httpServer.setExecutor(null);
    }

    public void start() {
        httpServer.start();
    }

    private void handle(HttpExchange httpExchange) {
        try {
            Request request = new Request(httpExchange);
            requests.put(request);
        } catch (Exception e) {
            throw new OperationFailedException("store request", e.getMessage());
        }
    }

    public Optional<Request> waitForRequest() {
        try {
            return Optional.ofNullable(requests.poll(3, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            throw new OperationFailedException("get request", e.getMessage());
        }
    }
}
