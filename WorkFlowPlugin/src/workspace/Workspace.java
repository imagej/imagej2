package workspace;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JComponent;
import javax.swing.JLayeredPane;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import renderable.BlockUtilities;
import renderable.RenderableBlock;
import workspace.typeblocking.FocusTraversalManager;
import workspace.typeblocking.TypeBlockManager;
import codeblocks.Block;
import codeblockutil.Explorer;
import codeblockutil.ExplorerEvent;
import codeblockutil.ExplorerListener;


/**
 * The Workspace is the main block area, where blocks are manipulated and assembled.
 * This class governs the blocks, the world, the view, drawing, dragging, animating.
 */

public class Workspace extends JLayeredPane implements ISupportMemento, RBParent, ChangeListener, ExplorerListener  {
	private static final long serialVersionUID = 328149080422L;
    /**
     * Single Workspace instance
     */
    private static Workspace ws = new Workspace();
    
	/** WorkspaceListeners that monitor:
	 * block: added, removed, dropped, label changed, connected, disconnected 
	 * workspace: scrolled, zoom changed
	 */
	private HashSet<WorkspaceListener> workspaceListeners = new HashSet<WorkspaceListener>();
	
	/** The reundomanager instance*/
    //private ReundoManager reundoManager;
	
	/** WorkspaceWidgets are components within the workspace other than blocks that
	 * include bars, buttons, factory drawers, and single instance widgets such as
	 * the MiniMap and the TrashCan.
	 */
	private TreeSet<WorkspaceWidget> workspaceWidgets = new TreeSet<WorkspaceWidget>(
			// store these in a sorted set according to their "draw depth"
			new Comparator<WorkspaceWidget>() {
				public int compare(WorkspaceWidget w1, WorkspaceWidget w2) {
					// by returning the difference in "draw depth", we make this comparitor
					// sort according to ascending "draw depth" (i.e. front to back)
                     //System.out.println("widget 1: "+w1);
                     //System.out.println("widget 2: "+w2);
                     //System.out.println("are they equal: "+w1.equals(w2)+" compare result: "+(ws.getComponentZOrder(w1.getComponent())-ws.getComponentZOrder(w2.getComponent())));
					//System.out.println("comparing "+w1+" with "+w2+" result1: "+(getDrawDepth(w1.getJComponent())>getDrawDepth(w2.getJComponent())));
                    double depth1 = getDrawDepth(w1.getJComponent());
                    double depth2 = getDrawDepth(w2.getJComponent());
                    
                    if (depth1 > depth2) {
						return 1;
					}
					else if (depth1<depth2) {
						return -1;
					}
                    //TODO ria should NEVER return zero unless (w1 == w2) otherwise widget will not be added!
                    //ask daniel about this
                    if(w1 != w2){
                        return -1;
                    }else{
                     //System.err.println("returned 0: this widget will not be added to workspace widgets: "+w1+ "comparing with: "+w2);
					return 0;
                    }
				}
			});
	
	
    public static boolean everyPageHasDrawer = false;
	
	/** The Workspace has a BlockCanvas widget on which blocks actually live.
	 * The blockCanvas is what takes care of allowing scrolling and drawing pages,
	 * so it is controlled by the Workspace, but it is also a regular WorkspaceWidget
	 * for the purposes of drag and drop.
	 */
	private BlockCanvas blockCanvas = new BlockCanvas();
    
    /** blockCanvasLayer allows for static components to be laid out beside the block canvas.  One example of 
     * such a component would be a static block factory.  In user testing, we found that novice users performed
     * better with a static block factory than one in which they could drag around and toggle the visibility 
     * of. */
    private JSplitPane blockCanvasLayer;
    
    /**
     * MiniMap associated with the blockCanvas
     */
    private MiniMap miniMap;
    
    private FactoryManager factory;
    
    private FocusTraversalManager focusManager;
    
	/// RENDERING LAYERS ///
    public final static Integer PAGE_LAYER = new Integer(0);
    public final static Integer BLOCK_HIGHLIGHT_LAYER = new Integer(1);
    public final static Integer BLOCK_LAYER = new Integer(2);
    public final static Integer WIDGET_LAYER = new Integer(3);
    public final static Integer DRAGGED_BLOCK_HIGHLIGHT_LAYER = new Integer(4);
    public final static Integer DRAGGED_BLOCK_LAYER = new Integer(5);
    
	private Workspace() {
		super();
        setLayout(null);
	    setBackground(Color.WHITE);
        setPreferredSize(new Dimension(1000, 600));
        
        this.factory = new FactoryManager(true, true);
        this.addWorkspaceListener(this.factory);
        this.blockCanvas.getHorizontalModel().addChangeListener(this);
        List<Explorer> explorers = factory.getNavigator().getExplorers();
        for (Explorer exp: explorers){
        	exp.addListener(this);
        }
        		
        this.miniMap = new MiniMap();
        this.addWidget(this.miniMap, true, true);
        this.addComponentListener(new ComponentAdapter(){
        	public void componentResized(ComponentEvent e){
        		miniMap.repositionMiniMap();
        		blockCanvas.reformBlockCanvas();
        		blockCanvasLayer.setSize(getSize());
                blockCanvasLayer.validate();
         	}
        });
        
    	blockCanvasLayer = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true,
    			factory.getJComponent(), blockCanvas.getJComponent());
    	blockCanvasLayer.setOneTouchExpandable(true);
    	blockCanvasLayer.setDividerSize(6);
        add(blockCanvasLayer, BLOCK_LAYER);
        validate();
        addPageAt(Page.getBlankPage(), 0, false);
        
        this.workspaceWidgets.add(factory);
        
        this.focusManager = new FocusTraversalManager();
	}
    
	/*
	 * Implements explorerEventOccurred method in ExplorerListener interface
	 * If event is of type "1" then hides the Minimize page button
	 * If event is of type "2" then shows the Minimize page button
	 * Event type details can be found in the GlassExplorerEvent class
	 */
	public void explorerEventOccurred(ExplorerEvent event){
		Explorer exp = event.getSource();
		if (event.getEventType()==1){
			List<Page> leftPages = blockCanvas.getLeftmostPages(exp.getSelectedCanvasWidth());
			for (Page p: leftPages){
				p.disableMinimize();
			}
		}
		if (event.getEventType()==2){
			List<Page> leftPages = blockCanvas.getLeftmostPages(exp.getSelectedCanvasWidth());
			for (Page p: leftPages){
				p.enableMinimize();
			}
		}
	}
	
    /**
     * Returns the one <code>Workspace</code> instance
     * @return the one <code>Workspace</code> instance
     */
    public static Workspace getInstance(){
        return ws;
    }
    
    public Dimension getCanvasSize() {
    		return blockCanvas.getCanvas().getSize();
    }
    
    public Dimension getCanvasOffset() {
    	return new Dimension(blockCanvas.getHorizontalModel().getValue()-blockCanvas.getJComponent().getX(),
    			blockCanvas.getVerticalModel().getValue()-blockCanvas.getJComponent().getY());
    }
    
    public Page getPageNamed(String pageName) {
    		return blockCanvas.getPageNamed(pageName);
    }
    
    public BlockCanvas getBlockCanvas(){
    	return blockCanvas;
    }
    
	/**
	 * @return MiniMap associated with this.blockcanvas
	 */
	public MiniMap getMiniMap(){
		return this.miniMap;
	}
	
	/**
	 * Returns the FocusTraversalManager instance
	 * @return FocusTraversalManager instance
	 */
	public FocusTraversalManager getFocusManager(){
		return focusManager;
	}
    
    /**
     * Disables the MiniMap from canvas 
     *
     */
    public void disableMiniMap(){
        miniMap.hideMiniMap();
    }
    
    ////////////////
    // WIDGETS
    ////////////////
    
    private Point p = new Point(0,0); // this is for speed - faster not to re-create Points
    
    /**
     * Returns the WorkspaceWidget currently at the specified point
     * @param point the <code>Point2D</code> to get the widget at, given
     *   in Workspace (i.e. window) coordinates
     * @return the WorkspaceWidget currently at the specified point
     */
    public WorkspaceWidget getWidgetAt(Point point){
        Iterator<WorkspaceWidget> it = workspaceWidgets.iterator();
        //TODO: HUGE HACK, get rid of this. bascally, the facotry has priority
        if(factory.contains(
        		SwingUtilities.convertPoint((JComponent)ws, point, factory.getJComponent()).x,
        		SwingUtilities.convertPoint((JComponent)ws, point, factory.getJComponent()).y)) return factory;
        WorkspaceWidget widget = null;
        while(it.hasNext()){
        	//convert point to the widgets' coordinate system
            widget = it.next();
            p = SwingUtilities.convertPoint((JComponent)ws, point, widget.getJComponent());
            //test if widget contains point and widget is visible
            if(widget.contains(p.x, p.y) && widget.getJComponent().isVisible()) {   
            	return widget; // because these are sorted by draw depth, the first hit is on top
            }
        }
        
        return null; // hopefully we never get here
    }
    
    /**
     * This helper method retuns a fractional "depth" representing the overall
     * z-order of a component in the workspace.  For example, a component with 
     * a "drawDepth" of 1.9 is most likely the first child (rendered on top of,
     * remember) a component with z-order 2 in this container.  1.99 means the 
     * first child of the first child, and so on.
     * @param c - the Component whose draw depth is required.  MUST be an eventual child of the Workspace.
     * @return the fractional "drawDepth" of the Component c.
     */
    private double getDrawDepth(Component c) {
        	int treeLevel = 0;
        	double depth = 0;
        	Container p = c.getParent();
            //System.out.println("getting drawer depth for: "+c);
        	if(p != null){ //ria added this condition (when removing widgets while resetting, they must have a parent, but by then they have none.  
            	// figure out how far down the tree this component is
            	while (p != this && p != null) {
                 p = p.getParent();
            		treeLevel++;
            	}
            	//System.out.println("tree level for "+c+": "+treeLevel);
            	// now walk up the tree, assigning small fractions for distant children,
            	// and getting more important closer to the top level.
            	p = c.getParent();
            	for (int level = treeLevel; level >= 0; level--) {
                if(p == null)
                    break;
            		if (level > 0) depth -= p.getComponentZOrder(c) / Math.pow(10, level);
            		else depth += p.getComponentZOrder(c);
            		c = p;
            		p = p.getParent();    		
            	}
                
                //System.out.println("returned depth "+depth);
             return depth;
         }
         return Double.MAX_VALUE;
     }
    
    /**
     * Adds the specified widget to this Workspace
     * @param widget the desired widget to add
     * @param floatOverCanvas if true, the Workspace will add and render this widget such that it "floats" 
     * above the canvas and its set of blocks.  If false, the widget will be laid out beside the canvas.  This feature
     * only applies if the specified widget is added graphically to the workspace (addGraphically = true)
     * @param addGraphically  a Swing dependent parameter to tell the Workspace whether or not to add 
     * the specified widget as a child component.  This parameter should be false for widgets that have a
     * parent already specified
     */
    public void addWidget(WorkspaceWidget widget, boolean addGraphically, boolean floatOverCanvas){
        if(addGraphically){
            if(floatOverCanvas){
                this.add((JComponent)widget, WIDGET_LAYER);
                widget.getJComponent().setVisible(true);
                revalidate();
                repaint();
            }else{
                blockCanvas.getJComponent().setPreferredSize(new Dimension(blockCanvas.getWidth() - widget.getJComponent().getWidth(), blockCanvasLayer.getHeight()));
            }
        }
        boolean success = workspaceWidgets.add(widget);
        if(!success)
            System.err.println("not able to add: "+widget);
    }
    
    /**
     * Removes the specified widget from this Workspace
     * @param widget the desired widget to remove
     */
    public void removeWidget(WorkspaceWidget widget){
        workspaceWidgets.remove(widget);
        this.remove((JComponent)widget);
    }
    
    /**
     * Returns an unmodifiable Iterable over all the WorkspaceWidgets
     * @return an unmodifiable Iterable over all the WorkspaceWidgets
     */
    public Iterable<WorkspaceWidget> getWorkspaceWidgets(){
        return Collections.unmodifiableSet(workspaceWidgets);
    } 
    
    /**
     * Returns the set of all RenderableBlocks in the Workspace.
     * Includes all live blocks on all pages. Does NOT include:
     *  	(1) Factory blocks,
     *  	(2) dead blocks,
     *  	(3) or subset blocks.
     *  If no blocks are found, it returns an empty set.
     * @return all the RenderableBlocks in the Workspace
     * 		   or an empty set if none exists.
     */
    public Iterable<RenderableBlock> getRenderableBlocks(){
    	//TODO: performance issue, must iterate through all blocks
    	return blockCanvas.getBlocks();
    }
    /**
     * Returns the set of all Blocks in the Workspace.
     * Includes all live blocks on all pages. Does NOT include:
     *  	(1) Factory blocks,
     *  	(2) dead blocks,
     *  	(3) or subset blocks.
     *  If no blocks are found, it returns an empty set.
     * @return all the Blocks in the Workspace
     * 		   or an empty set if none exists.
     */
    public Iterable<Block> getBlocks(){
    	//TODO: performance issue, must iterate through all blocks
    	ArrayList<Block> blocks = new ArrayList<Block>();
        for(RenderableBlock renderable : blockCanvas.getBlocks()){
        	blocks.add(Block.getBlock(renderable.getBlockID()));
        }
        return blocks;
    }
    
    /**
     * Returns all the RenderableBlocks of the specified genus.
     * Include all live blocks on all pages.  Does NOT include:
     *  	(1) all blocks of a different genus
     *  	(2) Factory blocks,
     *  	(3) dead blocks,
     *  	(4) or subset blocks.
     *  If no blocks are found, it returns an empty set.
     * @param genusName - the genus name of the blocks to return
     * @return all the RenderableBlocks of the specified genus
     * 		   or an empty set if none exists.
     */
    public Iterable<RenderableBlock> getRenderableBlocksFromGenus(String genusName){
    	//TODO: performance issue, must iterate through all blocks
        ArrayList<RenderableBlock> blocks = new ArrayList<RenderableBlock>();
        for(RenderableBlock block : blockCanvas.getBlocks()){
            if(Block.getBlock(block.getBlockID()).getGenusName().equals(genusName))
                blocks.add(block);
        }
        return blocks;
    }
    /**
     * Returns all the Blocks of the specified genus.
     * Include all live blocks on all pages.  Does NOT include:
     *  	(1) all blocks of a different genus
     *  	(2) Factory blocks,
     *  	(3) dead blocks,
     *  	(4) or subset blocks.
     *  If no blocks are found, it returns an empty set.
     * @param genusName - the genus name of the blocks to return
     * @return all the Blocks of the specified genus
     * 		   or an empty set if none exists.
     */
    public Iterable<Block> getBlocksFromGenus(String genusName){
    	//TODO: performance issue, must iterate through all blocks
        ArrayList<Block> blocks = new ArrayList<Block>();
        for(RenderableBlock renderable : blockCanvas.getBlocks()){
        	Block block = Block.getBlock(renderable.getBlockID());
        	if(block.getGenusName().equals(genusName))
                blocks.add(block);
        }
        return blocks;
    }
    
    /**
     * Returns the top level blocks in the Workspace (blocks that are 
     * parents of stacks)
     * @return the top level blocks in the Workspace 
     */
    public Iterable<RenderableBlock> getTopLevelBlocks(){
        return blockCanvas.getTopLevelBlocks();
    }
    
    /**
     * Cleans up all the blocks within the block canvas using the default 
     * arrangement algorithm.  
     * TODO ria for now its the naive arranger that uses just the y-coor.
     */
    public void cleanUpAllBlocks(){
        blockCanvas.arrangeAllBlocks();
    }
    
    /**
     * calls TypeBlockManager to copy the highlighted blocks on the canvas
     */
    
    public void copyBlocks(){
    	TypeBlockManager.copyBlock();
    }
    
    /**
     * calls TypeBlockManager to pastes the highlighted blocks on the canvas
     */
    
    public void pasteBlocks(){
    	TypeBlockManager.pasteBlock();
    }
    
    //////////////////////////
    // WORKSPACE LISTENERS
    //////////////////////////
    /**
     * Adds the specified WorkspaceListener
     */
    public void addWorkspaceListener(WorkspaceListener listener){
        if(listener != null) {
        	// warn of duplicate adds
        	assert (!workspaceListeners.contains(listener)): "WorkspaceListener "+listener.toString()+" has already been added.";
            workspaceListeners.add(listener);
        }
    }
    
    /**
     * Removes the specified WorkspaceListener
     * @param listener
     */
    public void removeWorkspaceListener(WorkspaceListener listener){
        if(listener != null)
            workspaceListeners.remove(listener);
    }
    
    /**
     * Notifies all Workspace listeners of the workspace event
     * @param event
     */
    public void notifyListeners(WorkspaceEvent event){
        for(WorkspaceListener wl : workspaceListeners){
            wl.workspaceEventOccurred(event);
        }
    }
    
    ////////////////////
    //TypeBLockManaging
    ////////////////////
    /**
     * Enables TypeBLocking if and only if enabled == true
     */
    public void enableTypeBlocking(boolean enabled){
    	if(enabled){
    		TypeBlockManager.enableTypeBlockManager(blockCanvas);
    	}else{
    		TypeBlockManager.disableTypeBlockManager();
    	}
    }
    
    ///////////////////
    // WORKSPACE ZOOM
    ///////////////////
    
    private double zoom =1.0;
    
    /**
     * Sets the Workspace zoom at the specified zoom level
     * @param newZoom the desired zoom level
     */
    public void setWorkspaceZoom(double newZoom){
    	double oldZoom = this.zoom;
    	int cDX = 0, cDY = 0;
    	
		this.zoom = newZoom;

		BlockUtilities.setZoomLevel(newZoom);
		for(RenderableBlock block : Workspace.getInstance().getRenderableBlocks()){
			block.setZoomLevel(newZoom);
		}
		for(RenderableBlock block : Workspace.getInstance().getFactoryManager().getBlocks()){
			block.setZoomLevel(newZoom);
		}
		for(Page p : Workspace.getInstance().getBlockCanvas().getPages()){
			for(RenderableBlock block : p.getTopLevelBlocks()){
				
				// checks if the x and y position has not been set yet, this happens when
				// a previously saved project is just opened and the blocks have not been
				// moved yet. otherwise, the unzoomed X and Y are calculated in RenderableBlock
				if (block.getUnzoomedX() == 0.0 && block.getUnzoomedY() == 0.0) {
					if (newZoom == 1.0) {
						block.setUnzoomedX(block.getX());
						block.setUnzoomedY(block.getY());
					} else {
						block.setUnzoomedX(block.calculateUnzoomedX(block.getX()));
						block.setUnzoomedY(block.calculateUnzoomedY(block.getY()));
					}
				} else {}
				
				
				if (block.hasComment()) {
					//determine the new relative position of the comment based on the current relative position
					cDX = (int) ((block.getComment().getX() - block.getX())/oldZoom*newZoom);
					cDY = (int) ((block.getComment().getY() - block.getY())/oldZoom*newZoom);
				}
				// calculates the new position based on the initial position when zoom is at 1.0
				block.setLocation((int)(block.getUnzoomedX()*this.zoom), (int)(block.getUnzoomedY()*this.zoom));
				if (block.hasComment()) {
					//Set the comment location to the new relative position
					block.getComment().setLocation(block.getX() + cDX, block.getY() + cDY);
				}
				block.redrawFromTop();
				block.repaint();
			}
		}
		Page.setZoomLevel(newZoom);
    }
    
    /**
     * Returns the current workspace zoom
     * @return the current workspace zoom
     */
    public double getCurrentWorkspaceZoom(){
        return zoom;
    }
    
    /**
     * Resets the workspace zoom to the default level 
     *
     */
    public void setWorkspaceZoomToDefault(){
        this.setWorkspaceZoom(1.0);
    }
    
    public void scrollToComponent(JComponent c){
    	blockCanvas.scrollToComponent(c);
    }
    
    public void stateChanged(ChangeEvent e){
    	List<Explorer> explorers = factory.getNavigator().getExplorers();
        for (Explorer exp: explorers){
			List<Page> leftMostPages = blockCanvas.getLeftmostPages(exp.getSelectedCanvasWidth());
			boolean expanded = exp.anyCanvasSelected();
			for (Page p: blockCanvas.getPages()){
				if (expanded){
					p.setHide(false);
				}
			}
			for (Page p: leftMostPages){
				if (expanded){
					p.setHide(true);
				}
			}
        }
    }

    ////////////////
    //PAGE METHODS (note: these may change)
    ////////////////
    
    /**
     * Adds the specified page to the Workspace at the right end of the canvas
     * @param page the desired page to add
     */
    public void addPage(Page page){
        addPage(page, blockCanvas.numOfPages());
    }
    
    /**
     * Adds the specified page to the Workspace at the specified position on the canvas
     * @param page the desired page to add
     */
    public void addPage(Page page, int position){
        //this method assumes that this addPage was a user or file loading 
        //event in which case a page added event should be thrown
        addPageAt(page, position, true);
    }
    
    /**
     * Places the specified page at the specified index.
     * If a page already exists at that index,
     * this method will replace it.
     * @param page the Page to place
     * @param position - the position to place the specified page,
     * 		  where 0 is the leftmost page
     */
    public void putPage(Page page, int position){
        if(blockCanvas.hasPageAt(position)){
            removePageAt(position);
        }
        addPageAt(page, blockCanvas.numOfPages(), true);
    }
    
    /**
     * Adds a Page in the specified position, where position 0 is the leftmost page
     * @param page - the desired Page to add
     * @param index - the desired position of the page
     * @param fireWorkspaceEvent if set to true, will fire a WorkspaceEvent that a 
     * Page was added
     */
    private void addPageAt(Page page, int index, boolean fireWorkspaceEvent){
        blockCanvas.addPage(page, index);
        workspaceWidgets.add(page);
        if(fireWorkspaceEvent) notifyListeners(new WorkspaceEvent(page, WorkspaceEvent.PAGE_ADDED));
    }
    
    /**
     * Removes the specified page from the Workspace at the specified position, 
     * where position 0 is the left most page
     * @param position
     */
    public void removePageAt(int position){
        removePage(blockCanvas.getPageAt(position));
    }
    
    /**
     * Removes the specified page from the Workspace
     * @param page the desired page to remove
     */
    public void removePage(Page page){
        boolean success = workspaceWidgets.remove(page);
        if (!success)
        	System.out.println("Page: "+page+", was NOT removed successfully");
        notifyListeners(new WorkspaceEvent(page, WorkspaceEvent.PAGE_REMOVED));
       	blockCanvas.removePage(page);
    }
    
    /**
     * Renames the page with the specified oldName to the specified newName.
     * @param oldName the oldName of the page to rename
     * @param newName the String name to change the page name to
     */
    public void renamePage(String oldName, String newName){
        Page renamedPage = blockCanvas.renamePage(oldName, newName);
        //TODO ria HACK TO GET DRAWERS AND PAGE IN SYNC
        //as a rule, all relevant data like pages and drawers should be updated before
        //an event is released because the listeners make assumptions on the state
        //of the data.  in the future, have the page rename its drawer
        factory.renameDynamicDrawer(oldName, newName);
        notifyListeners(new WorkspaceEvent(renamedPage, oldName, WorkspaceEvent.PAGE_RENAMED));
    }
    
    /**
     * Returns the number of pages contained within this.  By default
     * will always have a page even if a page was not specified.  The page
     * will just be blank.  
     * @return the number of pages contained within this
     */
    public int getNumPages(){
        return blockCanvas.numOfPages();
    }
    
    /**
     * Find the page that lies underneath this block
     * CAN RETURN NULL
     * @param block
     */
    public Page getCurrentPage(RenderableBlock block){
    	for (Page page : Workspace.getInstance().getBlockCanvas().getPages()){
    		if (page.contains(SwingUtilities.convertPoint(block.getParent(), block.getLocation(), page.getJComponent()))){
    			return page;
    		}
    	}
    	return null;
    }

    /**
     * Marks the page of the specified name as being selected.  The workspace 
     * view may shift to that page. 
     * @param page the Page selected
     * @param byUser true if Page was selected by the User
     */
    public void pageSelected(Page page, boolean byUser){
        blockCanvas.switchViewToPage(page);
    }
    
    public FactoryManager getFactoryManager() {
    	return factory;
    }
    
    /**
     * Returns an unmodifiable Iterable of all the SearchableContainers within this 
     * workspace.
     */
    public Iterable<SearchableContainer> getAllSearchableContainers(){
        ArrayList<SearchableContainer> containers = new ArrayList<SearchableContainer>(factory.getSearchableContainers());
        
        for(WorkspaceWidget w : workspaceWidgets){
            if(w instanceof SearchableContainer)
                containers.add((SearchableContainer)w);
        }
        
        return Collections.unmodifiableList(containers);
    }
    
    ////////////////////////
    //Subsets             //
    ////////////////////////
    /**
     * Sets up the subsets by clearing all subsets and installing
     * the new collection of subsets.  If "usingSys" is true,
     * the the factory and myblocks drawers will be accessible.
     * If "usingSubs" is true, then the subset drawers will
     * be accessible.
     * @param subsets - collection of subsets
     * @param usingSys - true for factory and myblocks
     * @param usingSubs - true for subsets
     */
    public void setupSubsets(Collection<Subset> subsets, boolean usingSys, boolean usingSubs){
    	this.factory.setupSubsets(subsets, usingSys, usingSubs);
    }
    
    ////////////////////////
    // SAVING AND LOADING //
    ////////////////////////
    /**
     * Returns the save String of this.  Currently returns the BlockCanvas 
     * save String only.
     * @return the save String of this.
     */
    public String getSaveString(){
        StringBuffer saveString = new StringBuffer();
        saveString.append(blockCanvas.getSaveString());
        return saveString.toString();
    }
    
    /**
     * Loads the workspace with the following content:
     * - RenderableBlocks and their associated Block instances that reside
     *   within the BlockCanvas
     * @param newRoot the XML Element containing the new desired content.  Some of the 
     * content in newRoot may override the content in originalLangRoot.  (For now, 
     * pages are automatically overwritten.  In the future, will allow drawers
     * to be optionally overriden or new drawers to be inserted.)
     * @param originalLangRoot the original language/workspace specification content
     * @requires originalLangRoot != null
     */
    public void loadWorkspaceFrom(Element newRoot, Element originalLangRoot){
        if(newRoot != null){
            //load pages, page drawers, and their blocks from save file
            blockCanvas.loadSaveString(newRoot);
            //load the block drawers specified in the file (may contain 
            //custom drawers) and/or the lang def file if the contents specify
            PageDrawerLoadingUtils.loadBlockDrawerSets(originalLangRoot, factory);
            PageDrawerLoadingUtils.loadBlockDrawerSets(newRoot, factory);
            loadWorkspaceSettings(newRoot);
        }else{
            //load from original language/workspace root specification
            blockCanvas.loadSaveString(originalLangRoot);
            //load block drawers and their content
            PageDrawerLoadingUtils.loadBlockDrawerSets(originalLangRoot, factory);
            loadWorkspaceSettings(originalLangRoot);
        }
        
    }
    
    /**
     * Loads the settings for this Workspace.  Settings include
     * specification of programming environment features such as 
     * the search bar, minimap, or zooming.  
     * @param root
     */
    private void loadWorkspaceSettings(Element root){
        Pattern attrExtractor=Pattern.compile("\"(.*)\"");
        Matcher nameMatcher;
        
        NodeList miniMapNodes = root.getElementsByTagName("MiniMap");
        Node miniMapNode;
        for(int i=0; i<miniMapNodes.getLength(); i++){
            miniMapNode = miniMapNodes.item(i);
            if(miniMapNode.getNodeName().equals("MiniMap")){
                nameMatcher=attrExtractor.matcher(miniMapNode.getAttributes().getNamedItem("enabled").toString());
                if (nameMatcher.find() && nameMatcher.group(1).equals("no")){
                    this.disableMiniMap();
                }
            }
        }
        
        NodeList typeNodes = root.getElementsByTagName("Typeblocking");
        Node typeNode;
        for(int i=0; i<typeNodes.getLength(); i++){
            typeNode = typeNodes.item(i);
            if(typeNode.getNodeName().equals("Typeblocking")){
                nameMatcher=attrExtractor.matcher(typeNode.getAttributes().getNamedItem("enabled").toString());
                if (nameMatcher.find() && nameMatcher.group(1).equals("no")){
                    this.enableTypeBlocking(false);
                }else{
                	this.enableTypeBlocking(true);
                }
            }
        }
    }
    
    /**
     * Clears the Workspace of:
     * - all the live blocks in the BlockCanvas.
     * - all the pages on the BlockCanvas
     * - all its BlockDrawers and the RB's that reside within them
     * - clears all the BlockDrawer bars of its drawer references and 
     *   their associated buttons
     * - clears all RenderableBlock instances (which clears their associated
     *   Block instances.)
     * Note: we want to get rid of all RendereableBlocks and their 
     * references.  
     * 
     * Want to get the Workspace ready to load another workspace 
     */
    public void reset(){
    	//we can't iterate and remove widgets at the same time so 
        //we remove widgets after we've collected all the widgets we want to remove
        //TreeSet.remove() doesn't always work on the TreeSet, so instead,
    	//we clear and re-add the widgets we want to keep
    	ArrayList<WorkspaceWidget> widgetsToRemove = new ArrayList<WorkspaceWidget>();
        ArrayList<WorkspaceWidget> widgetsToKeep = new ArrayList<WorkspaceWidget>();
        for(WorkspaceWidget w : workspaceWidgets){
            if(w instanceof Page){
                widgetsToRemove.add(w);
            }else{
            	widgetsToKeep.add(w);
            }
        }
        workspaceWidgets.clear();
        workspaceWidgets.addAll(widgetsToKeep);
        workspaceWidgets.add(factory);
        
        //We now reset the widgets we removed.
        //Doing this for each one gets costly.
        //Do not do this for Pages because on repaint,
        //the Page tries to access its parent.
        for (WorkspaceWidget w : widgetsToRemove){
            Container parent = w.getJComponent().getParent();
            if(w instanceof Page){
                ((Page)w).reset();
            }
	        if(parent != null){
	            parent.remove(w.getJComponent());
	            parent.validate();
	            parent.repaint();
	        }
        }
        
        //We now reset, the blockcanvas, the factory, and the renderableblocks
        blockCanvas.reset();
        addPageAt(Page.getBlankPage(), 0, false); //TODO: System expects PAGE_ADDED event
        factory.reset();
        RenderableBlock.reset();
        revalidate();
    }
    
    /***********************************
    * State Saving Stuff for Undo/Redo *
    ***********************************/
     
    private class WorkspaceState
    {
    	public Map<Long, Object> blockStates;
    	public Object blockCanvasState;
    }

	public Object getState()
	{
/*		//To add workspace specific state, put it here
		WorkspaceState state = new WorkspaceState();
		
		//Get the block states
		Map<Long, Object> blockStates = new HashMap<Long, Object>();
		for(Block block : Block.getAllBlocks())
		{
			blockStates.put(block.getBlockID(), block.getState());
		}
		
		//Save the blocks and the canvas state
		state.blockStates = blockStates;
		state.blockCanvasState = blockCanvas.getState();
		
		return state;
*/		return null;
	}

	public void loadState(Object memento)
	{
		assert memento instanceof WorkspaceState : ""; 
		if(memento instanceof WorkspaceState)
		{
			WorkspaceState state = (WorkspaceState)memento;
			//Load the blocks state
			for(Long blockID : state.blockStates.keySet())
			{
				Block toBeUpdated = Block.getBlock(blockID);
				toBeUpdated.loadState(state.blockStates.get(blockID));
			}			
			//Load the canvas state			
			blockCanvas.loadState(state.blockCanvasState);
		}				
	}
	
	public void undo()
	{
//		reundoManager.undo();
	}
	
	public void redo()
	{
//		reundoManager.redo();
	}

	/******************************************
	 * RBParent implemented methods
	 ******************************************/
	public void addToBlockLayer(Component c) {
		this.add(c, DRAGGED_BLOCK_LAYER);
	}

	public void addToHighlightLayer(Component c) {
		this.add(c, DRAGGED_BLOCK_HIGHLIGHT_LAYER);
	}
}
