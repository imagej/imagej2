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

package imagej.data.commands;

import imagej.command.Command;
import imagej.data.display.DatasetView;
import net.imglib2.RandomAccessibleInterval;

/**
 * Interface marking commands that can be toggled to run on the current plane,
 * instead of a complete image.
 * 
 * @author Mark Hiner hinerm at gmail.com
 *
 */
public interface PlanarCommand extends Command {
  
  
  /**
   * As {@link #run()}, but operating only on the specified plane.
   * 
   * @param plane the current plane.
   */
  void run(RandomAccessibleInterval<?> plane);
  
  // -- Parameter Accessors and Setters --
  
  /**
   * @return The view this command will operate on.
   */
  DatasetView getView();
  
  /**
   * @return true if this command will restrict operation to the current plane of
   * its view.
   */
  Boolean isPlanar();
  
  /**
   * @param view - The view this command will operate on.
   */
  void setView(DatasetView view);
  
  /**
   * @param planar - Whether or not to operate on the current plane only.
   */
  void setPlanar(Boolean planar);
}
