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

import com.palantir.logsafe.Arg;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;

final class ServiceExceptionUtils {
    private ServiceExceptionUtils() {}

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

    static String renderUnsafeMessage(ErrorType errorType, Class<?> clazz, Arg<?>... args) {
        String message = renderNoArgsMessage(errorType, clazz);

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

    static String renderNoArgsMessage(ErrorType errorType, Class<?> clazz) {
        return String.format("%s: %s (%s)", clazz.getSimpleName(), errorType.code(), errorType.name());
    }

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
            return UUID.randomUUID().toString();
        }
        if (cause instanceof ServiceException) {
            return ((ServiceException) cause).getErrorInstanceId();
        }
        if (cause instanceof RemoteException) {
            return ((RemoteException) cause).getError().errorInstanceId();
        }
        if (cause instanceof CheckedServiceException) {
            return ((CheckedServiceException) cause).getErrorInstanceId();
        }
        if (cause instanceof CheckedRemoteException) {
            return ((CheckedRemoteException) cause).getError().errorInstanceId();
        }
        return generateErrorInstanceId(cause.getCause(), dejaVu);
    }
}
