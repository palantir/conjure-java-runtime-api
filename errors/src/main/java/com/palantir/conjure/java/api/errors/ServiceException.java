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
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;

/** A {@link ServiceException} thrown in server-side code to indicate server-side {@link ErrorType error states}. */
public final class ServiceException extends RuntimeException implements SafeLoggable {

    private final ErrorType errorType;
    private final String errorInstanceId;
    private final String noArgsMessage;
    private final List<Arg<?>> args; // unmodifiable

    /**
     * Creates a new exception for the given error. All {@link com.palantir.logsafe.Arg parameters} are
     * propagated to clients; they are serialized via {@link Object#toString}.
     */
    public ServiceException(ErrorType errorType, Arg<?>... parameters) {
        this(errorType, null, parameters);
    }

    /** As above, but additionally records the cause of this exception. */
    public ServiceException(ErrorType errorType, @Nullable Throwable cause, Arg<?>... args) {
        // TODO(rfink): Memoize formatting?
        super(renderUnsafeMessage(errorType, args), cause);

        this.errorInstanceId = generateErrorInstanceId(cause);
        this.errorType = errorType;
        // Note that instantiators cannot mutate List<> args since it comes through copyToList in all code paths.
        this.args = copyArgsToUnmodifiableList(args);
        this.noArgsMessage = renderNoArgsMessage(errorType);
    }

    /** The {@link ErrorType} that gave rise to this exception. */
    public ErrorType getErrorType() {
        return errorType;
    }

    /** A unique identifier for (this instance of) this error. */
    public String getErrorInstanceId() {
        return errorInstanceId;
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

    private static List<Arg<?>> copyArgsToUnmodifiableList(Arg<?>[] args) {
        List<Arg<?>> argsList = new ArrayList<>(args.length);
        Collections.addAll(argsList, args);
        return Collections.unmodifiableList(argsList);
    }

    private static String renderUnsafeMessage(ErrorType errorType, Arg<?>... args) {
        String message = renderNoArgsMessage(errorType);

        if (args.length == 0) {
            return message;
        }

        StringBuilder builder = new StringBuilder();
        builder.append(message).append(": {");
        for (int i = 0; i < args.length; i++) {
            Arg<?> arg = args[i];
            if (i > 0) {
                builder.append(", ");
            }
            builder.append(arg.getName()).append("=").append(arg.getValue());
        }
        builder.append("}");

        return builder.toString();
    }

    private static String renderNoArgsMessage(ErrorType errorType) {
        return String.format("ServiceException: %s (%s)", errorType.code(), errorType.name());
    }

    /**
     * Finds the errorInstanceId of the most recent cause if present, otherwise generates a new random identifier.
     * Note that this only searches {@link Throwable#getCause() causal exceptions}, not
     * {@link Throwable#getSuppressed() suppressed causes}.
     */
    private static String generateErrorInstanceId(@Nullable Throwable cause) {
        return generateErrorInstanceId(cause, Collections.newSetFromMap(new IdentityHashMap<>()));
    }

    private static String generateErrorInstanceId(
            @Nullable Throwable cause,
            // Guard against cause cycles, see Throwable.printStackTrace(PrintStreamOrWriter)
            Set<Throwable> dejaVu) {
        if (cause == null || !dejaVu.add(cause)) {
            return UUID.randomUUID().toString();
        }
        if (cause instanceof ServiceException) {
            return ((ServiceException) cause).getErrorInstanceId();
        }
        if (cause instanceof RemoteException) {
            return ((RemoteException) cause).getError().errorInstanceId();
        }
        return generateErrorInstanceId(cause.getCause(), dejaVu);
    }
}
