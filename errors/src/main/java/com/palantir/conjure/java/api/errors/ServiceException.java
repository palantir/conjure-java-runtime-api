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

/** A {@link ServiceException} thrown in server-side code to indicate server-side {@link ErrorType error states}. */
public final class ServiceException extends RuntimeException implements SafeLoggable, TypedException {

    private final ErrorType errorType;
    private final List<Arg<?>> args; // unmodifiable

    private final String errorInstanceId;
    private final String unsafeMessage;
    private final String noArgsMessage;

    /**
     * Creates a new exception for the given error. All {@link com.palantir.logsafe.Arg parameters} are propagated to
     * clients; they are serialized via {@link Object#toString}.
     */
    public ServiceException(ErrorType errorType, Arg<?>... parameters) {
        this(errorType, null, parameters);
    }

    /** As above, but additionally records the cause of this exception. */
    public ServiceException(ErrorType errorType, @Nullable Throwable cause, Arg<?>... args) {
        // TODO(rfink): Memoize formatting?
        super(cause);

        this.errorInstanceId = ServiceExceptionUtils.generateErrorInstanceId(cause);
        this.errorType = errorType;
        // Note that instantiators cannot mutate List<> args since it comes through copyToList in all code paths.
        this.args = ServiceExceptionUtils.copyToUnmodifiableList(args);
        this.unsafeMessage = ServiceExceptionUtils.renderUnsafeMessage(errorType, this.getClass(), args);
        this.noArgsMessage = ServiceExceptionUtils.renderNoArgsMessage(errorType, this.getClass());
    }

    @Override
    public ErrorType getErrorType() {
        return errorType;
    }

    @Override
    public String getErrorInstanceId() {
        return errorInstanceId;
    }

    @Override
    public String getMessage() {
        // Including all args here since any logger not configured with safe-logging will log this message.
        return unsafeMessage;
    }

    @Override
    public String getLogMessage() {
        // Not returning safe args here since the safe-logging framework will log this message + args explicitly.
        return noArgsMessage;
    }

    @Override
    public List<Arg<?>> getArgs() {
        return args;
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
