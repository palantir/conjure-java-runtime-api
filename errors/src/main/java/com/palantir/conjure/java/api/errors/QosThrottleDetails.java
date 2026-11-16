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
import org.jetbrains.annotations.ApiStatus;

/**
 * Additional details about a {@link QosException.DetailedThrottle}, independent of its {@link QosReason}. These are
 * sent in the response body as {@link QosResponseBody#qosDetails}. Every field is optional and unknown fields are
 * ignored, so fields can be added later.
 * <p>
 * These details must be safe to log. They are sent to callers and may be shown to end users, so don't include
 * internal service names, identifiers or other implementation details.
 */
@ApiStatus.Experimental
@Safe
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class QosThrottleDetails {
    @Safe
    @Nullable
    @JsonProperty("userMessage")
    private final String userMessage;

    @JsonCreator
    private QosThrottleDetails(@JsonProperty("userMessage") @Safe @Nullable String userMessage) {
        this.userMessage = userMessage;
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * A message for end users, which UIs show as-is when they have no translation for this throttle. Assume customers
     * will see it: write it in plain language, without internal service names, identifiers or other implementation
     * details.
     */
    public Optional<@Safe String> userMessage() {
        return Optional.ofNullable(userMessage);
    }

    @Override
    public boolean equals(@Nullable Object other) {
        return this == other
                || (other instanceof QosThrottleDetails that && Objects.equals(userMessage, that.userMessage));
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(userMessage);
    }

    @Override
    public String toString() {
        return "QosThrottleDetails{userMessage=" + userMessage() + '}';
    }

    public static final class Builder {
        @Nullable
        private String userMessage;

        private Builder() {}

        /** See {@link QosThrottleDetails#userMessage}. */
        public Builder userMessage(@Safe String value) {
            this.userMessage = Preconditions.checkNotNull(value, "userMessage");
            return this;
        }

        public QosThrottleDetails build() {
            return new QosThrottleDetails(userMessage);
        }
    }
}
