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
package nl.xillio.xill.plugins.system.info;

import nl.xillio.xill.plugins.system.services.info.ProgressTrackerService;
import nl.xillio.xill.services.ProgressTracker;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.UUID;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Test the {@link ProgressTrackerService}
 */
public class ProgressTrackerServiceTest {

    /**
     * Test getProgress() usage.
     */
    @Test
    public void testGetProgress() {
        double progress = 0.26;
        ProgressTrackerService service = new ProgressTrackerService();
        UUID csid = new UUID(1,1);
        service.setProgress(csid, progress);

        // Run
        double result = service.getProgress(csid);

        // Verify
        assertEquals(result, progress);
    }

    /**
     * Test getProgress() usage when CSID is null.
     */
    @Test
    public void testGetProgressNullCSID() {
        ProgressTrackerService service = new ProgressTrackerService();

        // Run
        Double result = service.getProgress(null);

        // Verify
        assertEquals(result, null);
    }

    /**
     * Test remove() usage.
     */
    @Test
    public void testRemove() {
        ProgressTrackerService service = new ProgressTrackerService();
        UUID csid = new UUID(1,1);
        service.setProgress(csid, 0.26);

        // Run
        boolean result = service.remove(csid);

        // Verify
        assertEquals(result, true);
    }

    /**
     * Test remove() usage when map is empty.
     */
    @Test
    public void testRemoveWhenEmpty() {
        ProgressTrackerService service = new ProgressTrackerService();
        UUID csid = new UUID(1,1);

        // Run
        boolean result = service.remove(csid);

        // Verify
        assertEquals(result, false);
    }

    /**
     * Test getOnStopBehavior() usage.
     */
    @Test
    public void testGetOnStopBehavior() {
        ProgressTracker.OnStopBehavior behavior = ProgressTracker.OnStopBehavior.ZERO;
        ProgressTrackerService service = new ProgressTrackerService();
        UUID csid = new UUID(1,1);
        service.setOnStopBehavior(csid, behavior);

        // Run
        ProgressTracker.OnStopBehavior result = service.getOnStopBehavior(csid);

        // Verify
        assertEquals(result, behavior);
    }

    /**
     * Test getRemainingTime() under common circumstances.
     *
     * @throws InterruptedException
     */
    @Test
    public void testGetRemainingTime() throws InterruptedException {
        ProgressTrackerService service = new ProgressTrackerService();
        UUID csid = new UUID(1,1);
        service.setProgress(csid, 0);
        service.setProgress(csid, 0.1);

        // Run
        Duration result = service.getRemainingTime(csid);

        // Verify
        assertTrue(result != null);
    }

    /**
     * Test getRemainingTime() when CSID is null.
     */
    @Test
    public void testGetRemainingTimeNullCSID() {
        ProgressTrackerService service = new ProgressTrackerService();

        // Run
        Duration result = service.getRemainingTime(null);

        // Verify
        assertEquals(result, null);
    }

    /**
     * Test getRemainingTime() when progress is negative.
     */
    @Test
    public void testGetRemainingTimeNegativeProgress() {
        ProgressTrackerService service = new ProgressTrackerService();
        UUID csid = new UUID(1,1);
        service.setProgress(csid, -1);

        // Run
        Duration result = service.getRemainingTime(csid);

        // Verify
        assertEquals(result, null);
    }

    /**
     * Test getRemainingTime() when progress is 100%.
     */
    @Test
    public void testGetRemainingTimeDoneProgress() {
        ProgressTrackerService service = new ProgressTrackerService();
        UUID csid = new UUID(1,1);
        service.setProgress(csid, 0.5);
        service.setProgress(csid, 1);

        // Run
        Duration result = service.getRemainingTime(csid);

        // Verify
        assertEquals(result, Duration.ZERO);
    }
}
