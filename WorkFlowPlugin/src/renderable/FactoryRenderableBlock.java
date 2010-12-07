package renderable;

import java.awt.Container;
import java.awt.event.MouseEvent;

import javax.swing.SwingUtilities;

import codeblocks.Block;
import codeblocks.JComponentDragHandler;


import workspace.WorkspaceWidget;

/**
 * FactoryRenderableBlock extends RenderableBlock and is used within FactoryBlockDrawers.
 * Unlike its superclass RenderableBlock, FactoryRenderableBlock does not move or 
 * connect to any blocks.  Instead it has one function only, to produce new RenderableBlocks
 * and their associated Block instances.  It's block labels are also uneditable.
 * 
 * When a mouse is pressed over a FactoryRenderableBlock, a new RenderableBlock instance is 
 * created on top of it to receive further mouse events and a new Block instance is created
 * in the background.  
 */
public class FactoryRenderableBlock extends RenderableBlock {
	
	private static final long serialVersionUID = 1L;
	
    //the RenderableBlock to produce
    private RenderableBlock createdRB = null;
    private boolean createdRB_dragged = false;
    
    //we have this instance of the dragHandler so that we can use it mouse entered
    //mouseexited methods to change the cursor appropriately, so that we can make it 
    //"seem" that this block is draggable
    private JComponentDragHandler dragHandler;
    
    /**
     * Constructs a new FactoryRenderableBlock instance.
     * @param widget the parent widget of this
     * @param blockID the Long ID of its associated Block instance
     */
    public FactoryRenderableBlock(WorkspaceWidget widget, Long blockID){
        super(widget, blockID);
        this.setBlockLabelUneditable();
        dragHandler = new JComponentDragHandler(this);
    }
    
    /**
     * Returns a new RenderableBlock instance (and creates its associated Block) instance of the same genus as this.
     * @return a new RenderableBlock instance with a new associated Block instance of the same genus as this.
     */
    public RenderableBlock createNewInstance(){
        return BlockUtilities.cloneBlock(Block.getBlock(super.getBlockID()));
    }
    
    ///////////////////
    //MOUSE EVENTS (Overriding mouse events in super)
    ///////////////////
    
    public void mousePressed(MouseEvent e) {
    	this.requestFocus();
        //create new renderable block and associated block
        createdRB = createNewInstance();
        //add this new rb to parent component of this
        this.getParent().add(createdRB,0);
        //set the parent widget of createdRB to parent widget of this
        //createdRB not really "added" to widget (not necessary to since it will be removed)
        createdRB.setParentWidget(this.getParentWidget());
        //set the location of new rb from this 
        createdRB.setLocation(this.getX(), this.getY());
        //send the event to the mousedragged() of new block
        MouseEvent newE = SwingUtilities.convertMouseEvent(this, e, createdRB); 
        createdRB.mousePressed(newE);
        mouseDragged(e); // immediately make the RB appear under the mouse cursor
    }

    public void mouseDragged(MouseEvent e) {
        if(createdRB != null){
            //translate this e to a MouseEvent for createdRB
            MouseEvent newE = SwingUtilities.convertMouseEvent(this, e, createdRB);
            createdRB.mouseDragged(newE);
            createdRB_dragged = true;
        }
    }

    public void mouseReleased(MouseEvent e) {
        if(createdRB != null){
            if(!createdRB_dragged){
                Container parent = createdRB.getParent();
                parent.remove(createdRB);
                parent.validate();
                parent.repaint();
            }else{
                //translate this e to a MouseEvent for createdRB
                MouseEvent newE = SwingUtilities.convertMouseEvent(this, e, createdRB);
                createdRB.mouseReleased(newE);
            }
            createdRB_dragged = false;
        }
    }
    
    public void mouseEntered(MouseEvent e) {
        dragHandler.mouseEntered(e);
    }

    public void mouseExited(MouseEvent e) {
        dragHandler.mouseExited(e);
    }
    public void mouseClicked(MouseEvent e){}
    public void startDragging(MouseEvent e) {}
    public void stopDragging(MouseEvent e, WorkspaceWidget w) {}
    public void setZoomLevel(double newZoom){}
}