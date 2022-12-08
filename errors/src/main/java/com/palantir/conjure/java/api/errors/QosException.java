/*
 * (c) Copyright 2017 Palantir Technologies Inc. All rights reserved.
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

import com.palantir.logsafe.Arg;
import com.palantir.logsafe.SafeArg;
import com.palantir.logsafe.SafeLoggable;
import com.palantir.logsafe.UnsafeArg;
import java.net.URL;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * An exception raised by a service to indicate a potential Quality-of-Service problem, specifically requesting that the
 * client making the request should retry the request again now/later/never, possibly against a different node of this
 * service. Typically, this exception gets translated into appropriate error codes of the underlying transport layer,
 * e.g., HTTP status codes 429, 503, etc. in the case of HTTP transport.
 */
public abstract class QosException extends RuntimeException {

    private final QosReason reason;

    // Not meant for external subclassing.
    private QosException(String message, QosReason reason) {
        super(message);
        this.reason = reason;
    }

    private QosException(String message, Throwable cause, QosReason reason) {
        super(message, cause);
        this.reason = reason;
    }

    public QosReason getReason() {
        return reason;
    }

    public abstract <T> T accept(Visitor<T> visitor);

    public interface Visitor<T> {
        T visit(Throttle exception);

        T visit(RetryOther exception);

        T visit(Unavailable exception);
    }

    /**
     * Returns a {@link Throttle} exception indicating that the calling client should throttle its requests. The client
     * may retry against an arbitrary node of this service.
     */
    public static Throttle throttle() {
        return new Throttle(Optional.empty());
    }

    /**
     * Like {@link #throttle()}, but includes a reason.
     */
    public static Throttle throttle(QosReason reason) {
        return new Throttle(Optional.empty(), reason);
    }

    /**
     * Like {@link #throttle()}, but includes a cause.
     */
    public static Throttle throttle(Throwable cause) {
        return new Throttle(Optional.empty(), cause);
    }

    /**
     * Like {@link #throttle()}, but includes a reason, and a cause.
     */
    public static Throttle throttle(QosReason reason, Throwable cause) {
        return new Throttle(Optional.empty(), cause, reason);
    }

    /**
     * Like {@link #throttle()}, but additionally requests that the client wait for at least the given duration before
     * retrying the request.
     */
    public static Throttle throttle(Duration duration) {
        return new Throttle(Optional.of(duration));
    }

    /**
     * Like {@link #throttle(Duration)}, but includes a reason.
     */
    public static Throttle throttle(QosReason reason, Duration duration) {
        return new Throttle(Optional.of(duration), reason);
    }

    /**
     * Like {@link #throttle(Duration)}, but includes a cause.
     */
    public static Throttle throttle(Duration duration, Throwable cause) {
        return new Throttle(Optional.of(duration), cause);
    }

    /**
     * Like {@link #throttle(Duration)}, but includes a reason, and a cause.
     */
    public static Throttle throttle(QosReason reason, Duration duration, Throwable cause) {
        return new Throttle(Optional.of(duration), cause, reason);
    }

    /**
     * Returns a {@link RetryOther} exception indicating that the calling client should retry against the given node of
     * this service.
     */
    public static RetryOther retryOther(URL redirectTo) {
        return new RetryOther(redirectTo);
    }

    /**
     * Like {@link #retryOther(URL)}, but includes a reason.
     */
    public static RetryOther retryOther(QosReason reason, URL redirectTo) {
        return new RetryOther(redirectTo, reason);
    }

    /**
     * Like {@link #retryOther(URL)}, but includes a cause.
     */
    public static RetryOther retryOther(URL redirectTo, Throwable cause) {
        return new RetryOther(redirectTo, cause);
    }

    /**
     * Like {@link #retryOther(URL)}, but includes a reason, and a cause.
     */
    public static RetryOther retryOther(QosReason reason, URL redirectTo, Throwable cause) {
        return new RetryOther(redirectTo, cause, reason);
    }

    /**
     * An exception indicating that (this node of) this service is currently unavailable and the client may try again at
     * a later time, possibly against a different node of this service.
     */
    public static Unavailable unavailable() {
        return new Unavailable();
    }

    /**
     * Like {@link #unavailable()}, but includes a reason.
     */
    public static Unavailable unavailable(QosReason reason) {
        return new Unavailable(reason);
    }

    /**
     * Like {@link #unavailable()}, but includes a cause.
     */
    public static Unavailable unavailable(Throwable cause) {
        return new Unavailable(cause);
    }

    /**
     * Like {@link #unavailable()}, but includes a reason, and a cause.
     */
    public static Unavailable unavailable(QosReason reason, Throwable cause) {
        return new Unavailable(cause, reason);
    }

    /** See {@link #throttle}. */
    public static final class Throttle extends QosException implements SafeLoggable {
        private static final QosReason DEFAULT_REASON = QosReason.of("qos-throttle");
        private final Optional<Duration> retryAfter;

        private Throttle(Optional<Duration> retryAfter) {
            super("Suggesting request throttling with optional retryAfter duration: " + retryAfter, DEFAULT_REASON);
            this.retryAfter = retryAfter;
        }

        private Throttle(Optional<Duration> retryAfter, QosReason reason) {
            super("Suggesting request throttling with optional retryAfter duration: " + retryAfter, reason);
            this.retryAfter = retryAfter;
        }

        private Throttle(Optional<Duration> retryAfter, Throwable cause) {
            super(
                    "Suggesting request throttling with optional retryAfter duration: " + retryAfter,
                    cause,
                    DEFAULT_REASON);
            this.retryAfter = retryAfter;
        }

        private Throttle(Optional<Duration> retryAfter, Throwable cause, QosReason reason) {
            super("Suggesting request throttling with optional retryAfter duration: " + retryAfter, cause, reason);
            this.retryAfter = retryAfter;
        }

        public Optional<Duration> getRetryAfter() {
            return retryAfter;
        }

        @Override
        public <T> T accept(Visitor<T> visitor) {
            return visitor.visit(this);
        }

        @Override
        public String getLogMessage() {
            return "Suggested request throttling";
        }

        @Override
        public List<Arg<?>> getArgs() {
            return Collections.singletonList(SafeArg.of("retryAfter", retryAfter.orElse(null)));
        }
    }

    /** See {@link #retryOther}. */
    public static final class RetryOther extends QosException implements SafeLoggable {
        private static final QosReason DEFAULT_REASON = QosReason.of("qos-retry-other");
        private final URL redirectTo;

        private RetryOther(URL redirectTo) {
            super("Suggesting request retry against: " + redirectTo.toString(), DEFAULT_REASON);
            this.redirectTo = redirectTo;
        }

        private RetryOther(URL redirectTo, QosReason reason) {
            super("Suggesting request retry against: " + redirectTo.toString(), reason);
            this.redirectTo = redirectTo;
        }

        private RetryOther(URL redirectTo, Throwable cause) {
            super("Suggesting request retry against: " + redirectTo.toString(), cause, DEFAULT_REASON);
            this.redirectTo = redirectTo;
        }

        private RetryOther(URL redirectTo, Throwable cause, QosReason reason) {
            super("Suggesting request retry against: " + redirectTo.toString(), cause, reason);
            this.redirectTo = redirectTo;
        }

        /** Indicates an alternative URL of this service against which the request may be retried. */
        public URL getRedirectTo() {
            return redirectTo;
        }

        @Override
        public <T> T accept(Visitor<T> visitor) {
            return visitor.visit(this);
        }

        @Override
        public String getLogMessage() {
            return this.getClass().getSimpleName() + ": Requesting retry";
        }

        @Override
        public List<Arg<?>> getArgs() {
            return Collections.singletonList(UnsafeArg.of("redirectTo", redirectTo));
        }
    }

    /** See {@link #unavailable}. */
    public static final class Unavailable extends QosException implements SafeLoggable {
        private static final QosReason DEFAULT_REASON = QosReason.of("qos-unavailable");

        private static final String SERVER_UNAVAILABLE = "Server unavailable";

        private Unavailable() {
            super(SERVER_UNAVAILABLE, DEFAULT_REASON);
        }

        private Unavailable(QosReason reason) {
            super(SERVER_UNAVAILABLE, reason);
        }

        private Unavailable(Throwable cause) {
            super(SERVER_UNAVAILABLE, cause, DEFAULT_REASON);
        }

        private Unavailable(Throwable cause, QosReason reason) {
            super(SERVER_UNAVAILABLE, cause, reason);
        }

        @Override
        public <T> T accept(Visitor<T> visitor) {
            return visitor.visit(this);
        }

        @Override
        public String getLogMessage() {
            return SERVER_UNAVAILABLE;
        }

        @Override
        public List<Arg<?>> getArgs() {
            return Collections.emptyList();
        }
    }
}
