package org.imagejdev.sandbox.lookup.EventBus;

/**
 *
 * @author GBH
 */
public class EventA {

    String msg = "";

    public EventA(String msg) {
        this.msg = "EventA: " + msg;
    }
    public String getMsg() {
        return msg;
    }
}
