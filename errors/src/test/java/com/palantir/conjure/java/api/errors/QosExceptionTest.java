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

import static com.palantir.logsafe.testing.Assertions.assertThatLoggableExceptionThrownBy;
import static org.assertj.core.api.Assertions.assertThat;

import com.google.errorprone.annotations.CompileTimeConstant;
import com.palantir.conjure.java.api.errors.QosReason.DueTo;
import com.palantir.conjure.java.api.errors.QosReason.RetryHint;
import com.palantir.logsafe.SafeArg;
import com.palantir.logsafe.exceptions.SafeIllegalArgumentException;
import java.net.MalformedURLException;
import java.net.URL;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public final class QosExceptionTest {

    @Test
    public void testVisitorSanity() throws Exception {
        QosException.Visitor<Class<?>> visitor = new QosException.Visitor<>() {
            @Override
            public Class<?> visit(QosException.Throttle exception) {
                return exception.getClass();
            }

            @Override
            public Class<?> visit(QosException.RetryOther exception) {
                return exception.getClass();
            }

            @Override
            public Class<?> visit(QosException.Unavailable exception) {
                return exception.getClass();
            }
        };

        assertThat(QosException.throttle().accept(visitor)).isEqualTo(QosException.Throttle.class);
        assertThat(QosException.retryOther(new URL("http://foo")).accept(visitor))
                .isEqualTo(QosException.RetryOther.class);
        assertThat(QosException.unavailable().accept(visitor)).isEqualTo(QosException.Unavailable.class);
    }

    @Test
    public void testReason() {
        QosReason reason = QosReason.builder()
                .reason("custom-reason")
                .dueTo(DueTo.CUSTOM)
                .retryHint(RetryHint.DO_NOT_RETRY)
                .build();
        assertThat(QosException.throttle(reason).getReason()).isEqualTo(reason);
        assertThatLoggableExceptionThrownBy(() -> {
                    throw QosException.throttle(reason);
                })
                .containsArgs(
                        SafeArg.of("reason", "custom-reason"),
                        SafeArg.of("retryHint", RetryHint.DO_NOT_RETRY),
                        SafeArg.of("dueTo", DueTo.CUSTOM));
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                // Too long
                "reason-reason-reason-reason-reason-reason-reason---",
                // Unsupported characters
                "reason?",
                "Reason",
                // too short
                ""
            })
    public void testInvalidReason(@CompileTimeConstant String reason) {
        assertThatLoggableExceptionThrownBy(() -> QosReason.of(reason))
                .isInstanceOf(SafeIllegalArgumentException.class)
                .hasLogMessage("Reason must be at most 50 characters, and only contain lowercase letters, numbers, "
                        + "and hyphens (-).")
                .hasExactlyArgs(SafeArg.of("reason", reason));
    }

    @Test
    public void testDefaultReasons() throws MalformedURLException {
        assertThat(QosException.throttle().getReason().toString()).isEqualTo("qos-throttle");
        assertThat(QosException.retryOther(new URL("http://foo")).getReason().toString())
                .isEqualTo("qos-retry-other");
        assertThat(QosException.unavailable().getReason().toString()).isEqualTo("qos-unavailable");
    }
}
