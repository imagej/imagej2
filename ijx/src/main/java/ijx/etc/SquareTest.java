package ijx.etc;

/**
 * Copyright (c) 2010, Stephan Preibisch & Stephan Saalfeld
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.  Redistributions in binary
 * form must reproduce the above copyright notice, this list of conditions and
 * the following disclaimer in the documentation and/or other materials
 * provided with the distribution.  Neither the name of the Fiji project nor
 * the names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;

import java.io.File;
import java.io.IOException;

import javax.swing.JFileChooser;
import loci.formats.FormatException;

import mpicbg.imglib.container.Container;
import mpicbg.imglib.container.array.ArrayContainerFactory;
import mpicbg.imglib.container.imageplus.ImagePlusContainer;
import mpicbg.imglib.cursor.Cursor;
import mpicbg.imglib.exception.ImgLibException;
import mpicbg.imglib.image.Image;
import mpicbg.imglib.image.display.imagej.ImageJFunctions;
import mpicbg.imglib.io.ImageOpener;
import mpicbg.imglib.io.LOCI;
import mpicbg.imglib.type.numeric.RealType;
import mpicbg.imglib.type.numeric.real.FloatType;

/**
 * A very simple imglib test that squares an image.
 * Displays both input and output images onscreen using ImageJ.
 *
 * @author Curtis Rueden ctrueden at wisc.edu
 */
public class SquareTest<T extends RealType<T>> {

    /** Executes the test. */
    public void execute()
            throws FormatException, IOException {
        File file = chooseFile();
        if (file == null) {
            return;
        }
        final ImageOpener imageOpener = new ImageOpener();
        // read all arguments using auto-detected type with default container
        System.out.println("== AUTO-DETECTED TYPE, DEFAULT CONTAINER ==");

        Image<T> inImg = imageOpener.openImage(file.getPath());

        //Image<FloatType> inImg = LOCI.openLOCIFloatType(file.getPath(), new ArrayContainerFactory());
        Image<T> outImg = square(inImg);

        // show ImageJ control panel window
        if (IJ.getInstance() == null) {
            new ImageJ();
        }
        display(inImg, file.getName());
        display(outImg, "Squared");
    }

    /** Computes the square of a numeric image. */
    public <T extends RealType<T>> Image<T> square(Image<T> inputImage) {
        //ImageFactory<T> factory = new ImageFactory<T>(inputImage.createType(), new ArrayContainerFactory());
        //Image<T> outputImage = factory.createImage(new int[] {512, 512});
        Image<T> outputImage = inputImage.createNewImage();
        Cursor<T> inputCursor = inputImage.createCursor();
        Cursor<T> outputCursor = outputImage.createCursor();
        while (inputCursor.hasNext()) {
            inputCursor.fwd();
            outputCursor.fwd();
            float value = inputCursor.getType().getRealFloat();
            outputCursor.getType().setReal(value * value);
        }
        inputCursor.close();
        outputCursor.close();
        return outputImage;
    }

    /** Prompts the user to choose a file on disk. */
    public File chooseFile() {
        JFileChooser jc = new JFileChooser();
        int result = jc.showOpenDialog(null);
        if (result != JFileChooser.APPROVE_OPTION) {
            return null;
        }
        return jc.getSelectedFile();
    }

    /** Displays the given imglib image as an ImagePlus. */
    public static <T extends RealType<T>> void display(Image<T> img, String title) {
        ImagePlus imp = null;
        Container<T> c = img.getContainer();
        if (c instanceof ImagePlusContainer<?, ?>) {
            ImagePlusContainer<T, ?> ipc = (ImagePlusContainer<T, ?>) c;
            try {
                imp = ipc.getImagePlus();
            } catch (ImgLibException exc) {
                IJ.log("Warning: " + exc.getMessage());
            }
        }
        if (imp == null) {
            imp = ImageJFunctions.copyToImagePlus(img);
        }
        if (title != null) {
            imp.setTitle(title);
        }
        imp.show();
    }

    	/** Prints out some useful information about the {@link Image}. */
	public static <T extends RealType<T>> void reportInformation(Image<T> img) {
		System.out.println(img);
		final Cursor<T> cursor = img.createCursor();
		cursor.fwd();
		System.out.println("\tType = " + cursor.getType().getClass().getName());
		System.out.println("\tContainer = " + cursor.getStorageContainer().getClass().getName());
		cursor.close();
	}

    public static <T extends RealType<T>> void main(String[] args)
            throws FormatException, IOException {
        SquareTest<T> test = new SquareTest<T>();
        test.execute();
    }
}
