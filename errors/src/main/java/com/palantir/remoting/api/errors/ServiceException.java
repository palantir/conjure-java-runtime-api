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

package com.palantir.remoting.api.errors;

import com.palantir.logsafe.Arg;
import com.palantir.logsafe.SafeLoggable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;

/** A {@link ServiceException} thrown in server-side code to indicate server-side {@link ErrorType error states}. */
public final class ServiceException extends RuntimeException implements SafeLoggable {

    private final ErrorType errorType;
    private final List<Arg<?>> args;  // unmodifiable

    private final String errorId = UUID.randomUUID().toString();
    private final String message;

    /**
     * Creates a new exception for the given error. All {@link com.palantir.logsafe.SafeArg safe} parameters are
     * propagated to clients; they are serialized via {@link Object#toString}.
     */
    public ServiceException(ErrorType errorType, Arg<?>... parameters) {
        this(errorType, null, Arrays.asList(parameters));
    }

    /** As above, but additionally records the cause of this exception. */
    public ServiceException(ErrorType errorType, @Nullable Throwable cause, Arg<?>... args) {
        this(errorType, cause, Arrays.asList(args));
    }

    private ServiceException(ErrorType errorType, @Nullable Throwable cause, List<Arg<?>> args) {
        super(formatMessage(errorType, args), cause);

        // TODO(rfink): Memoize formatting?
        this.errorType = errorType;
        this.args = Collections.unmodifiableList(args);
        this.message = formatMessage(errorType, args);
    }

    /** The {@link ErrorType} that gave rise to this exception. */
    public ErrorType getErrorType() {
        return errorType;
    }

    /** A unique identifier for this error. */
    public String getErrorId() {
        return errorId;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public String getLogMessage() {
        return message;
    }

    private static String formatMessage(ErrorType errorType, List<Arg<?>> args) {
        String message = String.format("%s with name %s and description %s",
                ErrorType.class.getSimpleName(), errorType.name(), errorType.description());
        if (args.isEmpty()) {
            return message;
        }

        StringBuilder builder = new StringBuilder();
        builder.append(message).append(": {");
        for (int i = 0; i < args.size(); i++) {
            if (i > 0) {
                builder.append(", ");
            }

            Arg<?> arg = args.get(i);
            builder.append(arg.getName()).append("=").append(arg.getValue());
        }
        builder.append("}");

        return builder.toString();
    }

    @Override
    public List<Arg<?>> getArgs() {
        return args;
    }
}
