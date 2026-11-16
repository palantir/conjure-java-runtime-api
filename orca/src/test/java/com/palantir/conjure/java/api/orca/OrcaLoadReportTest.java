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
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import java.util.Map;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

final class OrcaLoadReportTest {

    @Test
    void defaults_to_a_report_without_metrics() {
        OrcaLoadReport report = OrcaLoadReport.builder().build();

        assertThat(report.cpuUtilization()).isEmpty();
        assertThat(report.memUtilization()).isEmpty();
        assertThat(report.rpsFractional()).isEmpty();
        assertThat(report.eps()).isEmpty();
        assertThat(report.namedMetrics()).isEmpty();
        assertThat(report.applicationUtilization()).isEmpty();
    }

    @Nested
    final class Validation {

        @Test
        void rejects_negative_utilization() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> OrcaLoadReport.builder()
                            .applicationUtilization(-0.1)
                            .build());
        }

        @Test
        void allows_application_utilization_above_one() {
            OrcaLoadReport report =
                    OrcaLoadReport.builder().applicationUtilization(1.1).build();

            assertThat(report.applicationUtilization()).hasValue(1.1);
        }

        @Test
        void rejects_memory_utilization_above_one() {
            assertThatIllegalArgumentException()
                    .isThrownBy(
                            () -> OrcaLoadReport.builder().memUtilization(1.1).build());
        }

        @Test
        void rejects_non_finite_metrics() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> OrcaLoadReport.builder()
                            .namedMetrics(Map.of("custom", Double.NaN))
                            .build());
        }
    }
}
