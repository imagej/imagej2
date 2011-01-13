
package org.imagejdev.tests;

import org.imagejdev.annotations.ActionRegistration;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
//import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ContextAwareAction;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import static org.junit.Assert.*;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class ActionRegistrationTest { //extends NbTestCase {

//    public ActionProcessorTest(String n) {
//        super(n);
//    }

//    @Override
//    protected boolean runInEQ() {
//        return true;
//    }

    @ActionRegistration(
        category="Tools",
        displayName="AlwaysOn"
    )
    public static final class Always implements ActionListener {
        static int cnt;
        public void actionPerformed(ActionEvent e) {
            cnt += e.getID();
        }
    }

    public void testAlwaysEnabledAction() throws Exception {
        FileObject fo = FileUtil.getConfigFile(
            "Actions/Tools/" + Always.class.getName().replace('.', '-') + ".instance"
        );
        assertNotNull("File found", fo);
        Object obj = fo.getAttribute("instanceCreate");
        assertNotNull("Attribute present", obj);
        assertTrue("It is an action", obj instanceof Action);
        Action a = (Action)obj;

        assertEquals("I am always on!", a.getValue(Action.NAME));
        a.actionPerformed(new ActionEvent(this, 300, ""));

        assertEquals("Action called", 300, Always.cnt);
    }

    @ActionRegistration(
        category="Tools",
        displayName="Key",
        id="my-action",
        key="klic"
    )
    public static final class Callback implements ActionListener {
        static int cnt;
        public void actionPerformed(ActionEvent e) {
            cnt += e.getID();
        }
    }

    public void testCallbackAction() throws Exception {
        FileObject fo = FileUtil.getConfigFile(
            "Actions/Tools/my-action.instance"
        );
        assertNotNull("File found", fo);
        Object obj = fo.getAttribute("instanceCreate");
        assertNotNull("Attribute present", obj);
        assertTrue("It is context aware action", obj instanceof ContextAwareAction);
        ContextAwareAction a = (ContextAwareAction)obj;

        class MyAction extends AbstractAction {
            int cnt;
            public void actionPerformed(ActionEvent e) {
                cnt += e.getID();
            }
        }
        MyAction my = new MyAction();
        ActionMap m = new ActionMap();
        m.put("klic", my);

        InstanceContent ic = new InstanceContent();
        AbstractLookup lkp = new AbstractLookup(ic);
        Action clone = a.createContextAwareInstance(lkp);
        ic.add(m);

        assertEquals("I am context", clone.getValue(Action.NAME));
        clone.actionPerformed(new ActionEvent(this, 300, ""));
        assertEquals("Local Action called", 300, my.cnt);
        assertEquals("Global Action not called", 0, Callback.cnt);

        ic.remove(m);
        clone.actionPerformed(new ActionEvent(this, 200, ""));
        assertEquals("Local Action stays", 300, my.cnt);
        assertEquals("Global Action called", 200, Callback.cnt);
    }

    @ActionRegistration(
        category="Tools",
        displayName="OnInt",
        id="on-int"
    )
    public static final class Context implements ActionListener {
        private final int context;

        public Context(Integer context) {
            this.context = context;
        }

        static int cnt;

        public void actionPerformed(ActionEvent e) {
            cnt += context;
        }

    }

    public void testContextAction() throws Exception {
        FileObject fo = FileUtil.getConfigFile(
            "Actions/Tools/on-int.instance"
        );
        assertNotNull("File found", fo);
        Object obj = fo.getAttribute("instanceCreate");
        assertNotNull("Attribute present", obj);
        assertTrue("It is context aware action", obj instanceof ContextAwareAction);
        ContextAwareAction a = (ContextAwareAction)obj;

        InstanceContent ic = new InstanceContent();
        AbstractLookup lkp = new AbstractLookup(ic);
        Action clone = a.createContextAwareInstance(lkp);
        ic.add(10);

        assertEquals("Number lover!", clone.getValue(Action.NAME));
        clone.actionPerformed(new ActionEvent(this, 300, ""));
        assertEquals("Global Action not called", 10, Context.cnt);

        ic.remove(10);
        clone.actionPerformed(new ActionEvent(this, 200, ""));
        assertEquals("Global Action stays same", 10, Context.cnt);
    }

    @ActionRegistration(
        category="Tools",
        displayName="OnInt",
        id="on-numbers"
    )
    public static final class MultiContext implements ActionListener {
        private final Collection<Number> context;

        public MultiContext(Collection<Number> context) {
            this.context = context;
        }

        static int cnt;

        public void actionPerformed(ActionEvent e) {
            for (Number n : context) {
                cnt += n.intValue();
            }
        }

    }

    public void testMultiContextAction() throws Exception {
        FileObject fo = FileUtil.getConfigFile(
            "Actions/Tools/on-numbers.instance"
        );
        assertNotNull("File found", fo);
        Object obj = fo.getAttribute("instanceCreate");
        assertNotNull("Attribute present", obj);
        assertTrue("It is context aware action", obj instanceof ContextAwareAction);
        ContextAwareAction a = (ContextAwareAction)obj;

        InstanceContent ic = new InstanceContent();
        AbstractLookup lkp = new AbstractLookup(ic);
        Action clone = a.createContextAwareInstance(lkp);
        ic.add(10);
        ic.add(3L);

        assertEquals("Number lover!", clone.getValue(Action.NAME));
        clone.actionPerformed(new ActionEvent(this, 300, ""));
        assertEquals("Global Action not called", 13, MultiContext.cnt);

        ic.remove(10);
        clone.actionPerformed(new ActionEvent(this, 200, ""));
        assertEquals("Adds 3", 16, MultiContext.cnt);

        ic.remove(3L);
        assertFalse("It is disabled", clone.isEnabled());
        clone.actionPerformed(new ActionEvent(this, 200, ""));
        assertEquals("No change", 16, MultiContext.cnt);
    }

}