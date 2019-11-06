/*
 * (c) Copyright 2018 Palantir Technologies Inc. All rights reserved.
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

/** An exception thrown by an RPC client to indicate remote/server-side failure from a non-remoting server. */
public final class UnknownRemoteException extends RuntimeException implements SafeLoggable {
    private static final long serialVersionUID = 1L;

    private final int status;
    private final String body;

    /** The HTTP status code of the HTTP response conveying the error. */
    public int getStatus() {
        return status;
    }

    /** Returns the body of the error response. */
    public String getBody() {
        return body;
    }

    public UnknownRemoteException(int status, String body) {
        super(String.format("Error %s. (Failed to parse response body as SerializableError.)", status));
        this.status = status;
        this.body = body;
    }

    @Override
    public String getLogMessage() {
        return "Failed to parse response body as SerializableError.";
    }

    @Override
    public List<Arg<?>> getArgs() {
        return Collections.singletonList(SafeArg.of("status", getStatus()));
    }
}
