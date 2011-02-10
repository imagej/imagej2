/*
 * WeakListenerWrapper.java
 *
 * Created on March 8, 2007, 12:56 PM
 */
package imagedisplay;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;

import java.util.EventListener;

import javax.swing.JButton;


/**
 * The <code>WeakListenerWrapper</code> must be extended for a particular
 * listener and talker. Create a specific instance of this listener, add the
 * listener to its target component (talker), and allow the listener to go out
 * of scope as required by your application.
 *
 * <p>
 * <b>Usage</b>
 * </p>
 *
 * <pre><code>
 * class ButtonActionWeakListener extends
 *         WeakListenerWrapper<ActionListener, JButton> implements ActionListener {
 *
 *     public ButtonActionWeakListener(ActionListener listener, JButton button) {
 *         super(listener, button);
 *     }
 *
 *     public void actionPerformed(ActionEvent e) {
 *         ActionListener listener = getListener();
 *         if (listener != null) {
 *             listener.actionPerformed(e);
 *         }
 *     }
 *
 *     protected void removeListener() {
 *         getTalker().removeActionListener(this);
 *     }
 * }
 * </code></pre>
 *
 * Please feel free to use this code snippet (<code>WeakListenerWrapper</code>)
 * in developing your own commercial or non-commercial applications, but please
 * credit the author, Dan Andrews, for any portion of this code that you use,
 * and provide a reference to <a href="www.ansir.ca">Ansir</a>. Thank you.
 *
 * @author Dan Andrews
 */


public abstract class WeakListenerWrapper<L extends EventListener, T> {
    /** Common <code>ReferenceQueue</code> instance for all subclasses. */
    private static ReferenceQueue<EventListener> queue;

    /** A weak reference to the wrapped <code>EventListener</code> object. */
    private ListenerWeakReference<L> listener;

    /** A reference to the object being listened to. */
    private T talker;

    /**
     * Constructor.
     * @param listener
     *            The <code>EventListener<code> to hold weakly.
     * @param talker
     *            The object being listened to.
     */
    public WeakListenerWrapper(L listener, T talker) {
        super();
        setListener(listener);
        this.talker = talker;
    }

    /**
     * Sets the <code>EventListener</code> to hold weakly.
     *
     * @param listener
     *            The <code>EventListener</code>.
     */
    @SuppressWarnings({"unchecked"})
    private void setListener(L listener) {
        ReferenceQueue<EventListener> queue = getQueue();

        for (ListenerWeakReference r = (ListenerWeakReference) queue.poll(); r != null;) {
            WeakListenerWrapper oldWeakListener = r.getOwner();
            if (oldWeakListener != null) {
                oldWeakListener.removeListener();
            }
            r = (ListenerWeakReference) queue.poll();
        }
        this.listener = new ListenerWeakReference<L>(listener, queue, this);
    }

    /**
     * Gets the <code>EventListener</code> which is held weakly.
     *
     * @return The <code>EventListener</code>.
     */
    protected L getListener() {
        if (listener == null) {
            return null;
        }
        return listener.get();
    }

    /**
     * Gets the object being listened to.
     *
     * @return The object being listened to.
     */
    protected T getTalker() {
        return talker;
    }

    /**
     * Removes this reference from the object being listened to.
     */
    protected abstract void removeListener();

    /**
     * Lazy initialization of the queue.
     *
     * @return the <code>ReferenceQueue</code>
     */
    private static ReferenceQueue<EventListener> getQueue() {
        synchronized (WeakListenerWrapper.class) {
            if (queue == null) {
                queue = new ReferenceQueue<EventListener>();
            }
        }
        return queue;
    }

    /**
     * Demonstration purposes only.
     *
     * @param args
     *            not used.
     */
    public static void main(String[] args) {
        
        class ButtonActionWeakListener extends WeakListenerWrapper<ActionListener, JButton>
            implements ActionListener {
            private String name;

            public ButtonActionWeakListener(ActionListener listener, JButton button, String name) {
                super(listener, button);
                this.name = name;
            }

            public void actionPerformed(ActionEvent e) {
                ActionListener listener = getListener();
                if (listener != null) {
                    listener.actionPerformed(e);
                }
            }

            protected void removeListener() {
                getTalker().removeActionListener(this);
                System.out.println("Removed: " + this);
            }

            public String toString() {
                return name;
            }
        }

        JButton button = new JButton();

        ActionListener holding = new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    System.out.println("Clicked Holding");
                }
            };

        button.addActionListener(new ButtonActionWeakListener(holding, button, "Holding"));
        
        
        for (int i = 0; i < 10; i++) {
            ActionListener l = new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        System.out.println("Clicked Not Holding");
                    }
                };
            button.addActionListener(new ButtonActionWeakListener(l, button, "Not Holding: " + i));
            l = null;
            System.gc();
            button.doClick();
        }

        ActionListener oneMore = new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    System.out.println("Clicked One More");
                }
            };

        button.addActionListener(new ButtonActionWeakListener(oneMore, button, "One More"));
        System.gc();
        button.doClick();

        holding = null;
        System.gc();
        button.doClick();
    }

    /**
     * Inner class - to couple owner to the weakly held
     * <code>EventListener</code> object. The WeakListenerWrapper.java
     */
    private static class ListenerWeakReference<U extends EventListener> extends WeakReference<U> {
        private WeakListenerWrapper owner;

        private ListenerWeakReference(U target, ReferenceQueue<?super U> queue,
            WeakListenerWrapper owner) {
            super(target, queue);
            this.owner = owner;
        }

        private WeakListenerWrapper getOwner() {
            return owner;
        }
    }
}
