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

package com.palantir.conjure.java.api.errors;

import static org.assertj.core.api.Assertions.assertThat;

import com.palantir.logsafe.SafeArg;
import com.palantir.logsafe.UnsafeArg;
import org.junit.jupiter.api.Test;

public final class FieldMissingExceptionTest {

    @Test
    public void asSerializableError_should_keep_both_safe_and_unsafe_args() {
        FieldMissingException exception = new FieldMissingException(
                SafeArg.of("safeField", "a safe field"), UnsafeArg.of("sensitiveField", "some sensitive field"));

        SerializableError expected = new SerializableError.Builder()
                .errorCode(FieldMissingException.ERROR_TYPE.code().name())
                .errorName(FieldMissingException.ERROR_TYPE.name())
                .errorInstanceId(exception.getErrorInstanceId())
                .putParameters("safeField", "a safe field")
                .putParameters("sensitiveField", "some sensitive field")
                .build();
        assertThat(exception.asSerializableError()).isEqualTo(expected);
    }
}
