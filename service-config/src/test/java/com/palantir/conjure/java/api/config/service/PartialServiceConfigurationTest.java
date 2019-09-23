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

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.palantir.conjure.java.api.config.ssl.SslConfiguration;
import com.palantir.conjure.java.api.ext.jackson.ObjectMappers;
import com.palantir.tokens.auth.BearerToken;
import java.nio.file.Paths;
import java.util.Optional;
import org.junit.jupiter.api.Test;

public final class PartialServiceConfigurationTest {

    public static final ObjectMapper mapper = ObjectMappers.newClientObjectMapper();

    @Test
    public void serDe() throws Exception {
        PartialServiceConfiguration serialized = PartialServiceConfiguration.builder()
                .apiToken(BearerToken.valueOf("bearerToken"))
                .security(SslConfiguration.of(Paths.get("truststore.jks")))
                .connectTimeout(HumanReadableDuration.days(1))
                .readTimeout(HumanReadableDuration.days(1))
                .writeTimeout(HumanReadableDuration.days(1))
                .maxNumRetries(Optional.of(5))
                .backoffSlotSize(HumanReadableDuration.days(1))
                .addUris("uri1")
                .proxyConfiguration(ProxyConfiguration.of("host:80"))
                .build();
        String camelCase = "{\"apiToken\":\"bearerToken\",\"security\":"
                + "{\"trustStorePath\":\"truststore.jks\",\"trustStoreType\":\"JKS\",\"keyStorePath\":null,"
                + "\"keyStorePassword\":null,\"keyStoreType\":\"JKS\",\"keyStoreKeyAlias\":null},\"uris\":[\"uri1\"],"
                + "\"connectTimeout\":\"1 day\",\"readTimeout\":\"1 day\",\"writeTimeout\":\"1 day\","
                + "\"maxNumRetries\":5,\"backoffSlotSize\":\"1 day\","
                + "\"enableGcmCipherSuites\":null,\"fallbackToCommonNameVerification\":null,"
                + "\"proxyConfiguration\":{\"hostAndPort\":\"host:80\",\"credentials\":null,"
                + "\"type\":\"HTTP\"}}";
        String kebabCase = "{\"api-token\":\"bearerToken\",\"security\":"
                + "{\"trust-store-path\":\"truststore.jks\",\"trust-store-type\":\"JKS\",\"key-store-path\":null,"
                + "\"key-store-password\":null,\"key-store-type\":\"JKS\",\"key-store-key-alias\":null},"
                + "\"connect-timeout\":\"1 day\",\"read-timeout\":\"1 day\",\"write-timeout\":\"1 day\","
                + "\"max-num-retries\":5,\"backoff-slot-size\":\"1 day\","
                + "\"uris\":[\"uri1\"],\"proxy-configuration\":{\"host-and-port\":\"host:80\",\"credentials\":null},"
                + "\"enable-gcm-cipher-suites\":null,\"fallback-to-common-name-verification\":null}";

        assertThat(mapper.writeValueAsString(serialized)).isEqualTo(camelCase);
        assertThat(mapper.readValue(camelCase, PartialServiceConfiguration.class)).isEqualTo(serialized);
        assertThat(mapper.readValue(kebabCase, PartialServiceConfiguration.class)).isEqualTo(serialized);
    }

    @Test
    public void serDe_optional() throws Exception {
        PartialServiceConfiguration serialized = PartialServiceConfiguration.builder().build();
        String camelCase = "{\"apiToken\":null,\"security\":null,\"uris\":[],\"connectTimeout\":null,"
                + "\"readTimeout\":null,\"writeTimeout\":null,\"maxNumRetries\":null,\"backoffSlotSize\":null,"
                + "\"enableGcmCipherSuites\":null,\"fallbackToCommonNameVerification\":null,"
                + "\"proxyConfiguration\":null}";
        String kebabCase = "{\"api-token\":null,\"security\":null,\"connect-timeout\":null,"
                + "\"read-timeout\":null,\"write-timeout\":null,\"max-num-retries\":null,\"backoff-slot-size\":null,"
                + "\"enable-gcm-cipher-suites\":null,\"fallback-to-common-name-verification\":null,"
                + "\"uris\":[],\"proxy-configuration\":null}";

        assertThat(ObjectMappers.newClientObjectMapper().writeValueAsString(serialized)).isEqualTo(camelCase);
        assertThat(mapper.readValue(camelCase, PartialServiceConfiguration.class)).isEqualTo(serialized);
        assertThat(mapper.readValue(kebabCase, PartialServiceConfiguration.class)).isEqualTo(serialized);
    }
}
