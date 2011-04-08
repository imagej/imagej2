package imagej.envisaje.api.splines;

import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class DefaultPathModel <T extends Entry> implements PathModel <T> {
    
    private final List <T> data = new ArrayList <T> ();
    private DefaultPathModel() {
    }
    
    private DefaultPathModel (List <T> l) {
        data.addAll (l);
    }
    
    private transient GeneralPath path;
    public GeneralPath getPath() {
        if (path == null) {
            path = new GeneralPath();
            for (T entry : data) {
                entry.perform (path);
            }
        }
        return path;
    }
    
    public void draw (Graphics2D g, Set <T> selection, Paint selColor) {
        g.draw(getPath());
        Paint p = g.getPaint();
        boolean sel;
        for (T entry : data) {
            sel = selection.contains(entry);
            if (sel) {
                g.setPaint(selColor);
            }
            entry.draw(g);
            if (sel) {
                g.setPaint (p);
            }
        }
    }
    
    public void setPoint (Node node, Point2D point) {
        if (node.x != point.getX() || node.y != point.getY()) {
            System.err.println("SETTING " + node + " from " + node + " to " + point);
            node.setLocation(point);
            fire();
        }
    }
    
    public Hit hit(Point p, int areaSize) {
        Point2D pp = new Point2D.Double (p.x, p.y);
        return hit (pp, areaSize);
    }

    public Hit hit (Point2D p, int areaSize) {
        Hit result = null;
        int sz = areaSize / 2;
        int ix = 0;
        for (T entry : data) {
            if (entry instanceof Close) {
                continue;
            }
            int pIdx = entry.hit(p, areaSize);
            if (pIdx != -1) {
                result = new Hit (this, entry, ix, pIdx);
                break;
            }
            ix++;
        }
        return result;
    }
    
    public Set<Hit> hit(Rectangle r, boolean includeControlPoints) {
        Set <Hit> result = new HashSet <Hit> ();
        int ix = 0;
        for (Entry entry : this) {
            Node[] nds = entry.getPoints();
            if (includeControlPoints) {
                for (int i = 0; i < nds.length; i++) {
                    if (r.contains (nds[i])) {
                        result.add (new Hit(this, entry, ix, i));
                    }
                }
            } else {
                if (r.contains (nds[0])) {
                    result.add (new Hit (this, entry, ix, 0));
                }
            }
            ix++;
        }
        return result;
    }

    public static PathModel <Entry> newInstance() {
        return new DefaultPathModel <Entry>();
    }
    
    public static PathModel <Entry> copy (PathModel <Entry> mdl) {
        return new DefaultPathModel <Entry> (mdl);
    }
    
    public static PathModel <Entry> create(Shape shape) {
        double[] d = new double[6];
        List <Entry> entries = new LinkedList <Entry> ();
        PathIterator iter = shape.getPathIterator(AffineTransform.getTranslateInstance(0, 0));
        while (!iter.isDone()) {
            int op = iter.currentSegment(d);
            switch (op) {
                case PathIterator.SEG_MOVETO :
                    entries.add (new MoveTo(d[0], d[1]));
                    break;
                case PathIterator.SEG_CUBICTO :
                    entries.add (new CurveTo(d[0], d[1], d[2], d[3], d[4], d[5]));
                    break;
                case PathIterator.SEG_LINETO :
                    entries.add (new LineTo (d[0], d[1]));
                    break;
                case PathIterator.SEG_QUADTO :
                    entries.add (new QuadTo (d[0], d[1], d[2], d[3]));
                    break;
                case PathIterator.SEG_CLOSE :
//                    if (!entries.isEmpty() && entries.get(0) instanceof MoveTo) {
//                        MoveTo mt = (MoveTo) entries.get(0);
//                        entries.add (new LineTo (mt.x, mt.y));
//                    }
                    entries.add(new Close());
                    break;
                default :
                    throw new AssertionError ("Not a PathIterator segment type: " + op);
            }
            iter.next();
        }
        return new DefaultPathModel(entries);
    }
    
    public int size() {
        return data.size();
    }

    public boolean isEmpty() {
        return data.isEmpty();
    }

    public boolean contains(Object o) {
        return data.contains(o);
    }

    public Iterator <T> iterator() {
        Iterator <T> wrap = data.iterator();
        if (wrap == null) {
            throw new NullPointerException();
        }
        return new Iter <T> (wrap);
    }

    public Object[] toArray() {
        return data.toArray();
    }

    public <T> T[] toArray(T[] a) {
        return data.toArray(a);
    }

    public boolean add(T e) {
        boolean result;
        if (result = data.add (e)) {
            fire();
        }
        return result;
    }

    public boolean remove(Object o) {
        boolean result;
        if (result = data.remove(o)) {
            fire();
        }
        return result;
    }

    public boolean containsAll(Collection<?> c) {
        return data.containsAll(c);
    }

    public boolean addAll(Collection <? extends T> c) {
        boolean result;
        if (result = data.addAll(c)) {
            fire();
        }
        return result;
    }

    public boolean addAll(int index, Collection <? extends T> c) {
        boolean result;
        if (result = data.addAll (index, c)) {
            fire();
        }
        return result;
    }

    public boolean removeAll(Collection<?> c) {
        boolean result;
        if (result = data.removeAll(c)) {
            fire();
        }
        return result;
    }
    
    public boolean retainAll(Collection <?> c) {
        boolean result;
        if (result = data.retainAll(c)) {
            fire();
        }
        return result;
    }
    
    public void clear() {
        boolean fire = isEmpty();
        data.clear();
        if (fire) fire();
    }

    public T get(int index) {
        return data.get(index);
    }

    public T set(int index, T element) {
        T result = data.set(index, element);
        if (result != element) {
            fire();
        }
        return result;
    }

    public void add(int index, T element) {
        data.add (index, element);
        fire();
    }

    public T remove(int index) {
        T result = data.remove (index);
        fire();
        return result;
    }

    public int indexOf(Object o) {
        return data.indexOf(o);
    }

    public int lastIndexOf(Object o) {
        return data.lastIndexOf(o);
    }

    public ListIterator<T> listIterator() {
        return new LI <T> (data.listIterator());
    }

    public ListIterator<T> listIterator(int index) {
        return new LI <T> (data.listIterator(index));
    }

    public List <T> subList(int fromIndex, int toIndex) {
        return new DefaultPathModel <T> (data.subList(fromIndex, toIndex));
    }
    
    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer (200);
        sb.append ("    GeneralPath gp = new GeneralPath();\n");
        for (T elem : data) {
            sb.append ("    ");
            sb.append (elem);
        }
        return sb.toString();
    }
    
    private transient ArrayList <ChangeListener> changeListenerList;
    public synchronized void addChangeListener(ChangeListener listener) {
        if (changeListenerList == null ) {
            changeListenerList = new java.util.ArrayList <ChangeListener> ();
        }
        changeListenerList.add (listener);
    }

    public synchronized void removeChangeListener(ChangeListener listener) {
        if (changeListenerList != null ) {
            changeListenerList.remove (listener);
        }
    }

    private void fire() {
        path = null;
        java.util.ArrayList list;
        javax.swing.event.ChangeEvent e = new ChangeEvent (this);
        synchronized (this) {
            if (changeListenerList == null) return;
            list = (ArrayList)changeListenerList.clone ();
        }
        for (int i = 0; i < list.size (); i++) {
            ((javax.swing.event.ChangeListener)list.get (i)).stateChanged (e);
        }
    }

    //Impl of java.awt.Shape
    
    public Rectangle getBounds() {
        return getPath().getBounds();
    }

    public Rectangle2D getBounds2D() {
        return getPath().getBounds2D();
    }

    public boolean contains(double x, double y) {
        return getPath().contains (x, y);
    }

    public boolean contains(Point2D p) {
        return getPath().contains (p);
    }

    public boolean intersects(double x, double y, double w, double h) {
        return getPath().intersects (x, y, w, h);
    }

    public boolean intersects(Rectangle2D r) {
        return getPath().intersects (r);
    }

    public boolean contains(double x, double y, double w, double h) {
        return getPath().contains (x, y, w, h);
    }

    public boolean contains(Rectangle2D r) {
        return getPath().contains (r);
    }

    public PathIterator getPathIterator(AffineTransform at) {
        return getPath().getPathIterator(at);
    }

    public PathIterator getPathIterator(AffineTransform at, double flatness) {
        return getPath().getPathIterator(at, flatness);
    }

    private class Iter <T> implements Iterator <T> {
        private Iterator <T> it;
        private Iter (Iterator <T> other) {
            this.it = other;
            if (it == null) {
                throw new NullPointerException();
            }
        }
        
        public boolean hasNext() {
            return it.hasNext();
        }

        public T next() {
            return it.next();
        }

        public void remove() {
            it.remove();
            fire();
        }
    }
    
    private class LI <T> implements ListIterator <T> {
        private final ListIterator <T> it;
        public LI (ListIterator <T> it) {
            this.it = it;
        }
        
        public boolean hasNext() {
            return it.hasNext();
        }

        public T next() {
            return it.next();
        }

        public boolean hasPrevious() {
            return it.hasPrevious();
        }

        public T previous() {
            return it.previous();
        }

        public int nextIndex() {
            return it.nextIndex();
        }

        public int previousIndex() {
            return it.previousIndex();
        }

        public void remove() {
            it.remove();
            fire();
        }

        public void set(T e) {
            it.set(e);
            fire();
        }

        public void add(T e) {
            it.add (e);
            fire();
        }
    }
}
