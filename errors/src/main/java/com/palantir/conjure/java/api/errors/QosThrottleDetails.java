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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.palantir.logsafe.Preconditions;
import com.palantir.logsafe.Safe;
import java.util.Objects;
import java.util.Optional;
import javax.annotation.Nullable;

/**
 * Additional details about a {@link QosException.Throttle}, independent of its {@link QosReason}. These are sent in
 * the response body as {@link QosResponseBody#qosDetails}. Every field is optional and unknown fields are ignored, so
 * fields can be added later. These details must be safe to log.
 */
@Safe
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class QosThrottleDetails {
    @Safe
    @Nullable
    @JsonProperty("debugMessage")
    private final String debugMessage;

    @JsonCreator
    private QosThrottleDetails(@JsonProperty("debugMessage") @Safe @Nullable String debugMessage) {
        this.debugMessage = debugMessage;
    }

    public static Builder builder() {
        return new Builder();
    }

    /** A message describing the throttle, intended for debugging. */
    public Optional<@Safe String> debugMessage() {
        return Optional.ofNullable(debugMessage);
    }

    @Override
    public boolean equals(@Nullable Object other) {
        return this == other
                || (other instanceof QosThrottleDetails that && Objects.equals(debugMessage, that.debugMessage));
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(debugMessage);
    }

    @Override
    public String toString() {
        return "QosThrottleDetails{debugMessage=" + debugMessage() + '}';
    }

    public static final class Builder {
        @Nullable
        private String debugMessage;

        private Builder() {}

        public Builder debugMessage(@Safe String value) {
            this.debugMessage = Preconditions.checkNotNull(value, "debugMessage");
            return this;
        }

        public QosThrottleDetails build() {
            return new QosThrottleDetails(debugMessage);
        }
    }
}
