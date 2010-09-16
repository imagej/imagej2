//
// ReadImage.java
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

import loci.formats.FormatException;
import mpicbg.imglib.cursor.Cursor;
import mpicbg.imglib.image.Image;
import mpicbg.imglib.type.numeric.RealType;

/**
 * A simple test for {@link ImageOpener}.
 *
 * <dl><dt><b>Source code:</b></dt>
 * <dd><a href="http://dev.imagejdev.org/trac/java/browser/trunk/projects/bf-imglib/src/main/java/imagej/io/ReadImage.java">Trac</a>,
 * <a href="http://dev.imagejdev.org/svn/java/trunk/projects/bf-imglib/src/main/java/imagej/io/ReadImage.java">SVN</a></dd></dl>
 */
public class ReadImage {

  public static <T extends RealType<T>> void main(String[] args)
  throws FormatException, IOException
  {
    final ImageOpener imageOpener = new ImageOpener();
    args = new String[] {//TEMP
      "/Users/curtis/data/Spindle_Green_d3d.dv",//TEMP
      "/Users/curtis/data/mitosis-test.ipw",//TEMP
      "/Users/curtis/data/test_greys.lif",//TEMP
      "/Users/curtis/data/slice1_810nm_40x_z1_pcc100_scanin_20s_01.sdt"//TEMP
    };//TEMP
    for (String arg : args) {
      Image<T> img = imageOpener.openImage(arg);

      // print out some useful information about the image
      System.out.println(img);
      final Cursor<T> cursor = img.createCursor();
      cursor.fwd();
      System.out.println("\tType = " + cursor.getType().getClass().getName());
      cursor.close();
    }
  }

}
