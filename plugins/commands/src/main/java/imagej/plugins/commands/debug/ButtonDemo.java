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

package imagej.plugins.commands.debug;

import imagej.command.Command;
import imagej.data.Dataset;
import imagej.plugins.tools.TunePlayer;
import imagej.widget.Button;
import net.imglib2.img.Img;
import net.imglib2.ops.img.ImageCombiner;
import net.imglib2.ops.operation.real.unary.RealAddConstant;
import net.imglib2.ops.operation.real.unary.RealSubtractConstant;

import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 * This command is example code that shows how one can make @Parameters that are
 * {@link Button}s which can fire callbacks when pressed.
 * 
 * @author Barry DeZonia
 */
@Plugin(type = Command.class, menuPath = "Plugins>Sandbox>Button Demo")
public class ButtonDemo implements Command {

	@Parameter
	private Dataset data;

	@Parameter(label = "Add 25", callback = "add")
	private Button add;

	@Parameter(label = "Subtract 25", callback = "subtract")
	private Button subtract;

	@Parameter(label = "Play Song", callback = "playSong")
	private Button playSong;

	@Override
	public void run() {
		// anything to do?? maybe not
	}

	// NOTE in real life you'd use the preview() capability of a PreviewCommand.
	// We just want to show that a Button can generate a callback that the command
	// can respond to.

	protected void add() {
		final RealAddConstant addConstantOp = new RealAddConstant(25);
		ImageCombiner.applyOp(addConstantOp, (Img) data.getImgPlus(), (Img) data
			.getImgPlus());
		data.update();
	}

	// NOTE in real life you'd use the preview() capability of a PreviewCommand.
	// We just want to show that a Button can generate a callback that the command
	// can respond to.

	protected void subtract() {
		final RealSubtractConstant subConstantOp = new RealSubtractConstant(25);
		ImageCombiner.applyOp(subConstantOp, (Img) data.getImgPlus(), (Img) data
			.getImgPlus());
		data.update();
	}

	protected void playSong() {
		new TunePlayer().play("T100 L32 A B C D E F G F E D C B A");
	}
}
