//
// PipesController.java
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

package imagej.workflowpipes.controller;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONObject;

import imagej.workflowpipes.experimental.LocalDefEvaluator;
import imagej.workflowpipes.pipes.Service;
import imagej.workflowpipes.pipesapi.Module;
import imagej.workflowpipes.pipesentity.Conf;
import imagej.workflowpipes.pipesentity.Def;
import imagej.workflowpipes.pipesentity.ID;
import imagej.workflowpipes.pipesentity.Layout;
import imagej.workflowpipes.pipesentity.Type;

public class PipesController {

	private HashMap< Type, Layout > layoutArrayList;
	private HashMap< Service, Module > modulesServiceHashMap;
	
	public PipesController( HashMap<Type, Layout> layoutArrayList, HashMap<Service, Module> modulesServiceHashMap  )
	{
		// add the layouts
		this.layoutArrayList = layoutArrayList;
		
		// add the modules
		this.modulesServiceHashMap = modulesServiceHashMap;
	}
	
	public JSONObject clonePipe( String parentID, String crumb, JSONObject json ) 
	{
		String uniqueID = null;
		try {
			uniqueID = getUniqueID( parentID + crumb );
		} catch (NoSuchAlgorithmException e) {
			//return error status
			//TODO:add error logging
			json.put("ok", new Integer(0) );
			e.printStackTrace();
		}
		
		//add the Layout
		layoutArrayList.put( new Type( uniqueID ), new Layout( uniqueID, layoutArrayList.get( parentID ).getLayoutDefinition(), layoutArrayList.get( parentID ).getLayoutName(), layoutArrayList.get( parentID ).getLayoutDescription(), layoutArrayList.get( parentID ).getLayoutTags() ) );
		
		//set return value for new id
		json.put("new_id", uniqueID);
		
		//set return value for success
		json.put("ok", new Integer(1) );
		
		return json;
	}


	/**
	 * 
	 * @param name - string based name of the layout
	 * @param def - layout
	 * @param id - id, stays the same and is returned if successful
	 * @param crumb - unique 
	 * @return
	 */
	public void updatePipe( String layoutID, String layoutDefinition, String layoutName, String layoutDescription, String layoutTags ) {
		layoutArrayList.remove( layoutID );
		layoutArrayList.put( new Type( layoutID ), new Layout( layoutID, layoutDefinition, layoutName, layoutDescription, layoutTags ));
	}
	
	//Credit:http://www.xinotes.org/notes/note/370/
	public static String toHex(byte[] a) {
        StringBuilder sb = new StringBuilder(a.length * 2);
        for (int i = 0; i < a.length; i++) {
            sb.append(Character.forDigit((a[i] & 0xf0) >> 4, 16));
            sb.append(Character.forDigit(a[i] & 0x0f, 16));
        }
        return sb.toString();
    }


	private String getUniqueID( String key ) throws NoSuchAlgorithmException
	{
		//get the parent Pipe
		byte[] bytesOfMessage = ( key + System.currentTimeMillis() + System.nanoTime() + new Random().nextFloat() ).getBytes();

		MessageDigest md = MessageDigest.getInstance("MD5");
		return toHex( md.digest( bytesOfMessage ) );
	}

	
	public String insertPipe(  String layoutID, String layoutDefinition, String layoutName, String layoutDescription, String layoutTags, String crumb ) throws NoSuchAlgorithmException {
		String id = getUniqueID( layoutDefinition + layoutName + crumb );
		layoutArrayList.put( new Type( id ), new Layout( layoutID, layoutDefinition, layoutName, layoutDescription, layoutTags ) );
		return id;
	}

	public JSONObject savePipe( String layoutID, String layoutDefinition, String layoutName, String layoutDescription, String layoutTags, JSONObject json, String crumb ) {

		if ( layoutArrayList.containsKey( layoutID )  )
		{
			//update the existing pipe
			updatePipe( layoutID, layoutDefinition, layoutName, layoutDescription, layoutTags );
			
			//set return action type
			json.put("action", "update");
			
			//set id 
			json.put("id", layoutID );
			
			//set status OK
			json.put("ok", new Integer( 1 ));
			return json;
		}
		
		//insert a new pipe
		String newID = null;
		
		try {
			//insert the pipe
			newID = insertPipe( layoutID, layoutDefinition, layoutName, layoutDescription, layoutTags, crumb );
		} catch (NoSuchAlgorithmException e) {
			//set return status not ok
			json.put("ok", new Integer(0));
			
			e.printStackTrace();
		}
		
		//set return id
		json.put("id", newID );
		
		//set return action type
		json.put("action", "insert");
		
		//set return status to success
		json.put("ok", new Integer( 1 ));
		
		return json;
	}

	public JSONObject userUpdatewebpath( String path, String rnd, String out, JSONObject json, String crumb ) {
		
		//TODO: add directory switching (Domain switching support)
		//simulate failure
		
		json.put("message","Update failed: '"+path+"' is not yet supported ");
		
		//TODO:simulate success
		//"data":"http:\/\/localhost\/web\/person.info?guid=KLJRE3343"
		
		//set return status to success
		json.put("ok", new Integer( 1 ));
		
		return json;
	}

	/**
	 * This
	 * @param id the pipe id to be deleted
	 * @param json - the result of the status
	 * @param crumb - user's session information
	 * @return response object
	 */
	public JSONObject deletePipe(String id, JSONObject json, String crumb) {
	
		//TODO: add user filtering, crumb verification
		layoutArrayList.remove( id );
		
		//set return status to success
		json.put("ok", new Integer( 1 ));
		
		return json;
	}

	/**
	 * Generates the HTML page the user hits when they want to browse their pipes
	 * @param guid
	 * @return
	 */
	public String getLayoutsHTML(String guid) {
		
		return null;
	}

	public JSONObject evaluate( JSONObject definitionJSON ) 
	{	
		System.out.println("PipesController :: Do evaluation called ");
	
		//Try to get a Java def
		Def def = new Def( definitionJSON.getJSONArray("modules"), definitionJSON.getJSONArray("wires") );
			
		System.out.println("PipesController :: def created using " + definitionJSON.getJSONArray("modules") + " and " + definitionJSON.getJSONArray("wires") );
		
		//Use a local Def Evaluator	
		JSONObject result = LocalDefEvaluator.getPreview( def, modulesServiceHashMap );
			
		return result;
		
	}

	public HashMap<Service, Module> getModulesServiceHashMap() {
		return modulesServiceHashMap;
	}
	
}
