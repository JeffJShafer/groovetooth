package com.shaferhund.groovetooth.consumer

import com.shaferhund.groovetooth.enums.MessageType
import android.os.Handler

class MessagingStreamConsumer implements ThreadedStreamConsumer<InputStream> {

    @Override
    Runnable consumerFor(InputStream inputStream, Handler handler) {
        return {
            BufferedInputStream stream = new BufferedInputStream(inputStream)

            try {
                byte[] buffer = new byte[2048]
                int bytes

                while(true) {
                    bytes = stream.read(buffer)

                    handler.obtainMessage(MessageType.READ.stateId, bytes, -1, buffer).sendToTarget()
                }
            } catch (IOException e) {
                Log.i(this.class.name, e.toString())
            }
        } as Runnable
    }
}