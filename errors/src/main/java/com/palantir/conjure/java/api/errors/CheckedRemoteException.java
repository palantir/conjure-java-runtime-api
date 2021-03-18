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
import java.util.Collections;
import java.util.List;

/**
 * A checked exception thrown by an RPC client to indicate remote/server-side failure that should be handled by the
 * client.
 *
 * This is a copy of {@link RemoteException}, except it extends {@link Exception}.
 **/
public abstract class CheckedRemoteException extends Exception implements SafeLoggable {
    private static final long serialVersionUID = 1L;

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

    protected CheckedRemoteException(SerializableError error, int status) {
        this.stableMessage = error.errorCode().equals(error.errorName())
                ? String.format("%s: %s", this.getClass().getSimpleName(), error.errorCode())
                : String.format("%s: %s (%s)", this.getClass().getSimpleName(), error.errorCode(), error.errorName());
        this.message = this.stableMessage + " with instance ID " + error.errorInstanceId();
        this.error = error;
        this.status = status;
        this.args = Collections.singletonList(SafeArg.of("errorInstanceId", error.errorInstanceId()));
    }

    @Override
    public final String getMessage() {
        return message;
    }

    @Override
    public final String getLogMessage() {
        return stableMessage;
    }

    @Override
    public final List<Arg<?>> getArgs() {
        // RemoteException explicitly does not support arguments because they have already been recorded
        // on the service which produced the causal SerializableError.
        return args;
    }
}
