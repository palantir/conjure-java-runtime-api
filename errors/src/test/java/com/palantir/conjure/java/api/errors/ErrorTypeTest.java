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

import static com.palantir.logsafe.testing.Assertions.assertThatLoggableExceptionThrownBy;
import static org.assertj.core.api.Assertions.assertThat;

import com.palantir.conjure.java.api.errors.ErrorType.Code;
import com.palantir.logsafe.SafeArg;
import com.palantir.logsafe.exceptions.SafeIllegalArgumentException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

public final class ErrorTypeTest {

    @Test
    public void testNameMustBeCamelCaseWithOptionalNameSpace() throws Exception {
        String[] badNames = new String[] {":", "foo:Bar", ":Bar", "Bar:", "foo:bar", "Foo:bar", "Foo:2Bar"};
        for (String name : badNames) {
            assertThatLoggableExceptionThrownBy(() -> ErrorType.create(Code.CUSTOM_CLIENT, name))
                    .isInstanceOf(SafeIllegalArgumentException.class)
                    .hasLogMessage("ErrorType names must be of the form 'UpperCamelNamespace:UpperCamelName'")
                    .hasExactlyArgs(SafeArg.of("name", name));
            assertThatLoggableExceptionThrownBy(() -> ErrorType.create(ErrorType.Code.CUSTOM_SERVER, name))
                    .isInstanceOf(SafeIllegalArgumentException.class)
                    .hasLogMessage("ErrorType names must be of the form 'UpperCamelNamespace:UpperCamelName'")
                    .hasExactlyArgs(SafeArg.of("name", name));
            assertThatLoggableExceptionThrownBy(() -> ErrorType.create(ErrorType.Code.FAILED_PRECONDITION, name))
                    .isInstanceOf(SafeIllegalArgumentException.class)
                    .hasLogMessage("ErrorType names must be of the form 'UpperCamelNamespace:UpperCamelName'")
                    .hasExactlyArgs(SafeArg.of("name", name));
        }

        String[] goodNames = new String[] {"Foo:Bar", "FooBar:Baz", "FooBar:BoomBang", "Foo:Bar2Baz3"};
        for (String name : goodNames) {
            ErrorType.create(ErrorType.Code.CUSTOM_CLIENT, name);
            ErrorType.create(ErrorType.Code.CUSTOM_SERVER, name);
            ErrorType.create(ErrorType.Code.INVALID_ARGUMENT, name);
        }
    }

    @Test
    public void testDefaultErrorTypeHttpErrorCodes() throws Exception {
        assertThat(ErrorType.UNAUTHORIZED.httpErrorCode()).isEqualTo(401);
        assertThat(ErrorType.PERMISSION_DENIED.httpErrorCode()).isEqualTo(403);
        assertThat(ErrorType.INVALID_ARGUMENT.httpErrorCode()).isEqualTo(400);
        assertThat(ErrorType.NOT_FOUND.httpErrorCode()).isEqualTo(404);
        assertThat(ErrorType.CONFLICT.httpErrorCode()).isEqualTo(409);
        assertThat(ErrorType.REQUEST_ENTITY_TOO_LARGE.httpErrorCode()).isEqualTo(413);
        assertThat(ErrorType.FAILED_PRECONDITION.httpErrorCode()).isEqualTo(500);
        assertThat(ErrorType.INTERNAL.httpErrorCode()).isEqualTo(500);
    }

    @Test
    public void testCanCreateCustomClientAndServerErrors() throws Exception {
        ErrorType customClient = ErrorType.create(ErrorType.Code.CUSTOM_CLIENT, "Namespace:MyDesc");
        assertThat(customClient.code()).isEqualTo(ErrorType.Code.CUSTOM_CLIENT);
        assertThat(customClient.httpErrorCode()).isEqualTo(400);
        assertThat(customClient.name()).isEqualTo("Namespace:MyDesc");

        ErrorType customServer = ErrorType.create(ErrorType.Code.CUSTOM_SERVER, "Namespace:MyDesc");
        assertThat(customServer.code()).isEqualTo(ErrorType.Code.CUSTOM_SERVER);
        assertThat(customServer.httpErrorCode()).isEqualTo(500);
        assertThat(customServer.name()).isEqualTo("Namespace:MyDesc");
    }

    @Test
    public void testCanCreateNewErrorTypes() throws Exception {
        ErrorType error = ErrorType.create(ErrorType.Code.FAILED_PRECONDITION, "Namespace:MyDesc");
        assertThat(error.code()).isEqualTo(ErrorType.Code.FAILED_PRECONDITION);
        assertThat(error.httpErrorCode()).isEqualTo(500);
        assertThat(error.name()).isEqualTo("Namespace:MyDesc");
    }

    @Nested
    class Propagation {
        @Test
        void testCreatedErrorTypesDoNotPropagate() {
            assertThat(ErrorType.create(Code.CUSTOM_CLIENT, "Namespace:MyDesc").propagates())
                    .isFalse();
            assertThat(ErrorType.PERMISSION_DENIED.propagates()).isFalse();
        }

        @Test
        void testCreatePropagating() {
            ErrorType error = ErrorType.createPropagating(Code.CUSTOM_CLIENT, "Namespace:MyDesc");
            assertThat(error.propagates()).isTrue();
            assertThat(error.code()).isEqualTo(Code.CUSTOM_CLIENT);
            assertThat(error.name()).isEqualTo("Namespace:MyDesc");
            assertThat(error.httpErrorCode()).isEqualTo(400);
            assertThat(error)
                    .as("propagation is part of the error type")
                    .isNotEqualTo(ErrorType.create(Code.CUSTOM_CLIENT, "Namespace:MyDesc"));
        }

        @ParameterizedTest
        @EnumSource(
                value = Code.class,
                names = {
                    "UNAUTHORIZED",
                    "PERMISSION_DENIED",
                    "INVALID_ARGUMENT",
                    "NOT_FOUND",
                    "CONFLICT",
                    "REQUEST_ENTITY_TOO_LARGE",
                    "CUSTOM_CLIENT"
                })
        void testClientErrorsCanPropagate(Code code) {
            assertThat(ErrorType.createPropagating(code, "Namespace:MyDesc").propagates())
                    .isTrue();
        }

        @ParameterizedTest
        @EnumSource(
                value = Code.class,
                names = {"FAILED_PRECONDITION", "INTERNAL", "TIMEOUT", "CUSTOM_SERVER"})
        void testServerErrorsCannotPropagate(Code code) {
            assertThatLoggableExceptionThrownBy(() -> ErrorType.createPropagating(code, "Namespace:MyDesc"))
                    .isInstanceOf(SafeIllegalArgumentException.class)
                    .hasLogMessage("Only client errors (4xx) can propagate")
                    .hasExactlyArgs(SafeArg.of("name", "Namespace:MyDesc"), SafeArg.of("code", code));
        }
    }
}
