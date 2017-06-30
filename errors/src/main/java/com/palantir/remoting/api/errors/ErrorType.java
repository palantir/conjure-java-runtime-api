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

import java.util.regex.Pattern;
import org.immutables.value.Value;

/**
 * Represents errors by code and name. {@link ErrorType} instance are meant to be compile-time constants in
 * the sense that the name should not contain information that is available at runtime only.
 */
@Value.Immutable
@ImmutablesStyle
public abstract class ErrorType {

    private static final Pattern UPPER_CAMEL_CASE = Pattern.compile("([A-Z][a-z]+)+");

    public enum Code {
        UNKNOWN(500),
        PERMISSION_DENIED(403),
        CLIENT_ERROR_INVALID_ARGUMENT(400),
        SERVER_ERROR_FAILED_PRECONDITION(500),
        INTERNAL(500),
        CUSTOM(null /* unused */);

        private final Integer httpErrorCode; // boxed so that we fail loudly if someone accesses CUSTOM.httpErrorCode

        Code(Integer httpErrorCode) {
            this.httpErrorCode = httpErrorCode;
        }
    }

    public static final ErrorType UNKNOWN = createInternal(Code.UNKNOWN, "Unknown");
    public static final ErrorType PERMISSION_DENIED = createInternal(Code.PERMISSION_DENIED, "PermissionDenied");
    public static final ErrorType CLIENT_ERROR_INVALID_ARGUMENT =
            createInternal(Code.CLIENT_ERROR_INVALID_ARGUMENT, "ClientErrorInvalidArgument");
    public static final ErrorType SERVER_ERROR_FAILED_PRECONDITION =
            createInternal(Code.SERVER_ERROR_FAILED_PRECONDITION, "ServerErrorFailedPrecondition");
    public static final ErrorType INTERNAL = createInternal(Code.INTERNAL, "Internal");

    /** The {@link Code} of this error. */
    public abstract Code code();

    /**
     * The name of this error type. Names should be compile-time constants and are considered part of the API of a
     * service that produces this error.
     */
    public abstract String name();

    /** The HTTP error code used to convey this error to HTTP clients. */
    public abstract int httpErrorCode();

    @Value.Check
    final void check() {
        if (!UPPER_CAMEL_CASE.matcher(name()).matches()) {
            throw new IllegalArgumentException("ErrorType names must be UpperCamelCase: " + name());
        }
    }

    /**
     * Creates a new error type with the given name and HTTP error code, and error type{@link Code#CUSTOM}.
     * Allowed error codes are {@code 400 BAD REQUEST} and {@code 500 INTERNAL SERVER ERROR}.
     */
    public static ErrorType custom(String name, int httpErrorCode) {
        if (httpErrorCode != 400 && httpErrorCode != 500) {
            throw new IllegalArgumentException("CUSTOM ErrorTypes must have HTTP error code 400 or 500");
        }
        return ImmutableErrorType.builder()
                .code(Code.CUSTOM)
                .name(name)
                .httpErrorCode(httpErrorCode)
                .build();
    }

    /**
     * Constructs an {@link ErrorType} with the given error {@link Code} and name. Cannot use the {@link
     * Code#CUSTOM} error code, see {@link #custom} instead.
     */
    public static ErrorType of(Code code, String name) {
        if (code == Code.CUSTOM) {
            throw new IllegalArgumentException("Use the custom() method to construct ErrorTypes with code CUSTOM");
        }
        return createInternal(code, name);
    }

    private static ErrorType createInternal(Code code, String name) {
        return ImmutableErrorType.builder()
                .code(code)
                .name(name)
                .httpErrorCode(code.httpErrorCode)
                .build();
    }
}
