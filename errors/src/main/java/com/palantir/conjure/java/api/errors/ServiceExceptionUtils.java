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

/**
 * This class is a collection of methods useful for creating {@link ServiceException}s and {@link CheckedServiceException}s.
 */
final class ServiceExceptionUtils {
    private ServiceExceptionUtils() {}

    /**
     * Creates an unmodifiable list from the given array. Null entries are filtered out as unmodifiable lists cannot
     * have null elements.
     *
     * @param elements the array to convert to an unmodifiable list
     * @return an unmodifiable list containing the non-null elements of the array
     * @see <a href="https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/Collection.html#unmodview">unmodifiable view</a>
     */
    static <T> List<T> arrayToUnmodifiableList(T[] elements) {
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

    /**
     * Create a message string that includes the exception name, error type, and all arguments irrespective of log
     * safety.
     *
     * @param exceptionName the name of the exception for which the message is being rendered
     * @param errorType the error type the exception represents
     * @param args the arguments to be included in the message
     * @return a message string that includes the exception name, error type, and arguments
     */
    static String renderUnsafeMessage(String exceptionName, ErrorType errorType, Arg<?>... args) {
        String message = renderNoArgsMessage(exceptionName, errorType);

        if (args == null || args.length == 0) {
            return message;
        }

        StringBuilder builder = new StringBuilder();
        builder.append(message).append(": {");
        boolean first = true;
        for (Arg<?> arg : args) {
            if (arg == null) {
                continue;
            }
            if (first) {
                first = false;
            } else {
                builder.append(", ");
            }
            builder.append(arg.getName()).append("=").append(arg.getValue());
        }
        builder.append("}");

        return builder.toString();
    }

    /**
     * Create a message string that includes the exception name and error type, but no arguments.
     *
     * @param exceptionName the name of the exception for which the message is being rendered
     * @param errorType the error type the exception represents
     * @return a message string
     */
    static String renderNoArgsMessage(String exceptionName, ErrorType errorType) {
        return exceptionName + ": " + errorType.code() + " (" + errorType.name() + ")";
    }

    /**
     * Finds the errorInstanceId of the most recent cause if present, otherwise generates a new random identifier. Note
     * that this only searches {@link Throwable#getCause() causal exceptions}, not {@link Throwable#getSuppressed()
     * suppressed causes}.
     */
    // VisibleForTesting
    static String generateErrorInstanceId(@Nullable Throwable cause) {
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
        if (cause instanceof CheckedServiceException) {
            return ((CheckedServiceException) cause).getErrorInstanceId();
        }
        if (cause instanceof RemoteException) {
            return ((RemoteException) cause).getError().errorInstanceId();
        }
        return generateErrorInstanceId(cause.getCause(), dejaVu);
    }
}
