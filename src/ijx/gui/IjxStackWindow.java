package ijx.gui;

import java.awt.event.ActionListener;
import java.awt.event.AdjustmentListener;
import java.awt.event.MouseWheelListener;

/**
 *
 * @author GBH
 */
public interface IjxStackWindow extends IjxImageWindow, ActionListener, AdjustmentListener, MouseWheelListener, Runnable {

    boolean isHyperStack();

    void setPosition(int channel, int slice, int frame);

    /**
     * Displays the specified slice and updates the stack scrollbar.
     */
    void showSlice(int index);

    /**
     * Updates the stack scrollbar.
     */
    void updateSliceSelector();

    boolean validDimensions();

}
