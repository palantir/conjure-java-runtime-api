/*
 * (c) Copyright 2018 Palantir Technologies Inc. All rights reserved.
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

import com.palantir.logsafe.SafeArg;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class UserAgents {

    /**
     * The {@link UserAgent.Agent#name agent name} identifying the conjure-java library in a {@link UserAgent}.
     */
    public static final String REMOTING_AGENT_NAME = "conjure-java";

    private static final Logger log = LoggerFactory.getLogger(UserAgents.class);

    private static final Pattern NAME_REGEX = Pattern.compile("[a-zA-Z][a-zA-Z0-9\\-]*");
    private static final Pattern LENIENT_VERSION_REGEX = Pattern.compile("[0-9a-z.-]+");
    private static final Pattern NODE_REGEX = Pattern.compile("[a-zA-Z0-9][a-zA-Z0-9.\\-]*");
    private static final Pattern VERSION_REGEX =
            Pattern.compile("^[0-9]+(\\.[0-9]+)*(-rc[0-9]+)?(-[0-9]+-g[a-f0-9]+)?$");

    private UserAgents() {}

    /** Returns the canonical string format for the given {@link UserAgent}. */
    public static String format(UserAgent userAgent) {
        Map<String, String> primaryComments = new HashMap<>();
        if (userAgent.nodeId().isPresent()) {
            primaryComments.put("nodeId", userAgent.nodeId().get());
        }

        List<String> agents = new LinkedList<>();
        agents.add(formatSingleAgent(userAgent.primary(), primaryComments));

        return Stream.concat(agents.stream(),
                userAgent.informational().stream().map(a -> formatSingleAgent(a, Collections.emptyMap())))
                .map(Object::toString)
                .collect(Collectors.joining(" "));
    }

    /**
     * Formats the given agent in the form {@code name/version (key:value; key:value)}, where the ()-block of comments
     * is omitted if zero comments are provided.
     */
    private static String formatSingleAgent(UserAgent.Agent agent, Map<String, String> comments) {
        // TODO(rfink): Think about validation comments here? Must not contain special characters.
        StringBuilder formatted = new StringBuilder()
                .append(agent.name())
                .append("/")
                .append(agent.version());

        String formattedComments = comments.entrySet()
                .stream()
                .map(e -> e.getKey() + ":" + e.getValue())
                .collect(Collectors.joining(";"));
        if (!formattedComments.isEmpty()) {
            formatted.append(" (")
                    .append(formattedComments)
                    .append(')');
        }
        return formatted.toString();
    }

    /**
     * Parses the given string into a {@link UserAgent} or throws an {@link IllegalArgumentException} if no correctly
     * formatted primary user agent can be found. Incorrectly formatted informational agents are omitted.
     * <p>
     * Valid user agent strings loosely follow RFC 7230 (https://tools.ietf.org/html/rfc7230#section-3.2.6).
     */
    public static UserAgent parse(String userAgent) {
        Preconditions.checkNotNull(userAgent, "userAgent must not be null");
        return parseInternal(userAgent, false /* strict */);
    }

    /**
     * Like {@link #parse}, but never fails and returns the primary agent {@code unknown/0.0.0} if no valid primary
     * agent can be parsed.
     */
    public static UserAgent tryParse(String userAgent) {

        return parseInternal(userAgent == null ? "" : userAgent, true /* lenient */);
    }

    private static UserAgent parseInternal(String userAgent, boolean lenient) {
        ImmutableUserAgent.Builder builder = ImmutableUserAgent.builder();

        Pattern segmentPattern = Pattern.compile(
                String.format("(%s)/(%s)( \\((.+?)\\))?", NAME_REGEX, LENIENT_VERSION_REGEX));
        Matcher matcher = segmentPattern.matcher(userAgent);
        boolean foundFirst = false;
        while (matcher.find()) {
            String name = matcher.group(1);
            String version = matcher.group(2);
            Optional<String> comments = Optional.ofNullable(matcher.group(4));

            if (!foundFirst) {
                // primary
                builder.primary(UserAgent.Agent.of(name, version));
                comments.ifPresent(c -> {
                    Map<String, String> parsedComments = parseComments(c);
                    if (parsedComments.containsKey("nodeId")) {
                        builder.nodeId(parsedComments.get("nodeId"));
                    }
                });
            } else {
                // informational
                builder.addInformational(UserAgent.Agent.of(name, version));
            }

            foundFirst = true;
        }

        if (!foundFirst) {
            if (lenient) {
                log.debug("Invalid user agent, falling back to default/unknown agent",
                        SafeArg.of("userAgent", userAgent));
                return builder.primary(UserAgent.Agent.of("unknown", UserAgent.Agent.DEFAULT_VERSION)).build();
            } else {
                throw new IllegalArgumentException("Failed to parse user agent string: " + userAgent);
            }
        }

        return builder.build();
    }

    private static Map<String, String> parseComments(String commentsString) {
        Map<String, String> comments = new HashMap<>();
        for (String comment : commentsString.split(",;")) {
            String[] fields = comment.split(":");
            if (fields.length == 0) {
                // continue
            } else if (fields.length == 2) {
                comments.put(fields[0], fields[1]);
            } else {
                comments.put(comment, comment);
            }
        }
        return comments;
    }

    static boolean isValidName(String name) {
        return NAME_REGEX.matcher(name).matches();
    }

    static boolean isValidNodeId(String instanceId) {
        return NODE_REGEX.matcher(instanceId).matches();
    }

    static boolean isValidVersion(String version) {
        if (VERSION_REGEX.matcher(version).matches()) {
            return true;
        }

        log.warn("Encountered invalid user agent version", SafeArg.of("version", version));
        return false;
    }
}
