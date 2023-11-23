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
import com.palantir.logsafe.Preconditions;
import com.palantir.logsafe.SafeArg;
import java.io.Serializable;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * A class representing the reason why a {@link QosException} was created.
 *
 * Clients should create a relatively small number of static constant {@code Reason} objects, which are reused when
 * throwing QosExceptions. The string used to construct a {@code Reason} object should be able to be used as a metric
 * tag, for observability into {@link QosException} calls. As such, the string is constrained to have at most 50
 * lowercase alphanumeric characters, and hyphens (-).
 */
public final class QosReason implements Serializable {

    @CompileTimeConstant
    private final String reason;

    private static final String PATTERN_STRING = "^[a-z0-9\\-]{1,50}$";
    private static final Pattern PATTERN = Pattern.compile(PATTERN_STRING);

    private QosReason(@CompileTimeConstant String reason) {
        this.reason = reason;
    }

    public static QosReason of(
            @CompileTimeConstant @org.intellij.lang.annotations.Pattern(PATTERN_STRING) String reason) {
        Preconditions.checkArgument(
                PATTERN.matcher(reason).matches(),
                "Reason must be at most 50 characters, and only contain lowercase letters, numbers, "
                        + "and hyphens (-).",
                SafeArg.of("reason", reason));
        return new QosReason(reason);
    }

    @Override
    public String toString() {
        return reason;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        } else if (!(other instanceof QosReason)) {
            return false;
        } else {
            QosReason otherReason = (QosReason) other;
            return Objects.equals(this.reason, otherReason.reason);
        }
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.reason);
    }
}
