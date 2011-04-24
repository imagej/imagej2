package imagej.display.view;

/*
 * $Id: GraphicsUtilities.java,v 1.1 2006/11/05 15:40:51 gfx Exp $
 *
 * Dual-licensed under LGPL (Sun and Romain Guy) and BSD (Romain Guy).
 *
 * Copyright 2005 Sun Microsystems, Inc., 4150 Network Circle,
 * Santa Clara, California 95054, U.S.A. All rights reserved.
 *
 * Copyright (c) 2006 Romain Guy <romain.guy@mac.com>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.awt.GraphicsConfiguration;
import java.awt.Transparency;
import java.awt.Graphics;
import java.awt.GraphicsEnvironment;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.io.IOException;
import java.net.URL;
import javax.imageio.ImageIO;

/**
 * <p><code>GraphicsUtilities</code> contains a set of tools to perform
 * common graphics operations easily. These operations are divided into
 * several themes, listed below.</p>
 * <h2>Compatible Images</h2>
 * <p>Compatible images can, and should, be used to increase drawing
 * performance. This class provides a number of methods to load compatible
 * images directly from files or to convert existing images to compatibles
 * images.</p>
 * <h2>Creating Thumbnails</h2>
 * <p>This class provides a number of methods to easily scale down images.
 * Some of these methods offer a trade-off between speed and result quality and
 * shouuld be used all the time. They also offer the advantage of producing
 * compatible images, thus automatically resulting into better runtime
 * performance.</p>
 * <p>All these methodes are both faster than
 * {@link java.awt.Image#getScaledInstance(int, int, int)} and produce
 * better-looking results than the various <code>drawImage()</code> methods
 * in {@link java.awt.Graphics}, which can be used for image scaling.</p>
 * <h2>Image Manipulation</h2>
 * <p>This class provides two methods to get and set pixels in a buffered image.
 * These methods try to avoid unmanaging the image in order to keep good
 * performance.</p>
 *
 * @author Romain Guy <romain.guy@mac.com>
 */
public class GraphicsUtilities {

  private static final GraphicsConfiguration CONFIGURATION =
          GraphicsEnvironment.getLocalGraphicsEnvironment().
          getDefaultScreenDevice().getDefaultConfiguration();

  private GraphicsUtilities() {
  }

  public static GraphicsConfiguration getCONFIGURATION() {
    return CONFIGURATION;
  }

  public static void showCofiguration() {
    System.out.println("defaultGraphicsConfiguration:" +
            "\ndevice: " + CONFIGURATION.getDevice() +
            "\nbounds: " + CONFIGURATION.getBounds() +
            "\ncolorModel: " + CONFIGURATION.getColorModel());


  }

  /**
   * <p>Returns a new <code>BufferedImage</code> using the same color model
   * as the image passed as a parameter. The returned image is only compatible
   * with the image passed as a parameter. This does not mean the returned
   * image is compatible with the hardware.</p>
   *
   * @param image the reference image from which the color model of the new
   *   image is obtained
   * @return a new <code>BufferedImage</code>, compatible with the color model
   *   of <code>image</code>
   */
  public static BufferedImage createColorModelCompatibleImage(BufferedImage image) {
    ColorModel cm = image.getColorModel();
    return new BufferedImage(cm,
            cm.createCompatibleWritableRaster(image.getWidth(),
            image.getHeight()),
            cm.isAlphaPremultiplied(), null);
  }

  /**
   * <p>Returns a new compatible image with the same width, height and
   * transparency as the image specified as a parameter.</p>
   *
   * @see java.awt.Transparency
   * @see #createCompatibleImage(int, int)
   * @see #createCompatibleImage(java.awt.image.BufferedImage, int, int)
   * @see #createTranslucentCompatibleImage(int, int)
   * @see #loadCompatibleImage(java.net.URL)
   * @see #toCompatibleImage(java.awt.image.BufferedImage)
   * @param image the reference image from which the dimension and the
   *   transparency of the new image are obtained
   * @return a new compatible <code>BufferedImage</code> with the same
   *   dimension and transparency as <code>image</code>
   */
  public static BufferedImage createCompatibleImage(BufferedImage image) {
    return createCompatibleImage(image, image.getWidth(), image.getHeight());
  }

  /**
   * <p>Returns a new compatible image of the specified width and height, and
   * the same transparency setting as the image specified as a parameter.</p>
   *
   * @see java.awt.Transparency
   * @see #createCompatibleImage(java.awt.image.BufferedImage)
   * @see #createCompatibleImage(int, int)
   * @see #createTranslucentCompatibleImage(int, int)
   * @see #loadCompatibleImage(java.net.URL)
   * @see #toCompatibleImage(java.awt.image.BufferedImage)
   * @param width the width of the new image
   * @param height the height of the new image
   * @param image the reference image from which the transparency of the new
   *   image is obtained
   * @return a new compatible <code>BufferedImage</code> with the same
   *   transparency as <code>image</code> and the specified dimension
   */
  public static BufferedImage createCompatibleImage(BufferedImage image,
          int width, int height) {
    return CONFIGURATION.createCompatibleImage(width, height,
            image.getTransparency());
  }

  /**
   * <p>Returns a new opaque compatible image of the specified width and
   * height.</p>
   *
   * @see #createCompatibleImage(java.awt.image.BufferedImage)
   * @see #createCompatibleImage(java.awt.image.BufferedImage, int, int)
   * @see #createTranslucentCompatibleImage(int, int)
   * @see #loadCompatibleImage(java.net.URL)
   * @see #toCompatibleImage(java.awt.image.BufferedImage)
   * @param width the width of the new image
   * @param height the height of the new image
   * @return a new opaque compatible <code>BufferedImage</code> of the
   *   specified width and height
   */
  public static BufferedImage createCompatibleImage(int width, int height) {
    return CONFIGURATION.createCompatibleImage(width, height);
  }

  /**
   * <p>Returns a new translucent compatible image of the specified width
   * and height.</p>
   *
   * @see #createCompatibleImage(java.awt.image.BufferedImage)
   * @see #createCompatibleImage(java.awt.image.BufferedImage, int, int)
   * @see #createCompatibleImage(int, int)
   * @see #loadCompatibleImage(java.net.URL)
   * @see #toCompatibleImage(java.awt.image.BufferedImage)
   * @param width the width of the new image
   * @param height the height of the new image
   * @return a new translucent compatible <code>BufferedImage</code> of the
   *   specified width and height
   */
  public static BufferedImage createTranslucentCompatibleImage(int width,
          int height) {
    return CONFIGURATION.createCompatibleImage(width, height,
            Transparency.TRANSLUCENT);
  }

  /**
   * <p>Returns a new compatible image from a URL. The image is loaded from the
   * specified location and then turned, if necessary into a compatible
   * image.</p>
   *
   * @see #createCompatibleImage(java.awt.image.BufferedImage)
   * @see #createCompatibleImage(java.awt.image.BufferedImage, int, int)
   * @see #createCompatibleImage(int, int)
   * @see #createTranslucentCompatibleImage(int, int)
   * @see #toCompatibleImage(java.awt.image.BufferedImage)
   * @param resource the URL of the picture to load as a compatible image
   * @return a new translucent compatible <code>BufferedImage</code> of the
   *   specified width and height
   * @throws java.io.IOException if the image cannot be read or loaded
   */
  public static BufferedImage loadCompatibleImage(URL resource)
          throws IOException {
    BufferedImage image = ImageIO.read(resource);
    return toCompatibleImage(image);
  }

  /**
   * <p>Return a new compatible image that contains a copy of the specified
   * image. This method ensures an image is compatible with the hardware,
   * and therefore optimized for fast blitting operations.</p>
   *
   * @see #createCompatibleImage(java.awt.image.BufferedImage)
   * @see #createCompatibleImage(java.awt.image.BufferedImage, int, int)
   * @see #createCompatibleImage(int, int)
   * @see #createTranslucentCompatibleImage(int, int)
   * @see #loadCompatibleImage(java.net.URL)
   * @param image the image to copy into a new compatible image
   * @return a new compatible copy, with the
   *   same width and height and transparency and content, of <code>image</code>
   */
  public static BufferedImage toCompatibleImage(BufferedImage image) {
    if (image.getColorModel().equals(CONFIGURATION.getColorModel())) {
      return image;
    }
    BufferedImage compatibleImage = CONFIGURATION.createCompatibleImage(
            image.getWidth(), image.getHeight(), image.getTransparency());
    Graphics g = compatibleImage.getGraphics();
    g.drawImage(image, 0, 0, null);
    g.dispose();

    return compatibleImage;
  }

  /**
   * <p>Returns a thumbnail of a source image. <code>newSize</code> defines
   * the length of the longest dimension of the thumbnail. The other
   * dimension is then computed according to the dimensions ratio of the
   * original picture.</p>
   * <p>This method favors speed over quality. When the new size is less than
   * half the longest dimension of the source image,
   * {@link #createThumbnail(BufferedImage, int)} or
   * {@link #createThumbnail(BufferedImage, int, int)} should be used instead
   * to ensure the quality of the result without sacrificing too much
   * performance.</p>
   *
   * @see #createThumbnailFast(java.awt.image.BufferedImage, int, int)
   * @see #createThumbnail(java.awt.image.BufferedImage, int)
   * @see #createThumbnail(java.awt.image.BufferedImage, int, int)
   * @param image the source image
   * @param newSize the length of the largest dimension of the thumbnail
   * @return a new compatible <code>BufferedImage</code> containing a
   *   thumbnail of <code>image</code>
   * @throws IllegalArgumentException if <code>newSize</code> is larger than
   *   the largest dimension of <code>image</code> or &lt;= 0
   */
  public static BufferedImage createThumbnailFast(BufferedImage image,
          int newSize) {
    float ratio;
    int width = image.getWidth();
    int height = image.getHeight();

    if (width > height) {
      if (newSize >= width) {
        throw new IllegalArgumentException("newSize must be lower than" +
                " the image width");
      } else if (newSize <= 0) {
        throw new IllegalArgumentException("newSize must" +
                " be greater than 0");
      }

      ratio = (float) width / (float) height;
      width = newSize;
      height = (int) (newSize / ratio);
    } else {
      if (newSize >= height) {
        throw new IllegalArgumentException("newSize must be lower than" +
                " the image height");
      } else if (newSize <= 0) {
        throw new IllegalArgumentException("newSize must" +
                " be greater than 0");
      }

      ratio = (float) height / (float) width;
      height = newSize;
      width = (int) (newSize / ratio);
    }

    BufferedImage temp = createCompatibleImage(image, width, height);
    Graphics2D g2 = temp.createGraphics();
    g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
            RenderingHints.VALUE_INTERPOLATION_BILINEAR);
    g2.drawImage(image, 0, 0, temp.getWidth(), temp.getHeight(), null);
    g2.dispose();

    return temp;
  }

  /**
   * <p>Returns a thumbnail of a source image.</p>
   * <p>This method favors speed over quality. When the new size is less than
   * half the longest dimension of the source image,
   * {@link #createThumbnail(BufferedImage, int)} or
   * {@link #createThumbnail(BufferedImage, int, int)} should be used instead
   * to ensure the quality of the result without sacrificing too much
   * performance.</p>
   *
   * @see #createThumbnailFast(java.awt.image.BufferedImage, int)
   * @see #createThumbnail(java.awt.image.BufferedImage, int)
   * @see #createThumbnail(java.awt.image.BufferedImage, int, int)
   * @param image the source image
   * @param newWidth the width of the thumbnail
   * @param newHeight the height of the thumbnail
   * @return a new compatible <code>BufferedImage</code> containing a
   *   thumbnail of <code>image</code>
   * @throws IllegalArgumentException if <code>newWidth</code> is larger than
   *   the width of <code>image</code> or if code>newHeight</code> is larger
   *   than the height of <code>image</code> or if one of the dimensions
   *   is &lt;= 0
   */
  public static BufferedImage createThumbnailFast(BufferedImage image,
          int newWidth, int newHeight) {
    if (newWidth >= image.getWidth() ||
            newHeight >= image.getHeight()) {
      throw new IllegalArgumentException("newWidth and newHeight cannot" +
              " be greater than the image" +
              " dimensions");
    } else if (newWidth <= 0 || newHeight <= 0) {
      throw new IllegalArgumentException("newWidth and newHeight must" +
              " be greater than 0");
    }

    BufferedImage temp = createCompatibleImage(image, newWidth, newHeight);
    Graphics2D g2 = temp.createGraphics();
    g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
            RenderingHints.VALUE_INTERPOLATION_BILINEAR);
    g2.drawImage(image, 0, 0, temp.getWidth(), temp.getHeight(), null);
    g2.dispose();

    return temp;
  }

  /**
   * <p>Returns a thumbnail of a source image. <code>newSize</code> defines
   * the length of the longest dimension of the thumbnail. The other
   * dimension is then computed according to the dimensions ratio of the
   * original picture.</p>
   * <p>This method offers a good trade-off between speed and quality.
   * The result looks better than
   * {@link #createThumbnailFast(java.awt.image.BufferedImage, int)} when
   * the new size is less than half the longest dimension of the source
   * image, yet the rendering speed is almost similar.</p>
   *
   * @see #createThumbnailFast(java.awt.image.BufferedImage, int, int)
   * @see #createThumbnailFast(java.awt.image.BufferedImage, int)
   * @see #createThumbnail(java.awt.image.BufferedImage, int, int)
   * @param image the source image
   * @param newSize the length of the largest dimension of the thumbnail
   * @return a new compatible <code>BufferedImage</code> containing a
   *   thumbnail of <code>image</code>
   * @throws IllegalArgumentException if <code>newSize</code> is larger than
   *   the largest dimension of <code>image</code> or &lt;= 0
   */
  public static BufferedImage createThumbnail(BufferedImage image,
          int newSize) {
    int width = image.getWidth();
    int height = image.getHeight();

    boolean isWidthGreater = width > height;

    if (isWidthGreater) {
      if (newSize >= width) {
        throw new IllegalArgumentException("newSize must be lower than" +
                " the image width");
      }
    } else if (newSize >= height) {
      throw new IllegalArgumentException("newSize must be lower than" +
              " the image height");
    }

    if (newSize <= 0) {
      throw new IllegalArgumentException("newSize must" +
              " be greater than 0");
    }

    float ratioWH = (float) width / (float) height;
    float ratioHW = (float) height / (float) width;

    BufferedImage thumb = image;

    do {
      if (isWidthGreater) {
        width /= 2;
        if (width < newSize) {
          width = newSize;
        }
        height = (int) (width / ratioWH);
      } else {
        height /= 2;
        if (height < newSize) {
          height = newSize;
        }
        width = (int) (height / ratioHW);
      }

      BufferedImage temp = createCompatibleImage(image, width, height);
      Graphics2D g2 = temp.createGraphics();
      g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
              RenderingHints.VALUE_INTERPOLATION_BILINEAR);
      g2.drawImage(thumb, 0, 0, temp.getWidth(), temp.getHeight(), null);
      g2.dispose();

      thumb = temp;
    } while (newSize != (isWidthGreater ? width : height));

    return thumb;
  }

  /**
   * <p>Returns a thumbnail of a source image.</p>
   * <p>This method offers a good trade-off between speed and quality.
   * The result looks better than
   * {@link #createThumbnailFast(java.awt.image.BufferedImage, int)} when
   * the new size is less than half the longest dimension of the source
   * image, yet the rendering speed is almost similar.</p>
   *
   * @see #createThumbnailFast(java.awt.image.BufferedImage, int)
   * @see #createThumbnailFast(java.awt.image.BufferedImage, int, int)
   * @see #createThumbnail(java.awt.image.BufferedImage, int)
   * @param image the source image
   * @param newWidth the width of the thumbnail
   * @param newHeight the height of the thumbnail
   * @return a new compatible <code>BufferedImage</code> containing a
   *   thumbnail of <code>image</code>
   * @throws IllegalArgumentException if <code>newWidth</code> is larger than
   *   the width of <code>image</code> or if code>newHeight</code> is larger
   *   than the height of <code>image or if one the dimensions is not &gt; 0</code>
   */
  public static BufferedImage createThumbnail(BufferedImage image,
          int newWidth, int newHeight) {
    int width = image.getWidth();
    int height = image.getHeight();

    if (newWidth >= width || newHeight >= height) {
      throw new IllegalArgumentException("newWidth and newHeight cannot" +
              " be greater than the image" +
              " dimensions");
    } else if (newWidth <= 0 || newHeight <= 0) {
      throw new IllegalArgumentException("newWidth and newHeight must" +
              " be greater than 0");
    }
    BufferedImage thumb = image;
    do {
      if (width > newWidth) {
        width /= 2;
        if (width < newWidth) {
          width = newWidth;
        }
      }
      if (height > newHeight) {
        height /= 2;
        if (height < newHeight) {
          height = newHeight;
        }
      }
      BufferedImage temp = createCompatibleImage(image, width, height);
      Graphics2D g2 = temp.createGraphics();
      g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
              RenderingHints.VALUE_INTERPOLATION_BILINEAR);
      g2.drawImage(thumb, 0, 0, temp.getWidth(), temp.getHeight(), null);
      g2.dispose();

      thumb = temp;
    } while (width != newWidth || height != newHeight);

    return thumb;
  }

  /**
   * <p>Returns an array of pixels, stored as integers, from a
   * <code>BufferedImage</code>. The pixels are grabbed from a rectangular
   * area defined by a location and two dimensions. Calling this method on
   * an image of type different from <code>BufferedImage.TYPE_INT_ARGB</code>
   * and <code>BufferedImage.TYPE_INT_RGB</code> will unmanage the image.</p>
   *
   * @param img the source image
   * @param x the x location at which to start grabbing pixels
   * @param y the y location at which to start grabbing pixels
   * @param w the width of the rectangle of pixels to grab
   * @param h the height of the rectangle of pixels to grab
   * @param pixels a pre-allocated array of pixels of size w*h; can be null
   * @return <code>pixels</code> if non-null, a new array of integers
   *   otherwise
   * @throws IllegalArgumentException is <code>pixels</code> is non-null and
   *   of length &lt; w*h
   */
  public static int[] getPixels(BufferedImage img,
          int x, int y, int w, int h, int[] pixels) {
    if (w == 0 || h == 0) {
      return new int[0];
    }

    if (pixels == null) {
      pixels = new int[w * h];
    } else if (pixels.length < w * h) {
      throw new IllegalArgumentException("pixels array must have a length" +
              " >= w*h");
    }

    int imageType = img.getType();
    if (imageType == BufferedImage.TYPE_INT_ARGB ||
            imageType == BufferedImage.TYPE_INT_RGB) {
      Raster raster = img.getRaster();
      return (int[]) raster.getDataElements(x, y, w, h, pixels);
    }

    // Unmanages the image
    return img.getRGB(x, y, w, h, pixels, 0, w);
  }

  /**
   * <p>Writes a rectangular area of pixels in the destination
   * <code>BufferedImage</code>. Calling this method on
   * an image of type different from <code>BufferedImage.TYPE_INT_ARGB</code>
   * and <code>BufferedImage.TYPE_INT_RGB</code> will unmanage the image.</p>
   *
   * @param img the destination image
   * @param x the x location at which to start storing pixels
   * @param y the y location at which to start storing pixels
   * @param w the width of the rectangle of pixels to store
   * @param h the height of the rectangle of pixels to store
   * @param pixels an array of pixels, stored as integers
   * @throws IllegalArgumentException is <code>pixels</code> is non-null and
   *   of length &lt; w*h
   */
  public static void setPixels(BufferedImage img,
          int x, int y, int w, int h, int[] pixels) {
    if (pixels == null || w == 0 || h == 0) {
      return;
    } else if (pixels.length < w * h) {
      throw new IllegalArgumentException("pixels array must have a length >= w*h");
    }

    int imageType = img.getType();
    if (imageType == BufferedImage.TYPE_INT_ARGB ||
            imageType == BufferedImage.TYPE_INT_RGB) {
      WritableRaster raster = img.getRaster();
      raster.setDataElements(x, y, w, h, pixels);
    } else {
      // Unmanages the image
      img.setRGB(x, y, w, h, pixels, 0, w);
    }
  }

  public static void main(String[] args) {
    showCofiguration();
  }
}
