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

/**
 * An exception thrown by an RPC client to indicate remote/server-side failure.
 */
public final class RemoteException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    private final SerializableError error;
    private final int status;

    /** Returns the error thrown by a remote process which caused an RPC call to fail. */
    public SerializableError getError() {
        return error;
    }

    /** The HTTP status code of the HTTP response conveying the remote exception. */
    public int getStatus() {
        return status;
    }

    public RemoteException(SerializableError error, int status) {
        super(error.errorCode().equals(error.errorName())
                ? String.format("RemoteException: %s with instance ID %s", error.errorCode(), error.errorInstanceId())
                : String.format("RemoteException: %s (%s) with instance ID %s",
                        error.errorCode(), error.errorName(), error.errorInstanceId()));

        this.error = error;
        this.status = status;
    }
}
