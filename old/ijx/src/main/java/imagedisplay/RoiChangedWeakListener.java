/*
 * RoiChangedWeakListener.java
 *
 * Created on March 8, 2007, 1:31 PM
 */

package imagedisplay;


class RoiChangedWeakListener
        extends WeakListenerWrapper<RoiChangeListener, RoiChangeTalker>
        implements RoiChangeListener {
    
    public RoiChangedWeakListener(RoiChangeListener listener, RoiChangeTalker talker) {
        super(listener, talker);
    }
    
    protected void removeListener() {
        getTalker().removeRoiChangeListener(this);
    }
    
    public void roiChanged(RoiChangeEvent e) {
        RoiChangeListener listener = getListener();
          if (listener != null) {
              listener.roiChanged(e);
          }
    }
    
}
