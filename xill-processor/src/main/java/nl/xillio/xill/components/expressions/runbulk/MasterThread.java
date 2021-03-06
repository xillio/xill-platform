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

import me.biesaart.utils.Log;
import nl.xillio.xill.api.components.MetaExpression;
import org.slf4j.Logger;

import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Thread keeping a queue of work to distribute to {@link WorkerThread workers} of the {@link RunBulkExpression}.
 */
class MasterThread extends Thread {
    private static final Logger LOGGER = Log.get();

    private final Iterator<MetaExpression> source;
    private final BlockingQueue<MetaExpression> queue;
    private final RunBulkControl control;

    /**
     * Create a master thread.
     *
     * @param source Source of the jobs
     * @param queue Queue for passing jobs to workers
     * @param control Controls runBulk threads
     */
    public MasterThread(final Iterator<MetaExpression> source, final BlockingQueue<MetaExpression> queue, final RunBulkControl control) {
        super("RunBulk MasterThread");
        this.source = source;
        this.queue = queue;
        this.control = control;
    }

    /**
     * Start inserting items into the internal queue.
     */
    @Override
    public void run() {
        try {
            offerItemsToQueue();
        } catch (InterruptedException e) {
            LOGGER.error("Interrupted while waiting for queue item", e);
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Insert items into the queue while there are items and we should continue running.
     *
     * @throws InterruptedException When offering an item to the queue is interrupted
     */
    private void offerItemsToQueue() throws InterruptedException {
        while (source.hasNext() && !control.shouldStop()) {
            MetaExpression item = source.next();
            while (!queue.offer(item, 100, TimeUnit.MILLISECONDS)) {
                if (control.shouldStop()) {
                    return;
                }
            }
        }
    }
}
