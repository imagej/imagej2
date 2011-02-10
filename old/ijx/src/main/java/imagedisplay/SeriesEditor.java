/*
 * SeriesEditor.java
 *
 * Created on February 22, 2007, 12:08 PM
 */
package imagedisplay;

import java.util.Arrays;


/**
 * For editing a seres of images
 *
 * @author GBH
 */
public class SeriesEditor {
    int frames;
    boolean[] deleted;  // indicates if a frame has been marked as deleted
    int selectionBegin = -1;
    int selectionEnd = -1;
    
    /** Creates a new instance of SeriesEditor */
    public SeriesEditor(int frames) {
        this.frames = frames;
        deleted = new boolean[frames];
        Arrays.fill(deleted, false);
    }
    
    public void markSelectionBegin(int frame) {
        // clear selection end frame if begin is past it
        if (frame > selectionEnd) {
            selectionEnd = -1;
        }
        selectionBegin = frame;
    }
    
    public void markSelectionEnd(int frame) {
        // clear selection begin frame if end is before it
        if (frame < selectionBegin) {
            selectionBegin = -1;
        }
        selectionBegin = frame;
    }
    
    public void deleteSelection() {
        if ((selectionBegin >= 0) && (selectionEnd >= 0) && (selectionEnd < frames)) {
            Arrays.fill(deleted, selectionBegin, selectionEnd, true);
        }
    }
    
    public void deleteFrame(int frame) {
        if ((frame > 0) && (frame < frames)) {
            deleted[frame] = true;
        }
    }
    
    public void deleteClearAll() {
        Arrays.fill(deleted, false);
    }
    
    public boolean[] getDeletedArray() {
        return deleted;
    }
}
