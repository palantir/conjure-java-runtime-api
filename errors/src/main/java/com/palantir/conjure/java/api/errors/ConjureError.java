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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.palantir.logsafe.Arg;
import java.util.Map;
import org.immutables.value.Value;

@SuppressWarnings("ImmutablesStyle")
@JsonDeserialize(builder = ConjureError.Builder.class)
@JsonSerialize(as = ImmutableConjureError.class)
@Value.Immutable
@Value.Style(overshadowImplementation = false)
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class ConjureError {
    /**
     * docs.
     */
    @JsonProperty("errorCode")
    public abstract String errorCode();

    /**
     * docs.
     */
    @JsonProperty("errorName")
    public abstract String errorName();

    @JsonProperty("errorInstanceId")
    @Value.Default
    @SuppressWarnings("checkstyle:designforextension")
    public String errorInstanceId() {
        return "";
    }

    public abstract Map<String, Object> parameters();

    public static ConjureError forException(CheckedServiceException exception) {
        Builder builder = new Builder()
                .errorCode(exception.getErrorType().code().name())
                .errorName(exception.getErrorType().name())
                .errorInstanceId(exception.getErrorInstanceId());

        for (Arg<?> arg : exception.getArgs()) {
            builder.putParameters(arg.getName(), arg.getValue());
        }

        return builder.build();
    }

    public static final class Builder extends ImmutableConjureError.Builder {}

    public static Builder builder() {
        return new Builder();
    }
}
