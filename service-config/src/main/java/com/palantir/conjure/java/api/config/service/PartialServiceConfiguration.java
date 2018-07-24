/*
 * (c) Copyright 2018 Palantir Technologies Inc. All rights reserved.
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

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.palantir.conjure.java.api.config.ssl.SslConfiguration;
import com.palantir.tokens.auth.BearerToken;
import java.util.List;
import java.util.Optional;
import org.immutables.value.Value.Immutable;

@Immutable
@JsonSerialize(as = ImmutablePartialServiceConfiguration.class)
@JsonDeserialize(builder = PartialServiceConfiguration.Builder.class)
@ImmutablesStyle
public interface PartialServiceConfiguration {

    /** The API token to be used to interact with the service. */
    @JsonAlias("api-token")
    Optional<BearerToken> apiToken();

    /** The SSL configuration needed to interact with the service. */
    Optional<SslConfiguration> security();

    /** A list of service URIs. */
    List<String> uris();

    /** Connect timeout for requests. */
    @JsonAlias("connect-timeout")
    Optional<HumanReadableDuration> connectTimeout();

    /** Read timeout for requests. */
    @JsonAlias("read-timeout")
    Optional<HumanReadableDuration> readTimeout();

    /** Write timeout for requests. */
    @JsonAlias("write-timeout")
    Optional<HumanReadableDuration> writeTimeout();

    /** The maximum number of times a failed RPC call should be retried. */
    @JsonAlias("max-num-retries")
    Optional<Integer> maxNumRetries();

    /**
     * The size of one backoff time slot for call retries. For example, an exponential backoff retry algorithm may
     * choose a backoff time in {@code [0, backoffSlotSize * 2^c]} for the c-th retry.
     */
    @JsonAlias("backoff-slot-size")
    Optional<HumanReadableDuration> backoffSlotSize();

    /** Enables slower, but more standard cipher suite support, defaults to false. */
    @JsonAlias("enable-gcm-cipher-suites")
    Optional<Boolean> enableGcmCipherSuites();

    /** Proxy configuration for connecting to the service. If absent, uses system proxy configuration. */
    @JsonAlias("proxy-configuration")
    Optional<ProxyConfiguration> proxyConfiguration();

    static PartialServiceConfiguration of(List<String> uris, Optional<SslConfiguration> sslConfig) {
        return PartialServiceConfiguration.builder()
                .uris(uris)
                .security(sslConfig)
                .build();
    }

    static Builder builder() {
        return new Builder();
    }

    class Builder extends ImmutablePartialServiceConfiguration.Builder { }
}
