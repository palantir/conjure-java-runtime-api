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

package com.palantir.conjure.java.api.config.service;

import com.palantir.logsafe.Preconditions;
import com.palantir.logsafe.SafeArg;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/** Given a {@link ServicesConfigBlock}, populates {@link ServiceConfiguration} instances for configured services. */
public final class ServiceConfigurationFactory {

    private final ServicesConfigBlock services;

    private ServiceConfigurationFactory(ServicesConfigBlock services) {
        this.services = services;
    }

    /** Constructs a factory for a fixed ServicesConfigBlock. */
    public static ServiceConfigurationFactory of(ServicesConfigBlock services) {
        return new ServiceConfigurationFactory(services);
    }

    /** Returns the {@link ServiceConfiguration} for the given name. */
    public ServiceConfiguration get(String serviceName) {
        PartialServiceConfiguration partial = services.services().get(serviceName);
        Preconditions.checkNotNull(partial, "No configuration found for service",
                SafeArg.of("serviceName", serviceName));
        return propagateDefaults(serviceName, partial);
    }

    /** Returns all {@link ServiceConfiguration}s. */
    public Map<String, ServiceConfiguration> getAll() {
        // Return a copy of the immutable data.
        Map<String, ServiceConfiguration> config = new HashMap<>();
        services.services().forEach((key, value) -> config.put(key, propagateDefaults(key, value)));
        return config;
    }

    /**
     * Checks if a service is enabled, i.e., if the configured {@link #services} contains a service configuration of the
     * given name, and the configuration has at least one {@link PartialServiceConfiguration#uris() URI}.
     */
    public boolean isEnabled(String serviceName) {
        PartialServiceConfiguration serviceConfig = services.services().get(serviceName);
        return serviceConfig != null && !serviceConfig.uris().isEmpty();
    }

    /**
     * Returns a new {@link ServiceConfiguration} obtained by copying all values from the given {@link
     * PartialServiceConfiguration} and then filling in absent optional values with defaults from this {@link
     * ServicesConfigBlock}.
     */
    private ServiceConfiguration propagateDefaults(String serviceName, PartialServiceConfiguration partial) {
        return ServiceConfiguration.builder()
                .apiToken(orElse(partial.apiToken(), services.defaultApiToken()))
                .security(orElse(partial.security(), services.defaultSecurity()).orElseThrow(
                        () -> new IllegalArgumentException("Must provide default security or "
                                + "service-specific security block for service: " + serviceName)))
                .uris(partial.uris())
                .connectTimeout(orElse(partial.connectTimeout(), services.defaultConnectTimeout())
                        .map(t -> Duration.ofMillis(t.toMilliseconds())))
                .readTimeout(orElse(partial.readTimeout(), services.defaultReadTimeout())
                        .map(t -> Duration.ofMillis(t.toMilliseconds())))
                .writeTimeout(orElse(partial.writeTimeout(), services.defaultWriteTimeout())
                        .map(t -> Duration.ofMillis(t.toMilliseconds())))
                .maxNumRetries(partial.maxNumRetries())
                .backoffSlotSize(orElse(partial.backoffSlotSize(), services.defaultBackoffSlotSize())
                        .map(t -> Duration.ofMillis(t.toMilliseconds())))
                .failedUrlCooldown(orElse(partial.failedUrlCooldown(), services.defaultFailedUrlCooldown())
                        .map(t -> Duration.ofMillis(t.toMilliseconds())))
                .nodeSelectionStrategy(orElse(partial.nodeSelectionStrategy(), services.defaultNodeSelectionStrategy()))
                .proxy(orElse(partial.proxyConfiguration(), services.defaultProxyConfiguration()))
                .enableGcmCipherSuites(
                        orElse(partial.enableGcmCipherSuites(), services.defaultEnableGcmCipherSuites()))
                .fallbackToCommonNameVerification(orElse(
                        partial.fallbackToCommonNameVerification(),
                        services.defaultFallbackToCommonNameVerification()))
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
