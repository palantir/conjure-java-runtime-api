/*
 * (c) Copyright 2021 Palantir Technologies Inc. All rights reserved.
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

import com.palantir.conjure.java.api.errors.ErrorType.Code;
import com.palantir.conjure.java.api.errors.SerializableError.Builder;
import com.palantir.logsafe.Arg;
import com.palantir.logsafe.SafeLoggable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;

public final class FieldMissingException extends RuntimeException implements SafeLoggable {
    public static final ErrorType ERROR_TYPE = ErrorType.create(Code.INVALID_ARGUMENT, "Error:MissingField");
    private final List<Arg<?>> args; // unmodifiable

    private final String errorInstanceId;
    private final String unsafeMessage;
    private final String noArgsMessage;

    public FieldMissingException(Arg<?>... args) {
        this(null, args);
    }

    public FieldMissingException(Throwable cause, Arg<?>... args) {
        super(cause);
        this.errorInstanceId = generateErrorInstanceId(cause);
        this.args = copyToUnmodifiableList(args);
        this.unsafeMessage = renderUnsafeMessage(ERROR_TYPE, args);
        this.noArgsMessage = renderNoArgsMessage(ERROR_TYPE);
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

    @Override
    public String getMessage() {
        // Including all args here since any logger not configured with safe-logging will log this message.
        return unsafeMessage;
    }

    public String getErrorInstanceId() {
        return errorInstanceId;
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
        return String.format("FieldMissingException: %s (%s)", errorType.code(), errorType.name());
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

    public SerializableError asSerializableError() {
        SerializableError.Builder builder = new Builder()
                .errorCode(FieldMissingException.ERROR_TYPE.code().toString())
                .errorName(FieldMissingException.ERROR_TYPE.name())
                .errorInstanceId(getErrorInstanceId());

        for (Arg<?> arg : getArgs()) {
            builder.putParameters(arg.getName(), Objects.toString(arg.getValue()));
        }

        return builder.build();
    }
}
