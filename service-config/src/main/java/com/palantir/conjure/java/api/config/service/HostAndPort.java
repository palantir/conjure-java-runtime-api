/*
 * (c) Copyright 2017 Palantir Technologies Inc. All rights reserved.
 * Adapted from Guava 18.0 under Apache 2 license.
 */

/*
 * Copyright (C) 2011 The Guava Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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

/** See Guava's {@code HostAndPort}. */
public final class HostAndPort {
    /** Magic value indicating the absence of a port number. */
    private static final int NO_PORT = -1;

    /** Hostname, IPv4/IPv6 literal, or unvalidated nonsense. */
    private final String host;

    /** Validated port number in the range [0..65535], or NO_PORT. */
    private final int port;

    private HostAndPort(String host, int port) {
        this.host = host;
        this.port = port;
    }

    /** Return true if this instance has a defined port. */
    boolean hasPort() {
        return port >= 0;
    }

    public static HostAndPort fromString(String hostPortString) {
        Preconditions.checkNotNull(hostPortString, "hostPortString");
        String host;
        String portString = null;

        if (hostPortString.startsWith("[")) {
            String[] hostAndPort = getHostAndPortFromBracketedHost(hostPortString);
            host = hostAndPort[0];
            portString = hostAndPort[1];
        } else {
            int colonPos = hostPortString.indexOf(':');
            if (colonPos >= 0 && hostPortString.indexOf(':', colonPos + 1) == -1) {
                // Exactly 1 colon.  Split into host:port.
                host = hostPortString.substring(0, colonPos);
                portString = hostPortString.substring(colonPos + 1);
            } else {
                // 0 or 2+ colons.  Bare hostname or IPv6 literal.
                host = hostPortString;
            }
        }

        int port = NO_PORT;
        if (portString != null && portString.length() > 0) {
            // Try to parse the whole port string as a number.
            // JDK7 accepts leading plus signs. We don't want to.
            Preconditions.checkArgument(!portString.startsWith("+"), "Unparseable port number",
                    SafeArg.of("port", hostPortString));
            try {
                port = Integer.parseInt(portString);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Unparseable port number: " + hostPortString);
            }
            Preconditions.checkArgument(isValidPort(port), "Port number out of range",
                    SafeArg.of("port", hostPortString));
        }

        return new HostAndPort(host, port);
    }

    private static String[] getHostAndPortFromBracketedHost(String hostPortString) {
        int colonIndex;
        int closeBracketIndex;
        Preconditions.checkArgument(hostPortString.charAt(0) == '[',
                "Bracketed host-port string must start with a bracket",  SafeArg.of("port", hostPortString));
        colonIndex = hostPortString.indexOf(':');
        closeBracketIndex = hostPortString.lastIndexOf(']');
        Preconditions.checkArgument(colonIndex > -1 && closeBracketIndex > colonIndex,
                "Invalid bracketed host/port",  SafeArg.of("port", hostPortString));

        String host = hostPortString.substring(1, closeBracketIndex);
        if (closeBracketIndex + 1 == hostPortString.length()) {
            return new String[] {host, ""};
        } else {
            Preconditions.checkArgument(hostPortString.charAt(closeBracketIndex + 1) == ':',
                    "Only a colon may follow a close bracket",  SafeArg.of("port", hostPortString));
            for (int i = closeBracketIndex + 2; i < hostPortString.length(); ++i) {
                Preconditions.checkArgument(Character.isDigit(hostPortString.charAt(i)),
                        "Port must be numeric",  SafeArg.of("port", hostPortString));
            }
            return new String[] {host, hostPortString.substring(closeBracketIndex + 2)};
        }
    }

    /** Rebuild the host:port string, including brackets if necessary. */
    @Override
    public String toString() {
        // "[]:12345" requires 8 extra bytes.
        StringBuilder builder = new StringBuilder(host.length() + 8);
        if (host.indexOf(':') >= 0) {
            builder.append('[').append(host).append(']');
        } else {
            builder.append(host);
        }
        if (hasPort()) {
            builder.append(':').append(port);
        }
        return builder.toString();
    }

    /** Return true for valid port numbers. */
    private static boolean isValidPort(int port) {
        return port >= 0 && port <= 65535;
    }
}
