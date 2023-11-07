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

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.palantir.conjure.java.api.ext.jackson.ObjectMappers;
import com.palantir.logsafe.SafeArg;
import com.palantir.logsafe.UnsafeArg;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

public final class SerializableErrorTest {

    private static final SerializableError ERROR = new SerializableError.Builder()
            .errorCode("PERMISSION_DENIED")
            .errorName("Product:SomethingBroke")
            .build();
    private static final ObjectMapper mapper = ObjectMappers.newServerObjectMapper();

    @Test
    public void forException_should_keep_both_safe_and_unsafe_args() {
        ErrorType error = ErrorType.FAILED_PRECONDITION;
        ServiceException exception = new ServiceException(
                error, SafeArg.of("safeKey", 42), UnsafeArg.of("sensitiveInfo", "some user-entered content"));

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
        ServiceException exception =
                new ServiceException(error, SafeArg.of("collision", "first"), UnsafeArg.of("collision", "second"));

        SerializableError expected = new SerializableError.Builder()
                .errorCode(error.code().name())
                .errorName(error.name())
                .errorInstanceId(exception.getErrorInstanceId())
                .putParameters("collision", "second")
                .build();
        assertThat(SerializableError.forException(exception)).isEqualTo(expected);
    }

    // Discussion of potentially changing the serialization format in these three _serializesWithToString tests is at
    // https://github.com/palantir/conjure-java/issues/1812
    @Test
    public void forException_listArgValue_serializesWithToString() {
        ErrorType error = ErrorType.INTERNAL;
        ServiceException exception = new ServiceException(
                error, SafeArg.of("safe-list", List.of("1", "2")), UnsafeArg.of("unsafe-list", List.of("3", "4")));

        SerializableError expected = new SerializableError.Builder()
                .errorCode(error.code().name())
                .errorName(error.name())
                .errorInstanceId(exception.getErrorInstanceId())
                .putParameters("safe-list", "[1, 2]")
                .putParameters("unsafe-list", "[3, 4]")
                .build();
        assertThat(SerializableError.forException(exception)).isEqualTo(expected);
    }

    @Test
    public void forException_mapArgValue_serializesWithToString() {
        ErrorType error = ErrorType.INTERNAL;
        ServiceException exception = new ServiceException(
                error, SafeArg.of("safe-map", Map.of("1", "2")), UnsafeArg.of("unsafe-map", Map.of("ABC", "DEF")));

        SerializableError expected = new SerializableError.Builder()
                .errorCode(error.code().name())
                .errorName(error.name())
                .errorInstanceId(exception.getErrorInstanceId())
                .putParameters("safe-map", "{1=2}")
                .putParameters("unsafe-map", "{ABC=DEF}")
                .build();
        assertThat(SerializableError.forException(exception)).isEqualTo(expected);
    }

    @Test
    public void forException_mapArrayArgValue_serializesWithToString() {
        ErrorType error = ErrorType.INTERNAL;
        ServiceException exception = new ServiceException(
                error,
                SafeArg.of("safe-array", new long[] {1L, 2L, 3L}),
                UnsafeArg.of("unsafe-array", new String[] {"A", "B", "C"}));

        SerializableError actual = SerializableError.forException(exception);

        assertThat(actual.parameters())
                .hasEntrySatisfying(
                        "safe-array",
                        value ->
                                // Example: [J@714afbd4
                                assertThat(value).matches(Pattern.quote("[J@") + "[0-9a-f]+"))
                .hasEntrySatisfying(
                        "unsafe-array",
                        value ->
                                // Example: [Ljava.lang.String;@769a49e3
                                assertThat(value).matches(Pattern.quote("[Ljava.lang.String;@") + "[0-9a-f]+"));
    }

    @Test
    public void forException_optionalArgValue_serializesWithToString() {
        ErrorType error = ErrorType.INTERNAL;
        ServiceException exception = new ServiceException(
                error,
                SafeArg.of("safe-optional-present", Optional.of("hello")),
                UnsafeArg.of("unsafe-optional-empty", Optional.empty()));

        SerializableError expected = new SerializableError.Builder()
                .errorCode(error.code().name())
                .errorName(error.name())
                .errorInstanceId(exception.getErrorInstanceId())
                .putParameters("safe-optional-present", "Optional[hello]")
                .putParameters("unsafe-optional-empty", "Optional.empty")
                .build();
        assertThat(SerializableError.forException(exception)).isEqualTo(expected);
    }

    @Test
    public void testSerializationContainsRedundantParameters() throws Exception {
        assertThat(mapper.writeValueAsString(ERROR))
                .isEqualTo("{\"errorCode\":\"PERMISSION_DENIED\",\"errorName\":\"Product:SomethingBroke\","
                        + "\"errorInstanceId\":\"\",\"parameters\":{}}");

        assertThat(mapper.writeValueAsString(SerializableError.builder()
                        .from(ERROR)
                        .errorInstanceId("errorId")
                        .build()))
                .isEqualTo("{\"errorCode\":\"PERMISSION_DENIED\",\"errorName\":\"Product:SomethingBroke\","
                        + "\"errorInstanceId\":\"errorId\",\"parameters\":{}}");
    }

    @Test
    public void testDeserializesWhenRedundantParamerersAreGiven() throws Exception {
        String serialized =
                "{\"errorCode\":\"PERMISSION_DENIED\",\"errorName\":\"Product:SomethingBroke\",\"exceptionClass\":"
                        + "\"java.lang.IllegalStateException\",\"message\":\"Human readable message\"}";
        assertThat(deserialize(serialized)).isEqualTo(ERROR);
    }

    @Test
    public void testDeserializesWhenExplicitErrorIdIsGiven() throws Exception {
        String serialized = "{\"errorCode\":\"PERMISSION_DENIED\",\"errorName\":\"Product:SomethingBroke\","
                + "\"errorInstanceId\":\"errorId\"}";
        assertThat(deserialize(serialized))
                .isEqualTo(SerializableError.builder()
                        .from(ERROR)
                        .errorInstanceId("errorId")
                        .build());
    }

    @Test
    public void testDeserializesWithDefaultNamesOnly() throws Exception {
        String serialized = "{\"errorCode\":\"PERMISSION_DENIED\",\"errorName\":\"Product:SomethingBroke\"}";
        assertThat(deserialize(serialized)).isEqualTo(ERROR);
    }

    @Test
    public void testDeserializationFailsWhenNeitherErrorNameNorMessageIsSet() throws Exception {
        String serialized = "{\"errorCode\":\"PERMISSION_DENIED\"}";
        assertThatThrownBy(() -> deserialize(serialized))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Expected either 'errorName' or 'message' to be set");
    }

    @Test
    public void testDeserializationWithPrimitiveParameterValues() throws Exception {
        String serialized = "{\n  \"errorCode\": \"TIMEOUT\",\n"
                + "  \"errorName\": \"MyApplication:Timeout\",\n"
                + "  \"errorInstanceId\": \"63085482-7d00-11ee-b962-0242ac120002\",\n"
                + "  \"parameters\": {\n"
                + "    \"key-1\": \"string\","
                + "    \"key-2\": 2,"
                + "    \"key-3\": 4.56,"
                + "    \"key-4\": false"
                + "    }\n"
                + "  }\n"
                + "}";
        assertThat(deserialize(serialized).parameters())
                .containsExactly(
                        // JSON primitive types are coerced to strings
                        Map.entry("key-1", "string"),
                        Map.entry("key-2", "2"),
                        Map.entry("key-3", "4.56"),
                        Map.entry("key-4", "false"));
    }

    @Test
    public void testDeserializationWithNullParameterValue() {
        String serialized = "{\n  \"errorCode\": \"TIMEOUT\",\n"
                + "  \"errorName\": \"MyApplication:Timeout\",\n"
                + "  \"errorInstanceId\": \"63085482-7d00-11ee-b962-0242ac120002\",\n"
                + "  \"parameters\": {\n"
                + "    \"some-key\": null\n"
                + "    }\n"
                + "  }\n"
                + "}";
        assertThatThrownBy(() -> deserialize(serialized))
                .isInstanceOf(JsonMappingException.class)
                .hasMessageContaining("parameters value for key: some-key");
    }

    @Test
    public void testDeserializationWithJsonMap() throws Exception {
        // Example from conjure-go's tests:
        // https://github.com/palantir/conjure-go-runtime/blob/fdb08c91f7566c2109ff8c5c2ecebb2f96c97d58/conjure-go-contract/errors/serializable_error_test.go#L41-L50
        String serialized = "{\n  \"errorCode\": \"TIMEOUT\",\n"
                + "  \"errorName\": \"MyApplication:Timeout\",\n"
                + "  \"errorInstanceId\": \"63085482-7d00-11ee-b962-0242ac120002\",\n"
                + "  \"parameters\": {\n"
                + "    \"metadata\": {\n"
                + "      \"keyB\": 4\n"
                + "    }\n"
                + "  }\n"
                + "}";
        assertThatThrownBy(() -> deserialize(serialized))
                .isInstanceOf(MismatchedInputException.class)
                .hasMessageContaining("Cannot deserialize value of type `java.lang.String` from Object value "
                        + "(token `JsonToken.START_OBJECT`)");
    }

    private static SerializableError deserialize(String serialized) throws IOException {
        return mapper.readValue(serialized, SerializableError.class);
    }
}
