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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.palantir.logsafe.Arg;
import com.palantir.logsafe.exceptions.SafeIllegalStateException;
import java.io.Serializable;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.immutables.value.Value;

/**
 * A JSON-serializable representation of an exception/error, represented by its error code, an error name identifying
 * the (specific sub-) type of error, an optional set of named parameters detailing the error condition. Intended to
 * transport errors through RPC channels such as HTTP responses.
 */
@JsonDeserialize(builder = SerializableError.Builder.class)
@JsonSerialize(as = ImmutableSerializableError.class)
@Value.Immutable
@Value.Style(visibility = Value.Style.ImplementationVisibility.PACKAGE)
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class SerializableError implements Serializable {

    /**
     * A fixed code word identifying the type of error. For errors generated from {@link ServiceException}, this
     * corresponds to the {@link ErrorType#code} and is part of the service's API surface. Clients are given access to
     * the server-side error code via {@link RemoteException#getError} and typically switch&dispatch on the error code
     * and/or name.
     */
    @JsonProperty("errorCode")
    // TODO(rfink): errorCode and exceptionClass are mutual delagates so that they can be either set independently or
    // one inherits from the other. This is quite a hack and should be removed when we remove support for the
    // exceptionClass field.
    @Value.Default
    public String errorCode() {
        return getExceptionClass().orElseThrow(() -> new SafeIllegalStateException(
                "Expected either 'errorCode' or 'exceptionClass' to be set"));
    }

    /**
     * A fixed name identifying the error. For errors generated from {@link ServiceException}, this corresponding to the
     * {@link ErrorType#name} and is part of the service's API surface. Clients are given access to the service-side
     * error name via {@link RemoteException#getError} and typically switch&dispatch on the error code and/or name.
     */
    @JsonProperty("errorName")
    // TODO(rfink): errorName and message are mutual delagates so that they can be either set independently or one
    // inherits from the other. This is quite a hack and should be removed when we remove support for the message field.
    @Value.Default
    public String errorName() {
        return getMessage().orElseThrow(() -> new SafeIllegalStateException(
                "Expected either 'errorName' or 'message' to be set"));
    }

    /**
     * A unique identifier for this error instance, typically used to correlate errors displayed in user-facing
     * applications with richer backend-level error information. In contrast to {@link #errorCode} and {@link
     * #errorName}, the {@link #errorInstanceId} identifies a specific occurrence of an error, not a class of errors. By
     * convention, this field is a UUID.
     */
    @JsonProperty("errorInstanceId")
    @Value.Default
    @SuppressWarnings("checkstyle:designforextension")
    public String errorInstanceId() {
        return "";
    }

    /** A set of parameters that further explain the error. */
    public abstract Map<String, String> parameters();

    /**
     * @deprecated Used by the serialization-mechanism for back-compat only. Do not use.
     */
    @Deprecated
    @JsonProperty(value = "exceptionClass", access = JsonProperty.Access.WRITE_ONLY)
    @SuppressWarnings("checkstyle:designforextension")
    // TODO(rfink): Remove once all error producers have switched to errorCode.
    abstract Optional<String> getExceptionClass();

    /**
     * @deprecated Used by the serialization-mechanism for back-compat only. Do not use.
     */
    @Deprecated
    @JsonProperty(value = "message", access = JsonProperty.Access.WRITE_ONLY)
    @SuppressWarnings("checkstyle:designforextension")
    // TODO(rfink): Remove once all error producers have switched to errorName.
    abstract Optional<String> getMessage();

    /**
     * Creates a {@link SerializableError} representation of this exception that derives from the error code and
     * message, as well as the {@link Arg#isSafeForLogging safe} and unsafe {@link ServiceException#args parameters}.
     */
    public static SerializableError forException(ServiceException exception) {
        Builder builder = new Builder()
                .errorCode(exception.getErrorType().code().name())
                .errorName(exception.getErrorType().name())
                .errorInstanceId(exception.getErrorInstanceId());

        for (Arg<?> arg : exception.getParameters()) {
            builder.putParameters(arg.getName(), Objects.toString(arg.getValue()));
        }

        return builder.build();
    }

    // TODO(rfink): Remove once all error producers have switched to errorCode/errorName.
    public static final class Builder extends ImmutableSerializableError.Builder {}

    public static Builder builder() {
        return new Builder();
    }
}
