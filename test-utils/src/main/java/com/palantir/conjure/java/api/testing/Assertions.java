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
import com.palantir.conjure.java.api.errors.RemoteException;
import com.palantir.conjure.java.api.errors.ServiceException;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.assertj.core.util.CanIgnoreReturnValue;
import org.assertj.core.util.CheckReturnValue;

@CheckReturnValue
public class Assertions extends org.assertj.core.api.Assertions {

    Assertions() {}

    public static ServiceExceptionAssert assertThat(ServiceException actual) {
        return new ServiceExceptionAssert(actual);
    }

    public static RemoteExceptionAssert assertThat(RemoteException actual) {
        return new RemoteExceptionAssert(actual);
    }

    public static QosExceptionAssert assertThat(QosException actual) {
        return new QosExceptionAssert(actual);
    }

    @CanIgnoreReturnValue
    public static ServiceExceptionAssert assertThatServiceExceptionThrownBy(ThrowingCallable shouldRaiseThrowable) {
        return assertThatThrownBy(shouldRaiseThrowable).asInstanceOf(ServiceExceptionAssert.instanceOfAssertFactory());
    }

    @CanIgnoreReturnValue
    public static RemoteExceptionAssert assertThatRemoteExceptionThrownBy(ThrowingCallable shouldRaiseThrowable) {
        return assertThatThrownBy(shouldRaiseThrowable).asInstanceOf(RemoteExceptionAssert.instanceOfAssertFactory());
    }

    @CanIgnoreReturnValue
    public static QosExceptionAssert assertThatQosExceptionThrownBy(ThrowingCallable shouldRaiseThrowable) {
        return assertThatThrownBy(shouldRaiseThrowable).asInstanceOf(QosExceptionAssert.instanceOfAssertFactory());
    }
}
