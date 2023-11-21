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

package com.palantir.conjure.java.api.testing;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.palantir.conjure.java.api.errors.ErrorType;
import com.palantir.conjure.java.api.errors.RemoteException;
import com.palantir.conjure.java.api.errors.SerializableError;
import com.palantir.conjure.java.api.errors.ServiceException;
import org.junit.jupiter.api.Test;

public final class RemoteExceptionAssertTest {

    @Test
    public void testSanity() {
        ErrorType actualType = ErrorType.FAILED_PRECONDITION;
        SerializableError error = SerializableError.forException(new ServiceException(actualType));

        Assertions.assertThat(new RemoteException(error, actualType.httpErrorCode()))
                .isGeneratedFromErrorType(actualType);

        assertThatThrownBy(() -> Assertions.assertThat(new RemoteException(error, actualType.httpErrorCode() + 1))
                        .isGeneratedFromErrorType(actualType))
                .isInstanceOf(AssertionError.class)
                .hasMessageContaining(
                        "Expected error status to be %s, but found %s",
                        actualType.httpErrorCode(), actualType.httpErrorCode() + 1)
                // Make sure the error type was captured.
                .hasMessageContaining("FAILED_PRECONDITION")
                // Make sure the instance ID was captured.
                .hasMessageContaining("with instance ID");
    }
}
