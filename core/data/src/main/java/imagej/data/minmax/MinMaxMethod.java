/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2014 Board of Regents of the University of
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

package imagej.data.minmax;

import imagej.ImageJPlugin;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.type.Type;

import org.scijava.plugin.RichPlugin;
import org.scijava.plugin.SingletonPlugin;

/**
 * The MinMaxMethod is used to find the minimum and maximum values of an
 * interval.
 * <p>
 * NB: An {@link #initialize} method must be called before performing any other
 * operations.
 * </p>
 * 
 * @author Mark Hiner
 */
public interface MinMaxMethod<T extends Type<T> & Comparable<T>> extends
	ImageJPlugin, RichPlugin, SingletonPlugin
{

  /**
   * Sets the provided Img as the target interval for computation,
   * as well as initial min and max values.
   */
  void initialize(final Img<T> img, final T min, final T max );

  /**
   * Sets the target interval for computation, as well as initial
   * min and max values.
   */
  void initialize(final IterableInterval<T> interval, final T min, final T max );

  /**
   * Sets the target interval for computation, as well as initial
   * min and max values.
   */
  void initialize(final RandomAccessibleInterval<T> interval, final T min, final T max );

  /**
   * Sets the provided Img as the target interval for computation.
   */
  void initialize(final Img<T> img );

  /**
   * Sets the target interval for computation.
   */
  void initialize(final IterableInterval<T> interval);

  /**
   * Sets the target interval for computation.
   */
  void initialize(final RandomAccessibleInterval<T> interval);
  
  /**
   * @return current discovered minimum value
   */
  T getMin();

  /**
   * @return current discovered maximum value
   */
  T getMax();

  /**
   * Begins searching the target interval for minimum and
   * maximum values.
   * 
   * @return true if successful. If false, see {@link #getErrorMessage()}
   */
  boolean process();

  /**
   * Verifies method inputs
   */
  boolean checkInput();

  /**
   * @return time taken by process method
   */
  long getProcessingTime();

  /**
   * Sets number of threads to default
   */
  void setNumThreads();

  /**
   * Sets number of threads to use
   */
  void setNumThreads(int numThreads);

  /**
   * @return number of threads used by this method
   */
  int getNumThreads();

  /**
   * @return Error message of failure during processing.
   */
  String getErrorMessage();
}
