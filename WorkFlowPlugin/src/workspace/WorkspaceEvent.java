package workspace;

import codeblocks.Block;
import codeblocks.BlockLink;

public class WorkspaceEvent {

    //workspace-wide events
    //affects layout and content of workspace and at least two or more blocks
    public final static int PAGE_ADDED = 1;
    public final static int PAGE_REMOVED = 2;
    public final static int BLOCK_ADDED = 3;
    public final static int BLOCK_REMOVED = 4;
    public final static int BLOCKS_CONNECTED = 5;
    public final static int BLOCKS_DISCONNECTED = 6;
    public final static int BLOCK_STACK_COMPILED = 7;
    
    //page specific events
    public final static int PAGE_RENAMED = 8;
    public final static int PAGE_RESIZED = 9;
    
    //block specific events
    public final static int BLOCK_RENAMED = 10;
    public final static int BLOCK_MOVED = 11;
    public final static int BLOCK_GENUS_CHANGED = 12;

    public final static int BLOCK_COMMENT_ADDED = 13;
    public final static int BLOCK_COMMENT_REMOVED = 14;
    public final static int BLOCK_COMMENT_MOVED = 15;
    public final static int BLOCK_COMMENT_RESIZED= 16;
    public final static int BLOCK_COMMENT_VISBILITY_CHANGE = 17;
    public final static int BLOCK_COMMENT_CHANGED= 18;
    
    //workspace specific event
    public final static int WORKSPACE_FINISHED_LOADING = 100;
    
    private Long blockID = Block.NULL;
    private int eventType;
    private WorkspaceWidget widget = null;
    private BlockLink link = null;
    private String oldWidgetName = null;
    
    //If this is a user spawned event or not
    private boolean userSpawned = false;
    
    /**
     * Constructs a new WorkspaceEvent.  This constructor should be used to report
     * page added, removed events.  The WorkspaceWidget page parameter should
     * be an instance of Page.
     * @param page
     * @param eventType
     */
    public WorkspaceEvent(WorkspaceWidget page,int eventType){
        this.widget = page;
        this.eventType = eventType;
        this.blockID = Block.NULL;
    }
    
    public WorkspaceEvent(WorkspaceWidget page,int eventType, boolean userSpawned){
    	this.widget = page;
        this.eventType = eventType;
        this.blockID = Block.NULL;
        this.userSpawned = userSpawned;        
    }
    
    /**
     * Constructs a new WorkspaceEvent.  This constructor should be used to report
     * page renamed events.  The WorkspaceWidget page parameter should
     * be an instance of Page.
     * @param page
     * @param oldName the old String name of this page
     * @param eventType
     */
    public WorkspaceEvent(WorkspaceWidget page, String oldName, int eventType){
        this.widget = page;
        this.eventType = eventType;
        this.blockID = Block.NULL;
        this.oldWidgetName = oldName;
    }
    
    public WorkspaceEvent(WorkspaceWidget page, String oldName, int eventType, boolean userSpawned){
        this.widget = page;
        this.eventType = eventType;
        this.blockID = Block.NULL;
        this.oldWidgetName = oldName;
        this.userSpawned = userSpawned;
    }
    
    /**
     * Constructs a new WorkspaceEvent.  This constructor should be used to report
     * the following: block added, removed, renamed, compiled, moved.
     * @param widget
     * @param blockID
     * @param eventType
     */
    public WorkspaceEvent(WorkspaceWidget widget, Long blockID, int eventType){
        this.widget = widget;
        this.eventType = eventType;
        this.blockID = blockID;
    }
    
    public WorkspaceEvent(WorkspaceWidget widget, Long blockID, int eventType, boolean userSpawned){
        this.widget = widget;
        this.eventType = eventType;
        this.blockID = blockID;
        this.userSpawned = userSpawned;
    }
    
    /**
     * Constructs a new WorkspaceEvent.  This constructor should be used to report
     * block connected/disconnected events.  The specified link contains the connection 
     * information.
     * @param widget
     * @param link
     * @param eventType
     */
    public WorkspaceEvent(WorkspaceWidget widget, BlockLink link, int eventType){
        this.widget = widget;
        this.link = link;
        this.eventType = eventType;
        this.blockID = Block.NULL;
    }
    
    public WorkspaceEvent(WorkspaceWidget widget, BlockLink link, int eventType, boolean userSpawned){
        this.widget = widget;
        this.link = link;
        this.eventType = eventType;
        this.userSpawned = userSpawned;
    }
       
    /**
     * Tells if this event is a user spawned event or not
     * @return true if this event was spawned by a user
     */
    public boolean isUserEvent()
    {
    	return userSpawned;
    }
    
    /**
     * Returns the WorkspaceWidget where this event occured. 
     * @return the WorkspaceWidget where this event occured.
     */
    public WorkspaceWidget getSourceWidget(){
        return widget;
    }
    /**
     * Returns the Long ID of the Block where this event occured.  For 
     * block connection events, this id is Block.NULL since the event occurred
     * from two blocks.
     */
    public Long getSourceBlockID(){
        return blockID;
    }
    /**
     * Returns the int event type of this
     * @return the int event type of this
     */
    public int getEventType(){
        return eventType;
    }
    
    /**
     * Returns the BlockLink where this event originated, or null if the event type
     * of this is not block connected or disconnected.
     * @return the BlockLink where this event originated, or null if the event type
     * of this is not block connected or disconnected.
     */
    public BlockLink getSourceLink(){
        return link;
    }
    
    /**
     * Returns the original name of the source widget; null if the source widget's 
     * name did not change.
     * @return the original name of the source widget; null if the source widget's 
     * name did not change.
     */
    public String getOldNameOfSourceWidget(){
        return oldWidgetName;
    }
    
    public String toString() {
        switch (eventType) {
        case PAGE_ADDED:
            return "WorkspaceEvent(PAGE_ADDED: " + widget + ")";
        case PAGE_REMOVED:
            return "WorkspaceEvent(PAGE_REMOVED: " + widget +")";
        case BLOCK_ADDED:
            return "WorkspaceEvent(BLOCK_ADDED: " + Block.getBlock(blockID) + ")";
        case BLOCK_REMOVED:
            return "WorkspaceEvent(BLOCK_REMOVED: " + Block.getBlock(blockID) + ")";
        case BLOCKS_CONNECTED:
            return "WorkspaceEvent(BLOCKS_CONNECTED: " + link + ")";
        case BLOCKS_DISCONNECTED:
            return "WorkspaceEvent(BLOCKS_DISCONNECTED: " + link + ")";
        case BLOCK_STACK_COMPILED:
            return "WorkspaceEvent(BLOCK_STACK_COMPILED: " + Block.getBlock(blockID) + ")";
        case PAGE_RENAMED:
            return "WorkspaceEvent(PAGE_RENAMED: " + widget + ")";
        case PAGE_RESIZED:
            return "WorkspaceEvent(PAGE_RESIZED: " + widget + ")";
        case BLOCK_RENAMED:
            return "WorkspaceEvent(BLOCK_RENAMED: " + Block.getBlock(blockID) + ")";
        case BLOCK_MOVED:
            if (link == null)
                return "WorkspaceEvent(BLOCK_MOVED: " + Block.getBlock(blockID) + ")";
            else
                return "WorkspaceEvent(BLOCK_MOVED: " + link + ")";
        case BLOCK_GENUS_CHANGED:
            return "WorkspaceEvent(BLOCK_GENUS_CHANGED: " + Block.getBlock(blockID) + ")";
        case WORKSPACE_FINISHED_LOADING:
            return "WorkspaceEvent(WORKSPACE_FINISHED_LOADING)";
        default:
            return "WorkspaceEvent(" + eventType + ")";    
        }
    }
    
}
