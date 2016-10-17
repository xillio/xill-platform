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
package nl.xillio.xill.components.expressions.runbulk;

import nl.xillio.xill.TestUtils;
import nl.xillio.xill.api.components.ExpressionDataType;
import nl.xillio.xill.api.components.MetaExpression;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.*;

/**
 * Tests for {@link MasterThread}
 *
 * @author Geert Konijnendijk
 */
public class MasterThreadTest extends TestUtils {

    /**
     * Test {@link MasterThread#run()} under normal circumstances
     */
    @Test
    public void testRun() throws InterruptedException {
        // Mock
        ArrayList<MetaExpression> sourceList = new ArrayList<>();
        MetaExpression metaExpression = mockExpression(ExpressionDataType.ATOMIC);
        sourceList.add(metaExpression);
        Iterator<MetaExpression> source = sourceList.iterator();
        BlockingQueue<MetaExpression> queue = mock(BlockingQueue.class);
        when(queue.offer(any(), anyInt(), any())).thenReturn(true);
        RunBulkControl control = mock(RunBulkControl.class);

        MasterThread masterThread = new MasterThread(source, queue, control);

        // Run
        masterThread.run();

        // Verify
        verify(queue).offer(eq(metaExpression), anyInt(), any());
    }

    /**
     * Test {@link MasterThread#run()} when offering an item to the queue is interrupted
     */
    @Test
    public void testRunInterrupted() throws InterruptedException {
        // Mock
        ArrayList<MetaExpression> sourceList = new ArrayList<>();
        sourceList.add(mockExpression(ExpressionDataType.ATOMIC));
        Iterator<MetaExpression> source = sourceList.iterator();
        BlockingQueue<MetaExpression> queue = mock(BlockingQueue.class);
        when(queue.offer(any(), anyInt(), any())).thenReturn(true).thenThrow(new InterruptedException());
        RunBulkControl control = mock(RunBulkControl.class);

        MasterThread masterThread = new MasterThread(source, queue, control);

        // Run
        masterThread.run();
    }

    /**
     * Test {@link MasterThread#run()} when {@link RunBulkControl} signals the thread to stop
     */
    @Test
    public void testRunControlStop() throws InterruptedException {
        // Mock
        ArrayList<MetaExpression> sourceList = new ArrayList<>();
        MetaExpression metaExpression = mockExpression(ExpressionDataType.ATOMIC);
        sourceList.add(metaExpression);
        Iterator<MetaExpression> source = sourceList.iterator();
        BlockingQueue<MetaExpression> queue = mock(BlockingQueue.class);
        when(queue.offer(any(), anyInt(), any())).thenReturn(false, false);
        RunBulkControl control = mock(RunBulkControl.class);
        when(control.shouldStop()).thenReturn(false, false, true);

        MasterThread masterThread = new MasterThread(source, queue, control);

        // Run
        masterThread.run();

        // Verify
        verify(queue, times(2)).offer(eq(metaExpression), anyInt(), any());
    }
}
