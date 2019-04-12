package com.shaferhund.groovetooth.handler

import android.os.Handler
import android.os.Message
import android.util.Log
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.SimpleType

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap


abstract class ChainedEventHandler<T> extends Handler {
    static final String TAG = 'ChainedEventHandler'

    ChainedEventHandler parent
    Map<String, ChainedEventHandler> children = [:]

    ConcurrentMap<T, List<Closure>> handlerMap = new ConcurrentHashMap<>()

    abstract T keyAdapter(Message msg)

    @Override
    void handleMessage(Message msg) {

        parent?.handleMessage(msg)

        T key = keyAdapter(msg)

        if (key) {
            handlerMap[key].each { Closure handler ->
                Log.d(TAG, "Handling event: ${key.toString()}\r\nCalling ${handler.metaClass.theClass} with message: ${msg}")
                handler.call(msg)
            }
        }
    }

    void on(T key, @ClosureParams(value=SimpleType, options=['android.os.Message']) Closure value) {
        this[key] = value
    }

    void putAt(T key, @ClosureParams(value=SimpleType, options=['android.os.Message']) Closure value) {
        if (!handlerMap[key]) {
            handlerMap[key] = [value]
        }
        else if (!this[key].find { it.metaClass.theClass == value.metaClass.theClass }) {
            handlerMap[key] << value
        }
        else {
            Log.d(TAG, "Closure ${value.metaClass.theClass.toString()} already registered for key: ${key.toString()}")
        }
    }

    List<Closure> getAt(T key) {
        return handlerMap[key] ?: []
    }

    void leftShift(ChainedEventHandler child) {
        children[UUID.randomUUID().toString()] = child
        child.parent = this
    }

    void leftShift(String id, ChainedEventHandler child) {
        children[id] = child
        child.parent = this
    }

    void rightShift(ChainedEventHandler parent) {
        this.parent = parent
        parent << this
    }

    boolean subsetOf(ChainedEventHandler<T> other) {
        if (other == null) {
            return false
        }
        return !handlerMap || handlerMap.every { T key, List<Closure> value ->
            value.every { Closure a ->
                other.handlerMap[key]?.any { Closure b ->
                    b.metaClass.theClass == b.metaClass.theClass
                }
            }
        }
    }

    @Override
    boolean equals(Object obj) {
        if (this == obj) {
            return true
        }

        if (!(obj instanceof ChainedEventHandler<T>)) {
            return false
        }

        ChainedEventHandler other = obj as ChainedEventHandler<T>

        if (handlerMap.keySet() != other.handlerMap.keySet()) {
            return false
        }

        boolean handlersEqual = handlerMap.every { T key, List<Closure> list ->
            List<Closure> otherList = other.handlerMap[key]

            return list.size() == otherList.size() && list.findResult(true) { Closure a ->
                a.metaClass.theClass == otherList[list.indexOf(a)]?.metaClass.theClass ? null : false
            }
        }

        return handlersEqual && this.parent == other.parent && this.children == other.children
    }

    @Override
    int hashCode() {
        int code = this.handlerMap.hashCode()
        code ^= this.parent.hashCode()
        code ^= children.hashCode()
        return code
    }
}