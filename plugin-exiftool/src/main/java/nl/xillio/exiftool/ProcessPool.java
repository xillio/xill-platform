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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * This class represents a pool that can run low level actions on available processes.
 *
 * @author Thomas Biesaart
 */
public class ProcessPool implements AutoCloseable {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessPool.class);

    /**
     * These are all the processes that have been built.
     */
    private List<ExifToolProcess> processes = new ArrayList<>();

    /**
     * These processes have been given away but not yet returned.
     */
    private List<ExifToolProcess> leasedProcesses = new ArrayList<>();
    private final Supplier<ExifToolProcess> processBuilder;

    public ProcessPool(Supplier<ExifToolProcess> processBuilder) {
        this.processBuilder = processBuilder;
    }

    public synchronized ExifTool getAvailable() {
        ExifToolProcess process = processes.stream()
                .filter(proc -> !leasedProcesses.contains(proc))
                .findAny()
                .orElse(null);

        if (process == null) {
            process = createNew();
        }

        leasedProcesses.add(process);

        LOGGER.info("Giving out {}", process);

        return new ExifTool(process, this::release);
    }

    private ExifToolProcess createNew() {
        LOGGER.info("Creating new exiftool process");
        ExifToolProcess process = processBuilder.get();
        processes.add(process);
        return process;
    }

    @SuppressWarnings("squid:UnusedPrivateMethod") // Sonar doesn't do lambdas
    private void release(ExifToolProcess process) {
        LOGGER.info("Releasing {}", process);
        if (!process.isAvailable()) {
            // This process is still busy. Kill it
            processes.remove(process);
            process.close();
        }
        leasedProcesses.remove(process);
    }

    /**
     * Close all idle processes.
     */
    public void clean() {
        List<ExifToolProcess> processesToKill = processes.stream()
                .filter(proc -> !leasedProcesses.contains(proc))
                .filter(ExifToolProcess::isAvailable)
                .collect(Collectors.toList());
        processesToKill.forEach(this::close);
    }

    @SuppressWarnings("squid:UnusedPrivateMethod") // Sonar doesn't do lambdas
    private void close(ExifToolProcess process) {
        processes.remove(process);
        process.close();
    }

    @Override
    public void close() {
        processes.forEach(ExifToolProcess::close);
    }

    public int size() {
        return processes.size();
    }
}
