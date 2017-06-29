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
 * Represents errors by code and description. {@link ErrorType} instance are meant to be compile-time constants in
 * the sense that the description should not contain information that is available at runtime only.
 */
@Value.Immutable
@ImmutablesStyle
public abstract class ErrorType {

    public enum Code {
        UNKNOWN(500),
        PERMISSION_DENIED(403),
        INVALID_ARGUMENT(400),
        FAILED_PRECONDITION(400),
        INTERNAL(500),
        CUSTOM(null /* unused */);

        private final Integer httpErrorCode; // boxed so that we fail loudly if someone accesses CUSTOM.httpErrorCode

        Code(Integer httpErrorCode) {
            this.httpErrorCode = httpErrorCode;
        }
    }

    public static final ErrorType UNKNOWN = create(Code.UNKNOWN);
    public static final ErrorType PERMISSION_DENIED = create(Code.PERMISSION_DENIED);
    public static final ErrorType INVALID_ARGUMENT = create(Code.INVALID_ARGUMENT);
    public static final ErrorType FAILED_PRECONDITION = create(Code.FAILED_PRECONDITION);
    public static final ErrorType INTERNAL = create(Code.INTERNAL);

    /** The {@link Code} of this error. */
    public abstract Code code();

    /**
     * The description of this error; for standard errors defined as constants in this class (e.g., {@link
     * #PERMISSION_DENIED}, {@link #INVALID_ARGUMENT}, etc), the description is identical to the error {@link #code},
     * while for {@link Code#CUSTOM} exceptions the description can differ from the {@link #code} in order to allow
     * error producers to provide additional, application-specific context on the nature of the error.
     */
    public abstract String description();

    /** The HTTP error code used to convey this error to HTTP clients. */
    public abstract int httpErrorCode();

    /**
     * Creates a new error type with the given description and HTTP error code, and error type{@link Code#CUSTOM}.
     * Allowed error codes are {@code 400 BAD REQUEST} and {@code 500 INTERNAL SERVER ERROR}.
     */
    public static ErrorType custom(String description, int httpErrorCode) {
        if (httpErrorCode != 400 && httpErrorCode != 500) {
            throw new IllegalArgumentException("CUSTOM ErrorTypes must have HTTP error code 400 or 500");
        }
        return ImmutableErrorType.builder()
                .code(Code.CUSTOM)
                .description(description)
                .httpErrorCode(httpErrorCode)
                .build();
    }

    /**
     * Constructs an {@link ErrorType} with the given error {@link Code} and description. Cannot use the {@link
     * Code#CUSTOM} error code, see {@link #custom} instead.
     */
    public static ErrorType of(Code code, String description) {
        if (code == Code.CUSTOM) {
            throw new IllegalArgumentException("Use the custom() method to construct ErrorTypes with code CUSTOM");
        }
        return ImmutableErrorType.builder()
                .code(code)
                .description(description)
                .httpErrorCode(code.httpErrorCode)
                .build();
    }

    private static ErrorType create(Code code) {
        return ImmutableErrorType.builder()
                .code(code)
                .description(code.name())
                .httpErrorCode(code.httpErrorCode)
                .build();
    }
}
