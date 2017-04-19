package com.shaferhund.groovetooth

import android.bluetooth.BluetoothAdapter
import com.shaferhund.groovetooth.config.BluetoothConnectionConfig
import com.shaferhund.groovetooth.config.BluetoothManagerConfig
import com.shaferhund.groovetooth.config.BluetoothMessageConfig

public class BluetoothConnectionManager {

    static final Closure<BluetoothConnectionManager> factory = BluetoothConnectionManager.metaClass.&invokeConstructor as Closure<BluetoothConnectionManager>

    BluetoothManagerConfig config

    BluetoothConnectionManager(BluetoothManagerConfig config) {
        this.config = config
        if (!adapter) {
            adapter = BluetoothAdapter.getDefaultAdapter()
        }
    }

    static BluetoothConnectionManager configure(@DelegatesTo(BluetoothManagerConfig.class) final Closure closure) {
        BluetoothAdapter mAdapter = BluetoothAdapter.getDefaultAdapter()
        BluetoothManagerConfig bluetoothManagerConfig = new BluetoothManagerConfig(mAdapter)
        closure.setDelegate(bluetoothManagerConfig)
        closure.setResolveStrategy(Closure.DELEGATE_FIRST)

        closure()
        return factory(bluetoothManagerConfig)
    }

    BluetoothAdapter getAdapter() {
        return config.adapter
    }

    void setAdapter(BluetoothAdapter adapter) {
        config.adapter = adapter
    }

    boolean getBluetoothEnabled() {
        return adapter?.enabled
    }

    Map<String, BluetoothConnection> getConnections() {
        return config.connections
    }

    void listen(@DelegatesTo(BluetoothManagerConfig.class) final Closure closure) {
        closure.setDelegate(config)
        closure.setResolveStrategy(Closure.DELEGATE_FIRST)
        closure()

        config.connection.listen()
    }

    BluetoothConnection connect(@DelegatesTo(BluetoothConnectionConfig.class) final Closure closure) {
        BluetoothConnectionConfig connectionConfig = new BluetoothConnectionConfig(adapter)
        closure.setDelegate(connectionConfig)
        closure.setResolveStrategy(Closure.DELEGATE_FIRST)
        closure.call()

        String connectionId = connectionConfig.connection.connectionId ?: UUID.randomUUID().toString()
        connectionConfig.connectionId = connectionId

        config.connections[connectionId] = connectionConfig.connection
        config.rootHandler << connectionConfig.connection.handler

        connectionConfig.connection.connect()

        return connectionConfig.connection
    }

/*    void send(@DelegatesTo(BluetoothMessageConfig.class) final Closure closure) {
        BluetoothMessageConfig messageConfig = new BluetoothMessageConfig()
        closure.setDelegate(messageConfig)
        closure.setResolveStrategy(Closure.DELEGATE_FIRST)
        closure()

        config.connection.handler << messageConfig.message.handler

        config.connection.write(messageConfig.message)
    }*/
}

