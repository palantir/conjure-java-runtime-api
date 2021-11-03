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
import com.palantir.logsafe.SafeArg;
import com.palantir.logsafe.SafeLoggable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** An exception thrown by an RPC client to indicate remote/server-side failure. */
public final class RemoteException extends RuntimeException implements SafeLoggable {
    private static final long serialVersionUID = 1L;
    private static final String ERROR_INSTANCE_ID = "errorInstanceId";
    private static final String ERROR_CODE = "errorCode";
    private static final String ERROR_NAME = "errorName";

    private final String message;
    private final String stableMessage;
    private final SerializableError error;
    private final int status;
    private final List<Arg<?>> args;

    /** Returns the error thrown by a remote process which caused an RPC call to fail. */
    public SerializableError getError() {
        return error;
    }

    /** The HTTP status code of the HTTP response conveying the remote exception. */
    public int getStatus() {
        return status;
    }

    public RemoteException(SerializableError error, int status) {
        this.stableMessage = error.errorCode().equals(error.errorName())
                ? String.format("RemoteException: %s", error.errorCode())
                : String.format("RemoteException: %s (%s)", error.errorCode(), error.errorName());
        this.message = this.stableMessage + " with instance ID " + error.errorInstanceId();
        this.error = error;
        this.status = status;
        this.args = Collections.unmodifiableList(Arrays.asList(
                SafeArg.of(ERROR_INSTANCE_ID, error.errorInstanceId()),
                SafeArg.of(ERROR_NAME, error.errorName()),
                SafeArg.of(ERROR_CODE, error.errorCode())));
    }

    public RemoteException(ServiceException serviceException) {
        this(
                SerializableError.forException(serviceException),
                serviceException.getErrorType().httpErrorCode());
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public String getLogMessage() {
        return stableMessage;
    }

    @Override
    public List<Arg<?>> getArgs() {
        // RemoteException explicitly does not support arguments because they have already been recorded
        // on the service which produced the causal SerializableError.
        return args;
    }
}
