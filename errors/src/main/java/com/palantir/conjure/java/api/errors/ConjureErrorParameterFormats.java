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

public final class ConjureErrorParameterFormats {
    // The HTTP header name used to specify the expected Conjure error parameter format.
    private static final String ACCEPT_CONJURE_ERROR_PARAMETER_FORMAT_HEADER = "Accept-Conjure-Error-Parameter-Format";

    public static <T> void encodeToRequest(
            ConjureErrorParameterFormat format,
            T request,
            ConjureErrorParameterFormatRequestEncodingAdapter<? super T> adapter) {
        Preconditions.checkNotNull(format, "Conjure error parameter format is required");
        Preconditions.checkNotNull(request, "Request is required");
        Preconditions.checkNotNull(adapter, "Adapter is required");

        adapter.setHeader(request, ACCEPT_CONJURE_ERROR_PARAMETER_FORMAT_HEADER, format.toString());
    }

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
