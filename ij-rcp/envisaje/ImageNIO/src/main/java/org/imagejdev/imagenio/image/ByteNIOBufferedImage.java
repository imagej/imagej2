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
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.color.ColorSpace;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.awt.image.PixelInterleavedSampleModel;
import java.awt.image.Raster;
import java.awt.image.RasterFormatException;
import java.awt.image.WritableRaster;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import org.imagejdev.imagenio.nio.CacheManager;

/**
 *
 *
 * @author Timothy Boudreau
 */
public class ByteNIOBufferedImage extends BufferedImage implements DisposableImage {
    public ByteNIOBufferedImage(int width, int height) {
        super(new CCM(), new ByteNIORaster(0, 0, width, height, 0, 0, null),
              false, new java.util.Hashtable());
//        System.err.println("Created a new NIO image " + width + "x" + height);
    }

    public ByteNIOBufferedImage(BufferedImage other) {
        this(other.getWidth(), other.getHeight());
        int otherType = other.getType();
        CCM ccm = (CCM)getColorModel();

        ccm.originalDataType = other.getType();
        Raster r = other.getRaster();
        NIODataBufferByte b = (NIODataBufferByte)getRaster().getDataBuffer();
        DataBuffer otherBuf = r.getDataBuffer();
        boolean unknownType = true;

        switch (otherType) {
          case BufferedImage.TYPE_INT_RGB:
          case BufferedImage.TYPE_INT_BGR:
          case BufferedImage.TYPE_4BYTE_ABGR:
          case BufferedImage.TYPE_INT_ARGB:
            if (otherBuf instanceof DataBufferByte) {
                DataBufferByte dbb = (DataBufferByte)otherBuf;

                dbb.getBankData();
                b.buf.rewind();
                b.buf.put(dbb.getBankData()[0]);
            }
            else if (otherBuf instanceof DataBufferInt) {
                DataBufferInt dbi = (DataBufferInt)otherBuf;

                b.buf.rewind();
                int numBanks = dbi.getNumBanks();
                IntBuffer ib = b.buf.asIntBuffer();

                for (int i = 0; i < numBanks; i++) {
                    ib.put(dbi.getData(i));
                }
            }
            else {
                unknownType = true;
            }
            break;
          default:
        }
        if (unknownType) {
            int[] rgb = other.getRGB(0, 0, other.getWidth(), other.getHeight(),
                                     null, 0, other.getWidth());

            b.buf.rewind();
            IntBuffer ib = b.buf.asIntBuffer();

            ccm.originalDataType = BufferedImage.TYPE_INT_ARGB;
            ib.put(rgb);
        }
    }

    public ByteNIOBufferedImage(BufferedImage other, Rectangle bds) {
//        this(Math.min (Math.max (0, other.getWidth() - bds.x), bds.width), 
//             Math.min(Math.max( 0, other.getHeight() - bds.y), bds.height));
        
        this (bds.width, bds.height);
        
//        System.err.println("Create a byte NIO buffered image from an image of " +
//                           other.getWidth() + "," + other.getHeight() +
//                           " subimage " + bds);
        Graphics2D g = createGraphics();

//        System.err.println("OFFSCREEN COPY : " + bds + " COPYING WIDTH " + 
//                Math.min (other.getWidth() - bds.x, bds.width) + " HEIGHT " + 
//                Math.min (other.getHeight() - bds.y, bds.height));
        try {
            g.drawRenderedImage(other.getSubimage(bds.x, bds.y, 
                Math.min (other.getWidth() - bds.x, bds.width),
                Math.min(other.getHeight() - bds.y, bds.height)),
                AffineTransform.getTranslateInstance(0, 0));
        } catch (RasterFormatException e) {
            IllegalStateException ise = new IllegalStateException(e.getMessage()
               + " Fetch rectangle " + bds + " in image of " + other.getWidth() 
               + "," + other.getHeight() + " right, bottom: " 
               + (bds.x + bds.width) + "," + (bds.y + bds.height), e);
            throw ise;
        } finally {
            g.dispose();
        }
    }
    
    BufferedImage toStandardBufferedImage() {
        BufferedImage result = new BufferedImage(getWidth(), getHeight(),
                                                 BufferedImage.TYPE_INT_ARGB);
        // XXX optimize this to only do one memory copy
        // WritableRaster raster = result.getRaster();
        // DataBufferInt db = (DataBufferInt) raster.getDataBuffer();
        // NIODataBufferByte ndbb = (NIODataBufferByte) getRaster().getDataBuffer();
        // ndbb.buf.rewind();
        // IntBuffer ib = ndbb.buf.asIntBuffer();
        // int[] px = new int[ndbb.getSize() / 4];
        // ib.get(px);
        // DataBufferInt dbi = new DataBufferInt (px, ndbb.getSize() / 4);
        Graphics2D g = result.createGraphics();

        g.drawRenderedImage(this, AffineTransform.getTranslateInstance(0, 0));
        g.dispose();
        return result;
    }

    public Raster getData() {
        ByteNIORaster r = (ByteNIORaster)getData();
        NIODataBufferByte b = (NIODataBufferByte)r.getDataBuffer();
        int minX = r.getMinX();
        int minY = r.getMinY();
        int w = r.getWidth();
        int h = r.getHeight();
        CopyOnWriteNIODataBufferByte buf = new CopyOnWriteNIODataBufferByte(b);
        return new ByteNIORaster(buf, minX, minY, w, h, 0, 0, null);
    }

    public void dispose() {
        NIODataBufferByte buf = (NIODataBufferByte)getRaster().getDataBuffer();

        CacheManager.dispose(buf);
    }
    // public int getType() {
    // return TYPE_INT_ARGB;
    // }
    private static class CCM extends ComponentColorModel {

        public CCM() {
            super(ColorSpace.getInstance(ColorSpace.CS_sRGB), true, false,
                  ComponentColorModel.TRANSLUCENT, DataBuffer.TYPE_BYTE);
        }
        int originalDataType = 0;

        public int getRGB(Object inData) {
            switch (originalDataType) {
              case BufferedImage.TYPE_INT_RGB:
                return (255 << 24) | (getGreen(inData) << 16) |
                       (getBlue(inData) << 8) | getRed(inData);
              case BufferedImage.TYPE_INT_BGR:
                return (255 << 24) | (getGreen(inData) << 16) |
                       (getRed(inData) << 8) | getBlue(inData);
              case BufferedImage.TYPE_4BYTE_ABGR:
                return (getRed(inData) << 24) | (getBlue(inData) << 16) |
                       (getAlpha(inData) << 8) | (getGreen(inData));
              case BufferedImage.TYPE_INT_ARGB:
                return (getAlpha(inData) << 24) | (getBlue(inData) << 16) |
                       (getGreen(inData) << 8) | (getRed(inData));
              default:
                return super.getRGB(inData);
            }
        }

        private String b2s(Object o) {
            byte[] b = (byte[])o;
            StringBuffer sb = new StringBuffer();

            for (int i = 0; i < b.length; i++) {
                sb.append(b[i]);
                if (i != b.length - 1) {
                    sb.append(',');
                }
            }
            return sb.toString();
        }
    }
    private static class ByteNIORaster extends WritableRaster {

        public ByteNIORaster(int x, int y, int width, int height, int offx,
                             int offy, WritableRaster parent) {
            super(new NIOPixelInterleavedSampleModel(width, height),
                  new NIODataBufferByte(width*height, 4, true),
                  new Rectangle(x, y, width, height), new Point(offx, offy),
                  parent);
        }

        public ByteNIORaster(NIODataBufferByte buf, int x, int y, int width,
                             int height, int offx, int offy,
                             WritableRaster parent) {
            super(new NIOPixelInterleavedSampleModel(width, height), buf,
                  new Rectangle(x, y, width, height), new Point(offx, offy),
                  parent);
        }
    }
    private static final class NIOPixelInterleavedSampleModel extends PixelInterleavedSampleModel {

        public NIOPixelInterleavedSampleModel(int w, int h) {
            super(DataBuffer.TYPE_BYTE, w, h, 4, w*4, new int[] {0, 0, 0, 0});
        }

        public DataBuffer createDataBuffer() {
            return new NIODataBufferByte(getWidth()*getHeight(), 4, true);
        }
        private byte[] scratchBytes = new byte[4];

        public Object getDataElements(int x, int y, int w, int h, Object obj,
                                      DataBuffer data) {
            // byte[] b = obj == null ? scratchBytes : (byte[]) obj;
            // NIODataBufferByte buf = (NIODataBufferByte) data;
            // b[0] = buf.getElem(i);
            // b[1] = buf.getElem(1, )
            return super.getDataElements(x, y, w, h, obj, data);
        }

        public int[] getPixels(int x, int y, int w, int h, int[] iArray,
                               DataBuffer data) {
            System.err.println("getPixels ");
            return super.getPixels(x, y, w, h, iArray, data);
        }

        public int[] getPixel(int x, int y, int[] iArray, DataBuffer data) {
            return super.getPixel(x, y, iArray, data);
        }

        public void setDataElements(int x, int y, Object obj, DataBuffer data) {
            if ((x < 0) || (y < 0) || (x >= width) || (y >= height)) {
                throw new ArrayIndexOutOfBoundsException("Coordinate out of bounds!");
            }
            if (data instanceof NIODataBufferByte) {
                NIODataBufferByte db = (NIODataBufferByte)data;
                byte[] b = (byte[])obj;
                int w = getWidth();
                int pos = (y*w) + x;

                db.set(pos, b);
            }
            else {
                super.setDataElements(x, y, obj, data);
            }
        }

        public Object getDataElements(int x, int y, Object obj, DataBuffer data) {
            if (data instanceof NIODataBufferByte) {
                NIODataBufferByte db = (NIODataBufferByte)data;
                byte[] b = obj == null ? scratchBytes
                                       : (byte[])obj;
                int w = getWidth();
                int pos = (y*w) + x;

                db.get(pos, b);
                return b;
            }
            else {
                return super.getDataElements(x, y, obj, data);
            }
        }
    }
    static class NIODataBufferByte extends DataBuffer {
        ByteBuffer buf = null;

        protected void alloc(int bytes) {
            buf = CacheManager.requestBuffer(this, bytes + 16);
        }

        int allocatedSize() {
            return (size*getNumBanks()) + 16;
        }
        // public NIODataBufferByte(int size) {
        // super(TYPE_BYTE,size);
        // alloc (size);
        // }

        public NIODataBufferByte(int size, int numBanks, boolean alloc) {
            super(TYPE_BYTE, size, numBanks);
            if (alloc)
                alloc(size*numBanks);
        }
        // public NIODataBufferByte(byte dataArray[], int size) {
        // super(TYPE_BYTE,size);
        // buf = ByteBuffer.wrap(dataArray);
        // }
        // public NIODataBufferByte(byte dataArray[], int size, int offset){
        // super(TYPE_BYTE,size,1,offset);
        // buf = ByteBuffer.wrap (dataArray);
        // buf.position (offset);
        // buf = buf.slice();
        // }

        public byte[] getData() {
            if (buf.hasArray()) {
                return buf.array();
            }
            else {
                byte[] b = new byte[size];

                buf.position(0);
                buf.get(b);
                return b;
            }
        }

        public byte[] getData(int bank) {
            if (bank == 0) {
                return getData();
            }
            else {
                byte[] b = new byte[size];

                buf.position(bank*size);
                buf.get(b);
                return b;
            }
        }

        public byte[][] getBankData() {
            byte[][] result = new byte[getNumBanks()][size];

            for (int i = 0; i < getNumBanks(); i++) {
                buf.position(i*size);
                buf.get(result[i]);
            }
            return result;
        }

        public byte get(int i) {
            return get(0, i);
        }

        public byte get(int bank, int i) {
            return buf.get(toBufferPosition(bank, offset, i, size));
        }

        public int getElem(int i) {
            return (int)(get(i) & 255);
        }

        public int getElem(int bank, int i) {
            return buf.get((bank*size) + i) & 255;
        }

        public void setElem(int i, int val) {
            // buf.put (toBufferPosition (0, offset, i, size), (byte) val);
            set(0, i, (byte)val);
        }

        public void set(int bank, int i, byte val) {
            buf.put(toBufferPosition(bank, offset, i, size), val);
        }

        public void setElem(int bank, int i, int val) {
            set(bank, i, (byte)val);
        }

        public void set(int pos, byte[] b) {
            buf.position(toBufferPosition(0, offset, pos, size));
            buf.put(b);
        }

        public void get(int pos, byte[] b) {
            buf.position(toBufferPosition(0, offset, pos, size));
            buf.get(b);
        }
        boolean splitChannels = false;

        private final int toBufferPosition(int bank, int offset, int index,
                                           int size) {
            return !splitChannels ? ((index + offset)*4) + bank
                                  : (size*bank) + index + offset;
        }

        public String toString() {
            return super.toString() + " using buffer " +
                   (buf == null ? "null"
                                : Integer.toString(buf.limit())) + " size " +
                   size;
        }
    }
    private static class CopyOnWriteNIODataBufferByte extends NIODataBufferByte {

        public CopyOnWriteNIODataBufferByte(NIODataBufferByte other) {
            super(other.getSize(), other.getNumBanks(), false);
            buf = other.buf;
        }
        boolean pristine = true;

        public void set(int pos, byte[] b) {
            if (pristine) {
                copy();
            }
            super.set(pos, b);
        }

        public void set(int bank, int i, byte val) {
            if (pristine) {
                copy();
            }
            super.set(bank, i, val);
        }

        private void copy() {
            ByteBuffer orig = buf;

            alloc(size*getNumBanks());
            buf.put(orig);
            pristine = false;
        }
    }

    public static BufferedImage copy(BufferedImage img) {
        ByteNIOBufferedImage result = null;

        if (!(img instanceof ByteNIOBufferedImage)) {
            result = new ByteNIOBufferedImage(img);
        }
        else {
            result = (ByteNIOBufferedImage)img;
        }
        return result;
    }

    public static BufferedImage toStandard(BufferedImage img) {
        BufferedImage result = null;

        if (img instanceof ByteNIOBufferedImage) {
            result = ((ByteNIOBufferedImage)img).toStandardBufferedImage();
        }
        else {
            result = (BufferedImage)img;
        }
        return result;
    }
}
