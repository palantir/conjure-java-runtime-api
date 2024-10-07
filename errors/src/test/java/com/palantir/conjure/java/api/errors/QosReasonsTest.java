/*
 * (c) Copyright 2024 Palantir Technologies Inc. All rights reserved.
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

import com.google.common.collect.ImmutableMap;
import com.palantir.conjure.java.api.errors.QosReason.DueTo;
import com.palantir.conjure.java.api.errors.QosReason.RetryHint;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;

class QosReasonsTest {

    @Test
    public void testReasonWithNoAdditionalFields() {
        Map<String, String> headers = new HashMap<>();
        QosReason reason = QosReason.of("reason");
        QosReasons.encodeToResponse(reason, headers, Encoder.INSTANCE);
        assertThat(headers).isEmpty();
    }

    @Test
    public void testReasonWithFields() {
        Map<String, String> headers = new HashMap<>();
        QosReason reason = QosReason.builder()
                .reason("reason")
                .dueTo(DueTo.CUSTOM)
                .retryHint(RetryHint.DO_NOT_RETRY)
                .build();
        QosReasons.encodeToResponse(reason, headers, Encoder.INSTANCE);
        assertThat(headers).isEqualTo(ImmutableMap.of("Qos-Due-To", "custom", "Qos-Retry-Hint", "do-not-retry"));
    }

    @Test
    public void testRoundTrip() {
        Map<String, String> headers = new HashMap<>();
        QosReason original = QosReason.builder()
                .reason("reason")
                .dueTo(DueTo.CUSTOM)
                .retryHint(RetryHint.DO_NOT_RETRY)
                .build();
        QosReasons.encodeToResponse(original, headers, Encoder.INSTANCE);
        QosReason recreated = QosReasons.parseFromResponse(headers, Decoder.INSTANCE);
        assertThat(recreated).isNotEqualTo(original);
        assertThat(recreated)
                .isEqualTo(QosReason.builder()
                        .from(original)
                        .reason("client-qos-response")
                        .build());
    }

    @Test
    public void dueToRoundTrip() {
        SoftAssertions softly = new SoftAssertions();
        for (DueTo expected : DueTo.values()) {
            Optional<DueTo> actual = QosReasons.parseDueTo(QosReasons.toHeaderValue(expected));
            softly.assertThat(actual).hasValue(expected);
        }
        softly.assertAll();
    }

    @Test
    public void dueToCaseSensitivity() {
        assertThat(QosReasons.parseDueTo("custom")).hasValue(DueTo.CUSTOM);
        assertThat(QosReasons.parseDueTo("CUSTOM")).hasValue(DueTo.CUSTOM);
    }

    @Test
    public void retryHintRoundTrip() {
        SoftAssertions softly = new SoftAssertions();
        for (RetryHint expected : RetryHint.values()) {
            Optional<RetryHint> actual = QosReasons.parseRetryHint(QosReasons.toHeaderValue(expected));
            softly.assertThat(actual).hasValue(expected);
        }
        softly.assertAll();
    }

    @Test
    public void retryHintCaseSensitivity() {
        assertThat(QosReasons.parseRetryHint("do-not-retry")).hasValue(RetryHint.DO_NOT_RETRY);
        assertThat(QosReasons.parseRetryHint("DO-NOT-RETRY")).hasValue(RetryHint.DO_NOT_RETRY);
    }

    private enum Encoder implements QosReasons.QosResponseEncodingAdapter<Map<String, String>> {
        INSTANCE;

        @Override
        public void setHeader(Map<String, String> stringStringMap, String headerName, String headerValue) {
            stringStringMap.put(headerName, headerValue);
        }
    }

    private enum Decoder implements QosReasons.QosResponseDecodingAdapter<Map<String, String>> {
        INSTANCE;

        @Override
        public Optional<String> getFirstHeader(Map<String, String> stringStringMap, String headerName) {
            return Optional.ofNullable(stringStringMap.get(headerName));
        }
    }
}
