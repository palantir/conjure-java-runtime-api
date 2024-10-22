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
import com.palantir.logsafe.SafeArg;
import com.palantir.logsafe.SafeLoggable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

// TODO(pm): Move common methods into utils class.
public final class CheckedRemoteException extends Exception implements SafeLoggable {
    // TODO(pm): what is the purpose of serialVersionUID?

    private static final String ERROR_INSTANCE_ID = "errorInstanceId";
    private static final String ERROR_CODE = "errorCode";
    private static final String ERROR_NAME = "errorName";

    private final String stableMessage;
    private final SerializableConjureDefinedError error;
    private final int status;
    private final List<Arg<?>> args;
    // Lazily evaluated based on the stableMessage, errorInstanceId, and args.
    @SuppressWarnings("MutableException")
    private String unsafeMessage;

    /** Returns the error thrown by a remote process which caused an RPC call to fail. */
    public SerializableConjureDefinedError getError() {
        return error;
    }

    /** The HTTP status code of the HTTP response conveying the remote exception. */
    public int getStatus() {
        return status;
    }

    public CheckedRemoteException(SerializableConjureDefinedError error, int status) {
        this.stableMessage = error.errorCode().equals(error.errorName())
                ? "CheckedRemoteException: " + error.errorCode()
                : "CheckedRemoteException: " + error.errorCode() + " (" + error.errorName() + ")";
        this.error = error;
        this.status = status;
        this.args = Collections.unmodifiableList(Arrays.asList(
                SafeArg.of(ERROR_INSTANCE_ID, error.errorInstanceId()),
                SafeArg.of(ERROR_NAME, error.errorName()),
                SafeArg.of(ERROR_CODE, error.errorCode())));
    }

    @Override
    public String getMessage() {
        // This field is not used in most environments so the cost of computation may be avoided.
        String messageValue = unsafeMessage;
        if (messageValue == null) {
            messageValue = renderUnsafeMessage();
            unsafeMessage = messageValue;
        }
        return messageValue;
    }

    private String renderUnsafeMessage() {
        StringBuilder builder = new StringBuilder()
                .append(stableMessage)
                .append(" with instance ID ")
                .append(error.errorInstanceId());
        if (!error.parameters().isEmpty()) {
            builder.append(": {");
            error.parameters().forEach(errorParameter -> builder.append(errorParameter.name())
                    .append('=')
                    .append(errorParameter.serializedValue())
                    .append(", "));
            // remove the trailing space
            builder.setLength(builder.length() - 1);
            // replace the trailing comma with a close curly brace
            builder.setCharAt(builder.length() - 1, '}');
        }
        return builder.toString();
    }

    @Override
    public String getLogMessage() {
        return stableMessage;
    }

    @Override
    public List<Arg<?>> getArgs() {
        // CheckedRemoteException explicitly does not support arguments because they have already been recorded
        // on the service which produced the causal SerializableError.
        return args;
    }
}
