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

package com.palantir.conjure.java.api.errors;

import com.palantir.logsafe.Arg;
import com.palantir.logsafe.SafeLoggable;
import java.util.List;
import javax.annotation.Nullable;

/**
 * A {@link ServiceException} thrown in server-side code to indicate server-side {@link ErrorType error states}.
 */
public final class ServiceException extends BaseServiceException implements SafeLoggable {

    private static final String EXCEPTION_NAME = "ServiceException";

    public ServiceException(ErrorType errorType, Arg<?>... parameters) {
        super(errorType, parameters);
    }

    public ServiceException(ErrorType errorType, @Nullable Throwable cause, Arg<?>... args) {
        super(errorType, cause, args);
    }

    @Override
    protected String exceptionName() {
        return EXCEPTION_NAME;
    }

    /**
     * Deprecated.
     *
     * @deprecated use {@link #getArgs}.
     */
    @Deprecated
    public List<Arg<?>> getParameters() {
        return getArgs();
    }
}
