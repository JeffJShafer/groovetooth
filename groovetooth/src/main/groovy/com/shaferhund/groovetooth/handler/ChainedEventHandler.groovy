package com.shaferhund.groovetooth.handler

import android.os.Handler
import android.os.Message
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.SimpleType

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

abstract class ChainedEventHandler<T> extends Handler {
    ChainedEventHandler parent
    ChainedEventHandler child

    ConcurrentMap<T, List<Closure>> handlerMap = new ConcurrentHashMap<>()

    abstract T keyAdapter(Message msg)

    @Override
    void handleMessage(Message msg) {

        child?.handleMessage(msg)

        T key = keyAdapter(msg)

        if (key) {
            handlerMap[key].each { Closure handler -> handler.call(msg) }
        }
    }

    void on(T key, @ClosureParams(value=SimpleType, options=['android.os.Message']) Closure value) {
        handlerMap[key] = value
    }

    void putAt(T key, @ClosureParams(value=SimpleType, options=['android.os.Message']) Closure value) {
        if (!handlerMap[key]) {
            handlerMap[key] = [value]
        }
        else {
            handlerMap[key] << value
        }
    }

    List<Closure> getAt(T key) {
        return handlerMap[key] ?: []
    }

    void leftShift(ChainedEventHandler child) {
        this.child = child
        child.parent = this
    }

    void rightShift(ChainedEventHandler parent) {
        this.parent = parent
        parent.child = this
    }
}