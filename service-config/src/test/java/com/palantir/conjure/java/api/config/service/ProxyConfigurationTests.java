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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.google.common.io.Resources;
import com.palantir.conjure.java.api.ext.jackson.ObjectMappers;
import java.io.IOException;
import java.net.URL;
import org.junit.jupiter.api.Test;

@SuppressWarnings("CheckReturnValue") // .build() is used to throw validation exceptions
public final class ProxyConfigurationTests {
    private final ObjectMapper mapper = new ObjectMapper(new YAMLFactory()).registerModule(new Jdk8Module());

    @Test
    public void testDeserializationWithoutCredentials() throws IOException {
        URL resource = Resources.getResource("configs/proxy-config-without-credentials.yml");
        ProxyConfiguration actualProxyConfiguration = mapper.readValue(resource.openStream(), ProxyConfiguration.class);

        ProxyConfiguration expectedProxyConfiguration = ProxyConfiguration.of("squid:3128");
        assertThat(expectedProxyConfiguration).isEqualTo(actualProxyConfiguration);
    }

    @Test
    public void testDeserializationWithCredentials() throws IOException {
        URL resource = Resources.getResource("configs/proxy-config-with-credentials.yml");
        ProxyConfiguration actualProxyConfiguration = mapper.readValue(resource.openStream(), ProxyConfiguration.class);

        ProxyConfiguration expectedProxyConfiguration =
                ProxyConfiguration.of("squid:3128", BasicCredentials.of("username", "password"));

        assertThat(expectedProxyConfiguration).isEqualTo(actualProxyConfiguration);
    }

    @Test
    public void testDeserializationDirect() throws Exception {
        URL resource = Resources.getResource("configs/proxy-config-direct.yml");
        ProxyConfiguration config = mapper.readValue(resource, ProxyConfiguration.class);
        assertThat(config).isEqualTo(ProxyConfiguration.DIRECT);
    }

    @Test
    public void testDeserializationMesh() throws Exception {
        URL resource = Resources.getResource("configs/proxy-config-mesh.yml");
        ProxyConfiguration config = mapper.readValue(resource, ProxyConfiguration.class);
        assertThat(config).isEqualTo(ProxyConfiguration.builder()
                .type(ProxyConfiguration.Type.MESH)
                .hostAndPort("localhost:123")
                .build());
    }

    @Test
    public void testNonHttpProxyWithHostAndPort() {
        assertThatThrownBy(() -> new ProxyConfiguration.Builder()
                        .hostAndPort("squid:3128")
                        .type(ProxyConfiguration.Type.DIRECT)
                        .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Neither credential nor host-and-port may be configured for DIRECT proxies");
    }

    @Test
    public void credentialsWithNonHttp() {
        assertThatThrownBy(() -> ProxyConfiguration.builder()
                        .credentials(BasicCredentials.of("foo", "bar"))
                        .type(ProxyConfiguration.Type.DIRECT)
                        .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Neither credential nor host-and-port may be configured for DIRECT proxies");
        assertThatThrownBy(() -> ProxyConfiguration.builder()
                        .type(ProxyConfiguration.Type.MESH)
                        .credentials(BasicCredentials.of("foo", "bar"))
                        .hostAndPort("localhost:1234")
                        .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("credentials only valid for HTTP proxies");
    }

    @Test
    public void serDe() throws Exception {
        ProxyConfiguration config = ProxyConfiguration.of("host:80", BasicCredentials.of("username", "password"));
        String camelCase = "{\"hostAndPort\":\"host:80\",\"credentials\":{\"username\":\"username\","
                + "\"password\":\"password\"},\"type\":\"HTTP\"}";
        String kebabCase = "{\"host-and-port\":\"host:80\",\"credentials\":{\"username\":\"username\","
                + "\"password\":\"password\"},\"type\":\"HTTP\"}";

        assertThat(ObjectMappers.newClientObjectMapper().writeValueAsString(config)).isEqualTo(camelCase);
        assertThat(ObjectMappers.newClientObjectMapper().readValue(camelCase, ProxyConfiguration.class))
                .isEqualTo(config);
        assertThat(ObjectMappers.newClientObjectMapper().readValue(kebabCase, ProxyConfiguration.class))
                .isEqualTo(config);
    }
}
