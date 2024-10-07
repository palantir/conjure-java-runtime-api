/*
 * (c) Copyright 2024 Palantir Technologies Inc. All rights reserved.
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

import com.palantir.conjure.java.api.errors.QosReason.DueTo;
import com.palantir.conjure.java.api.errors.QosReason.RetryHint;
import java.util.Optional;

public final class QosReasons {

    private static final String CLIENT_REASON = "client-qos-response";
    private static final QosReason DEFAULT_CLIENT_REASON = QosReason.of(CLIENT_REASON);
    private static final String DUE_TO_HEADER = "Qos-Due-To";
    private static final String RETRY_HINT_HEADER = "Qos-Retry-Hint";

    private static final String DUE_TO_CUSTOM_HEADER_VALUE = "custom";
    private static final String RETRY_HINT_PROPAGATE_HEADER_VALUE = "propagate";

    public static <T> void encodeToResponse(
            QosReason reason, T response, QosResponseEncodingAdapter<? super T> adapter) {
        // Likely hot path, avoid ifPresent lambda
        if (reason.dueTo().isPresent()) {
            adapter.setHeader(
                    response, DUE_TO_HEADER, toHeaderValue(reason.dueTo().get()));
        }
        if (reason.retryHint().isPresent()) {
            adapter.setHeader(
                    response,
                    RETRY_HINT_HEADER,
                    toHeaderValue(reason.retryHint().get()));
        }
    }

    public static <T> QosReason parseFromResponse(T response, QosResponseDecodingAdapter<? super T> adapter) {
        Optional<String> maybeDueTo = adapter.getFirstHeader(response, DUE_TO_HEADER);
        Optional<String> maybeRetryHint = adapter.getFirstHeader(response, RETRY_HINT_HEADER);
        if (maybeDueTo.isEmpty() && maybeRetryHint.isEmpty()) {
            return DEFAULT_CLIENT_REASON;
        }
        return QosReason.builder()
                .reason(CLIENT_REASON)
                .dueTo(maybeDueTo.flatMap(QosReasons::parseDueTo))
                .retryHint(maybeRetryHint.flatMap(QosReasons::parseRetryHint))
                .build();
    }

    public interface QosResponseEncodingAdapter<RESPONSE> {
        void setHeader(RESPONSE response, String headerName, String headerValue);
    }

    public interface QosResponseDecodingAdapter<RESPONSE> {
        Optional<String> getFirstHeader(RESPONSE response, String headerName);
    }

    // VisibleForTesting
    static Optional<DueTo> parseDueTo(String dueTo) {
        if (DUE_TO_CUSTOM_HEADER_VALUE.equalsIgnoreCase(dueTo)) {
            return Optional.of(DueTo.CUSTOM);
        }
        return Optional.empty();
    }

    // VisibleForTesting
    static Optional<RetryHint> parseRetryHint(String retryHint) {
        if (RETRY_HINT_PROPAGATE_HEADER_VALUE.equalsIgnoreCase(retryHint)) {
            return Optional.of(RetryHint.PROPAGATE);
        }
        return Optional.empty();
    }

    // VisibleForTesting
    static String toHeaderValue(DueTo dueTo) {
        return switch (dueTo) {
            case CUSTOM -> DUE_TO_CUSTOM_HEADER_VALUE;
        };
    }

    // VisibleForTesting
    static String toHeaderValue(RetryHint retryHint) {
        return switch (retryHint) {
            case PROPAGATE -> RETRY_HINT_PROPAGATE_HEADER_VALUE;
        };
    }

    private QosReasons() {}
}
