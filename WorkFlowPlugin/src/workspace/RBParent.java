package workspace;

import java.awt.Component;

/**
 * RBParents have methods for adding any Component to either the BlockLayer
 * or HighlightLayer.  The HighlightLayer must be rendered behind the BlockLayer
 * such that all Components on the HighlightLayer are rendered behind ALL
 * Components on the BlockLayer.
 * @author Daniel
 *
 */
public interface RBParent {

	/**
	 * Add this Component the BlockLayer, which is understood to be above the 
	 * HighlightLayer, although no guarantee is made about its order relative
	 * to any other layers this RBParent may have.
	 * @param c the Component to add
	 */
	public void addToBlockLayer(Component c);
	
	/**
	 * Add this Component to the HighlightLayer, which is understood to be
	 * directly and completely beneath the BlockLayer, such that all Components 
	 * on the HighlightLayer are rendered behind ALL Components on the BlockLayer.
	 * @param c the Component to add
	 */
	public void addToHighlightLayer(Component c);
	
}
