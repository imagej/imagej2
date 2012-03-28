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

package imagej.workflowpipes.modules;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import imagej.workflow.debug.PreviewInfo;

import imagej.workflowpipes.pipesapi.Module;
import imagej.workflowpipes.pipesentity.Description;
import imagej.workflowpipes.pipesentity.Error;
import imagej.workflowpipes.pipesentity.ID;
import imagej.workflowpipes.pipesentity.Name;
import imagej.workflowpipes.pipesentity.Tag;
import imagej.workflowpipes.pipesentity.Terminal;
import imagej.workflowpipes.pipesentity.Type;
import imagej.workflowpipes.pipesentity.UI;

/**
 * Represents the module type "OUTPUT"
 * @author rick
 *
 */
public class Output extends Module implements Serializable {

	private static final long serialVersionUID = -7994336091379467057L;

	public static Output getOutput() {
		
		Output output = new Output();
		
		output.id = new ID("_OUTPUT");
		
		output.terminals.add( Terminal.getInputTerminal("rss","_INPUT") );

		output.ui = new UI("");

		output.name = new Name("Pipe Output");

		output.type = new Type("output");

		output.description = new Description("The pipe output needs to be fed to this module");

		Tag tag = new Tag(null);
		output.tags = Tag.getTagsArray(tag);
		
		return output;
	}


	public void go( List<PreviewInfo> previewInfoList ) {
		
		//process input
		
		
	}

}
