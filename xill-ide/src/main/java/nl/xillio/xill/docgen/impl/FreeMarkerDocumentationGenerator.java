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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import me.biesaart.utils.Log;
import nl.xillio.xill.docgen.DocumentationEntity;
import nl.xillio.xill.docgen.DocumentationGenerator;
import nl.xillio.xill.docgen.PropertiesProvider;
import nl.xillio.xill.docgen.exceptions.ParsingException;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * This is the FreeMarker implementation of the {@link DocumentationGenerator}
 *
 * @author Thomas Biesaart
 * @author Ivor van der Hoog
 * @since 12-8-2015
 */
public class FreeMarkerDocumentationGenerator implements DocumentationGenerator {
    private static final Gson GSON = new GsonBuilder()
            .create();
    private static final Logger LOGGER = Log.get();
    private static final String JSON_INDEX_NAME = "_index.json";
    private final String packageName;
    private final Configuration fmConfig;
    private final File documentationFolder;
    private boolean isClosed;
    private final File packageFolder;
    private final PackageDocumentationEntity packageEntity;
    private final Map<String, Object> defaultValues = new HashMap<>();

    public FreeMarkerDocumentationGenerator(String collectionIdentity, Configuration fmConfig, File documentationFolder) {
        packageName = collectionIdentity;
        this.fmConfig = fmConfig;
        this.documentationFolder = documentationFolder;
        this.packageFolder = new File(documentationFolder, collectionIdentity);
        packageEntity = new PackageDocumentationEntity();
        packageEntity.setName(collectionIdentity);
    }

    @Override
    public void generate(DocumentationEntity entity) throws ParsingException {
        assertNotClosed();

        //Get template
        Template template = getTemplate(entity.getType());
        Map<String, Object> model = getModel(entity);

        //Prepare target
        File target = new File(packageFolder, entity.getIdentity() + ".html");

        //Generate
        try {
            doGenerate(template, target, model);
        } catch (IOException e) {
            throw new ParsingException("Failed to write to " + target.getAbsolutePath(), e);
        } catch (TemplateException e) {
            throw new ParsingException("Error in template " + template.getName(), e);
        }

        //Save to list
        packageEntity.add(entity);
    }

    void assertNotClosed() {
        if (isClosed) {
            throw new IllegalStateException("This generator has already been closed");
        }
    }

    @Override
    public void generateIndex() throws ParsingException {
        List<PackageDocumentationEntity> packages = getPackagesFromJson();

        //Build model
        Map<String, Object> model = defaultModel();
        model.put("packages", PropertiesProvider.extractContent(packages));

        //Get template
        Template template = getTemplate("index");

        //Generate
        File target = new File(documentationFolder, "index.html");

        try {
            doGenerate(template, target, model);
        } catch (IOException e) {
            throw new ParsingException("Failed to write to " + target.getAbsolutePath(), e);
        } catch (TemplateException e) {
            throw new ParsingException("Syntax error", e);
        }
    }

    @Override
    public void setProperty(String name, String value) {
        defaultValues.put(name, value);
    }

    @Override
    public String getIdentity() {
        return packageName;
    }

    public List<PackageDocumentationEntity> getPackagesFromJson() {
        List<PackageDocumentationEntity> result = new ArrayList<>();

        //Gather all json files
        File[] foldersArray = getDocumentationSubFolders();
        List<File> jsonFiles = Arrays.stream(foldersArray)
                .filter(Objects::nonNull)
                .map(this::getJsonFile)
                .filter(Objects::nonNull)
                .sorted(File::compareTo)
                .collect(Collectors.toList());


        for (File jsonFile : jsonFiles) {

            try {
                String json = getJsonFromFile(jsonFile);
                result.add(GSON.fromJson(json, PackageDocumentationEntity.class));
            } catch (IOException e) {
                LOGGER.error("Failed to read json from " + jsonFile.getAbsolutePath(), e);
            }
        }

        return result;
    }

    String getJsonFromFile(File file) throws IOException {
        return FileUtils.readFileToString(file);
    }

    File[] getDocumentationSubFolders() {
        File[] folders = documentationFolder.listFiles();
        if (folders == null) {
            LOGGER.error("No generated packages were found.");
            return new File[0];
        }
        return folders;
    }

    File getJsonFile(File folder) {
        File jsonFile = new File(folder, JSON_INDEX_NAME);
        if (!jsonFile.exists()) {
            return null;
        }

        return jsonFile;
    }

    void doGenerate(Template template, File target, Map<String, Object> model) throws IOException, TemplateException {
        //Make sure the target file exists
        touch(target);

        //Generate
        FileWriter writer = new FileWriter(target);
        template.process(model, writer);
        writer.close();
    }

    void touch(File file) throws IOException {
        FileUtils.touch(file);
    }

    private Map<String, Object> defaultModel() {
        return new HashMap<>(defaultValues);
    }

    Map<String, Object> getModel(DocumentationEntity entity) {
        Map<String, Object> model = defaultModel();
        model.putAll(entity.getProperties());
        model.put("packageName", packageName);
        return model;
    }

    Template getTemplate(String name) throws ParsingException {
        try {
            return fmConfig.getTemplate(name + ".html");
        } catch (IOException e) {
            throw new ParsingException("Failed to get template", e);
        }
    }

    @Override
    public void close() throws Exception {
        assertNotClosed();
        isClosed = true;

        try {
            generatePackageIndex();
            saveJsonSummary();
        } catch (ParsingException e) {
            LOGGER.error("Failed to generate package index for " + packageName, e);
            throw e;
        }
    }

    void generatePackageIndex() throws ParsingException, IOException, TemplateException {
        File target = new File(packageFolder, "_index.html");
        Template template = getTemplate("plugin");
        Map<String, Object> packageModel = getModel(packageEntity);
        doGenerate(template, target, packageModel);
    }

    void saveJsonSummary() throws ParsingException {
        String jsonString = GSON.toJson(packageEntity);

        try {
            FileUtils.writeStringToFile(new File(packageFolder, JSON_INDEX_NAME), jsonString);
        } catch (IOException e) {
            throw new ParsingException("Failed to write json summary of " + packageName, e);
        }
    }
}
