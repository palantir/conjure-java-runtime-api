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

import com.palantir.conjure.java.api.errors.ErrorType;
import com.palantir.logsafe.Arg;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.assertj.core.api.AbstractThrowableAssert;
import org.assertj.core.util.Throwables;

abstract class AbstractServiceExceptionAssert<T extends AbstractThrowableAssert<T, U>, U extends Throwable>
        extends AbstractThrowableAssert<T, U> {

    protected AbstractServiceExceptionAssert(U throwable, Class<T> selfType) {
        super(throwable, selfType);
    }

    void hasCode(ErrorType.Code code, ErrorType.Code actualCode) {
        isNotNull();
        failIfNotEqual("Expected ErrorType.Code to be %s, but found %s", code, actualCode);
    }

    void hasType(ErrorType type, ErrorType actualType) {
        isNotNull();
        failIfNotEqual("Expected ErrorType to be %s, but found %s", type, actualType);
    }

    final void hasArgs(List<Arg<?>> actualArguments, Arg<?>... expectedArguments) {
        isNotNull();

        AssertableArgs actualArgs = AssertableArgs.fromArgs(actualArguments);
        AssertableArgs expectedArgs = AssertableArgs.fromArgs(Arrays.asList(expectedArguments));

        failIfNotEqual("Expected safe args to be %s, but found %s", expectedArgs.safeArgs(), actualArgs.safeArgs());
        failIfNotEqual(
                "Expected unsafe args to be %s, but found %s", expectedArgs.unsafeArgs(), actualArgs.unsafeArgs());
    }

    final void hasNoArgs(List<Arg<?>> actualArguments) {
        isNotNull();
        AssertableArgs actualArgs = AssertableArgs.fromArgs(actualArguments);
        if (!actualArgs.safeArgs().isEmpty() || !actualArgs.unsafeArgs().isEmpty()) {
            Map<String, Object> allArgs = new HashMap<>();
            allArgs.putAll(actualArgs.safeArgs());
            allArgs.putAll(actualArgs.unsafeArgs());
            failWithMessage(
                    "Expected no args, but found %s; service exception: %s", allArgs, Throwables.getStackTrace(actual));
        }
    }

    final void containsArgs(List<Arg<?>> actualArguments, Arg<?>... expectedArguments) {
        isNotNull();

        AssertableArgs actualArgs = AssertableArgs.fromArgs(actualArguments);
        AssertableArgs expectedArgs = AssertableArgs.fromArgs(Arrays.asList(expectedArguments));

        failIfDoesNotContain(
                "Expected safe args to contain %s, but found %s", expectedArgs.safeArgs(), actualArgs.safeArgs());
        failIfDoesNotContain(
                "Expected unsafe args to contain %s, but found %s", expectedArgs.unsafeArgs(), actualArgs.unsafeArgs());
    }

    void failIfDoesNotContain(String message, Map<String, Object> expectedArgs, Map<String, Object> actualArgs) {
        if (!actualArgs.entrySet().containsAll(expectedArgs.entrySet())) {
            failWithMessage(
                    message + "; service exception: %s", expectedArgs, actualArgs, Throwables.getStackTrace(actual));
        }
    }

    <V> void failIfNotEqual(String message, V expectedValue, V actualValue) {
        if (!Objects.equals(expectedValue, actualValue)) {
            failWithMessage(
                    message + "; service exception: ", expectedValue, actualValue, Throwables.getStackTrace(actual));
        }
    }
}
