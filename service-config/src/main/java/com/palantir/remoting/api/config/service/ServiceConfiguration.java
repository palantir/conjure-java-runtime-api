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

import com.palantir.remoting.api.config.ssl.SslConfiguration;
import com.palantir.tokens2.auth.BearerToken;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import org.immutables.value.Value;

/** A fully-instantiated variant of {@link PartialServiceConfiguration}. */
@Value.Immutable
@ImmutablesStyle
public interface ServiceConfiguration {

    Optional<BearerToken> apiToken();

    SslConfiguration security();

    List<String> uris();

    Duration connectTimeout();

    Duration readTimeout();

    Duration writeTimeout();

    boolean enableGcmCipherSuites();

    ProxyConfiguration proxy();

    int maxNumRetries();
}
