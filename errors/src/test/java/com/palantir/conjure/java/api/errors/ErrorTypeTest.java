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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

public final class ErrorTypeTest {

    @Test
    public void testNameMustBeCamelCaseWithOptionalNameSpace() throws Exception {
        String[] badNames = new String[] {":", "foo:Bar", ":Bar", "Bar:", "foo:bar", "Foo:bar", "Foo:2Bar"};
        for (String name : badNames) {
            assertThatThrownBy(() -> ErrorType.create(ErrorType.Code.CUSTOM_CLIENT, name))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("ErrorType names must be of the form 'UpperCamelNamespace:UpperCamelName': %s", name);
            assertThatThrownBy(() -> ErrorType.create(ErrorType.Code.CUSTOM_SERVER, name))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("ErrorType names must be of the form 'UpperCamelNamespace:UpperCamelName': %s", name);
            assertThatThrownBy(() -> ErrorType.create(ErrorType.Code.FAILED_PRECONDITION, name))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("ErrorType names must be of the form 'UpperCamelNamespace:UpperCamelName': %s", name);
        }

        String[] goodNames = new String[] {"Foo:Bar", "FooBar:Baz", "FooBar:BoomBang", "Foo:Bar2Baz3"};
        for (String name : goodNames) {
            ErrorType.create(ErrorType.Code.CUSTOM_CLIENT, name);
            ErrorType.create(ErrorType.Code.CUSTOM_SERVER, name);
            ErrorType.create(ErrorType.Code.INVALID_ARGUMENT, name);
        }
    }

    @Test
    public void testNamespaceMustNotBeDefault() throws Exception {
        assertThatThrownBy(() -> ErrorType.create(ErrorType.Code.CUSTOM_SERVER, "Default:Foo"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Namespace must not be 'Default' in ErrorType name: Default:Foo");
        assertThatThrownBy(() -> ErrorType.create(ErrorType.Code.CUSTOM_SERVER, "Default:Foo"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Namespace must not be 'Default' in ErrorType name: Default:Foo");
        assertThatThrownBy(() -> ErrorType.create(ErrorType.Code.INVALID_ARGUMENT, "Default:Foo"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Namespace must not be 'Default' in ErrorType name: Default:Foo");
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
}
