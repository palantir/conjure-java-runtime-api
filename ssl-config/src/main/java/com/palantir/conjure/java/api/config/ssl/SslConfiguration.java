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

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.collect.ImmutableSet;
import com.palantir.logsafe.exceptions.SafeIllegalArgumentException;
import java.nio.file.Path;
import java.util.Optional;
import org.immutables.value.Value;

@JsonDeserialize(builder = SslConfiguration.Builder.class)
@Value.Immutable
@ImmutablesStyle
public abstract class SslConfiguration {

    private static final ImmutableSet<String> PEM_EXTENSIONS = ImmutableSet.of("key", "pem", "cer", "crt");

    public enum StoreType {
        JKS,
        PEM,
        PKCS12,
        PUPPET
    }

    @JsonAlias("trust-store-path")
    public abstract Path trustStorePath();

    @SuppressWarnings("checkstyle:designforextension")
    @Value.Default
    @JsonAlias("trust-store-type")
    public StoreType trustStoreType() {
        if (PEM_EXTENSIONS.stream().anyMatch(ext -> trustStorePath().getFileName().toString().endsWith(ext))) {
            return StoreType.PEM;
        } else {
            return StoreType.JKS;
        }
    }

    @JsonAlias("key-store-path")
    public abstract Optional<Path> keyStorePath();

    @JsonAlias("key-store-password")
    @Value.Redacted
    public abstract Optional<String> keyStorePassword();

    @SuppressWarnings("checkstyle:designforextension")
    @Value.Default
    @JsonAlias("key-store-type")
    public StoreType keyStoreType() {
        if (keyStorePath().map(keyStore -> PEM_EXTENSIONS.stream()
                .anyMatch(ext -> keyStore.getFileName().toString().endsWith(ext)))
                .orElse(false)) {
            return StoreType.PEM;
        } else {
            return StoreType.JKS;
        }
    }

    @JsonAlias("key-store-key-alias")
    /** Alias of the key that should be used in the key store. If absent, first entry returned by key store is used. */
    public abstract Optional<String> keyStoreKeyAlias();

    @Value.Check
    protected final void check() {
        if (keyStorePath().isPresent() && keyStoreType().equals(StoreType.JKS) && !keyStorePassword().isPresent()) {
            throw new SafeIllegalArgumentException(
                    "keyStorePassword must be present if keyStoreType is JKS");
        }
        if (keyStoreKeyAlias().isPresent() && !keyStorePath().isPresent()) {
            throw new SafeIllegalArgumentException(
                    "keyStorePath must be present if keyStoreKeyAlias is present");
        }
    }

    public static SslConfiguration of(Path trustStorePath) {
        return SslConfiguration.builder().trustStorePath(trustStorePath).build();
    }

    public static SslConfiguration of(Path trustStorePath, Path keyStorePath, String keyStorePassword) {
        return SslConfiguration.builder()
                .trustStorePath(trustStorePath)
                .keyStorePath(keyStorePath)
                .keyStorePassword(keyStorePassword)
                .build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder extends ImmutableSslConfiguration.Builder {}
}
