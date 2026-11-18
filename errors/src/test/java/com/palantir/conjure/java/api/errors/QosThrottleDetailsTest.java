/*
 * (c) Copyright 2026 Palantir Technologies Inc. All rights reserved.
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

import static com.palantir.logsafe.testing.Assertions.assertThatLoggableExceptionThrownBy;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.palantir.conjure.java.api.ext.jackson.ObjectMappers;
import com.palantir.logsafe.SafeArg;
import com.palantir.logsafe.exceptions.SafeIllegalArgumentException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public final class QosThrottleDetailsTest {
    private static final ObjectMapper mapper = ObjectMappers.newClientObjectMapper();

    @Test
    public void testSerialization() throws Exception {
        assertThat(mapper.writeValueAsString(QosThrottleDetails.builder()
                        .userMessage("Quota exceeded")
                        .build()))
                .isEqualTo("{\"userMessage\":\"Quota exceeded\"}");
    }

    @Test
    public void testSerializationWithoutUserMessage() throws Exception {
        assertThat(mapper.writeValueAsString(QosThrottleDetails.builder().build()))
                .isEqualTo("{}");
    }

    @Test
    public void testDeserialization() throws Exception {
        assertThat(mapper.readValue("{\"userMessage\":\"Quota exceeded\"}", QosThrottleDetails.class))
                .isEqualTo(QosThrottleDetails.builder()
                        .userMessage("Quota exceeded")
                        .build());
    }

    @Test
    public void testDeserializationWithoutUserMessage() throws Exception {
        assertThat(mapper.readValue("{}", QosThrottleDetails.class).userMessage())
                .isEmpty();
    }

    @Test
    public void testDeserializationIgnoresUnknownFields() throws Exception {
        assertThat(mapper.readValue("{\"userMessage\":\"Quota exceeded\",\"newField\":1}", QosThrottleDetails.class))
                .isEqualTo(QosThrottleDetails.builder()
                        .userMessage("Quota exceeded")
                        .build());
    }

    @Nested
    final class UserMessageLength {
        // userMessage must be a compile-time constant, so build long messages from constant expressions
        private static final String CHARS_64 = "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef";
        private static final String CHARS_128 = CHARS_64 + CHARS_64;
        private static final String CHARS_129 = CHARS_128 + "x";

        @Test
        void builderAcceptsMaxLength() {
            assertThat(QosThrottleDetails.builder()
                            .userMessage(CHARS_128)
                            .build()
                            .userMessage())
                    .contains(CHARS_128);
        }

        @Test
        void builderRejectsOverMaxLength() {
            QosThrottleDetails.Builder builder = QosThrottleDetails.builder();
            assertThatLoggableExceptionThrownBy(() -> builder.userMessage(CHARS_129))
                    .isInstanceOf(SafeIllegalArgumentException.class)
                    .hasLogMessage("userMessage is too long")
                    .hasExactlyArgs(SafeArg.of("length", 129), SafeArg.of("maxLength", 128));
        }

        @Test
        void deserializationKeepsMaxLength() throws Exception {
            assertThat(mapper.readValue("{\"userMessage\":\"" + CHARS_128 + "\"}", QosThrottleDetails.class)
                            .userMessage())
                    .contains(CHARS_128);
        }

        @Test
        void deserializationDropsOverMaxLength() throws Exception {
            assertThat(mapper.readValue("{\"userMessage\":\"" + CHARS_129 + "\"}", QosThrottleDetails.class)
                            .userMessage())
                    .isEmpty();
        }

        @Test
        void deserializationRejectsBareString() {
            assertThatThrownBy(() -> mapper.readValue("\"" + CHARS_129 + "\"", QosThrottleDetails.class))
                    .isInstanceOf(MismatchedInputException.class);
        }
    }
}
