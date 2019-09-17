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

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.palantir.logsafe.Preconditions;
import com.palantir.logsafe.SafeArg;
import com.palantir.logsafe.exceptions.SafeIllegalStateException;
import java.util.Optional;
import org.immutables.value.Value;
import org.immutables.value.Value.Immutable;

@Immutable
@JsonSerialize(as = ImmutableProxyConfiguration.class)
@JsonDeserialize(builder = ProxyConfiguration.Builder.class)
@ImmutablesStyle
public abstract class ProxyConfiguration {

    public static final ProxyConfiguration DIRECT = new ProxyConfiguration.Builder().type(Type.DIRECT).build();

    public enum Type {

        /** Use a direct connection. This option will bypass any JVM-level configured proxy settings. */
        DIRECT,

        /**
         * Use an http-proxy specified by {@link ProxyConfiguration#hostAndPort()}  and (optionally) {@link
         * ProxyConfiguration#credentials()}.
         */
        HTTP,

        /**
         * Redirects requests to the {@link #hostAndPort} and sets the HTTP Host header to the original request's
         * authority.
         */
        MESH
    }

    /**
     * The hostname and port of the HTTP/HTTPS Proxy. Recognized formats include those recognized by {@code
     * com.google.common.net.HostAndPort}, for instance {@code foo.com:80}, {@code 192.168.3.100:8080}, etc.
     */
    @JsonProperty("hostAndPort")
    @JsonAlias("host-and-port")
    public abstract Optional<String> hostAndPort();

    /**
     * Credentials if the proxy needs authentication.
     */
    public abstract Optional<BasicCredentials> credentials();

    /**
     * The type of Proxy. Defaults to {@link Type#HTTP}.
     * <p>
     * TODO(rfink): Would be great to make this field required, but that's a config break.
     */
    @Value.Default
    @SuppressWarnings("checkstyle:designforextension")
    @JsonProperty("type")
    public Type type() {
        return Type.HTTP;
    }

    @Value.Check
    protected final void check() {
        switch (type()) {
            case MESH:
            case HTTP:
                Preconditions.checkArgument(hostAndPort().isPresent(),
                        "host-and-port must be "
                                + "configured for an HTTP proxy");
                HostAndPort host = HostAndPort.fromString(hostAndPort().get());
                Preconditions.checkArgument(host.hasPort(),
                        "Given hostname does not contain a port number",
                        SafeArg.of("hostname", host));
                break;
            case DIRECT:
                Preconditions.checkArgument(!hostAndPort().isPresent() && !credentials().isPresent(),
                        "Neither credential nor host-and-port may be configured for DIRECT proxies");
                break;
            default:
                throw new SafeIllegalStateException("Unrecognized case; this is a library bug");
        }

        if (credentials().isPresent()) {
            Preconditions.checkArgument(type() == Type.HTTP, "credentials only valid for HTTP proxies");
        }
    }

    public static ProxyConfiguration of(String hostAndPort) {
        return builder().hostAndPort(hostAndPort).build();
    }

    public static ProxyConfiguration of(String hostAndPort, BasicCredentials credentials) {
        return builder().hostAndPort(hostAndPort).credentials(credentials).build();
    }

    public static ProxyConfiguration mesh(String hostAndPort) {
        return builder().type(Type.MESH).hostAndPort(hostAndPort).build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder extends ImmutableProxyConfiguration.Builder {}
}
