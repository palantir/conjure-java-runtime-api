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

package com.palantir.conjure.java.api.errors;

import com.palantir.logsafe.Arg;
import com.palantir.logsafe.SafeLoggable;
import javax.annotation.Nullable;

/*
 * This is identical to ServiceException, but is used in Conjure-generated code to indicate that an exception was thrown
 * from a service endpoint.
 */
public class EndpointServiceException extends BaseServiceException implements SafeLoggable {

    private static final String EXCEPTION_NAME = "EndpointServiceException";

    public EndpointServiceException(ErrorType errorType, Arg<?>... parameters) {
        super(errorType, parameters);
    }

    public EndpointServiceException(ErrorType errorType, @Nullable Throwable cause, Arg<?>... args) {
        super(errorType, cause, args);
    }

    @Override
    protected String exceptionName() {
        return EXCEPTION_NAME;
    }
}
