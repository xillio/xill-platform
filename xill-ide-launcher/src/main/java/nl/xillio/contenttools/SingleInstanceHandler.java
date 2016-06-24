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
package nl.xillio.contenttools;

import me.biesaart.utils.Log;
import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.StringJoiner;

/**
 * This class is a singleton responsible for handling single instance behaviour.
 *
 * @author Xavier Pardonnet
 */
public class SingleInstanceHandler extends ServerSocket {

    private static final Logger LOGGER = Log.get();
    private static final Set<SingleInstanceListener> LISTENERS = new HashSet<>();

    /**
     * The port through which instance can communicate.
     */
    private static final int PORT = 24991;

    private static SingleInstanceHandler instance;

    private SingleInstanceHandler(int port) throws IOException {
        super(port);
    }

    /**
     * Tries to start a socket server on the dedicated port.
     * If it succeeds, listens forever on this port and notifies the listeners when a second instance is started.
     *
     * @throws IOException in case the port is already in use, meaning an instance is already running
     */
    public static void start() throws IOException {
        instance = new SingleInstanceHandler(PORT);
        Thread thread = new Thread(() -> {
            while (true) {
                try (Socket socket = instance.accept();
                     BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                    String message = reader.readLine();
                    LISTENERS.forEach(listener -> listener.newInstance(message.split(",")));
                } catch (Exception e) {
                    LOGGER.error(e.getLocalizedMessage(), e);
                }
            }
        }, SingleInstanceHandler.class.getSimpleName());
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * Communicates (through the dedicated port) with the other instances.
     *
     * @param message the list of robots to open
     */
    public static void notifyListeners(String... message) {
        try (Socket socket = new Socket("127.0.0.1", PORT)) {
            try (PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
                StringJoiner joiner = new StringJoiner(",");
                Arrays.stream(message).forEach(joiner::add);
                out.println(joiner);
            }
        } catch (IOException e) {
            LOGGER.error(e.getLocalizedMessage(), e);
        }
    }

    /**
     * Registers a item as listener, to be updated when a second instance is started.
     *
     * @param listener the listener to add
     */
    public static void addListener(SingleInstanceListener listener) {
        LISTENERS.add(listener);
    }

    /**
     * Removes a listener.
     *
     * @param listener the listener to remove
     */
    public static void removeListener(SingleInstanceListener listener) {
        LISTENERS.remove(listener);
    }

    /**
     * Represents an object that can be updated when a second instance is started.
     */
    public interface SingleInstanceListener {

        /**
         * Called when a second instance is started if the object is registered as a listener of the {@link SingleInstanceHandler}.
         *
         * @param message the list of bots to open
         */
        void newInstance(String... message);
    }
}
