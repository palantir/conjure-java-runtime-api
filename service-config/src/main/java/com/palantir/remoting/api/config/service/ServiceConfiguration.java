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

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.palantir.remoting.api.config.ssl.SslConfiguration;
import com.palantir.tokens.auth.BearerToken;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import org.immutables.value.Value;

/** A variant of {@link PartialServiceConfiguration} in which some fields (e.g., {@link #security}) are required. */
@JsonDeserialize(as = ImmutableServiceConfiguration.class)
@Value.Immutable
@ImmutablesStyle
public interface ServiceConfiguration {

    Optional<BearerToken> apiToken();

    SslConfiguration security();

    List<String> uris();

    Optional<Duration> connectTimeout();

    Optional<Duration> readTimeout();

    Optional<Duration> writeTimeout();

    Optional<Integer> maxNumRetries();

    Optional<Duration> backoffSlotSize();

    Optional<Boolean> enableGcmCipherSuites();

    Optional<ProxyConfiguration> proxy();

    static ImmutableServiceConfiguration.Builder builder() {
        return new Builder();
    }

    /**
     * Returns Conjure's {@link com.palantir.conjure.java.api.config.service.ServiceConfiguration} type for forward
     * compatibility.
     */
    @Value.Lazy
    default com.palantir.conjure.java.api.config.service.ServiceConfiguration asConjure() {
        return com.palantir.conjure.java.api.config.service.ServiceConfiguration.builder()
                .apiToken(apiToken())
                .security(security().asConjure())
                .addAllUris(uris())
                .connectTimeout(connectTimeout())
                .readTimeout(readTimeout())
                .writeTimeout(writeTimeout())
                .maxNumRetries(maxNumRetries())
                .backoffSlotSize(backoffSlotSize())
                .enableGcmCipherSuites(enableGcmCipherSuites())
                .proxy(proxy().map(ProxyConfiguration::asConjure))
                .build();
    }

    class Builder extends ImmutableServiceConfiguration.Builder {}
}
