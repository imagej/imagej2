
package org.imagejdev.sandbox.fromImagine;
/*
 * SensorTest.java
 * JUnit based test
 *
 * Created on October 27, 2006, 7:58 PM
 */

// from: package org.netbeans.paint.api.actions;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import junit.framework.TestCase;
import org.imagejdev.sandbox.fromImagine.Sensor.Notifiable;
import org.openide.util.Lookup;
import org.openide.util.Utilities;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author Tim Boudreau
 */
public class SensorTest extends TestCase {

    public SensorTest(String testName) {
        super(testName);
    }

    InstanceContent content;
    AbstractLookup lkp;
    protected void setUp() throws Exception {
        content = new InstanceContent();
        lkp = new AbstractLookup (content);
    }

    /**
     * Test of getSensor method, of class org.netbeans.paint.api.actions.Sensor.
     */
    public void testGetSensor() {
        System.out.println("testGetSensor");
        N <String> something = new N <String> ();
        Lookup lkp = Utilities.actionsGlobalContext();
        Sensor sensor = Sensor.create(String.class, lkp);
        assertNotNull(sensor);
        sensor.doRegister(something);
        assertEquals (1, sensor.toNotify.size());
        sensor.doUnregister(something);
        WeakReference ref = new WeakReference (sensor);
        sensor = null;
        for (int i=0; i < 10; i++) {
            System.gc();
            System.runFinalization();
            if (ref.get() == null) break;
        }
        assertNull (ref.get());
    }

    /**
     * Test of resultChanged method, of class org.netbeans.paint.api.actions.Sensor.
     */
    public void testResultChanged() throws Exception {
        System.out.println("testResultChanged");
        N <String> n = new N <String> ();
        Sensor sensor = Sensor.create(String.class, lkp);
        sensor.doRegister (n);

        N <String> n1 = new N <String> ();
        sensor.doRegister(n1);
        content.set (Collections.singleton(new Object()), null);
        n.clear();
        n1.clear();
        content.set(Collections.singleton(new Object()), null);
        n.assertNotNotified();
        n1.assertNotNotified();
        content.set (Arrays.asList("Hello", "There"), null);
        wait (n);
        n.assertNotified();
        n1.assertCount(2);
        content.set (Arrays.asList("Goodbye"), null);
        wait (n);
        n.assertNotified();
        n1.assertCount (1);
        content.set (Collections.EMPTY_LIST, null);
        wait (n);
        n1.assertNotified();
        n.assertEmpty();
        content.set (Collections.singleton(new Object()), null);
        wait (n);
        n1.assertNotNotified();
        n.assertNotNotified();
        assertEquals (2, sensor.toNotify.size());
        sensor.doUnregister(n);
        assertEquals (1, sensor.toNotify.size());
        content.set(Arrays.asList("This", "Is", "A", "Test"), null);
        wait (n);
        n1.assertNotified();
        n.assertNotNotified();
        sensor.doRegister (n);
        assertEquals (2, sensor.toNotify.size());
        WeakReference wr = new WeakReference (n);
        n = null;
        for (int i=0; i < 10; i++) {
            System.gc();
            System.runFinalization();
            if (wr.get() == null) break;
        }
        assertNull (wr.get());
        content.set (Collections.singleton("Foo"), null);
        wait (n1);
        assertEquals (1, sensor.toNotify.size());
        sensor.doUnregister(n1);
        n1.clear();
        content.set (Collections.singleton("Goo goo g'joob"), null);
        wait (n1);
        n1.assertNotNotified();
    }

    private void wait (Notifiable n) throws Exception {
        synchronized (n) {
            n.wait(1000);
        }
    }

    private static final class N <T extends Object> implements Notifiable <T> {
        Collection <T> coll;

        public void notify(Collection coll, Class clazz) {
            this.coll = coll;
        }

        void clear() {
            coll = null;
        }

        public Collection <T> assertNotified() {
            Collection <T> c = coll;
            coll = null;
            assertTrue (c!= null);
            return c;
        }

        public void assertNotEmpty() {
            Collection <T> c = assertNotified();
            assertFalse (c.isEmpty());
        }

        public void assertEmpty() {
            Collection <T> c = assertNotified();
            assertTrue (c.isEmpty());
        }

        public void assertNotNotified() {
            assertNull (coll);
        }

        public void assertCount (int count) {
            Collection c = assertNotified();
            assertEquals (count, c.size());
        }
    }
}
