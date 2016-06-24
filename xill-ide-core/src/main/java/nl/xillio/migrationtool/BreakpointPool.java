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

import nl.xillio.xill.api.Breakpoint;
import nl.xillio.xill.api.components.RobotID;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class is responsible for the administration of breakpoints
 */
public enum
BreakpointPool {
    INSTANCE;
    //can be transient, because we do not serialize BreakpointPools
    private final transient Map<RobotID, List<Integer>> breakpoints = new HashMap<>();

    /**
     * Get all breakpoints in a certain robot
     *
     * @param robot
     * @return a List of line numbers
     */
    public List<Integer> get(final RobotID robot) {
        List<Integer> bps = breakpoints.get(robot);

        if (bps == null) {
            return new ArrayList<>();
        }

        return bps;
    }

    /**
     * Add a breakpoint to the pool
     *
     * @param robot
     * @param line
     */
    public void add(final RobotID robot, final int line) {
        List<Integer> bpList = breakpoints.get(robot);

        if (bpList == null) {
            bpList = new ArrayList<>();
            breakpoints.put(robot, bpList);
        }

        bpList.add(line);
    }

    /**
     * @return A list of all breakpoints
     */
    public List<Breakpoint> get() {
        List<Breakpoint> bpList = new ArrayList<>();

        breakpoints.entrySet().forEach(entry -> entry.getValue().forEach(line -> bpList.add(new Breakpoint(entry.getKey(), line))));

        return bpList;
    }

    /**
     * Clear breakpoints in a certain robot
     *
     * @param robot
     */
    public void clear(final RobotID robot) {
        breakpoints.remove(robot);
    }

    /**
     * Remove all breakpoints
     */
    public void clear() {
        breakpoints.clear();
    }
}
