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
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
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
 * Client for Xill server.
 * Class contains all needed methods for communicating with Xill server using REST API related to project and resource handling
 */
public class XillServerUploader implements AutoCloseable {
    
    private static final String OAUTH_CLIENT_ID = "xillServer";
    private static final String OAUTH_CLIENT_SECRET = "";
    
    private static final Logger LOGGER = Log.get();
    private Executor executor;
    private String baseUrl;
    private JsonParser jsonParser = new JacksonParser(true);
    
    private Header authorizationHeader;

    public XillServerUploader() {
        executor = Executor.newInstance();
    }

    /**
     * Authenticate on Xill server.
     *
     * @param serverUrl Protocol, host [port] [uri] of the Xill server - e.g. http://localhost:8080
     * @param username Username
     * @param password Password
     * @throws IOException if authentication fails
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

        HttpHost httpHost = new HttpHost(host, port);
        String response = executor.auth(OAUTH_CLIENT_ID, OAUTH_CLIENT_SECRET)
            .authPreemptive(httpHost)
            .execute(
                Request.Post(serverUrl + "/oauth/token")
                .bodyForm(
                    new BasicNameValuePair("grant_type", "password"),
                    new BasicNameValuePair("username", username),
                    new BasicNameValuePair("password", password)
                )
            )
            .returnContent()
            .asString();
        try {
            String accessToken = (String) jsonPath("access_token", jsonParser.fromJson(response, Map.class));
            authorizationHeader = new BasicHeader("Authorization", "Bearer " + accessToken);
        } catch (JsonException e) {
            throw new IOException("The server response is invalid.", e);
        }
    }

    /**
     * Find project on the server.
     *
     * @param projectName Name of the project
     * @return projectId if found, otherwise null (if not found)
     * @throws IOException if finding the projects fails
     */
    public String findProject(final String projectName) throws IOException {
        String response = doGet("projects/search/findByProjectName?projectName=" + urlEncode(projectName));

        List projects;
        try {
            projects = (List) jsonPath("_embedded/projects", jsonParser.fromJson(response, Map.class));
        } catch (JsonException e) {
            throw new IOException("The server response is invalid.", e);
        }
        if (projects == null || projects.size() != 1) {
            return null;
        }

        String uri = (String) jsonPath("_links/self/href", projects.get(0));
        if (uri == null) {
            return null;
        }
        return uri.substring(uri.lastIndexOf('/') + 1);
    }

    /**
     * Find object in Json given by path of object's keys (very simplified XPath like - for objects only).
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
     * Check if provided robot exist on the server.
     *
     * @param projectId project ID
     * @param robotFqn robot FQN
     * @return true if exist, otherwise false
     * @throws IOException if the existence check fails
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

    /**
     * Tests if the resource does exist on the server.
     *
     * @param projectId project ID
     * @param resourceName resource name
     * @return true if resource exists, otherwise false
     * @throws IOException if the existence check fails
     */
    public boolean existResource(final String projectId, final String resourceName) throws IOException {
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
        List resources;
        try {
            resources = jsonParser.fromJson(resourcesJson, List.class);
        } catch (JsonException e) {
            throw new IOException("The server response is invalid.", e);
        }
        if (resources == null) {
            return false;
        }
        return resources.stream().filter(r -> ((Map) r).get("projectPath").equals(resourceName)).findAny().isPresent();
    }

    /**
     * Check if the project exists on the server - if not then create it.
     *
     * @param projectName Name of the project
     * @return projectId
     * @throws IOException if the existence check fails
     */
    public String ensureProjectExist(final String projectName) throws IOException {
        String projectId = findProject(projectName);
        if (projectId == null) {
            createProject(projectName);
            projectId = findProject(projectName);
        }
        return projectId;
    }

    /**
     * Create project on the server.
     * @param projectName Name of the project
     * @throws IOException if the project creation fails
     */
    public void createProject(final String projectName) throws IOException {
        HashMap<String, String> data = new HashMap<>();
        data.put("projectName", projectName);
        try {
            doPost("projects", jsonParser.toJson(data));
        } catch (JsonException e) {
            throw new IOException("Could not create project on the server.", e);
        }
    }

    /**
     * Perform a validation request on Xill server which checks if robot(s) are valid.
     *
     * @param projectId the project id
     * @param robotFqns list of robots (theirs FQNs)
     * @throws RobotValidationException if at least one of the robot's code is not valid
     * @throws IOException if validation process failed
     */
    @SuppressWarnings("unchecked")
    public void validateRobots(final String projectId, final List<String> robotFqns) throws IOException {
        try {
            String responseJson = doPost(String.format("projects/%1$s/validate", projectId), jsonParser.toJson(robotFqns));
            Map<String, String> response = jsonParser.fromJson(responseJson, Map.class);
            if (response.get("valid").equals("true")) {
                return;
            }
            // Not valid robot
            throw new RobotValidationException(response.get("message"));

        } catch (JsonException e) {
            throw new IOException("Could not validate robot on the server.", e);
        }
    }

    /**
     * Delete project from the server.
     *
     * @param projectId Name of the project
     */
    public void deleteProject(final String projectId) throws IOException {
        doDelete("projects/" + projectId);
    }

    private String doGet(final String uri) throws IOException {
        return executor.execute(Request.Get(baseUrl + uri).addHeader(authorizationHeader))
                .returnContent()
                .asString();
    }

    private void doDelete(final String uri) throws IOException {
        executor.execute(Request.Delete(baseUrl + uri));
    }

    private String doPost(final String uri, final HttpEntity entity) throws IOException {
        return executor.execute(
                Request.Post(baseUrl + uri)
                        .body(entity)
                        .addHeader(authorizationHeader)
        )
                .returnContent()
                .asString();
    }

    private String doPost(final String uri, final String jsonData) throws IOException {
        return executor.execute(
                Request.Post(baseUrl + uri)
                        .bodyString(jsonData, ContentType.APPLICATION_JSON)
                        .addHeader(authorizationHeader)
        )
                .returnContent()
                .asString();
    }

    private String doPut(final String uri, final String jsonData) throws IOException {
        return executor.execute(
                Request.Put(baseUrl + uri)
                        .bodyString(jsonData, ContentType.APPLICATION_JSON)
                        .addHeader(authorizationHeader)
        )
                .returnContent()
                .asString();
    }

    /**
     * Create fully qualified name of the robot.
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
     * Create resource name from resource file and project folder.
     *
     * @param resourceFile the resource file
     * @param projectFolder the project folder
     * @return resource name
     */
    public String getResourceName(final File resourceFile, final File projectFolder) {
        return projectFolder.toURI().relativize(resourceFile.toURI()).getPath().replace('/', ':'); // This replacement is because path having / (slash char) cannot be part of uri even if it's url encoded - so it must be replaced by some other char, ideally by some not valid for path itself
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
     * Upload the robot to the server.
     *
     * @param projectId project ID
     * @param robotFqn robot FQN
     * @param code source code of the robot
     * @throws IOException if upload fails
     */
    public void uploadRobot(final String projectId, final String robotFqn, final String code) throws IOException {
        HashMap<String, String> data = new HashMap<>();
        data.put("code", code);
        try {
            processAcknowledgedMessage(doPut(String.format("projects/%1$s/robots/%2$s", projectId, urlEncode(robotFqn)), jsonParser.toJson(data)));
        } catch (JsonException e) {
            throw new IOException("Could not upload robot " + robotFqn + "to the server.", e);
        }
    }

    /**
     * Upload the resource to the server.
     *
     * @param projectId project ID
     * @param resourceName resource name
     * @param resourceFile resource file
     * @throws IOException if upload fails
     */
    public void uploadResource(final String projectId, final String resourceName, final File resourceFile) throws IOException {
        HttpEntity httpEntity = MultipartEntityBuilder
                .create()
                .addBinaryBody("file", resourceFile)
                .build();

        processAcknowledgedMessage(doPost(String.format("projects/%1$s/resources/%2$s", projectId, urlEncode(resourceName)), httpEntity));
    }

    /**
     * Query Xill server for current server settings.
     *
     * @return The server settings.
     * @throws IOException if the request fails
     */
    @SuppressWarnings("unchecked")
    public Map<String, String> querySettings() throws IOException {
        final String response = doGet("settings");
        try {
            return (Map<String, String>) jsonParser.fromJson(response, Map.class);
        } catch (JsonException e) {
            throw new IOException("The server response is invalid.", e);
        }
    }

    private void processAcknowledgedMessage(final String response) throws IOException {
        try {
            final Object jsonParsed = jsonParser.fromJson(response, Map.class);
            boolean ack = (Boolean) jsonPath("acknowledged", jsonParsed);
            if (!ack) {
                throw new IOException((String) jsonPath("errorMessage", jsonParsed));
            }
        } catch (JsonException e) {
            throw new IOException("The server response is invalid.", e);
        }
    }

    private String urlEncode(final String uri) throws IOException {
        try {
            return URLEncoder.encode(uri, "UTF-8").replace("+", "%20");
        } catch (UnsupportedEncodingException e) {
            throw new IOException(e.getMessage(), e);
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
