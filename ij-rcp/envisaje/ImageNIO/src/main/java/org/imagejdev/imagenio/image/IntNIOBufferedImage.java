/*
 *
 * Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.imagejdev.imagenio.image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DirectColorModel;
import java.awt.image.SampleModel;
import java.awt.image.SinglePixelPackedSampleModel;
import java.awt.image.WritableRaster;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import org.imagejdev.imagenio.nio.CacheManager;

/**
 *
 * Poor man's memory management II.  Image over direct bytebuffer, off the java heap.
 * Much slower than accelerated images, but we won't have an OutOfMemoryError with
 * these.
 *
 * @author Timothy Boudreau
 */
public class IntNIOBufferedImage extends BufferedImage implements DisposableImage {
    static final int[] MASKS = new int[] {16711680, 65280, 255, -16777216};

    public IntNIOBufferedImage(int width, int height) {
        super(new DirectColorModel(32, MASKS[0], MASKS[1], MASKS[2], MASKS[3]),
              new NIOIntRaster(0, 0, width, height, 0, 0, null), false,
              new java.util.Hashtable());
        setAccelerationPriority(1.0F);
    }

    public void dispose() {
        NIODataBufferInt buf = (NIODataBufferInt)getRaster().getDataBuffer();

        CacheManager.dispose(buf);
    }

    public int getRGB(int x, int y) {
        NIODataBufferInt buf = (NIODataBufferInt)getData().getDataBuffer();
        int pos = toBufferPosition(x, y, getWidth(), 0);

        return buf.get(pos);
    }
    private static class NIOIntRaster extends WritableRaster {
        // XXX this class duplicates the similar one in the byte image.
        // Consolidate unless real optimization is possible here.

        public NIOIntRaster(int x, int y, int width, int height, int offx,
                            int offy, WritableRaster parent) {
            super(new NIOSinglePixelPackedSampleModel(width, height),
                  new NIODataBufferInt(width, height),
                  new Rectangle(x, y, width, height), new Point(offx, offy),
                  parent);
        }
    }

    private static int toBufferPosition(final int x, final int y, final int w,
                                        final int b) {
        return (y*w) + x;
    }
    private static class NIOSinglePixelPackedSampleModel extends SinglePixelPackedSampleModel {
        final int w;
        final int h;

        public NIOSinglePixelPackedSampleModel(int w, int h) {
            super(DataBuffer.TYPE_INT, w, h, MASKS);
            this.w = w;
            this.h = h;
        }

        public int getNumDataElements() {
            return 1;
        }
        private int[] scratch = new int[1];

        public Object getDataElements(int x, int y, Object obj, DataBuffer data) {
            int[] result = obj == null ? scratch
                                       : (int[])obj;
            int pos = toBufferPosition(x, y, w, 0);

            ((NIODataBufferInt)data).get(pos, result);
            return result;
        }

        public void setDataElements(int x, int y, Object obj, DataBuffer data) {
            // assert data instanceof MMDataBuffer;
            // assert obj instanceof int[];
            int pos = toBufferPosition(x, y, w, 0);

            // ((MMDataBuffer) data).set (pos, (int[]) obj);
            ((NIODataBufferInt)data).set(pos, ((int[])obj)[0]);
        }

        public int getSample(int x, int y, int b, DataBuffer data) {
            // assert data instanceof MMDataBuffer;
            int pos = toBufferPosition(x, y, w, b);

            return ((NIODataBufferInt)data).get(pos);
        }

        public void setSample(int x, int y, int b, int s, DataBuffer data) {
            // assert data instanceof MMDataBuffer;
            int pos = toBufferPosition(x, y, w, b);

            ((NIODataBufferInt)data).set(pos, s);
        }

        public SampleModel createCompatibleSampleModel(int w, int h) {
            return (w == this.w && h == this.h) ? this
                                                : new NIOSinglePixelPackedSampleModel(w,
                                                                                      h);
        }

        public SampleModel createSubsetSampleModel(int[] bands) {
            throw new UnsupportedOperationException();
        }

        public DataBuffer createDataBuffer() {
            return new NIODataBufferInt(w, h);
        }

        public int[] getSampleSize() {
            return new int[] {8, 8, 8, 8};
        }

        public int getSampleSize(int band) {
            return 8;
        }
    }
    private static class NIODataBufferInt extends DataBuffer {
        IntBuffer store = null;

        public NIODataBufferInt(int w, int h) {
            super(DataBuffer.TYPE_INT, 4*(w*h));
            alloc();
        }

        public int getElem(int bank, int i) {
            return store.get((i*4) + bank);
        }

        public void setElem(int bank, int i, int val) {
            store.put((i*4) + bank, val);
        }

        public void get(int pos, int[] out) {
            store.position(pos);
            store.get(out);
        }

        public int get(int pos) {
            store.position(pos);
            return store.get();
        }

        public void set(int pos, int[] val) {
            store.position(pos);
            store.put(val);
        }

        public void set(int pos, int val) {
            store.put(pos, val);
        }

        private void alloc() {
            // No, I have no idea what uses the extra magical 16 bytes.
            ByteBuffer buf = CacheManager.requestBuffer(this, getSize() + 16);

            store = buf.asIntBuffer();
        }
    }
}
