package com.shaferhund.groovetooth

import com.shaferhund.groovetooth.handler.BluetoothMessageHandler

class BluetoothMessage {
    String id = UUID.randomUUID().toString()
    byte[] data

    BluetoothMessageHandler handler = new BluetoothMessageHandler()
}