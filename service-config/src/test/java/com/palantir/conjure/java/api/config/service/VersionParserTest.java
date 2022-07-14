/*
 * (c) Copyright 2022 Palantir Technologies Inc. All rights reserved.
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

import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class VersionParserTest {

    @ParameterizedTest
    @MethodSource("versions")
    void validVersions(String input, int result, boolean isValid) {
        assertThat(VersionParser.countNumericDotGroups(input)).isEqualTo(result);
        assertThat(UserAgents.isValidVersion(input)).isEqualTo(isValid);
    }

    private static Stream<Arguments> versions() {
        return Stream.of(
                Arguments.of("", -1, false),
                Arguments.of("  ", -1, false),
                Arguments.of("bad", -1, false),
                Arguments.of("1", 1, true),
                Arguments.of("1.2", 2, true),
                Arguments.of("1.2.3", 3, true),
                Arguments.of("1.2.3.4", 4, true),
                Arguments.of("1.2.3-rc4", -1, true),
                Arguments.of("1.2.3-4-gabc", -1, true),
                Arguments.of("1.2.3.4-rc5-6-gabc", -1, true),
                Arguments.of("1.2.3-4", -1, false),
                Arguments.of("0-0-0", -1, false));
    }
}
