/**
 * Copyright (C) 2014 Xillio (support@xillio.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.xillio.xill.webservice;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import nl.xillio.xill.webservice.exceptions.BaseException;
import nl.xillio.xill.webservice.exceptions.InvalidStateException;
import nl.xillio.xill.webservice.exceptions.RobotNotFoundException;
import nl.xillio.xill.webservice.services.WebService;
import nl.xillio.xill.webservice.types.WorkerID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This class represents the main API controller. It is responsible for interacting with the actor that
 * calls the web API.
 */
@RestController
@RequestMapping(path = "${xws.api.base.path}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class WebServiceController {

    private final WebService webService;

    @Autowired
    public WebServiceController(WebService webService) {
        this.webService = webService;
    }

    /**
     * This is for the testing purposes only.
     *
     * @return the info about this web service
     * @throws RobotNotFoundException if the robot can not be found
     */
    @RequestMapping(path = "ping", method = RequestMethod.GET)
    @ResponseBody
    @ApiOperation(
            value = "Aliveness service",
            notes = "Returns basic info about the microservice as quickly as possible, can be used to verify the aliveness of the service."
    )
    @ResponseStatus(code = HttpStatus.OK)
    @ApiResponses({@ApiResponse(code = 200, message = "OK")})
    public Map<String, String> ping() throws RobotNotFoundException {
        final Map<String, String> info = new LinkedHashMap<>();
        info.put("Name", "Xill Microservice Server");
        info.put("Version", "0.1");
        info.put("WebService", webService.toString());
        return info;
    }

    /**
     * Allocates a worker for the robot.
     *
     * @param robotFQN the robot ID
     * @param request the request
     * @param response the response
     * @return the worker id
     * @throws BaseException
     */
    @RequestMapping(value = "workers", method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation(
            value = "Allocate a worker for a Xill robot",
            notes = "The given robot, by fully qualified name, is compiled and readied to be executed."
    )
    @ResponseStatus(code = HttpStatus.CREATED)
    @ApiResponses({
            @ApiResponse(code = 201, message = "Worker successfully allocated to the robot"),
            @ApiResponse(code = 404, message = "Robot not found"),
            @ApiResponse(code = 406, message = "Cannot allocate worker: resource limit reached"),
            @ApiResponse(code = 409, message = "The robot does not compile")})
    public Map<String, Object> allocateWorker(@RequestParam("robotFullyQualifiedName") String robotFQN, HttpServletRequest request, HttpServletResponse response) throws BaseException {
        final Map<String, Object> result = new HashMap<>();
        WorkerID workerID = webService.allocateWorker(robotFQN);
        result.put("workerId", workerID.toString());
        response.addHeader("Location", request.getRequestURL().toString() + "/" + workerID.toString());
        return result;
    }

    /**
     * Releases an existing worker (i.e. detach the robot associated with the worker).
     *
     * @param workerId the worker id
     */
    @RequestMapping(value = "worker/{id}", method = RequestMethod.DELETE)
    @ResponseBody
    @ApiOperation(
            value = "Release a worker",
            notes = "Remove the worker from the worker pool: the worker and its associated robot will not be available anymore and the pool will acquire capacity to allocate another worker. The worker must be in IDLE state."
    )
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    @ApiResponses({
            @ApiResponse(code = 204, message = "Worker successfully released"),
            @ApiResponse(code = 404, message = "Worker not found"),
            @ApiResponse(code = 409, message = "The worker is not in IDLE state")})
    public void releaseWorker(@PathVariable("id") String workerId) throws BaseException {
        webService.releaseWorker(new WorkerID(Integer.parseInt(workerId)));
    }

    /**
     * Runs the worker (i.e run the robot associated with the worker).
     *
     * @param workerId the worker id
     * @param requestBody the parameters used for the associated robot run
     * @return the result from robot run
     */
    @RequestMapping(value = "worker/{id}/run", method = RequestMethod.POST,
            produces = {MediaType.APPLICATION_JSON_UTF8_VALUE, MediaType.APPLICATION_OCTET_STREAM_VALUE})
    @ResponseBody
    @ApiOperation(
            value = "Run a worker with the given payload as argument",
            notes = "The robot associated with this worker is invoked similarly to a callbot() invocation with the arguments in the POST data. This call is synchronous and, upon termination of the robot, replies with the result of the robot's return. Returned file streams are materialized in the response. The worker must be in IDLE state."
    )
    @ApiResponses({
            @ApiResponse(code = 204, message = "No return value"),
            @ApiResponse(code = 404, message = "Worker not found"),
            @ApiResponse(code = 409, message = "The worker is not in IDLE state")})
    public Object runRobot(@PathVariable("id") String workerId, @RequestBody(required = false) Map<String, Object> requestBody, HttpServletResponse response) throws BaseException {
        Object result = webService.runWorker(new WorkerID(Integer.parseInt(workerId)), requestBody);
        if (result == null) {
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            return null;
        }
        return parseRobotResult(result);
    }

    /**
     * Handles a robot return value. Handles input streams as a special case.
     *
     * @param result the robot's result
     * @return an object that can be handled by Spring
     */
    private Object parseRobotResult(Object result) {
        if (result instanceof InputStream) {
            return ResponseEntity
                    .ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(new InputStreamResource((InputStream) result));
        }

        return result;
    }

    /**
     * Stops a running worker (i.e stops the running robot associated with the worker).
     *
     * @param workerId the worker id.
     * @param response the response
     */
    @RequestMapping(value = "worker/{id}/stop", method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation(
            value = "Abort a running worker",
            notes = "This will gracefully abort a running robot, terminating the currently running instruction. A timeout can be configured in the service properties. The robot must be in RUNNING state."
    )
    @ResponseStatus(code = HttpStatus.NO_CONTENT, reason = "The robot has been stopped")
    @ApiResponses({
            @ApiResponse(code = 404, message = "Worker not found"),
            @ApiResponse(code = 409, message = "The worker is not in RUNNING state")})
    public void stopRobot(@PathVariable("id") String workerId, HttpServletResponse response) throws BaseException {
        webService.stopWorker(new WorkerID(Integer.parseInt(workerId)));
        response.setStatus(HttpServletResponse.SC_NO_CONTENT);
    }
}
