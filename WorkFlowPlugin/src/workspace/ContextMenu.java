package workspace;

import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import renderable.RenderableBlock;


/**
 * ContextMenu handles all the right-click menus within the Workspace.
 * TODO ria enable customization of what menu items appear, fire events depending
 * on what items are clicked (if we enabled the first feature)
 * 
 * TODO ria still haven't enabled the right click menu for blocks
 */
public class ContextMenu extends PopupMenu implements ActionListener{
	private static final long serialVersionUID = 328149080421L;

    //context menu renderableblocks plus
    //menu items for renderableblock context menu
    private static ContextMenu rndBlockMenu = new ContextMenu();
    private static ContextMenu addCommentMenu = new ContextMenu();
    private static MenuItem addCommentItem;
    private final static String ADD_COMMENT_BLOCK = "ADDCOMMENT";
    private static boolean addCommentMenuInit = false;
    private static ContextMenu removeCommentMenu = new ContextMenu();
    private static MenuItem removeCommentItem;
    private final static String REMOVE_COMMENT_BLOCK = "REMOVECOMMENT";
    private static boolean removeCommentMenuInit = false;
    
    //context menu for canvas plus
    //menu items for canvas context menu
    private static ContextMenu canvasMenu = new ContextMenu();
    private static MenuItem arrangeAllBlocks;
    private final static String ARRANGE_ALL_BLOCKS = "ARRANGE_ALL_BLOCKS";
    private static boolean canvasMenuInit = false;
    
    /** The JComponent that launched the context menu in the first place */
    private static Object activeComponent = null;
    
    //privatize the constructor
    private ContextMenu(){
    }
    
    /**
     * Initializes the context menu for adding Comments.
     */
    private static void initAddCommentMenu(){
    	addCommentItem = new MenuItem("Add Comment");
        addCommentItem.setActionCommand(ADD_COMMENT_BLOCK);
        addCommentItem.addActionListener(rndBlockMenu);
        addCommentMenu.add(addCommentItem);
        addCommentMenuInit = true;
    }
    
    /**
     * Initializes the context menu for deleting Comments.
     */
    private static void initRemoveCommentMenu(){
        
        removeCommentItem = new MenuItem("Delete Comment");
        removeCommentItem.setActionCommand(REMOVE_COMMENT_BLOCK);
        removeCommentItem.addActionListener(rndBlockMenu);        
        
        removeCommentMenu.add(removeCommentItem);
        //rndBlockMenu.add(runBlockItem);
        
        removeCommentMenuInit = true;
    }
    
    /**
     * Initializes the context menu for the BlockCanvas
     *
     */
    private static void initCanvasMenu(){
        arrangeAllBlocks = new MenuItem("Organize all blocks");  //TODO some workspaces don't have pages
        arrangeAllBlocks.setActionCommand(ARRANGE_ALL_BLOCKS);
        arrangeAllBlocks.addActionListener(canvasMenu);
        
        canvasMenu.add(arrangeAllBlocks);
        
        canvasMenuInit = true;
    }
    
    /**
     * Returns the right click context menu for the specified JComponent.  If there is 
     * none, returns null.
     * @param o JComponent object seeking context menu
     * @return the right click context menu for the specified JComponent.  If there is 
     * none, returns null.
     */
	public static PopupMenu getContextMenuFor(Object o){
        if(o instanceof RenderableBlock){
            if(((RenderableBlock)o).hasComment()){
            	if(!removeCommentMenuInit){initRemoveCommentMenu();}
                activeComponent = o;
                return removeCommentMenu;
            }else{
            	if(!addCommentMenuInit){initAddCommentMenu();}
                activeComponent = o;
                return addCommentMenu;
            }
        }else if(o instanceof BlockCanvas){
            if(!canvasMenuInit)
                initCanvasMenu();
            activeComponent = o;
            return canvasMenu;
        }
        return null;
    }

    public void actionPerformed(ActionEvent a) {
        if(a.getActionCommand() == ARRANGE_ALL_BLOCKS){
            //notify the component that launched the context menu in the first place
            if(activeComponent != null && activeComponent instanceof BlockCanvas){
                ((BlockCanvas)activeComponent).arrangeAllBlocks();
            }
        }else if(a.getActionCommand() == ADD_COMMENT_BLOCK){
        	//notify the renderableblock componenet that lauched the conetxt menu
        	if(activeComponent != null && activeComponent instanceof RenderableBlock){
        		((RenderableBlock)activeComponent).addComment();
        	}
        }else if(a.getActionCommand() == REMOVE_COMMENT_BLOCK){
        	//notify the renderableblock componenet that lauched the conetxt menu
        	if(activeComponent != null && activeComponent instanceof RenderableBlock){
        		((RenderableBlock)activeComponent).removeComment();
        	}
        }
    }
}
