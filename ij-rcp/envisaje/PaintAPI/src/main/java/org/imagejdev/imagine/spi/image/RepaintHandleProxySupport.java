/*
 * RepaintHandleProxySupport.java
 *
 * Created on October 28, 2006, 9:18 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.imagejdev.imagine.spi.image;

import java.awt.Cursor;
import java.awt.EventQueue;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Set;

/**
 * Handy class for proxying repaint handles.
 *
 * @author Tim Boudreau
 */
class RepaintHandleProxySupport implements RepaintHandle {
    private Reference <RepaintHandle> [] handles =
            new Reference [5];

    private final String id;
    RepaintHandleProxySupport (String id) {
        this.id = id;
    }

    public void add (RepaintHandle handle) {
        assert EventQueue.isDispatchThread();
        if (handle == null) {
            throw new NullPointerException ("Null handle");
        }
        if (allHandles().contains (handle)) {
//            throw new IllegalArgumentException ("I already have" +
//                    handle);
            return;
        }
        if (handle == this) {
            throw new IllegalArgumentException ("Adding a " + //NOI18N
                    "repaint handle to itself"); //NOI18N
        }
        int last = 0;
        for (last = 0; last < handles.length; last++) {
            RepaintHandle curr = handles[last] == null ? null :
                handles[last].get();
            if (curr == handle) {
                throw new IllegalArgumentException ("Added same " + //NOI18N
                        "repaint handle twice: " + handle); //NOI18N
            }
            if (curr == null) {
                handles[last] = new WeakReference(handle);
                break;
            } else if (curr == handle) {
                return;
            }
        }
        if (last == handles.length) {
            Reference <RepaintHandle>[] nue =
                new Reference [handles.length + 5];
            System.arraycopy (handles, 0, nue, 0, handles.length);
            nue [handles.length] = new WeakReference(handle);
            handles = nue;
        }
        lastNonNull = -1;
    }

    public void remove (RepaintHandle handle) {
        assert EventQueue.isDispatchThread();
        for (int i = 0; i < handles.length; i++) {
            RepaintHandle h = handles[i] == null ? null : handles[i].get();
            if (h == handle) {
                handles[i] = null;
                lastNonNull = -1;
                break;
            }
        }
    }

    int lastNonNull = -1;
    boolean inRepaintArea = false;
    public void repaintArea(final int x, final int y, final int w, final int h) {
        if (inRepaintArea) {
//            throw new IllegalStateException ("Called recursively");
            return;
        }
        inRepaintArea = true;
        Reference <RepaintHandle>[] handles = this.handles;
        if (true) lastNonNull = -1;
        int max = lastNonNull == -1 ? handles.length : lastNonNull;
        boolean painted = false;
        for (int i = 0; i < max; i++) {
            if (handles[i] != null) {
                lastNonNull = i;
                RepaintHandle hl = handles[i].get();
                if (hl != null) {
                    painted = true;
                    hl.repaintArea (x, y, w, h);
                }
            }
        }
        inRepaintArea = false;
    }

    public void setCursor(final Cursor cursor) {
        Reference <RepaintHandle>[] handles = this.handles;
        int max = lastNonNull == -1 ? handles.length : lastNonNull;
        for (int i = 0; i < max; i++) {
            if (handles[i] != null) {
                RepaintHandle hl = handles[i].get();
                lastNonNull = i;
                if (hl != null) {
                    hl.setCursor (cursor);
                }
            }
        }
    }

    private Set allHandles() {
        HashSet result = new HashSet();
        int handleCount = 0;
        for (int i = 0; i < handles.length; i++) {
            if (handles[i] != null) {
                RepaintHandle hl = handles[i].get();
                if (hl != null) {
                    handleCount++;
                    result.add (hl);
                }
            }
        }
        return result;
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append ('[');
        for (int i = 0; i < handles.length; i++) {
            if (handles[i] != null) {
                RepaintHandle hl = handles[i].get();
                if (hl != null) {
                    if (b.length() != 0) {
                        b.append (',');
                    }
                    b.append(hl.toString());
                }
            }
        }
        b.append (']');
        return super.toString() + " for " + id + " with " + b;
    }
}
