/*
 * (c) Copyright 2017 Palantir Technologies Inc. All rights reserved.
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

import com.palantir.conjure.java.api.errors.ErrorType;
import com.palantir.conjure.java.api.errors.ServiceException;
import com.palantir.logsafe.Arg;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.assertj.core.api.AbstractThrowableAssert;

public class ServiceExceptionAssert extends AbstractThrowableAssert<ServiceExceptionAssert, ServiceException> {

    ServiceExceptionAssert(ServiceException actual) {
        super(actual, ServiceExceptionAssert.class);
    }

    public final ServiceExceptionAssert hasType(ErrorType type) {
        isNotNull();
        failIfNotEqual("Expected ErrorType to be %s, but found %s", type, actual.getErrorType());
        return this;
    }

    public final ServiceExceptionAssert hasArgs(Arg<?>... args) {
        isNotNull();

        AssertableArgs actualArgs = new AssertableArgs(actual.getParameters());
        AssertableArgs expectedArgs = new AssertableArgs(Arrays.asList(args));

        failIfNotEqual("Expected safe args to be %s, but found %s", expectedArgs.safeArgs, actualArgs.safeArgs);
        failIfNotEqual("Expected unsafe args to be %s, but found %s", expectedArgs.unsafeArgs, actualArgs.unsafeArgs);

        return this;
    }

    private void failIfNotEqual(String message, Object expected, Object actual) {
        if (!Objects.equals(expected, actual)) {
            failWithMessage(message, expected, actual);
        }
    }

    private static final class AssertableArgs {
        private final Map<String, Object> safeArgs = new HashMap<>();
        private final Map<String, Object> unsafeArgs = new HashMap<>();

        private AssertableArgs(List<Arg<?>> args) {
            args.forEach(arg -> {
                if (arg.isSafeForLogging()) {
                    assertPutSafe(arg);
                } else {
                    assertPutUnsafe(arg);
                }
            });
        }

        private void assertPutSafe(Arg<?> arg) {
            assertPut(safeArgs, arg.getName(), arg.getValue(), "safe");
        }

        private void assertPutUnsafe(Arg<?> arg) {
            assertPut(unsafeArgs, arg.getName(), arg.getValue(), "unsafe");
        }

        private static void assertPut(Map<String, Object> map, String key, Object value, String name) {
            Object previous = map.put(key, value);
            if (previous != null) {
                throw new AssertionError(String.format("Duplicate %s arg name '%s', first value: %s, second value: %s",
                        name, key, previous, value));
            }
        }
    }
}
