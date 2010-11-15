package org.imagejdev.sandbox.lookup.injectablesingleton;

import org.openide.util.Lookup;

/**
 * Injectable Singleton
 * 
 * e.g. MyService.getDefault().doSomething();
 *
 * @author GBH
 */

public abstract class MyService {

  public static MyService getDefault() {
    MyService result = (MyService) Lookup.getDefault().lookup(MyService.class);
    if (result == null) {
      result = new TrivialImplementationOfMyService();
    }
    return result;
  }

  public abstract void doSomething();

  private static class TrivialImplementationOfMyService extends MyService {

    public TrivialImplementationOfMyService() { }

    @Override
    public void doSomething() {
      System.out.println("TrivialImplementationOfMyService doing something...");
    }
  }
}

// or...
// return def != null ? def : new Impl();