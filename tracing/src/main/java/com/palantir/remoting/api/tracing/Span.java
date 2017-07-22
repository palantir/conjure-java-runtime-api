/*
 * Copyright 2017 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.remoting.api.tracing;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.Map;
import java.util.Optional;
import org.immutables.value.Value;

/**
 * A value class representing a completed Span, see {@link OpenSpan} for a description of the fields.
 */
@JsonDeserialize(as = ImmutableSpan.class)
@JsonSerialize(as = ImmutableSpan.class)
@Value.Immutable
@Value.Style(visibility = Value.Style.ImplementationVisibility.PACKAGE)
public abstract class Span {

    public abstract String getTraceId();
    public abstract Optional<String> getParentSpanId();
    public abstract String getSpanId();
    public abstract SpanType type();
    public abstract String getOperation();
    public abstract long getStartTimeMicroSeconds();
    public abstract long getDurationNanoSeconds();
    /**
     * Returns a map of custom key-value metadata with which spans will be annotated. For example, a "userId" key could
     * be added to associate spans with the requesting user.
     */
    public abstract Map<String, String> getMetadata();

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends ImmutableSpan.Builder {}

}
