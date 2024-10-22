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
import org.immutables.value.Value;

@SuppressWarnings("ImmutablesStyle")
@JsonDeserialize(builder = SerializableConjureErrorParameter.Builder.class)
@JsonSerialize(as = ImmutableSerializableConjureErrorParameter.class)
@Value.Immutable
// This ensures that build() will return the concrete ImmutableSer... and not the abstract type.
@Value.Style(overshadowImplementation = false)
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class SerializableConjureErrorParameter implements Serializable {

    @JsonProperty("name")
    public abstract String name();

    @JsonProperty("serializedValue")
    public abstract String serializedValue();

    @JsonProperty("isSafeForLogging")
    public abstract boolean isSafeForLogging();

    public static final class Builder extends ImmutableSerializableConjureErrorParameter.Builder {}

    public static Builder builder() {
        return new Builder();
    }
}
