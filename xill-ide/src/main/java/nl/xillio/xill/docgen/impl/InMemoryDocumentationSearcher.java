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
package nl.xillio.xill.docgen.impl;

import nl.xillio.xill.docgen.DocumentationEntity;
import nl.xillio.xill.docgen.DocumentationSearcher;

import java.util.*;
import java.util.stream.Collectors;

/**
 * This class represents a searcher that keeps all constructs in memory.
 *
 * @author Thomas Biesaart
 */
public class InMemoryDocumentationSearcher implements DocumentationSearcher {
    private final Map<String, Map<String, DocumentationEntity>> constructs = new HashMap<>();
    private final Map<String, List<String>> searchTags = new HashMap<>();

    @Override
    public String[] search(String query) {

        Set<String> results = new LinkedHashSet<>();
        Collections.addAll(results, searchByName(query));
        Collections.addAll(results, searchByTags(query));
        return results.toArray(new String[results.size()]);
    }

    String[] searchByTags(String query) {
        String[] tags = query.toLowerCase().split("\\s");

        List<String> results = new ArrayList<>();

        for (String tag : tags) {
            if (searchTags.containsKey(tag)) {
                results.addAll(searchTags.get(tag));
            }
        }

        return results.toArray(new String[results.size()]);
    }

    String[] searchByName(String query) {
        String[] parts = query.split("\\.");
        if (parts.length == 0) {
            return new String[0];
        }
        List<String> results = new ArrayList<>();

        for (String packageName : getPackages(parts)) {
            //Add all constructs from this package
            results.addAll(constructs.get(packageName)
                    .values().stream()
                    .map(construct -> packageName + "." + construct.getIdentity())
                    .collect(Collectors.toList()));
        }

        //Filter the package results by construct name if the first part is an explicit package
        String namePart = parts[parts.length - 1];
        results = results.stream().filter(item -> matchConstructName(item, namePart)).collect(Collectors.toList());

        return results.toArray(new String[results.size()]);
    }

    boolean matchConstructName(String item, String query) {
        String[] parts = item.split("\\.");

        return parts[1].toLowerCase().contains(query.toLowerCase()) || parts[0].toLowerCase().contains(query.toLowerCase());
    }

    /**
     * Returns a array of all possible package matches. If no explicit package is provided then this will return all packages
     *
     * @param parts the search query
     * @return the packages
     */
    private String[] getPackages(String[] parts) {
        if (parts.length > 1) {
            return constructs.keySet().stream().filter(pack -> matchPackage(pack, parts)).toArray(String[]::new);
        } else {
            return constructs.keySet().toArray(new String[constructs.size()]);
        }
    }

    boolean matchPackage(String packageName, String[] parts) {
        return packageName.toLowerCase().contains(parts[0].trim().toLowerCase());
    }


    @Override
    public void index(String packet, DocumentationEntity entity) {
        if (!constructs.containsKey(packet)) {
            constructs.put(packet, new HashMap<>());
        }

        constructs.get(packet).put(entity.getIdentity(), entity);
        entity.getTags().forEach(tag -> putSearchTag(tag, packet, entity.getIdentity()));
    }

    void putSearchTag(String searchTag, String packageName, String constructName) {
        String tag = searchTag.toLowerCase();
        if (!searchTags.containsKey(tag)) {
            searchTags.put(tag, new ArrayList<>());
        }

        searchTags.get(tag).add(packageName + "." + constructName);
    }
}
