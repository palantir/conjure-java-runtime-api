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

import com.palantir.logsafe.Preconditions;
import com.palantir.logsafe.Safe;

/**
 * A class representing the Conjure error parameter format that a client expects to receive from a server.
 *
 * <p>Built-in formats are:
 * <ul>
 *   <li>{@link #JSON_FORMAT}: Parameters are expected to be serialized as JSON</li>
 *   <li>
 *       {@link #JSON_JAVA_STRING_FORMAT}: Parameters are expected to be serialized as JSON and Java strings (using
 *       {@code Objects.toString()})
 *   </li>
 * </ul>
 *
 * <p>Clients should use the utility methods in {@link ConjureErrorParameterFormats} rather than
 * directly constructing instances of this class.
 */
@Safe
public final class ConjureErrorParameterFormat {
    private static final String JSON = "JSON";
    private static final String JSON_AND_JAVA_STRING = "JSON+JAVA-STRING";
    public static final ConjureErrorParameterFormat JSON_FORMAT = new ConjureErrorParameterFormat(JSON);
    public static final ConjureErrorParameterFormat JSON_JAVA_STRING_FORMAT =
            new ConjureErrorParameterFormat(JSON_AND_JAVA_STRING);

    @Safe
    private final String value;

    private ConjureErrorParameterFormat(@Safe String value) {
        this.value = Preconditions.checkNotNull(value, "Value is required");
    }

    static ConjureErrorParameterFormat valueOf(@Safe String value) {
        Preconditions.checkNotNull(value, "Value is required");
        if (JSON.equalsIgnoreCase(value)) {
            return JSON_FORMAT;
        }
        if (JSON_AND_JAVA_STRING.equalsIgnoreCase(value)) {
            return JSON_JAVA_STRING_FORMAT;
        }
        return new ConjureErrorParameterFormat(value);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        ConjureErrorParameterFormat format = (ConjureErrorParameterFormat) other;
        return value.equals(format.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() {
        return value;
    }
}
