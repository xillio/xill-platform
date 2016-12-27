package nl.xillio.xill.webservice;

import nl.xillio.xill.webservice.exceptions.BaseException;
import nl.xillio.xill.webservice.exceptions.RobotNotFoundException;
import nl.xillio.xill.webservice.services.WebService;
import nl.xillio.xill.webservice.types.WorkerID;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;

/**
 * Tests for {@link WebServiceController}.
 *
 * @author Geert Konijnendijk
 */
public class WebServiceControllerTest {

    private WebServiceController controller;

    private WebService webService;

    @BeforeMethod
    public void setup() {
        webService = mock(WebService.class);
        controller = new WebServiceController(webService);
    }

    /**
     * Test {@link WebServiceController#ping()}.
     */
    @Test
    public void testPing() throws RobotNotFoundException {
        Map<String, String> ping = controller.ping();

        Map<String, String> expected = new HashMap<>();
        expected.put("Name", "Xill Microservice Server");
        expected.put("Version", "0.1");
        expected.put("WebService", webService.toString());

        assertEquals(ping, expected);
    }

    /**
     * Test {@link WebServiceController#allocateWorker(String, HttpServletRequest, HttpServletResponse)} under normal circumstances.
     */
    @Test
    public void testAllocateWorker() throws BaseException {
        // Mock
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        String requestUrl = "http://xillwebservice.com";
        when(request.getRequestURL()).thenReturn(new StringBuffer(requestUrl));

        WorkerID id = new WorkerID();
        when(webService.allocateWorker(anyString())).thenReturn(id);

        // Run
        Map<String, Object> result = controller.allocateWorker("robot.fqn", request, response);

        // Verify
        verify(webService).allocateWorker("robot.fqn");
        verify(response).addHeader("Location", requestUrl + "/" + id.toString());

        // Assert
        Map<String, Object> expected = new HashMap<>();
        expected.put("workerId", id.toString());

        assertEquals(result, expected, "Returned worker ID did not match");
    }

    /**
     * Test {@link WebServiceController#allocateWorker(String, HttpServletRequest, HttpServletResponse)} when an exception is thrown.
     */
    @Test(expectedExceptions = BaseException.class)
    public void testAllocatedWorkerException() throws BaseException {
        // Mock
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        when(webService.allocateWorker(anyString())).thenThrow(BaseException.class);

        // Run
        controller.allocateWorker("robot.fqn", request, response);
    }

    /**
     * Test {@link WebServiceController#releaseWorker(String)} under normal circumstances.
     */
    @Test
    public void testReleaseWorker() throws BaseException {
        // Run
        controller.releaseWorker("1234");

        // Verify
        verify(webService).releaseWorker(new WorkerID(1234));
    }

    /**
     * Test {@link WebServiceController#releaseWorker(String)} when an exception is thrown.
     */
    @Test(expectedExceptions = BaseException.class)
    public void testReleaseWorkerException() throws BaseException {
        // Mock
        doThrow(BaseException.class).when(webService).releaseWorker(new WorkerID(1234));


        // Run
        controller.releaseWorker("1234");
    }

    /**
     * Test {@link WebServiceController#runRobot(String, Map, HttpServletResponse)} under normal circumstances.
     */
    @Test
    public void testRunRobot() throws BaseException {
        // Mock
        HttpServletResponse response = mock(HttpServletResponse.class);
        HashMap<String, Object> requestBody = new HashMap<>();

        Object robotResult = new Object();
        when(webService.runWorker(any(), anyMap())).thenReturn(robotResult);

        // Run
        Object result = controller.runRobot("1234", requestBody, response);

        // Verify
        verify(webService).runWorker(new WorkerID(1234), requestBody);

        // Assert
        assertSame(result, robotResult, "Robot result does not match expected");
    }

    /**
     * Test {@link WebServiceController#runRobot(String, Map, HttpServletResponse)} when the robot returns {@code null}.
     */
    @Test
    public void testRunRobotNoResult() throws BaseException {
        // Mock
        HttpServletResponse response = mock(HttpServletResponse.class);
        HashMap<String, Object> requestBody = new HashMap<>();

        when(webService.runWorker(any(), anyMap())).thenReturn(null);

        // Run
        Object result = controller.runRobot("1234", requestBody, response);

        // Verify
        verify(webService).runWorker(new WorkerID(1234), requestBody);
        verify(response).setStatus(HttpStatus.NO_CONTENT.value());

        // Assert
        assertSame(result, null, "Robot result does not match expected");
    }

    /**
     * Test {@link WebServiceController#runRobot(String, Map, HttpServletResponse)} when the robot returns a stream.
     */
    @Test
    public void testRunRobotStreamResult() throws BaseException, IOException {
        // Mock
        HttpServletResponse response = mock(HttpServletResponse.class);
        HashMap<String, Object> requestBody = new HashMap<>();

        Object robotResult = mock(InputStream.class);
        when(webService.runWorker(any(), anyMap())).thenReturn(robotResult);

        // Run
        Object result = controller.runRobot("1234", requestBody, response);

        // Verify
        verify(webService).runWorker(new WorkerID(1234), requestBody);

        // Assert
        ResponseEntity responseEntity = (ResponseEntity) result;
        Object body = responseEntity.getBody();
        assertSame(((InputStreamResource)body).getInputStream(), robotResult, "Robot result does not match expected");
        assertEquals(responseEntity.getStatusCode(), HttpStatus.OK);
    }

    /**
     * Test {@link WebServiceController#runRobot(String, Map, HttpServletResponse)} when an exception is thrown.
     */
    @Test(expectedExceptions = BaseException.class)
    public void testRunRobotException() throws BaseException {
        // Mock
        HttpServletResponse response = mock(HttpServletResponse.class);
        HashMap<String, Object> requestBody = new HashMap<>();

        when(webService.runWorker(any(), anyMap())).thenThrow(BaseException.class);

        // Run
        controller.runRobot("1234", requestBody, response);
    }

    /**
     * Test {@link WebServiceController#stopRobot(String, HttpServletResponse)} under normal circumstances.
     */
    @Test
    public void testStopRobot() throws BaseException {
        // Mock
        HttpServletResponse response = mock(HttpServletResponse.class);

        // Run
        controller.stopRobot("1234", response);

        // Verify
        verify(webService).stopWorker(new WorkerID(1234));
    }

    /**
     * Test {@link WebServiceController#stopRobot(String, HttpServletResponse)} when an exception is thrown.
     */
    @Test(expectedExceptions = BaseException.class)
    public void testStopRobotException() throws BaseException {
        // Mock
        HttpServletResponse response = mock(HttpServletResponse.class);
        doThrow(BaseException.class).when(webService).stopWorker(any());

        // Run
        controller.stopRobot("1234", response);
    }
}
