package controller;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Random;

import pipesentity.Layout;

public class PipesController {

	private HashMap<String, Layout> pipesArrayList;
	
	public PipesController( HashMap<String, Layout> pipesArrayList  )
	{
		this.pipesArrayList = pipesArrayList;
	}
	
	public String clonePipe( String parentID, String crumb ) throws NoSuchAlgorithmException
	{
		String uniqueID = getUniqueID( parentID );
		
		//add the Layout
		pipesArrayList.put( uniqueID, new Layout( uniqueID, pipesArrayList.get( parentID ).getLayout() ) );
		
		return uniqueID;
	}
	
	public HashMap< String, Layout > getPipesArrayList()
	{
		return pipesArrayList;
	}

	/**
	 * 
	 * @param name - string based name of the layout
	 * @param def - layout
	 * @param id - id, stays the same and is returned if successful
	 * @param crumb - unique 
	 * @return
	 */
	public void updatePipe( String name, String def, String id, String crumb ) {
		pipesArrayList.remove( id );
		pipesArrayList.put( id, new Layout( id, def ));
	}

	private String getUniqueID( String key ) throws NoSuchAlgorithmException
	{
		//get the parent Pipe
		byte[] bytesOfMessage = (pipesArrayList.get( key ).toString() + System.currentTimeMillis() + System.nanoTime() + new Random().nextFloat() ).getBytes();

		MessageDigest md = MessageDigest.getInstance("MD5");
		byte[] theDigest = md.digest( bytesOfMessage );
		
		return theDigest.toString();
	}

	/**
	 * Update the local structure to reflect the addition of a new Layout
	 * @param name
	 * @param def
	 * @param crumb
	 * @return the new unique object id
	 * @throws NoSuchAlgorithmException
	 */
	public String insertPipe(String name, String def, String crumb) throws NoSuchAlgorithmException {
		String id = getUniqueID( def + name + crumb );
		pipesArrayList.put( id, new Layout( id, def ));
		return id;
	}
	
}
