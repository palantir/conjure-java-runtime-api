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

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;

/** Given a {@link ServicesConfiguration}, populates {@link ServiceConfiguration} instances for configured services. */
public final class ServiceConfigurationFactory {

    // Defaults for parameters that are optional in PartialServiceConfiguration.
    private static final HumanReadableDuration DEFAULT_CONNECT_TIMEOUT = HumanReadableDuration.seconds(10);
    private static final HumanReadableDuration DEFAULT_READ_TIMEOUT = HumanReadableDuration.minutes(10);
    private static final HumanReadableDuration DEAULT_WRITE_TIMEOUT = HumanReadableDuration.minutes(10);
    private static final ProxyConfiguration DEFAULT_PROXY_CONFIG = ProxyConfiguration.SYSTEM;
    private static final boolean DEFAULT_ENABLE_GCM_CIPHERS = false;
    private static final int DEFAULT_MAX_NUM_RETRIES = 0;

    private final ServicesConfiguration services;

    private ServiceConfigurationFactory(ServicesConfiguration services) {
        this.services = services;
    }

    /** Constructs a factory for a fixed ServicesConfiguration. */
    public static ServiceConfigurationFactory of(ServicesConfiguration services) {
        return new ServiceConfigurationFactory(services);
    }

    /** Returns the {@link ServiceConfiguration} for the given name. */
    public ServiceConfiguration get(String serviceName) {
        PartialServiceConfiguration partial = services.services().get(serviceName);
        Preconditions.checkNotNull(partial, "No configuration found for service: %s", serviceName);
        return constructClient(serviceName, partial);
    }

    /** Returns all {@link ServiceConfiguration}s. */
    public Map<String, ServiceConfiguration> getAll() {
        return ImmutableMap.copyOf(Maps.transformEntries(services.services(), this::constructClient));
    }

    /**
     * Checks if a service is enabled, i.e., if the configured {@link #services} contains a service configuration of the
     * given name, and the configuration has at least for {@link PartialServiceConfiguration#uris() URI}.
     */
    public boolean isEnabled(String serviceName) {
        PartialServiceConfiguration serviceConfig = services.services().get(serviceName);
        return serviceConfig != null && !serviceConfig.uris().isEmpty();
    }

    /**
     * Returns a new {@link ServicesConfiguration} obtained by copying all values from the given {@link
     * PartialServiceConfiguration} and then filling in absent optional values with defaults from this {@link
     * ServicesConfiguration}.
     */
    private ServiceConfiguration constructClient(String serviceName, PartialServiceConfiguration partial) {
        return ImmutableServiceConfiguration.builder()
                .apiToken(orElse(partial.apiToken(), services.defaultApiToken()))
                .security(orElse(partial.security(), services.defaultSecurity()).orElseThrow(
                        () -> new IllegalArgumentException("Must provide default security or"
                                + "service-specific security block for service: " + serviceName)))
                .uris(partial.uris())
                .connectTimeout(Duration.ofSeconds(
                        orElse(partial.connectTimeout(),
                                services.defaultConnectTimeout())
                                .orElse(DEFAULT_CONNECT_TIMEOUT).toSeconds()))
                .readTimeout(Duration.ofSeconds(
                        orElse(partial.readTimeout(),
                                services.defaultReadTimeout())
                                .orElse(DEFAULT_READ_TIMEOUT).toSeconds()))
                .writeTimeout(Duration.ofSeconds(
                        partial.writeTimeout().orElse(DEAULT_WRITE_TIMEOUT).toSeconds()))
                .proxy(orElse(partial.proxyConfiguration(), services.defaultProxyConfiguration())
                        .orElse(DEFAULT_PROXY_CONFIG))
                .enableGcmCipherSuites(orElse(partial.enableGcmCipherSuites(),
                        services.defaultEnableGcmCipherSuites()).orElse(DEFAULT_ENABLE_GCM_CIPHERS))
                .maxNumRetries(DEFAULT_MAX_NUM_RETRIES)
                .build();
    }

    // Returns the first Optional if present, or the second Optional otherwise.
    private static <T> Optional<T> orElse(Optional<T> first, Optional<T> second) {
        if (first.isPresent()) {
            return first;
        } else {
            return second;
        }
    }
}
