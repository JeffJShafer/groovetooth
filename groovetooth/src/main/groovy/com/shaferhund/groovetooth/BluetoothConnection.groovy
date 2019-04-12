package com.shaferhund.groovetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.os.Bundle
import android.os.Message
import android.util.Log
import com.shaferhund.groovetooth.config.BluetoothMessageConfig
import com.shaferhund.groovetooth.consumer.MessagingStreamConsumer
import com.shaferhund.groovetooth.consumer.ThreadedStreamConsumer
import com.shaferhund.groovetooth.enums.ConnectionState
import com.shaferhund.groovetooth.enums.MessageType
import com.shaferhund.groovetooth.handler.BluetoothConnectionHandler
import groovy.transform.Memoized

import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets

class BluetoothConnection {
    static final String TAG = "BluetoothConnection"

    BluetoothAdapter adapter

    String address
    String uuid

    String connectionId = UUID.randomUUID().toString()

    BluetoothConnectionHandler handler = new BluetoothConnectionHandler()
    ThreadedStreamConsumer streamReader
    OutputStream outputStream

    BluetoothSocket connectSocket
    BluetoothServerSocket serverSocket

    ConnectionState currentState

    BluetoothConnection() {

    }

    BluetoothConnection(BluetoothAdapter adapter) {
        this.adapter = adapter
        this.streamReader = new MessagingStreamConsumer()
    }

    @Memoized
    BluetoothDevice getDevice() {
        return address ? adapter.getRemoteDevice(address) : null
    }

    void setReader(ThreadedStreamConsumer reader) {
        this.streamReader = reader
    }

    ThreadedStreamConsumer<InputStream> getReader() {
        return streamReader
    }

    void close() {
        connectSocket?.close()
        serverSocket?.close()
        state = ConnectionState.NONE
    }

    synchronized void setState(ConnectionState newState) {
        Log.d(TAG, "State transition: [ ${state.name()} -> ${newState.name()} ]")

        handler.obtainMessage(MessageType.STATE_CHANGE.stateId, state.stateId, newState.stateId, connectionId).sendToTarget()

        currentState = newState
    }

    ConnectionState getState() {
        return currentState ?: ConnectionState.NONE
    }

    synchronized void listen() {
        Thread.start {
            try {
                if (state.notIn(ConnectionState.CONNECTING, ConnectionState.CONNECTED)) {
                    state = ConnectionState.LISTEN

                    serverSocket = adapter.listenUsingRfcommWithServiceRecord('Bluetooth Secure', UUID.fromString(uuid))
                    connectSocket = serverSocket.accept()
                    serverSocket.close()

                    Message msg = handler.obtainMessage(MessageType.DEVICE_NAME.stateId)
                    Bundle bundle = new Bundle()
                    bundle.putString(MessageType.DEVICE_NAME.key, device.name)
                    bundle.putString(MessageType.DEVICE_ADDRESS.key, device.address)
                    msg.setData(bundle)
                    handler.sendMessage(msg)

                    outputStream = connectSocket.outputStream
                    Thread.startDaemon { reader.consumerFor(connectSocket.inputStream, handler).run() }

                    state = ConnectionState.CONNECTED
                }
            }
            catch (IOException e) {
                Log.i(TAG, e.toString())
                state = ConnectionState.NONE
            }
        }
    }

    synchronized void connect() {
        serverSocket?.close()
        Thread.start {
            try {
                state = ConnectionState.CONNECTING
                adapter.cancelDiscovery()
                connectSocket = device.createRfcommSocketToServiceRecord(UUID.fromString(uuid))

                connectSocket.connect()

                Log.d(TAG, "Socket connected: ${connectSocket.remoteDevice.address}")

                outputStream = connectSocket.outputStream
                Thread.startDaemon { reader.consumerFor(connectSocket.inputStream, handler).run() }
                state = ConnectionState.CONNECTED

            }
            catch (IOException e) {
                Log.i(TAG, e.toString())
                state = ConnectionState.NONE
            }
        }
    }

    BluetoothMessage send(@DelegatesTo(BluetoothMessageConfig.class) final Closure closure) {
        BluetoothMessageConfig messageConfig = new BluetoothMessageConfig()
        closure.setDelegate(messageConfig)
        closure.setResolveStrategy(Closure.DELEGATE_FIRST)
        closure()

        BluetoothMessage message = messageConfig.message

        handler.children[message.id] = message.handler

        write(message)

        return message
    }

    void write(BluetoothMessage message) {
        try {
            if (outputStream) {
                byte[] id = message.id.getBytes(StandardCharsets.UTF_8)

                byte[] out = ByteBuffer.allocate(4 + id.length + message.data.length)
                        .putInt(id.length)
                        .put(id)
                        .put(message.data)
                        .array()

                outputStream.write(out)
                message.handler.obtainMessage(MessageType.WRITE.stateId, -1, -1, out).sendToTarget()
            }
        }
        catch (IOException e) {
            Log.e(this.class.name, e.toString())
        }
    }

    @Override
    boolean equals(Object obj) {
        if (!(obj instanceof BluetoothConnection)) {
            return false
        }
        BluetoothConnection other = obj as BluetoothConnection

        if (this.address != other.address) {
            return false
        }
        if (this.connectionId != other.connectionId) {
            return false
        }
        if (this.adapter != other.adapter) {
            return false
        }
        if (this.reader != other.reader) {
            return false
        }
        if (this.handler != other.handler) {
            return false
        }

        return true
    }

    @Override
    int hashCode() {
        int code = this.address.hashCode()
        code ^= this.connectionId.hashCode()
        code ^= this.adapter.hashCode()
        code ^= this.reader.hashCode()
        code ^= this.handler.hashCode()
        reader code
    }
}