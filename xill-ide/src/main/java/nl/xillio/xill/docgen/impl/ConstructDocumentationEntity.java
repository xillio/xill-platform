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
import nl.xillio.xill.docgen.PropertiesProvider;
import nl.xillio.xill.docgen.data.Example;
import nl.xillio.xill.docgen.data.Parameter;
import nl.xillio.xill.docgen.data.Reference;
import nl.xillio.xill.docgen.data.ParDescription;

import java.util.*;
import java.util.stream.Collectors;

/**
 * This class represents the documentation of a single construct
 *
 * @author Thomas Biesaart
 * @author Ivor van der Hoog
 * @since 12-8-2015
 */
public class ConstructDocumentationEntity implements DocumentationEntity {
    private final String identity;
    private String description;
    private List<Parameter> parameters;
    private List<ParDescription> parameterDescriptions;
    private String longDescription;
    private List<Example> examples;
    private List<Reference> references;
    private Set<String> searchTags;
    private boolean deprecated;
    private String deprecateDescription;

    /**
     * The constructor for the {@link ConstructDocumentationEntity}.
     * Gets handed the name of the package of the construct and the name of the construct.
     *
     * @param identity The name of the package of the construct
     */
    public ConstructDocumentationEntity(final String identity) {
        this.identity = identity;
    }

    @Override
    public String getIdentity() {
        return identity;
    }

    @Override
    public Map<String, Object> getProperties() {
        Map<String, Object> properties = new LinkedHashMap<>();

        properties.put("identity", identity);
        properties.put("description", description);
        properties.put("parameters", PropertiesProvider.extractContent(parameters));
        properties.put("parameterDescriptions", PropertiesProvider.extractContent(parameterDescriptions));
        properties.put("longDescription", longDescription);
        properties.put("examples", PropertiesProvider.extractContent(examples));
        properties.put("references", PropertiesProvider.extractContent(references));
        properties.put("tags", searchTags);
        properties.put("deprecated", deprecated);
        properties.put("deprecateDescription", deprecateDescription);


        return properties;
    }

    /**
     * Sets the description of the {@link ConstructDocumentationEntity}
     *
     * @param description The description of the function.
     */
    public void setDescription(final String description) {
        this.description = description;
    }

    /**
     * Returns the description of the construct.
     *
     * @return The description.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Set the {@link Parameter}(s) of the construct.
     *
     * @param parameters The parameters the construct has.
     */
    public void setParameters(final List<Parameter> parameters) {
        this.parameters = parameters;
    }

    /**
     * Returns the parameters of the construct.
     *
     * @return The parameters.
     */
    public List<Parameter> getParameters() {
        return parameters;
    }

    /**
     * Returns the names of all parameters in an array.
     *
     * @return The name of all parameters.
     */
    public List<String> getParameterNames() {
        return parameters.stream()
                .map(Parameter::getName)
                .collect(Collectors.toList());
    }

    /**
     * Set the {@link Example}(s) of the construct.
     *
     * @param examples The example we want to construct to have.
     */
    public void setExamples(final List<Example> examples) {
        this.examples = examples;
    }

    /**
     * Set the references of the construct.
     *
     * @param references The references we want the construct to have.
     */
    public void setReferences(final List<Reference> references) {
        this.references = references;
    }

    /**
     * Set the searchTags of the construct.
     *
     * @param searchTags The searchTags we want the construct to have.
     */
    public void setSearchTags(final Set<String> searchTags) {
        this.searchTags = searchTags;
    }

    /**
     * Set whether this construct is deprecated.
     *
     * @param deprecated True if deprecated, false if not
     */
    public void setDeprecated(final boolean deprecated) {
        this.deprecated = deprecated;
    }

    /**
     * Set the description shown when a construct is deprecated.
     *
     * Useful for mentioning reasons and alternative constructs.
     *
     * @param deprecateDescription The deprecate description we want the construct to have.
     */
    public void setDeprecateDescription(final String deprecateDescription) {
        this.deprecateDescription = deprecateDescription;
    }

    public void setParameterDescription(final List<ParDescription> parameterDescriptions) {
        this.parameterDescriptions = parameterDescriptions;
    }

    public void setLongDescription(final String longDescription) {
        this.longDescription = longDescription;
    }

    /**
     * Returns all search tags in an array.
     *
     * @return All search tags.
     */
    public List<String> getSearchTags() {
        return searchTags.stream().collect(Collectors.toList());
    }

    @Override
    public String getType() {
        return "construct";
    }

    @Override
    public List<String> getTags() {
        return new ArrayList<>(searchTags);
    }
}
