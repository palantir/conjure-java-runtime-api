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
import java.io.Serializable;
import java.util.List;
import org.immutables.value.Value;

@SuppressWarnings("ImmutablesStyle")
@JsonDeserialize(builder = SerializableConjureDefinedError.Builder.class)
@JsonSerialize(as = ImmutableSerializableConjureDefinedError.class)
@Value.Immutable
@Value.Style(overshadowImplementation = false)
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class SerializableConjureDefinedError implements Serializable {

    @JsonProperty("errorCode")
    public abstract String errorCode();

    @JsonProperty("errorName")
    public abstract String errorName();

    @JsonProperty("errorInstanceId")
    @Value.Default
    @SuppressWarnings("checkstyle:designforextension")
    public String errorInstanceId() {
        return "";
    }

    /** A set of parameters that further explain the error. It's up to the creator of this object to serialize the value
     *  in SerializableConjureErrorParameter.
     **/
    public abstract List<SerializableConjureErrorParameter> parameters();

    // In Conjure-Java - ConjureExceptions.java we'd create this object:
    //    public static SerializableConjureDefinedError forException(CheckedServiceException exception) {
    //        return builder()
    //                .errorCode(exception.getErrorType().code().name())
    //                .errorName(exception.getErrorType().name())
    //                .errorInstanceId(exception.getErrorInstanceId())
    //                .parameters(exception.getArgs().stream()
    //                        .map(arg -> SerializableConjureErrorParameter.builder()
    //                                .name(arg.getName())
    //                                .serializedValue() // Serialize the parameter
    //                                .isSafeForLogging(arg.isSafeForLogging())
    //                                .build())
    //                        .toList())
    //                .build();
    //    }

    public static final class Builder extends ImmutableSerializableConjureDefinedError.Builder {}

    public static Builder builder() {
        return new Builder();
    }
}
