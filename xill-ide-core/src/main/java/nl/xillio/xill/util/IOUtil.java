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
package nl.xillio.xill.util;

import me.biesaart.utils.Log;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.CodeSource;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Utility class that offers various generic file/io functions
 */
public class IOUtil {
    private static Map<String, List<URL>> urlCache = new Hashtable<>();
    private static final Logger LOGGER = Log.get();

    /**
     * Returns a list of files inside the application's jar file, or if there is none, from the filesystem
     *
     * @param clazz
     * @param folder The folder to look for
     * @return A list of filenames; directories are skipped
     */
    public static List<URL> readFolder(final Class<?> clazz, final String folder) {
        // The urlCache dramatically increases performance of jar-files, as they only have to be scanned once for each folder, instead of with every request
        if (urlCache.containsKey(folder)) {
            return urlCache.get(folder);
        }

        // 1. Try to read from jarfile
        List<URL> urls = readJarFolder(clazz, folder);

        // 2. If that failed, then try to read the stuff straight from file
        if (urls == null || urls.isEmpty()) {
            File[] fileList = getFileList(folder);
            urls = Arrays.stream(fileList).parallel()
                    .filter(file -> !file.isDirectory())
                    .map(file -> "file://" + file.getAbsolutePath() + "/" + file.getName())
                    .map(IOUtil::toUrl)
                    .filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList());
        }
        urlCache.put(folder, urls);
        return urls;
    }

    private static File[] getFileList(String folder) {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        File[] result = new File[0];
        URL url = classLoader.getResource(folder);
        if (url != null) {
            try {
                result = new File(url.toURI()).listFiles();
            } catch (URISyntaxException e) {
                LOGGER.debug("Invalid URI", e);
            }
        }
        return result;
    }

    private static Optional<URL> toUrl(String stringUrl) {
        try {
            return Optional.of(new URL(stringUrl));
        } catch (MalformedURLException e) {
            LOGGER.debug("Could not parse URL.", e);
        }
        return Optional.empty();
    }

    /**
     * Returns a list of files inside the application's jar file
     *
     * @param folder The folder to be looked up
     * @return A list of filenames; directories are skipped
     */
    private static List<URL> readJarFolder(final Class<?> clazz, final String folder) {
        CodeSource src = clazz.getProtectionDomain().getCodeSource();
        List<URL> list = new ArrayList<>();

        if (src != null) {
            URL jar = src.getLocation();
            try {
                ZipInputStream zip = new ZipInputStream(jar.openStream());
                ZipEntry zipEntry = null;
                while ((zipEntry = zip.getNextEntry()) != null) {
                    if (!zipEntry.isDirectory() && zipEntry.getName().startsWith(folder)) {
                        String url = src.getLocation() + "!/" + folder + "/" + zipEntry.getName();
                        list.add(new URL(url));
                    }
                }
            } catch (IOException e) {
                LOGGER.debug("Could not open jar file.", e);
                return null;
            }
        } else {
            return null;
        }
        return list;
    }

    /**
     * Lists the constructs in the specified folder
     *
     * @param clazz
     * @param constructPackage
     * @return
     */
    public static List<String> listConstructs(final Class<?> clazz, final String constructPackage) {
        List<String> constructNames = new LinkedList<>();
        try {
            constructNames = readFolder(clazz, constructPackage.replace('.', '/')).stream()
                    .map(URL::getPath)
                    .filter(path -> path.endsWith("Construct.class"))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return constructNames;
    }

}
