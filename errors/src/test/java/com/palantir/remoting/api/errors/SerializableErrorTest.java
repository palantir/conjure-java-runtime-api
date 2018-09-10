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

package com.palantir.remoting.api.errors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.palantir.logsafe.SafeArg;
import com.palantir.logsafe.UnsafeArg;
import com.palantir.remoting.api.ext.jackson.ObjectMappers;
import java.io.IOException;
import org.junit.Test;

public final class SerializableErrorTest {

    private static final SerializableError ERROR = new SerializableError.Builder()
            .errorCode("code")
            .errorName("name")
            .build();
    private static final ObjectMapper mapper = ObjectMappers.newServerObjectMapper();

    private static SerializableError deserialize(String serialized) throws IOException {
        return mapper.readValue(serialized, SerializableError.class);
    }

    @Test
    public void forException_should_keep_both_safe_and_unsafe_args() {
        ErrorType error = ErrorType.FAILED_PRECONDITION;
        ServiceException exception = new ServiceException(error,
                SafeArg.of("safeKey", 42),
                UnsafeArg.of("sensitiveInfo", "some user-entered content"));

        SerializableError expected = new SerializableError.Builder()
                .errorCode(error.code().name())
                .errorName(error.name())
                .errorInstanceId(exception.getErrorInstanceId())
                .putParameters("safeKey", "42")
                .putParameters("sensitiveInfo", "some user-entered content")
                .build();
        assertThat(SerializableError.forException(exception)).isEqualTo(expected);
    }

    @Test
    public void forException_arg_key_collisions_just_use_the_last_one() {
        ErrorType error = ErrorType.INTERNAL;
        ServiceException exception = new ServiceException(
                error,
                SafeArg.of("collision", "first"),
                UnsafeArg.of("collision", "second"));

        SerializableError expected = new SerializableError.Builder()
                .errorCode(error.code().name())
                .errorName(error.name())
                .errorInstanceId(exception.getErrorInstanceId())
                .putParameters("collision", "second")
                .build();
        assertThat(SerializableError.forException(exception)).isEqualTo(expected);
    }

    @Test
    public void testSerializationContainsRedundantParameters() throws Exception {
        assertThat(mapper.writeValueAsString(ERROR))
                .isEqualTo(
                        "{\"errorCode\":\"code\",\"errorName\":\"name\",\"errorInstanceId\":\"\",\"parameters\":{}}");

        assertThat(mapper.writeValueAsString(
                SerializableError.builder().from(ERROR).errorInstanceId("errorId").build()))
                .isEqualTo("{\"errorCode\":\"code\",\"errorName\":\"name\",\"errorInstanceId\":\"errorId\""
                        + ",\"parameters\":{}}");
    }

    @Test
    public void testDeserializesWhenRedundantParamerersAreGiven() throws Exception {
        String serialized =
                "{\"errorCode\":\"code\",\"errorName\":\"name\",\"exceptionClass\":\"code\",\"message\":\"name\"}";
        assertThat(deserialize(serialized)).isEqualTo(ERROR);
    }

    @Test
    public void testDeserializesWhenExplicitErrorIdIsGiven() throws Exception {
        String serialized = "{\"errorCode\":\"code\",\"errorName\":\"name\",\"errorInstanceId\":\"errorId\"}";
        assertThat(deserialize(serialized))
                .isEqualTo(SerializableError.builder().from(ERROR).errorInstanceId("errorId").build());
    }

    @Test
    public void testDeserializesWithDefaultNamesOnly() throws Exception {
        String serialized = "{\"errorCode\":\"code\",\"errorName\":\"name\"}";
        assertThat(deserialize(serialized)).isEqualTo(ERROR);
    }

    @Test
    public void testDeserializationFailsWhenNeitherErrorNameNorMessageIsSet() throws Exception {
        String serialized = "{\"errorCode\":\"code\"}";
        assertThatThrownBy(() -> deserialize(serialized))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Expected either 'errorName' or 'message' to be set");
    }

    @Test
    public void asConjure_converts_remoting3_json() throws IOException {
        assertThat(deserialize("{\"errorCode\":\"code\",\"errorName\":\"name\"}").asConjure())
                .isEqualTo(com.palantir.conjure.java.api.errors.SerializableError.builder()
                        .errorCode("code")
                        .errorName("name")
                        .build());
    }

    @Test
    public void asConjure_converts_remoting2_json() throws IOException {
        assertThat(deserialize("{\"exceptionClass\":\"java.lang.IllegalStateException\","
                + "\"message\":\"Human readable message\"}").asConjure())
                .isEqualTo(com.palantir.conjure.java.api.errors.SerializableError.builder()
                        .exceptionClass("java.lang.IllegalStateException")
                        .message("Human readable message")
                        .build());
    }

    @Test
    public void asConjure_converts_json_with_both_remoting2_and_remoting3_fields() throws IOException {
        assertThat(deserialize("{\"errorCode\":\"PERMISSION_DENIED\",\"errorName\":\"Product:SomethingBroke\","
                + "\"exceptionClass\":\"java.lang.IllegalStateException\",\"message\":\"Human readable message\"}")
                .asConjure())
                .isEqualTo(com.palantir.conjure.java.api.errors.SerializableError.builder()
                        .errorCode("PERMISSION_DENIED")
                        .errorName("Product:SomethingBroke")
                        .build());
    }

    @Test
    public void all_fields_are_preserved_when_present() throws IOException {
        String string = "{\n"
                + "    \"errorCode\":\"CONFLICT\",\n"
                + "    \"errorName\":\"Product:TransactionConflict\",\n"
                + "    \"errorInstanceId\":\"7d345390-e41d-49dd-8dcb-38dd716c0347\",\n"
                + "    \"parameters\":{},\n"
                + "    \"exceptionClass\":\"CONFLICT\",\n"
                + "    \"message\":\"Refer to the server logs with this errorInstanceId: "
                + "7d345390-e41d-49dd-8dcb-38dd716c0347\"\n"
                + "}";

        com.palantir.conjure.java.api.errors.SerializableError expected =
                com.palantir.conjure.java.api.errors.SerializableError.builder()
                .errorCode("CONFLICT")
                .errorName("Product:TransactionConflict")
                .errorInstanceId("7d345390-e41d-49dd-8dcb-38dd716c0347")
                .message("Refer to the server logs with this errorInstanceId: 7d345390-e41d-49dd-8dcb-38dd716c0347")
                .exceptionClass("CONFLICT")
                .build();

        assertThat(deserialize(string).asConjure()).isEqualTo(expected);
    }
}
