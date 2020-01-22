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
import org.assertj.core.api.AbstractThrowableAssert;

public class RemoteExceptionAssert extends AbstractThrowableAssert<RemoteExceptionAssert, RemoteException> {

    RemoteExceptionAssert(RemoteException actual) {
        super(actual, RemoteExceptionAssert.class);
    }

    public final RemoteExceptionAssert isGeneratedFromErrorType(ErrorType type) {
        isNotNull();
        String actualCode = actual.getError().errorCode();
        String actualName = actual.getError().errorName();
        int actualStatus = actual.getStatus();

        if (!actualCode.equals(type.code().name())) {
            failWithMessage(
                    "Expected error code to be %s, but found %s", type.code().name(), actualCode);
        }
        if (!actualName.equals(type.name())) {
            failWithMessage("Expected error name to be %s, but found %s", type.name(), actualName);
        }
        if (!(actualStatus == type.httpErrorCode())) {
            failWithMessage("Expected error status to be %s, but found %s", type.httpErrorCode(), actualStatus);
        }

        return this;
    }
}
