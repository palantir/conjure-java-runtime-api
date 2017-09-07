/*
 * Copyright 2017 Palantir Technologies, Inc. All rights reserved.
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

import com.palantir.logsafe.Arg;
import com.palantir.remoting.api.errors.ErrorType;
import com.palantir.remoting.api.errors.ServiceException;
import java.util.Objects;
import org.assertj.core.api.AbstractThrowableAssert;

public class ServiceExceptionAssert extends AbstractThrowableAssert<ServiceExceptionAssert, ServiceException> {

    ServiceExceptionAssert(ServiceException actual) {
        super(actual, ServiceExceptionAssert.class);
    }

    public final ServiceExceptionAssert hasType(ErrorType type) {
        isNotNull();
        if (!(actual.getErrorType().equals(type))) {
            failWithMessage("Expected ErrorType to be %s, but found %s", type, actual.getErrorType());
        }

        return this;
    }

    public final ServiceExceptionAssert hasArgs(Arg<?>... args) {
        isNotNull();

        if (args.length != actual.getArgs().size()) {
            failWithMessage("Expected args be size %s, but found %s", args.length, actual.getArgs().size());
        }

        for (int i = 0; i < args.length; i++) {
            Arg<?> expected = args[i];
            Arg<?> actual = this.actual.getArgs().get(i);

            if (!expected.getClass().equals(actual.getClass())) {
                failWithMessage("Expected arg %s to be %s, but found %s", i, expected.getClass(),
                        actual.getClass());
            }
            if (!expected.getName().equals(actual.getName())) {
                failWithMessage("Expected arg %s to be named %s, but found %s", i, expected.getName(),
                        actual.getName());
            }
            Object expectedValue =
                    expected.isSafeForLogging() ? Objects.toString(expected.getValue()) : expected.getValue();
            Object actualValue =
                    actual.isSafeForLogging() ? Objects.toString(actual.getValue()) : actual.getValue();
            if (!expectedValue.equals(actualValue)) {
                failWithMessage("Expected arg %s to have value %s, but found %s", i, expectedValue,
                        actualValue);
            }
        }

        return this;
    }
}
