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

import org.scijava.Context;

import net.imglib2.Positionable;
import net.imglib2.RealPositionable;
import net.imglib2.display.ColorTable;
import net.imglib2.img.ImgPlus;
import net.imglib2.meta.AxisType;
import net.imglib2.type.numeric.ComplexType;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;

// TODO
// Using this class we should relax Dataset's base type to NumericType
// from RealType. Then those who need more specific access can use the
// static helper methods below. Thus we can break ImageJ2's reliance
// on RealType. And we can support LongType with no data loss by static
// cast to IntegerType.
/**
 * TypedDataset is a class that allows untyped Datasets to be accessed in
 * a typed fashion. It has a number of static helper methods for constructing
 * TypedDatasets.
 * 
 * @author Barry DeZonia
 */
@SuppressWarnings("unchecked")
public class TypedDataset<T> implements Dataset {

	// -- fields --
	
	private final Dataset ds;
	
	// -- constructor --
	
	// NB - private to disallow incorrectly typed data. to instantiate use static helper methods.
	private TypedDataset(Dataset ds) {
		this.ds = ds;
	}
	
	// -- public methods --
	
	/**
	 * Returns the internal ImgPlus data in a typed fashion.
	 */
	public ImgPlus<T> getData() {
		return (ImgPlus<T>) ds.getImgPlus();
	}

	/**
	 * Returns true if this TypedDataset is based upon the given Dataset.
	 */
	public boolean isBasedOn(Dataset ds) {
		return this.ds == ds;
	}
	
	// TODO
	// allow this?
	// might allow us to mess up internal consistency by rogue ds.setImgPlus()
	// Dataset getDataset() { return ds; }
	
	// -- static construction helpers --
	
	/**
	 * Create a TypedDataset of NumericType given a Dataset. Returns null if
	 * the given Dataset does not have data of NumericType.
	 */
	@SuppressWarnings("rawtypes")
	public static TypedDataset<NumericType<?>> numeric(Dataset dataset) {
		Object type = dataset.getImgPlus().firstElement();
		if (NumericType.class.isAssignableFrom(type.getClass())) {
			TypedDataset typedDataset = new TypedDataset(dataset);
			return (TypedDataset<NumericType<?>>) typedDataset;
		}
		return null;
	}

	/**
	 * Create a TypedDataset of ComplexType given a Dataset. Returns null if
	 * the given Dataset does not have data of ComplexType.
	 */
	@SuppressWarnings("rawtypes")
	public static TypedDataset<ComplexType<?>> complex(Dataset dataset) {
		Object type = dataset.getImgPlus().firstElement();
		if (ComplexType.class.isAssignableFrom(type.getClass())) {
			TypedDataset typedDataset = new TypedDataset(dataset);
			return (TypedDataset<ComplexType<?>>) typedDataset;
		}
		return null;
	}
	
	/**
	 * Create a TypedDataset of RealType given a Dataset. Returns null if
	 * the given Dataset does not have data of RealType.
	 */
	@SuppressWarnings("rawtypes")
	public static TypedDataset<RealType<?>> real(Dataset dataset) {
		Object type = dataset.getImgPlus().firstElement();
		if (NumericType.class.isAssignableFrom(type.getClass())) {
			TypedDataset typedDataset = new TypedDataset(dataset);
			return (TypedDataset<RealType<?>>) typedDataset;
		}
		return null;
	}
	
	/**
	 * Create a TypedDataset of IntegerType given a Dataset. Returns null if
	 * the given Dataset does not have data of IntegerType.
	 */
	@SuppressWarnings("rawtypes")
	public static TypedDataset<IntegerType<?>> integer(Dataset dataset) {
		Object type = dataset.getImgPlus().firstElement();
		if (NumericType.class.isAssignableFrom(type.getClass())) {
			TypedDataset typedDataset = new TypedDataset(dataset);
			return (TypedDataset<IntegerType<?>>) typedDataset;
		}
		return null;
	}

	/**
	 * Create a TypedDataset of a given type from a Dataset. Returns null if
	 * the given Dataset does not have data of the given type.
	 */
	@SuppressWarnings("rawtypes")
	public static <T extends NumericType<T>> TypedDataset<T> asType(Dataset dataset, T type) {
		Object var = dataset.getImgPlus().firstElement();
		if (var.getClass().isAssignableFrom(type.getClass())) {
			TypedDataset typedDataset = new TypedDataset(dataset);
			return (TypedDataset<T>) typedDataset;
		}
		return null;
	}
	
	// -- Dataset methods (implemented by delegation) --
	
	@Override
	public void incrementReferences() {
		ds.incrementReferences(); // TODO correct??
	}

	@Override
	public void decrementReferences() {
		ds.decrementReferences(); // TODO correct??
	}

	@Override
	public AxisType[] getAxes() {
		return ds.getAxes();
	}

	@Override
	public boolean isDiscrete() {
		return ds.isDiscrete();
	}

	@Override
	public Extents getExtents() {
		return ds.getExtents();
	}

	@Override
	public long[] getDims() {
		return ds.getDims();
	}

	@Override
	public void axes(AxisType[] axes) {
		ds.axes(axes);
	}

	@Override
	public AxisType axis(int axisNumber) {
		return ds.axis(axisNumber);
	}

	@Override
	public double calibration(int axisNumber) {
		return ds.calibration(axisNumber);
	}

	@Override
	public void calibration(double[] calibrations) {
		ds.calibration(calibrations);
	}

	@Override
	public void calibration(float[] calibrations) {
		ds.calibration(calibrations);
	}

	@Override
	public int getAxisIndex(AxisType axisType) {
		return ds.getAxisIndex(axisType);
	}

	@Override
	public void setAxis(AxisType axisType, int axisNum) {
		ds.setAxis(axisType, axisNum);
	}

	@Override
	public void setCalibration(double[] calibrations) {
		ds.setCalibration(calibrations);
	}

	@Override
	public void setCalibration(float[] calibrations) {
		ds.setCalibration(calibrations);
	}

	@Override
	public void setCalibration(double cal, int axisNum) {
		ds.setCalibration(cal, axisNum);
	}

	@Override
	public int numDimensions() {
		return ds.numDimensions();
	}

	@Override
	public long max(int axisNum) {
		return ds.max(axisNum);
	}

	@Override
	public void max(long[] maxes) {
		ds.max(maxes);
	}

	@Override
	public void max(Positionable pos) {
		ds.max(pos);
	}

	@Override
	public long min(int axisNum) {
		return ds.min(axisNum);
	}

	@Override
	public void min(long[] mins) {
		ds.min(mins);
	}

	@Override
	public void min(Positionable pos) {
		ds.min(pos);
	}

	@Override
	public double realMax(int axisNum) {
		return ds.realMax(axisNum);
	}

	@Override
	public void realMax(double[] realMaxes) {
		ds.realMax(realMaxes);
	}

	@Override
	public void realMax(RealPositionable pos) {
		ds.realMax(pos);
	}

	@Override
	public double realMin(int axisNum) {
		return ds.realMin(axisNum);
	}

	@Override
	public void realMin(double[] realMins) {
		ds.realMin(realMins);
	}

	@Override
	public void realMin(RealPositionable pos) {
		ds.realMin(pos);
	}

	@Override
	public long dimension(int axisNum) {
		return ds.dimension(axisNum);
	}

	@Override
	public void dimensions(long[] dims) {
		ds.dimensions(dims);
	}

	@Override
	public String getName() {
		return ds.getName();
	}

	@Override
	public void setName(String name) {
		ds.setName(name);
	}

	@Override
	public Context getContext() {
		return ds.getContext();
	}

	@Override
	public void setContext(Context context) {
		ds.setContext(context);	
	}

	@Override
	public String getSource() {
		return ds.getSource();
	}

	@Override
	public void setSource(String src) {
		ds.setSource(src);
	}

	@Override
	public double getChannelMaximum(int axisNum) {
		return ds.getChannelMaximum(axisNum);
	}

	@Override
	public double getChannelMinimum(int axisNum) {
		return ds.getChannelMinimum(axisNum);
	}

	@Override
	public ColorTable getColorTable(int index) {
		return ds.getColorTable(index);
	}

	@Override
	public int getColorTableCount() {
		return ds.getColorTableCount();
	}

	@Override
	public int getCompositeChannelCount() {
		return ds.getCompositeChannelCount();
	}

	@Override
	public int getValidBits() {
		return ds.getValidBits();
	}

	@Override
	public void initializeColorTables(int tableCount) {
		ds.initializeColorTables(tableCount);
	}

	@Override
	public void setChannelMaximum(int axisNum, double value) {
		ds.setChannelMaximum(axisNum, value);
	}

	@Override
	public void setChannelMinimum(int axisNum, double value) {
		ds.setChannelMinimum(axisNum, value);
	}

	@Override
	public void setColorTable(ColorTable table, int tableNum) {
		ds.setColorTable(table, tableNum);
	}

	@Override
	public void setCompositeChannelCount(int count) {
		ds.setCompositeChannelCount(count);
	}

	@Override
	public void setValidBits(int count) {
		ds.setValidBits(count);
	}

	@Override
	public boolean isDirty() {
		return ds.isDirty();
	}

	@Override
	public void setDirty(boolean value) {
		ds.setDirty(value);
	}

	@Override
	public ImgPlus<? extends RealType<?>> getImgPlus() {
		return ds.getImgPlus();
	}

	@Override
	public <U extends RealType<U>> ImgPlus<U> typedImg(U type) {
		TypedDataset<U> typedDataset = asType(ds, type);
		if (typedDataset == null) return null;
		return (ImgPlus<U>) typedDataset.getImgPlus();
	}

	@Override
	public void setImgPlus(ImgPlus<? extends RealType<?>> imgPlus) {
		// TODO - think about this
		throw new UnsupportedOperationException("what to do here?");
	}

	@Override
	public Object getPlane(int planeNumber) {
		return ds.getPlane(planeNumber);
	}

	@Override
	public Object getPlane(int planeNumber, boolean copyOK) {
		return ds.getPlane(planeNumber, copyOK);
	}

	@Override
	public boolean setPlane(int planeNum, Object newPlane) {
		return ds.setPlane(planeNum, newPlane);
	}

	@Override
	public boolean setPlaneSilently(int planeNum, Object newPlane) {
		return ds.setPlaneSilently(planeNum, newPlane);
	}

	@Override
	public RealType<?> getType() {
		return ds.getType();
	}

	@Override
	public boolean isSigned() {
		return ds.isSigned();
	}

	@Override
	public boolean isInteger() {
		return ds.isInteger();
	}

	@Override
	public String getTypeLabelShort() {
		return ds.getTypeLabelShort();
	}

	@Override
	public String getTypeLabelLong() {
		return ds.getTypeLabelLong();
	}

	@Override
	public Dataset duplicate() {
		// TODO : do something more?
		return ds.duplicate();
	}

	@Override
	public Dataset duplicateBlank() {
		// TODO : do something more?
		return ds.duplicateBlank();
	}

	@Override
	public void copyInto(Dataset target) {
		ds.copyInto(target);
	}

	@Override
	public void setRGBMerged(boolean rgbMerged) {
		ds.setRGBMerged(rgbMerged);
	}

	@Override
	public boolean isRGBMerged() {
		return ds.isRGBMerged();
	}

	@Override
	public void copyDataFrom(Dataset other) {
		ds.copyDataFrom(other);
	}

	@Override
	public double getBytesOfInfo() {
		return ds.getBytesOfInfo();
	}

	@Override
	public void setAxes(AxisType[] axes) {
		ds.setAxes(axes);
	}

	// TODO - the four remaining methods are event oriented. Our event code
	// should determine if the changed Dataset is owned by a TypedDataset.
	// This somewhat complicates when to react to XChangedEvents but it is
	// feasible.
	
	@Override
	public void update() {
		ds.update();
	}

	@Override
	public void rebuild() {
		ds.rebuild();
	}
	
	@Override
	public void typeChange() {
		ds.typeChange();
	}

	@Override
	public void rgbChange() {
		ds.rgbChange();
	}

}
