package nl.xillio.xill.plugins.httpserver;

import com.google.inject.Singleton;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class ServerPool {
    private final Map<Integer, Server> servers = new ConcurrentHashMap<>();

    public synchronized Server getServer(int port) throws IOException {
        Server server = servers.get(port);
        if (server == null) {
            server = new Server(HttpServer.create(new InetSocketAddress(port), 0));
            servers.put(port, server);
            server.start();
        }
        return server;
    }
}
