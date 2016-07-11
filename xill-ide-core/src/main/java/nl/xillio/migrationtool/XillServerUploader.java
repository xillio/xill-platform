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
package nl.xillio.migrationtool;

import me.biesaart.utils.Log;
import nl.xillio.migrationtool.gui.FXController;
import nl.xillio.xill.api.XillEnvironment;
import nl.xillio.xill.services.json.JacksonParser;
import nl.xillio.xill.services.json.JsonException;
import nl.xillio.xill.services.json.JsonParser;
import nl.xillio.xill.util.settings.ProjectSettings;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.message.BasicHeader;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Client for Xill server (for projects only)
 * Class contains all needed methods for communicating with Xill server using REST API related to project handling
 */
public class XillServerUploader implements AutoCloseable {
    private static final Logger LOGGER = Log.get();
    private Executor executor;
    private CookieStore cookieStore;
    private Header xsrfHeader;
    private String baseUrl;
    private JsonParser jsonParser = new JacksonParser(true);

    public XillServerUploader() {
        cookieStore = new BasicCookieStore();
        executor = Executor.newInstance().use(cookieStore);
    }

    /**
     * Authenticate on Xill server
     *
     * @param serverUrl Protocol, host [port] [uri] of the Xill server - e.g. http://localhost:8080
     * @param username Username
     * @param password Password
     */
    public void authenticate(final String serverUrl, final String username, final String password) throws IOException {
        // Xill Server authentication
        baseUrl = serverUrl + "/api/1.0/";

        String host;
        int port;
        try {
            URL url = new URL(baseUrl);
            host = url.getHost();
            port = url.getPort();
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Invalid host.", e);
        }

        // Authentication
        executor.auth(new HttpHost(host, port), username, password)
                .authPreemptive(new HttpHost(host, port));

        // Login to get the session settings. There's currently an extra arbitrary get request because otherwise the session isn't starting up correctly.
        executor.execute(Request.Get(baseUrl + "login"));
        executor.execute(Request.Get(baseUrl + "/projects")).returnContent().asString(); // This getting response without usage is intentional - it would not fail if credentials were wrong without this code!

        // Store CSRF token
        xsrfHeader = new BasicHeader("X-XSRF-TOKEN", cookieStore.getCookies().stream().filter(p -> p.getName().equals("XSRF-TOKEN")).findFirst().get().getValue());
    }

    /**
     * Find project on the server
     *
     * @param projectName Name of the project
     * @return projectId if found, otherwise null (if not found)
     */
    public String findProject(final String projectName) throws IOException, JsonException {
        String response = doGet("projects/search/findByProjectName?projectName=" + urlEncode(projectName));

        List projects = (List) jsonPath("_embedded/projects", jsonParser.fromJson(response, Map.class));
        if (projects == null || projects.size() != 1) {
            return null;
        }

        String uri = (String) jsonPath("_links/project/href", projects.get(0));
        if (uri == null) {
            return null;
        }
        return uri.substring(uri.lastIndexOf('/')+1);
    }

    /**
     * Find object in Json given by path of object's keys (very simplified XPath like - for objects only)
     *
     * @param path Path of object's keys (e.g. "user/address/street")
     * @param jsonParsed Object of parsed Json
     * @return Object if found, otherwise null (if not found)
     */
    @SuppressWarnings("unchecked")
    private Object jsonPath(final String path, final Object jsonParsed) {
        String[] pathItems = path.split("/");
        Object current = jsonParsed;
        for (String item : pathItems) {
            Map<String, ?> map = (Map) current;
            if (map == null) {
                return null;
            }
            current = map.get(item);
            if (current == null) {
                return null;
            }
        }
        return current;
    }

    /**
     * Check if provided robot exist on the server
     *
     * @param projectId project ID
     * @param robotFqn robot FQN
     * @return true if exist, otherwise false
     */
    public boolean existRobot(final String projectId, final String robotFqn) throws IOException {
        try {
            String response = doGet(String.format("projects/%1$s/robot/%2$s", projectId, urlEncode(robotFqn)));
            if (response.isEmpty()) {
                return false;
            }
        } catch (IOException e) {
            if (getResponseCode(e) == 404) {
                return false;
            } else {
                throw e;
            }
        }
        return true;
    }

    public boolean existResource(final String projectId, final String resourceName) throws JsonException, IOException {
        // Get the JSON list of resources for given projectId
        final String resourcesJson;
        try {
            resourcesJson = doGet(String.format("projects/%1$s/resources", projectId));
            if (resourcesJson.isEmpty()) {
                return false;
            }
        } catch (IOException e) {
            if (getResponseCode(e) == 404) {
                return false;
            } else {
                throw e;
            }
        }

        // Find the resourceName in the list
        List resources = (List) jsonParser.fromJson(resourcesJson, List.class);
        if (resources == null) {
            return false;
        }
        return resources.stream().filter(r -> ((Map)r).get("projectPath").equals(resourceName)).findAny().isPresent();
    }

    /**
     * Check if the project exists on the server - if not then create it
     *
     * @param projectName Name of the project
     * @return projectId
     */
    public String ensureProjectExist(final String projectName) throws IOException, JsonException {
        String projectId = findProject(projectName);
        if (projectId == null) {
            createProject(projectName);
            projectId = findProject(projectName);
        }
        return projectId;
    }

    /**
     * Create project on the server
     * @param projectName Name of the project
     */
    public void createProject(final String projectName) {
        HashMap<String,String> data = new HashMap<>();
        data.put("projectName", projectName);
        try {
            doPost("projects", jsonParser.toJson(data));
        } catch (JsonException | IOException e) {
            throw new RuntimeException("Could not create project on the server.", e);
        }
    }

    /**
     * Delete project from the server
     *
     * @param projectId Name of the project
     */
    public void deleteProject(final String projectId) {
        try {
            doDelete("projects/" + projectId);
        } catch (IOException e) {
            throw new RuntimeException("Could not delete project from the server.", e);
        }
    }

    private String doGet(final String uri) throws IOException {
        return executor.execute(Request.Get(baseUrl + uri))
                .returnContent()
                .asString();
    }

    private void doDelete(final String uri) throws IOException {
        executor.execute(Request.Delete(baseUrl + uri).addHeader(xsrfHeader));
    }

    private String doPost(final String uri, final HttpEntity entity) throws IOException {
        return executor.execute(
                Request.Post(baseUrl + uri)
                        .addHeader(xsrfHeader)
                        .body(entity)
        )
                .returnContent()
                .asString();
    }

    private String doPost(final String uri, final String jsonData) throws IOException {
        return executor.execute(
                Request.Post(baseUrl + uri)
                        .addHeader(xsrfHeader)
                        .bodyString(jsonData, ContentType.APPLICATION_JSON)
        )
                .returnContent()
                .asString();
    }

    private String doPut(final String uri, final String jsonData) throws IOException {
        return executor.execute(
                Request.Put(baseUrl + uri)
                        .addHeader(xsrfHeader)
                        .bodyString(jsonData, ContentType.APPLICATION_JSON)
        )
                .returnContent()
                .asString();
    }

    /**
     * Create fully qualified name of the robot
     *
     * @param robotFile robot file
     * @param projectFolder project folder
     * @return robot FQN
     */
    public String getFqn(final File robotFile, final File projectFolder) {
        return projectFolder.toURI()
                .relativize(robotFile.toURI()).getPath()
                .replaceAll("[/\\\\]", ".")
                .replace(XillEnvironment.ROBOT_EXTENSION, "");
    }

    /**
     * Create resource name from resource file and project folder
     *
     * @param resourceFile the resource file
     * @param projectFolder the project folder
     * @return resource name
     */
    public String getResourceName(final File resourceFile, final File projectFolder) {
        return projectFolder.toURI().relativize(resourceFile.toURI()).getPath().replace('/',':'); // This replacement is because path having / (slash char) cannot be part of uri even if it's url encoded - so it must be replaced by some other char, ideally by some not valid for path itself
    }

    /**
     * Create name of the project from project folder.
     * It returns the name of the project from project settings.
     *
     * @param projectFolder project folder
     * @return name of the project
     */
    public String getProjectName(final File projectFolder) {
        final List<ProjectSettings> projects = FXController.settings.project().getAll();
        ProjectSettings foundProject = projects.stream()
                .filter(p -> new File(p.getFolder()).equals(projectFolder))
                .findAny()
                .orElse(null);
        if (foundProject == null) {
            LOGGER.warn("Could not find the project in project settings.");
            return projectFolder.getName();
        }
        return foundProject.getName();
    }

    /**
     * Upload the robot to the server
     *
     * @param projectId project ID
     * @param robotFqn robot FQN
     * @param code source code of the robot
     */
    public void uploadRobot(final String projectId, final String robotFqn, final String code) {
        HashMap<String,String> data = new HashMap<>();
        data.put("code", code);
        try {
            doPut(String.format("projects/%1$s/robots/%2$s", projectId, urlEncode(robotFqn)), jsonParser.toJson(data));
        } catch (JsonException | IOException e) {
            throw new RuntimeException("Could not upload robot " + robotFqn + "to the server.", e);
        }
    }

    /**
     * Upload the resource to the server
     *
     * @param projectId project ID
     * @param resourceName resource name
     * @param resourceFile resource file
     */
    public void uploadResource(final String projectId, final String resourceName, final File resourceFile) {
        HttpEntity httpEntity  = MultipartEntityBuilder
            .create()
            .addBinaryBody("file", resourceFile)
            .build();
        try {
            doPost(String.format("projects/%1$s/resources/%2$s", projectId, urlEncode(resourceName)), httpEntity);
        } catch (IOException e) {
            throw new RuntimeException("Could not upload resource " + resourceName + " to the server.", e);
        }
    }

    private String urlEncode(final String uri) {
        try {
           return URLEncoder.encode(uri, "UTF-8").replace("+", "%20");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private int getResponseCode(final IOException e) {
        if (e instanceof HttpResponseException) {
            return ((HttpResponseException) e).getStatusCode();
        }
        return -1;
    }

    @Override
    public void close() {
        executor.clearAuth();
        executor.clearCookies();
    }
}
