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

package com.palantir.conjure.java.api.testing;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.palantir.conjure.java.api.errors.ErrorType;
import com.palantir.conjure.java.api.errors.ServiceException;
import com.palantir.logsafe.SafeArg;
import com.palantir.logsafe.UnsafeArg;
import org.junit.jupiter.api.Test;

public class ServiceExceptionAssertTest {

    @Test
    public void testSanity() throws Exception {
        ErrorType actualType = ErrorType.FAILED_PRECONDITION;

        Assertions.assertThat(new ServiceException(actualType, SafeArg.of("a", "b"), UnsafeArg.of("c", "d")))
                .hasType(actualType)
                .hasArgs(SafeArg.of("a", "b"), UnsafeArg.of("c", "d"));

        Assertions.assertThat(new ServiceException(actualType, SafeArg.of("a", "b"), UnsafeArg.of("c", "d")))
                .hasType(actualType)
                .hasArgs(UnsafeArg.of("c", "d"), SafeArg.of("a", "b")); // Order doesn't matter

        assertThatThrownBy(() -> Assertions.assertThat(new ServiceException(actualType)).hasType(ErrorType.INTERNAL))
                .isInstanceOf(AssertionError.class)
                .hasMessage("Expected ErrorType to be %s, but found %s", ErrorType.INTERNAL, actualType);

        assertThatThrownBy(() -> Assertions.assertThat(new ServiceException(actualType, SafeArg.of("a", "b")))
                        .hasArgs(SafeArg.of("c", "d")))
                .isInstanceOf(AssertionError.class)
                .hasMessage("Expected safe args to be {c=d}, but found {a=b}");

        assertThatThrownBy(() -> Assertions.assertThat(new ServiceException(actualType, UnsafeArg.of("a", "b")))
                        .hasArgs(UnsafeArg.of("c", "d")))
                .isInstanceOf(AssertionError.class)
                .hasMessage("Expected unsafe args to be {c=d}, but found {a=b}");
    }
}
