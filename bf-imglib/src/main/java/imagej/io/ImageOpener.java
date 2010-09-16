//
// ImageOpener.java
//

/*
 Bio-Formats support for ImgLib.
 Copyright (C) 2010, UW-Madison LOCI

 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package imagej.io;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import loci.common.DataTools;
import loci.formats.ChannelFiller;
import loci.formats.ChannelSeparator;
import loci.formats.FormatException;
import loci.formats.FormatTools;
import loci.formats.IFormatReader;
import mpicbg.imglib.container.ContainerFactory;
import mpicbg.imglib.container.array.ArrayContainerFactory;
import mpicbg.imglib.cursor.LocalizableByDimCursor;
import mpicbg.imglib.image.Image;
import mpicbg.imglib.image.ImageFactory;
import mpicbg.imglib.type.numeric.RealType;
import mpicbg.imglib.type.numeric.integer.ByteType;
import mpicbg.imglib.type.numeric.integer.IntType;
import mpicbg.imglib.type.numeric.integer.ShortType;
import mpicbg.imglib.type.numeric.integer.UnsignedByteType;
import mpicbg.imglib.type.numeric.integer.UnsignedIntType;
import mpicbg.imglib.type.numeric.integer.UnsignedShortType;
import mpicbg.imglib.type.numeric.real.DoubleType;
import mpicbg.imglib.type.numeric.real.FloatType;

/**
 * Reads in an imglib Image using Bio-Formats.
 *
 * <dl>
 * <dt><b>Source code:</b></dt>
 * <dd><a href="http://dev.imagejdev.org/trac/java/browser/trunk/projects/bf-imglib/src/main/java/imagej/io/ReadImage.java">Trac</a>,
 * <a href="http://dev.imagejdev.org/svn/java/trunk/projects/bf-imglib/src/main/java/imagej/io/ReadImage.java">SVN</a></dd>
 * </dl>
 */
public class ImageOpener {

  /** Reads in an imglib Image from the given source (e.g., file on disk). */
  public <T extends RealType<T>> Image<T> openImage(String id)
  throws FormatException, IOException
  {
    IFormatReader r = null;
    r = new ChannelSeparator();
    r = new ChannelFiller(r);
    r.setId(id);

    final String[] dimTypes = getDimTypes(r);
    final int[] dimLengths = getDimLengths(r);

    // determine data type
    final int pixelType = r.getPixelType();
    final T type = makeType(pixelType);

    // TEMP - make suffix out of dimension types, until imglib supports them
    final String suffix = toString(dimTypes);

    // create image object
    final ContainerFactory containerFactory = new ArrayContainerFactory();
    final ImageFactory<T> imageFactory =
      new ImageFactory<T>(type, containerFactory);
    final Image<T> img =
      imageFactory.createImage(dimLengths, id + " " + suffix);

    // TODO - create better container types; either:
    // 1) an array container type using one byte array per plane
    // 2) an array container type using one matching primitive array per plane
    // 3) as #1, but with an IFormatReader reference reading planes on demand
    // 4) as #2, but with an IFormatReader reference reading planes on demand

    // #1 is useful for efficient Bio-Formats import, and useful for tools
    //   needing byte arrays (e.g., BufferedImage Java3D texturing by reference?)
    // #2 is useful for efficient access to pixels in ImageJ (e.g., getPixels)
    // #3 is useful for efficient memory use for tools wanting matching
    //   primitive arrays (e.g., virtual stacks in ImageJ)
    // #4 is useful for efficient memory use

    // the solution below is general and works regardless of container,
    // but at the expense of performance both now and later

    // populate planes
    final LocalizableByDimCursor<T> cursor = img.createLocalizableByDimCursor();
    final int planeCount = r.getImageCount();
    byte[] plane = null;
    for (int no=0; no<planeCount; no++) {
      if (plane == null) plane = r.openBytes(no);
      else r.openBytes(no, plane);
      populatePlane(r, no, plane, cursor);
    }
    cursor.close();

    return img;
  }

  /** Converts Bio-Formats pixel type to imglib Type object. */
  @SuppressWarnings("unchecked")
  public <T extends RealType<T>> T makeType(int pixelType) {
    final RealType<?> type;
    switch (pixelType) {
      case FormatTools.UINT8:
        type = new UnsignedByteType();
        break;
      case FormatTools.INT8:
        type = new ByteType();
        break;
      case FormatTools.UINT16:
        type = new UnsignedShortType();
        break;
      case FormatTools.INT16:
        type = new ShortType();
        break;
      case FormatTools.UINT32:
        type = new UnsignedIntType();
        break;
      case FormatTools.INT32:
        type = new IntType();
        break;
      case FormatTools.FLOAT:
        type = new FloatType();
        break;
      case FormatTools.DOUBLE:
        type = new DoubleType();
        break;
      default:
        type = null;
    }
    return (T) type;
  }

  // -- Helper methods --

  /** Compiles an N-dimensional list of axis types from the given reader. */
  private String[] getDimTypes(IFormatReader r) {
    final int sizeX = r.getSizeX();
    final int sizeY = r.getSizeY();
    final int sizeZ = r.getSizeZ();
    final int sizeT = r.getSizeT();
    final String[] cDimTypes = r.getChannelDimTypes();
    final int[] cDimLengths = r.getChannelDimLengths();
    final String dimOrder = r.getDimensionOrder();
    final List<String> dimTypes = new ArrayList<String>();

    // add core dimensions
    for (char dim : dimOrder.toCharArray()) {
      switch (dim) {
        case 'X':
          if (sizeX > 1) dimTypes.add("X");
          break;
        case 'Y':
          if (sizeY > 1) dimTypes.add("Y");
          break;
        case 'Z':
          if (sizeZ > 1) dimTypes.add("Z");
          break;
        case 'T':
          if (sizeT > 1) dimTypes.add("Time");
          break;
        case 'C':
          for (int c=0; c<cDimTypes.length; c++) {
            int len = cDimLengths[c];
            if (len > 1) dimTypes.add(cDimTypes[c]);
          }
          break;
      }
    }

    return dimTypes.toArray(new String[0]);
  }

  /** Compiles an N-dimensional list of axis lengths from the given reader. */
  private int[] getDimLengths(IFormatReader r) {
    final int sizeX = r.getSizeX();
    final int sizeY = r.getSizeY();
    final int sizeZ = r.getSizeZ();
    final int sizeT = r.getSizeT();
    //final String[] cDimTypes = r.getChannelDimTypes();
    final int[] cDimLengths = r.getChannelDimLengths();
    final String dimOrder = r.getDimensionOrder();

    final List<Integer> dimLengthsList = new ArrayList<Integer>();

    // add core dimensions
    for (int i=0; i<dimOrder.length(); i++) {
      final char dim = dimOrder.charAt(i);
      switch (dim) {
        case 'X':
          if (sizeX > 1) dimLengthsList.add(sizeX);
          break;
        case 'Y':
          if (sizeY > 1) dimLengthsList.add(sizeY);
          break;
        case 'Z':
          if (sizeZ > 1) dimLengthsList.add(sizeZ);
          break;
        case 'T':
          if (sizeT > 1) dimLengthsList.add(sizeT);
          break;
        case 'C':
          for (int c=0; c<cDimLengths.length; c++) {
            int len = cDimLengths[c];
            if (len > 1) dimLengthsList.add(len);
          }
          break;
      }
    }

    // convert result to primitive array
    final int[] dimLengths = new int[dimLengthsList.size()];
    for (int i=0; i<dimLengths.length; i++){
      dimLengths[i] = dimLengthsList.get(i);
    }
    return dimLengths;
  }

  /** Copies the current dimensional position into the given array. */
  private void getPosition(IFormatReader r, int no, int[] pos) {
    final int sizeX = r.getSizeX();
    final int sizeY = r.getSizeY();
    final int sizeZ = r.getSizeZ();
    final int sizeT = r.getSizeT();
    //final String[] cDimTypes = r.getChannelDimTypes();
    final int[] cDimLengths = r.getChannelDimLengths();
    final String dimOrder = r.getDimensionOrder();

    final int[] zct = r.getZCTCoords(no);

    int index = 0;
    for (int i=0; i<dimOrder.length(); i++) {
      final char dim = dimOrder.charAt(i);
      switch (dim) {
        case 'X':
          if (sizeX > 1) index++; // NB: Leave X axis position alone.
          break;
        case 'Y':
          if (sizeY > 1) index++; // NB: Leave Y axis position alone.
          break;
        case 'Z':
          if (sizeZ > 1) pos[index++] = zct[0];
          break;
        case 'T':
          if (sizeT > 1) pos[index++] = zct[2];
          break;
        case 'C':
          final int[] cPos = FormatTools.rasterToPosition(cDimLengths, zct[1]);
          for (int c=0; c<cDimLengths.length; c++) {
            if (cDimLengths[c] > 1) pos[index++] = cPos[c];
          }
          break;
      }
    }
  }

  /** Compiles the given array of strings into a single string. */
  private String toString(String[] array) {
    final StringBuilder sb = new StringBuilder();
    boolean first = true;
    for (String s : array) {
      if (first) {
        sb.append("[");
        first = false;
      }
      else sb.append(" ");
      sb.append(s);
    }
    sb.append("]");
    return sb.toString();
  }

  private <T extends RealType<T>> void populatePlane(IFormatReader r,
    int no, byte[] plane, LocalizableByDimCursor<T> cursor)
  {
    final int sizeX = r.getSizeX();
    final int sizeY = r.getSizeY();
    final int pixelType = r.getPixelType();
    final boolean little = r.isLittleEndian();

    final int[] dimLengths = getDimLengths(r);
    final int[] pos = new int[dimLengths.length];

    for (int y=0; y<sizeY; y++) {
      for (int x=0; x<sizeX; x++) {
        final int index = sizeY * x + y;
        final double value = decodeWord(plane, index, pixelType, little);
        // need method to get N-dimensional position from reader
        // call getZCTCoords, then decode sub-C dims
        // set imglib cursor position to match, and assign decoded value
        getPosition(r, no, pos);
        pos[0] = x;
        pos[1] = y;
        cursor.setPosition(pos);
        cursor.getType().setReal(value);
      }
    }
  }

  private static double decodeWord(byte[] plane, int index,
    int pixelType, boolean little)
  {
    final double value;
    switch (pixelType) {
      case FormatTools.UINT8:
        value = plane[index] & 0xff;
        break;
      case FormatTools.INT8:
        value = plane[index];
        break;
      case FormatTools.UINT16:
        value = DataTools.bytesToShort(plane, 2 * index, 2, little) & 0xffff;
        break;
      case FormatTools.INT16:
        value = DataTools.bytesToShort(plane, 2 * index, 2, little);
        break;
      case FormatTools.UINT32:
        value = DataTools.bytesToInt(plane, 4 * index, 4, little) & 0xffffffff;
        break;
      case FormatTools.INT32:
        value = DataTools.bytesToInt(plane, 4 * index, 4, little);
        break;
      case FormatTools.FLOAT:
        value = DataTools.bytesToFloat(plane, 4 * index, 4, little);
        break;
      case FormatTools.DOUBLE:
        value = DataTools.bytesToDouble(plane, 4 * index, 4, little);
        break;
      default:
        value = Double.NaN;
    }
    return value;
  }

}
