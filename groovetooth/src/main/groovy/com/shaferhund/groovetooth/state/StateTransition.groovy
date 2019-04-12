package com.shaferhund.groovetooth.state

import groovy.transform.CompileStatic

class StateTransition<T extends Enum<T>> extends Tuple2<T, T> {

    //Map<Integer, StateTransition> transitions

    StateTransition() {
        super(null, null)
    }

    StateTransition(T first, T second) {
        super(first, second)
        //transitions = getTransitions(T.class)
    }
/*
    <T extends Enum<T>> void fromId(Class<T> c, int id) {
        transitions = getTransitions(c)
        this.first = transitions[id].first
        this.second = transitions[id].second
    }

    static <E extends EnumSet<E>> List<E> getEnumValues(Class<E> c) {
        def a = c.enumConstants
        List l = Arrays.asList(c.enumConstants).asImmutable()
        return l
    }

    static <E extends Enum<E>> Map<Integer, StateTransition<E>> getTransitions(Class<E> c) {
        def cl = c
        final EnumSet<E> values = EnumSet.allOf(cl)
        return values.collect {
            E first -> (values-first).collect {
                E second -> new StateTransition<E>(first, second)
            }
        }.flatten().collectEntries {[ (new Integer(i++)) : it ]}
    }*/
/*
    static {
        int i = 0
        //Binary cartesian product of enum values representing all possible state transitions
        transitions = getEumValues(c.collect {
            T first -> (enumValues-first).collect {
                T second -> new StateTransition<T>(first, second)
            }
        }.flatten().collectEntries {[ (new Integer(i++)) : it ]}
    }*/

/*    int getId() {
        return transitions.find { k, v -> this == v }.key
    }

    static <E extends Enum<E>> StateTransition<E> from(Class<E> c, int id) {
        StateTransition transition = new StateTransition<E>()
        transition.fromId(c, id)
        return transition
    }

    StateTransition<T> fromId(Integer id) {
        this.first = transitions[id].first
        this.second = transitions[id].second
        return this
    }*/

    @Override
    String toString() {
        return "StateTransition: [${this.first} >>> ${this.second}]"
    }

    @Override
    boolean equals(Object obj) {
        if (!(obj instanceof StateTransition<T>)) {
            return false
        }

        StateTransition other = obj as StateTransition<T>

        return this.first == other.first && this.second == other.second
    }

    @Override
    int hashCode() {
        int code = this.first.hashCode()
        code ^= this.second.hashCode()
        return code
    }
}