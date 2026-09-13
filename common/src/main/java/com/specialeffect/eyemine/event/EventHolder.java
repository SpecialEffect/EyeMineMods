package com.specialeffect.eyemine.event;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class EventHolder<T> {
    private final List<T> listeners = new CopyOnWriteArrayList<>();

    public void register(T listener) {
        listeners.add(listener);
    }

    public List<T> getListeners() {
        return listeners;
    }
}
