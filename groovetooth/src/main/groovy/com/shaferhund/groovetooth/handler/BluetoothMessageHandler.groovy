package com.shaferhund.groovetooth.handler

import android.os.Message
import com.shaferhund.groovetooth.enums.MessageType


public class BluetoothMessageHandler extends ChainedEventHandler<MessageType> {

    @Override
    MessageType keyAdapter(Message msg) {
        return MessageType.fromId(msg.what) ?: MessageType.NOT_FOUND
    }

    @Override
    void handleMessage(Message msg) {
        super.handleMessage(msg)
        if (keyAdapter(msg)) {
            handlerMap[MessageType.ALL].each { Closure handler -> handler.call(msg) }
        }
        else {
            handlerMap[MessageType.NOT_FOUND].each { Closure handler -> handler.call(msg) }
        }
    }
}
