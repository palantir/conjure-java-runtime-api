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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public final class QosResponseBodyTest {
    private static final ObjectMapper mapper = ObjectMappers.newClientObjectMapper();
    private static final QosThrottleDetails DETAILS =
            QosThrottleDetails.builder().debugMessage("Quota exceeded").build();

    @Test
    public void testSerialization() throws Exception {
        assertThat(mapper.writeValueAsString(QosResponseBody.of(DETAILS)))
                .isEqualTo("{\"qosDetails\":{\"debugMessage\":\"Quota exceeded\"}}");
    }

    @Test
    public void testDeserialization() throws Exception {
        assertThat(mapper.readValue("{\"qosDetails\":{\"debugMessage\":\"Quota exceeded\"}}", QosResponseBody.class))
                .isEqualTo(QosResponseBody.of(DETAILS));
    }

    @Test
    public void testDeserializationIgnoresUnknownFields() throws Exception {
        assertThat(mapper.readValue(
                                "{\"qosDetails\":{\"debugMessage\":\"Quota exceeded\",\"newField\":1},\"other\":2}",
                                QosResponseBody.class)
                        .qosDetails())
                .contains(DETAILS);
    }

    @ParameterizedTest
    @ValueSource(strings = {"{}", "{\"qosDetails\":null}"})
    public void testDeserializationWithoutDetails(String body) throws Exception {
        assertThat(mapper.readValue(body, QosResponseBody.class).qosDetails()).isEmpty();
    }
}
