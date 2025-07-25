/*
 * (c) Copyright 2025 Palantir Technologies Inc. All rights reserved.
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

package com.palantir.conjure.java.api.errors;

public abstract class AbstractSerializableError<T> {
    private final String errorCode;
    private final String errorName;
    private final String errorInstanceId;
    private final T errorParameters;

    public final String errorCode() {
        return errorCode;
    }

    public final String errorName() {
        return errorName;
    }

    public final String errorInstanceId() {
        return errorInstanceId;
    }

    public final T errorParameters() {
        return errorParameters;
    }

    protected AbstractSerializableError(String errorCode, String errorName, String errorInstanceId, T errorParameters) {
        this.errorCode = errorCode;
        this.errorName = errorName;
        this.errorInstanceId = errorInstanceId;
        this.errorParameters = errorParameters;
    }
}
