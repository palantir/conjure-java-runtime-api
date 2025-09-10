/*
 * (c) Copyright 2025 Palantir Technologies Inc. All rights reserved.
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

/**
 * This interface has one method that provides an instance of a class that extends {@link AbstractSerializableError}.
 * This interface should be implemented by classes that extend {@code RemoteException} and would like to provide access
 * to a custom serializable error.
 *<p>
 * The following is an example of how to implement this interface in a custom exception class:
 *
 * <pre>
 *     {@code
 *     public final class MyCustomRemoteException extends RemoteException
 *         implements SerializableErrorProvider<CustomParameters> {
 *         private final CustomParameters parameters;
 *
 *         public ConflictingCauseSafeArgException(MyCustomSerializableError error, int status) {
 *             super(error.toSerializableError(), status);
 *             this.error = error;
 *         }
 *
 *         public MyCustomSerializableError error() {
 *             return error;
 *         }
 *     }
 *     }
 * </pre>
 *
 * where {@code CustomParameters} is a user-defined class representing the error parameters, and
 * {@code MyCustomSerializableError} is a class extending {@code AbstractSerializableError<CustomParameters>}.
 */
public interface SerializableErrorProvider<T> {
    /**
     * Provides an instance of a class that extends {@link AbstractSerializableError} with parameter type {@code T}.
     */
    AbstractSerializableError<T> error();
}
