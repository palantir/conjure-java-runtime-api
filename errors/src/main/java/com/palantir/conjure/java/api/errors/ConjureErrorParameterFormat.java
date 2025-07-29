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
 */
@Safe
public final class ConjureErrorParameterFormat {
    private static final String JSON = "JSON";
    public static final ConjureErrorParameterFormat JSON_FORMAT = new ConjureErrorParameterFormat(JSON);

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
