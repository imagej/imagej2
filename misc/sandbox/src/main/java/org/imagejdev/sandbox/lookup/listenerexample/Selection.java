package org.imagejdev.sandbox.lookup.listenerexample;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/*
 * http://blogs.sun.com/geertjan/entry/lookup_example
 * Provider module:
 */
public final class Selection {

    private Selection() {
    }

    private static MyLookup LKP = new MyLookup();

    //Make the Lookup accessible:
    public static Lookup getSelection() {
        return LKP;
    }

    private static final class MyLookup extends ProxyLookup implements Runnable {

        private static ScheduledExecutorService EX = Executors.newSingleThreadScheduledExecutor();

        public MyLookup() {
            EX.schedule(this, 2000, TimeUnit.MILLISECONDS);
        }
        private int i;

        @Override
        public void run() {
            //Add to the Lookup a new MyHello:
            setLookups(Lookups.singleton(new MyHello(i++)));
            EX.schedule(this, 2000, TimeUnit.MILLISECONDS);
        }
    }

    private static final class MyHello implements HelloProvider {

        private String text;

        public MyHello(int i) {
            text = i % 2 == 0 ? "Hello from Tom" : "Hello from Jerry";
        }

        public String sayHello() {
            return text;
        }
    }
}
