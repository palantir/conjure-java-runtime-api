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

package com.palantir.remoting.api.config.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.palantir.remoting.api.config.ssl.SslConfiguration;
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
    Optional<BearerToken> apiToken();

    /** The SSL configuration needed to interact with the service. */
    Optional<SslConfiguration> security();

    /** A list of service URIs. */
    List<String> uris();

    /** Connect timeout for requests. */
    Optional<HumanReadableDuration> connectTimeout();

    /** Read timeout for requests. */
    Optional<HumanReadableDuration> readTimeout();

    /** Write timeout for requests. */
    Optional<HumanReadableDuration> writeTimeout();

    /** The maximum number of times a failed RPC call should be retried. */
    Optional<Integer> maxNumRetries();

    /**
     * The size of one backoff time slot for call retries. For example, an exponential backoff retry algorithm may
     * choose a backoff time in {@code [0, backoffSlotSize * 2^c]} for the c-th retry.
     */
    Optional<HumanReadableDuration> backoffSlotSize();

    /** Enables slower, but more standard cipher suite support, defaults to false. */
    Optional<Boolean> enableGcmCipherSuites();

    /** Proxy configuration for connecting to the service. If absent, uses system proxy configuration. */
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

    // TODO(jnewman): #317 - remove kebab-case methods when Jackson 2.7 is picked up
    class Builder extends ImmutablePartialServiceConfiguration.Builder {

        @JsonProperty("api-token")
        Builder apiTokenKebabCase(Optional<BearerToken> apiToken) {
            return apiToken(apiToken);
        }

        @JsonProperty("connect-timeout")
        Builder connectTimeoutKebabCase(Optional<HumanReadableDuration> connectTimeout) {
            return connectTimeout(connectTimeout);
        }

        @JsonProperty("read-timeout")
        Builder readTimeoutKebabCase(Optional<HumanReadableDuration> readTimeout) {
            return readTimeout(readTimeout);
        }

        @JsonProperty("write-timeout")
        Builder writeTimeoutKebabCase(Optional<HumanReadableDuration> writeTimeout) {
            return writeTimeout(writeTimeout);
        }

        @JsonProperty("max-num-retries")
        Builder maxNumRetriesKebabCase(Optional<Integer> maxNumRetries) {
            return maxNumRetries(maxNumRetries);
        }

        @JsonProperty("backoff-slot-size")
        Builder backoffSlotSizeKebabCase(Optional<HumanReadableDuration> backoffSlotSize) {
            return backoffSlotSize(backoffSlotSize);
        }

        @JsonProperty("proxy-configuration")
        Builder proxyConfigurationKebabCase(Optional<ProxyConfiguration> proxyConfiguration) {
            return proxyConfiguration(proxyConfiguration);
        }

        @JsonProperty("enable-gcm-cipher-suites")
        Builder enableGcmCipherSuitesKebabCase(Optional<Boolean> enableGcmCipherSuites) {
            return enableGcmCipherSuites(enableGcmCipherSuites);
        }
    }
}
