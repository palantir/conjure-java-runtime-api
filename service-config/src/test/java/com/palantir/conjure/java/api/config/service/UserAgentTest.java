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

import static com.palantir.logsafe.testing.Assertions.assertThatLoggableExceptionThrownBy;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.palantir.conjure.java.api.config.service.UserAgent.Agent;
import com.palantir.logsafe.SafeArg;
import java.util.List;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class UserAgentTest {

    @Test
    public void validAndInvalidNodeSyntax() {
        // Valid nodeId
        for (String nodeId :
                new String[] {"nodeId", "NODEID", "node-id", "node.id", "nodeId.", "192.168.0.1", "my.server.foo.local"
                }) {
            UserAgent.of(UserAgent.Agent.of("valid-service", "1.0.0"), nodeId);
        }

        // Invalid nodeId
        for (String nodeId : new String[] {".nodeId", "node$", "node_id"}) {
            assertThatLoggableExceptionThrownBy(
                            () -> UserAgent.of(UserAgent.Agent.of("valid-service", "1.0.0"), nodeId))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Test
    public void testCorrectHeaderFormatWithNodeId() {
        UserAgent baseUserAgent = UserAgent.of(UserAgent.Agent.of("service", "1.0.0"), "myNode");
        assertThat(UserAgents.format(baseUserAgent)).isEqualTo("service/1.0.0 (nodeId:myNode)");

        UserAgent derivedAgent = baseUserAgent.addAgent(UserAgent.Agent.of("conjure", "2.0.0"));
        assertThat(UserAgents.format(derivedAgent)).isEqualTo("service/1.0.0 (nodeId:myNode) conjure/2.0.0");
    }

    @Test
    public void testCorrectHeaderFormatWithoutNodeId() {
        UserAgent baseUserAgent = UserAgent.of(UserAgent.Agent.of("service", "1.0.0"));
        assertThat(UserAgents.format(baseUserAgent)).isEqualTo("service/1.0.0");

        UserAgent derivedAgent = baseUserAgent.addAgent(UserAgent.Agent.of("conjure", "2.0.0"));
        assertThat(UserAgents.format(derivedAgent)).isEqualTo("service/1.0.0 conjure/2.0.0");
    }

    @Test
    void testPrimaryWithInformational() {
        UserAgent baseUserAgent = UserAgent.of(Agent.of("service", "1.0.0"));
        List<Agent> info = ImmutableList.of(Agent.of("conjure", "1.2.3"), Agent.of("jdk", "17.0.4.1"));
        UserAgent first = UserAgent.of(baseUserAgent, info);
        assertThat(first).satisfies(agent -> {
            assertThat(agent.primary()).isEqualTo(baseUserAgent.primary());
            assertThat(agent.informational()).hasSize(2).containsExactlyElementsOf(info);
            assertThat(UserAgents.format(agent)).isEqualTo("service/1.0.0 conjure/1.2.3 jdk/17.0.4.1");
            assertThat(UserAgents.parse(UserAgents.format(agent))).isEqualTo(agent);
            assertThat(UserAgent.of(agent, ImmutableList.of())).isEqualTo(agent);
        });

        List<Agent> moreInfo = ImmutableList.of(Agent.of("test", "4.5.6"));
        assertThat(UserAgent.of(first, moreInfo)).satisfies(agent -> {
            assertThat(agent.primary()).isEqualTo(baseUserAgent.primary());
            assertThat(agent.informational()).hasSize(3).containsExactlyElementsOf(Iterables.concat(info, moreInfo));
            assertThat(UserAgents.format(agent)).isEqualTo("service/1.0.0 conjure/1.2.3 jdk/17.0.4.1 test/4.5.6");
            assertThat(UserAgents.parse(UserAgents.format(agent))).isEqualTo(agent);
            assertThat(UserAgent.of(agent, ImmutableList.of())).isEqualTo(agent);
        });
    }

    @Test
    public void testInvalidServiceName() {
        assertThatLoggableExceptionThrownBy(() -> UserAgent.Agent.of("invalid service name", "1.0.0"))
                .hasLogMessage("Illegal agent name format")
                .hasExactlyArgs(SafeArg.of("name", "invalid service name"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testInvalidNodeId() {
        assertThatLoggableExceptionThrownBy(
                        () -> UserAgent.of(UserAgent.Agent.of("serviceName", "1.0.0"), "invalid node id"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasExactlyArgs(SafeArg.of("nodeId", "invalid node id"))
                .hasLogMessage("Illegal node id format");
    }

    @Test
    public void testInvalidVersion() {
        assertThat(UserAgents.format(UserAgent.of(UserAgent.Agent.of("serviceName", "1 0 0"), "myNode")))
                .isEqualTo("serviceName/0.0.0 (nodeId:myNode)");
    }

    @Test
    public void parse_handlesPrimaryAgent() {
        // Valid strings
        for (String agent : new String[] {
            "service/1.2",
            "service/1.2.3-2-g4658d8a",
            "service/1.2.3-rc1-2-g4658d8a",
            "service/10.20.30",
            "service/10.20.30 (nodeId:myNode)",
        }) {
            assertThat(UserAgents.format(UserAgents.parse(agent))).isEqualTo(agent);
        }

        // Formatting ignores invalid comments
        assertThat(UserAgents.format(UserAgents.parse("service/1.2.3 (())"))).isEqualTo("service/1.2.3");

        // Formatting retains valid comments
        assertThat(UserAgents.format(UserAgents.parse("service/1.2.3 (foo:bar)")))
                .isEqualTo("service/1.2.3 (foo:bar)");

        // Finds primary agent even when there is a prefix
        assertThat(UserAgents.format(UserAgents.parse("  service/1.2.3"))).isEqualTo("service/1.2.3");
        assertThat(UserAgents.format(UserAgents.parse("bogus  service/1.2.3"))).isEqualTo("service/1.2.3");

        // Fixes primary agent version to 0.0.0 if it cannot be parsed
        assertThat(UserAgents.format(UserAgents.parse("service/foo-1.2.3"))).isEqualTo("service/0.0.0");

        // Invalid syntax throws exception
        for (String agent : new String[] {
            "s", "foo|1.2.3",
        }) {
            assertThatLoggableExceptionThrownBy(() -> UserAgents.format(UserAgents.parse(agent)))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasLogMessage("Failed to parse user agent string")
                    .hasExactlyArgs(SafeArg.of("userAgent", agent));
        }
    }

    @Test
    public void parse_handlesInformationalAgents() {
        // Valid strings
        for (String agent :
                new String[] {"serviceA/1.2.3 serviceB/4.5.6", "serviceB/1.2.3 (nodeId:myNode) serviceB/4.5.6"}) {
            assertThat(UserAgents.format(UserAgents.parse(agent)))
                    .withFailMessage(agent)
                    .isEqualTo(agent);
        }

        // nodeId on informational agents is retained
        UserAgent nodeIdOnInformational = UserAgents.parse("serviceA/1.2.3 serviceB/4.5.6 (nodeId:myNode)");
        assertThat(UserAgents.format(nodeIdOnInformational)).isEqualTo("serviceA/1.2.3 serviceB/4.5.6 (nodeId:myNode)");
        assertThat(nodeIdOnInformational.nodeId())
                .as("Only primary agents nodeId should be reported")
                .isEmpty();

        // Malformed informational agents are omitted
        assertThat(UserAgents.format(UserAgents.parse("serviceA/1.2.3 serviceB|4.5.6")))
                .isEqualTo("serviceA/1.2.3");
    }

    @Test
    public void parse_canParseBrowserAgent() {
        String chrome = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_6) AppleWebKit/537.36 (KHTML, like Gecko) "
                + "Chrome/61.0.3163.100 Safari/537.36";
        String expected = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_6) AppleWebKit/537.36 (KHTML, like Gecko) "
                + "Chrome/61.0.3163.100 Safari/537.36";
        assertThat(UserAgents.format(UserAgents.tryParse(chrome))).isEqualTo(expected);
        assertThat(UserAgents.format(UserAgents.parse(chrome))).isEqualTo(expected);
    }

    @Test
    public void parse_canParseBrowserAgentWithEmptyComment() {
        String chrome = "Mozilla/5.0 ( ) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/61.0.3163.100 Safari/537.36";
        String expected = "Mozilla/5.0 AppleWebKit/537.36 (KHTML, like Gecko) Chrome/61.0.3163.100 Safari/537.36";
        assertThat(UserAgents.format(UserAgents.tryParse(chrome))).isEqualTo(expected);
        assertThat(UserAgents.format(UserAgents.parse(chrome))).isEqualTo(expected);
    }

    @Test
    public void tryParse_parsesWithBestEffort() {
        // Fixes up the primary agent
        assertThat(UserAgents.format(UserAgents.tryParse(null))).isEqualTo("unknown/0.0.0");
        assertThat(UserAgents.format(UserAgents.tryParse(""))).isEqualTo("unknown/0.0.0");
        assertThat(UserAgents.format(UserAgents.tryParse("serviceA|1.2.3"))).isEqualTo("unknown/0.0.0");
        assertThat(UserAgents.format(UserAgents.tryParse("foo serviceA/1.2.3"))).isEqualTo("serviceA/1.2.3");
        assertThat(UserAgents.format(UserAgents.tryParse("foo&serviceA/1.2.3"))).isEqualTo("serviceA/1.2.3");

        // Omits malformed informational agents
        assertThat(UserAgents.format(UserAgents.tryParse("serviceA/1.2.3 bogus|1.2.3 foo bar (boom)")))
                .isEqualTo("serviceA/1.2.3");
    }

    @Test
    public void tryParsePrimaryName_ignoresExtraStuff() {
        validateTryParsePrimaryName("serviceA/1.2.3", "serviceA");
        validateTryParsePrimaryName("serviceA/1.2.3", "serviceA");
        validateTryParsePrimaryName("serviceA/1.2.3 (foobar)", "serviceA");
        validateTryParsePrimaryName("serviceA/1.2.3 serviceB/4.5.6 (foobar fizz buzz", "serviceA");
    }

    @Test
    public void tryParserPrimaryName_parsesWithBestEffort() {
        validateTryParsePrimaryName(null, "unknown");
        validateTryParsePrimaryName("", "unknown");
        validateTryParsePrimaryName("a", "unknown");
        validateTryParsePrimaryName("serviceA|1.2.3", "unknown");
        validateTryParsePrimaryName("foo serviceA/1.2.3", "serviceA");
        validateTryParsePrimaryName("foo&serviceA/1.2.3", "serviceA");
        validateTryParsePrimaryName("serviceA/1.2.3 bogus|1.2.3 foo bar (boom)", "serviceA");
        validateTryParsePrimaryName("foo bar baz serviceA/1.2.3 (some stuff)", "serviceA");
        validateTryParsePrimaryName(" serviceA/1.2.3", "serviceA");
        validateTryParsePrimaryName("\tserviceA/1.2.3", "serviceA");
        validateTryParsePrimaryName("&/1.2.3", "unknown");
    }

    @Test
    public void tryParsePrimaryName_stillParsesOnInvalidUserAgentString() {
        // tryParsePrimaryName explicitly tries to avoid doing extra work like parsing a version string, which
        // relies on a regex match that could be expensive (and we're not interested in it anyway)
        // this means calls can sometimes have unintended behavior for a user-agent string that's not well formed,
        // but that's probably okay for most use cases.

        // note that this is distinct from calling UserAgents.tryParse("foo/").primary().name(), which would return
        // "unknown"
        assertThat(UserAgents.tryParsePrimaryName("foo/")).isEqualTo("foo");
    }

    // validates that UserAgents.tryParsePrimaryName both:
    //    - matches the expected primary name output
    //    - matches the output of UserAgents.tryParse(input).primary().name()
    private static void validateTryParsePrimaryName(String userAgent, String expectedResult) {
        assertThat(UserAgents.tryParsePrimaryName(userAgent)).isEqualTo(expectedResult);
        assertThat(UserAgents.tryParsePrimaryName(userAgent))
                .isEqualTo(UserAgents.tryParse(userAgent).primary().name());
    }

    @Test
    public void valid_names() {
        assertThat("a").satisfies(UserAgentTest::isValidName);
        assertThat("a1").satisfies(UserAgentTest::isValidName);
        assertThat("foo").satisfies(UserAgentTest::isValidName);
        assertThat("foo-bar").satisfies(UserAgentTest::isValidName);
        assertThat("foo-bar123").satisfies(UserAgentTest::isValidName);
    }

    @Test
    public void invalid_names() {
        assertThat((String) null).satisfies(UserAgentTest::isNotValidName);
        assertThat("").satisfies(UserAgentTest::isNotValidName);
        assertThat(" ").satisfies(UserAgentTest::isNotValidName);
        assertThat("1").satisfies(UserAgentTest::isNotValidName);
        assertThat("1a").satisfies(UserAgentTest::isNotValidName);
        assertThat("1.0").satisfies(UserAgentTest::isNotValidName);
        assertThat("service name").satisfies(UserAgentTest::isNotValidName);
        assertThat("service.name").satisfies(UserAgentTest::isNotValidName);
        assertThat("service_name").satisfies(UserAgentTest::isNotValidName);
    }

    @Test
    public void invalid_comment_messages() {
        assertThatThrownBy(() -> UserAgents.checkComment(";"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Comment contains disallowed characters");
        assertThat(UserAgents.isValidComment(";")).isFalse();
        assertThatThrownBy(() -> UserAgents.checkComment(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Comment must not be null");
        assertThat(UserAgents.isValidComment(null)).isFalse();
        assertThatThrownBy(() -> UserAgents.checkComment(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Comment must not be empty");
        assertThat(UserAgents.isValidComment("")).isFalse();
        assertThatThrownBy(() -> UserAgents.checkComment(" leading whitespace"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Comment must not start with whitespace");
        assertThat(UserAgents.isValidComment(" leading whitespace")).isFalse();
        assertThatThrownBy(() -> UserAgents.checkComment("trailing whitespace "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Comment must not end with whitespace");
        assertThat(UserAgents.isValidComment("trailing whitespace ")).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {"nodeId:nodeId", "MyResource/ri.abc.def.ghi-jkl", "KHTML, like Gecko"})
    public void valid_comments(String comment) {
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(UserAgents.isValidComment(comment)).isTrue();
        softly.assertThatCode(() -> UserAgents.checkComment(comment)).doesNotThrowAnyException();
        softly.assertAll();
    }

    @ParameterizedTest
    @ValueSource(strings = {";", "", " leading", "trailing ", "(parens)"})
    public void invalid_comments(String comment) {
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(UserAgents.isValidComment(comment)).isFalse();
        softly.assertThatThrownBy(() -> UserAgents.checkComment(comment)).isInstanceOf(IllegalArgumentException.class);
        softly.assertAll();
    }

    @Test
    public void testNodeIdReplacement() {
        UserAgent original = UserAgent.of(Agent.of("name", "0.0.0"), "nodeId1");
        UserAgent replacement = UserAgent.of(original.primary(), "nodeId2");
        assertThat(UserAgents.format(replacement)).isEqualTo("name/0.0.0 (nodeId:nodeId2)");
    }

    @Test
    public void testNodeIdOnInformationalAgent() {
        UserAgent original = UserAgent.of(Agent.of("primary", "0.0.0"), "nodeId1");
        UserAgent updated = original.addAgent(
                UserAgent.of(Agent.of("info", "0.0.0"), "nodeId2").primary());
        assertThat(UserAgents.format(updated)).isEqualTo("primary/0.0.0 (nodeId:nodeId1) info/0.0.0 (nodeId:nodeId2)");
    }

    private static void isValidName(String name) {
        assertThat(UserAgents.isValidName(name)).isTrue();
        assertThat(name)
                .describedAs("Name should match regex, inconsistent with isValidName for '%s'", name)
                .matches(UserAgents.NAME_REGEX);
    }

    private static void isNotValidName(String name) {
        assertThat(UserAgents.isValidName(name)).isFalse();
        assertThat(name)
                .describedAs("Name should not match regex, inconsistent with isValidName for '%s'", name)
                .doesNotMatch(UserAgents.NAME_REGEX);
    }
}
