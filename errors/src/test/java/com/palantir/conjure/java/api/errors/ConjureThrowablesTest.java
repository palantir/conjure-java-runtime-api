/*
 * (c) Copyright 2019 Palantir Technologies Inc. All rights reserved.
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

import static com.palantir.conjure.java.api.testing.Assertions.assertThatServiceExceptionThrownBy;
import static org.assertj.core.api.Assertions.assertThatCode;

import com.palantir.logsafe.UnsafeArg;
import org.junit.jupiter.api.Test;

public class ConjureThrowablesTest {

    private static final ErrorType ERROR_TYPE = ErrorType.create(ErrorType.Code.CUSTOM_CLIENT, "Namespace:MyDesc");
    private static final String ERROR_INSTANCE_ID = "85afe977";

    @Test
    public void testThrowsIfErrorTypeMatches() {
        SerializableError error = new SerializableError.Builder()
                .errorName("Namespace:MyDesc")
                .errorCode("CUSTOM_CLIENT")
                .errorInstanceId(ERROR_INSTANCE_ID)
                .putParameters("arg", "value")
                .build();
        RemoteException remoteException = new RemoteException(error, 400);

        assertThatServiceExceptionThrownBy(
                () -> ConjureThrowables.propagateIfErrorTypeEquals(remoteException, ERROR_TYPE))
                        .hasType(ERROR_TYPE)
                        .hasErrorInstanceId(ERROR_INSTANCE_ID)
                        .hasArgs(UnsafeArg.of("arg", "value"));
    }

    @Test
    public void testThrowsIfDefaultErrorTypeMatches() {
        SerializableError error = new SerializableError.Builder()
                .errorName("Default:InvalidArgument")
                .errorCode("INVALID_ARGUMENT")
                .build();
        RemoteException remoteException = new RemoteException(error, 400);

        assertThatServiceExceptionThrownBy(
                () -> ConjureThrowables.propagateIfErrorTypeEquals(remoteException, ErrorType.INVALID_ARGUMENT))
                        .hasType(ErrorType.INVALID_ARGUMENT);
    }

    @Test
    public void testDoesntThrowIfErrorTypeMismatch() {
        SerializableError error = new SerializableError.Builder()
                .errorName("Namespace:MyDesc")
                .errorCode("CUSTOM_CLIENT")
                .build();
        RemoteException remoteException = new RemoteException(error, 400);

        assertThatCode(
                () -> ConjureThrowables.propagateIfErrorTypeEquals(remoteException, ErrorType.INVALID_ARGUMENT))
                        .doesNotThrowAnyException();
    }

    @Test
    public void testDoesntThrowIfErrorTypeInvalid() {
        SerializableError error = new SerializableError.Builder()
                .errorName("Invalid")
                .errorCode("INVALID")
                .build();
        RemoteException remoteException = new RemoteException(error, 400);

        assertThatCode(
                () -> ConjureThrowables.propagateIfErrorTypeEquals(remoteException, ErrorType.INVALID_ARGUMENT))
                        .doesNotThrowAnyException();
    }
}
