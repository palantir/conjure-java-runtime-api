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
import com.palantir.logsafe.UnsafeArg;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * An exception thrown by an RPC client to indicate remote/server-side failure.
 */
public final class RemoteException extends RuntimeException implements SafeLoggable {
    private static final long serialVersionUID = 1L;

    private final SerializableError error;
    private final int status;

    private final String noArgsMessage;
    private final List<Arg<?>> args; // unmodifiable

    public RemoteException(SerializableError error, int status) {
        super(renderUnsafeMessage(error));

        this.error = error;
        this.status = status;
        this.noArgsMessage = renderNoArgsMessage(error);
        this.args = copyArgsToUnmodifiableList(error);
    }

    /** Returns the error thrown by a remote process which caused an RPC call to fail. */
    public SerializableError getError() {
        return error;
    }

    /** The HTTP status code of the HTTP response conveying the remote exception. */
    public int getStatus() {
        return status;
    }

    @Override
    public String getLogMessage() {
        return noArgsMessage;
    }

    @Override
    public List<Arg<?>> getArgs() {
        return args;
    }

    private static List<Arg<?>> copyArgsToUnmodifiableList(SerializableError error) {
        Map<String, String> parameters = error.parameters();

        List<Arg<?>> argsList = new ArrayList<>(parameters.size());
        for (Entry<String, String> parameter : parameters.entrySet()) {
            argsList.add(UnsafeArg.of(parameter.getKey(), parameter.getValue()));
        }
        return Collections.unmodifiableList(argsList);
    }

    private static String renderUnsafeMessage(SerializableError error) {
        String message = renderNoArgsMessage(error);
        Map<String, String> parameters = error.parameters();

        if (parameters.size() == 0) {
            return message;
        }

        StringBuilder builder = new StringBuilder();
        builder.append(message).append(": {");
        boolean first = true;
        for (Entry<String, String> parameter : parameters.entrySet()) {
            if (first) {
                first = false;
            } else {
                builder.append(", ");
            }
            builder.append(parameter.getKey()).append("=").append(parameter.getValue());
        }
        builder.append("}");

        return builder.toString();
    }

    private static String renderNoArgsMessage(SerializableError error) {
        return error.errorCode().equals(error.errorName())
                ? String.format("RemoteException: %s with instance ID %s", error.errorCode(), error.errorInstanceId())
                : String.format("RemoteException: %s (%s) with instance ID %s",
                        error.errorCode(), error.errorName(), error.errorInstanceId());
    }
}
