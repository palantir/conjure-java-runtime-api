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

import org.immutables.value.Value;

/**
 * Represents errors by name and description. {@link ErrorType} instance are meant to be compile-time constants in
 * the sense that the description should not contain information that is available at runtime only.
 */
@Value.Immutable
@ImmutablesStyle
public abstract class ErrorType {

    public enum Name {
        UNKNOWN,
        PERMISSION_DENIED,
        INVALID_ARGUMENT,
        FAILED_PRECONDITION,
        CUSTOM,
    }

    public static final ErrorType UNKNOWN = create(Name.UNKNOWN, 500);
    public static final ErrorType PERMISSION_DENIED = create(Name.PERMISSION_DENIED, 403);
    public static final ErrorType INVALID_ARGUMENT = create(Name.INVALID_ARGUMENT, 400);
    public static final ErrorType FAILED_PRECONDITION = create(Name.FAILED_PRECONDITION, 400);

    /** The {@link Name} of this error, one of a fixed number of constant names. */
    public abstract Name name();

    /**
     * The description of this error; for standard errors defined as constants in this class (e.g., {@link
     * #PERMISSION_DENIED}, {@link #INVALID_ARGUMENT}, etc), the description is identical to the error {@link #name},
     * while for {@link Name#CUSTOM} exceptions the description can differ from the {@link #name} in order to allow
     * error producers to provide additional, application-specific context on the nature of the error.
     */
    public abstract String description();

    /** The HTTP error code used to convey this error to HTTP clients. */
    public abstract int httpErrorCode();

    /**
     * Creates a new error type with the given description and HTTP error code. Allowed error codes are {@code 400 BAD
     * REQUEST} and {@code 500 INTERNAL SERVER ERROR}.
     */
    public static ErrorType custom(String description, int httpErrorCode) {
        if (httpErrorCode != 400 && httpErrorCode != 500) {
            throw new IllegalArgumentException("Custom ErrorTypes must have HTTP error code 400 or 500");
        }
        return create(Name.CUSTOM, description, httpErrorCode);
    }

    private static ErrorType create(Name name, int httpErrorCode) {
        return create(name, name.name(), httpErrorCode);
    }

    private static ErrorType create(Name name, String description, int httpErrorCode) {
        return ImmutableErrorType.builder()
                .name(name)
                .description(description)
                .httpErrorCode(httpErrorCode)
                .build();
    }
}
