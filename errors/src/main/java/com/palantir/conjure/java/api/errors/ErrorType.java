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

import com.palantir.logsafe.SafeArg;
import com.palantir.logsafe.exceptions.SafeIllegalArgumentException;
import java.io.Serializable;
import java.util.regex.Pattern;
import org.immutables.value.Value;

/**
 * Represents errors by code and name. {@link ErrorType} instance are meant to be compile-time constants in the sense
 * that the name should not contain information that is available at runtime only. By convention, and in alignment with
 * the HTTP specification, errors associated with a {@code 4xx} HTTP status code are client errors, and errors
 * associated with a {@code 5xx} status are server errors.
 */
@Value.Immutable
@ImmutablesStyle
public abstract class ErrorType implements Serializable {

    private static final String UPPER_CAMEL_CASE = "(([A-Z][a-z0-9]+)+)";
    // UpperCamel with UpperCamel namespace prefix.
    private static final Pattern ERROR_NAME_PATTERN =
            Pattern.compile(String.format("%s:%s", UPPER_CAMEL_CASE, UPPER_CAMEL_CASE));

    public enum Code {
        UNAUTHORIZED(401),
        PERMISSION_DENIED(403),
        INVALID_ARGUMENT(400),
        NOT_FOUND(404),
        CONFLICT(409),
        REQUEST_ENTITY_TOO_LARGE(413),
        FAILED_PRECONDITION(500),
        INTERNAL(500),
        TIMEOUT(500),
        CUSTOM_CLIENT(400),
        CUSTOM_SERVER(500);

        private final Integer httpErrorCode; // boxed so that we fail loudly if someone accesses CUSTOM.httpErrorCode

        Code(Integer httpErrorCode) {
            this.httpErrorCode = httpErrorCode;
        }
    }

    public static final ErrorType UNAUTHORIZED = create(Code.UNAUTHORIZED, "Default:Unauthorized");
    public static final ErrorType PERMISSION_DENIED = create(Code.PERMISSION_DENIED, "Default:PermissionDenied");
    public static final ErrorType INVALID_ARGUMENT = create(Code.INVALID_ARGUMENT, "Default:InvalidArgument");
    public static final ErrorType REQUEST_ENTITY_TOO_LARGE =
            create(Code.REQUEST_ENTITY_TOO_LARGE, "Default:RequestEntityTooLarge");
    public static final ErrorType NOT_FOUND = create(Code.NOT_FOUND, "Default:NotFound");
    public static final ErrorType CONFLICT = create(Code.CONFLICT, "Default:Conflict");
    public static final ErrorType FAILED_PRECONDITION = create(Code.FAILED_PRECONDITION, "Default:FailedPrecondition");
    public static final ErrorType INTERNAL = create(Code.INTERNAL, "Default:Internal");
    public static final ErrorType TIMEOUT = create(Code.TIMEOUT, "Default:Timeout");

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
        if (!ERROR_NAME_PATTERN.matcher(name()).matches()) {
            throw new SafeIllegalArgumentException(
                    "ErrorType names must be of the form 'UpperCamelNamespace:UpperCamelName'",
                    SafeArg.of("name", name()));
        }
    }

    /** Constructs an {@link ErrorType} with the given error {@link Code} and name. */
    public static ErrorType create(Code code, String name) {
        return ImmutableErrorType.builder()
                .code(code)
                .name(name)
                .httpErrorCode(code.httpErrorCode)
                .build();
    }
}
