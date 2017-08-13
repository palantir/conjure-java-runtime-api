/*
 * Copyright 2017 Palantir Technologies, Inc. All rights reserved.
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

package com.palantir.remoting.api.errors;

import com.palantir.logsafe.Arg;
import com.palantir.logsafe.SafeArg;
import com.palantir.logsafe.SafeLoggable;
import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * An exception raised by a service to indicate a potential Quality-of-Service problem, specifically requesting that the
 * client making the request should retry the request again now/later/never, possibly against a different node of this
 * service. Typically, this exception gets translated into appropriate error codes of the underlying transport layer,
 * e.g., HTTP status codes 429, 503, etc. in the case of HTTP transport.
 */
public class QosException extends RuntimeException {

    // Not meant for subclassing outside this class.
    private QosException() {}

    /**
     * Returns a {@link Retry} exception indicating that the calling client should retry the request without delay and
     * against an arbitrary node of this service.
     */
    public static Retry retryNow() {
        return new Retry(Optional.empty(), Optional.empty());
    }

    /**
     * Returns a {@link Retry} exception indicating that the calling client should retry the given minimum delay and
     * against an arbitrary node of this service.
     */
    public static Retry retryLater(Duration backoff) {
        return new Retry(Optional.empty(), Optional.of(backoff));

    }

    /**
     * Returns a {@link Retry} exception indicating that the calling client should retry the request without delay and
     * against the given node of this service.
     */
    public static Retry retryOther(URL redirectTo) {
        return new Retry(Optional.of(redirectTo), Optional.empty());

    }

    /**
     * See {@link Unavailable}.
     */
    public static Unavailable unavailable() {
        return new Unavailable();
    }

    /**
     * An exception indicating that a request against this service may be retried, potentially against a different node
     * of this service and with a given delay.
     */
    public static final class Retry extends QosException implements SafeLoggable {
        private final Optional<URL> redirectTo;
        private final Optional<Duration> backoff;

        private Retry(Optional<URL> redirectTo, Optional<Duration> backoff) {
            this.redirectTo = redirectTo;
            this.backoff = backoff;
        }

        /**
         * If non-empty, indicates an alternative URL of this service against which the request may be retried. If
         * empty, indicates that an arbitrary node may be used for the retry.
         */
        public Optional<URL> getRedirectTo() {
            return redirectTo;
        }

        /** If non-empty, indicates the minimum suggest delay/backoff time before the request is retried. */
        public Optional<Duration> getBackoff() {
            return backoff;
        }

        @Override
        public String getLogMessage() {
            return this.getClass().getSimpleName() + ": Requesting retry";
        }

        @Override
        public List<Arg<?>> getArgs() {
            List<Arg<?>> args = new ArrayList<>();
            redirectTo.ifPresent(r -> args.add(SafeArg.of("redirectTo", r)));
            backoff.ifPresent(b -> args.add(SafeArg.of("backoff", b)));
            return args;
        }
    }

    /**
     * An exception indicating that all nodes of this service are currently unavailable and the client shall not attempt
     * to wait for it to become available again. Typically, human intervention may be required to bring this service
     * back up.
     */
    public static final class Unavailable extends QosException implements SafeLoggable {

        @Override
        public String getLogMessage() {
            return this.getClass().getSimpleName() + ": Service unavailable";
        }

        @Override
        public List<Arg<?>> getArgs() {
            return Collections.emptyList();
        }
    }
}
