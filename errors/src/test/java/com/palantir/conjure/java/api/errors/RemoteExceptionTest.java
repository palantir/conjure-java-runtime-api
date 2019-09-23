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

import org.apache.commons.lang3.SerializationUtils;
import org.junit.jupiter.api.Test;

public final class RemoteExceptionTest {

    @Test
    public void testJavaSerialization() {
        // With explicit error instance
        SerializableError error = new SerializableError.Builder()
                .errorCode("errorCode")
                .errorName("errorName")
                .errorInstanceId("errorId")
                .build();
        RemoteException expected = new RemoteException(error, 500);
        RemoteException actual = SerializationUtils.deserialize(SerializationUtils.serialize(expected));
        assertThat(actual).isEqualToComparingFieldByField(expected);

        // Without error instance
        error = new SerializableError.Builder()
                .errorCode("errorCode")
                .errorName("errorName")
                .build();
        expected = new RemoteException(error, 500);
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
        assertThat(new RemoteException(error, 500).getMessage())
                .isEqualTo("RemoteException: errorCode (errorName) with instance ID errorId");

        error = new SerializableError.Builder()
                .errorCode("errorCode")
                .errorName("errorCode")
                .errorInstanceId("errorId")
                .build();
        assertThat(new RemoteException(error, 500).getMessage())
                .isEqualTo("RemoteException: errorCode with instance ID errorId");
    }
}
