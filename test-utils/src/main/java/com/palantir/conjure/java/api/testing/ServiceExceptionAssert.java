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

import com.palantir.conjure.java.api.errors.ServiceException;
import org.assertj.core.api.InstanceOfAssertFactory;

public class ServiceExceptionAssert extends AbstractServiceExceptionAssert<ServiceExceptionAssert, ServiceException> {

    private static final InstanceOfAssertFactory<ServiceException, ServiceExceptionAssert> INSTANCE_OF_ASSERT_FACTORY =
            new InstanceOfAssertFactory<>(ServiceException.class, ServiceExceptionAssert::new);

    public ServiceExceptionAssert(ServiceException actual) {
        super(actual, ServiceExceptionAssert.class);
    }

    public static InstanceOfAssertFactory<ServiceException, ServiceExceptionAssert> instanceOfAssertFactory() {
        return INSTANCE_OF_ASSERT_FACTORY;
    }
}
