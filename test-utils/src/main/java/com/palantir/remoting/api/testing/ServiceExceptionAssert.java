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
import com.palantir.logsafe.SafeArg;
import com.palantir.remoting.api.errors.ErrorType;
import com.palantir.remoting.api.errors.ServiceException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
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

        // toString is called on SafeArgs in SerializableError
        List<Serializable> actualStrings = actual.getArgs().stream()
                .map(arg -> arg instanceof SafeArg ? arg.toString() : arg)
                .collect(Collectors.toList());
        List<Serializable> givenStrings = Arrays.asList(args).stream()
                .map(arg -> arg instanceof SafeArg ? arg.toString() : arg)
                .collect(Collectors.toList());
        if (!(actualStrings.equals(givenStrings))) {
            failWithMessage("Expected args to be %s, but found %s", givenStrings, actualStrings);
        }

        return this;
    }
}
