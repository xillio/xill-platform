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
package nl.xillio.exiftool;

import nl.xillio.exiftool.process.ExifToolProcess;
import org.testng.annotations.Test;

import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;

/**
 * @author Thomas Biesaart
 */
public class ProcessPoolTest {

    @Test
    public void testGetProcessRoutine() {
        Factory factory = mock(Factory.class, RETURNS_DEEP_STUBS);
        when(factory.build().isAvailable()).thenReturn(true);

        ProcessPool processPool = new ProcessPool(factory::build);

        verify(factory, times(1)).build();

        try (ExifTool tool = processPool.getAvailable()) {
            verify(factory, times(2)).build();
        }

        try (ExifTool tool = processPool.getAvailable()) {
            verify(factory, times(2)).build();

            try (ExifTool tool2 = processPool.getAvailable()) {
                verify(factory, times(3)).build();
            }
        }

        try (ExifTool tool = processPool.getAvailable()) {
            verify(factory, times(3)).build();
        }

        assertEquals(processPool.size(), 2);
        processPool.clean();
        assertEquals(processPool.size(), 0);
        processPool.close();


    }

    private interface Factory {
        ExifToolProcess build();
    }
}
