package com.shaferhund.groovetooth

import android.bluetooth.BluetoothAdapter
import com.shaferhund.groovetooth.config.BluetoothConnectionConfig
import com.shaferhund.groovetooth.config.BluetoothManagerConfig
import com.shaferhund.groovetooth.enums.ConnectionState
import com.shaferhund.groovetooth.handler.BluetoothConnectionHandler

class BluetoothConnectionManager {

    static final Closure<BluetoothConnectionManager> factory = BluetoothConnectionManager.metaClass.&invokeConstructor as Closure<BluetoothConnectionManager>

    BluetoothManagerConfig config



    BluetoothConnectionManager(BluetoothManagerConfig config) {
        this.config = config
        if (!adapter) {
            adapter = BluetoothAdapter.getDefaultAdapter()
        }
    }

    static BluetoothConnectionManager configure(@DelegatesTo(BluetoothManagerConfig.class) final Closure closure) {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter()
        BluetoothManagerConfig bluetoothManagerConfig = new BluetoothManagerConfig(adapter)
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

    BluetoothConnection connectionByHashCode(int hashcode) {
        return connections.find { k, v -> k.hashCode() == hashCode()}.value
    }

    BluetoothConnectionHandler getRootHandler() {
        return config.rootHandler
    }

    BluetoothConnection listen(@DelegatesTo(BluetoothConnectionConfig.class) final Closure closure) {
        BluetoothConnectionConfig connectionConfig = new BluetoothConnectionConfig(adapter)
        closure.setDelegate(connectionConfig)
        closure.setResolveStrategy(Closure.DELEGATE_FIRST)
        closure()

        BluetoothConnection existingConnection = connections[connectionConfig.connection.connectionId]

        //If not specifying additional handlers, calling connect on a completed or pending connection
        //with an identical configuration will return that connection
        if (existingConnection?.state in [ConnectionState.CONNECTED, ConnectionState.CONNECTING]) {
            if (connectionConfig.connection.handler == null || connectionConfig.connection.handler.handlerMap.isEmpty()) {
                connectionConfig.connection.handler = existingConnection.handler
            }

            if (connectionConfig.connection != existingConnection) {
                throw new IllegalArgumentException("Attempting to reconfigure connection with id ${connection.connectionId} while already connected or connecting")
            }
            else {
                return existingConnection
            }
        }
        else if (existingConnection?.state == ConnectionState.LISTEN) {
            existingConnection.stopThreads()
        }
        //Reconfigure preexisting connection (if not connected/connecting), add new handlers to existing set
        //and attempt to connect
        else if (existingConnection) {
            connectionConfig = new BluetoothConnectionConfig(adapter, existingConnection)
            closure.setDelegate(connectionConfig)
            closure.call()
        }

        BluetoothConnection connection = connectionConfig.connection

        connections[connection.connectionId] = connection
        rootHandler.children[connection.connectionId] = connection.handler
        connection.handler.parent = rootHandler

        connection.uuid ?: config.uuid

        connection.listen()

        return connection
    }

    BluetoothConnection connect(@DelegatesTo(BluetoothConnectionConfig.class) final Closure closure) {
        BluetoothConnectionConfig connectionConfig = new BluetoothConnectionConfig(adapter)
        closure.setDelegate(connectionConfig)
        closure.setResolveStrategy(Closure.DELEGATE_FIRST)
        closure.call()

        BluetoothConnection existingConnection = connections[connectionConfig.connection.connectionId]

        //If not specifying additional handlers, calling connect on a completed or pending connection
        //with an identical configuration will return that connection
        if (existingConnection?.state in [ConnectionState.CONNECTED, ConnectionState.CONNECTING]) {
            if (connectionConfig.connection.handler.subsetOf(existingConnection.handler)) {
                connectionConfig.connection.handler = existingConnection.handler
            }

            if (connectionConfig.connection != existingConnection) {
                throw new IllegalArgumentException("Attempting to reconfigure connection with id ${existingConnection.connectionId} while already connected or connecting")
            }
            else {
                return existingConnection
            }
        }

        else if (existingConnection?.state == ConnectionState.LISTEN) {
            existingConnection.stopThreads()

            connectionConfig = new BluetoothConnectionConfig(adapter, existingConnection)
            closure.setDelegate(connectionConfig)
            closure.call()
        }
        //Reconfigure preexisting connection (if not connected/connecting), add new handlers to existing set
        //and attempt to connect
        else if (existingConnection) {
            connectionConfig = new BluetoothConnectionConfig(adapter, existingConnection)
            closure.setDelegate(connectionConfig)
            closure.call()
        }

        BluetoothConnection connection = connectionConfig.connection

        connections[connection.connectionId] = connection
        rootHandler.children[connection.connectionId] = connection.handler
        connection.handler.parent = rootHandler


        connection.uuid = connection.uuid ?: config.uuid

        connection.connect()

        return connection
    }
}

