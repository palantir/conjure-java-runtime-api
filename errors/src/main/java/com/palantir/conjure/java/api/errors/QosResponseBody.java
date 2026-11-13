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

package com.palantir.conjure.java.api.errors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.palantir.logsafe.Preconditions;
import java.util.Objects;
import java.util.Optional;
import javax.annotation.Nullable;

/**
 * The JSON body of a {@link QosException} response, currently only written for a {@link QosException.Throttle} with
 * {@link QosThrottleDetails}. Unknown fields are ignored, so fields can be added later.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class QosResponseBody {
    @Nullable
    @JsonProperty("qosDetails")
    private final QosThrottleDetails qosDetails;

    @JsonCreator
    private QosResponseBody(@JsonProperty("qosDetails") @Nullable QosThrottleDetails qosDetails) {
        this.qosDetails = qosDetails;
    }

    public static QosResponseBody of(QosThrottleDetails qosDetails) {
        return new QosResponseBody(Preconditions.checkNotNull(qosDetails, "qosDetails"));
    }

    public Optional<QosThrottleDetails> qosDetails() {
        return Optional.ofNullable(qosDetails);
    }

    @Override
    public boolean equals(@Nullable Object other) {
        return this == other || (other instanceof QosResponseBody that && Objects.equals(qosDetails, that.qosDetails));
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(qosDetails);
    }

    @Override
    public String toString() {
        return "QosResponseBody{qosDetails=" + qosDetails() + '}';
    }
}
