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
package nl.xillio.xill.plugins.xurl.constructs;

import nl.xillio.xill.TestUtils;
import nl.xillio.xill.api.NullDebugger;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.components.RobotID;
import nl.xillio.xill.api.construct.Construct;
import nl.xillio.xill.api.construct.ConstructContext;
import nl.xillio.xill.api.construct.ConstructProcessor;
import nl.xillio.xill.api.errors.RobotRuntimeException;
import nl.xillio.xill.plugins.xurl.data.Options;
import nl.xillio.xill.plugins.xurl.services.ActivityLogger;
import nl.xillio.xill.plugins.xurl.services.ResponseParser;
import org.apache.http.HttpHost;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.HttpHostConnectException;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.UUID;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.testng.AssertJUnit.assertEquals;


public class AbstractRequestConstructTest extends TestUtils {
    private final AbstractRequestConstruct construct = new MockBody();

    @Test
    public void testPrepareProcess() {
        ConstructProcessor processor = construct.prepareProcess(context(construct));
        assertEquals(processor.getNumberOfArguments(), 3);


        Construct mockNoBody = new MockNoBody();
        ConstructProcessor processor2 = mockNoBody.prepareProcess(context(mockNoBody));
        assertEquals(processor2.getNumberOfArguments(), 2);
    }

    @Test(expectedExceptions = RobotRuntimeException.class, expectedExceptionsMessageRegExp = ".*timed out.*")
    public void testExecuteTimeout() throws IOException {
        construct.execute(executor(new ConnectTimeoutException()), null);
    }

    @Test(expectedExceptions = RobotRuntimeException.class, expectedExceptionsMessageRegExp = ".*protocol.*")
    public void testExecuteProtocol() throws IOException {
        construct.execute(executor(new ClientProtocolException()), null);
    }

    @Test(expectedExceptions = RobotRuntimeException.class, expectedExceptionsMessageRegExp = "No host found .*")
    public void testExecuteUnknownHost() throws IOException {
        construct.execute(executor(new UnknownHostException()), null);
    }

    @Test(expectedExceptions = RobotRuntimeException.class, expectedExceptionsMessageRegExp = ".*host.*")
    public void testExecuteHostConnectException() throws IOException {
        construct.execute(executor(new HttpHostConnectException(new IOException(), new HttpHost("https://fakeurl.com"))), null);
    }

    @Test(expectedExceptions = RobotRuntimeException.class, expectedExceptionsMessageRegExp = ".*failed.*")
    public void testExecuteIOException() throws IOException {
        construct.execute(executor(new IOException()), null);
    }

    @Test
    public void testGetUriValidUri() {
        String url = "http://xillio.com";
        URI uri = construct.getUri(fromValue(url));
        assertEquals(uri.getHost(), "xillio.com");
        assertEquals(uri.toString(), "http://xillio.com");
    }

    @Test(expectedExceptions = RobotRuntimeException.class, expectedExceptionsMessageRegExp = ".*url is not valid.*")
    public void testGetUriInvalidUrl() {
        construct.getUri(fromValue("I AM GROOT"));
    }


    @Test(expectedExceptions = RobotRuntimeException.class, expectedExceptionsMessageRegExp = ".*not a valid uri.*")
    public void testGetUriInvalidUri() {
        construct.getUri(fromValue("http://test.com/[]"));
    }

    @Test(expectedExceptions = RobotRuntimeException.class, expectedExceptionsMessageRegExp = ".*cannot be null.*")
    public void testGetUriNull() {
        construct.getUri(NULL);
    }

    @Test
    public void testFromValueNormalPath() throws IOException {
        AbstractRequestConstruct construct = new MockBody();
        ActivityLogger logger = mock(ActivityLogger.class);
        construct.setActivityLogger(logger);

        // We will mock ResponseParser because it has been test already
        MetaExpression result = fromValue("This will be the result");
        ResponseParser responseParser = mock(ResponseParser.class);
        when(responseParser.build(any(), any(), any())).thenReturn(result);
        construct.setResponseParser(responseParser);

        MetaExpression output = construct.fromValue(null, mock(Response.class), context(construct), new Options());

        assertEquals(output, result);
        verify(logger).handle(any(), any(), any(), any());
    }

    @Test(expectedExceptions = RobotRuntimeException.class, expectedExceptionsMessageRegExp = ".*response.*")
    public void testFromValueIOException() throws IOException {
        AbstractRequestConstruct construct = new MockBody();
        ActivityLogger logger = mock(ActivityLogger.class);
        construct.setActivityLogger(logger);

        // We will mock ResponseParser because it has been test already
        ResponseParser responseParser = mock(ResponseParser.class);
        when(responseParser.build(any(), any(), any())).thenThrow(new IOException());
        construct.setResponseParser(responseParser);

        construct.fromValue(null, mock(Response.class), context(construct), new Options());
    }


    private Executor executor(Throwable connectTimeoutException) throws IOException {
        Executor executor = mock(Executor.class);
        when(executor.execute(any())).thenThrow(connectTimeoutException);
        return executor;
    }

    private class MockBody extends AbstractRequestConstruct {

        @Override
        protected Request buildRequest(URI uri) {
            return null;
        }
    }

    @NoBody
    private class MockNoBody extends MockBody {

    }

}
