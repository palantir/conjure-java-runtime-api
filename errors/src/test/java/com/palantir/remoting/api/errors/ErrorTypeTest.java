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
    public void testDefaultErrorTypeHttpErrorCodes() throws Exception {
        assertThat(ErrorType.UNKNOWN.httpErrorCode()).isEqualTo(500);
        assertThat(ErrorType.PERMISSION_DENIED.httpErrorCode()).isEqualTo(403);
        assertThat(ErrorType.INVALID_ARGUMENT.httpErrorCode()).isEqualTo(400);
        assertThat(ErrorType.FAILED_PRECONDITION.httpErrorCode()).isEqualTo(400);
        assertThat(ErrorType.INTERNAL.httpErrorCode()).isEqualTo(500);
    }

    @Test
    public void testCustomErrors() throws Exception {
        ErrorType custom400 = ErrorType.custom("myDesc", 400);
        assertThat(custom400.code()).isEqualTo(ErrorType.Code.CUSTOM);
        assertThat(custom400.httpErrorCode()).isEqualTo(400);
        assertThat(custom400.name()).isEqualTo("myDesc");

        ErrorType custom500 = ErrorType.custom("myDesc", 500);
        assertThat(custom500.code()).isEqualTo(ErrorType.Code.CUSTOM);
        assertThat(custom500.httpErrorCode()).isEqualTo(500);
        assertThat(custom500.name()).isEqualTo("myDesc");

        assertThatThrownBy(() -> ErrorType.custom("myDesc", 403))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("CUSTOM ErrorTypes must have HTTP error code 400 or 500");
    }

    @Test
    public void testCanCreateNewErrorTypes() throws Exception {
        ErrorType error = ErrorType.of(ErrorType.Code.FAILED_PRECONDITION, "myDesc");
        assertThat(error.code()).isEqualTo(ErrorType.Code.FAILED_PRECONDITION);
        assertThat(error.httpErrorCode()).isEqualTo(400);
        assertThat(error.name()).isEqualTo("myDesc");

        assertThatThrownBy(() -> ErrorType.of(ErrorType.Code.CUSTOM, "myDesc"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Use the custom() method to construct ErrorTypes with code CUSTOM");
    }
}
