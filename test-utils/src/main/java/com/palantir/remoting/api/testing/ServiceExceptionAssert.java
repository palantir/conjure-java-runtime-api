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

package com.palantir.remoting.api.testing;

import com.google.common.collect.ImmutableList;
import com.palantir.logsafe.Arg;
import com.palantir.remoting.api.errors.ErrorType;
import com.palantir.remoting.api.errors.ServiceException;
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

        AssertableArgs actualArgs = new AssertableArgs(actual.getArgs());
        AssertableArgs expectedArgs = new AssertableArgs(ImmutableList.copyOf(args));

        failIfNotEqual("Expected safe args to be %s, but found %s", expectedArgs.safeArgs, actualArgs.safeArgs);
        failIfNotEqual("Expected unsafe args to be %s, but found %s", expectedArgs.unsafeArgs, actualArgs.unsafeArgs);

        return this;
    }

    private void failIfNotEqual(String message, Object expected, Object actual) {
        if (!Objects.equals(expected, actual)) {
            failWithMessage(message, expected, actual);
        }
    }

    private static class AssertableArgs {
        private final Map<String, Object> safeArgs = new HashMap<>();
        private final Map<String, Object> unsafeArgs = new HashMap<>();

        private AssertableArgs(List<Arg<?>> args) {
            args.forEach(arg -> {
                if (arg.isSafeForLogging()) {
                    if (safeArgs.put(arg.getName(), arg.getValue()) != null) {
                        throw new AssertionError(String.format("Duplicate safe arg name '%s'", arg.getName()));
                    }
                } else {
                    if (unsafeArgs.put(arg.getName(), arg.getValue()) != null) {
                        throw new AssertionError(String.format("Duplicate unsafe arg name '%s'", arg.getName()));
                    }
                }
            });
        }
    }
}
