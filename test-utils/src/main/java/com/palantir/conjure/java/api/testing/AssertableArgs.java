/*
 * (c) Copyright 2025 Palantir Technologies Inc. All rights reserved.
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

package com.palantir.conjure.java.api.testing;

import com.palantir.logsafe.Arg;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

record AssertableArgs(Map<String, Object> safeArgs, Map<String, Object> unsafeArgs) {
    static AssertableArgs fromArgs(List<Arg<?>> args) {
        Map<String, Object> safeArgs = new HashMap<>();
        Map<String, Object> unsafeArgs = new HashMap<>();
        args.forEach(arg -> {
            if (arg.isSafeForLogging()) {
                assertPutSafe(safeArgs, arg);
            } else {
                assertPutUnsafe(unsafeArgs, arg);
            }
        });
        return new AssertableArgs(safeArgs, unsafeArgs);
    }

    private static void assertPutSafe(Map<String, Object> args, Arg<?> arg) {
        assertPut(args, arg.getName(), arg.getValue(), "safe");
    }

    private static void assertPutUnsafe(Map<String, Object> args, Arg<?> arg) {
        assertPut(args, arg.getName(), arg.getValue(), "unsafe");
    }

    private static void assertPut(Map<String, Object> map, String key, Object value, String name) {
        Object previous = map.put(key, value);
        if (previous != null) {
            throw new AssertionError(String.format(
                    "Duplicate %s arg name '%s', first value: %s, second value: %s", name, key, previous, value));
        }
    }
}
