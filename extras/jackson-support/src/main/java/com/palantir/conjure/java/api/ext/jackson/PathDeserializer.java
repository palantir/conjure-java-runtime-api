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

package com.palantir.conjure.java.api.ext.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer;
import com.google.errorprone.annotations.CompileTimeConstant;
import com.palantir.logsafe.Arg;
import com.palantir.logsafe.SafeLoggable;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public final class PathDeserializer extends StdScalarDeserializer<Path> {
    private static final long serialVersionUID = 1;

    public PathDeserializer() {
        super(Path.class);
    }

    @Override
    public Path deserialize(JsonParser parser, DeserializationContext ctxt) throws IOException {
        JsonToken token = parser.getCurrentToken();
        if (token != null) {
            if (token.isScalarValue()) {
                return Paths.get(parser.getValueAsString());
            }
            // 16-Oct-2015: should we perhaps allow JSON Arrays (of Strings) as well?
        }
        throw new SafeJsonMappingException(
                "Could not deserialize path", parser, ctxt.wrongTokenException(parser, Path.class, token, null));
    }

    private static final class SafeJsonMappingException extends JsonMappingException implements SafeLoggable {
        private final String logMessage;

        SafeJsonMappingException(@CompileTimeConstant String message, JsonParser parser, JsonMappingException cause) {
            super(parser, message, cause);
            this.logMessage = message;
        }

        @Override
        public String getLogMessage() {
            return logMessage;
        }

        @Override
        public List<Arg<?>> getArgs() {
            return List.of();
        }
    }
}
