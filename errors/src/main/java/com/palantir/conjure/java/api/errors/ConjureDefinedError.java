package com.palantir.conjure.java.api.errors;

import com.palantir.logsafe.Arg;
import com.palantir.logsafe.SafeLoggable;

// TODO(pm): according to java docs on Error an "Error - indicates serious problems that a reasonable application should
//  not try to catch." Perhaps this isn't a good name, but we've already called something SerializableError and the
//  "error" matches the conjure concept so this seems fine but check if folks care.
// TODO(pm): copy-paste the ServiceException logic. I don't like extending ServiceException because we have
//  (instanceof ServiceException) checks scattered across a few repos. Enumerate them, and see if they need to be
//  handled differently compared to ServiceException.
public final class ConjureDefinedError extends ServiceException implements SafeLoggable {
    public ConjureDefinedError(ErrorType errorType, Arg<?>... parameters) {
        super(errorType, parameters);
    }
}
