
package codeblocks;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import codeblocks.BlockConnector.PositionType;

import renderable.RenderableBlock;
import workspace.Workspace;
import workspace.WorkspaceEvent;

/**
 * <code>BlockStub</code> are a special form of blocks that provide a particular
 * reference to its "parent" block.  These references can set, get, or increment
 * the value of its "parent" block.  References may also get the value for a 
 * particular agent.  Finally, for a procedure block, its reference is a call
 * block, which executes the procedure.  
 * 
 * The parent instance for a set of stubs is not permanent.  The parent intance
 * may change if the original parent it removed and then a new one with the 
 * same parent name is added to the block canvas. BlockStub manages the mapping 
 * between stubs and their parent.
 */
public class BlockStub extends Block{
    
    /** STUB HASH MAPS
     * key: parentName + parentGenus 
     * 
     * Key includes both parentName and parentGenus because the names of two parents
     * may be the same if they are of different genii
     * blockids of parents are not used as a reference because parents and stubs are
     * connected by the parentName+parentGenus information not the blockID.  This 
     * connection is more apparent when the parent Block is removed/deleted.  The stubs 
     * become dangling references. These stubs are resolved when a new parent block is 
     * created with the previous parent's name.  
     * */
    private static HashMap<String, Long> parentNameToParentBlock = new HashMap<String, Long>();
    private static HashMap<String, ArrayList<Long>> parentNameToBlockStubs = new HashMap<String, ArrayList<Long>>();
    
    /**
     * Temporary mapping for parent type (caller plugs). 
     * TODO remove once BlockUtilities cloneBlock() is finished
     */
    private static Map<String, String> parentToPlugType = new HashMap<String, String>();
    
    //stub type string constants
    private static final String GETTER_STUB = "getter";
    private static final String SETTER_STUB = "setter";
    private static final String CALLER_STUB = "caller";
    private static final String AGENT_STUB = "agent";
    //this particular stub type is unique to Starlogo TNG - may choose to remove it
    private static final String INC_STUB = "inc";
    
    
    private String parentName;
    private final String parentGenus;
    
    private final String stubGenus;
    
    /**
     * mySocketToParentSocket maps the sockets of this stubs to the sockets of its parent
     * this mapping is used to help in the maintanence this stub's sockets with respect to its parent
     */
    //private HashMap<BlockConnector,BlockConnector> mySocketToParentSocket = new HashMap<BlockConnector, BlockConnector>();
    
    
    /**
     * Constructs a new <code>BlockStub</code> instance using the specified
     * genus name of its parent block, the block id of its parent, the block name of parent
     * and its stub genus.  The exact reference to the parent through the specified initParentID 
     * is needed, in addition to the other specified parameters, to completely construct a new block
     * stub.
     * @param initParentID the Long block ID of its initial parent
     * @param parentGenus the BlockGenus String name of its initial parent
     * @param parentName 
     * @param stubGenus
     */
    public BlockStub(Long initParentID, String parentGenus, String parentName, String stubGenus) {
        super(stubGenus);
        
        assert initParentID != Block.NULL : "Parent id of stub should not be null";
        
        this.parentGenus = parentGenus;
        this.parentName = parentName;
        this.stubGenus = stubGenus;
        
        //initial parent of this
        Block parent = Block.getBlock(initParentID);
        //has parent block label
        this.setBlockLabel(parent.getBlockLabel());
        //initialize stub properties based on stubGenus such as sockets, plugs, and labels
        //this initialization assumes that nothing is connected to the parent yet
        //note: instead of modifying the stub blocks currect sockets, we replace them with whole new ones
        //such that the initkind of the stub blocks connectors are the same as their parents
        if(stubGenus.startsWith(GETTER_STUB)){
            //set plug to be the single socket of parent or plug if parent has no sockets
            if(parent.getNumSockets() > 0)
                this.setPlug(parent.getSocketAt(0).getKind(), this.getPlug().getPositionType(), this.getPlugLabel(), this.getPlug().isLabelEditable(), Block.NULL);
            else 
                this.setPlug(parent.getPlugKind(), this.getPlug().getPositionType(), this.getPlugLabel(), this.getPlug().isLabelEditable(), Block.NULL);
            
        }else if(stubGenus.startsWith(SETTER_STUB)){
            BlockConnector mySoc = this.getSocketAt(0);
            //set socket type to be parent socket type or plug if parent has no sockets
            if(parent.getNumSockets() > 0)
                this.setSocketAt(0, parent.getSocketAt(0).getKind(), mySoc.getPositionType(), 
                        mySoc.getLabel(), mySoc.isLabelEditable(), mySoc.isExpandable(), mySoc.getBlockID());
            else
                this.setSocketAt(0, parent.getPlugKind(), mySoc.getPositionType(), 
                        mySoc.getLabel(), mySoc.isLabelEditable(), mySoc.isExpandable(), mySoc.getBlockID());
        }else if(stubGenus.startsWith(CALLER_STUB)){
            //if parent has socket block connected to its first socket or has multiple sockets (parent may have blocks connected to its other sockets)
            if(parent.getSocketAt(0).getBlockID() != Block.NULL || parent.getNumSockets() > 1){
                //retrieve sockets from parent and set sockets accordingly
                Iterator<BlockConnector> sockets = parent.getSockets().iterator();
                for(int i=0; sockets.hasNext(); i++){
                    BlockConnector socket = sockets.next();
                    //socket labels should correspond with the socket blocks of parent
                    if(socket.getBlockID() != Block.NULL)
                        addSocket(socket.getKind(), BlockConnector.PositionType.SINGLE, Block.getBlock(socket.getBlockID()).getBlockLabel(), false, false, Block.NULL);
                }
            }
            
            //TODO: remove the following once BlockUtilities.cloneBlock() is finished
            // If our parent already has a plug type, we want to update 
            // Note that we don't need to call renderables, since we are still
            // in the constructor
            String kind = parentToPlugType.get(parent.getBlockLabel() + parent.getGenusName());
            if (kind != null) {
                removeBeforeAndAfter();
                //TODO ria commented code relates to creating mirror plugs for caller stubs that have no sockets
                //if(this.getNumSockets() == 0){
                //	setPlug(kind, PositionType.MIRROR, "", false, Block.NULL);
                //} else {
                	setPlug(kind, PositionType.SINGLE, "", false, Block.NULL);
                //}
            }
            
        }else if(stubGenus.startsWith(AGENT_STUB)){
            //getter for specific who
            //set plug to be parent single socket kind or plug kind if parent has no sockets
            if(parent.getNumSockets() > 0)
                setPlug(parent.getSocketAt(0).getKind(), this.getPlug().getPositionType(), this.getPlugLabel(), this.getPlug().isLabelEditable(), this.getPlugBlockID());
            else
                setPlug(parent.getPlugKind(), this.getPlug().getPositionType(), this.getPlugLabel(), this.getPlug().isLabelEditable(), this.getPlugBlockID());
            
        }else if(stubGenus.startsWith(INC_STUB)){
            //only included for number variables
            //do nothing for now
        }
        
        //has  page label of parent if parent has page label
        this.setPageLabel(parent.getPageLabel());
        //add new stub to hashmaps
        //parent should have existed in hashmap before this stub was created
        //(look at main Block constructor)
        //thus no problem should occur with following line
        parentNameToBlockStubs.get(parentName+parentGenus).add(this.getBlockID());
        
    }
    
    /**
     * Constructs a new BlockStub instance.  This contructor is protected as it should only be called 
     * while Block loads its information from the save String
     * @param blockID the Long block ID of this
     * @param stubGenus the BlockGenus of this
     * @param label the Block label of this
     * @param parentName the String name of its parent
     * @param parentGenus the String BlockGenus name of its parent
     */
    protected BlockStub(Long blockID, String stubGenus, String label, String parentName, String parentGenus){
        super(blockID, stubGenus, label, true);   //stubs may have stubs...
        //unlike the above constructor, the blockID specified should already
        //be referencing a fully loaded block with all necessary information
        //such as sockets, plugs, labels, etc.
        //the only information we need to handle is the stub information here.
        this.stubGenus = stubGenus;
        this.parentName = parentName;
        this.parentGenus = parentGenus;
        
        //there's a chance that the parent for this has not been added to parentNameToBlockStubs mapping
        String key = parentName + parentGenus;
        if(parentNameToBlockStubs.containsKey(key))
            parentNameToBlockStubs.get(parentName+parentGenus).add(this.getBlockID());
        else{
            ArrayList<Long> stubs = new ArrayList<Long>();
            stubs.add(this.getBlockID());
            parentNameToBlockStubs.put(key, stubs);
        }
    }
    
    /**
     * Clears all the mappings between parents and stubs.
     */
    public static void reset(){
        parentNameToBlockStubs.clear();
        parentNameToParentBlock.clear();
    }
    
    /**
     * Returns a list of the block ids of the specified parent's stubs
     * @param blockID
     */
    public static Iterable<Long> getStubsOfParent(Long blockID){
        ArrayList<Long> stubs = parentNameToBlockStubs.get(Block.getBlock(blockID).getBlockLabel()+Block.getBlock(blockID).getGenusName());
        if(stubs != null)
            return stubs;
        else
            return new ArrayList<Long>();
    }
    
    /**
     * Saves the parent block information with the specified blockID in the Stub Map
     * @param blockID
     */
    public static void putNewParentInStubMap(Long blockID){
        String key = Block.getBlock(blockID).getBlockLabel()+Block.getBlock(blockID).getGenusName();
        parentNameToParentBlock.put(key, blockID);
        
        if(parentNameToBlockStubs.get(key) == null)
            parentNameToBlockStubs.put(key, new ArrayList<Long>());
        
        //notify dangling stubs and update their renderables
        //dangling stubs will be waiting to have a parent assigned to them
        //and reflect that graphically
        for(Long stubID : parentNameToBlockStubs.get(key)){
            BlockStub stub = (BlockStub)Block.getBlock(stubID);
            stub.notifyRenderable();
        }
        
    }
    
    /**
     * Updates BlockStub hashmaps and the BlockStubs of the parent of its new name
     * @param oldParentName
     * @param newParentName
     * @param parentID
     */
    public static void parentNameChanged(String oldParentName, String newParentName, Long parentID){
        String oldKey = oldParentName + Block.getBlock(parentID).getGenusName();
        String newKey = newParentName + Block.getBlock(parentID).getGenusName();

        //only update if parents name really did "change" meaning the new parent name is 
        //different from the old parent name
        if(!oldKey.equals(newKey)){
            parentNameToParentBlock.put(newKey, parentID);

            //update the parent name of each stub 
            ArrayList<Long> stubs = parentNameToBlockStubs.get(oldKey);
            for(Long stub : stubs){
                BlockStub blockStub = ((BlockStub)Block.getBlock(stub));
                blockStub.parentName = newParentName;
                //update block label of each
                blockStub.setBlockLabel(newParentName);
                blockStub.notifyRenderable();
            }
            
            //check if any stubs already exist for new key
            ArrayList<Long> existingStubs = parentNameToBlockStubs.get(newKey);
            if(existingStubs != null)
                stubs.addAll(existingStubs);
        
            parentNameToBlockStubs.put(newKey, stubs);
        
            //remove old parent name from hash maps
            parentNameToParentBlock.remove(oldKey);
            parentNameToBlockStubs.remove(oldKey);
        }
    }
    
    /**
     * Updates the BlockStubs associated with the parent of its new page label
     * @param newPageLabel
     * @param parentID
     */
    public static void parentPageLabelChanged(String newPageLabel, Long parentID){
        String key = Block.getBlock(parentID).getBlockLabel() + Block.getBlock(parentID).getGenusName();
        
        //update each stub 
        ArrayList<Long> stubs = parentNameToBlockStubs.get(key);
        for(Long stub : stubs){
            BlockStub blockStub = ((BlockStub)Block.getBlock(stub));
            blockStub.setPageLabel(newPageLabel);
            blockStub.notifyRenderable();
        }
        
    }
    
    /**
     * Updates the BlocksStubs associated with the parent of its new page label
     * @param parentID
     */
    public static void parentConnectorsChanged(Long parentID){
        String key = Block.getBlock(parentID).getBlockLabel() + Block.getBlock(parentID).getGenusName();
        
        //update each stub only if stub is a caller (as callers are the only type of stubs that 
        //can change its connectors after being created)
        ArrayList<Long> stubs = parentNameToBlockStubs.get(key);
        for(Long stub : stubs){
            BlockStub blockStub = ((BlockStub)Block.getBlock(stub));
            if(blockStub.stubGenus.startsWith(CALLER_STUB)){
                blockStub.updateConnectors();
                //System.out.println("updated connectors of: "+blockStub);
                blockStub.notifyRenderable();
            }
        }
    }
    
    /**
     * Updates the plug on caller stubs associated with the given parent.
     * @param kind the new plug kind that callers should set
     */
    public static void parentPlugChanged(Long parentID, String kind) {
        String key = Block.getBlock(parentID).getBlockLabel() + Block.getBlock(parentID).getGenusName();
        
        // Update our type mapping.
        if (kind == null) 
            parentToPlugType.remove(key);
        else
            parentToPlugType.put(key, kind);
        
        // update each stub only if stub is a caller 
        ArrayList<Long> stubs = parentNameToBlockStubs.get(key);
        for(Long stub : stubs){
            BlockStub blockStub = ((BlockStub)Block.getBlock(stub));
            if(blockStub.stubGenus.startsWith(CALLER_STUB)){
                if (kind == null)
                    blockStub.restoreInitConnectors();
                else
                    blockStub.updatePlug(kind);
            }
        }
    }
    
    ////////////////////////////////////
    // PARENT INFORMATION AND METHODS //
    ////////////////////////////////////
    
    /**
     * Returns the parent name of this stub
     * @return the parent name of this stub
     */
    public String getParentName(){
        return parentName;
    }
    
    /**
     * Returns the parent block of this stub
     * @return the parent block of this stub
     */
    public Block getParent(){
        String key = parentName+parentGenus;
        if(!parentNameToParentBlock.containsKey(key))
            return null;
        return Block.getBlock(parentNameToParentBlock.get(key));
    }

    /**
     * Returns the parent block genus of this stub
     * @return the parent block genus of this stub
     */
    public String getParentGenus(){
        return parentGenus;
    }

    /**
     *
     */
    public boolean doesParentExist(){
        //TODO ria: needs to check BlockCanvas if parent is "alive"
        
        return true;
    }
    
    ///////////////////////////////////
    // METHODS OVERRIDDEN FROM BLOCK //
    ///////////////////////////////////
    
    /**
     * Overriden from Block.  Can not change the genus of a Stub.
     */
    public void changeGenusTo(String genusName){
        //return null;
    }
    
    //////////////////////////////////////////////////
    //BLOCK STUB CONNECTION INFORMATION AND METHODS //
    //////////////////////////////////////////////////
    
    /**
     * Updates the conenctors of this stub according to its parent.
     * For now only caller stubs should update their connector information after 
     * being created.  
     */
    private void updateConnectors(){
        Block parent = getParent();
        if(parent != null) {
	        //retrieve sockets from parent and set sockets accordingly
	        Iterator<BlockConnector> parentSockets = parent.getSockets().iterator();
	        int i; //socket index
	        //clear all sockets TODO temporary solution
	        for(BlockConnector socket : getSockets())
	            removeSocket(socket);
	        //add parent sockets
	        for(i=0; parentSockets.hasNext(); i++){
	            BlockConnector parentSocket = parentSockets.next();
	            if(parentSocket.getBlockID() != Block.NULL){
	                //may need to add more sockets if parent has > 1 sockets
	                if(i>this.getNumSockets()-1){
	                    //socket labels should correspond with the socket blocks of parent
	                    if(parentSocket.getBlockID() != Block.NULL)
	                        addSocket(parentSocket.getKind(), BlockConnector.PositionType.SINGLE, Block.getBlock(parentSocket.getBlockID()).getBlockLabel(), false, false, Block.NULL);
	                }else{
	                    BlockConnector con = getSocketAt(i);
	                    this.setSocketAt(i, parentSocket.getKind(), con.getPositionType(), Block.getBlock(parentSocket.getBlockID()).getBlockLabel(), con.isLabelEditable(),
	                            con.isExpandable(), con.getBlockID());
	                    //TODO ria remove this eventually
	                    //BlockConnector con = getSocketAt(i);
	                    //con.setKind(parentSocket.getKind());
	                    //con.setLabel(Block.getBlock(parentSocket.getBlockID()).getBlockLabel());
	                }
	            }
	        }
	        //TODO ria commented code relates to creating mirror plugs for caller stubs that have no sockets
	        /*if(stubGenus.startsWith(CALLER_STUB)){
	        	if(this.getNumSockets() > 0) {
	        		if(this.hasPlug() &&
	        			this.getPlug().getPositionType().equals(BlockConnector.PositionType.MIRROR)) {
	        			this.getPlug().setPositionType(PositionType.SINGLE);
	        		}
	        	} else {
	        		if(this.hasPlug() &&
		        			this.getPlug().getPositionType().equals(BlockConnector.PositionType.SINGLE)) {
		        			this.getPlug().setPositionType(PositionType.MIRROR);
		        		}
	        	}
	        }*/
        }
    }
    
    /**
     * Restores the initial state of the before, after, and plug. Disconnects
     * any invalid blocks. Only caller stubs should use this method.
     */
    private void restoreInitConnectors() {
        if (!hasPlug()) return;     // Already in original state
       
        // We have to check for a plug connector.
        Long id = getPlugBlockID();
        if (id != null && !id.equals(Block.NULL))
            disconnectBlock(id);

        // Always synchronize! We can't have both a plug and a before.
        removePlug();
        resetBeforeAndAfter();
        RenderableBlock.getRenderableBlock(getBlockID()).updateConnectors();
        notifyRenderable();
    }
    
    /**
     * Updates the plug type. Disconnects any invalid blocks. Only caller
     * stubs should use this method.
     * @param kind must not be null
     */
    private void updatePlug(String kind) {
        if (hasPlug() && getPlugKind().equals(kind)) return;
       
        // We have to check for a before and after block.
        Long id = getBeforeBlockID();
        if (id != null && !id.equals(Block.NULL))
            disconnectBlock(id);
        
        id = getAfterBlockID();
        if (id != null && !id.equals(Block.NULL)) 
            disconnectBlock(id);
        
        // We also need to check the plug, because it may be connected to
        // the wrong type.
        id = getPlugBlockID();
        if (id != null && !id.equals(Block.NULL))
            disconnectBlock(id);

        // Always synchronize! We can't have both a plug and a before.
        removeBeforeAndAfter();
        //TODO ria commented code relates to creating mirror plugs for caller stubs that have no sockets
        /*if (this.getNumSockets() == 0) {
        	setPlug(kind, PositionType.MIRROR, kind, false, Block.NULL);
        } else {*/
        	setPlug(kind, PositionType.SINGLE, kind, false, Block.NULL);
        //}
        RenderableBlock.getRenderableBlock(getBlockID()).updateConnectors();
        notifyRenderable();
    }
    
    /**
     * Disconnect the given block from us. Must have a valid id.
     */
    private void disconnectBlock(Long id) {
        Block b2 = Block.getBlock(id);
        BlockConnector conn2 = b2.getConnectorTo(getBlockID());
        BlockConnector conn = getConnectorTo(id);
        BlockLink link = BlockLink.getBlockLink(this, b2, conn, conn2);
        RenderableBlock rb = RenderableBlock.getRenderableBlock(link.getSocketBlockID());
        link.disconnect();
        rb.blockDisconnected(link.getSocket());
        Workspace.getInstance().notifyListeners(
                new WorkspaceEvent(rb.getParentWidget(), link, WorkspaceEvent.BLOCKS_DISCONNECTED));

    }
    
    ////////////////////////////////////////
    // METHODS FROM BLOCK GENUS           //
    ////////////////////////////////////////
    
    /**
     * Returns the Color of this; May return Color.Black if color was unspecified.
     * @return the Color of this; May return Color.Black if color was unspecified.
     */
    public Color getColor(){ 
        if(getParent() == null)
            return super.getColor();
        return getParent().getColor();
    }
    
    
    /**
     * @returns current information about block
     */
    public String toString() {
            return "Block Stub +"+ getBlockID()+": "+ getBlockLabel() + " with sockets: " + getSockets() + " and plug: " + getPlug();
    }
    
    @Override
    public boolean isCommandBlock(){
        return hasAfterConnector() && hasBeforeConnector();
    }
    
    @Override
    public boolean isDataBlock(){
        return !hasAfterConnector() && !hasBeforeConnector();
    }
    
    
    @Override
    public boolean isFunctionBlock(){
        return hasPlug() && (this.getNumSockets() > 0);
    }
    
    ////////////////////////
    // SAVING AND LOADING //
    ////////////////////////
    
    public String getSaveString(int x, int y, String commentSaveString, boolean collapsed){
        StringBuffer buf = new StringBuffer();
        buf.append("<BlockStub>");
        buf.append("<StubParentName>");
        buf.append(parentName);
        buf.append("</StubParentName>");
        buf.append("<StubParentGenus>");
        buf.append(parentGenus);
        buf.append("</StubParentGenus>");
        buf.append(super.getSaveString(x, y, commentSaveString,  collapsed));
        buf.append("</BlockStub>");
        return buf.toString();
    }
}
