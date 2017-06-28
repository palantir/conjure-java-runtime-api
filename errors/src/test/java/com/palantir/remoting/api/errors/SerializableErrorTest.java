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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.palantir.logsafe.SafeArg;
import com.palantir.logsafe.UnsafeArg;
import com.palantir.remoting.api.ext.jackson.ObjectMappers;
import java.io.IOException;
import org.junit.Test;

public final class SerializableErrorTest {

    private static final SerializableError ERROR = new SerializableError.Builder()
            .message("foo")
            .errorName("bar")
            .build();
    private static final ObjectMapper mapper = ObjectMappers.newServerObjectMapper();

    @Test
    public void testExceptionToError() {
        ErrorType error = ErrorType.FAILED_PRECONDITION;
        ServiceException exception =
                new ServiceException(error, SafeArg.of("safeKey", 42), UnsafeArg.of("foo", "bar"));
        SerializableError expected = new SerializableError.Builder()
                .errorName(error.name().name())
                .message(error.description() + " (ErrorId " + exception.getErrorId() + ")")
                .putParameters("safeKey", "42")
                .build();
        assertThat(SerializableError.forException(exception)).isEqualTo(expected);
    }

    @Test
    public void testSerializationContainsBothErrorNameAndExceptionClass() throws Exception {
        assertThat(mapper.writeValueAsString(ERROR))
                .isEqualTo("{\"errorName\":\"bar\",\"message\":\"foo\",\"parameters\":{},\"exceptionClass\":\"bar\"}");
    }

    @Test
    public void testDeserializesWithBothErrorNameOnlyAndExceptionClass() throws Exception {
        String serialized = "{\"errorName\":\"bar\",\"message\":\"foo\",\"exceptionClass\":\"bar\"}";
        assertThat(deserialize(serialized)).isEqualTo(ERROR);
    }

    @Test
    public void testDeserializesWithErrorNameOnly() throws Exception {
        String serialized = "{\"errorName\":\"bar\",\"message\":\"foo\"}";
        assertThat(deserialize(serialized)).isEqualTo(ERROR);
    }

    @Test
    public void testDeserializesWithExceptionClassOnly() throws Exception {
        String serialized = "{\"message\":\"foo\",\"exceptionClass\":\"bar\"}";
        assertThat(deserialize(serialized)).isEqualTo(ERROR);
    }

    private static SerializableError deserialize(String serialized) throws IOException {
        return mapper.readValue(serialized, SerializableError.class);
    }
}
