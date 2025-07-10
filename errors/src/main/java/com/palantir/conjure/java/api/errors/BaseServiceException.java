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
import java.util.List;
import javax.annotation.Nullable;

public abstract class BaseServiceException extends RuntimeException {

    protected final ErrorType errorType;

    protected final List<Arg<?>> args; // unmodifiable

    protected final String errorInstanceId;

    protected final String unsafeMessage;
    protected final String noArgsMessage;

    /**
     * Creates a new exception for the given error. All {@link com.palantir.logsafe.Arg parameters} are propagated to
     * clients; they are serialized via {@link Object#toString}.
     */
    BaseServiceException(ErrorType errorType, Arg<?>... parameters) {
        this(errorType, null, parameters);
    }

    /**
     * As above, but additionally records the cause of this exception.
     */
    BaseServiceException(ErrorType errorType, @Nullable Throwable cause, Arg<?>... args) {
        // TODO(rfink): Memoize formatting?
        super(cause);

        this.errorType = errorType;
        this.errorInstanceId = ServiceExceptionUtils.generateErrorInstanceId(cause);

        // Note that instantiators cannot mutate List<> args since it comes through copyToList in all code paths.
        this.args = ServiceExceptionUtils.arrayToUnmodifiableList(args);

        this.unsafeMessage = ServiceExceptionUtils.renderUnsafeMessage(exceptionName(), errorType, args);
        this.noArgsMessage = ServiceExceptionUtils.renderNoArgsMessage(exceptionName(), errorType);
    }

    /**
     * The name of the exception. Typically, the class name.
     */
    protected abstract String exceptionName();

    /**
     * The {@link ErrorType} that gave rise to this exception.
     */
    public ErrorType getErrorType() {
        return errorType;
    }

    /**
     * A unique identifier for (this instance of) this error.
     */
    public String getErrorInstanceId() {
        return errorInstanceId;
    }

    @Override
    public String getMessage() {
        // Including all args here since any logger not configured with safe-logging will log this message.
        return unsafeMessage;
    }

    public String getLogMessage() {
        // Not returning safe args here since the safe-logging framework will log this message + args explicitly.
        return noArgsMessage;
    }

    public List<Arg<?>> getArgs() {
        return args;
    }
}
