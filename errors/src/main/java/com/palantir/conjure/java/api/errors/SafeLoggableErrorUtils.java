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
import com.palantir.tritium.ids.UniqueIds;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;

final class SafeLoggableErrorUtils {
    private SafeLoggableErrorUtils() {}

    /**
     * Finds the errorInstanceId of the most recent cause if present, otherwise generates a new random identifier. Note
     * that this only searches {@link Throwable#getCause() causal exceptions}, not {@link Throwable#getSuppressed()
     * suppressed causes}.
     */
    static String generateErrorInstanceId(@Nullable Throwable cause) {
        return generateErrorInstanceId(cause, Collections.newSetFromMap(new IdentityHashMap<>()));
    }

    static String generateErrorInstanceId(
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
        if (cause instanceof CheckedServiceException) {
            return ((CheckedServiceException) cause).getErrorInstanceId();
        }
        if (cause instanceof RemoteException) {
            return ((RemoteException) cause).getError().errorInstanceId();
        }
        return generateErrorInstanceId(cause.getCause(), dejaVu);
    }

    static <T> List<T> copyToUnmodifiableList(T[] elements) {
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

    static String renderUnsafeMessage(String exceptionName, ErrorType errorType, Arg<?>... args) {
        String message = renderNoArgsMessage(exceptionName, errorType);

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

    static String renderNoArgsMessage(String exceptionName, ErrorType errorType) {
        return exceptionName + ": " + errorType.code() + " (" + errorType.name() + ")";
    }
}
