package com.shaferhund.groovetooth.enums

enum MessageType {

    READ(2),
    WRITE(3),
    STATE_CHANGE(1),
    DEVICE_NAME(4, 'device_name'),
    DEVICE_ADDRESS(6, 'device_address'),
    TOAST(5),
    MOTION_COMPLETE(7),
    ALL(Integer.MAX_VALUE),
    NOT_FOUND(Integer.MIN_VALUE)


    int stateId
    String key

    private MessageType(int stateId, String key = null) {
        this.stateId = stateId
        this.key = key
    }

    static MessageType fromId(int stateId) {
        return values().find { it.stateId == stateId }
    }

    boolean equals(int stateId) {
        return this.stateId == stateId
    }
}