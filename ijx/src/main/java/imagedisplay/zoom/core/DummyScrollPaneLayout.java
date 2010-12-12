package imagedisplay.zoom.core;

import javax.swing.*;
import javax.swing.border.Border;

import java.awt.*;


/**
 * The Layout used exclusively by DummyScrollPane.
 *
 * If you need to modify this file or use this library in your own project, please
 * let me know. Thanks!
 *
 * @author Qiang Yu (qiangyu@gmail.com)
 */
public class DummyScrollPaneLayout extends ScrollPaneLayout {

    public Dimension preferredLayoutSize(Container parent) {
        DummyScrollPane scrollPane = (DummyScrollPane) parent;
        int vv = scrollPane.getVSBValue();
        int vmin = scrollPane.getVSBMin();
        int vmax = scrollPane.getVSBMax();
        int vp = vmax + 1;
        int hv = scrollPane.getHSBValue();
        int hmin = scrollPane.getHSBMin();
        int hmax = scrollPane.getHSBMax();
        int hp = hmax + 1;

        vsb.setValues(vv, vp, vmin, vmax);
        hsb.setValues(hv, hp, hmin, hmax);
        return super.preferredLayoutSize(parent);
    }

    /**
     * check whether the scroll var should be displayed or not
     *
     * @param value current scroll bar value
     * @param page  scroll bar block/page value
     * @param min   minum value of the scroll bar
     * @param max   maxium value of the scroll bar
     */
    private boolean checkSBVisibility(int value, int page, int min, int max) {
        return !((value <= min) && (value + page >= max));
    }

    /**
     * layout management
     */
    @Override
    public void layoutContainer(Container parent) {

        DummyScrollPane scrollPane = (DummyScrollPane) parent;

        vsbPolicy = scrollPane.getVerticalScrollBarPolicy();
        hsbPolicy = scrollPane.getHorizontalScrollBarPolicy();

        Rectangle availR = scrollPane.getBounds();

        availR.x = availR.y = 0;

        Insets insets = parent.getInsets();

        availR.x = insets.left;
        availR.y = insets.top;
        availR.width -= insets.left + insets.right;
        availR.height -= insets.top + insets.bottom;

        int vv = scrollPane.getVSBValue();
        int vmin = scrollPane.getVSBMin();
        int vmax = scrollPane.getVSBMax();
        int vp = vmax + 1;
        int hv = scrollPane.getHSBValue();
        int hmin = scrollPane.getHSBMin();
        int hmax = scrollPane.getHSBMax();
        int hp = hmax + 1;
        Rectangle colHeadR = new Rectangle(0, availR.y, 0, 0);

        if ((colHead != null) && (colHead.isVisible())) {
            int colHeadHeight = Math.min(availR.height,
                    colHead.getPreferredSize().height);

            colHeadR.height = colHeadHeight;
            availR.y += colHeadHeight;
            availR.height -= colHeadHeight;
        }

        Rectangle rowHeadR = new Rectangle(0, 0, 0, 0);

        if ((rowHead != null) && (rowHead.isVisible())) {
            int rowHeadWidth = Math.min(availR.width,
                    rowHead.getPreferredSize().width);

            rowHeadR.width = rowHeadWidth;
            availR.width -= rowHeadWidth;
            rowHeadR.x = availR.x;
            availR.x += rowHeadWidth;
        }

        Border viewportBorder = scrollPane.getViewportBorder();
        Insets vpbInsets;

        if (viewportBorder != null) {
            vpbInsets = viewportBorder.getBorderInsets(parent);
            availR.x += vpbInsets.left;
            availR.y += vpbInsets.top;
            availR.width -= vpbInsets.left + vpbInsets.right;
            availR.height -= vpbInsets.top + vpbInsets.bottom;
        } else {
            vpbInsets = new Insets(0, 0, 0, 0);
        }

        vp = availR.height;
        hp = availR.width;

        boolean isEmpty = ((availR.width < 0) || (availR.height < 0));
        Rectangle vsbR = new Rectangle(0, availR.y - vpbInsets.top, 0, 0);
        boolean vsbNeeded;

        if (isEmpty) {
            vsbNeeded = false;
        } else if (vsbPolicy == VERTICAL_SCROLLBAR_ALWAYS) {
            vsbNeeded = true;
        } else if (vsbPolicy == VERTICAL_SCROLLBAR_NEVER) {
            vsbNeeded = false;
        } else {
            vsbNeeded = checkSBVisibility(vv, vp, vmin, vmax);
        }

        if ((vsb != null) && vsbNeeded) {
            adjustForVSB(true, availR, vsbR, vpbInsets, true);

            hp -= Math.max(0, Math.min(vsb.getPreferredSize().width,
                    availR.width));
        }

        Rectangle hsbR = new Rectangle(availR.x - vpbInsets.left, 0, 0, 0);
        boolean hsbNeeded;

        if (isEmpty) {
            hsbNeeded = false;
        } else if (hsbPolicy == HORIZONTAL_SCROLLBAR_ALWAYS) {
            hsbNeeded = true;
        } else if (hsbPolicy == HORIZONTAL_SCROLLBAR_NEVER) {
            hsbNeeded = false;
        } else {
            hsbNeeded = checkSBVisibility(hv, hp, hmin, hmax);
        }

        if ((hsb != null) && hsbNeeded) {
            adjustForHSB(true, availR, hsbR, vpbInsets);

            vp -= Math.max(0, Math.min(availR.height,
                    hsb.getPreferredSize().height));

            if ((vsb != null) && !vsbNeeded
                    && (vsbPolicy != VERTICAL_SCROLLBAR_NEVER)) {
                vsbNeeded = checkSBVisibility(vv, vp, vmin, vmax);

                if (vsbNeeded) {
                    adjustForVSB(true, availR, vsbR, vpbInsets, true);

                    hp -= Math.max(0, Math.min(vsb.getPreferredSize().width,
                            availR.width));
                }
            }
        }

        if (viewport != null) {
            viewport.setBounds(availR);

            boolean oldHSBNeeded = hsbNeeded;
            boolean oldVSBNeeded = vsbNeeded;

            if ((vsb != null)
                    && (vsbPolicy == VERTICAL_SCROLLBAR_AS_NEEDED)) {
                boolean newVSBNeeded = checkSBVisibility(vv, vp, vmin, vmax);

                if (newVSBNeeded != vsbNeeded) {
                    vsbNeeded = newVSBNeeded;

                    adjustForVSB(vsbNeeded, availR, vsbR, vpbInsets, true);

                    if (vsbNeeded) {
                        hp -= Math
                                .max(0, Math.min(vsb.getPreferredSize().width,
                                        availR.width));
                    }
                }
            }

            if ((hsb != null)
                    && (hsbPolicy == HORIZONTAL_SCROLLBAR_AS_NEEDED)) {
                boolean newHSBbNeeded = checkSBVisibility(hv, hp, hmin, hmax);

                if (newHSBbNeeded != hsbNeeded) {
                    hsbNeeded = newHSBbNeeded;

                    adjustForHSB(hsbNeeded, availR, hsbR, vpbInsets);

                    if (hsbNeeded) {
                        vp -= Math
                                .max(0, Math.min(availR.height,
                                        hsb.getPreferredSize().height));
                    }

                    if ((vsb != null) && !vsbNeeded
                            && (vsbPolicy != VERTICAL_SCROLLBAR_NEVER)) {
                        vsbNeeded = checkSBVisibility(vv, vp, vmin, vmax);

                        if (vsbNeeded) {
                            adjustForVSB(true, availR, vsbR, vpbInsets, true);

                            hp -= Math
                                    .max(0, Math.min(vsb.getPreferredSize().width,
                                            availR.width));
                        }
                    }
                }

                if ((oldHSBNeeded != hsbNeeded)
                        || (oldVSBNeeded != vsbNeeded)) {
                    viewport.setBounds(availR);
                }
            }
        }

        vsbR.height = availR.height + vpbInsets.top + vpbInsets.bottom;
        hsbR.width = availR.width + vpbInsets.left + vpbInsets.right;
        rowHeadR.height = availR.height + vpbInsets.top + vpbInsets.bottom;
        rowHeadR.y = availR.y - vpbInsets.top;
        colHeadR.width = availR.width + vpbInsets.left + vpbInsets.right;
        colHeadR.x = availR.x - vpbInsets.left;

        if (rowHead != null) {
            rowHead.setBounds(rowHeadR);
        }

        if (colHead != null) {
            colHead.setBounds(colHeadR);
        }

        if (vsb != null) {
            vsb.setValues(vv, vp, vmin, vmax);

            if (vsbNeeded) {
                vsb.setVisible(true);
                vsb.setBounds(vsbR);
            } else {
                vsb.setVisible(false);
            }
        }

        if (hsb != null) {
            hsb.setValues(hv, hp, hmin, hmax);
            if (hsbNeeded) {
                hsb.setVisible(true);
                hsb.setBounds(hsbR);
            } else {
                hsb.setVisible(false);
            }
        }

        if (lowerLeft != null) {
            lowerLeft.setBounds(rowHeadR.x, hsbR.y, rowHeadR.width,
                    hsbR.height);
        }

        if (lowerRight != null) {
            lowerRight.setBounds(vsbR.x, hsbR.y, vsbR.width, hsbR.height);
        }

        if (upperLeft != null) {
            upperLeft.setBounds(rowHeadR.x, colHeadR.y, rowHeadR.width,
                    colHeadR.height);
        }

        if (upperRight != null) {
            upperRight.setBounds(vsbR.x, colHeadR.y, vsbR.width,
                    colHeadR.height);
        }
    }

    /**
     * adjust the vertical scroll bar properties
     */
    private void adjustForVSB(boolean wantsVSB, Rectangle available,
                              Rectangle vsbR, Insets vpbInsets,
                              boolean leftToRight) {

        int oldWidth = vsbR.width;

        if (wantsVSB) {
            int vsbWidth = Math.max(0, Math.min(vsb.getPreferredSize().width,
                    available.width));

            available.width -= vsbWidth;
            vsbR.width = vsbWidth;

            if (leftToRight) {
                vsbR.x = available.x + available.width + vpbInsets.right;
            } else {
                vsbR.x = available.x - vpbInsets.left;
                available.x += vsbWidth;
            }
        } else {
            available.width += oldWidth;
        }
    }

    /**
     * adjust the horizontal scroll bar properties
     */
    private void adjustForHSB(boolean wantsHSB, Rectangle available,
                              Rectangle hsbR, Insets vpbInsets) {

        int oldHeight = hsbR.height;

        if (wantsHSB) {
            int hsbHeight =
                    Math.max(0, Math.min(available.height,
                            hsb.getPreferredSize().height));

            available.height -= hsbHeight;
            hsbR.y = available.y + available.height
                    + vpbInsets.bottom;
            hsbR.height = hsbHeight;
        } else {
            available.height += oldHeight;
        }
    }
}
