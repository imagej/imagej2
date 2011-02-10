package org.imagejdev.sandbox.lookup.EventBus;

public class Sender {

  public Sender() {
    EventA eventA = new EventA("1");
    EventBus.getDefault().publish(eventA);

    EventB eventB = new EventB("1");
    EventBus.getDefault().publish(eventB);

    eventA = new EventA("2");
    EventBus.getDefault().publish(eventA);

    eventB = new EventB("2");
    EventBus.getDefault().publish(eventB);
  }
}
