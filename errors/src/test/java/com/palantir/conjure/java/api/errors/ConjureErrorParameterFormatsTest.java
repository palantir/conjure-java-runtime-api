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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.palantir.conjure.java.api.errors.ConjureErrorParameterFormats.ConjureErrorParameterFormatRequestDecodingAdapter;
import com.palantir.conjure.java.api.errors.ConjureErrorParameterFormats.ConjureErrorParameterFormatRequestEncodingAdapter;
import com.palantir.logsafe.exceptions.SafeNullPointerException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

public final class ConjureErrorParameterFormatsTest {

    @Test
    public void encodeToRequest_setsHeaderCorrectly() {
        Map<String, String> request = new HashMap<>();
        ConjureErrorParameterFormat format = ConjureErrorParameterFormat.JSON_FORMAT;
        ConjureErrorParameterFormats.encodeToRequest(format, request, Encoder.INSTANCE);
        assertThat(request)
                .containsEntry(ConjureErrorParameterFormats.ACCEPT_CONJURE_ERROR_PARAMETER_FORMAT_HEADER, "JSON");
    }

    @Test
    public void encodeToRequest_null_checks() {
        Map<String, String> request = new HashMap<>();
        assertThatThrownBy(() -> ConjureErrorParameterFormats.encodeToRequest(null, request, Encoder.INSTANCE))
                .isInstanceOf(SafeNullPointerException.class)
                .hasMessage("Conjure error parameter format is required");

        ConjureErrorParameterFormat format = ConjureErrorParameterFormat.JSON_FORMAT;
        assertThatThrownBy(() -> ConjureErrorParameterFormats.encodeToRequest(format, null, Encoder.INSTANCE))
                .isInstanceOf(SafeNullPointerException.class)
                .hasMessage("Request is required");

        assertThatThrownBy(() -> ConjureErrorParameterFormats.encodeToRequest(format, request, null))
                .isInstanceOf(SafeNullPointerException.class)
                .hasMessage("Adapter is required");
    }

    @Test
    public void parseFromRequest_headerPresent_returnsFormat() {
        Map<String, String> request = new HashMap<>();
        request.put(ConjureErrorParameterFormats.ACCEPT_CONJURE_ERROR_PARAMETER_FORMAT_HEADER, "JSON");
        assertThat(ConjureErrorParameterFormats.parseFromRequest(request, Decoder.INSTANCE))
                .contains(ConjureErrorParameterFormat.JSON_FORMAT);
    }

    @Test
    public void parseFromRequest_headerMissing_returnsEmpty() {
        Map<String, String> request = new HashMap<>();
        assertThat(ConjureErrorParameterFormats.parseFromRequest(request, Decoder.INSTANCE))
                .isEmpty();
    }

    @Test
    public void parseFromRequest_headerEmpty_returnsEmpty() {
        Map<String, String> request = new HashMap<>();
        request.put(ConjureErrorParameterFormats.ACCEPT_CONJURE_ERROR_PARAMETER_FORMAT_HEADER, "");
        assertThat(ConjureErrorParameterFormats.parseFromRequest(request, Decoder.INSTANCE))
                .isEmpty();
    }

    @Test
    public void parseFromRequest_null_checks() {
        assertThatThrownBy(() -> ConjureErrorParameterFormats.parseFromRequest(null, Decoder.INSTANCE))
                .isInstanceOf(SafeNullPointerException.class)
                .hasMessage("Request is required");

        Map<String, String> request = new HashMap<>();
        assertThatThrownBy(() -> ConjureErrorParameterFormats.parseFromRequest(request, null))
                .isInstanceOf(SafeNullPointerException.class)
                .hasMessage("Adapter is required");
    }

    @Test
    public void parseFromRequest_customFormat_returnsCorrectFormat() {
        Map<String, String> request = new HashMap<>();
        request.put(ConjureErrorParameterFormats.ACCEPT_CONJURE_ERROR_PARAMETER_FORMAT_HEADER, "CUSTOM_FORMAT");
        Optional<ConjureErrorParameterFormat> result =
                ConjureErrorParameterFormats.parseFromRequest(request, Decoder.INSTANCE);
        assertThat(result).isPresent();
        assertThat(result.get().toString()).isEqualTo("CUSTOM_FORMAT");
    }

    @Test
    public void roundTrip() {
        Map<String, String> request = new HashMap<>();
        ConjureErrorParameterFormat originalFormat = ConjureErrorParameterFormat.JSON_FORMAT;
        ConjureErrorParameterFormats.encodeToRequest(originalFormat, request, Encoder.INSTANCE);
        Optional<ConjureErrorParameterFormat> parsedFormat =
                ConjureErrorParameterFormats.parseFromRequest(request, Decoder.INSTANCE);
        assertThat(parsedFormat).contains(originalFormat);
    }

    private enum Encoder implements ConjureErrorParameterFormatRequestEncodingAdapter<Map<String, String>> {
        INSTANCE;

        @Override
        public void setHeader(Map<String, String> request, String headerName, String headerValue) {
            request.put(headerName, headerValue);
        }
    }

    private enum Decoder implements ConjureErrorParameterFormatRequestDecodingAdapter<Map<String, String>> {
        INSTANCE;

        @Override
        public String getFirstHeader(Map<String, String> request, String headerName) {
            return request.get(headerName);
        }
    }
}
