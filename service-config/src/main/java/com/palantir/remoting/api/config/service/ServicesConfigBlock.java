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

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.palantir.remoting.api.config.ssl.SslConfiguration;
import com.palantir.tokens.auth.BearerToken;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.immutables.value.Value;
import org.immutables.value.Value.Immutable;

/**
 * Configuration class that contains a map of {@code serviceName}s and their respective {@link
 * PartialServiceConfiguration}s. Allows users to specify default properties for all services. Use a {@link
 * ServiceConfigurationFactory} to propagate the defaults to the partially-specified services and obtain fully-specified
 * {@link ServiceConfiguration} objects.
 */
@Immutable
@JsonSerialize(as = ImmutableServicesConfigBlock.class)
@JsonDeserialize(builder = ServicesConfigBlock.Builder.class)
@ImmutablesStyle
public abstract class ServicesConfigBlock {

    /**
     * Fallback API token to be used if the service specific API token is not defined in the {@link
     * PartialServiceConfiguration}.
     */
    @JsonProperty("apiToken")
    @JsonAlias("api-token")
    public abstract Optional<BearerToken> defaultApiToken();

    /**
     * Fallback SSL Configuration to be used if the service specific SSL configuration is not defined in the {@link
     * PartialServiceConfiguration}.
     */
    @JsonProperty("security")
    public abstract Optional<SslConfiguration> defaultSecurity();

    @JsonProperty("services")
    public abstract Map<String, PartialServiceConfiguration> services();

    /**
     * Default global proxy configuration for connecting to the services.
     */
    @JsonProperty("proxyConfiguration")
    @JsonAlias("proxy-configuration")
    public abstract Optional<ProxyConfiguration> defaultProxyConfiguration();

    /**
     * Default global connect timeout.
     */
    @JsonProperty("connectTimeout")
    @JsonAlias("connect-timeout")
    public abstract Optional<HumanReadableDuration> defaultConnectTimeout();

    /**
     * Default global read timeout.
     */
    @JsonProperty("readTimeout")
    @JsonAlias("read-timeout")
    public abstract Optional<HumanReadableDuration> defaultReadTimeout();

    /**
     * Default global write timeout.
     */
    @JsonProperty("writeTimeout")
    @JsonAlias("write-timeout")
    public abstract Optional<HumanReadableDuration> defaultWriteTimeout();

    /**
     * Default global backoff slot size, see {@link PartialServiceConfiguration#backoffSlotSize()}.
     */
    @JsonProperty("backoffSlotSize")
    @JsonAlias("backoff-slot-size")
    public abstract Optional<HumanReadableDuration> defaultBackoffSlotSize();

    /**
     * Default enablement of gcm cipher suites, defaults to false.
     */
    @JsonProperty("enableGcmCipherSuites")
    @JsonAlias("enable-gcm-cipher-suites")
    public abstract Optional<Boolean> defaultEnableGcmCipherSuites();

    /**
     * Returns Conjure's {@link com.palantir.conjure.java.api.config.service.ServicesConfigBlock} type for forward
     * compatibility.
     */
    @Value.Lazy
    public com.palantir.conjure.java.api.config.service.ServicesConfigBlock asConjure() {
        return com.palantir.conjure.java.api.config.service.ServicesConfigBlock.builder()
                .defaultApiToken(defaultApiToken())
                .defaultSecurity(defaultSecurity().map(SslConfiguration::asConjure))
                .services(services().entrySet().stream().collect(Collectors.toMap(
                        e -> e.getKey(), e -> e.getValue().asConjure())))
                .defaultProxyConfiguration(
                        defaultProxyConfiguration().map(ProxyConfiguration::asConjure))
                .defaultConnectTimeout(
                        defaultConnectTimeout().map(HumanReadableDuration::asConjure))
                .defaultReadTimeout(defaultReadTimeout().map(HumanReadableDuration::asConjure))
                .defaultWriteTimeout(defaultWriteTimeout().map(HumanReadableDuration::asConjure))
                .defaultBackoffSlotSize(
                        defaultBackoffSlotSize().map(HumanReadableDuration::asConjure))
                .defaultEnableGcmCipherSuites(defaultEnableGcmCipherSuites())
                .build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder extends ImmutableServicesConfigBlock.Builder {}
}
