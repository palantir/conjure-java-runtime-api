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
import com.palantir.tritium.ids.UniqueIds;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;

/** A {@link ServiceException} thrown in server-side code to indicate server-side {@link ErrorType error states}. */
public final class ServiceException extends RuntimeException implements SafeLoggable {

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

        this.errorInstanceId = generateErrorInstanceId(cause);
        this.errorType = errorType;
        // Note that instantiators cannot mutate List<> args since it comes through copyToList in all code paths.
        this.args = copyToUnmodifiableList(args);
        this.unsafeMessage = renderUnsafeMessage(errorType, args);
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

    private static <T> List<T> copyToUnmodifiableList(T[] elements) {
        if (elements == null || elements.length == 0) {
            return Collections.emptyList();
        }
        List<T> list = new ArrayList<>(elements.length);
        for (T item : elements) {
            if (item != null) {
                list.add(item);
            }
        }
        return Collections.unmodifiableList(list);
    }

    private static String renderUnsafeMessage(ErrorType errorType, Arg<?>... args) {
        String message = renderNoArgsMessage(errorType);

        if (args == null || args.length == 0) {
            return message;
        }

        StringBuilder builder = new StringBuilder();
        boolean first = true;
        builder.append(message).append(": {");
        for (Arg<?> arg : args) {
            if (arg != null) {
                if (first) {
                    first = false;
                } else {
                    builder.append(", ");
                }
                builder.append(arg.getName()).append("=").append(arg.getValue());
            }
        }
        builder.append("}");

        return builder.toString();
    }

    private static String renderNoArgsMessage(ErrorType errorType) {
        return "ServiceException: " + errorType.code() + " (" + errorType.name() + ")";
    }

    /**
     * Finds the errorInstanceId of the most recent cause if present, otherwise generates a new random identifier. Note
     * that this only searches {@link Throwable#getCause() causal exceptions}, not {@link Throwable#getSuppressed()
     * suppressed causes}.
     */
    private static String generateErrorInstanceId(@Nullable Throwable cause) {
        return generateErrorInstanceId(cause, Collections.newSetFromMap(new IdentityHashMap<>()));
    }

    private static String generateErrorInstanceId(
            @Nullable Throwable cause,
            // Guard against cause cycles, see Throwable.printStackTrace(PrintStreamOrWriter)
            Set<Throwable> dejaVu) {
        if (cause == null || !dejaVu.add(cause)) {
            // we don't need cryptographically secure random UUIDs
            return UniqueIds.pseudoRandomUuidV4().toString();
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
