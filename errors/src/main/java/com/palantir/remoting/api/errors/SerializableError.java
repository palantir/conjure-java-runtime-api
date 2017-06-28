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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.palantir.logsafe.Arg;
import java.io.Serializable;
import java.util.Map;
import org.immutables.value.Value;

/**
 * A JSON-serializable representation of an exception/error, represented by its exception message, an error name
 * identifying the type of error, an optional set of named parameters detailing the error condition. Intended to
 * transport errors through RPC channels such as HTTP responses.
 */
@JsonDeserialize(builder = SerializableError.Builder.class)
@JsonSerialize(as = ImmutableSerializableError.class)
@Value.Immutable
@Value.Style(visibility = Value.Style.ImplementationVisibility.PACKAGE)
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class SerializableError implements Serializable {

    /**
     * A name identifying the type of error; this is typically the fully-qualified name of a server-side exception or
     * some other application-defined string identifying the error. Clients are given access to the server-side error
     * name via {@link RemoteException#getError} and typically switch&dispatch on the error name.
     */
    @JsonProperty("errorName")
    public abstract String getErrorName();

    /** A description of the error. */
    public abstract String getMessage();

    /** A set of parameters that further explain the error. */
    public abstract Map<String, String> parameters();

    // TODO(rfink): Remove once all error producers have switched to errorName.
    @Value.Derived
    @JsonProperty("exceptionClass")
    @SuppressWarnings("checkstyle:designforextension")
    public String getExceptionClass() {
        return getErrorName();
    }

    /**
     * Creates a {@link SerializableError} representation of this exception that derives from the error name and
     * message, as well as the {@link Arg#isSafeForLogging safe} {@link ServiceException#args parameters}.
     */
    public static SerializableError forException(ServiceException exception) {
        Builder builder = new Builder()
                .errorName(exception.getErrorType().name().name())
                .message(exception.getErrorType().description() + " (ErrorId " + exception.getErrorId() + ")");
        for (Arg<?> arg : exception.getArgs()) {
            if (arg.isSafeForLogging()) {
                builder.putParameters(arg.getName(), arg.getValue().toString());
            }
        }

        return builder.build();
    }

    // TODO(rfink): Remove once all error producers have switched to errorName.
    public static final class Builder extends ImmutableSerializableError.Builder {
        @JsonProperty("exceptionClass")
        Builder exceptionClass(String exceptionClass) {
            return errorName(exceptionClass);
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}
