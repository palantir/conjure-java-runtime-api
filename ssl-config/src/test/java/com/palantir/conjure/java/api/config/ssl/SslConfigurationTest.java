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

package com.palantir.conjure.java.api.config.ssl;

import static com.palantir.logsafe.testing.Assertions.assertThatLoggableExceptionThrownBy;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.palantir.conjure.java.api.ext.jackson.ObjectMappers;
import com.palantir.logsafe.exceptions.SafeIllegalArgumentException;
import java.nio.file.Paths;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

public final class SslConfigurationTest {

    private static final ObjectMapper MAPPER = ObjectMappers.newClientObjectMapper();

    @Test
    public void testSerDe() throws Exception {
        SslConfiguration serialized = SslConfiguration.builder()
                .trustStorePath(Paths.get("truststore.jks"))
                .trustStoreType(SslConfiguration.StoreType.JKS)
                .keyStorePath(Paths.get("keystore.jks"))
                .keyStoreType(SslConfiguration.StoreType.JKS)
                .keyStoreKeyAlias("alias")
                .keyStorePassword("password")
                .build();
        String deserializedCamelCase = "{\"trustStorePath\":\"truststore.jks\",\"trustStoreType\":\"JKS\","
                + "\"trustDefaultCas\":true,\"keyStorePath\":\"keystore.jks\",\"keyStorePassword\":\"password\","
                + "\"keyStoreType\":\"JKS\",\"keyStoreKeyAlias\":\"alias\"}";
        String deserializedKebabCase = "{\"trust-store-path\":\"truststore.jks\",\"trust-store-type\":\"JKS\","
                + "\"key-store-path\":\"keystore.jks\",\"key-store-password\":\"password\","
                + "\"key-store-type\":\"JKS\",\"key-store-key-alias\":\"alias\"}";

        assertThat(MAPPER.writeValueAsString(serialized)).isEqualTo(deserializedCamelCase);
        assertThat(MAPPER.readValue(deserializedCamelCase, SslConfiguration.class))
                .isEqualTo(serialized);
        assertThat(MAPPER.readValue(deserializedKebabCase, SslConfiguration.class))
                .isEqualTo(serialized);
    }

    @Test
    public void serDe_optional() throws Exception {
        SslConfiguration serialized = SslConfiguration.of(Paths.get("trustStore.jks"));
        String deserializedCamelCase = "{\"trustStorePath\":\"trustStore.jks\",\"trustStoreType\":\"JKS\","
                + "\"trustDefaultCas\":true,\"keyStorePath\":null,\"keyStorePassword\":null,"
                + "\"keyStoreType\":\"JKS\",\"keyStoreKeyAlias\":null}";
        String deserializedKebabCase = "{\"trust-store-path\":\"trustStore.jks\",\"trust-store-type\":\"JKS\","
                + "\"key-store-path\":null,\"key-store-password\":null,\"key-store-type\":\"JKS\","
                + "\"key-store-key-alias\":null}";

        assertThat(MAPPER.writeValueAsString(serialized)).isEqualTo(deserializedCamelCase);
        assertThat(MAPPER.readValue(deserializedCamelCase, SslConfiguration.class))
                .isEqualTo(serialized);
        assertThat(MAPPER.readValue(deserializedKebabCase, SslConfiguration.class))
                .isEqualTo(serialized);
    }

    @Test
    public void trustDefaultCasDefaultsToTrue() {
        assertThat(SslConfiguration.builder()
                        .trustStorePath(Paths.get("truststore.jks"))
                        .build()
                        .trustDefaultCas())
                .isTrue();
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    public void trustDefaultCasExplicitBuilderValue(boolean trustDefaultCas) {
        assertThat(SslConfiguration.builder()
                        .trustStorePath(Paths.get("truststore.jks"))
                        .trustDefaultCas(trustDefaultCas)
                        .build()
                        .trustDefaultCas())
                .isEqualTo(trustDefaultCas);
    }

    @ParameterizedTest
    @ValueSource(strings = {"trustStorePath", "trust-store-path"})
    public void trustDefaultCasDefaultsToTrueWhenOmittedFromJson(String trustStoreProperty) throws Exception {
        SslConfiguration configuration =
                MAPPER.readValue("{\"" + trustStoreProperty + "\":\"truststore.jks\"}", SslConfiguration.class);

        assertThat(configuration.trustDefaultCas()).isTrue();
        assertThat(configuration).isEqualTo(SslConfiguration.of(Paths.get("truststore.jks")));
    }

    @ParameterizedTest
    @CsvSource({"trustDefaultCas, true", "trustDefaultCas, false", "trust-default-cas, true", "trust-default-cas, false"
    })
    public void trustDefaultCasExplicitJsonValue(String property, boolean trustDefaultCas) throws Exception {
        SslConfiguration configuration = MAPPER.readValue(
                "{\"trustStorePath\":\"truststore.jks\",\"" + property + "\":" + trustDefaultCas + "}",
                SslConfiguration.class);

        assertThat(configuration.trustDefaultCas()).isEqualTo(trustDefaultCas);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    public void trustDefaultCasSerializationRoundTrip(boolean trustDefaultCas) throws Exception {
        SslConfiguration configuration = SslConfiguration.builder()
                .trustStorePath(Paths.get("truststore.jks"))
                .trustDefaultCas(trustDefaultCas)
                .build();

        assertThat(MAPPER.readValue(MAPPER.writeValueAsBytes(configuration), SslConfiguration.class))
                .isEqualTo(configuration);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    public void trustDefaultCasBuilderFromPreservesConfiguration(boolean trustDefaultCas) {
        SslConfiguration configuration = SslConfiguration.builder()
                .trustStorePath(Paths.get("truststore.p12"))
                .trustStoreType(SslConfiguration.StoreType.PKCS12)
                .trustDefaultCas(trustDefaultCas)
                .keyStorePath(Paths.get("keystore.p12"))
                .keyStoreType(SslConfiguration.StoreType.PKCS12)
                .keyStorePassword("password")
                .keyStoreKeyAlias("alias")
                .build();

        assertThat(SslConfiguration.builder()
                        .trustDefaultCas(!trustDefaultCas)
                        .from(configuration)
                        .build())
                .isEqualTo(configuration);
    }

    @Test
    public void jksKeystorePassword() {
        assertThatLoggableExceptionThrownBy(() -> SslConfiguration.builder()
                        .trustStorePath(Paths.get("truststore.jks"))
                        .keyStorePath(Paths.get("keystore.jks"))
                        .keyStoreType(SslConfiguration.StoreType.JKS)
                        .build())
                .isInstanceOf(SafeIllegalArgumentException.class)
                .hasLogMessage("keyStorePassword must be present if keyStoreType is JKS")
                .hasNoArgs();
    }

    @Test
    public void keystorePasswordWithoutPath() {
        assertThatLoggableExceptionThrownBy(() -> SslConfiguration.builder()
                        .trustStorePath(Paths.get("truststore.jks"))
                        .keyStoreType(SslConfiguration.StoreType.PEM)
                        .keyStorePassword("password")
                        .build())
                .isInstanceOf(SafeIllegalArgumentException.class)
                .hasLogMessage("keyStorePath must be present if a keyStorePassword is provided")
                .hasNoArgs();
    }

    @Test
    public void nonJksKeystorePassword() {
        assertThatCode(() -> SslConfiguration.builder()
                        .trustStorePath(Paths.get("truststore.jks"))
                        .keyStorePath(Paths.get("key.pem"))
                        .keyStoreType(SslConfiguration.StoreType.PEM)
                        .build())
                .doesNotThrowAnyException();
    }

    @Test
    public void testDefaultTypeIsPem() {
        SslConfiguration sslConfiguration = SslConfiguration.builder()
                .trustStorePath(Paths.get("cert.cer"))
                .keyStorePath(Paths.get("key.pem"))
                .keyStorePassword("password")
                .build();
        assertThat(sslConfiguration.trustStoreType()).isEqualTo(SslConfiguration.StoreType.PEM);
        assertThat(sslConfiguration.keyStoreType()).isEqualTo(SslConfiguration.StoreType.PEM);
    }

    @Test
    public void testDefaultTypeIsJks() {
        SslConfiguration sslConfiguration = SslConfiguration.of(Paths.get("truststore.jks"));
        assertThat(sslConfiguration.trustStoreType()).isEqualTo(SslConfiguration.StoreType.JKS);
        assertThat(sslConfiguration.keyStoreType()).isEqualTo(SslConfiguration.StoreType.JKS);
    }

    @Test
    public void testDefaultType() {
        SslConfiguration sslConfiguration = SslConfiguration.builder()
                .trustStorePath(Paths.get("cert.cer"))
                .keyStorePath(Paths.get("keystore.jks"))
                .keyStorePassword("password")
                .build();
        assertThat(sslConfiguration.trustStoreType()).isEqualTo(SslConfiguration.StoreType.PEM);
        assertThat(sslConfiguration.keyStoreType()).isEqualTo(SslConfiguration.StoreType.JKS);
    }
}
