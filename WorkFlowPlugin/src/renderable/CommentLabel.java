package renderable;

import java.awt.Color;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;

import workspace.Workspace;
import workspace.WorkspaceEvent;

/**
 * The CommentLabel class controls the visibility of a Comment on a RenderableBlock
 * @author joshua
 *
 */
public class CommentLabel extends BlockControlLabel {
	
	private static final long serialVersionUID = 1L;
	
	public CommentLabel(long blockID) {
		 super(blockID);
		 this.setBackground(Color.darkGray);
		 this.setOpaque(true);
	}

	/**
	 * setup current visual state of button 
	 */
	public void update() {
		RenderableBlock rb = RenderableBlock.getRenderableBlock(getBlockID());
		
		if (rb != null) {
	        int x = 5;
	        int y = 7;
	        
	        if (rb.getBlock().isCommandBlock()) {
	        	y-=2;
	        	x-=3;
	        }
	        
	        if (rb.getBlock().isDataBlock() || rb.getBlock().isFunctionBlock()) {
	        	x+=6;
	        	y-=2;
	        }
	        
	        if(rb.getBlock().isInfix() && rb.getBlock().getSocketAt(0) != null){ 
	            if(!rb.getBlock().getSocketAt(0).hasBlock()){
	                x+=30;
	            }else{
	            	if (rb.getSocketSpaceDimension(rb.getBlock().getSocketAt(0)) != null)
	                x+=rb.getSocketSpaceDimension(rb.getBlock().getSocketAt(0)).width + 2;
	            }
	            y += 2;
	            x += 1;
	        }
	        
	        
	        x=rb.rescale(x);
	        y=rb.rescale(y);
	        
			setLocation(x, y);
			setSize(rb.rescale(14), rb.rescale(14));
	
			if (isActive()) {
				setText("?");
				this.setForeground(new Color(255,255,0));
			} else {
				setText("?");
				this.setForeground(Color.lightGray);
			}
			rb.setComponentZOrder(this, 0);
		}
	}

    /**
     * Implement MouseListener interface
     * toggle collapse state of block if button pressed
     */
	public void mouseClicked(MouseEvent e) {
		toggle();
		RenderableBlock rb = RenderableBlock.getRenderableBlock(getBlockID());
		rb.getComment().setVisible(isActive());
		Workspace.getInstance().notifyListeners(new WorkspaceEvent(rb.getComment().getCommentSource().getParentWidget(), WorkspaceEvent.BLOCK_COMMENT_VISBILITY_CHANGE));
		update();
		rb.revalidate();
		rb.repaint();
		Workspace.getInstance().getMiniMap().repaint();
	}
	
    /**
     * Implement MouseListener interface
     * highlight button state
	 */
	public void mouseEntered(MouseEvent e) {			
		super.mouseEntered(e);
		this.setBorder(BorderFactory.createLineBorder(Color.yellow));
		Comment comment = RenderableBlock.getRenderableBlock(getBlockID()).getComment();
		comment.setVisible(true);
		comment.showOnTop();
	}

    /**
     * Implement MouseListener interface
     * de-highlight button state
	 */
	public void mouseExited(MouseEvent e) {
		super.mouseExited(e);
		this.setBorder(BorderFactory.createLineBorder(Color.gray));
		Comment comment = RenderableBlock.getRenderableBlock(getBlockID()).getComment();
		if (!isActive()) {
			comment.setVisible(false);
		}
	}


}


