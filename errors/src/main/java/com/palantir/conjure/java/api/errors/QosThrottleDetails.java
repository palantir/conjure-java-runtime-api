/*
 * (c) Copyright 2026 Palantir Technologies Inc. All rights reserved.
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
 * Additional, free-form details about a {@link QosException.Throttle}, independent of its {@link QosReason}.
 * These details must be safe to log. Unlike a reason, they are not constrained to metric tag values.
 */
@Safe
public final class QosThrottleDetails {
    @Safe
    private final String details;

    private QosThrottleDetails(@Safe String details) {
        this.details = Preconditions.checkNotNull(details, "details");
    }

    public static QosThrottleDetails of(@Safe String details) {
        return new QosThrottleDetails(details);
    }

    @Safe
    public String details() {
        return details;
    }

    @Override
    public boolean equals(Object other) {
        return this == other || (other instanceof QosThrottleDetails that && details.equals(that.details));
    }

    @Override
    public int hashCode() {
        return details.hashCode();
    }

    @Override
    public String toString() {
        return details;
    }
}
