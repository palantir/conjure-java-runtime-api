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

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.palantir.logsafe.Preconditions;
import com.palantir.logsafe.Safe;
import com.palantir.logsafe.SafeArg;
import com.palantir.logsafe.exceptions.SafeIllegalArgumentException;
import com.palantir.logsafe.logger.SafeLogger;
import com.palantir.logsafe.logger.SafeLoggerFactory;
import java.util.List;
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
            Pattern.compile("^[0-9]+(?:\\.[0-9]+)*(?:-rc[0-9]+)?(?:-[0-9]+-g[a-f0-9]+)?$");
    private static final Pattern SEGMENT_PATTERN =
            Pattern.compile(String.format("(%s)/(%s)( \\((.+?)\\))?", NAME_REGEX, LENIENT_VERSION_REGEX));
    private static final Splitter SEMICOLON_SPLITTER =
            Splitter.on(CharMatcher.is(';').precomputed()).trimResults().omitEmptyStrings();
    private static final CharMatcher COMMENT_VALID_CHARS = CharMatcher.inRange('a', 'z')
            .or(CharMatcher.inRange('A', 'Z'))
            .or(CharMatcher.inRange('0', '9'))
            .or(CharMatcher.anyOf(".-:_/ ,"))
            .precomputed();

    private UserAgents() {}

    /** Returns the canonical string format for the given {@link UserAgent}. */
    @Safe
    public static String format(@Safe UserAgent userAgent) {
        StringBuilder formatted = new StringBuilder(64); // preallocate larger buffer for longer agents
        formatSimpleAgent(userAgent.primary(), formatted);
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
        List<String> comments = agent.comments();
        if (!comments.isEmpty()) {
            output.append(" (");
            for (int i = 0; i < comments.size(); i++) {
                if (i > 0) {
                    output.append("; ");
                }
                output.append(comments.get(i));
            }
            output.append(')');
        }
    }

    /**
     * Parses the given string into a {@link UserAgent} or throws an {@link IllegalArgumentException} if no correctly
     * formatted primary user agent can be found. Incorrectly formatted informational agents are omitted.
     *
     * <p>Valid user agent strings loosely follow RFC 7230 (https://tools.ietf.org/html/rfc7230#section-3.2.6).
     */
    public static UserAgent parse(@Safe String userAgent) {
        Preconditions.checkNotNull(userAgent, "userAgent must not be null");
        return parseInternal(userAgent, false /* strict */);
    }

    /**
     * Like {@link #parse}, but never fails and returns the primary agent {@code unknown/0.0.0} if no valid primary
     * agent can be parsed.
     */
    public static UserAgent tryParse(@Safe String userAgent) {

        return parseInternal(userAgent == null ? "" : userAgent, true /* lenient */);
    }

    /**
     * Parse and return only the `name` component of the primary user-agent string, or "unknown" if no valid primary
     * agent name can be parsed.
     *
     * This is functionally similar to calling `tryParse(userAgent).primary().name()`, but optimized to not use
     * regular expressions.
     */
    public static String tryParsePrimaryName(@Safe String userAgent) {
        if (userAgent == null) {
            return "unknown";
        }
        int split = userAgent.indexOf('/');
        if (split <= 0) {
            return "unknown";
        }
        // the regex-based logic will only match on blocks of the form "foo/1.2.3 (some extra stuff)", where the name
        // component "foo" must match NAME_REGEX (or return true from isValidName()). this means that
        // it will possibly ignore leading characters (even non-whitespace) up until the "foo/1.2.3" part
        // so we further split primaryName into _just_ the set of characters immediately preceding
        // the "/" for which isValidCharForName() returns true.
        @SuppressWarnings("for-rollout:Var")
        int lindex = split - 1;
        while (lindex >= 0) {
            if (!isValidCharForName(userAgent.charAt(lindex))) {
                break;
            }
            --lindex;
        }
        // lindex points to the first invalid character encountered walking backwards, which we want to exclude
        ++lindex;
        if (lindex == split) {
            // empty substring
            return "unknown";
        }
        // the first character must match isAlpha()
        if (!isAlpha(userAgent.charAt(lindex))) {
            return "unknown";
        }
        // we can avoid an extra call to isValidName() now, since we have validated that the first character matches
        // isAlpha(), and every character thereafter matches isValidCharForName()
        return userAgent.substring(lindex, split);
    }

    private static UserAgent parseInternal(@Safe String userAgent, boolean lenient) {
        ImmutableUserAgent.Builder builder = ImmutableUserAgent.builder();

        Matcher matcher = SEGMENT_PATTERN.matcher(userAgent);
        @SuppressWarnings("for-rollout:Var")
        boolean foundFirst = false;
        while (matcher.find()) {
            String name = matcher.group(1);
            String version = matcher.group(2);
            Optional<String> comments = Optional.ofNullable(matcher.group(4));

            UserAgent.Agent agent = UserAgent.Agent.of(
                    name, version, comments.map(UserAgents::parseComments).orElseGet(ImmutableList::of));
            if (!foundFirst) {
                // primary
                builder.primary(agent);
            } else {
                // informational
                builder.addInformational(agent);
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

    private static List<String> parseComments(String commentsString) {
        List<String> results = SEMICOLON_SPLITTER.splitToList(commentsString);
        for (int i = 0; i < results.size(); ++i) {
            // In most cases, all comments will be valid, so we avoid stream overhead.
            if (!isValidComment(results.get(i))) {
                return results.stream().filter(UserAgents::isValidComment).toList();
            }
        }
        return results;
    }

    static void checkComment(@Safe String comment) {
        if (comment == null) {
            throw new SafeIllegalArgumentException("Comment must not be null");
        }
        if (comment.isEmpty()) {
            throw new SafeIllegalArgumentException("Comment must not be empty");
        }
        if (comment.startsWith(" ")) {
            throw new SafeIllegalArgumentException(
                    "Comment must not start with whitespace", SafeArg.of("comment", comment));
        }
        if (comment.endsWith(" ")) {
            throw new SafeIllegalArgumentException(
                    "Comment must not end with whitespace", SafeArg.of("comment", comment));
        }
        if (!COMMENT_VALID_CHARS.matchesAllOf(comment)) {
            throw new SafeIllegalArgumentException(
                    "Comment contains disallowed characters",
                    SafeArg.of("allowed", "a-zA-Z0-9.-:_/ "),
                    SafeArg.of("comment", comment));
        }
    }

    static boolean isValidComment(@Safe String comment) {
        return comment != null
                && !comment.isEmpty()
                && !comment.startsWith(" ")
                && !comment.endsWith(" ")
                && COMMENT_VALID_CHARS.matchesAllOf(comment);
    }

    static boolean isValidName(String name) {
        // hand rolled implementation of NAME_REGEX.matcher(name).matches() to avoid allocations
        // "[a-zA-Z][a-zA-Z0-9\\-]*"
        if (name == null || name.isEmpty()) {
            return false;
        }
        @SuppressWarnings("for-rollout:Var")
        char ch = name.charAt(0);
        if (!isAlpha(ch)) {
            return false;
        }

        for (int i = 1; i < name.length(); i++) {
            ch = name.charAt(i);
            if (!isValidCharForName(ch)) {
                return false;
            }
        }

        return true;
    }

    private static boolean isValidCharForName(char ch) {
        return isAlpha(ch) || isNumeric(ch) || ch == '-';
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

    static void checkNodeId(@Safe String instanceId) {
        if (!isValidNodeId(instanceId)) {
            throw new SafeIllegalArgumentException("Illegal node id format", SafeArg.of("nodeId", instanceId));
        }
    }

    static boolean isValidVersion(@Safe String version) {
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
