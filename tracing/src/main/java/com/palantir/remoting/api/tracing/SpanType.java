/*
 * Copyright 2017 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.remoting.api.tracing;

public enum SpanType {
    /**
     * Indicates that this span encapsulates server-side work of an RPC call. This is typically the outermost span of a
     * set of calls made within one service as a result of an incoming RPC call.
     */
    SERVER_INCOMING,

    /**
     * Indicates that this is the innermost span encapsulating remote work, typically the last span opened by an RPC
     * client.
     */
    CLIENT_OUTGOING,

    /** Indicates a local method call or computation that does not involve RPC. */
    LOCAL
}
