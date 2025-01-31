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

import com.google.common.collect.ImmutableList;
import com.palantir.logsafe.SafeArg;
import com.palantir.logsafe.exceptions.SafeIllegalArgumentException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.immutables.value.Value;

/**
 * Constructs, validates, and formats a canonical User-Agent header. Because the http header spec
 * (https://www.w3.org/Protocols/rfc2616/rfc2616-sec4.html#sec4.2) requires headers to be joined on commas, individual
 * {@link Agent} header strings must never contain commas.
 */
@Value.Immutable
@ImmutablesStyle
public interface UserAgent {

    /** Identifies the node (e.g., IP address, container identifier, etc) on which this user agent was constructed. */
    @Value.Lazy
    default Optional<String> nodeId() {
        if (primary().comments().isEmpty()) {
            // fast path to avoid stream overhead
            return Optional.empty();
        }
        return primary().comments().stream()
                .filter(item -> item.startsWith("nodeId:"))
                .map(item -> item.substring(7).trim())
                .filter(UserAgents::isValidNodeId)
                .findFirst();
    }

    /** The primary user agent, typically the name/version of the service initiating an RPC call. */
    Agent primary();

    /**
     * A list of additional libraries that participate (client-side) in the RPC call, for instance RPC libraries, API
     * JARs, etc.
     */
    List<Agent> informational();

    /** Creates a new {@link UserAgent} with the given {@link #primary} agent and originating node id. */
    static UserAgent of(Agent agent, String nodeId) {
        UserAgents.checkNodeId(nodeId);
        List<String> comments = Stream.concat(
                        agent.comments().stream().filter(item -> !item.startsWith("nodeId:")),
                        Stream.of("nodeId:" + nodeId))
                .toList();
        return ImmutableUserAgent.builder()
                .primary(Agent.of(agent.name(), agent.version(), comments))
                .build();
    }

    /**
     * Like {@link #of(Agent, String)}, but with an empty/unknown node id. Users should generally prefer the version
     * with explicit node in order to facilitate server-side client trackingb
     */
    static UserAgent of(Agent agent) {
        return ImmutableUserAgent.builder().primary(agent).build();
    }

    /**
     * Returns a user agent with the given base agent combined with specified additional informational agents.
     */
    static UserAgent of(UserAgent base, Iterable<Agent> additional) {
        return ImmutableUserAgent.builder()
                .from(base)
                .addAllInformational(additional)
                .build();
    }

    /**
     * Returns a new {@link UserAgent} instance whose {@link #informational} agents are this instance's agents plus the
     * given agent.
     */
    default UserAgent addAgent(Agent agent) {
        return ImmutableUserAgent.builder().from(this).addInformational(agent).build();
    }

    /** Specifies an agent that participates (client-side) in an RPC call in terms of its name and version. */
    @Value.Immutable
    @ImmutablesStyle
    interface Agent {
        String DEFAULT_VERSION = "0.0.0";

        String name();

        String version();

        List<String> comments();

        @Value.Check
        default void check() {
            if (!UserAgents.isValidName(name())) {
                throw new SafeIllegalArgumentException("Illegal agent name format", SafeArg.of("name", name()));
            }
            if (!UserAgents.isValidVersion(version())) {
                // Should never hit the following.
                throw new SafeIllegalArgumentException(
                        "Illegal version format. This is a bug", SafeArg.of("version", version()));
            }
        }

        static Agent of(String name, String version) {
            return ImmutableAgent.builder()
                    .name(name)
                    .version(UserAgents.isValidVersion(version) ? version : DEFAULT_VERSION)
                    .build();
        }

        static Agent of(String name, String version, Iterable<String> comments) {
            ImmutableList<String> immutableComments = ImmutableList.copyOf(comments);
            for (int i = 0; i < immutableComments.size(); i++) {
                String comment = immutableComments.get(i);
                UserAgents.checkComment(comment);
            }
            return ImmutableAgent.builder()
                    .name(name)
                    .version(UserAgents.isValidVersion(version) ? version : DEFAULT_VERSION)
                    .comments(immutableComments)
                    .build();
        }
    }
}
