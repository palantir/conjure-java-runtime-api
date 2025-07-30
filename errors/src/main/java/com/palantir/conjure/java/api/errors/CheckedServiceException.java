/*
 * (c) Copyright 2024 Palantir Technologies Inc. All rights reserved.
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
 * An exception raised by a service to indicate an expected error state.
 */
public abstract class CheckedServiceException extends Exception implements SafeLoggable {
    private static final String EXCEPTION_NAME = "CheckedServiceException";
    private final ErrorType errorType;
    private final List<Arg<?>> args; // This is an unmodifiable list.
    private final String errorInstanceId;
    private final String unsafeMessage;
    private final String noArgsMessage;

    /**
     * Creates a new exception for the given error. All {@link com.palantir.logsafe.Arg parameters} are propagated to
     * clients.
     * <p>
     * @deprecated Do not extend this class, it will be removed in a future version.
     */
    @Deprecated
    public CheckedServiceException(ErrorType errorType, Arg<?>... parameters) {
        this(errorType, null, parameters);
    }

    /**
     * As above, but additionally records the cause of this exception.
     * <p>
     * @deprecated Do not extend this class, it will be removed in a future version.
     */
    @Deprecated
    public CheckedServiceException(ErrorType errorType, @Nullable Throwable cause, Arg<?>... args) {
        super(cause);
        this.errorInstanceId = ServiceExceptionUtils.generateErrorInstanceId(cause);
        this.errorType = errorType;
        this.args = ServiceExceptionUtils.arrayToUnmodifiableList(args);
        this.unsafeMessage = ServiceExceptionUtils.renderUnsafeMessage(EXCEPTION_NAME, errorType, args);
        this.noArgsMessage = ServiceExceptionUtils.renderNoArgsMessage(EXCEPTION_NAME, errorType);
    }

    /** The {@link ErrorType} that gave rise to this exception. */
    public final ErrorType getErrorType() {
        return errorType;
    }

    /** A unique identifier for (this instance of) this error. */
    public final String getErrorInstanceId() {
        return errorInstanceId;
    }

    /** A string that includes the exception name, error type, and all arguments irrespective of log-safety. */
    @Override
    public final String getMessage() {
        // Including all args here since any logger not configured with safe-logging will log this message.
        return unsafeMessage;
    }

    /** A string that includes the exception name and error type, without any arguments. */
    @Override
    public final String getLogMessage() {
        return noArgsMessage;
    }

    /** The list of arguments. */
    @Override
    public final List<Arg<?>> getArgs() {
        return args;
    }
}
