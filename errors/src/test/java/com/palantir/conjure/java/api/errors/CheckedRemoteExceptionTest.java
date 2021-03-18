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

import com.palantir.logsafe.SafeArg;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.jupiter.api.Test;

public final class CheckedRemoteExceptionTest {

    private static class CheckedRemoteExceptionImpl extends CheckedRemoteException {
        protected CheckedRemoteExceptionImpl(SerializableError error, int status) {
            super(error, status);
        }
    }

    @Test
    public void testJavaSerialization() {
        // With explicit error instance
        SerializableError error = new SerializableError.Builder()
                .errorCode("errorCode")
                .errorName("errorName")
                .errorInstanceId("errorId")
                .build();
        CheckedRemoteExceptionImpl expected = new CheckedRemoteExceptionImpl(error, 500);
        CheckedRemoteExceptionImpl actual = SerializationUtils.deserialize(SerializationUtils.serialize(expected));
        assertThat(actual).isEqualToComparingFieldByField(expected);

        // Without error instance
        error = new SerializableError.Builder()
                .errorCode("errorCode")
                .errorName("errorName")
                .build();
        expected = new CheckedRemoteExceptionImpl(error, 500);
        actual = SerializationUtils.deserialize(SerializationUtils.serialize(expected));
        assertThat(actual).isEqualToComparingFieldByField(expected);
    }

    @Test
    public void testSuperMessage() {
        SerializableError error = new SerializableError.Builder()
                .errorCode("errorCode")
                .errorName("errorName")
                .errorInstanceId("errorId")
                .build();
        assertThat(new CheckedRemoteExceptionImpl(error, 500).getMessage())
                .isEqualTo("CheckedRemoteExceptionImpl: errorCode (errorName) with instance ID errorId");

        error = new SerializableError.Builder()
                .errorCode("errorCode")
                .errorName("errorCode")
                .errorInstanceId("errorId")
                .build();
        assertThat(new CheckedRemoteExceptionImpl(error, 500).getMessage())
                .isEqualTo("CheckedRemoteExceptionImpl: errorCode with instance ID errorId");
    }

    @Test
    public void testLogMessageMessage() {
        SerializableError error = new SerializableError.Builder()
                .errorCode("errorCode")
                .errorName("errorName")
                .errorInstanceId("errorId")
                .build();
        CheckedRemoteExceptionImpl remoteException = new CheckedRemoteExceptionImpl(error, 500);
        assertThat(remoteException.getLogMessage()).isEqualTo("CheckedRemoteExceptionImpl: errorCode (errorName)");
    }

    @Test
    public void testArgsIsEmpty() {
        CheckedRemoteExceptionImpl remoteException = new CheckedRemoteExceptionImpl(
                new SerializableError.Builder()
                        .errorCode("errorCode")
                        .errorName("errorName")
                        .errorInstanceId("errorId")
                        .putParameters("param", "value")
                        .build(),
                500);
        assertThat(remoteException.getArgs()).containsExactly(SafeArg.of("errorInstanceId", "errorId"));
    }
}
