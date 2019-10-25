/*
 * (c) Copyright 2019 Palantir Technologies Inc. All rights reserved.
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
import com.palantir.logsafe.UnsafeArg;

/**
 * Static utility to propagate instances of {@link RemoteException} as {@link ServiceException}.
 */
public final class ConjureThrowables {
    private ConjureThrowables() {}

    /**
     * Re-throws {@code remoteException} as instance of {@link ServiceException} iff it matches {@code errorType}.
     * Parameters and errorInstanceId of the {@code remoteException} are preserved.
     *
     * <pre>
     * try {
     *     service.someMethod();
     * } catch (RemoteException e) {
     *     ConjureThrowables.propagateIfErrorTypeEquals(e, ServiceErrors.REMOTE_ERROR_TYPE);
     *     throw e;
     * }
     * </pre>
     */
    public static void propagateIfErrorTypeEquals(RemoteException remoteException, ErrorType errorType) {
        if (hasErrorType(remoteException, errorType)) {
            throw new ServiceException(errorType, remoteException, argsFromRemoteException(remoteException));
        }
    }

    private static ErrorType getErrorTypeFromRemoteException(RemoteException remoteException) {
        ErrorType.Code errorCode = ErrorType.Code.valueOf(remoteException.getError().errorCode());
        return ImmutableErrorType.builder()
                .code(errorCode)
                .name(remoteException.getError().errorName())
                .build();
    }

    private static boolean hasErrorType(RemoteException remoteException, ErrorType errorType) {
        try {
            ErrorType remoteErrorType = getErrorTypeFromRemoteException(remoteException);
            return remoteErrorType.equals(errorType);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static Arg<?>[] argsFromRemoteException(RemoteException remoteException) {
        return remoteException.getError()
                .parameters()
                .entrySet()
                .stream()
                .map(entry -> UnsafeArg.of(entry.getKey(), entry.getValue()))
                .toArray(Arg<?>[]::new);
    }
}
