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

package imagej.workflowpipes.pipesentity;

import java.io.Serializable;

/**
 * The Layout class maps the UI's javascript components to a Java class structure
 * @author rick
 *
 */
public class Layout implements Serializable {
    
	String layoutID;
	String layoutDefinition;
	String layoutName;
	String layoutDescription;
	String layoutTags;
	
	/**
	 * 
	 * @param layoutID - a unique id used to track a layout
	 * @param layoutDefinition - the JSON string representation of the layout
	 * @param layoutName - the name the user defines for the layout
	 * @param layoutDescription - the text description the user gives for the layout
	 * @param layoutTags - sequence of words given to aid in the discovery of this layout
	 */
	public Layout( String layoutID, String layoutDefinition, String layoutName, String layoutDescription, String layoutTags )
	{
		this.layoutID = layoutID;
		this.layoutDefinition = layoutDefinition;
		this.layoutName = layoutName;
		this.layoutDescription = layoutDescription;
		this.layoutTags = layoutTags;
	}
	
	public String getLayoutTags() {
		return this.layoutTags;
	}
	
	public String getLayoutDefinition() {
		return layoutDefinition;
	}

	public void setLayoutDefinition(String def) {
		this.layoutDefinition = def;
	}

	public String getLayoutName() {
		return layoutName;
	}

	public void setLayoutName(String name) {
		this.layoutName = name;
	}

	public String getLayoutDescription() {
		return layoutDescription;
	}
	
	public void setLayoutDescription( String layoutDescription ) {
		this.layoutDescription = layoutDescription;
	}

	public String getLayoutID()
	{
		return layoutID;
	}
	
	@Override
	public String toString() {
		return this.layoutName + " " + this.layoutID + " " + this.layoutDescription + this.layoutDefinition;
	}
}
