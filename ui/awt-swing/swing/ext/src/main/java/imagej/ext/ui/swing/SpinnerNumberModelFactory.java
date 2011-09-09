//
// SpinnerNumberModelFactory.java
//

/*
ImageJ software for multidimensional image processing and analysis.

Copyright (c) 2010, ImageJDev.org.
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the names of the ImageJDev.org developers nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
POSSIBILITY OF SUCH DAMAGE.
*/

package imagej.ext.ui.swing;

import imagej.util.NumberUtils;

import java.math.BigDecimal;
import java.math.BigInteger;

import javax.swing.SpinnerNumberModel;

/**
 * Factory for producing the correct {@link SpinnerNumberModel} subclass based
 * on the type of {@link Number}.
 * 
 * @author Curtis Rueden
 * @see SpinnerNumberModel
 * @see SpinnerBigDecimalModel
 * @see SpinnerBigIntegerModel
 */
public class SpinnerNumberModelFactory {

	public SpinnerNumberModel createModel(final Number value, final Number min,
		final Number max, final Number stepSize)
	{
		final Class<?> c = value.getClass();
		if (BigInteger.class.isAssignableFrom(c)) {
			final BigInteger biValue = (BigInteger) value;
			final BigInteger biMin = (BigInteger) min;
			final BigInteger biMax = (BigInteger) max;
			final BigInteger biStepSize = (BigInteger) stepSize;
			return new SpinnerBigIntegerModel(biValue, biMin, biMax, biStepSize);
		}
		if (BigDecimal.class.isAssignableFrom(c)) {
			final BigDecimal bdValue = (BigDecimal) value;
			final BigDecimal bdMin = (BigDecimal) min;
			final BigDecimal bdMax = (BigDecimal) max;
			final BigDecimal bdStepSize = (BigDecimal) stepSize;
			return new SpinnerBigDecimalModel(bdValue, bdMin, bdMax, bdStepSize);
		}

		@SuppressWarnings("unchecked")
		final Comparable<Number> cMin = (Comparable<Number>) min;
		@SuppressWarnings("unchecked")
		final Comparable<Number> cMax = (Comparable<Number>) max;

		final Number clampedValue =
			NumberUtils.clampToRange(value.getClass(), value, min, max);
		return new SpinnerNumberModel(clampedValue, cMin, cMax, stepSize);
	}

}
