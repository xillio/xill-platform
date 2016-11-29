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

import nl.xillio.xill.webservice.exceptions.*;
import nl.xillio.xill.webservice.services.XillWebService;
import nl.xillio.xill.webservice.types.XWID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.BasePathAwareController;
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
@BasePathAwareController
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
    @RequestMapping(value = "ping", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, String> ping() {
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
    @RequestMapping(value = "workers/{id}", method = RequestMethod.DELETE)
    @ResponseBody
    public void releaseWorker(@PathVariable("id") int workerId, HttpServletResponse response) throws XillInvalidStateException, XillNotFoundException {
        xillWebService.releaseWorker(new XWID(workerId));
        response.setStatus(HttpServletResponse.SC_NO_CONTENT);
    }

    /**
     * Run the worker (i.e run the robot associated with the worker).
     *
     * @param workerId The worker id.
     * @param requestBody The parameters used for the associated robot run.
     * @return The result from robot run.
     */
    @RequestMapping(value = "workers/{id}/run", method = RequestMethod.POST)
    @ResponseBody
    public Object runRobot(@PathVariable("id") int workerId, @RequestBody(required = false) Map<String, Object> requestBody) throws XillInvalidStateException, XillNotFoundException {
        return xillWebService.runWorker(new XWID(workerId), requestBody);
    }

    /**
     * Stop running worker (i.e stop running robot associated with the worker).
     *
     * @param workerId The worker id.
     * @param response The response.
     */
    @RequestMapping(value = "workers/{id}/stop", method = RequestMethod.POST)
    @ResponseBody
    public void stopRobot(@PathVariable("id") int workerId, HttpServletResponse response) throws XillInvalidStateException, XillNotFoundException {
        xillWebService.stopWorker(new XWID(workerId));
        response.setStatus(HttpServletResponse.SC_NO_CONTENT);
    }
}
