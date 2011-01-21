package imagej.workflowpipes.controller;

import org.json.JSONObject;

import imagej.workflowpipes.pipesentity.Definition;

public class ImageJJSONProcessor implements DefinitionProcessor {

	@Override
	public Object execute( Definition definition ) {
		
		definition.getJSONObject();
		
		return new Object();
	}

}
