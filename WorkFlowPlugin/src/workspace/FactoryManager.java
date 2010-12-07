package workspace;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JPanel;

import renderable.FactoryRenderableBlock;
import renderable.RenderableBlock;
import codeblocks.Block;
import codeblocks.BlockStub;
import codeblockutil.CBorderlessButton;
import codeblockutil.CLabel;
import codeblockutil.Canvas;
import codeblockutil.Navigator;


/**
 * ***********************OVERVIEW**************************
 * The FactoryManager manages all block factories in the workspace.
 * It has three main functions:  to control and display all factories
 * in one simple UI design, to manage the additions of new drawers,
 * and to add blocks to throse drawers appropriately.
 * 
 * The FactoryManager manages two factories: the static factory
 * and dynamic factory.  Each factory has a set of drawers.  NO TWO
 * DRAWERS WITHIN ANY FACTORY MAY SHARE the same name.
 * 
 * ********************FACTORY STRUCTURE***********************
 * 
 * Let's take a look into the stucture of a factory.  Factory is
 * a pallete that sits on the far left side of the workspace.
 * It has a bunch of drawers that slides up and down.  Each
 * drawer contains a bunch of related blocks that can be dragged
 * out.
 * 
 * The FactoryManager has two types of drawers: static and dynamic.
 * To add, remove, rename, drawers of either type, users should
 * invoke the name that specifies a particular drawer. Users
 * may also add blocks to the drawers or retrieve the set of blocks
 * that each drawer holds.
 * 
 * *************IMPLEMENTATION DETAIL******************
 * 
 * How the FactoryManager implements this UI is implementation
 * dependant.  Right now, it uses the Navigator-Explorer-Canvas deisgn.
 * Clients of the FactoryManager should know nothing about the
 * internal GUIs used to control the interface.  Internally,
 * a Canvas (rahter than an instance of Drawer) is created for every
 * "drawer" that the user wishes to add.  But this is an implementation
 * detail that the user should not be bothered with.  All the user should
 * know is that a "drawer" specified by some String object was created.
 * The handling of the drawers themselves are dealt with internally.
 * In a previous design of the factories, developers had to create
 * instance of Drawers and pass them along to the the factories.
 * In the NEW design, we remove that burden from the developer and allow the
 * developer to access drawers by calling its name only.  This may
 * limit extensibility but keeps the system more robust.
 * 
 * *********************A WORD ON DRAWER**********************
 * Please note that the word "drawer" as it is used by the
 * FactoryManager refers to the object that holds blocks.
 * A factory holds a bunch of drawers, which in turn holds
 * a bunch of blocks.
 * 
 * Please do not mix this definition with the CSwing Drawer class.
 * A CSwing Drawer is a low-level component that is used
 * in a CSwing Exlorer.  Here, when the documentation refers
 * to drawers, it is NOT refering to the CSwing Drawer.  Rather,
 * when we say "drawer", we are referign to that object that holds blocks.
 * 
 * *****************NAMING OF DRAWERS*************************
 * Each factory may have only ONE drawer with a particular name.
 * Two different factories may NOT share a name.   If we have
 * a static drawer named "FOO", we may not have another drawer named
 * "FOO" in the dynamic drawers.
 * 
 * @author An Ho
 *
 */
public class FactoryManager implements WorkspaceWidget, ComponentListener, WorkspaceListener{
	/** The string identifier of static drawers */
	private static final String STATIC_NAME = "Factory";
	/** The string identifier of dynamic drawers */
	private static final String DYNAMIC_NAME = "My Blocks";
	/** The string identifier of subset drawers */
	private static final String SUBSETS_NAME = "Subsets";
	/** The high-level UI that manages the controlling of internal CWsing components */
	private Navigator navigator;
	/** The high-level UI widget that manages swicthing between different factories */
	private JComponent factorySwicther;
	/** the set os static drawers */
	private List<FactoryCanvas> staticCanvases;
	/** The set of dynaic drawers */
	private List<FactoryCanvas> dynamicCanvases;
	/** The set of subset drawers */
	private List<FactoryCanvas> subsetCanvases;
	
	/**
	 * Constucts new Factorymanager
	 * @param hasStatic
	 * @param hasDynamic
	 */
	public FactoryManager(boolean hasStatic, boolean hasDynamic){
		this.navigator = new Navigator();
		this.navigator.getJComponent().setPreferredSize(new Dimension(160,600));
		this.navigator.addExlorer(STATIC_NAME);
		this.navigator.addExlorer(DYNAMIC_NAME);
		this.navigator.addExlorer(SUBSETS_NAME);
		this.factorySwicther = new JPanel(new BorderLayout());
		this.factorySwicther.add(navigator.getSwitcher());
		this.factorySwicther.setOpaque(false);
		this.navigator.getJComponent().addComponentListener(this);
		this.staticCanvases = new ArrayList<FactoryCanvas>();
		this.dynamicCanvases = new ArrayList<FactoryCanvas>();
		this.subsetCanvases = new ArrayList<FactoryCanvas>();
	}
	/**
	 * Resets FactoryManager
	 */
    public void reset(){
		this.staticCanvases = new ArrayList<FactoryCanvas>();
		this.dynamicCanvases = new ArrayList<FactoryCanvas>();
		this.subsetCanvases = new ArrayList<FactoryCanvas>();
		this.navigator.setCanvas(this.staticCanvases, STATIC_NAME);
		this.navigator.setCanvas(this.dynamicCanvases, DYNAMIC_NAME);
		this.navigator.setCanvas(this.subsetCanvases, SUBSETS_NAME);
    }
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
    	if(usingSubs){
	    	this.subsetCanvases.clear();
	    	for(Subset subset : subsets){
	    		FactoryCanvas canvas = new FactoryCanvas(subset.getName(), subset.getColor());
	    		for(RenderableBlock frb : subset.getBlocks()){
	    			canvas.addBlock(frb);
		            Workspace.getInstance().notifyListeners(new WorkspaceEvent(this, frb.getBlockID(), WorkspaceEvent.BLOCK_ADDED));
				}
				canvas.layoutBlocks();
	    		this.subsetCanvases.add(canvas);
	    	}
	    	this.navigator.setCanvas(this.subsetCanvases, SUBSETS_NAME);
	    	if(usingSys){
	    		this.factorySwicther.removeAll();
	    		this.factorySwicther.add(this.navigator.getSwitcher());
	    	}else{
	    		this.factorySwicther.removeAll();
	    		this.factorySwicther.add(new CLabel(SUBSETS_NAME));
	    	}
    		this.viewSubsetsDrawers();
    	}else if (usingSys){
    		this.factorySwicther.removeAll();
    		final CBorderlessButton factoryButton = new CBorderlessButton(STATIC_NAME);
    		final CBorderlessButton myblocksButton = new CBorderlessButton(DYNAMIC_NAME);
    		ActionListener listener = new ActionListener(){
    			public void actionPerformed(ActionEvent e){
    				if (factoryButton.equals(e.getSource())){
    					FactoryManager.this.viewStaticDrawers();
    				}else if(myblocksButton.equals(e.getSource())){
    					FactoryManager.this.viewDynamicDrawers();
    				}
    			}
    		};
    		factoryButton.addActionListener(listener);
    		myblocksButton.addActionListener(listener);
    		this.factorySwicther.add(factoryButton, BorderLayout.WEST);
    		this.factorySwicther.add(myblocksButton, BorderLayout.EAST);
    		this.viewStaticDrawers();
    	}
    	this.factorySwicther.revalidate();
    	this.factorySwicther.repaint();
    }
    
	/**
	 * prints an error message in red without ending the run process.
	 * For debuggin purposes
	 * @param m
	 */
	private void printError(String m){
		new RuntimeException(m).printStackTrace();
	}
	
	/////////////////////
	//Reforming methods//
	/////////////////////
	
    public void componentResized(ComponentEvent e){
    	this.relayoutFactory();
    	//this.relayoutBlocks();
    }
	public void componentHidden(ComponentEvent e){}
	public void componentMoved(ComponentEvent e){}
    public void componentShown(ComponentEvent e){}
	/**
	 * Relayout all factories
	 */
	private void relayoutFactory(){
		this.navigator.reformView();
	}
	/**
	 * Relayout all the drawers
	 */
	public void relayoutBlocks(){
		for(FactoryCanvas canvas : this.staticCanvases){
			canvas.layoutBlocks();
		}
		for(FactoryCanvas canvas : this.dynamicCanvases){
			canvas.layoutBlocks();
		}
	}
	
	public Navigator getNavigator(){
		return this.navigator;
	}
	
	//////////////////
	//Drawer Methods//
	//////////////////
	
	
	/**
	 * @return the set of searchable contianers in all factories
	 */
	public Collection<SearchableContainer> getSearchableContainers(){
		Collection<SearchableContainer> containers = new HashSet<SearchableContainer>();
		for(SearchableContainer con : this.staticCanvases){
			containers.add(con);
		}
		for(SearchableContainer con : this.dynamicCanvases){
			containers.add(con);
		}
		return containers;
	}
	
	/**
	 * Returns a collection of the subsets within this
	 * @return a collection of the subsets within this
	 */
	public Collection<Subset> getSubsets() {
		Collection<Subset> subsets = new ArrayList<Subset>();
		for (FactoryCanvas subset : subsetCanvases) {
			Iterable<RenderableBlock> blocks = subset.getBlocks();
			subsets.add(new Subset(subset.getName(), subset.getColor(), blocks));
		}
		return subsets;
	}
	
	/**
	 * @return an array containing the set of drawers
	 * 			in no particular order.  If no drawers exists,
	 * 			then an empty set is returned. The return value
	 * 			MAY NOT BE NULL.
	 */
	public Collection<String> getStaticDrawers(){
		Collection<String> drawers = new HashSet<String>();
		for(Canvas canvas : this.staticCanvases){
			String name = canvas.getName();
			if(name == null){
				this.printError("Drawer name may not be null");
			}else if(drawers.contains(name)){
				this.printError("Duplicate Drawer name!");
			}else{
				drawers.add(name);
			}
		}
		return drawers;
	}
	public Collection<String> getDynamicDrawers(){
		Collection<String> drawers = new HashSet<String>();
		for(Canvas canvas : this.dynamicCanvases){
			String name = canvas.getName();
			if(name == null){
				this.printError("Drawer name may not be null");
			}else if(drawers.contains(name)){
				this.printError("Duplicate Drawer name!");
			}else{
				drawers.add(name);
			}
		}
		return drawers;
	}
	/**
	 * Swicth view to the set of static drawers
	 */
	public void viewStaticDrawers(){
		this.navigator.setView(STATIC_NAME);
	}
	/**
	 * Switch view to the set of dynamic drawers
	 *
	 */
	public void viewDynamicDrawers(){
		this.navigator.setView(DYNAMIC_NAME);
	}
	public void viewSubsetsDrawers(){
		this.navigator.setView(SUBSETS_NAME);
	}
	/**
	 * may not two draers with the same name
	 * @param sta
	 * @param dyn
	 * @param name
	 * @param position
	 * @return true if and only if the following conditions are met:
	 * 			-specified name is not null,
	 * 			-if "sta" is true, then 0<=position<staticdrawers.size
	 * 			-if "dyn" is true, then 0<=position<static drawers.size
	 * 			-there is NO other drawers with the same name as the
	 * 			 specified name (in oth static or dynamic sets)
	 */
	private boolean isValidDrawer(boolean sta, boolean dyn, String name, int position){
		if(sta){
			if (position < 0) return false;
			if (position > this.staticCanvases.size()) return false;
			for(Canvas canvas : this.staticCanvases){
				if (canvas.getName().equals(name)) return false;
			}
		}
		if(dyn){
			if (position < 0) return false;
			if (position > this.dynamicCanvases.size()) return false;
			for(Canvas canvas : this.dynamicCanvases){
				if (canvas.getName().equals(name)) return false;
			}
		}
		return true;
	}
	/**
	 * Adds a static drawer if no drawer with the specified name already exists.
	 * If one alreaedy exist, then do ntohing.  If the name is null, do nothing
	 * @param name - name os drawer, may not be null
	 * @param color
	 * 
	 * @requires name != null &&
	 * 			 drawer to not already exist in BOTH static and dynamic set
	 */
	public void addStaticDrawer(String name, Color color){
    	this.addStaticDrawer(name, staticCanvases.size(), color);
	}
	/**
	 * Adds a static drawer if no drawer with the specified name already exists.
	 * If one alreaedy exist, then do ntohing.  If the name is null, do nothing
	 * @param name - name os drawer, may not be null
	 * @param color
	 * @param position
	 * 
	 * @requires name != null &&
	 * 			 drawer to not already exist in BOTH static and dynamic set
	 */
	public void addStaticDrawer(String name, int position, Color color){
		if(isValidDrawer(true, false, name, position)){
			FactoryCanvas canvas = new FactoryCanvas(name, color);
			this.staticCanvases.add(position, canvas);
			this.navigator.setCanvas(staticCanvases, STATIC_NAME);
		}else{
			this.printError("Invalid Drawer: trying to add a drawer that already exists: "+ name);
		}
	}
	
	/**
	 * Adds a new Subset drawer with the specified name and button color.  
	 * Places the drawer button below the last added subset drawer
	 * @param name String name of new subset drawer, should not be null
	 * @param color Color of drawer button
	 */
	public void addSubsetDrawer(String name, Color color){
		this.addSubsetDrawer(name, subsetCanvases.size(), color);
	}
	
	/**
	 * Adds a new Subset drawer with the specified name and button color at the specified position.
	 * @param name String name of the new subset drawer, should not be null.
	 * @param position index of drawer button position in block drawer set
	 * @param color button color of drawer
	 */
	public void addSubsetDrawer(String name, int position, Color color){
		FactoryCanvas canvas = new FactoryCanvas(name, color);
		this.subsetCanvases.add(position, canvas);
		this.navigator.setCanvas(subsetCanvases, SUBSETS_NAME);
	}
	/**
	 * Adds a static drawer if no drawer with the specified name already exists.
	 * If one alreaedy exist, then do ntohing.  If the name is null, do nothing
	 * @param name - name os drawer, may not be null
	 * 
	 * @requires name != null &&
	 * 			 drawer to not already exist in BOTH static and dynamic set
	 */
	public void addDynamicDrawer(String name){
    	this.addDynamicDrawer(name, dynamicCanvases.size());
	}
	/**
	 * Adds a duynamic drawer if no drawer with the specified name already exists.
	 * If one alreaedy exist, then do ntohing.  If the name is null, do nothing
	 * @param name - name os drawer, may not be null
	 * @param position
	 * 
	 * @requires name != null &&
	 * 			 drawer to not already exist in BOTH static and dynamic set
	 */
	public void addDynamicDrawer(String name, int position){
		if(isValidDrawer(false, true, name, position)){
			FactoryCanvas canvas = new FactoryCanvas(name);
			this.dynamicCanvases.add(position, canvas);
			this.navigator.setCanvas(dynamicCanvases, DYNAMIC_NAME);
		}else{
			this.printError("Invalid Drawer: trying to add a drawer that already exists: "+name);
		}
	}
	/**
	 * Renames drawer from oldName to newName.  Only perform this action if:
	 * 		(1) there exists a drawer specified by oldName,
	 * 		(2) there exists no drawers specified by newName
	 * 		(3) oldName and newName != null
	 * @param oldName
	 * @param newName
	 * 
	 * @requires oldName != null &&
	 * 			 drawer with newName exists in EITHER static or dynamic set &&
	 * 			 drawer with newName to not already exist in BOTH static and dynamic set
	 */
	public void renameStaticDrawer(String oldName, String newName){
		//check rep
		if( oldName == null || newName ==null){
			this.printError("Drawers may not have a null instance for a name.");
			return;
		}
		for(FactoryCanvas duplicateCanvas : this.staticCanvases){
			if (duplicateCanvas.getName().equals(newName)){
				this.printError("Drawer already exists with name: "+newName);
				return;
			}
		}
		//rename
		for(FactoryCanvas oldCanvas : this.staticCanvases){
			if (oldCanvas.getName().equals(oldName)){
				oldCanvas.setName(newName);
				return;
			}
		}
		this.printError("No Drawer was found with the name: "+oldName);
		return;
	}
	public void renameDynamicDrawer(String oldName, String newName){
		//check rep
		if( oldName == null || newName ==null){
			this.printError("Drawers may not have a null instance for a name.");
			return;
		}
		for(FactoryCanvas duplicateCanvas : this.dynamicCanvases){
			if (duplicateCanvas.getName().equals(newName)){
				this.printError("Drawer already exists with name: "+newName);
				return;
			}
		}
		//rename
		for(FactoryCanvas oldCanvas : this.dynamicCanvases){
			if (oldCanvas.getName().equals(oldName)){
				oldCanvas.setName(newName);
				return;
			}
		}
		this.printError("No Drawer was found with the name: "+oldName);
		return;
	}
	/**
	 * removes drawer with specified name.  Only perform this action if:
	 * 		(1) there exists a drawer specified by name,
	 * 		(3) name != null
	 * @param name
	 * 
	 * @requires name != null && there exists a drawer with sepcified name
	 */
	public void removeStaticDrawer(String name){
		FactoryCanvas canvas = null;
		for(FactoryCanvas c : this.staticCanvases){
			if (c.getName().equals(name)){
				canvas = c;
			}
		}
		if(canvas != null){
			this.staticCanvases.remove(canvas);
			this.navigator.setCanvas(this.staticCanvases, STATIC_NAME);
			return;
		}
		this.printError("No Drawer found with name: "+name);
		return;
	}
	public void removeDynamicDrawer(String name){
		FactoryCanvas canvas = null;
		for(FactoryCanvas c : this.dynamicCanvases){
			if (c.getName().equals(name)){
				canvas = c;
			}
		}
		if(canvas != null){
			this.dynamicCanvases.remove(canvas);
			this.navigator.setCanvas(this.dynamicCanvases, DYNAMIC_NAME);
			return;
		}
		this.printError("No Drawer found with name: "+name);
		return;
	}
	
	/////////////////
	//Block Methods//
	/////////////////
	
	/**
	 * @returns set of blocks found in drawer with the specified name.
	 * 			If no blocks are found in the drawer, return an empty set.
	 * 			If no Drawers are found with specified name, return empty set.
	 */
	public Collection<RenderableBlock> getStaticBlocks(String name){
		ArrayList<RenderableBlock> blocks = new ArrayList<RenderableBlock> ();
		for(FactoryCanvas canvas : this.staticCanvases){
			if(canvas.getName().equals(name)){
				blocks.addAll(canvas.getBlocks());
				return blocks;
			}
		}
		this.printError("Drawer not found: "+name);
		return blocks;
    }
	public Collection<RenderableBlock> getDynamicBlocks(String name){
		ArrayList<RenderableBlock> blocks = new ArrayList<RenderableBlock> ();
		for(FactoryCanvas canvas : this.dynamicCanvases){
			if(canvas.getName().equals(name)){
				blocks.addAll(canvas.getBlocks());
				return blocks;
			}
		}
		this.printError("Drawer not found: "+name);
		return blocks;
    }
	/**
	 * @return all blocks in all drawers.  If no blocks found, return
	 * 			an empty set.  Ifno drawers exists in either factories,
	 * 			return an empty set.
	 */
	public Collection<RenderableBlock> getBlocks() {
		ArrayList<RenderableBlock> blocks = new ArrayList<RenderableBlock> ();
		for(FactoryCanvas canvas : this.staticCanvases){
			blocks.addAll(canvas.getBlocks());
		}
		for(FactoryCanvas canvas : this.dynamicCanvases){
			blocks.addAll(canvas.getBlocks());
		}
		return blocks;
    }
	/**
	 * Add blocks to drawer if drawer can be found.  Do nothing
	 * if no drawer if specified name is found.
	 * @param block
	 * @param drawer
	 */
	public void addStaticBlock(RenderableBlock block, String drawer) {
		for(FactoryCanvas canvas : this.staticCanvases){
			if (canvas.getName().equals(drawer)){
				if(block == null || Block.NULL.equals(block.getBlockID())){
					printError("Attempting to add a null instance of block");
					return;
				}else{
					canvas.addBlock(block);
					Workspace.getInstance().notifyListeners(new WorkspaceEvent(this, block.getBlockID(), WorkspaceEvent.BLOCK_ADDED));
					canvas.layoutBlocks();
					return;
				}
			}
		}
		this.printError("Drawer not found: "+drawer);
		return;
	}
	public void addDynamicBlock(RenderableBlock block, String drawer) {
		for(FactoryCanvas canvas : this.dynamicCanvases){
			if (canvas.getName().equals(drawer)){
				if(block == null || Block.NULL.equals(block.getBlockID())){
					printError("Attempting to add a null instance of block");
					return;
				}else{
					canvas.addBlock(block);
					Workspace.getInstance().notifyListeners(new WorkspaceEvent(this, block.getBlockID(), WorkspaceEvent.BLOCK_ADDED));
					canvas.layoutBlocks();
					return;
				}
			}
		}
		this.printError("Drawer not found: "+drawer);
		return;
	}
	/**
	 * Add blocks to drawer if drawer can be found.  Add graphically
	 * and alos throw event.  Do nothing if no drawer if specified
	 * name is found.
	 * 
	 * @param blocks
	 * @param drawer
	 */
	public void addStaticBlocks(Collection<RenderableBlock> blocks, String drawer){
		//find canvas
		for(FactoryCanvas canvas : this.staticCanvases){
			if (canvas.getName().equals(drawer)){
				for(RenderableBlock block : blocks){
		        	if(block == null || Block.NULL.equals(block.getBlockID())) continue;
		            canvas.addBlock(block);
		            Workspace.getInstance().notifyListeners(new WorkspaceEvent(this, block.getBlockID(), WorkspaceEvent.BLOCK_ADDED));
					
				}
				canvas.layoutBlocks();
				return;
			}
		}
		this.printError("Drawer not found: "+drawer);
		return;
	}
	public void addDynamicBlocks(Collection<RenderableBlock> blocks, String drawer){
		//find canvas
		for(FactoryCanvas canvas : this.dynamicCanvases){
			if (canvas.getName().equals(drawer)){
				for(RenderableBlock block : blocks){
		        	if(block == null || Block.NULL.equals(block.getBlockID())) continue;
		            canvas.addBlock(block);
		            Workspace.getInstance().notifyListeners(new WorkspaceEvent(this, block.getBlockID(), WorkspaceEvent.BLOCK_ADDED));
					
				}
				canvas.layoutBlocks();
				return;
			}
		}
		this.printError("Drawer not found: "+drawer);
		return;
	}
	
	/**
	 * Adds the specified RenderableBlocks to the drawer with the specified drawerName.  Do nothing
	 * if the drawer with the specified drawerName does not exist.
	 * @param blocks Collection of RenderableBlocks to the drawer with name: drawerName
	 * @param drawerName String name of the drawer to add blocks to
	 */
	public void addSubsetBlocks(Collection<RenderableBlock> blocks, String drawerName) {
		//find canvas //TODO generalize the following code to one method where you pass in the blocks, drawername, and canvas types
		for(FactoryCanvas canvas : this.subsetCanvases) {
			if(canvas.getName().equals(drawerName)) {
				for(RenderableBlock block : blocks) {
					if(block == null || Block.NULL.equals(block.getBlockID())) continue;
					canvas.addBlock(block);
					Workspace.getInstance().notifyListeners(new WorkspaceEvent(this, block.getBlockID(), WorkspaceEvent.BLOCK_ADDED));
					
				}
				canvas.layoutBlocks();
				return;
 			}
		}
		this.printError("Drawer not found: "+drawerName);
		return;
	}
	
	/**
	 * Removes block from specified drawer.  DO nothing if no drawer is found
	 * with specified name.
	 * @param block
	 * @param drawer
	 */
    public void removeStaticBlock(RenderableBlock block, String drawer) {
		//find canvas
		for(FactoryCanvas canvas : this.staticCanvases){
			if (canvas.getName().equals(drawer)){
				canvas.removeBlock(block);
				//DO NOT THROW AN EVENT FOR REMOVING DRAWER BLOCKS!!!
				//Workspace.getInstance().notifyListeners(new WorkspaceEvent(FactoryManager.this, block.getBlockID(), WorkspaceEvent.BLOCK_REMOVED));
				canvas.layoutBlocks();
				return;
			}
		}
		this.printError("Drawer not found: "+drawer);
		return;
    }
    public void removeDynamicBlock(RenderableBlock block, String drawer) {
		//find canvas
		for(FactoryCanvas canvas : this.dynamicCanvases){
			if (canvas.getName().equals(drawer)){
				canvas.removeBlock(block);
				//DO NOT THROW AN EVENT FOR REMOVING DRAWER BLOCKS!!!
				//Workspace.getInstance().notifyListeners(new WorkspaceEvent(FactoryManager.this, block.getBlockID(), WorkspaceEvent.BLOCK_REMOVED));
				canvas.layoutBlocks();
				return;
			}
		}
		this.printError("Drawer not found: "+drawer);
		return;
    }
    
	//////////////////
    //Widget Methods//
    //////////////////
    //documentation found in WorkspaceWidgets.java
    
    public void blockEntered(RenderableBlock block) {}
    public void blockExited(RenderableBlock block) {}
    public void blockDragged(RenderableBlock block) {}
    public void removeBlock(RenderableBlock block) {}
    public void addBlock(RenderableBlock block){}
    public void addBlocks(Collection<RenderableBlock> blocks){}
    public void blockDropped(RenderableBlock block) {
        //remove block
        WorkspaceWidget oldParent = block.getParentWidget();
        if(oldParent != null)
            oldParent.removeBlock(block);
        
        Container parent = block.getParent();
        if(parent != null){
            parent.remove(block);
            parent.validate();
            parent.repaint();
            block.setParentWidget(null);
        }
        
        //fire to workspace that block was removed
        //DO FIRE AN EVENT IF BLOCK IS REMOVED BY USER!!!!
        //NOTE however that we do not throw na event for adding internally
        Workspace.getInstance().notifyListeners(new WorkspaceEvent(this, block.getBlockID(), WorkspaceEvent.BLOCK_REMOVED));
    }
    public JComponent getJComponent() {
    	return this.navigator.getJComponent();
    }
    public JComponent getFactorySwitcher(){
    	return this.factorySwicther;
    }
    public boolean contains(int x, int y){
    	return this.navigator.getJComponent().contains(x, y);
    }
    public void workspaceEventOccurred(WorkspaceEvent event){
    	//THIS ENTIRE METHOD IS A HACK!
    	//PLEASE CHANGE WITH CAUTION
    	//IT DOES SOME PREETY STRANGE THINGS
    	if(event.getEventType() == WorkspaceEvent.BLOCK_ADDED){
        	if(event.getSourceWidget() instanceof Page){
        		Page page = (Page)event.getSourceWidget();
        		Block block = Block.getBlock(event.getSourceBlockID());
        		//block may not be null if this is a block added event
        		if(block.hasStubs()){
                    for(BlockStub stub : block.getFreshStubs()){
                        this.addDynamicBlock(
                        		new FactoryRenderableBlock(this, stub.getBlockID()),
                        		page.getPageDrawer());
                    }
                }
        	}
    	}else if(event.getEventType() == WorkspaceEvent.BLOCK_REMOVED){
    		//may not be removing a null stanc eof block, so DO NOT check for it
    		Block block = Block.getBlock(event.getSourceBlockID());
            if(block.hasStubs()){
            	for(Long stub : BlockStub.getStubsOfParent(block.getBlockID())){
                	RenderableBlock rb = RenderableBlock.getRenderableBlock(stub);
                	if(rb != null && !rb.getBlockID().equals(Block.NULL) && 
                			rb.getParentWidget() != null && rb.getParentWidget().equals(this)){
                		//rb.getParent() should not be null
                		rb.getParent().remove(rb);
                		rb.setParentWidget(null);
                		
                	}
                }
            }
            this.relayoutBlocks();
    	}else if(event.getEventType() == WorkspaceEvent.BLOCK_MOVED){
    		Block block = Block.getBlock(event.getSourceBlockID());
    		if(block != null && block.hasStubs()){
            	for(Long stub : BlockStub.getStubsOfParent(block.getBlockID())){
                	RenderableBlock rb = RenderableBlock.getRenderableBlock(stub);
                	if(rb != null && !rb.getBlockID().equals(Block.NULL) && 
                			rb.getParentWidget() != null && rb.getParentWidget().equals(this)){
                		//rb.getParent() should not be null
                		rb.getParent().remove(rb);
                		rb.setParentWidget(null);
                		
                	}
                }
                this.relayoutBlocks();
            }
    	
    	}else if(event.getEventType() == WorkspaceEvent.PAGE_RENAMED){
    		//this.relayoutBlocks();
    	}
    }
    
    public String toString(){
    	return "FactoryManager: "+this.navigator.getJComponent();
    }
}