package com.shaferhund.groovetooth.config

import android.bluetooth.BluetoothAdapter
import com.shaferhund.groovetooth.BluetoothConnection
import com.shaferhund.groovetooth.handler.BluetoothConnectionHandler

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

class BluetoothManagerConfig {
    ConcurrentMap<String, BluetoothConnection> connections = new ConcurrentHashMap<>()
    BluetoothAdapter adapter

    String uuid

    BluetoothConnectionHandler rootHandler = new BluetoothConnectionHandler()

    ConcurrentMap<String, BluetoothConnection> getConnections() {
        return connections
    }

    BluetoothManagerConfig(BluetoothAdapter adapter) {
        this.adapter = adapter
    }
}