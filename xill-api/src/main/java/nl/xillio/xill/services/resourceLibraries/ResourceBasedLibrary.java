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
package nl.xillio.xill.services.resourceLibraries;


import me.biesaart.utils.Log;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;

/**
 * This class represents a library that can load data from a plain text resource.
 * Resource format:
 * <code>
 * category         key1 key2 key3
 * </code>
 * <p>
 * All keys will be lower cased.
 *
 * @author Thomas Biesaart
 */
public abstract class ResourceBasedLibrary {
    private static final Logger LOGGER = Log.get();
    private final Map<String, String> contentTypes = new HashMap<>();

    protected ResourceBasedLibrary(URL... resources) {
        for (URL resource : resources) {
            if (resource != null) {
                loadResource(resource);
            }
        }
    }

    protected void loadResource(Path path) {
        if (!Files.exists(path)) {
            return;
        }
        try (InputStream stream = Files.newInputStream(path, StandardOpenOption.READ)) {
            loadResource(stream);
        } catch (IOException e) {
            LOGGER.error("Failed to build content type library", e);
        }
    }

    protected void loadResource(URL resource) {
        try (InputStream stream = resource.openStream()) {
            loadResource(stream);
        } catch (IOException e) {
            LOGGER.error("Failed to build content type library", e);
        }
    }

    protected void loadResource(InputStream stream) {
        Scanner scanner = new Scanner(stream);
        while (scanner.hasNextLine())

        {
            String line = scanner.nextLine();
            if (line == null) {
                break;
            }
            if (line.startsWith("#")) {
                // This is a comment
                continue;
            }

            String[] parts = line.split("\\s+");
            if (parts.length < 2) {
                // No extension is found on this line
                continue;
            }

            for (int i = 1; i < parts.length; i++) {
                contentTypes.put(parts[i].toLowerCase(), parts[0]);
            }
        }

    }

    public boolean hasKey(String key) {
        return contentTypes.containsKey(key);
    }

    public Optional<String> get(Path path) {
        Path fileName = path.getFileName();
        if (fileName == null) {
            return Optional.empty();
        }
        String name = fileName.toString();
        int index = name.lastIndexOf('.');
        if (index == -1) {
            return Optional.empty();
        }

        return get(name.substring(index).toLowerCase());
    }

    public Optional<String> get(String extension) {
        String noDotExtension = extension.startsWith(".") ? extension.substring(1) : extension;
        return Optional.ofNullable(contentTypes.get(noDotExtension.toLowerCase()));
    }
}
