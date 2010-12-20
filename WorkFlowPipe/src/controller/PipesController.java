package controller;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Random;

import pipesentity.Layout;

public class PipesController {

	private HashMap<String, Layout> layoutArrayList;
	
	public PipesController( HashMap<String, Layout> layoutArrayList  )
	{
		this.layoutArrayList = layoutArrayList;
	}
	
	public String clonePipe( String parentID, String crumb ) 
	{
		String uniqueID = null;
		try {
			uniqueID = getUniqueID( parentID + crumb );
		} catch (NoSuchAlgorithmException e) {
			//TODO: add error handling
			e.printStackTrace();
		}
		
		//add the Layout
		layoutArrayList.put( uniqueID, new Layout( uniqueID, layoutArrayList.get( parentID ).getLayout(), layoutArrayList.get( parentID ).getName(), layoutArrayList.get( parentID ).getDesc() ) );
		
		return uniqueID;
	}
	
	public HashMap< String, Layout > getPipesArrayList()
	{
		return layoutArrayList;
	}

	/**
	 * 
	 * @param name - string based name of the layout
	 * @param def - layout
	 * @param id - id, stays the same and is returned if successful
	 * @param crumb - unique 
	 * @return
	 */
	public void updatePipe( String name, String def, String id, String crumb, String desc, String tags ) {
		layoutArrayList.remove( id );
		layoutArrayList.put( id, new Layout( id, def, desc, tags ));
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
		layoutArrayList.put( id, new Layout( id, def, "", "" ));
		return id;
	}
	
}
