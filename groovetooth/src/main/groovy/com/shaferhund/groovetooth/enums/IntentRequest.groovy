package com.shaferhund.groovetooth.enums

enum IntentRequest {

    CONNECT_DEVICE(384),
    ENABLE_BT(385)

    int id

    IntentRequest(int requestId) {
        this.id = requestId
    }

    static IntentRequest from(int requestId) {
        return values().find { it.id == requestId }
    }
}