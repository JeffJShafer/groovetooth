package com.shaferhund.groovetooth.config

import android.bluetooth.BluetoothAdapter
import com.shaferhund.groovetooth.BluetoothConnection
import com.shaferhund.groovetooth.handler.BluetoothConnectionHandler

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

public class BluetoothManagerConfig {
    ConcurrentMap<UUID, BluetoothConnection> connections = new ConcurrentHashMap<>()
    BluetoothAdapter adapter

    BluetoothConnectionHandler rootHandler = new BluetoothConnectionHandler()

    ConcurrentMap<UUID, BluetoothConnection> getConnections() {
        return connections
    }

    BluetoothConnectionHandler getHandler() {
        return rootHandler
    }

    BluetoothManagerConfig(BluetoothAdapter adapter) {
        this.adapter = adapter
        connection.adapter = adapter
    }
}