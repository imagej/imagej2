//
// Output.java
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
