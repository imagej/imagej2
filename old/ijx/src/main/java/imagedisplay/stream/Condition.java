package imagedisplay.stream;

/**
 *	This class implements a simple "condition variable." The notion
 *	is that a thread waits for some condition to become true.
 *	If the condition is false, then no wait occurs.
 *
 *	Be very careful of nested-monitor-lockout here:
 * <pre>
 *	 class lockout
 *	 {	Condition godot = new Condition(false);
 *
 *	 	synchronized void f()
 *	 	{
 *	 		some_code();
 *	 		godot.wait_for_true();
 *	 	}
 *
 *	 	synchronized void set() // <b>Deadlock if another thread is in f()</b>
 *	 	{	godot.set_true();
 *	 	}
 *	 }
 * </pre>
 *	You enter f(), locking the monitor, then block waiting for the
 *  condition to become true. Note that you have not released the
 *  monitor for the "lockout" object.
 *	[The only way to set <code>godot</code> true
 *  is to call <code>set()</code>, but you'll block on entry
 *  to <code>set()</code> because
 *	the original caller to <code>f()</code> has the monitor
 *	containing "lockout" object.]
 *	<p>Solve the problem by releasing the monitor before waiting:
 * <pre>
 *	 class okay
 *	 {	Condition godot = new Condition(false);
 *
 *	 	void f()
 *	 	{	synchronized( this )
 *	 		{	some_code();
 *	 		}
 *	 		godot.wait_for_true();	// <b>Move the wait outside the monitor</b>
 *	 	}
 *
 *	 	synchronized void set()
 *	 	{	godot.set_true();
 *	 	}
 *	 }
 * </pre>
 * or by not synchronizing the <code>set()</code> method:
 * <pre>
 *	 class okay
 *	 {	Condition godot = new Condition(false);
 *
 *	 	synchronized void f()
 *	 	{	some_code();
 *	 		godot.wait_for_true();
 *	 	}
 *
 *	 	void set() // <b>Remove the synchronized statement</b>
 *	 	{	godot.set_true();
 *	 	}
 *	}
 * </pre>
 * The normal <code>wait()</code>/<code>notify()</code> mechanism
 * doesn't have this problem since
 * <code>wait()</code> releases the monitor,
 * but you can't always use <code>wait()</code>/<code>notify()</code>.
 * <br><br>
 *
 * <table border=1 cellspacing=0 cellpadding=5><tr><td><font size=-1><i>
 * <center>(c) 2000, Allen I. Holub.</center>
 * <p>
 * This code may not be distributed by yourself except in binary form,
 * incorporated into a java .class file. You may use this code freely
 * for personal purposes, but you may not incorporate it into any
 * commercial product without
 * the express written permission of Allen I. Holub.
 * </font></i></td></tr></table>
 */

public class Condition
      implements Semaphore
{
   private boolean is_true;

   /**	Create a new condition variable in a known state.
    */
   public Condition (boolean is_true) {
      this.is_true = is_true;
   }


   /** Set the condition to false. Waiting threads are not affected.
    *	Setting an already false condition variable to false is a
    *  harmless no-op.
    */
   public synchronized void set_false () {
      is_true = false;
   }


   /** Set the condition to true, releasing any waiting threads.
    */
   public synchronized void set_true () {
      is_true = true;
      notifyAll();
   }


   /** For those who actually know what "set" and "reset" mean, I've
    *  provided versions of those as well. (Set and "set to true" mean
    *  the same thing. You "reset" to enter the false condition.)
    */

   public final void set () {
      set_true();
   }


   public final void reset () {
      set_false();
   }


   /** Returns true if the condition variable is in the "true" state.
    *  Can be dangerous to call this method if the condition can change.
    */

   public final boolean is_true () {
      return is_true;
   }


   /** Release all waiting threads without setting the condition true. This
    * method is effectively a "stateless" or "pulsed" condition variable,
    * as is implemented by Java's <code>wait()</code> and <code>notifY()</code>
    * calls. Only those threads that are waiting are released and subsequent
    * threads will block on this call. The main difference between raw Java
    * and the use of this function is that {@link wait_for_true()}, unlike
    * <code>wait()</code> indicates a timeout with an exception toss. In Java
    * there is no way to distinguish whether <code>wait()</code> returned because of
    * an expired time out or because the object was notified.
    */
   public final synchronized void release_all () {
      notifyAll();
   }


   /** Release one waiting thread without setting the condition true
    */
   public final synchronized void release_one () {
      notify();
   }


   /** Wait for the condition to become true.
    *  @param timeout Timeout, in milliseconds. If 0, method returns
    *			immediately.
    *  @return false if the timeout was 0 and the condition was
    *			false, true otherwise.
    */

   public final synchronized boolean wait_for_true (long timeout) throws
         InterruptedException,
         Semaphore.Timed_out {
      if (timeout == 0 || is_true) {
         return is_true;
      }

      if (timeout == Semaphore.FOREVER) {
         return wait_for_true();
      }

      long expiration = System.currentTimeMillis() +
                        timeout;
      while (!is_true) { //#spin_lock
         long time_remaining = expiration -
                               System.currentTimeMillis();
         if (time_remaining <= 0) {
            throw new Semaphore.Timed_out(
                  "Timed out waiting to acquire Condition Variable");
         }

         wait(time_remaining);
      }

      if (!is_true) { // assume we've timed out.
         throw new Semaphore.Timed_out();
      }

      return true;
   }


   /** Wait (potentially forever) for the condition to become true.
    *  This call is a bit more efficient than
    *  <code>wait_for_true(Semaphore.FOREVER);</code>
    */
   public final synchronized boolean wait_for_true () throws
         InterruptedException {
      while (!is_true) {
         wait();
      }
      return true;
   }


   //------------------------------------------------------------------
   // Support for the Semaphore interface:
   //------------------------------------------------------------------

   private final int _id = Lock_manager.new_id();

   /** The <code>id()</code> method returns the unique integer
    *  identifier of the current condition
    *	variable. You can use this ID to sort an array of semaphores
    *  in order to acquire them in the same order, thereby avoiding
    *	a common deadlock scenario.
    */
   public int id () {
      return _id;
   }


   /** Identical to {@link wait_for_true(long)}
    */
   public boolean acquire (long timeout) throws InterruptedException,
         Semaphore.Timed_out {
      return wait_for_true(timeout);
   }


   /** Identical to {@link set_true()}
    */

   public void release () {
      set_true();
   }

   public static void main (String[] args) {

   }
}
