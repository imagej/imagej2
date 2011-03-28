package imagej.roi.gui.jhotdraw;

import org.jhotdraw.framework.Figure;
import imagej.roi.ImageJROI;

/**
 * @author leek
 *
 *Implement the IJHotDrawROIAdapter to create an adapter
 *that lets JHotDraw edit ImageJ ROIs
 */
public interface IJHotDrawROIAdapter {
	/**
	 * The name to show the user - should indicate what kind of ROI the adapter handles
	 * @return name of adapter
	 */
	public String displayName();
	/**
	 * Name of the icon to be displayed.
	 * 
	 * Typically, these are in the JHotDraw jar at /org/jhotdraw/images, but
	 * perhaps they are in your jar instead? An example is:
	 * 
	 * "/org/jhotdraw/images/RECT"
	 *
	 * @return the path to the icon name
	 */
	public String getIconName();
	/**
	 * Determine whether the adapter can handle a particular roi
	 * @param roi - a ROI that might be editable
	 * @return
	 */
	public boolean supports(ImageJROI roi);
	
	/**
	 * Get the user-displayable names of the ROIs that this adapter
	 * can create.
	 * @return names of ROIs that can be used to fetch the
	 * ROI creation tool.
	 */
	public String [] getROITypeNames();
	
	/**
	 * Create a new ROI of the type indicated by the given name.
	 * The name must be from those returned by getROITypeNames
	 * 
	 * @param name the name of a ROI that this adapter can create
	 * @return a ROI of the associated type in the default initial state
	 */
	public ImageJROI createNewROI(String name);
	
	/**
	 * Create an appropriate figure for the ROI and attach the adapter
	 * to it. The adapter should manage changes to the figure's model
	 * by propagating them to the ROI.
	 * 
	 * @param roi
	 * @return
	 */
	public Figure attachFigureToROI(ImageJROI roi);
	
	/**
	 * The figure and ROI are separated as the editing session ends.
	 * This may be a place where incompleteness in the ROI is
	 * reconciled, such as connecting the final line in a polygon.
	 * 
	 * @param figure
	 */
	public void detachFigureFromROI(Figure figure);
}
