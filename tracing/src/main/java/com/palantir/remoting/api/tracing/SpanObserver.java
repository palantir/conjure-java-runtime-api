/*
 * Copyright 2017 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.remoting.api.tracing;

/**
 * Represents the event receiver for span completion events. Implementations are invoked synchronously on the primary
 * execution thread, and as a result must execute quickly.
 */
public interface SpanObserver {
    void consume(Span span);
}
