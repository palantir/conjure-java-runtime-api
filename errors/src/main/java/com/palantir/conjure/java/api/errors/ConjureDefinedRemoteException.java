package com.palantir.conjure.java.api.errors;

// TODO(pm): copy-paste RemoteException logic. we have instanceof checks scattered across a few repos that we should be
//  intentional about.
public final class ConjureDefinedRemoteException extends RemoteException {
    public ConjureDefinedRemoteException(SerializableError error, int status) {
        super(error, status);
    }
}
