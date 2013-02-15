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

package imagej.data.overlay;


import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import net.imglib2.meta.Axes;
import net.imglib2.roi.GeneralPathRegionOfInterest;
import net.imglib2.roi.GeneralPathSegmentHandler;

import org.scijava.Context;

/**
 * An overlay for the {@link GeneralPathRegionOfInterest}.
 * 
 * The {@link GeneralPathRegionOfInterest} class implements composite regions
 * of interest, consisting of areas defined by closed Beziér curves combined
 * via the even/odd winding rule.
 * 
 * @author Johannes Schindelin
 * @author Lee Kamentsky
 */
public class GeneralPathOverlay extends
	AbstractROIOverlay<GeneralPathRegionOfInterest>
{

	public GeneralPathOverlay(final Context context) {
		super(context, new GeneralPathRegionOfInterest());
		setAxis(Axes.X, Axes.X.ordinal());
		setAxis(Axes.Y, Axes.Y.ordinal());
	}

	private static final long serialVersionUID = 1L;

	@SuppressWarnings("serial")
	private class WrappedIOException extends RuntimeException {
		final IOException ioException;

		public WrappedIOException(final IOException exception) {
			ioException = exception;
		}

		public IOException getIOException() {
			return ioException;
		}
	}

	@Override
	public void setRegionOfInterest(GeneralPathRegionOfInterest roi) {
		super.setRegionOfInterest(roi);
	}

	@Override
	public void writeExternal(final ObjectOutput out) throws IOException {
		super.writeExternal(out);
		final GeneralPathRegionOfInterest roi = getRegionOfInterest();
		try {
			roi.iteratePath(new GeneralPathSegmentHandler() {

				@Override
				public void moveTo(double x, double y) {
					try {
						out.writeChar('M');
						out.writeDouble(x);
						out.writeDouble(y);
					} catch (final IOException e) {
						throw new WrappedIOException(e);
					}
				}

				@Override
				public void lineTo(double x, double y) {
					try {
						out.writeChar('L');
						out.writeDouble(x);
						out.writeDouble(y);
					} catch (final IOException e) {
						throw new WrappedIOException(e);
					}
				}

				@Override
				public void quadTo(double x1, double y1, double x, double y) {
					try {
						out.writeChar('Q');
						out.writeDouble(x1);
						out.writeDouble(y1);
						out.writeDouble(x);
						out.writeDouble(y);
					} catch (final IOException e) {
						throw new WrappedIOException(e);
					}
				}

				@Override
				public void cubicTo(double x1, double y1, double x2, double y2, double x,
					double y)
				{
					try {
						out.writeChar('C');
						out.writeDouble(x1);
						out.writeDouble(y1);
						out.writeDouble(x2);
						out.writeDouble(y2);
						out.writeDouble(x);
						out.writeDouble(y);
					} catch (final IOException e) {
						throw new WrappedIOException(e);
					}
				}

				@Override
				public void close() {
					try {
						out.writeChar(')');
					} catch (final IOException e) {
						throw new WrappedIOException(e);
					}
				}
			});
		} catch (WrappedIOException e) {
			throw e.getIOException();
		}
		out.writeChar('.');
	}

	@Override
	public void readExternal(final ObjectInput in) throws IOException,
		ClassNotFoundException
	{
		super.readExternal(in);
		final GeneralPathRegionOfInterest roi = getRegionOfInterest();
		roi.reset();
		for (;;) {
			char type = in.readChar();
			switch (type) {
				case 'M':
					roi.moveTo(in.readDouble(), in.readDouble());
					break;
				case 'L':
					roi.lineTo(in.readDouble(), in.readDouble());
					break;
				case 'Q':
					roi.quadTo(in.readDouble(), in.readDouble(), in.readDouble(), in.readDouble());
					break;
				case 'C':
					roi.cubicTo(in.readDouble(), in.readDouble(), in.readDouble(), in.readDouble(), in.readDouble(), in.readDouble());
					break;
				case ')':
					roi.close();
					break;
				case '.':
					break;
			}
		}
	}

	@Override
	public void move(double[] deltas) {
		throw new RuntimeException("TODO");
	}

}
