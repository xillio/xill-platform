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
package nl.xillio.migrationtool.gui;

import me.biesaart.utils.Log;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.nio.file.StandardWatchEventKinds.*;

public class WatchDir implements Runnable {
    private static final Logger LOGGER = Log.get();

    private final WatchService watcher;
    private volatile Map<WatchKey, Path> keys;
    private volatile Map<FolderListener, List<Path>> listeners;
    private boolean stop = false;

    private static int threadCounter = 0;

    /**
     * Create a WatchService.
     */
    public WatchDir() throws IOException {
        watcher = FileSystems.getDefault().newWatchService();
        keys = new HashMap<>();
        listeners = new HashMap<>();
    }

    /**
     * Create a daemon thread with approprioate name from this WatchDir.
     *
     * @return The created thread.
     */
    public Thread createThread() {
        Thread thread = new Thread(this);
        thread.setDaemon(true);
        thread.setName("watchdir-" + threadCounter++);
        return thread;
    }

    /**
     * Add a folder listener to the path.
     *
     * @param listener The folder listener to add.
     * @param dir      The path to listen for changes.
     * @throws IOException if an I/O error occurs while registering the dir.
     */
    public void addFolderListener(final FolderListener listener, final Path dir) throws IOException {
        List<Path> paths;
        if (listeners.containsKey(listener)) {
            paths = listeners.get(listener);
        } else {
            paths = new LinkedList<>();
            listeners.put(listener, paths);
        }

        if (!paths.contains(dir)) {
            paths.add(dir);
        }

        registerAll(dir);
    }

    /**
     * Stop the WatchDir.
     */
    public void stop() {
        stop = true;
    }

    /**
     * Register the given directory with the WatchService.
     */
    private void registerDir(final Path dir) throws IOException {
        WatchKey key = dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
        keys.put(key, dir);
    }

    /**
     * Register the given directory and all its sub-directories with the WatchService.
     *
     * @param start The top path to register.
     * @throws IOException if an I/O error occurs while registering one of the directories.
     */
    private void registerAll(final Path start) throws IOException {
        // Register directory and sub-directories.
        Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes att) throws IOException {
                registerDir(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    @SuppressWarnings("unchecked")
    private static <T> WatchEvent<T> cast(final WatchEvent<?> event) {
        return (WatchEvent<T>) event;
    }

    @Override
    public void run() {
        while (!stop) {
            // Wait for key to be signalled.
            WatchKey key = tryGetWatchKey();
            if (key == null) {
                break;
            }

            // Get the path, if it is null ignore the event.
            Path dir = keys.get(key);
            if (dir == null) {
                continue;
            }

            key.pollEvents().forEach(event -> handleEvent(dir, cast(event)));

            // Reset the key, if it is invalid remove it from the keys.
            if (!key.reset()) {
                keys.remove(key);

                // All directories are inaccessible.
                if (keys.isEmpty()) {
                    break;
                }
            }
        }

        closeWatcher();
    }

    @SuppressWarnings("squid:S1166") // InterruptedException thrown by watcher.take() is handled correctly.
    private WatchKey tryGetWatchKey() {
        try {
            return watcher.take();
        } catch (InterruptedException | ClosedWatchServiceException e) {
            return null;
        }
    }

    private void handleEvent(final Path dir, final WatchEvent<Path> event) {
        // Context for directory entry event is the file name of entry.
        Path name = event.context();
        Path child = dir.resolve(name);

        // Call folderChanged for each path under dir for each listener.
        for (Map.Entry<FolderListener, List<Path>> entry : listeners.entrySet()) {
            entry.getValue().stream().filter(dir::startsWith).forEach(p -> entry.getKey().folderChanged(dir, child, event));
        }

        // Register new directories that were created in this event.
        if (event.kind() == ENTRY_CREATE) {
            registerNewDirectories(child);
        }
    }

    private void registerNewDirectories(final Path child) {
        try {
            if (Files.isDirectory(child, NOFOLLOW_LINKS)) {
                registerAll(child);
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    private void closeWatcher() {
        try {
            watcher.close();
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    public interface FolderListener {
        void folderChanged(final Path dir, final Path child, final WatchEvent<Path> event);
    }
}
