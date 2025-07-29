/*
 * (c) Copyright 2025 Palantir Technologies Inc. All rights reserved.
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
import java.util.Optional;

/**
 * Utility methods for encoding and decoding the expected format for Conjure error parameters in HTTP requests.
 *
 * <p>This class provides methods for clients to set their expected format for receiving error parameters
 * from servers, and for servers to parse these preferences from incoming requests.
 *
 * <p>The format is transmitted via the {@value #ACCEPT_CONJURE_ERROR_PARAMETER_FORMAT_HEADER} HTTP header.
 */
public final class ConjureErrorParameterFormats {
    // The HTTP header name used to specify the expected Conjure error parameter format.
    // VisibleForTesting
    static final String ACCEPT_CONJURE_ERROR_PARAMETER_FORMAT_HEADER = "Accept-Conjure-Error-Parameter-Format";

    /**
     * Encodes the error parameter format into an outgoing request via HTTP header.
     *
     * <p>This method sets the {@value #ACCEPT_CONJURE_ERROR_PARAMETER_FORMAT_HEADER} header on the
     * provided request object using the supplied adapter.
     *
     * @param <T> the type of request object
     * @param format the format for error parameters
     * @param request the request object to set headers on
     * @param adapter the adapter for setting headers on the request
     */
    public static <T> void encodeToRequest(
            ConjureErrorParameterFormat format,
            T request,
            ConjureErrorParameterFormatRequestEncodingAdapter<? super T> adapter) {
        Preconditions.checkNotNull(format, "Conjure error parameter format is required");
        Preconditions.checkNotNull(request, "Request is required");
        Preconditions.checkNotNull(adapter, "Adapter is required");

        adapter.setHeader(request, ACCEPT_CONJURE_ERROR_PARAMETER_FORMAT_HEADER, format.toString());
    }

    /**
     * Parses the error parameter format from an incoming request's HTTP headers.
     *
     * <p>This method reads the {@value #ACCEPT_CONJURE_ERROR_PARAMETER_FORMAT_HEADER} header from the
     * provided request object using the supplied adapter.
     *
     * @param <T> the type of request object
     * @param request the request object to read headers from
     * @param adapter the adapter for reading headers from the request
     * @return the format for error parameters, or empty if not specified
     */
    public static <T> Optional<ConjureErrorParameterFormat> parseFromRequest(
            T request, ConjureErrorParameterFormatRequestDecodingAdapter<? super T> adapter) {
        Preconditions.checkNotNull(request, "Request is required");
        Preconditions.checkNotNull(adapter, "Adapter is required");

        String headerValue = adapter.getFirstHeader(request, ACCEPT_CONJURE_ERROR_PARAMETER_FORMAT_HEADER);
        if (headerValue == null || headerValue.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(ConjureErrorParameterFormat.valueOf(headerValue));
    }

    public interface ConjureErrorParameterFormatRequestEncodingAdapter<RESPONSE> {
        void setHeader(RESPONSE response, String headerName, String headerValue);
    }

    public interface ConjureErrorParameterFormatRequestDecodingAdapter<RESPONSE> {
        String getFirstHeader(RESPONSE response, String headerName);
    }

    private ConjureErrorParameterFormats() {}
}
