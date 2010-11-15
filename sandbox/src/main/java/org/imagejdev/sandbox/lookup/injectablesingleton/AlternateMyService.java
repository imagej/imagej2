package org.imagejdev.sandbox.lookup.injectablesingleton;

import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service=MyService.class)
  public class AlternateMyService extends MyService {

    public AlternateMyService() {
    }

    @Override
    public void doSomething() {
      System.out.println("AlternateMyService doing something...");
    }
  }