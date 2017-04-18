package com.shaferhund.groovetooth.enums

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

    Tuple2<ConnectionState, ConnectionState> rightShiftUnsigned(ConnectionState newState) {
        return new Tuple2(this, newState)
    }
}