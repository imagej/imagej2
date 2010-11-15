package org.imagejdev.sandbox.lookup.injectablesingleton;


public class TryMyService {
  public static void main(String[] args) {
    MyService serv = MyService.getDefault();
    serv.doSomething();
  }

}
