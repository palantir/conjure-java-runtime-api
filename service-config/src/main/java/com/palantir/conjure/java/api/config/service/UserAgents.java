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

import com.palantir.logsafe.Preconditions;
import com.palantir.logsafe.SafeArg;
import com.palantir.logsafe.exceptions.SafeIllegalArgumentException;
import com.palantir.logsafe.logger.SafeLogger;
import com.palantir.logsafe.logger.SafeLoggerFactory;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class UserAgents {

    /** The {@link UserAgent.Agent#name agent name} identifying the conjure-java library in a {@link UserAgent}. */
    public static final String CONJURE_AGENT_NAME = "conjure-java-runtime";

    private static final SafeLogger log = SafeLoggerFactory.get(UserAgents.class);

    // performance note: isValidName uses hand-rolled parser to validate name effectively matches NAME_REGEX
    // visible for testing compatibility
    static final Pattern NAME_REGEX = Pattern.compile("[a-zA-Z][a-zA-Z0-9\\-]*");
    private static final Pattern LENIENT_VERSION_REGEX = Pattern.compile("[0-9a-z.-]+");
    private static final Pattern NODE_REGEX = Pattern.compile("[a-zA-Z0-9][a-zA-Z0-9.\\-]*");
    private static final Pattern VERSION_REGEX =
            Pattern.compile("^[0-9]+(\\.[0-9]+)*(-rc[0-9]+)?(-[0-9]+-g[a-f0-9]+)?$");
    private static final Pattern SEGMENT_PATTERN =
            Pattern.compile(String.format("(%s)/(%s)( \\((.+?)\\))?", NAME_REGEX, LENIENT_VERSION_REGEX));

    private UserAgents() {}

    /** Returns the canonical string format for the given {@link UserAgent}. */
    public static String format(UserAgent userAgent) {
        StringBuilder formatted = new StringBuilder(64); // preallocate larger buffer for longer agents
        formatSimpleAgent(userAgent.primary(), formatted);
        if (userAgent.nodeId().isPresent()) {
            formatted.append(" (nodeId:").append(userAgent.nodeId().get()).append(')');
        }
        for (UserAgent.Agent informationalAgent : userAgent.informational()) {
            formatted.append(' ');
            formatSimpleAgent(informationalAgent, formatted);
        }
        return formatted.toString();
    }

    private static void formatSimpleAgent(UserAgent.Agent agent, StringBuilder output) {
        output.ensureCapacity(
                output.length() + 1 + agent.name().length() + agent.version().length());
        output.append(agent.name()).append('/').append(agent.version());
    }

    /**
     * Parses the given string into a {@link UserAgent} or throws an {@link IllegalArgumentException} if no correctly
     * formatted primary user agent can be found. Incorrectly formatted informational agents are omitted.
     *
     * <p>Valid user agent strings loosely follow RFC 7230 (https://tools.ietf.org/html/rfc7230#section-3.2.6).
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

        Matcher matcher = SEGMENT_PATTERN.matcher(userAgent);
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
                if (log.isDebugEnabled()) {
                    log.debug(
                            "Invalid user agent '{}', falling back to default/unknown agent",
                            SafeArg.of("userAgent", userAgent));
                }
                return builder.primary(UserAgent.Agent.of("unknown", UserAgent.Agent.DEFAULT_VERSION))
                        .build();
            } else {
                throw new SafeIllegalArgumentException(
                        "Failed to parse user agent string", SafeArg.of("userAgent", userAgent));
            }
        }

        return builder.build();
    }

    private static Map<String, String> parseComments(String commentsString) {
        Map<String, String> comments = new HashMap<>();
        for (String comment : commentsString.split("[,;]")) {
            String[] fields = comment.split(":");
            if (fields.length == 2) {
                comments.put(fields[0], fields[1]);
            } else {
                comments.put(comment, comment);
            }
        }
        return comments;
    }

    static boolean isValidName(String name) {
        // hand rolled implementation of NAME_REGEX.matcher(name).matches() to avoid allocations
        // "[a-zA-Z][a-zA-Z0-9\\-]*"
        if (name == null || name.isEmpty()) {
            return false;
        }
        char ch = name.charAt(0);
        if (!isAlpha(ch)) {
            return false;
        }

        for (int i = 1; i < name.length(); i++) {
            ch = name.charAt(i);
            if (!isAlpha(ch) && !isNumeric(ch) && ch != '-') {
                return false;
            }
        }

        return true;
    }

    private static boolean isAlpha(char ch) {
        return ('a' <= ch && ch <= 'z') || ('A' <= ch && ch <= 'Z');
    }

    private static boolean isNumeric(char ch) {
        return '0' <= ch && ch <= '9';
    }

    static boolean isValidNodeId(String instanceId) {
        return NODE_REGEX.matcher(instanceId).matches();
    }

    static boolean isValidVersion(String version) {
        if (VersionParser.countNumericDotGroups(version) >= 2 // fast path for numeric & dot only version numbers
                || versionMatchesRegex(version)) {
            return true;
        }

        if (log.isDebugEnabled()) {
            log.debug("Encountered invalid user agent version '{}'", SafeArg.of("version", version));
        }

        return false;
    }

    // visible for benchmarking
    static boolean versionMatchesRegex(String version) {
        return VERSION_REGEX.matcher(version).matches();
    }
}
