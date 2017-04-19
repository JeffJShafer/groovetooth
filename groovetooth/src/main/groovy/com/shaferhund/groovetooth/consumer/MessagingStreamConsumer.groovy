package com.shaferhund.groovetooth.consumer

import com.shaferhund.groovetooth.enums.MessageType
import android.os.Handler
import com.shaferhund.groovetooth.handler.ChainedEventHandler

import java.nio.ByteBuffer

class MessagingStreamConsumer implements ThreadedStreamConsumer<InputStream> {

    @Override
    Runnable consumerFor(InputStream inputStream, Handler handler) {
        ChainedEventHandler chainedHandler = handler as ChainedEventHandler
        return {
            BufferedInputStream stream = new BufferedInputStream(inputStream)

            try {
                byte[] buffer = new byte[2048]
                int bytes

                while(true) {
                    bytes = stream.read(buffer)

                    int tagLength = ByteBuffer.wrap(buffer[0..4]).getInt() + 4
                    String messageTag = new String(buffer[4..tagLength], 'UTF-8')

                    ChainedEventHandler child = chainedHandler.children[messageTag]

                    if (child) {
                        child.obtainMessage(MessageType.READ.stateId, bytes, messageTag, buffer[tagLength..bytes]).sendToTarget()
                    }
                    else  {
                        chainedHandler.obtainMessage(MessageType.READ.stateId, bytes, -1, buffer).sendToTarget()
                    }
                }
            } catch (IOException e) {
                Log.i(this.class.name, e.toString())
            }
        } as Runnable
    }
}