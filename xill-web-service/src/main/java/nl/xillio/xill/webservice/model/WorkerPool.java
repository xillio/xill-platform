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
package nl.xillio.xill.webservice.model;

import nl.xillio.xill.webservice.exceptions.*;
import nl.xillio.xill.webservice.types.WorkerID;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * This class represents the pool of the workers for the given work directory.
 */
public class WorkerPool {

    private final Path workDirectory;
    private final int poolCardinality;
    private final WorkerFactory workerFactory;

    private final Map<WorkerID, Worker> pool = new HashMap<>();

    public WorkerPool(final Path workDirectory, int poolCardinality, WorkerFactory workerFactory) {
        this.workDirectory = workDirectory;
        this.poolCardinality = poolCardinality;
        this.workerFactory = workerFactory;
    }

    /**
     * Allocates a worker for the robot.
     * It can create new worker or reuse free existing worker,
     * or throw an exception if no worker can be created or reused.
     *
     * @param robotFQN the robot fully qualified name
     * @return the allocated worker
     * @throws RobotNotFoundException  if the robot was not found
     * @throws AllocateWorkerException there are no resource available for worker allocation
     * @throws RobotNotFoundException  when the robot is not found
     * @throws CompileException        when the compilation of the robot has failed
     */
    public Worker allocateWorker(final String robotFQN) throws BaseException {
        synchronized (pool) {
            checkWorkerPoolSize();
        }

        // This operation includes compiling and can last longer time so it must be outside the synchronised block
        Worker worker = createWorker(robotFQN);

        synchronized (pool) {
            checkWorkerPoolSize(); // The check must be done again to ensure atomic operation on the pool
            pool.put(worker.getId(), worker);
            return worker;
        }
    }

    private Worker createWorker(final String robotFQN) throws BaseException {
        return workerFactory.constructWorker(workDirectory, robotFQN);
    }

    private void checkWorkerPoolSize() throws AllocateWorkerException {
        if (pool.size() >= poolCardinality) {
            throw new AllocateWorkerException("Could not allocate new worker. The worker pool has reached its maximum amount of workers.");
        }
    }

    /**
     * Runs the worker (i.e. robot associated with the worker).
     *
     * @param id         the worker ID
     * @param parameters The parameters for the robot run
     * @return the result of the robot run
     * @throws InvalidStateException  if the worker is not in IDLE state
     * @throws RobotNotFoundException when the robot is not found
     */
    public Object runWorker(WorkerID id, final Map<String, Object> parameters) throws BaseException {
        Worker worker;
        synchronized (pool) {
            worker = findWorker(id);
        }
        // The protection of the xill worker against parallel run and stop operations is implemented in Worker itself so no need to deal with it
        return worker.run(parameters);
    }

    /**
     * Stops running worker (i.e. stop robot associated with the worker).
     *
     * @param id the worker ID
     * @throws RobotNotFoundException when the robot is not found
     * @throws InvalidStateException  if the worker is not in RUNNING state
     */
    public void stopWorker(WorkerID id) throws BaseException {
        Worker worker;
        synchronized (pool) {
            worker = findWorker(id);
        }
        // The protection of the xill worker against parallel run and stop operations is implemented in Worker itself so no need to deal with it
        worker.abort();
    }

    /**
     * Finds the worker in the worker pool.
     *
     * @param workerId the worker ID
     * @return the found Worker
     * @throws RobotNotFoundException if the worker was not found
     */
    private Worker findWorker(WorkerID workerId) throws RobotNotFoundException {
        if (!pool.containsKey(workerId)) {
            throw new RobotNotFoundException(String.format("The worker %1$d cannot be found.", workerId.getId()));
        }
        return pool.get(workerId);
    }

    /**
     * Releases the worker in the worker pool.
     *
     * @param workerId the worker ID
     * @throws RobotNotFoundException if the worker was not found
     * @throws InvalidStateException  if the worker is not in IDLE state
     */
    public void releaseWorker(WorkerID workerId) throws BaseException {
        synchronized (pool) {
            Worker worker = findWorker(workerId);
            if (worker.getState() != WorkerState.IDLE) {
                throw new InvalidStateException(String.format("The worker %1$d cannot be released as it is not in the IDLE state.", worker.getId().getId()));
            }
            worker.close();
            pool.remove(worker.getId());
        }
    }

    /**
     * Releases all workers in the worker pool.
     */
    public void releaseAllWorkers() {
        synchronized (pool) {
            pool.forEach((id, worker) -> worker.close());
            pool.clear();
        }
    }
}
