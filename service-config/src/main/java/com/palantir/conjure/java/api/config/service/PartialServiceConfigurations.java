/*
 * (c) Copyright 2020 Palantir Technologies Inc. All rights reserved.
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

/**
 * Utility class to create {@link PartialServiceConfiguration} objects.
 */
public final class PartialServiceConfigurations {

    private PartialServiceConfigurations() {}

    /**
     * Creates a {@link PartialServiceConfiguration} object out of a {@link ServiceConfiguration} one.
     */
    public static PartialServiceConfiguration fromServiceConfiguration(ServiceConfiguration serviceConfiguration) {
        return builderFromServiceConfiguration(serviceConfiguration).build();
    }

    /**
     * Creates a {@link PartialServiceConfiguration.Builder} from a {@link ServiceConfiguration}.
     */
    public static PartialServiceConfiguration.Builder builderFromServiceConfiguration(
            ServiceConfiguration serviceConfiguration) {
        return PartialServiceConfiguration.builder()
                .apiToken(serviceConfiguration.apiToken())
                .backoffSlotSize(serviceConfiguration.backoffSlotSize().map(HumanReadableDuration::fromJavaDuration))
                .connectTimeout(serviceConfiguration.connectTimeout().map(HumanReadableDuration::fromJavaDuration))
                .enableGcmCipherSuites(serviceConfiguration.enableGcmCipherSuites())
                .enableHttp2(serviceConfiguration.enableHttp2())
                .fallbackToCommonNameVerification(serviceConfiguration.fallbackToCommonNameVerification())
                .maxNumRetries(serviceConfiguration.maxNumRetries())
                .proxyConfiguration(serviceConfiguration.proxy())
                .readTimeout(serviceConfiguration.readTimeout().map(HumanReadableDuration::fromJavaDuration))
                .security(serviceConfiguration.security())
                .uris(serviceConfiguration.uris())
                .writeTimeout(serviceConfiguration.writeTimeout().map(HumanReadableDuration::fromJavaDuration));
    }
}
