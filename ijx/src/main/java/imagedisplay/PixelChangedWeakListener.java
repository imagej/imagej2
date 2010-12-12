/*
 * PixelChangedWeakListener.java
 *
 * Created on March 8, 2007, 1:31 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package imagedisplay;

class PixelChangedWeakListener
        extends WeakListenerWrapper<PixelChangeListener, PixelChangeTalker>
        implements PixelChangeListener {
    
    public PixelChangedWeakListener(PixelChangeListener listener, PixelChangeTalker talker) {
        super(listener, talker);
    }
        protected void removeListener() {
        getTalker().removePixelChangeListener(this);
    }
    
    public void pixelChanged(PixelChangeEvent e) {
        PixelChangeListener listener = getListener();
          if (listener != null) {
              listener.pixelChanged(e);
          }
    }
    
}
