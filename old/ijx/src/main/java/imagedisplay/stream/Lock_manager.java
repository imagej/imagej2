package imagedisplay.stream;

import java.util.*;


/** The Lock_manager class helps manage groups of locks. It provides
 *	a way to safely (without deadlock) acquire all of a set of locks.
 * <br>
 * <br>
 * <table border=1 cellspacing=0 cellpadding=0>
 * <tr><td>11/17/02</td><td>Fixed constructor bug reported by Ralph Schaer</td></tr>
 * </table
 * <table border=1 cellspacing=0 cellpadding=5><tr><td><font size=-1><i>
 * <center>(c) 2002, Allen I. Holub.</center>
 * <p>
 * This code may not be distributed by yourself except in binary form,
 * incorporated into a java .class file. You may use this code freely
 * for personal purposes, but you may not incorporate it into any
 * commercial product without
 * the express written permission of Allen I. Holub.
 * </font></i></td></tr></table>
 *
 * @author Allen I. Holub
 */

public class Lock_manager {
  private static Object id_lock = new Object();
  private static int id_pool = 0;

  private Lock_manager() {}; // Make sure it can't be instantiated

  /** Return a unique integer ID. Used by implementers of Semaphore
   *  to get a value to return from their <code>id()</code> method.
   */

  public static int new_id() { // alone lock rather than
    // synchronizing new_id() because
    synchronized (id_lock) { // I don't want to prevent a call
      return id_pool++; } // to acquire_multiple() while
    // another thread is calling
    // new_id().
  }

  /** The comparator used to sort arrays of locks into ID order.
   */

  private static final Comparator compare_strategy =
      new Comparator() {public int compare(Object a, Object b) {return ( (
      Semaphore) a).id() - ( (Semaphore) b).id();
  }

  public boolean equals(Object obj) {return obj == this;
  }
  };

  /**
   *	This function returns once all of the locks in the incoming
   *  array have been successfully acquired. Locks are always
   *  acquired in ascending order of ID to attempt to avoid
   *  deadlock situations. If the acquire operation is interrupted,
   *	or if it times out, all the locks that have been acquired will
   *	be released.
   *
   *	@param <b>locks</b>	All of these locks must be acquired before
   *			acquire_multiple returns. <b>Warning:</b> It's your job
   *			to make sure that The <code>locks</code> array is not modified while
   *			<code>acquire_multiple()</code> is executing.
   *	@param <b>timeout</b> Maximum time to wait to acquire each
   *			lock (milliseconds). The total time for the multiple
   *			acquire operation could be (timeout * locks.length).
   **/

  public static void acquire_multiple(Semaphore[] locks,
      long timeout) throws InterruptedException,
      Semaphore.Timed_out {acquire(locks, timeout);
  }

  /** Just like {@link #acquire_multiple(Semaphore[],long)}, except
   *	that it takes a collection--rather than an array--argument.
   */

  public static void acquire_multiple(Collection semaphores,
      long timeout) throws InterruptedException,
      Semaphore.Timed_out {acquire(semaphores.toArray(), timeout);
  }

  /** Actually do the acquisition here. The main reason this work can't
   *	be done in acquire_multiple is that the <code>toArray()</code> method called
   *  in the Collection version returns an array of <code>Object</code>, and you
   *	can't cast an array of <code>Object</code> into an array of <code>Semaphore</code>.
   */

  private static void acquire(Object[] locks,
      long timeout) throws InterruptedException,
      Semaphore.Timed_out {
    int current_lock = 0;

    try {
      // It's potentially dangerous to work directly on the locks
      // array rather than on a copy. I didn't want to incur the
      // overhead of making a copy, however.

      long expiration = (timeout == Semaphore.FOREVER)
          ? Semaphore.FOREVER
          : System.currentTimeMillis() + timeout;
      ;

      Arrays.sort(locks, compare_strategy); //#sort
      for (; current_lock < locks.length; ++current_lock) {
        long time_remaining = expiration - System.currentTimeMillis();
        if (time_remaining <= 0)
          throw new Semaphore.Timed_out(
              "Timed out waiting to acquire multiple locks");

        ( (Semaphore) locks[current_lock]).acquire(time_remaining);
      }
    } catch (Exception exception) { // Release all locks up to (but not including)
      // locks[current_lock];

      while (--current_lock >= 0)
        ( (Semaphore) locks[current_lock]).release();

      if (exception instanceof InterruptedException)
        throw (InterruptedException) exception;
      else if (exception instanceof Semaphore.Timed_out)
        throw (Semaphore.Timed_out) exception;
      else
        throw new Error("Unexpected exception:" + exception);
    }
  }
}
