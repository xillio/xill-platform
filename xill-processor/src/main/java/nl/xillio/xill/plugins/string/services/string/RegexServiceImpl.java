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
package nl.xillio.xill.plugins.string.services.string;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import nl.xillio.xill.api.XillThreadFactory;
import nl.xillio.xill.api.errors.RobotRuntimeException;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

/**
 * This is the main implementation of the RegexService
 */
@Singleton
public class RegexServiceImpl implements RegexService {

    // Regex for escaping a string so it can be included inside a regex
    public static final Pattern REGEX_ESCAPE_PATTERN = Pattern.compile("\\\\[a-zA-Z0-9_]|\\W");
    // The default timeout for regular expressions.
    private static final int REGEX_TIMEOUT = 5000;

    private final CachedTimer cachedTimer;

    /**
     * The implementation of the {@link RegexService}
     */
    @Inject
    public RegexServiceImpl(XillThreadFactory xillThreadFactory) {
        cachedTimer = new CachedTimer();
        Thread thread = xillThreadFactory.create(cachedTimer, "Regex Cached Timer");
        thread.setDaemon(true);
        thread.start();
    }


    @Override
    public Matcher getMatcher(final String regex, final String value, int timeout) throws IllegalArgumentException {
        long targetTime;

        if (timeout < 0) {
            // If no (valid) timeout is given, use the default timeout
            targetTime = REGEX_TIMEOUT + cachedTimer.getCachedTime();
        } else if (timeout == 0) {
            // Use no time out
            targetTime = Long.MAX_VALUE;
        } else {
            targetTime = timeout + cachedTimer.getCachedTime();
        }

        return Pattern.compile(regex, Pattern.DOTALL).matcher(new TimeoutCharSequence(value, targetTime));
    }

    @Override
    public int getRegexTimeout() {
        return REGEX_TIMEOUT;
    }

    @Override
    public boolean matches(final Matcher matcher) {
        return matcher.matches();
    }

    @Override
    public String replaceAll(final Matcher matcher, final String replacement) {
        return matcher.replaceAll(replacement);
    }

    @Override
    public String replaceFirst(final Matcher matcher, final String replacement) {
        return matcher.replaceFirst(replacement);
    }

    @Override
    public List<String> tryMatch(final Matcher matcher) {
        List<String> list = new ArrayList<>();
        while (matcher.find()) {
            list.add(matcher.group());
        }
        return list;
    }

    @Override
    public List<String> tryMatchElseNull(final Matcher matcher) {
        List<String> list = new ArrayList<>();
        for (int i = 0; i <= matcher.groupCount(); i++) {
            list.add(matcher.group(i));
        }
        return list;
    }

    @Override
    public String escapeRegex(String toEscape) {
        Matcher matcher = REGEX_ESCAPE_PATTERN.matcher(toEscape);
        return matcher.replaceAll("\\\\$0");
    }

    /**
     * Char sequence that can be timed out (to prevent "infinite"  loops for regex checking)
     */
    private class TimeoutCharSequence implements CharSequence {

        private final CharSequence inner;
        private final long targetTime;

        public TimeoutCharSequence(final CharSequence inner, long targetTime) {
            super();
            this.targetTime = targetTime;
            this.inner = inner;
        }

        @Override
        public char charAt(final int index) {
            if (targetTime != 0 && cachedTimer.getCachedTime() > this.targetTime) {
                throw new RobotRuntimeException("Pattern match timed out!");
            }
            return inner.charAt(index);
        }

        @Override
        public IntStream chars() {
            return inner.chars();
        }

        @Override
        public IntStream codePoints() {
            return inner.codePoints();
        }

        @Override
        public int length() {
            return inner.length();
        }

        @Override
        public CharSequence subSequence(final int start, final int end) {
            return new TimeoutCharSequence(inner.subSequence(start, end), targetTime);
        }

        @Override
        public String toString() {
            return inner.toString();
        }
    }

    /**
     * Class to cache time retrieved from System.currentTimeMillis.
     * This is cached because the high precision is not necessary and would be a too great performance hit.
     */
    private class CachedTimer implements Runnable {
        private long cachedTime;

        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                cachedTime = System.currentTimeMillis();
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    break;
                }
            }
        }

        public long getCachedTime() {
            return cachedTime;
        }
    }

}
