/*
 * (c) Copyright 2022 Palantir Technologies Inc. All rights reserved.
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

import com.google.errorprone.annotations.CompileTimeConstant;
import java.util.Objects;

public final class QosReason {

    @CompileTimeConstant
    private final String name;

    private QosReason(@CompileTimeConstant String name) {
        this.name = name;
    }

    public static QosReason of(@CompileTimeConstant String name) {
        return new QosReason(name);
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        } else if (!(other instanceof QosReason)) {
            return false;
        } else {
            QosReason otherReason = (QosReason) other;
            return Objects.equals(this.name, otherReason.name);
        }
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.name);
    }
}
