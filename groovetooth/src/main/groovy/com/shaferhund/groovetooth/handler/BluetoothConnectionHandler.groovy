package com.shaferhund.groovetooth.handler

import android.os.Message
import com.shaferhund.groovetooth.enums.ConnectionState
import com.shaferhund.groovetooth.enums.MessageType

public class BluetoothConnectionHandler extends ChainedEventHandler<Tuple2<ConnectionState, ConnectionState>> {

    @Override
    Tuple2<ConnectionState, ConnectionState> keyAdapter(Message msg) {
        return MessageType.STATE_CHANGE == MessageType.fromId(msg.what) ? new Tuple2(ConnectionState.fromId(msg.arg2), ConnectionState.fromId(msg.arg1)) : null
    }

    @Override
    void handleMessage(Message msg) {
        super.handleMessage(msg)
        def key = keyAdapter(msg)
        if (key) {
            handlerMap[new Tuple2(key.first, ConnectionState.ALL)].each { Closure handler -> handler.call(msg) }
            handlerMap[new Tuple2(ConnectionState.ALL, key.second)].each { Closure handler -> handler.call(msg) }
            handlerMap[new Tuple2(ConnectionState.ALL, ConnectionState.ALL)].each { Closure handler -> handler.call(msg) }
        }
        else if (MessageType.STATE_CHANGE == MessageType.fromId(msg.what)) {
            handlerMap[ConnectionState.NOT_FOUND].each { Closure handler -> handler.call(msg) }
        }
    }

    void on(ConnectionState connectionState) {
        super.on(new Tuple2(ConnectionState.ALL, connectionState))
    }
}