package com.shaferhund.groovetooth

import com.shaferhund.groovetooth.handler.BluetoothMessageHandler

public class BluetoothMessage {
    UUID id = UUID.randomUUID()
    byte[] data

    BluetoothMessageHandler handler = new BluetoothMessageHandler()
}