package com.shaferhund.groovetooth.enums

import com.shaferhund.groovetooth.state.StateTransition
import groovy.transform.CompileStatic

@CompileStatic
enum ConnectionState {
    NONE(0),
    LISTEN (1),
    CONNECTING(2),
    CONNECTED (3),
    NULL(-1),
    ALL(Integer.MAX_VALUE),
    NOT_FOUND(Integer.MIN_VALUE)

    int stateId

    ConnectionState(int stateId) {
        this.stateId = stateId
    }

    static ConnectionState fromId(int stateId) {
        return values().find { it.stateId == stateId }
    }

    StateTransition<ConnectionState> rightShiftUnsigned(ConnectionState newState) {
        return new StateTransition<ConnectionState>(this, newState)
    }

    boolean notIn(ConnectionState... states) {
        return !(this in states)
    }
}