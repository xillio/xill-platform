/**
 * Copyright (C) 2014 Xillio (support@xillio.com)
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.xillio.xill.webservice;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.restdocs.ManualRestDocumentation;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;

import static nl.xillio.xill.webservice.IsValidUrlMatcher.isValidUrl;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.text.IsEmptyString.isEmptyString;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * This class will run tests on the web api. It will also generate snippets that can be included
 * in asciidoc documentation.
 *
 * @author Thomas Biesaart
 */
@SpringBootTest
public class WebServiceIT extends AbstractTestNGSpringContextTests {
    private final ManualRestDocumentation restDocumentation = new ManualRestDocumentation("tmp-test");

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeMethod
    public void setUp(Method method) {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(documentationConfiguration(this.restDocumentation)).build();
        this.restDocumentation.beforeTest(getClass(), method.getName());
    }

    @AfterMethod
    public void tearDown() {
        this.restDocumentation.afterTest();

        // TODO Wipe worker pool state
    }

    @Test
    public void testCreateWorker() throws Exception {
        // When creating a worker
        this.mockMvc.perform(
                post("/workers")
                        .contentType(MediaType.APPLICATION_JSON)
                        // With a valid robot name
                        .content(jsonBody(
                                "robot", "project.ScrapeFiles"
                        ))
        )
                // The response must be 201-CREATED
                .andExpect(status().isNotFound())
                // And contain a Location URL as a header
                .andExpect(header().string("Location", isValidUrl()))
                .andDo(document("create-worker"));
    }

    @Test
    public void testCreateWorkerWithoutRobot() throws Exception {
        // When creating a worker
        this.mockMvc.perform(
                post("/workers")
                        .contentType(MediaType.APPLICATION_JSON)
                        // With an invalid robot name
                        .content(jsonBody(
                                "robot", "Path/To/My Robot.xill"
                        ))
        )
                // The response must be 400-BAD Request
                .andExpect(status().isBadRequest())
                .andDo(document("create-worker-invalid-robot"));
    }

    @Test
    public void testCreateWorkerWithFullQueue() throws Exception {
        // TODO This test assumes a worker pool size of 3

        // Fill the worker pool with 3
        for (int i = 0; i < 3; i++) {
            this.mockMvc.perform(
                    post("/workers")
                            .contentType(MediaType.APPLICATION_JSON)
                            // With a valid robot name
                            .content(jsonBody(
                                    "robot", "unittest.pool.Worker" + i
                            ))

            )
                    // The response must be 201-CREATED
                    .andExpect(status().isNotFound());
        }

        // Adding the fourth worker should error
        this.mockMvc.perform(
                post("/workers")
                        .contentType(MediaType.APPLICATION_JSON)
                        // With a valid robot name
                        .content(jsonBody(
                                "robot", "unittest.pool.WorkerError"
                        ))
        )
                .andExpect(status().isServiceUnavailable())
                .andDo(document("create-worker-pool-full"));
    }

    @Test
    public void testDeleteWorker() throws Exception {

        // Deleting a worker by id
        this.mockMvc.perform(
                delete("/workers/{id}").param("id", "3")
        )
                // Should return 204-No Content
                .andExpect(status().isNoContent())
                .andDo(document("delete-worker"));
    }

    @Test
    public void testDeleteNonExistingWorker() throws Exception {
        // Deleting a non-existing worker
        this.mockMvc.perform(
                delete("/workers/{id}").param("id", "000400")
        )
                // Should return 404-Not Found
                .andExpect(status().isNotFound())
                .andDo(document("delete-worker-not-exist"));
    }

    @Test
    public void testRunLoadedRobotWithJsonReturnValue() throws Exception {
        // Run a robot
        this.mockMvc.perform(
                post("/workers/{id}/activate").param("id", "2")
        )
                // Should return 200 - OK
                .andExpect(status().isOk())
                // And contain a json body
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string(not(isEmptyString())));
    }

    @Test
    public void testRunLoadedRobotWithStreamReturnValue() throws Exception {
        // Run a robot
        this.mockMvc.perform(
                post("/workers/{id}/activate").param("id", "1")
        )
                // Should return 200 - OK
                .andExpect(status().isOk())
                // And contain a steam body
                .andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM))
                .andExpect(content().string(not(isEmptyString())));
    }

    @Test
    public void testRunLoadedRobotWithoutReturnValue() throws Exception {
        // Run a robot
        this.mockMvc.perform(
                post("/workers/{id}/activate").param("id", "3")
        )
                // Should return 204 - NO CONTENT
                .andExpect(status().isNoContent());
    }

    @Test
    public void testRunNotLoadedRobot() throws Exception {
        // Run a non existing robot/worker
        this.mockMvc.perform(
                post("/workers/{id}/activate").param("id", "000404")
        )
                // Should return 404 - NOT FOUND
                .andExpect(status().isNotFound());
    }

    @Test
    public void testRunRobotWithError() throws Exception {
        // Run a non existing robot/worker
        this.mockMvc.perform(
                post("/workers/{id}/activate").param("id", "000404")
        )
                // Should return 500 - INTERNAL SERVER ERROR
                .andExpect(status().isInternalServerError())
                // And it should contain a body explaining the error
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string(not(isEmptyString())));
    }

    @Test
    public void testTerminateRunningWorker() throws Exception {
        // Terminate a running worker
        this.mockMvc.perform(
                post("/workers/{id}/terminate").param("id", "5")
        )
                // Should return 204 - NO CONTENT
                .andExpect(status().isNoContent())
                .andDo(document("terminate-worker"));
    }

    @Test
    public void testTerminateNotRunningWorker() throws Exception {
        // Terminate a non-running worker
        this.mockMvc.perform(
                post("/workers/{id}/terminate").param("id", "5")
        )
                // Should return 400 - BAD REQUEST
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testTerminateNonExistingWorker() throws Exception {
        // Terminate a non-existing worker
        this.mockMvc.perform(
                post("/workers/{id}/terminate").param("id", "000404")
        )
                // Should return 404 - NOT FOUND
                .andExpect(status().isNotFound());
    }


    private String jsonBody(Object... params) {
        Map<String, Object> data = new LinkedHashMap<>();

        for (int i = 0; i < params.length; i++) {
            data.put(params[i++].toString(), params[i]);
        }

        try {
            return objectMapper.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }
}