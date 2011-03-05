//
// Terminal.java
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

package imagej.workflowpipes.pipesentity;

import java.io.Serializable;

public class Terminal implements Serializable {
	
	//populate with defaults
	private String typeKey;
	private String typeValue;
	private String directionKey = "name";
	
	// is either "_INPUT" or "_OUTPUT"
	private String directionValue; 
	// TODO fix the variable names to input/output type : "name": instance name
	public static Terminal getInputTerminal( String terminalConnectorType, String connectorName )
	{
		Terminal terminal = new Terminal();
		terminal.directionValue = connectorName;
		terminal.typeKey = "input";
		
		//assign type value
		terminal.typeValue = terminalConnectorType;
		
		return terminal;
	}
	
	public static Terminal getOutputTerminal ( String terminalConnectorType, String connectorName )
	{
		Terminal terminal = new Terminal();
		terminal.directionValue = connectorName;
		terminal.typeKey = "output";
		
		//assign type value
		terminal.typeValue = terminalConnectorType;
		
		return terminal;
	}
	
	public String getTypeKey() {
		return this.typeKey;
	}
	
	public String getTypeValue() {
		return this.typeValue;
	}

	public String getDirectionKey() {
		return directionKey;
	}

	public String getDirectionValue() {
		return directionValue;
	}
}
