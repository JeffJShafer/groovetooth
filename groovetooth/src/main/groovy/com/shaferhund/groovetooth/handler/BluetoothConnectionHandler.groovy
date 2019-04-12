package com.shaferhund.groovetooth.handler

import android.os.Message
import android.util.Log
import com.shaferhund.groovetooth.enums.ConnectionState
import com.shaferhund.groovetooth.enums.MessageType
import com.shaferhund.groovetooth.state.StateTransition
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.SimpleType

class BluetoothConnectionHandler extends ChainedEventHandler<StateTransition<ConnectionState>> {
    static final String TAG = 'BluetoothConnectionHandler'

    @Override
    StateTransition<ConnectionState> keyAdapter(Message msg) {
        return MessageType.STATE_CHANGE == MessageType.fromId(msg.what) ? new StateTransition<ConnectionState>(ConnectionState.fromId(msg.arg1), ConnectionState.fromId(msg.arg2)) : null
    }

    @Override
    void handleMessage(Message msg) {
        super.handleMessage(msg)
        def key = keyAdapter(msg)
        if (key) {
            handlerMap[new StateTransition(key.first, ConnectionState.ALL)].each { Closure handler ->
                Log.d(TAG, "Handling event: ${key.toString()}\r\nCalling ${handler.metaClass.theClass} with message: ${msg}")
                handler.call(msg)
            }
            handlerMap[new StateTransition(ConnectionState.ALL, key.second)].each { Closure handler ->
                Log.d(TAG, "Handling event: ${key.toString()}\r\nCalling ${handler.metaClass.theClass} with message: ${msg}")
                handler.call(msg)
            }
            handlerMap[new StateTransition(ConnectionState.ALL, ConnectionState.ALL)].each { Closure handler ->
                Log.d(TAG, "Handling event: ${key.toString()}\r\nCalling ${handler.metaClass.theClass} with message: ${msg}")
                handler.call(msg)
            }
        }
        else if (MessageType.STATE_CHANGE == MessageType.fromId(msg.what)) {
            handlerMap[new StateTransition(ConnectionState.ALL, ConnectionState.NOT_FOUND)].each { Closure handler ->
                Log.w(TAG, "Handling invalid state change\r\nCalling ${handler.metaClass.theClass} with message: ${msg}")
                handler.call(msg)
            }
        }
    }

    void on(ConnectionState connectionState, @ClosureParams(value=SimpleType, options=['android.os.Message']) Closure value) {
        super.on(new StateTransition(ConnectionState.ALL, connectionState), value)
    }
}