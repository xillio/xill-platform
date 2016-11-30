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
package nl.xillio.xill.webservice;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import nl.xillio.xill.webservice.exceptions.XillAllocateWorkerException;
import nl.xillio.xill.webservice.exceptions.XillCompileException;
import nl.xillio.xill.webservice.exceptions.XillInvalidStateException;
import nl.xillio.xill.webservice.exceptions.XillNotFoundException;
import nl.xillio.xill.webservice.services.XillWebService;
import nl.xillio.xill.webservice.types.XWID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This class represents the main API controller. It is responsible for interacting with the actor that
 * calls the web API.
 */
@RestController
@RequestMapping(path = "${xws.api.base.path}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class XillWebServiceController {

    private final XillWebService xillWebService;

    @Autowired
    public XillWebServiceController(XillWebService xillWebService) {
        this.xillWebService = xillWebService;
    }

    /**
     * This is for the testing purposes only.
     *
     * @return The info about this web service.
     */
    @RequestMapping(path = "ping", method = RequestMethod.GET)
    @ResponseBody
    @ApiOperation(
            value = "Aliveness service",
            notes = "Returns basic info about the microservice as quickly as possible, can be used to verify the aliveness of the service."
    )
    @ResponseStatus(code = HttpStatus.OK)
    @ApiResponses({@ApiResponse(code = 200, message = "OK")})
    public Map<String, String> ping() throws XillNotFoundException {
        final Map<String, String> info = new LinkedHashMap<>();
        info.put("Name", "Xill Microservice Server");
        info.put("Version", "0.1");
        info.put("XillWebService", xillWebService.toString());
        return info;
    }

    /**
     * Allocate worker for the robot.
     *
     * @param robotFQN The robot identificator.
     * @param request The request.
     * @param response The response.
     * @return The worker id.
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
    public Map<String, Object> allocateWorker(@RequestParam("robotFullyQualifiedName") String robotFQN, HttpServletRequest request, HttpServletResponse response) throws XillCompileException, XillAllocateWorkerException, XillNotFoundException {
        final Map<String, Object> result = new HashMap<>();
        XWID xwid = xillWebService.allocateWorker(robotFQN);
        result.put("workerId", xwid.toString());
        response.setStatus(HttpServletResponse.SC_CREATED);
        response.addHeader("Location", request.getRequestURL().toString() + "/" + xwid.toString());
        return result;
    }

    /**
     * Release existing worker - i.e. detach the robot associated with the worker.
     *
     * @param workerId The worker id.
     * @param response The response.
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
    public void releaseWorker(@PathVariable("id") String workerId, HttpServletResponse response) throws XillInvalidStateException, XillNotFoundException {
        xillWebService.releaseWorker(new XWID(Integer.parseInt(workerId)));
        response.setStatus(HttpServletResponse.SC_NO_CONTENT);
    }

    /**
     * Run the worker (i.e run the robot associated with the worker).
     *
     * @param workerId The worker id.
     * @param requestBody The parameters used for the associated robot run.
     * @return The result from robot run.
     */
    @RequestMapping(value = "worker/{id}/run", method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation(
            value = "Run a worker with the given payload as argument",
            notes = "The robot associated with this worker is invoked similarly to a callbot() invocation with the arguments in the POST data. This call is synchronous and, upon termination of the robot, replies with the result of the robot's return. Returned file streams are materialized in the response. The worker must be in IDLE state."
    )
    @ResponseStatus(code = HttpStatus.OK)
    @ApiResponses({
            @ApiResponse(code = 404, message = "Worker not found"),
            @ApiResponse(code = 409, message = "The worker is not in IDLE state")})
    public Object runRobot(@PathVariable("id") String workerId, @RequestBody(required = false) Map<String, Object> requestBody) throws XillInvalidStateException, XillNotFoundException {
        return xillWebService.runWorker(new XWID(Integer.parseInt(workerId)), requestBody);
    }

    /**
     * Stop running worker (i.e stop running robot associated with the worker).
     *
     * @param workerId The worker id.
     * @param response The response.
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
    public void stopRobot(@PathVariable("id") String workerId, HttpServletResponse response) throws XillInvalidStateException, XillNotFoundException {
        xillWebService.stopWorker(new XWID(Integer.parseInt(workerId)));
        response.setStatus(HttpServletResponse.SC_NO_CONTENT);
    }
}
