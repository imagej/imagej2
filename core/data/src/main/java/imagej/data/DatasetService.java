/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2013 Board of Regents of the University of
 * Wisconsin-Madison, Broad Institute of MIT and Harvard, and Max Planck
 * Institute of Molecular Cell Biology and Genetics.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * 
 * The views and conclusions contained in the software and documentation are
 * those of the authors and should not be interpreted as representing official
 * policies, either expressed or implied, of any organization.
 * #L%
 */

package imagej.data;

import imagej.data.display.ImageDisplay;
import imagej.service.IJService;

import java.io.IOException;
import java.util.List;

import net.imglib2.img.ImgFactory;
import net.imglib2.meta.AxisType;
import net.imglib2.meta.ImgPlus;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

import org.scijava.object.ObjectService;

/**
 * Interface for service that works with {@link Dataset}s.
 * 
 * @author Curtis Rueden
 */
public interface DatasetService extends IJService {

	ObjectService getObjectService();

	/**
	 * Gets a list of all {@link Dataset}s. This method is a shortcut that
	 * delegates to {@link ObjectService}.
	 */
	List<Dataset> getDatasets();

	/**
	 * Gets a list of {@link Dataset}s linked to the given {@link ImageDisplay}.
	 */
	List<Dataset> getDatasets(ImageDisplay display);

	/**
	 * Creates a new dataset.
	 * 
	 * @param dims The dataset's dimensional extents.
	 * @param name The dataset's name.
	 * @param axes The dataset's dimensional axis labels.
	 * @param bitsPerPixel The dataset's bit depth. Currently supported bit depths
	 *          include 1, 8, 12, 16, 32 and 64.
	 * @param signed Whether the dataset's pixels can have negative values.
	 * @param floating Whether the dataset's pixels can have non-integer values.
	 * @return The newly created dataset.
	 * @throws IllegalArgumentException If the combination of bitsPerPixel, signed
	 *           and floating parameters do not form a valid data type.
	 */
	Dataset create(long[] dims, String name, AxisType[] axes, int bitsPerPixel,
		boolean signed, boolean floating);

	/**
	 * Creates a new dataset.
	 * 
	 * @param <T> The type of the dataset.
	 * @param type The type of the dataset.
	 * @param dims The dataset's dimensional extents.
	 * @param name The dataset's name.
	 * @param axes The dataset's dimensional axis labels.
	 * @return The newly created dataset.
	 */
	<T extends RealType<T> & NativeType<T>> Dataset create(T type, long[] dims,
		String name, AxisType[] axes);

	/**
	 * Creates a new dataset using the provided {@link ImgFactory}.
	 * 
	 * @param <T> The type of the dataset.
	 * @param factory The ImgFactory to use to create the data.
	 * @param type The type of the dataset.
	 * @param dims The dataset's dimensional extents.
	 * @param name The dataset's name.
	 * @param axes The dataset's dimensional axis labels.
	 * @return The newly created dataset.
	 */
	<T extends RealType<T>> Dataset create(
		ImgFactory<T> factory, T type, long[] dims, String name, AxisType[] axes);

	/**
	 * Creates a new dataset using the provided {@link ImgPlus}.
	 * 
	 * @param imgPlus The {@link ImgPlus} backing the dataset.
	 * @return The newly created dataset.
	 */
	<T extends RealType<T>> Dataset create(ImgPlus<T> imgPlus);

	/**
	 * Determines whether the given source can be opened as a {@link Dataset}
	 * using the {@link #open(String)} method.
	 */
	boolean canOpen(String source);

	/**
	 * Determines whether the given destination can be used to save a
	 * {@link Dataset} using the {@link #save(Dataset, String)} method.
	 */
	boolean canSave(String destination);

	/** Loads a dataset from a source (such as a file on disk). */
	Dataset open(String source) throws IOException;

	/** Reverts the given dataset to its original source. */
	void revert(Dataset dataset) throws IOException;

	/** Saves a dataset to a destination (such as a file on disk). */
	void save(Dataset dataset, String destination) throws IOException;
}
