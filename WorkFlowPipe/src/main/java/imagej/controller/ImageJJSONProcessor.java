package imagej.controller;

import org.json.JSONObject;

import imagej.pipesentity.Definition;

public class ImageJJSONProcessor implements DefinitionProcessor {

	@Override
	public Object execute( Definition definition ) {
		
		definition.getJSONObject();
		
		return new Object();
	}

}
