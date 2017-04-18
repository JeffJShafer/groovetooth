package com.shaferhund.groovetooth.consumer

import android.os.Handler

interface ThreadedStreamConsumer<T extends InputStream> {

    Runnable consumerFor(T inputStream, Handler handler)
}