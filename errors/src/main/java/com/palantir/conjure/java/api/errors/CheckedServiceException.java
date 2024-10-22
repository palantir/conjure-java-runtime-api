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
import java.util.List;
import javax.annotation.Nullable;

public abstract class CheckedServiceException extends Exception implements SafeLoggableError {
    private static final String EXCEPTION_NAME = "CheckedServiceException";

    private final ErrorType errorType;
    private final List<Arg<?>> args; // unmodifiable

    private final String errorInstanceId;
    private final String unsafeMessage;
    private final String noArgsMessage;

    /**
     * Creates a new exception for the given error. All {@link com.palantir.logsafe.Arg parameters} are propagated to
     * clients.
     */
    public CheckedServiceException(ErrorType errorType, Arg<?>... parameters) {
        this(errorType, null, parameters);
    }

    /** As above, but additionally records the cause of this exception. */
    public CheckedServiceException(ErrorType errorType, @Nullable Throwable cause, Arg<?>... args) {
        super(cause);
        this.errorInstanceId = SafeLoggableErrorUtils.generateErrorInstanceId(cause);
        this.errorType = errorType;
        this.args = SafeLoggableErrorUtils.copyToUnmodifiableList(args);
        this.unsafeMessage = SafeLoggableErrorUtils.renderUnsafeMessage(EXCEPTION_NAME, errorType, args);
        this.noArgsMessage = SafeLoggableErrorUtils.renderNoArgsMessage(EXCEPTION_NAME, errorType);
    }

    /** The {@link ErrorType} that gave rise to this exception. */
    @Override
    public ErrorType getErrorType() {
        return errorType;
    }

    /** A unique identifier for (this instance of) this error. */
    @Override
    public String getErrorInstanceId() {
        return errorInstanceId;
    }

    /**
     * Java doc.
     */
    @Override
    public String getMessage() {
        // Including all args here since any logger not configured with safe-logging will log this message.
        return unsafeMessage;
    }

    /**
     * Java doc.
     */
    @Override
    public String getLogMessage() {
        // Not returning safe args here since the safe-logging framework will log this message + args explicitly.
        return noArgsMessage;
    }

    /**
     * Java doc.
     */
    @Override
    public List<Arg<?>> getArgs() {
        return args;
    }
}
