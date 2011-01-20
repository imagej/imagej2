/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package imagej.envisaje.imagenio.nio;

import java.util.List;

/**
 *
 * @author tim
 */
public interface CacheMap {
    public enum State {
        IN_USE, UNUSED, USED_BUT_OWNER_GONE,
    }
    
    public int getSize();
    public List<Region> getRegions();
    public long getCacheFileLength();
    
    public interface Region extends Comparable <Region> {
        public long getStart();
        public long getEnd();
        public int getUsedSize();
        public boolean hasBuffer();
        public State getState();
        public StackTraceElement[] getAllocationBacktrace();
    }
}
