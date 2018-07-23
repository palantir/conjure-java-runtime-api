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

package com.palantir.conjure.java.api.testing;

import com.palantir.remoting.api.errors.RemoteException;
import com.palantir.remoting.api.errors.ServiceException;
import org.assertj.core.api.AssertionsForClassTypes;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.assertj.core.internal.Failures;

public class Assertions extends org.assertj.core.api.Assertions {

    Assertions() {}

    public static ServiceExceptionAssert assertThat(ServiceException actual) {
        return new ServiceExceptionAssert(actual);
    }

    public static RemoteExceptionAssert assertThat(RemoteException actual) {
        return new RemoteExceptionAssert(actual);
    }

    public static ServiceExceptionAssert assertThatServiceExceptionThrownBy(ThrowingCallable shouldRaiseThrowable) {
        Throwable throwable = AssertionsForClassTypes.catchThrowable(shouldRaiseThrowable);
        checkThrowableIsOfType(throwable, ServiceException.class);
        return new ServiceExceptionAssert((ServiceException) throwable);
    }

    public static RemoteExceptionAssert assertThatRemoteExceptionThrownBy(ThrowingCallable shouldRaiseThrowable) {
        Throwable throwable = AssertionsForClassTypes.catchThrowable(shouldRaiseThrowable);
        checkThrowableIsOfType(throwable, RemoteException.class);
        return new RemoteExceptionAssert((RemoteException) throwable);
    }

    private static void checkThrowableIsOfType(Throwable throwable, Class<?> clazz) {
        if (throwable == null) {
            throw Failures.instance().failure("Expecting code to raise a throwable.");
        }
        if (!clazz.isInstance(throwable)) {
            throw Failures.instance().failure(String.format("Expecting code to throw a %s, but caught a %s.",
                    clazz.getCanonicalName(), throwable.getClass().getCanonicalName()));
        }
    }
}
