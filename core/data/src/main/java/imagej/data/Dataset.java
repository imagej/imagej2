/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2012 Board of Regents of the University of
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

import net.imglib2.img.ImgPlus;
import net.imglib2.meta.Metadata;
import net.imglib2.type.numeric.RealType;

/**
 * Dataset is the primary image data structure in ImageJ. A Dataset wraps an
 * ImgLib {@link ImgPlus}. It also provides a number of convenience methods,
 * such as the ability to access pixels on a plane-by-plane basis, and create
 * new Datasets of various types easily.
 * 
 * @author Curtis Rueden
 * @author Barry DeZonia
 */
public interface Dataset extends Data, Metadata {

	/** TODO */
	boolean isDirty();

	/** TODO */
	void setDirty(boolean value);

	/** TODO */
	ImgPlus<? extends RealType<?>> getImgPlus();

	/** TODO */
	<T extends RealType<T>> ImgPlus<T> typedImg(T t);

	/** TODO */
	void setImgPlus(final ImgPlus<? extends RealType<?>> imgPlus);

	/**
	 * gets a plane of data from the Dataset. The representation of the plane is
	 * determined by the native ImgLib container. This method will create a copy
	 * of the original data if it cannot obtain a direct reference.
	 */
	Object getPlane(final int planeNumber);

	/**
	 * gets a plane of data from the Dataset. The representation of the plane is
	 * determined by the native ImgLib container. The behavior of this method when
	 * a reference to the actual data cannot be obtained depends upon the value of
	 * the input copyOK boolean. If copyOK is true a copy of the data is created
	 * and returned. If copyOK is false null is returned.
	 */
	Object getPlane(final int planeNumber, boolean copyOK);

	/** TODO */
	void setPlane(final int no, final Object newPlane);

	/** TODO */
	RealType<?> getType();

	/** TODO */
	boolean isSigned();

	/** TODO */
	boolean isInteger();

	/** Gets a short string description of the dataset's pixel type. */
	String getTypeLabelShort();

	/** Gets the full string description of the dataset's pixel type. */
	String getTypeLabelLong();

	/** Creates a copy of the dataset. */
	Dataset duplicate();

	/** Creates a copy of the dataset, but without copying any pixel values. */
	Dataset duplicateBlank();

	/** Copies the dataset's pixels into the given target dataset. */
	void copyInto(final Dataset target);

	// TODO - eliminate legacy layer specific functionality in favor of a
	// generic properties system (setProperty(String, Object),
	// getProperty(String)) for storing arbitrary key/value pairs about the data.
	// The property system can be part of Data, and implemented in AbstractData.

	/**
	 * For use in legacy layer only, this flag allows the various legacy layer
	 * image translators to support color images correctly.
	 */
	void setRGBMerged(final boolean rgbMerged);

	/**
	 * For use in legacy layer only, this flag allows the various legacy layer
	 * image translators to support color images correctly.
	 */
	boolean isRGBMerged();

	/** TODO */
	void typeChange();

	/** TODO */
	void rgbChange();

	/**
	 * Changes a Dataset's internal data and metadata to match that from a given
	 * Dataset. Only its name stays the same. Written to allow nonplanar
	 * representations to copy data from other Datasets as needed to get around
	 * the fact that its data is not being shared by reference.
	 */
	void copyDataFrom(final Dataset other);

	double getBytesOfInfo();
}
