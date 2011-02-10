/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ijx.exec;

// from Goetz presentation

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

// Future and ConcurrentHashMap can be used together to implement an efficient results cache
public class Cache<K, V> {
    Map<K, FutureTask<V>> map = new ConcurrentHashMap();

    public V get(final K key) throws ExecutionException, InterruptedException {
        FutureTask<V> f = map.get(key);
        if (f == null) {
            Callable<V> c = new Callable<V>() {
                public V call() {
                    // return value associated with key
                    return null;
                }
            };
            f = new FutureTask<V>(c);
            FutureTask<V> old = map.put(key, f);
            if (old == null) {
                f.run();
            } else {
                f = old;
            }
        }
        return f.get();
    }
}
