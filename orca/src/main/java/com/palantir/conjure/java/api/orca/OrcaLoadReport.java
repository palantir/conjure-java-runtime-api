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

import com.palantir.logsafe.SafeArg;
import com.palantir.logsafe.exceptions.SafeIllegalArgumentException;
import java.util.Map;
import java.util.OptionalDouble;
import org.immutables.value.Value;

/** A transport-independent representation of an Open Request Cost Aggregation (ORCA) load report. */
@Value.Immutable
@ImmutablesStyle
public interface OrcaLoadReport {

    /** CPU utilization as a fraction of available CPU resources. Values may exceed one. */
    OptionalDouble cpuUtilization();

    /** Memory utilization as a fraction of available memory resources. */
    OptionalDouble memUtilization();

    /** Total requests per second served by the endpoint. */
    OptionalDouble rpsFractional();

    /** Total errors per second served by the endpoint. */
    OptionalDouble eps();

    /** Application-specific opaque metrics. */
    Map<String, Double> namedMetrics();

    /** Application-specific utilization as a fraction of available resources. Values may exceed one. */
    OptionalDouble applicationUtilization();

    @Value.Check
    default void check() {
        cpuUtilization().ifPresent(value -> checkNonNegative("cpuUtilization", value));
        memUtilization().ifPresent(value -> checkFraction("memUtilization", value));
        rpsFractional().ifPresent(value -> checkNonNegative("rpsFractional", value));
        eps().ifPresent(value -> checkNonNegative("eps", value));
        namedMetrics().forEach(OrcaLoadReport::checkFinite);
        applicationUtilization().ifPresent(value -> checkNonNegative("applicationUtilization", value));
    }

    static Builder builder() {
        return new Builder();
    }

    final class Builder extends ImmutableOrcaLoadReport.Builder {}

    private static void checkNonNegative(String field, double value) {
        checkFinite(field, value);
        if (value < 0.0) {
            throw new SafeIllegalArgumentException(
                    "ORCA metric must be greater than or equal to zero", SafeArg.of("metric", field));
        }
    }

    private static void checkFraction(String field, double value) {
        checkNonNegative(field, value);
        if (value > 1.0) {
            throw new SafeIllegalArgumentException(
                    "ORCA metric must be less than or equal to one", SafeArg.of("metric", field));
        }
    }

    private static void checkFinite(String field, double value) {
        if (!Double.isFinite(value)) {
            throw new SafeIllegalArgumentException("ORCA metric must be finite", SafeArg.of("metric", field));
        }
    }
}
