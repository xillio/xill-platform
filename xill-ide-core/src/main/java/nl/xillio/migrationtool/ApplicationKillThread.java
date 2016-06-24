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
package nl.xillio.migrationtool;

/**
 * This thread will wait for 5 seconds and then kill the application
 */
public class ApplicationKillThread extends Thread {

    private ApplicationKillThread() {
        super("ApplicationKillThread");
        setDaemon(true);
    }

    @Override
    @SuppressWarnings("squid:S1147") // use of System.exit
    public void run() {
        try {
            sleep(5000);
        } catch (InterruptedException e) {
            System.out.println("Application was closed before it had to be force killed");
            return;
        }

        System.err.println("Forcing application to close by killing all threads!");
        System.exit(0);
    }

    public static void exit() {
        ApplicationKillThread thread = new ApplicationKillThread();
        thread.start();
    }
}
