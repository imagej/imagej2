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

package imagej.command;

import imagej.AbstractContextual;
import imagej.ImageJ;
import imagej.plugin.ServicePreprocessor;

/**
 * A command that knows its context. Its service parameters are
 * automatically populated at construction, to make it easier to use via Java
 * API calls (i.e., without invoking it via {@link CommandService#run}). This
 * improves compile-time safety of downstream code that calls the command.
 * <p>
 * Here is an example command execution using {@link CommandService#run}:
 * </p>
 * <pre>
 * Future&lt;CommandModule&lt;FindEdges&gt;&gt; future =
 *   commandService.run(findEdges.class, &quot;display&quot;, myDisplay);
 * CommandModule&lt;FindEdges&gt; module = future.get(); // block till complete
 * ImageDisplay outDisplay = (ImageDisplay) module.getOutput(&quot;display&quot;);
 * </pre>
 * <p>
 * Note that <code>FindEdges</code> also has two other inputs, a
 * <code>ImageDisplayService</code> and an <code>OverlayService</code>, which
 * get automatically populated by the {@link ServicePreprocessor}.
 * </p>
 * <p>
 * Here is the same command execution via direct Java calls:
 * </p>
 * <pre>
 * FindEdges findEdges = new FindEdges();
 * findEdges.setContext(context); // populates service parameters
 * findEdges.setDisplay(myDisplay);
 * findEdges.run(); // execute on the same thread
 * ImageDisplay outDisplay = findEdges.getDisplay();
 * </pre>
 * <p>
 * We believe the latter is more intuitive for most Java programmers, and so
 * encourage commands to extend this class and provide API to use them
 * directly.
 * </p>
 * <p>
 * That said, there are times when you cannot extend a particular class (usually
 * because you must extend a different class instead). In that case, you can
 * still implement the {@link Command} interface and end up with a
 * perfectly serviceable command. The consequence is only that other Java
 * programmers will not be able to use the latter paradigm above to invoke your
 * code in a fully compile-time-safe way.
 * 
 * @author Curtis Rueden
 */
public abstract class ContextCommand extends AbstractContextual implements
	Command
{

	// -- Contextual methods --

	@Override
	public void setContext(final ImageJ context) {
		super.setContext(context);

		// populate service parameters
		final CommandService commandService =
			context.getService(CommandService.class);
		if (commandService == null) {
			throw new IllegalArgumentException("Context has no command service");
		}
		commandService.populateServices(this);
	}

}
