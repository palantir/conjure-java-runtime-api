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

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.palantir.conjure.java.api.ext.jackson.ObjectMappers;
import org.junit.jupiter.api.Test;

public final class QosThrottleDetailsTest {
    private static final ObjectMapper mapper = ObjectMappers.newClientObjectMapper();

    @Test
    public void testSerialization() throws Exception {
        assertThat(mapper.writeValueAsString(QosThrottleDetails.builder()
                        .debugMessage("Quota exceeded")
                        .build()))
                .isEqualTo("{\"debugMessage\":\"Quota exceeded\"}");
    }

    @Test
    public void testSerializationWithoutDebugMessage() throws Exception {
        assertThat(mapper.writeValueAsString(QosThrottleDetails.builder().build()))
                .isEqualTo("{}");
    }

    @Test
    public void testDeserialization() throws Exception {
        assertThat(mapper.readValue("{\"debugMessage\":\"Quota exceeded\"}", QosThrottleDetails.class))
                .isEqualTo(QosThrottleDetails.builder()
                        .debugMessage("Quota exceeded")
                        .build());
    }

    @Test
    public void testDeserializationWithoutDebugMessage() throws Exception {
        assertThat(mapper.readValue("{}", QosThrottleDetails.class).debugMessage())
                .isEmpty();
    }

    @Test
    public void testDeserializationIgnoresUnknownFields() throws Exception {
        assertThat(mapper.readValue("{\"debugMessage\":\"Quota exceeded\",\"newField\":1}", QosThrottleDetails.class))
                .isEqualTo(QosThrottleDetails.builder()
                        .debugMessage("Quota exceeded")
                        .build());
    }
}
