/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package imagej.envisaje.imagenio.nio;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * A crude memory manager to recycle unused regions of a memory mapped file
 * that serves as off-heap storage for raster data for undo operations and
 * images that are not currently on-screen.
 *
 * @author Tim Boudreau
 */
public class CacheManager implements CacheMap {
    static boolean DEBUG = false;
    private final Object LOCK = new Object();
    private static final CacheManager INSTANCE = new CacheManager();
    private final Set<Entry> entries = new HashSet<Entry>();
    private File cacheFile;
    private FileChannel fc;
    CacheManager() {
        try {
            cacheFile = File.createTempFile("imagine_" + new Random(System.currentTimeMillis()).nextLong(), "imgdata");
            fc = new RandomAccessFile(cacheFile, "rw").getChannel();
        } catch (IOException ex) {
            throw new Error("Could not create cache file");
        }
    }

    public static ByteBuffer requestBuffer(Object requestor, int size) {
        return INSTANCE._requestBuffer(requestor, size);
    }
    
    public static void shutdown() {
        
    }
    
    public static void dispose(Object owner) {
        if (INSTANCE != null) {
            INSTANCE._dispose(owner);
        }
    }
    
    public static CacheMap getMap() {
        return INSTANCE;
    }
    

    public int getSize() {
        synchronized (LOCK) {
            return entries.size();
        }
    }

    public List<Region> getRegions() {
        List<Region> result;
        synchronized (LOCK) {
            result = new ArrayList<Region>(entries);
        }
        Collections.sort(result);
        return result;
    }

    public long getCacheFileLength() {
        try {
            return fc.size();
        } catch (IOException ex) {
            ex.printStackTrace(); //XXX
            return cacheFile == null ? 0 : cacheFile.length();
        }
    }
    private int maxSize = 0;

    private void _dispose(Object owner) {
        synchronized (LOCK) {
            for (Entry e : entries) {
                if (e.owner != null && e.owner.get() == owner) {
                    e.owner = null;
                }
            }
        }
    }

    private void addRegion(Entry e) {
        synchronized (LOCK) {
            entries.add(e);
            maxSize = Math.max(e.getUsedSize(), maxSize);
        }
    }
    
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Region e : getRegions()) {
            sb.append (e);
        }
        return sb.toString();
    }
    
    private void clearByteBuffer(ByteBuffer buf, int size) {
        //XXX is this necessary?
        buf.clear();
        int factor = size/8;
        int remainder = size%8;
        byte[] b = new byte[factor];
        int count = size/factor;

        for (int i = 0; i < count; i++) {
            buf.put(b);
        }
        if (remainder != 0) {
            buf.put(new byte[remainder]);
        }
        buf.rewind();
    }
    
    private final class Entry implements Region {

        private final long start;
        private int size;
        private int capacity;
        private Reference<MappedByteBuffer> buffer;
        private Reference<Object> owner;
        private Exception exception;

        Entry(long start, int size, MappedByteBuffer buf, Object owner) {
            this.size = this.capacity = size;
            this.start = start;
            this.buffer = buffer == null ? null : new WeakReference<MappedByteBuffer>(buf);
            this.owner = new WeakReference<Object>(owner);
            if (DEBUG) {
                exception = new Exception();
                exception.printStackTrace();
            }
        }

        boolean volunteer(Object newOwner, int requestedSize) {
            if (owner != null && owner.get() != null) {
                return false;
            } else {
                if (capacity >= requestedSize) {
                    if (capacity >= requestedSize * 2) {
                        //Don't waste this entry on such a small region
                        return false;
                    } else {
                        owner = new WeakReference(newOwner);
                        buffer = null;
                        this.size = requestedSize;
                        return true;
                    }
                } else {
                    return false;
                }
            }
        }

        public long getStart() {
            return start;
        }

        public long getEnd() {
            return start + capacity;
        }

        public int getUsedSize() {
            return size;
        }

        public boolean hasBuffer() {
            return buffer != null && buffer.get() != null;
        }

        public MappedByteBuffer getBuffer() throws IOException {
            MappedByteBuffer result = buffer == null ? null : buffer.get();
            if (result == null) {
                result = fc.map(MapMode.READ_WRITE, start, size);
                this.buffer = new WeakReference<MappedByteBuffer>(result);
                result.order(ByteOrder.nativeOrder());
            } else {
                result.rewind();
            }
            result.limit(size);
//            clearByteBuffer (result, size);
            return result;
        }

        public State getState() {
            if (owner != null && owner.get() != null) {
                return State.IN_USE;
            } else if (hasBuffer()) {
                return State.USED_BUT_OWNER_GONE;
            } else {
                return State.UNUSED;
            }
        }

        public int compareTo(Region o) {
            return (int) (getStart() - o.getStart());
        }

        public StackTraceElement[] getAllocationBacktrace() {
            if (exception != null) {
                return exception.getStackTrace();
            } else {
                return new StackTraceElement[0];
            }
        }
        
        public String toString() {
            return "[start:" + start + " capacity:" + capacity + " used: " + size + " state:" + getState() + "]";
        }
    }

    private ByteBuffer _requestBuffer(Object requestor, int size) {
        Entry toUse = null;
        synchronized (LOCK) {
            if (size < maxSize) {
                for (Entry e : entries) {
                    if (e.volunteer(requestor, size)) {
                        toUse = e;
                        break;
                    }
                }
            }
            try {
                if (toUse != null) {
                    return toUse.getBuffer();
                } else {
                    long start = fc.size();
                    toUse = new Entry(start, size, null, requestor);
                    addRegion(toUse);
                    return toUse.getBuffer();
                }
            } catch (IOException ex) {
                throw new IllegalStateException(ex);
            }
        }
    }
}
