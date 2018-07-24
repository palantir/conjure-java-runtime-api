/*
 * (c) Copyright 2017 Palantir Technologies Inc. All rights reserved.
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

package com.palantir.remoting.api.config.service;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;
import org.immutables.value.Value.Immutable;

@Immutable
@JsonSerialize(as = ImmutableBasicCredentials.class)
@JsonDeserialize(as = ImmutableBasicCredentials.class)
@ImmutablesStyle
public abstract class BasicCredentials {

    @Value.Parameter
    public abstract String username();

    @Value.Parameter
    public abstract String password();

    /**
     * Returns Conjure's {@link com.palantir.conjure.java.api.config.service.BasicCredentials} type for forward
     * compatibility.
     */
    @Value.Lazy
    public com.palantir.conjure.java.api.config.service.BasicCredentials asConjure() {
        return com.palantir.conjure.java.api.config.service.BasicCredentials.of(username(), password());
    }

    public static BasicCredentials of(String username, String password) {
        return ImmutableBasicCredentials.of(username, password);
    }
}
