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

import com.palantir.conjure.java.api.errors.QosException;
import com.palantir.conjure.java.api.errors.QosReason;
import java.util.Objects;
import org.assertj.core.api.AbstractThrowableAssert;
import org.assertj.core.api.InstanceOfAssertFactory;

public class QosExceptionAssert extends AbstractThrowableAssert<QosExceptionAssert, QosException> {

    private static final InstanceOfAssertFactory<QosException, QosExceptionAssert> INSTANCE_OF_ASSERT_FACTORY =
            new InstanceOfAssertFactory<>(QosException.class, QosExceptionAssert::new);

    QosExceptionAssert(QosException actual) {
        super(actual, QosExceptionAssert.class);
    }

    public static InstanceOfAssertFactory<QosException, QosExceptionAssert> instanceOfAssertFactory() {
        return INSTANCE_OF_ASSERT_FACTORY;
    }

    public final QosExceptionAssert hasReason(QosReason reason) {
        isNotNull();
        failIfNotEqual("Expected QosReason to be %s, but found %s", reason, actual.getReason());
        return this;
    }

    private void failIfNotEqual(String message, Object expected, Object actual) {
        if (!Objects.equals(expected, actual)) {
            failWithMessage(message, expected, actual);
        }
    }
}
