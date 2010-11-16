package ijx.exec;

import java.util.concurrent.*;

/**
 * BackgroundTask
 * <p/>
 * Background task class supporting
 * - cancellation,
 * - completion notification, and
 * - progress notification
 * Adapted from Brian Goetz and Tim Peierls, Java Concurrency Book
 */
public abstract class BackgroundTask<V> implements Runnable, Future<V> {

    private final FutureTask<V> theTask = new TheTask();

    private class TheTask extends FutureTask<V> {
        public TheTask() {
            super(new Callable<V>() {
                public V call() throws Exception {
                    return BackgroundTask.this.runTask();
                }
            });
        }

        protected final void done() {
            GuiExecutor.instance().execute(new Runnable() {
                public void run() {
                    V value = null;
                    Throwable thrown = null;
                    boolean cancelled = false;
                    try {
                        value = get();
                    } catch (ExecutionException e) {
                        thrown = e.getCause();
                    } catch (CancellationException e) {
                        cancelled = true;
                    } catch (InterruptedException consumed) {
                    } finally {
                        onCompletion(value, thrown, cancelled);
                    }
                };
            });
        }
    }

    protected void setProgress(final int current, final int max) {
        GuiExecutor.instance().execute(new Runnable() {
            public void run() {
                onProgress(current, max);
            }
        });
    }

    // Called in the background thread
    protected abstract V runTask() throws Exception;

    // Called in the event thread
    protected void onCompletion(V result, Throwable exception,
            boolean cancelled) {
    }

    protected void onProgress(int current, int max) {
    }

    // Other Future methods just forwarded to computation
    public boolean cancel(boolean mayInterruptIfRunning) {
        return theTask.cancel(mayInterruptIfRunning);
    }

    public V get() throws InterruptedException, ExecutionException {
        return theTask.get();
    }

    public V get(long timeout, TimeUnit unit)
            throws InterruptedException,
            ExecutionException,
            TimeoutException {
        return theTask.get(timeout, unit);
    }

    public boolean isCancelled() {
        return theTask.isCancelled();
    }

    public boolean isDone() {
        return theTask.isDone();
    }

    public void run() {
        theTask.run();
    }
}
