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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.Test;

public final class ErrorTypeTest {

    @Test
    public void testNameMustBeCamelCaseWithOptionalNameSpace() throws Exception {
        assertThatThrownBy(() -> ErrorType.create(ErrorType.Code.FAILED_PRECONDITION, "foo"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageStartingWith(
                        "ErrorType names must be of the form 'UpperCamelNamespace:UpperCamelName': foo");

        String[] badNames = new String[] {":", "foo:Bar", ":Bar", "Bar:", "foo:bar", "Foo:bar"};
        for (String name : badNames) {
            assertThatThrownBy(() -> ErrorType.client(name))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageStartingWith(
                            "ErrorType names must be of the form 'UpperCamelNamespace:UpperCamelName': " + name);
        }

        String[] goodNames = new String[] {"Foo:Bar", "FooBar:Baz", "FooBar:BoomBang"};
        for (String name : goodNames) {
            ErrorType.client(name);
        }
    }

    @Test
    public void testDefaultErrorTypeHttpErrorCodes() throws Exception {
        assertThat(ErrorType.PERMISSION_DENIED.httpErrorCode()).isEqualTo(403);
        assertThat(ErrorType.INVALID_ARGUMENT.httpErrorCode()).isEqualTo(400);
        assertThat(ErrorType.NOT_FOUND.httpErrorCode()).isEqualTo(404);
        assertThat(ErrorType.FAILED_PRECONDITION.httpErrorCode()).isEqualTo(500);
        assertThat(ErrorType.INTERNAL.httpErrorCode()).isEqualTo(500);
    }

    @Test
    public void testCustomErrors() throws Exception {
        ErrorType customClient = ErrorType.client("Namespace:MyDesc");
        assertThat(customClient.code()).isEqualTo(ErrorType.Code.CUSTOM_CLIENT);
        assertThat(customClient.httpErrorCode()).isEqualTo(400);
        assertThat(customClient.name()).isEqualTo("Namespace:MyDesc");

        ErrorType customServer = ErrorType.server("Namespace:MyDesc");
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

        assertThatThrownBy(() -> ErrorType.create(ErrorType.Code.CUSTOM_CLIENT, "Namespace:MyDesc"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Use the client() or server() methods to construct ErrorTypes with code CUSTOM_CLIENT "
                        + "or CUSTOM_SERVER");
        assertThatThrownBy(() -> ErrorType.create(ErrorType.Code.CUSTOM_SERVER, "MyDesc"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Use the client() or server() methods to construct ErrorTypes with code CUSTOM_CLIENT "
                        + "or CUSTOM_SERVER");
    }
}
