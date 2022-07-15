/*
 * (c) Copyright 2022 Palantir Technologies Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.palantir.conjure.java.api.config.service;

import com.google.errorprone.annotations.Immutable;

/**
 * An optimized version parser that supports version numbers with up to four segments,
 * covering the most common {@link com.palantir.sls.versions.OrderableSlsVersion} and
 * browser user-agent version strings.
 * <p>
 * We're using parser combinator-style ideas here, where each parser function
 * {@link #number(String, int)} accepts an index into our source string and returns two values:
 * <p>
 * - an updated index into the string representing how many characters were parsed
 * - a value that was actually parsed out of the string (if the parser was able to parse the string), otherwise a
 * clear signal that the parser failed (we use {@link Integer#MIN_VALUE} for this).
 * <p>
 * We bit-pack these two integer values into a single long using {@link #ok(int, int)} and {@link #fail(int)} functions
 * because primitive longs live on the stack and don't impact GC.
 */
@Immutable
final class VersionParser {
    private VersionParser() {}

    /** Returns the count of version number groups parsed if a valid version string, or -1 otherwise. */
    public static int countNumericDotGroups(String string) {
        long state = 0;
        for (int i = 1; getIndex(state) < string.length(); i++) {
            state = number(string, getIndex(state));
            if (failed(state)) {
                return -1;
            }

            state = literalDot(string, getIndex(state));
            if (failed(state)) {
                if (getIndex(state) < string.length()) {
                    return -1; // reject due to trailing stuff
                }
                return i; // no more dots
            }
        }

        // reject due to trailing stuff
        return -1;
    }

    private static final long INT_MASK = (1L << 32) - 1;
    private static final int PARSE_FAILED = Integer.MIN_VALUE;

    static long number(String string, int startIndex) {
        int next = startIndex;
        int len = string.length();
        while (next < len) {
            int codepoint = string.codePointAt(next);
            if (Character.isDigit(codepoint)) {
                next += 1;
            } else {
                break;
            }
        }
        if (next == startIndex) {
            return fail(startIndex);
        } else if (next == startIndex + 1) {
            return ok(next, Character.digit(string.codePointAt(startIndex), 10));
        } else {
            try {
                int result = Integer.parseUnsignedInt(string, startIndex, next, 10);
                if (result < 0) {
                    // i.e. we overflowed the int
                    return fail(startIndex);
                }
                return ok(next, result);
            } catch (NumberFormatException e) {
                if (e.getMessage() != null && e.getMessage().endsWith("exceeds range of unsigned int.")) {
                    return fail(startIndex);
                } else {
                    throw e;
                }
            }
        }
    }

    static long literalDot(String string, int startIndex) {
        if (startIndex < string.length() && string.codePointAt(startIndex) == '.') {
            return ok(startIndex + 1, 0);
        } else {
            return fail(startIndex);
        }
    }

    /**
     * We are bit-packing two integers into a single long.  The 'index' occupies half of the bits and the 'result'
     * occupies the other half.
     */
    static long ok(int index, int result) {
        return ((long) index) << 32 | (result & INT_MASK);
    }

    static long fail(int index) {
        return ((long) index) << 32 | (PARSE_FAILED & INT_MASK);
    }

    static boolean isOk(long state) {
        return getResult(state) != PARSE_FAILED;
    }

    static boolean failed(long state) {
        return !isOk(state);
    }

    static int getResult(long state) {
        return (int) (state & INT_MASK);
    }

    static int getIndex(long state) {
        return (int) (state >>> 32);
    }
}
