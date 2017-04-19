package com.shaferhund.groovetooth.config

import android.bluetooth.BluetoothAdapter
import com.shaferhund.groovetooth.BluetoothConnection

class BluetoothConnectionConfig {
    BluetoothConnection connection
    BluetoothAdapter adapter

    BluetoothConnectionConfig(BluetoothAdapter adapter) {
        this.adapter = adapter
    }
}