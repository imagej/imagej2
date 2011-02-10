package org.imagejdev.sandbox.lookup.EventBus;

/**
 *
 * @author GBH
 */
public class EventB {

    String msg = "";

    public EventB(String msg) {
        this.msg = "EventA: " + msg;
    }
    public String getMsg() {
        return msg;
    }
}
