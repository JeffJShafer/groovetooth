package com.shaferhund.groovetooth.config

import android.bluetooth.BluetoothAdapter
import com.shaferhund.groovetooth.BluetoothConnection

public class BluetoothManagerConfig {
    BluetoothConnection connection = new BluetoothConnection()
    BluetoothAdapter adapter

    BluetoothConnection getConnection() {
        return connection
    }

    void setConnection(BluetoothConnection connection) {
        this.connection = connection
    }

    BluetoothManagerConfig(BluetoothAdapter adapter) {
        this.adapter = adapter
        connection.adapter = adapter
    }
}
