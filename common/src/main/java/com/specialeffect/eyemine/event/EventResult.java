package com.specialeffect.eyemine.event;

public enum EventResult {
    PASS,
    INTERRUPT_TRUE,
    INTERRUPT_FALSE;

    public static EventResult pass() { return PASS; }
    public static EventResult interruptTrue() { return INTERRUPT_TRUE; }
    public static EventResult interruptFalse() { return INTERRUPT_FALSE; }
    public boolean isPresent() { return this != PASS; }
}
