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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;

/** A {@link ServiceException} thrown in server-side code to indicate server-side {@link ErrorType error states}. */
public final class ServiceException extends RuntimeException implements SafeLoggable {

    private final ErrorType errorType;
    private final List<Arg<?>> args;  // unmodifiable

    private final String errorId = UUID.randomUUID().toString();
    private final String safeMessage;
    private final String noArgsMessage;

    /**
     * Creates a new exception for the given error. All {@link com.palantir.logsafe.SafeArg safe} parameters are
     * propagated to clients; they are serialized via {@link Object#toString}.
     */
    public ServiceException(ErrorType errorType, Arg<?>... parameters) {
        this(errorType, null, copyToList(parameters));
    }

    /** As above, but additionally records the cause of this exception. */
    public ServiceException(ErrorType errorType, @Nullable Throwable cause, Arg<?>... args) {
        this(errorType, cause, copyToList(args));
    }

    private ServiceException(ErrorType errorType, @Nullable Throwable cause, List<Arg<?>> args) {
        // TODO(rfink): Memoize formatting?
        super(renderSafeMessage(errorType, args), cause);

        this.errorType = errorType;
        // Note that instantiators cannot mutate List<> args since it comes through copyToList in all code paths.
        this.args = Collections.unmodifiableList(args);
        this.safeMessage = renderSafeMessage(errorType, args);
        this.noArgsMessage = renderNoArgsMessage(errorType);
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
        // Including safe args here since any logger not configured with safe-logging will log this message.
        return safeMessage;
    }

    @Override
    public String getLogMessage() {
        // Not returning safe args here since the safe-logging framework will log this message + args explicitly.
        return noArgsMessage;
    }

    private static String renderSafeMessage(ErrorType errorType, List<Arg<?>> args) {
        String message = renderNoArgsMessage(errorType);

        if (args.isEmpty()) {
            return message;
        }

        StringBuilder builder = new StringBuilder();
        builder.append(message).append(": {");
        for (int i = 0; i < args.size(); i++) {
            Arg<?> arg = args.get(i);
            if (arg.isSafeForLogging()) {
                if (i > 0) {
                    builder.append(", ");
                }

                builder.append(arg.getName()).append("=").append(arg.getValue());
            }
        }
        builder.append("}");

        return builder.toString();
    }

    private static String renderNoArgsMessage(ErrorType errorType) {
        return errorType.code().name().equals(errorType.name())
                ? String.format("ServiceException: %s", errorType.code())
                : String.format("ServiceException: %s (%s)", errorType.code(), errorType.name());
    }

    @Override
    public List<Arg<?>> getArgs() {
        return args;
    }

    private static <T> List<T> copyToList(T[] elements) {
        ArrayList<T> list = new ArrayList<>(elements.length);
        Collections.addAll(list, elements);
        return list;
    }
}
