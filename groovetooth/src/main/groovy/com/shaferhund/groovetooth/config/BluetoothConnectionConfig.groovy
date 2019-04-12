package com.shaferhund.groovetooth.config

import android.bluetooth.BluetoothAdapter
import android.widget.Adapter
import com.shaferhund.groovetooth.BluetoothConnection

class BluetoothConnectionConfig {
    BluetoothConnection connection
    BluetoothAdapter adapter

    BluetoothConnectionConfig(BluetoothAdapter adapter) {
        this.adapter = adapter
        this.connection = new  BluetoothConnection(adapter)
    }

    BluetoothConnectionConfig(BluetoothAdapter adapter, BluetoothConnection connection) {
        this.adapter = adapter
        this.connection = connection
    }
}