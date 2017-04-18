package com.shaferhund.groovetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.os.Bundle
import android.os.Message
import android.util.Log
import com.shaferhund.groovetooth.consumer.MessagingStreamConsumer
import com.shaferhund.groovetooth.consumer.ThreadedStreamConsumer
import com.shaferhund.groovetooth.enums.ConnectionState
import com.shaferhund.groovetooth.enums.MessageType
import com.shaferhund.groovetooth.handler.BluetoothConnectionHandler

class BluetoothConnection {
    BluetoothAdapter adapter

    String address
    String uuid

    BluetoothConnectionHandler handler = new BluetoothConnectionHandler()
    ThreadedStreamConsumer streamReader
    OutputStream outputStream

    ConnectionState currentState

    Thread listenThread
    Thread connectThread

    BluetoothConnection() {

    }

    BluetoothConnection(BluetoothAdapter adapter) {
        this.adapter = adapter
        this.streamReader = new MessagingStreamConsumer()
    }

    BluetoothDevice getDevice() {
        return address ? adapter.getRemoteDevice(address) : null
    }

    void setReader(ThreadedStreamConsumer reader) {
        this.streamReader = reader
    }

    ThreadedStreamConsumer<InputStream> getReader() {
        return streamReader
    }

    synchronized void setState(ConnectionState newState) {
        Log.d("BluetoothConnection", "State transition: [ ${this.currentState?.name()} -> ${newState.name()} ]")

        handler.obtainMessage(MessageType.STATE_CHANGE.stateId, newState.stateId, currentState?.stateId ?: ConnectionState.NONE.stateId).sendToTarget()

        currentState = newState
    }

    ConnectionState getState() {
        return currentState ?: ConnectionState.NONE
    }

    synchronized void listen() {
        listenThread = Thread.start {
            try {
                if (state in [ConnectionState.CONNECTING, ConnectionState.CONNECTED]) {
                    state = ConnectionState.LISTEN

                    BluetoothSocket socket = adapter.listenUsingRfcommWithServiceRecord('Bluetooth Secure', UUID.fromString(uuid)).accept()

                    Message msg = handler.obtainMessage(MessageType.DEVICE_NAME.stateId)
                    Bundle bundle = new Bundle()
                    bundle.putString(MessageType.DEVICE_NAME.key, device.name)
                    bundle.putString(MessageType.DEVICE_ADDRESS.key, device.address)
                    msg.setData(bundle)
                    handler.sendMessage(msg)

                    outputStream = socket.outputStream
                    Thread.startDaemon { reader.consumerFor(socket.inputStream, handler).run() }

                    state = ConnectionState.CONNECTED
                }
            }
            catch (IOException e) {
                Log.i("BluetoothConnection", e.toString())
                state = ConnectionState.NONE
            }
        }
    }

    synchronized void connect() {
        connectThread = Thread.start {
            try {
                state = ConnectionState.CONNECTING
                adapter.cancelDiscovery()
                BluetoothSocket socket = device.createRfcommSocketToServiceRecord(UUID.fromString(uuid))

                socket.connect()

                Log.d("BluetoothConnection", "Socket connected: ${socket.remoteDevice.address}")
                outputStream = socket.outputStream
                Thread.startDaemon { reader.consumerFor(socket.inputStream, handler).run() }
                state = ConnectionState.CONNECTED

            }
            catch (IOException e) {
                Log.i("BluetoothConnection", e.toString())
                state = ConnectionState.NONE
            }
        }
    }

    void write(BluetoothMessage message) {
        write(message.data)
    }

    void write(byte[] data) {
        try {
            if (outputStream) {
                outputStream.write(data)
                handler.obtainMessage(MessageType.WRITE.stateId, -1, -1, data).sendToTarget()
            }
        }
        catch (IOException e) {
            Log.e(this.class.name, e.toString())
        }
    }
}