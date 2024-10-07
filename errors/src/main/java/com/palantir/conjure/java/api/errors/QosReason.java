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
import com.palantir.logsafe.Safe;
import com.palantir.logsafe.SafeArg;
import com.palantir.logsafe.exceptions.SafeIllegalArgumentException;
import java.util.Objects;
import java.util.Optional;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nullable;

/**
 * A class representing the reason why a {@link QosException} was created.
 *
 * Clients should create a relatively small number of static constant {@code Reason} objects, which are reused when
 * throwing QosExceptions. The string used to construct a {@code Reason} object should be able to be used as a metric
 * tag, for observability into {@link QosException} calls. As such, the string is constrained to have at most 50
 * lowercase alphanumeric characters, and hyphens (-).
 */
@Safe
public final class QosReason {

    @Safe
    private final String reason;

    private final Optional<RetryHint> retryHint;
    private final Optional<DueTo> dueTo;

    private static final String PATTERN_STRING = "^[a-z0-9\\-]{1,50}$";

    private QosReason(@Safe String reason, Optional<RetryHint> retryHint, Optional<DueTo> dueTo) {
        checkReason(reason);
        this.reason = reason;
        this.retryHint = retryHint;
        this.dueTo = dueTo;
    }

    public static QosReason of(
            @Safe @CompileTimeConstant @org.intellij.lang.annotations.Pattern(PATTERN_STRING) String reason) {
        return new QosReason(reason, Optional.empty(), Optional.empty());
    }

    @Safe
    public String reason() {
        return reason;
    }

    public Optional<RetryHint> retryHint() {
        return retryHint;
    }

    public Optional<DueTo> dueTo() {
        return dueTo;
    }

    /** Returns the {@link #reason()} for historical reasons, and should not be updated. */
    @Override
    public String toString() {
        return reason;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        } else if (other instanceof QosReason otherReason) {
            return Objects.equals(this.reason, otherReason.reason)
                    && Objects.equals(this.retryHint, otherReason.retryHint)
                    && Objects.equals(this.dueTo, otherReason.dueTo);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.reason);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        @Nullable
        private String reason;

        @Nullable
        private RetryHint retryHint;

        @Nullable
        private DueTo dueTo;

        private Builder() {}

        // other.reason must be a compile-time-constant
        @SuppressWarnings("CompileTimeConstant")
        public Builder from(QosReason other) {
            return reason(other.reason()).retryHint(other.retryHint()).dueTo(other.dueTo());
        }

        public Builder reason(
                @Safe @CompileTimeConstant @org.intellij.lang.annotations.Pattern(PATTERN_STRING) String value) {
            this.reason = Preconditions.checkNotNull(value, "reason");
            return this;
        }

        public Builder retryHint(RetryHint value) {
            this.retryHint = Preconditions.checkNotNull(value, "retryHint");
            return this;
        }

        public Builder retryHint(Optional<RetryHint> value) {
            this.retryHint = Preconditions.checkNotNull(value, "retryHint").orElse(null);
            return this;
        }

        public Builder dueTo(DueTo value) {
            this.dueTo = Preconditions.checkNotNull(value, "dueTo");
            return this;
        }

        public Builder dueTo(Optional<DueTo> value) {
            this.dueTo = Preconditions.checkNotNull(value, "dueTo").orElse(null);
            return this;
        }

        public QosReason build() {
            return new QosReason(
                    Preconditions.checkNotNull(reason, "reason"),
                    Optional.ofNullable(retryHint),
                    Optional.ofNullable(dueTo));
        }
    }

    /**
     * Conveys the servers opinion on whether a QoS failure should be retried, or propagate
     * back to the caller and result in a failure. There is no guarantee that these values
     * will be respected by all clients, and should be considered best-effort.
     */
    public enum RetryHint {
        /**
         * Clients should not attempt to retry this failure,
         * providing the failure as context back to the initial caller.
         */
        DO_NOT_RETRY;
    }

    /**
     * Describes the cause of a QoS failure when known to be non-default. By default, we assume that
     * a 503 Unavailable is the result of a node-wide limit being reached, and that a 429 is specific
     * to an individual endpoint on a node. These assumptions do not hold true in all cases, so
     * {@link DueTo} informs relays this intent.
     */
    public enum DueTo {
        /**
         * A cause that the RPC system isn't directly aware of, for example a user or user-agent specific limit, or
         * based on a specific resource that's being accessed, as opposed to the target node as a whole, or endpoint.
         * QosReasons with this cause shouldn't impact things like the dialogue concurrency limiter.
         */
        CUSTOM;
    }

    private static void checkReason(@Safe String reason) {
        if (reason == null || reason.isEmpty() || reason.length() > 50) {
            throw invalidReason(reason);
        }
        for (int i = 0; i < reason.length(); i++) {
            char character = reason.charAt(i);
            boolean validCharacter = (character >= 'a' && character <= 'z')
                    || (character >= '0' && character <= '9')
                    || character == '-';
            if (!validCharacter) {
                throw invalidReason(reason);
            }
        }
    }

    @CheckReturnValue
    private static SafeIllegalArgumentException invalidReason(@Safe String reason) {
        return new SafeIllegalArgumentException(
                "Reason must be at most 50 characters, and only contain lowercase letters, numbers, "
                        + "and hyphens (-).",
                SafeArg.of("reason", reason));
    }
}
