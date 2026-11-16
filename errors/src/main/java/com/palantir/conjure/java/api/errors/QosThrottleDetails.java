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
import com.google.errorprone.annotations.CompileTimeConstant;
import com.palantir.logsafe.Preconditions;
import com.palantir.logsafe.Safe;
import com.palantir.logsafe.SafeArg;
import com.palantir.logsafe.exceptions.SafeIllegalArgumentException;
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
    private static final int MAX_USER_MESSAGE_LENGTH = 128;

    @Safe
    @Nullable
    @JsonProperty("userMessage")
    private final String userMessage;

    @JsonCreator
    private QosThrottleDetails(@JsonProperty("userMessage") @Safe @Nullable String userMessage) {
        // The builder rejects long messages, so this only drops one received from another service
        this.userMessage = userMessage != null && userMessage.length() <= MAX_USER_MESSAGE_LENGTH ? userMessage : null;
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * A message for end users, which UIs show as-is when they have no translation for this throttle. Assume customers
     * will see it: write it in plain language, without internal service names, identifiers or other implementation
     * details. It is a compile-time constant of at most 128 characters. A longer message received from another
     * service is dropped.
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

        /**
         * Sets {@link QosThrottleDetails#userMessage}. The value must be a compile-time constant, which Error Prone
         * enforces, of at most 128 characters.
         *
         * @throws SafeIllegalArgumentException if the message is longer than 128 characters
         */
        public Builder userMessage(@CompileTimeConstant @Safe String value) {
            Preconditions.checkNotNull(value, "userMessage");
            if (value.length() > MAX_USER_MESSAGE_LENGTH) {
                throw new SafeIllegalArgumentException(
                        "userMessage is too long",
                        SafeArg.of("length", value.length()),
                        SafeArg.of("maxLength", MAX_USER_MESSAGE_LENGTH));
            }
            this.userMessage = value;
            return this;
        }

        public QosThrottleDetails build() {
            return new QosThrottleDetails(userMessage);
        }
    }
}
