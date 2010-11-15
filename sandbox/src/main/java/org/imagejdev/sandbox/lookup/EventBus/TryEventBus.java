package org.imagejdev.sandbox.lookup.EventBus;

/**
 * Demonstration of EventBus implemented using Lookup
 * Two receivers are registered, each of which responds to a specific event type.
 * @author GBH
 */
public class TryEventBus {
  
  public static void main(String[] args) {
    ReceiverA rA = new ReceiverA();
    ReceiverB rB = new ReceiverB();
    Sender s = new Sender();
    
  }

}
