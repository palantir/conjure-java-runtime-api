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

package com.palantir.conjure.java.api.orca;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

final class OrcaLoadReportsTest {

    @Test
    void round_trips_supported_fields() {
        OrcaLoadReport original = OrcaLoadReport.builder()
                .cpuUtilization(0.1)
                .memUtilization(0.2)
                .rpsFractional(5.5)
                .eps(0.6)
                .applicationUtilization(0.8)
                .build();
        Map<String, String> headers = new HashMap<>();

        OrcaLoadReports.encodeToResponse(original, headers, HeaderAdapter.INSTANCE);

        assertThat(OrcaLoadReports.parseFromResponse(headers, HeaderAdapter.INSTANCE))
                .contains(original);
    }

    @Test
    void empty_report_does_not_add_a_header() {
        Map<String, String> headers = new HashMap<>();

        OrcaLoadReports.encodeToResponse(OrcaLoadReport.builder().build(), headers, HeaderAdapter.INSTANCE);

        assertThat(headers).isEmpty();
    }

    @Test
    void uses_the_text_wire_format() {
        Map<String, String> headers = new HashMap<>();
        OrcaLoadReports.encodeToResponse(
                OrcaLoadReport.builder()
                        .cpuUtilization(0.1)
                        .memUtilization(0.2)
                        .rpsFractional(5.5)
                        .eps(0.6)
                        .applicationUtilization(0.8)
                        .build(),
                headers,
                HeaderAdapter.INSTANCE);

        assertThat(headers)
                .containsEntry(
                        "endpoint-load-metrics",
                        "TEXT cpu_utilization=0.1,mem_utilization=0.2,rps_fractional=5.5,eps=0.6,"
                                + "application_utilization=0.8");
    }

    @Test
    void parses_the_text_wire_format() {
        Map<String, String> headers = Map.of(
                "endpoint-load-metrics",
                "TEXT cpu_utilization=0.1,mem_utilization:0.2,rps_fractional=5.5,eps:0.6,"
                        + "application_utilization=0.8");

        assertThat(OrcaLoadReports.parseFromResponse(headers, HeaderAdapter.INSTANCE))
                .contains(OrcaLoadReport.builder()
                        .cpuUtilization(0.1)
                        .memUtilization(0.2)
                        .rpsFractional(5.5)
                        .eps(0.6)
                        .applicationUtilization(0.8)
                        .build());
    }

    @Test
    void ignores_invalid_fields_without_discarding_valid_fields() {
        Map<String, String> headers =
                Map.of("endpoint-load-metrics", "TEXT mem_utilization=1.01,application_utilization=0.8");

        assertThat(OrcaLoadReports.parseFromResponse(headers, HeaderAdapter.INSTANCE))
                .contains(OrcaLoadReport.builder().applicationUtilization(0.8).build());
    }

    @Test
    void preserves_explicit_zero_values() {
        Map<String, String> headers = Map.of("endpoint-load-metrics", "TEXT cpu_utilization=0.0");

        assertThat(OrcaLoadReports.parseFromResponse(headers, HeaderAdapter.INSTANCE))
                .contains(OrcaLoadReport.builder().cpuUtilization(0.0).build());
    }

    @Test
    void round_trips_named_metrics() {
        OrcaLoadReport original = OrcaLoadReport.builder()
                .applicationUtilization(0.8)
                .namedMetrics(Map.of("queue_depth", 12.0))
                .build();
        Map<String, String> headers = new HashMap<>();

        OrcaLoadReports.encodeToResponse(original, headers, HeaderAdapter.INSTANCE);

        assertThat(headers)
                .containsEntry(
                        "endpoint-load-metrics", "TEXT application_utilization=0.8,named_metrics.queue_depth=12.0");
        assertThat(OrcaLoadReports.parseFromResponse(headers, HeaderAdapter.INSTANCE))
                .contains(original);
    }

    @Test
    void percent_encodes_named_metric_keys() {
        OrcaLoadReport original = OrcaLoadReport.builder()
                .namedMetrics(Map.of("foo,bar=baz", 1.0))
                .build();
        Map<String, String> headers = new HashMap<>();

        OrcaLoadReports.encodeToResponse(original, headers, HeaderAdapter.INSTANCE);

        assertThat(headers).containsEntry("endpoint-load-metrics", "TEXT named_metrics.foo%2Cbar%3Dbaz=1.0");
        assertThat(OrcaLoadReports.parseFromResponse(headers, HeaderAdapter.INSTANCE))
                .contains(original);
    }

    @Test
    void drops_named_metrics_with_non_numeric_values() {
        Map<String, String> headers =
                Map.of("endpoint-load-metrics", "TEXT named_metrics.ok=1.0,named_metrics.bad=oops");

        assertThat(OrcaLoadReports.parseFromResponse(headers, HeaderAdapter.INSTANCE))
                .contains(
                        OrcaLoadReport.builder().namedMetrics(Map.of("ok", 1.0)).build());
    }

    @Test
    void duplicate_fields_discard_the_report() {
        Map<String, String> headers = Map.of(
                "endpoint-load-metrics", "TEXT cpu_utilization=0.1,cpu_utilization:0.2,application_utilization=0.8");

        assertThat(OrcaLoadReports.parseFromResponse(headers, HeaderAdapter.INSTANCE))
                .isEmpty();
    }

    @Test
    void missing_header_returns_empty() {
        assertThat(OrcaLoadReports.parseFromResponse(Map.of(), HeaderAdapter.INSTANCE))
                .isEmpty();
    }

    @Test
    void malformed_header_returns_empty() {
        Map<String, String> headers = Map.of("endpoint-load-metrics", "cpu_utilization=0.3");

        assertThat(OrcaLoadReports.parseFromResponse(headers, HeaderAdapter.INSTANCE))
                .isEmpty();
    }

    private enum HeaderAdapter
            implements
                    OrcaLoadReports.OrcaResponseEncodingAdapter<Map<String, String>>,
                    OrcaLoadReports.OrcaResponseDecodingAdapter<Map<String, String>> {
        INSTANCE;

        @Override
        public void setHeader(Map<String, String> response, String headerName, String headerValue) {
            response.put(headerName, headerValue);
        }

        @Override
        public Optional<String> getFirstHeader(Map<String, String> response, String headerName) {
            return Optional.ofNullable(response.get(headerName));
        }
    }
}
