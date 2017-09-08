package edu.cmu.cs.lti.deiis.core.error

class StateError extends DeiisError {
    StateError() {
    }

    StateError(String message) {
        super(message)
    }

    StateError(String message, Throwable t) {
        super(message, t)
    }

    StateError(Throwable t) {
        super(t)
    }

    StateError(String message, Throwable t, boolean enableSuppression, boolean writeStackTrace) {
        super(message, t, enableSuppression, writeStackTrace)
    }
}
