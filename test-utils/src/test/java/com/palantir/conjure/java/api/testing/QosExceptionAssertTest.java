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

import com.palantir.conjure.java.api.errors.QosException;
import com.palantir.conjure.java.api.errors.QosReason;
import org.junit.jupiter.api.Test;

public final class QosExceptionAssertTest {

    @Test
    public void testSanity() {
        QosReason reason = QosReason.of("reason");

        Assertions.assertThat(QosException.unavailable(reason)).hasReason(reason);

        assertThatThrownBy(() -> Assertions.assertThat(QosException.unavailable(QosReason.of("other")))
                        .hasReason(reason))
                .isInstanceOf(AssertionError.class)
                .hasMessage("Expected QosReason to be reason, but found other");
    }
}
