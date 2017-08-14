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
import java.util.ArrayList;
import java.util.List;

/**
 * An exception raised by a service to indicate a potential Quality-of-Service problem, specifically requesting that the
 * client making the request should retry the request again now/later/never, possibly against a different node of this
 * service. Typically, this exception gets translated into appropriate error codes of the underlying transport layer,
 * e.g., HTTP status codes 429, 503, etc. in the case of HTTP transport.
 */
public class QosException extends RuntimeException {

    // Not meant for external subclassing.
    private QosException() {}

    /**
     * Returns a {@link Retry} exception indicating that the calling client should retry the request against an
     * arbitrary node of this service.
     */
    public static Retry retry() {
        return new Retry();
    }

    /**
     * Returns a {@link RetryOther} exception indicating that the calling client should retry against the given node of
     * this service.
     */
    public static RetryOther retryOther(URL redirectTo) {
        return new RetryOther(redirectTo);
    }

    /**
     * An exception indicating that all nodes of this service are currently unavailable and the client shall not attempt
     * to wait for it to become available again. Typically, human intervention may be required to bring this service
     * back up.
     */
    public static Unavailable unavailable() {
        return new Unavailable();
    }

    /** See {@link #retry}. */
    public static final class Retry extends QosException {
        private Retry() {}
    }

    /** See {@link #retryOther}. */
    public static final class RetryOther extends QosException implements SafeLoggable {
        private final URL redirectTo;

        private RetryOther(URL redirectTo) {
            this.redirectTo = redirectTo;
        }

        /** Indicates an alternative URL of this service against which the request may be retried. */
        public URL getRedirectTo() {
            return redirectTo;
        }

        @Override
        public String getLogMessage() {
            return this.getClass().getSimpleName() + ": Requesting retry";
        }

        @Override
        public List<Arg<?>> getArgs() {
            List<Arg<?>> args = new ArrayList<>();
            args.add(SafeArg.of("redirectTo", redirectTo));
            return args;
        }
    }

    /** See {@link #unavailable}. */
    public static final class Unavailable extends QosException {
        private Unavailable() {}
    }
}
