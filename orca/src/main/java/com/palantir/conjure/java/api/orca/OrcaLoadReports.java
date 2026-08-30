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

import com.palantir.logsafe.exceptions.SafeIllegalArgumentException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;

/**
 * Utilities for encoding and decoding ORCA load reports using the {@code endpoint-load-metrics} HTTP response header.
 * Values use a comma-separated format beginning with the literal {@code TEXT } prefix, for example
 * {@code TEXT cpu_utilization=0.3,mem_utilization=0.8,named_metrics.queue_depth=12.0}. Custom
 * {@code named_metrics.} keys are percent-encoded so reserved characters ({@code , = :}) round-trip safely.
 */
public final class OrcaLoadReports {

    private static final String LOAD_METRICS_HEADER = "endpoint-load-metrics";
    private static final String TEXT_PREFIX = "TEXT ";
    private static final String NAMED_METRICS_PREFIX = "named_metrics.";

    public static <T> void encodeToResponse(
            OrcaLoadReport report, T response, OrcaResponseEncodingAdapter<? super T> adapter) {
        if (isEmpty(report)) {
            return;
        }
        adapter.setHeader(response, LOAD_METRICS_HEADER, encode(report));
    }

    public static <T> Optional<OrcaLoadReport> parseFromResponse(
            T response, OrcaResponseDecodingAdapter<? super T> adapter) {
        Optional<String> header = adapter.getFirstHeader(response, LOAD_METRICS_HEADER);
        if (header.isEmpty()) {
            return Optional.empty();
        }
        return decode(header.get());
    }

    public interface OrcaResponseEncodingAdapter<RESPONSE> {
        void setHeader(RESPONSE response, String headerName, String headerValue);
    }

    public interface OrcaResponseDecodingAdapter<RESPONSE> {
        Optional<String> getFirstHeader(RESPONSE response, String headerName);
    }

    private static String encode(OrcaLoadReport report) {
        StringJoiner result = new StringJoiner(",", TEXT_PREFIX, "");
        if (report.cpuUtilization().isPresent()) {
            result.add("cpu_utilization=" + report.cpuUtilization().getAsDouble());
        }
        if (report.memUtilization().isPresent()) {
            result.add("mem_utilization=" + report.memUtilization().getAsDouble());
        }
        if (report.rpsFractional().isPresent()) {
            result.add("rps_fractional=" + report.rpsFractional().getAsDouble());
        }
        if (report.eps().isPresent()) {
            result.add("eps=" + report.eps().getAsDouble());
        }
        if (report.applicationUtilization().isPresent()) {
            result.add(
                    "application_utilization=" + report.applicationUtilization().getAsDouble());
        }
        report.namedMetrics()
                .forEach((name, value) -> result.add(NAMED_METRICS_PREFIX + encodeMetricName(name) + '=' + value));
        return result.toString();
    }

    private static Optional<OrcaLoadReport> decode(String header) {
        if (!header.startsWith(TEXT_PREFIX)) {
            return Optional.empty();
        }
        try {
            OrcaLoadReport.Builder builder = OrcaLoadReport.builder();
            Set<String> fields = new HashSet<>();
            String value = header.substring(TEXT_PREFIX.length());
            if (value.isEmpty()) {
                return Optional.empty();
            }
            for (String entry : value.split(",", -1)) {
                parseEntry(builder, fields, entry);
            }
            OrcaLoadReport report = builder.build();
            return isEmpty(report) ? Optional.empty() : Optional.of(report);
        } catch (IllegalArgumentException _exception) {
            return Optional.empty();
        }
    }

    private static void parseEntry(OrcaLoadReport.Builder builder, Set<String> fields, String entry) {
        int separator = separatorIndex(entry);
        if (separator <= 0 || separator == entry.length() - 1) {
            throw new SafeIllegalArgumentException("Invalid ORCA metric");
        }
        String name = entry.substring(0, separator);
        boolean namedMetric = name.startsWith(NAMED_METRICS_PREFIX);
        if (!namedMetric && !isRecognized(name)) {
            return;
        }
        checkDuplicate(fields, name);
        double value;
        try {
            value = Double.parseDouble(entry.substring(separator + 1));
        } catch (NumberFormatException _exception) {
            return;
        }
        if (namedMetric) {
            if (Double.isFinite(value)) {
                builder.putNamedMetrics(decodeMetricName(name), value);
            }
            return;
        }
        switch (name) {
            case "cpu_utilization" -> {
                if (isNonNegative(value)) {
                    builder.cpuUtilization(value);
                }
            }
            case "mem_utilization" -> {
                if (isFraction(value)) {
                    builder.memUtilization(value);
                }
            }
            case "rps_fractional" -> {
                if (isNonNegative(value)) {
                    builder.rpsFractional(value);
                }
            }
            case "eps" -> {
                if (isNonNegative(value)) {
                    builder.eps(value);
                }
            }
            case "application_utilization" -> {
                if (isNonNegative(value)) {
                    builder.applicationUtilization(value);
                }
            }
            default -> throw new SafeIllegalArgumentException("Invalid ORCA metric");
        }
    }

    private static int separatorIndex(String entry) {
        int equals = entry.indexOf('=');
        int colon = entry.indexOf(':');
        if (equals < 0) {
            return colon;
        }
        return colon < 0 ? equals : Math.min(equals, colon);
    }

    private static boolean isRecognized(String name) {
        return switch (name) {
            case "cpu_utilization", "mem_utilization", "rps_fractional", "eps", "application_utilization" -> true;
            default -> false;
        };
    }

    private static boolean isNonNegative(double value) {
        return Double.isFinite(value) && value >= 0.0;
    }

    private static boolean isFraction(double value) {
        return isNonNegative(value) && value <= 1.0;
    }

    private static String encodeMetricName(String name) {
        return URLEncoder.encode(name, StandardCharsets.UTF_8);
    }

    private static String decodeMetricName(String name) {
        return URLDecoder.decode(name.substring(NAMED_METRICS_PREFIX.length()), StandardCharsets.UTF_8);
    }

    private static void checkDuplicate(Set<String> fields, String field) {
        if (!fields.add(field)) {
            throw new SafeIllegalArgumentException("Duplicate ORCA metric");
        }
    }

    private static boolean isEmpty(OrcaLoadReport report) {
        return report.cpuUtilization().isEmpty()
                && report.memUtilization().isEmpty()
                && report.rpsFractional().isEmpty()
                && report.eps().isEmpty()
                && report.applicationUtilization().isEmpty()
                && report.namedMetrics().isEmpty();
    }

    private OrcaLoadReports() {}
}
