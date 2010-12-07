package renderable;

import java.awt.Component;
import java.awt.Container;
import java.awt.Point;

import workspace.WorkspaceWidget;

/**
 * CommentSource interface that must be implemented by a class if a comment is to be linked to it.
 * 
 * @author joshua
 *
 */
public interface CommentSource {
	
	/**
	 * returns the parent of the commentSource
	 * @return
	 */
	public Container getParent();

	/**
	 * Returns the parent WorkspaceWidget containing this
	 * @return the parent WorkspaceWidget containing this
	 */
	public WorkspaceWidget getParentWidget();
	
	/**
     * returns where the CommentArrow should draw from
     * @return
     */
    public Point getCommentLocation();

    /**
     * add commentLabel to the commentSource 
     * @param commentLabel
     */
	public Component add(Component commentLabel);

	public Long getBlockID();
}
