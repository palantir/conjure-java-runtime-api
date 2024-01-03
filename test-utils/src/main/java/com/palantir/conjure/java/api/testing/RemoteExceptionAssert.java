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

import com.palantir.conjure.java.api.errors.ErrorType;
import com.palantir.conjure.java.api.errors.RemoteException;
import java.util.Objects;
import org.assertj.core.api.AbstractThrowableAssert;
import org.assertj.core.api.InstanceOfAssertFactory;
import org.assertj.core.util.Throwables;

public class RemoteExceptionAssert extends AbstractThrowableAssert<RemoteExceptionAssert, RemoteException> {

    private static final InstanceOfAssertFactory<RemoteException, RemoteExceptionAssert> INSTANCE_OF_ASSERT_FACTORY =
            new InstanceOfAssertFactory<>(RemoteException.class, RemoteExceptionAssert::new);

    RemoteExceptionAssert(RemoteException actual) {
        super(actual, RemoteExceptionAssert.class);
    }

    public static InstanceOfAssertFactory<RemoteException, RemoteExceptionAssert> instanceOfAssertFactory() {
        return INSTANCE_OF_ASSERT_FACTORY;
    }

    public final RemoteExceptionAssert isGeneratedFromErrorType(ErrorType type) {
        isNotNull();

        String actualCode = actual.getError().errorCode();
        String actualName = actual.getError().errorName();
        int actualStatus = actual.getStatus();

        failIfNotEqual("error code", type.code().name(), actualCode);
        failIfNotEqual("error name", type.name(), actualName);
        failIfNotEqual("error status", type.httpErrorCode(), actualStatus);

        return this;
    }

    private <T> void failIfNotEqual(String fieldName, T expectedValue, T actualValue) {
        if (!Objects.equals(expectedValue, actualValue)) {
            failWithMessage(
                    "Expected %s to be %s, but found %s; remote exception: %s",
                    fieldName, expectedValue, actualValue, Throwables.getStackTrace(actual));
        }
    }
}
