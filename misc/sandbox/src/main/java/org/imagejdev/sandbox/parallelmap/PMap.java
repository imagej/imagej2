package org.imagejdev.sandbox.parallelmap;

import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorCompletionService;

/*
 * Generic Parallel Map Function
 * enabling you to pass in an arbitrary function as an argument to the map function.
 * "By the way, the above code does not handle failures elegantly. One approach would
 * be to have an type that specifies how a failure in one of the sub-tasks should be
 * handled (i.e. fail all tasks, or continue and return null for that individual task)."
 *
 * From http://ibhana.wordpress.com/2009/08/
 *
 */
public class PMap {

    public static <I, R> void map(final Function<I, R> func, Iterable<I> input, Collection<R> output, Executor executor) 
            throws InterruptedException, ExecutionException {

        CompletionService<R> ecs = new ExecutorCompletionService<R>(executor);

        int count = 0;
        for (final I i : input) {
            Callable<R> callableFunc = new Callable<R>() {

                @Override
                public R call() throws Exception {
                    return func.apply(i);
                }
            };
            ecs.submit(callableFunc);
            count++;
        }

        for (int i = 0; i < count; ++i) {
            output.add(ecs.take().get());
        }
    }

    private PMap() {
    }
}
