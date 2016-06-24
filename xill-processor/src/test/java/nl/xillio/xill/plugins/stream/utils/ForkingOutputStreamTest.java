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
package nl.xillio.xill.plugins.stream.utils;

import nl.xillio.xill.TestUtils;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.io.SimpleIOStream;
import org.testng.annotations.Test;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;


public class ForkingOutputStreamTest extends TestUtils {

    @Test
    public void testWriteInt() throws Exception {
        OutputStream stream1 = mock(OutputStream.class);
        OutputStream stream2 = mock(OutputStream.class);
        OutputStream fork = new ForkingOutputStream(NULL, stream1, stream2);
        int data = 222;
        fork.write(data);

        verify(stream1).write(data);
        verify(stream2).write(data);
    }


    @Test
    public void testWriteArray() throws Exception {
        OutputStream stream1 = mock(OutputStream.class);
        OutputStream stream2 = mock(OutputStream.class);
        OutputStream fork = new ForkingOutputStream(NULL, stream1, stream2);
        byte[] data = new byte[]{1, 2, 5, 3, 2};
        fork.write(data);

        verify(stream1).write(data);
        verify(stream2).write(data);
    }

    @Test
    public void testWrite2() throws Exception {
        OutputStream stream1 = mock(OutputStream.class);
        OutputStream stream2 = mock(OutputStream.class);
        OutputStream fork = new ForkingOutputStream(NULL, stream1, stream2);
        byte[] data = new byte[]{1, 2, 5, 3, 2};
        fork.write(data, 5, 10);

        verify(stream1).write(data, 5, 10);
        verify(stream2).write(data, 5, 10);
    }

    @Test
    public void testFlush() throws Exception {
        OutputStream stream1 = mock(OutputStream.class);
        OutputStream stream2 = mock(OutputStream.class);
        OutputStream fork = new ForkingOutputStream(NULL, stream1, stream2);
        fork.flush();

        verify(stream1).flush();
        verify(stream2).flush();
    }

    @Test
    public void testClose() throws Exception {
        OutputStream stream1 = mock(OutputStream.class);
        OutputStream stream2 = mock(OutputStream.class);
        List<MetaExpression> list = new ArrayList<>();
        list.add(fromValue(new SimpleIOStream(stream1, null)));
        list.add(fromValue(new SimpleIOStream(stream2, null)));
        MetaExpression expression = fromValue(list);

        OutputStream fork = new ForkingOutputStream(expression, stream1, stream2);

        verify(stream1, never()).close();
        verify(stream2, never()).close();
        fork.close();

        verify(stream1).close();
        verify(stream2).close();
    }

}