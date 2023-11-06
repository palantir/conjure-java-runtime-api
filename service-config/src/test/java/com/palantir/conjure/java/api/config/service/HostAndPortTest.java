/*
 * (c) Copyright 2023 Palantir Technologies Inc. All rights reserved.
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

package com.palantir.conjure.java.api.config.service;

import static com.palantir.logsafe.testing.Assertions.assertThatLoggableExceptionThrownBy;
import static org.assertj.core.api.Assertions.assertThat;

import com.palantir.logsafe.UnsafeArg;
import org.junit.jupiter.api.Test;

class HostAndPortTest {

    @Test
    void fromString_onHostAndPort_succeeds() {
        HostAndPort hostAndPort = HostAndPort.fromString("example.com:8080");
        assertThat(hostAndPort.getHost()).isEqualTo("example.com");
        assertThat(hostAndPort.getPort()).isEqualTo(8080);
    }

    @Test
    void fromString_onHostAndPortWithHttpProtocolPrefix_doesNotThrow() {
        assertThatLoggableExceptionThrownBy(() -> HostAndPort.fromString("http://example.com:8080"))
                .hasMessageContaining("hostPortString must not contain a protocol prefix like http:// or https://")
                .containsArgs(UnsafeArg.of("hostPortString", "http://example.com:8080"));
    }

    @Test
    void fromString_onHostAndPortWithHttpsProtocolPrefix_doesNotThrow() {
        assertThatLoggableExceptionThrownBy(() -> HostAndPort.fromString("https://example.com:8080"))
                .hasMessageContaining("hostPortString must not contain a protocol prefix like http:// or https://")
                .containsArgs(UnsafeArg.of("hostPortString", "https://example.com:8080"));
    }

    @Test
    void fromString_onHostAndPortWithAbcProtocolPrefix_doesNotThrow() {
        assertThatLoggableExceptionThrownBy(() -> HostAndPort.fromString("abc://example.com:8080"))
                .hasMessageContaining("hostPortString must not contain a protocol prefix like http:// or https://")
                .containsArgs(UnsafeArg.of("hostPortString", "abc://example.com:8080"));
    }
}
