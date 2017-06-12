/*
 * Copyright 2017 Palantir Technologies, Inc. All rights reserved.
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
import com.palantir.tokens2.auth.BearerToken;
import java.util.Map;
import java.util.Optional;
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
     * Fallback API token to be used if the service specific API token is not defined in the
     * {@link PartialServiceConfiguration}.
     */
    @JsonProperty("apiToken")
    public abstract Optional<BearerToken> defaultApiToken();

    /**
     * Fallback SSL Configuration to be used if the service specific SSL configuration is not defined in the
     * {@link PartialServiceConfiguration}.
     */
    @JsonProperty("security")
    public abstract Optional<SslConfiguration> defaultSecurity();

    @JsonProperty("services")
    public abstract Map<String, PartialServiceConfiguration> services();

    /**
     * Default global proxy configuration for connecting to the services.
     */
    @JsonProperty("proxyConfiguration")
    public abstract Optional<ProxyConfiguration> defaultProxyConfiguration();

    /**
     * Default global connect timeout.
     */
    @JsonProperty("connectTimeout")
    public abstract Optional<HumanReadableDuration> defaultConnectTimeout();

    /**
     * Default global read timeout.
     */
    @JsonProperty("readTimeout")
    public abstract Optional<HumanReadableDuration> defaultReadTimeout();

    /**
     * Default enablement of gcm cipher suites, defaults to false.
     */
    @JsonProperty("enableGcmCipherSuites")
    public abstract Optional<Boolean> defaultEnableGcmCipherSuites();

    public static Builder builder() {
        return new Builder();
    }

    // TODO(jnewman): #317 - remove kebab-case methods when Jackson 2.7 is picked up
    public static final class Builder extends ImmutableServicesConfigBlock.Builder {

        @JsonProperty("api-token")
        Builder defaultApiTokenKebabCase(Optional<BearerToken> defaultApiToken) {
            return defaultApiToken(defaultApiToken);
        }

        @JsonProperty("proxy-configuration")
        Builder defaultProxyConfigurationKebabCase(Optional<ProxyConfiguration> defaultProxyConfiguration) {
            return defaultProxyConfiguration(defaultProxyConfiguration);
        }

        @JsonProperty("connect-timeout")
        Builder defaultConnectTimeoutKebabCase(Optional<HumanReadableDuration> defaultConnectTimeout) {
            return defaultConnectTimeout(defaultConnectTimeout);
        }

        @JsonProperty("read-timeout")
        Builder defaultReadTimeoutKebabCase(Optional<HumanReadableDuration> defaultReadTimeout) {
            return defaultReadTimeout(defaultReadTimeout);
        }

        @JsonProperty("enable-gcm-cipher-suites")
        Builder defaultEnableGcmCipherSuitesKebabCase(Optional<Boolean> defaultEnableGcmCipherSuites) {
            return defaultEnableGcmCipherSuites(defaultEnableGcmCipherSuites);
        }
    }
}
