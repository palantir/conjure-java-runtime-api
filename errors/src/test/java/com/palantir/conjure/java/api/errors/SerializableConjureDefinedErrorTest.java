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

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.palantir.logsafe.SafeArg;
import com.palantir.logsafe.UnsafeArg;
import java.util.Objects;
import org.junit.jupiter.api.Test;

public final class SerializableConjureDefinedErrorTest {
    @Test
    public void forException_should_keep_both_safe_and_unsafe_args() throws Exception {
        record FancyContent(int fancyValue, String fancyString) {
            static FancyContent of() {
                return new FancyContent(42, "hello");
            }
        }

        ErrorType errorType = ErrorType.FAILED_PRECONDITION;
        CheckedServiceException exception = new CheckedServiceException(
                errorType,
                SafeArg.of("safeKey", 42),
                UnsafeArg.of("sensitiveInfo", "some user-entered content"),
                SafeArg.of("fancyContent", FancyContent.of())) {};

        SerializableConjureDefinedError expected = SerializableConjureDefinedError.builder()
                .errorCode(errorType.code().name())
                .errorName(errorType.name())
                .errorInstanceId(exception.getErrorInstanceId())
                .addParameters(SerializableConjureErrorParameter.builder()
                        .name("safeKey")
                        .serializedValue("42")
                        .isSafeForLogging(true)
                        .build())
                .addParameters(SerializableConjureErrorParameter.builder()
                        .name("sensitiveInfo")
                        .serializedValue("some user-entered content")
                        .isSafeForLogging(false)
                        .build())
                .addParameters(SerializableConjureErrorParameter.builder()
                        .name("fancyContent")
                        .serializedValue("FancyContent[fancyValue=42, fancyString=hello]")
                        .isSafeForLogging(true)
                        .build())
                .build();
        assertThat(forException(exception)).isEqualTo(expected);
    }

    private static SerializableConjureDefinedError forException(CheckedServiceException exception) {
        return SerializableConjureDefinedError.builder()
                .errorCode(exception.getErrorType().code().name())
                .errorName(exception.getErrorType().name())
                .errorInstanceId(exception.getErrorInstanceId())
                .parameters(exception.getArgs().stream()
                        .map(arg -> SerializableConjureErrorParameter.builder()
                                .name(arg.getName())
                                .serializedValue(Objects.toString(arg.getValue()))
                                .isSafeForLogging(arg.isSafeForLogging())
                                .build())
                        .toList())
                .build();
    }
}
