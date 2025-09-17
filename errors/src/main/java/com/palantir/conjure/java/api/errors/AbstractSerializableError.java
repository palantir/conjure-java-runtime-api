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

import java.util.Map;

/**
 * Base class for serializable Conjure errors with generic parameter type support.
 * <p>
 * This abstract class represents serialized Conjure errors. It supports generic parameter types to allow for
 * deserialization of custom error parameters.
 * <p>
 * Usage example where {@code CustomErrorParameters} is a user-defined class representing the error parameters:
 * <pre>
 * {@code
 * class CustomError extends AbstractSerializableError<CustomErrorParameters> {
 *     @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
 *     CustomError(
 *         @JsonProperty("errorCode") String errorCode,
 *         @JsonProperty("errorName") String errorName,
 *         @JsonProperty("errorInstanceId") String errorInstanceId,
 *         @JsonProperty("parameters") CustomErrorParameters parameters) {
 *         super(errorCode, errorName, errorInstanceId, parameters);
 *     }
 * }
 * }
 * </pre>
 */
public abstract class AbstractSerializableError<T> {
    private final String errorCode;
    private final String errorName;
    private final String errorInstanceId;
    private final T parameters;
    private final Map<String, Object> parameterMap;

    public final String errorCode() {
        return errorCode;
    }

    public final String errorName() {
        return errorName;
    }

    public final String errorInstanceId() {
        return errorInstanceId;
    }

    public final T parameters() {
        return parameters;
    }

    public final Map<String, Object> parameterMap() {
        return parameterMap;
    }

    protected AbstractSerializableError(
            String errorCode,
            String errorName,
            String errorInstanceId,
            T parameters,
            Map<String, Object> parameterMap) {
        this.errorCode = errorCode;
        this.errorName = errorName;
        this.errorInstanceId = errorInstanceId;
        this.parameters = parameters;
        this.parameterMap = parameterMap;
    }
}
