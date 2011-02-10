
package ijx.etc;

/**
 *
 * @author GBH <imagejdev.org>
 */
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
    return result != null ? result : new DefaultImplementation();
  }

  public abstract void doSomething();

  //

  private static class DefaultImplementation extends MyService {

    public DefaultImplementation() { }

    @Override
    public void doSomething() {
      System.out.println("DefaultImplementation doing something...");
    }
  }
}

/*  Alternative Implementation of MyService
 *

import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service=MyService.class)
public class AlternateMyService extends MyService {

  public AlternateMyService() {}
   @Override
  public void doSomething() {
    System.out.println("AlternateMyService doing something...");
  }
}
 *
 */
