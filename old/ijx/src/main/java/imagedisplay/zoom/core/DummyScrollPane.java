package imagedisplay.zoom.core;

import java.awt.*;

import javax.swing.plaf.*;
import javax.swing.*;

/**
 * DummyScrollPane is derived from Java Swing's JScrollPane.
 * The major difference between DummyScrollPane and JScrollPane is that
 * ScrollBars, in DummyScrollPane, are not sensitive to the size of the
 * related view. The property of JScrollBars should/could be explictily set
 * by programmers so that programmers have a total control of its visual properties.
 *
 * If you need to modify this file or use this library in your own project, please
 * let me know. Thanks!
 *
 * @author Qiang Yu (qiangyu@gmail.com)
 */
public class DummyScrollPane extends JScrollPane {

    /**
     * constructor
     */
    public DummyScrollPane(Component view, int vsbPolicy, int hsbPolicy) {

        setLayout(new DummyScrollPaneLayout.UIResource());
        setVerticalScrollBarPolicy(vsbPolicy);
        setHorizontalScrollBarPolicy(hsbPolicy);
        setViewport(createViewport());
        setVerticalScrollBar(createVerticalScrollBar());
        setHorizontalScrollBar(createHorizontalScrollBar());
        if (view != null) {
            setViewportView(view);
        }
        setOpaque(true);
        setLayout(new DummyScrollPaneLayout());
    }

    /**
     * constructor
     */
    public DummyScrollPane(Component view) {
        this(view, VERTICAL_SCROLLBAR_AS_NEEDED,
                HORIZONTAL_SCROLLBAR_AS_NEEDED);
    }

    /**
     * cinstructor
     */
    public DummyScrollPane(int vsbPolicy, int hsbPolicy) {
        this(null, vsbPolicy, hsbPolicy);
    }

    /**
     * constructor
     */
    public DummyScrollPane() {
        this(null, VERTICAL_SCROLLBAR_AS_NEEDED,
                HORIZONTAL_SCROLLBAR_AS_NEEDED);
    }

    int vv = 0, vmin = 0, vmax = 0;
    int hv = 0, hmin = 0, hmax = 0;

    /**
     * get the current value of the vertical scroll bar
     *
     * @return the current value of the vertical scroll bar
     */
    public int getVSBValue() {
        return vv;
    }

    /**
     * get the minium value of the vertical scroll bar
     *
     * @return the minium value of the vertical scroll bar
     */
    public int getVSBMin() {
        return vmin;
    }

    /**
     * get the maxium value of the vertical scroll bar
     *
     * @return the maxium value of the vertical scroll bar
     */
    public int getVSBMax() {
        return vmax;
    }

    /**
     * get the current value of the horizontal scroll var
     *
     * @return the current value of the horizontal scroll bar
     */
    public int getHSBValue() {
        return hv;
    }

    /**
     * get the minium value of the horizontal scroll bar
     *
     * @return the minium value of the horizontal scroll bar
     */
    public int getHSBMin() {
        return hmin;
    }

    /**
     * get the maxium value of the horizontal scroll bar
     *
     * @return the maxium value of the horizontal scroll bar
     */
    public int getHSBMax() {
        return hmax;
    }

    /**
     * set vertical scroll bar values
     *
     * @param v   current value
     * @param min minium value
     * @param max maxium value
     */
    public void setVSBValues(int v, int min, int max) {

        vv = v;
        vmin = min;
        vmax = max;
    }

    /**
     * set horizontal scroll bar values
     *
     * @param v   current value
     * @param min minium value
     * @param max maxium value
     */
    public void setHSBValues(int v, int min, int max) {

        hv = v;
        hmin = min;
        hmax = max;
    }

    @Override
    public void setUI(ScrollPaneUI ui) {
    }

    @Override
    public void updateUI() {
    }

    @Override
    public String getUIClassID() {
        return null;
    }
}
