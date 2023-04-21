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

import static com.palantir.conjure.java.api.testing.Assertions.assertThatQosExceptionThrownBy;
import static com.palantir.conjure.java.api.testing.Assertions.assertThatRemoteExceptionThrownBy;
import static com.palantir.conjure.java.api.testing.Assertions.assertThatServiceExceptionThrownBy;
import static com.palantir.conjure.java.api.testing.Assertions.assertThatThrownBy;

import com.palantir.conjure.java.api.errors.ErrorType;
import com.palantir.conjure.java.api.errors.QosException;
import com.palantir.conjure.java.api.errors.QosReason;
import com.palantir.conjure.java.api.errors.RemoteException;
import com.palantir.conjure.java.api.errors.SerializableError;
import com.palantir.conjure.java.api.errors.ServiceException;
import org.junit.jupiter.api.Test;

public final class AssertionsTest {

    @Test
    public void testAssertThatServiceExceptionThrownBy_failsIfNothingThrown() {
        assertThatThrownBy(() -> assertThatServiceExceptionThrownBy(() -> {
                    // Not going to throw anything
                }))
                .hasMessageContaining("Expecting code to raise a throwable.");
    }

    @Test
    public void testAssertThatServiceExceptionThrownBy_failsIfWrongExceptionThrown() {
        assertThatThrownBy(() -> assertThatServiceExceptionThrownBy(() -> {
                    throw new RuntimeException("My message");
                }))
                .hasMessageContaining(
                        "com.palantir.conjure.java.api.errors.ServiceException",
                        "java.lang.RuntimeException",
                        "My message");
    }

    @Test
    public void testAssertThatServiceExceptionThrownBy_catchesServiceException() {
        assertThatServiceExceptionThrownBy(() -> {
                    throw new ServiceException(ErrorType.INTERNAL);
                })
                .hasType(ErrorType.INTERNAL);
    }

    @Test
    public void testAssertThatRemoteExceptionThrownBy_failsIfNothingThrown() {
        assertThatThrownBy(() -> assertThatRemoteExceptionThrownBy(() -> {
                    // Not going to throw anything
                }))
                .hasMessageContaining("Expecting code to raise a throwable.");
    }

    @Test
    public void testAssertThatRemoteExceptionThrownBy_failsIfWrongExceptionThrown() {
        assertThatThrownBy(() -> assertThatRemoteExceptionThrownBy(() -> {
                    throw new RuntimeException("My message");
                }))
                .hasMessageContaining(
                        "com.palantir.conjure.java.api.errors.RemoteException",
                        "java.lang.RuntimeException",
                        "My message");
    }

    @Test
    public void testAssertThatRemoteExceptionThrownBy_catchesServiceException() {
        assertThatRemoteExceptionThrownBy(() -> {
                    throw new RemoteException(
                            SerializableError.forException(new ServiceException(ErrorType.INTERNAL)),
                            ErrorType.INTERNAL.httpErrorCode());
                })
                .isGeneratedFromErrorType(ErrorType.INTERNAL);
    }

    @Test
    public void testAssertThatQosExceptionThrownBy_failsIfNothingThrown() {
        assertThatThrownBy(() -> assertThatQosExceptionThrownBy(() -> {
                    // Not going to throw anything
                }))
                .hasMessageContaining("Expecting code to raise a throwable.");
    }

    @Test
    public void testAssertThatQosExceptionThrownBy_failsIfWrongExceptionThrown() {
        assertThatThrownBy(() -> assertThatQosExceptionThrownBy(() -> {
                    throw new RuntimeException("My message");
                }))
                .hasMessageContaining(
                        "com.palantir.conjure.java.api.errors.QosException",
                        "java.lang.RuntimeException",
                        "My message");
    }

    @Test
    public void testAssertThatQosExceptionThrownBy_catchesQosException() {
        assertThatQosExceptionThrownBy(() -> {
                    throw QosException.unavailable(QosReason.of("reason"));
                })
                .hasReason(QosReason.of("reason"));
    }
}
