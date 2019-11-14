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
