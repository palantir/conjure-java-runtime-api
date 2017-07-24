/*
 * Copyright 2017 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.remoting.api.tracing;

/** Zipkin-compatible HTTP header names. */
public interface TraceHttpHeaders {
    String TRACE_ID = "X-B3-TraceId";
    String PARENT_SPAN_ID = "X-B3-ParentSpanId";
    String SPAN_ID = "X-B3-SpanId";
    String IS_SAMPLED = "X-B3-Sampled";
}
