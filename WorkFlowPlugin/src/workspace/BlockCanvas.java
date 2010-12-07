package workspace;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.PopupMenu;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.BoundedRangeModel;
import javax.swing.JComponent;
import javax.swing.JLayeredPane;
import javax.swing.SwingUtilities;

import org.w3c.dom.Element;

import renderable.RenderableBlock;
import codeblockutil.CGraphite;
import codeblockutil.CHoverScrollPane;
import codeblockutil.CScrollPane;
import codeblockutil.CScrollPane.ScrollPolicy;


/**
 * A BlockCanvas is a container of Pages and is a scrollable 
 * panel.  When a page is added to a BlockCanvas, that 
 * particular new page must be added to both the data 
 * structure holding the set of pages and the scrollable 
 * panel that renders the page.
 * 
 * A BlockCanvas is also a PageChangeListener.  When any 
 * pages are changed, the Blockcanvas must update itself 
 * appropriately to reflect this change.
 * 
 * As of the current implementation, the BlockCanvas must 
 * have at least one Page when it becomes visible (that is,
 * when it’s viewable JComponent becomes visible).
 */
public class BlockCanvas implements PageChangeListener, ISupportMemento  {
	/** serial version ID */
	private static final long serialVersionUID = 7458721329L;
	/** the collection of pages that this BlockCanvas stores */
	private List<Page> pages = new ArrayList<Page>();
	/** the collection of PageDivideres that this BlockCanvas stores */
	private List<PageDivider> dividers = new ArrayList<PageDivider>();
	/** The Swing representation of the page container */
	private JComponent canvas;
	/** The scrollable JComponent representing the graphical part of this BlockCanvas */
	private CScrollPane scrollPane;

	//////////////////////////////
	//Constructor/Destructor	//
	//////////////////////////////
    
    /**
     * Constructs BlockCanvas and subscribes
     * this BlockCanvas to PageChange events
     */
    public BlockCanvas() {
    	this.canvas = new Canvas();
    	this.scrollPane = new CHoverScrollPane(canvas,
    			ScrollPolicy.VERTICAL_BAR_ALWAYS,
    			ScrollPolicy.HORIZONTAL_BAR_ALWAYS,
    			18,CGraphite.blue, null);
    	scrollPane.setScrollingUnit(5);
        canvas.setLayout(null);
        canvas.setBackground(Color.gray);  
        canvas.setOpaque(true);
        PageChangeEventManager.addPageChangeListener(this);
    }
    
    /**
     * @effects resets BlockCanvas by removing all pages, dividers, and blocks.
     */
    public void reset(){
        pages.clear();
        canvas.removeAll();
        dividers.clear();
        scrollPane.revalidate();
    }
    
	//////////////////////////////
	//Rendering View Accessor	//
	//////////////////////////////
    
    /** @returns X Coordinate of BlockCanvas graphical representation */
    public int getX(){
    	return scrollPane.getX();
    }
    /** @returns Y coordinate of BlockCanvas graphical representation */
    public int getY(){
    	return scrollPane.getY();
    }
    /** @returns width of BlockCanvas graphical representation */
    public int getWidth(){
    	return scrollPane.getWidth();
    }
    /** @returns height of BlockCanvas graphical representation */
    public int getHeight(){
    	return scrollPane.getHeight();
    }
    /** @returns vertical scroll bar bounding range model.  MAY BE NULL */
	public BoundedRangeModel getVerticalModel(){
		return scrollPane.getVerticalModel();
	}
    /** @returns horizontal scroll bar bounding range model.  MAY BE NULL */
	public BoundedRangeModel getHorizontalModel(){
		return scrollPane.getHorizontalModel();
	}
    /** 
     * @returns the Swing Container that holds all the graphical panels of
     * 			all the pages in this Blockcanvas
     */
    public JComponent getCanvas(){
    	return this.canvas;
    }
    /**
     * @returns JComponent representation of this
     * @warning Please take special care in useing this method, as it exposes
     * 			implementation details.
     */
    public JComponent getJComponent() {
    	return scrollPane;
    }
    /** @returns string representation of this */
    public String toString(){
        return "BlockCanvas "+pages.size()+" pages.";
    }
    
    public List<Page> getLeftmostPages(int left){
    	List<Page> leftmostPages = new ArrayList<Page>();
    	int scrollPosition = this.scrollPane.getHorizontalModel().getValue();
    	int pagePosition = 0;
    	for (Page p: this.pages){
    		pagePosition+=p.getJComponent().getWidth();
    		if (pagePosition>=scrollPosition){
    			if (pagePosition-p.getJComponent().getWidth()-scrollPosition<=left-10){
    				leftmostPages.add(p);
    			}
    		}
    	}
    	return leftmostPages;
    }
    
	//////////////////////////////
	//Block Mutators/Accessors	//
	//////////////////////////////
    
    /**
     * @return the RendearbleBlocks that are contained within this widget
     * 			or an empty Iterable if no blocks exists
     */
    public Iterable<RenderableBlock> getBlocks() {
        ArrayList<RenderableBlock> allPageBlocks = new ArrayList<RenderableBlock>();
        for(Page p: pages){
            allPageBlocks.addAll(p.getBlocks());
        }
        return allPageBlocks;
    }  
    /**
     * @effects Automatically arranges all the blocks within this.
     */
    public void arrangeAllBlocks(){
    	for(Page page : pages){
           	page.reformBlockOrdering();
        }
    }
	/**
	 * @return a collection of top level blocks within this page (blocks with no
	 * 			parents that and are the first block of each stack) or an empty
	 * 			collection if no blocks are found on this page.
	 */
    public Iterable<RenderableBlock> getTopLevelBlocks(){
        return null;
    }
    /**
     * @param block - the RenderableBlock to make sure is shown in the viewport
     * @requires block ! null
     * @modifies the vertical and horizontal scrollbal boundedRangeModel
     * @effects This method causes the workspace to scroll if needed
     * 			to complete show the given RenderableBlock.
     */
	public void scrollToShowBlock(RenderableBlock block) {
		//not yet implemented
	}
	public void scrollToComponent(JComponent c) {
		//not yet implemented
	}
	
	//////////////////////////////
	//Page Mutators/Accessors	//
	//////////////////////////////
    
    /**
     * @returns the number of Pages.
     */
    public int numOfPages(){
        return pages.size();
    }
    /**
     * @param position - 0 is the left most position
     * 
     * @requires none
     * @return true if there exists a page at the specified position
     */
    public boolean hasPageAt(int position){
        return (position >= 0 && position < pages.size());
    }
    /**
     * @param position - 0 is the left most position
     * 
     * @requires none
     * @return page at position or null if non exists at position
     */
    protected Page getPageAt(int position){
        if(hasPageAt(position)){
        	return pages.get(position);
        }else{
        	return null;
        }
    }
    /**
     * @param name - name of page
     * 
     * @requires none
     * @return  FIRST page with matchng name (if more than
     * 			one page has matching name, it returns the first) or null
     * 			if no matching name exists.
     */
    public Page getPageNamed(String name) {
    		for (Page p : pages)
    			if (p.getPageName().equals(name))
    				return p;
    		return null;
    }
    /**
     * @return List of pages or an empty list if no pages exists
     */
    public List<Page> getPages(){
    	return new ArrayList<Page>(pages);
    }
    /**
     * @param page the page to add to the BlockCanvas
     * 
     * @requires page != null
     * @modifies this.pages
     * @effects Adds the given page to the rightmost side of the BlockCanvas
     */
    public void addPage(Page page) {
    	this.addPage(page, pages.size());
    }
    
    /**
     * @param page - page to be added
     * @param position - the index at which to add the page where 0 is rightmost
     * 
     * @requires none
     * @modifies this.pages
     * @effects Inserts the specified page at the specified position.
     * 			Shifts the element currently at that position (if any)
     * 			and any subsequent elements to the right (adds one to
     * 			their current position).
     * @throws RuntimeException if (position < 0 || position > pages.size() || page == null) 
     */
    public void addPage(Page page, int position) {
    	if(page == null){
    		throw new RuntimeException("Invariant Violated: May not add null Pages");
    	}else if(position<0 || position > pages.size()){
    		System.out.println(position+", "+pages.size());
    		throw new RuntimeException("Invariant Violated: Specified position out of bounds");
    	}
    	pages.add(position, page);
    	canvas.add(page.getJComponent(), 0);
    	PageDivider pd = new PageDivider(page);
    	dividers.add(pd);
    	canvas.add(pd,0);
    	PageChangeEventManager.notifyListeners();
     }
    
    /**
     * @param page - the page to be removed
     * 
     * @requires page != null
     * @modifies this.pages
     * @effects Removes the given Page from the BlockCanvas.
     * 			If specified page not found in BlockCanvas, do nothing.
     * 			if more than one page equals the specified page, then remove
     * 			the first equal() instance.
     */
    public Page removePage(Page page) {
    	if (page != null){
	    	// clear the blocks from the page and remove it internally
	    	page.clearPage();
	    	pages.remove(page);
	    	
	    	// remove the pageDivider for this page too
	    	for (PageDivider div: dividers) {
	    		if (div.getLeftPage() == page) {
	    			dividers.remove(div);
	    			canvas.remove(div);
	    			break;
	    		}
	    	}
	    	
	    	// remove the page from the canvas and revalidate so it looks okay
	    	canvas.remove(page.getJComponent());
	    	canvas.revalidate();
	    	canvas.repaint();
	    	PageChangeEventManager.notifyListeners();
    	}
        return page;
    }
    
    /**
     * @param position - 0 is the left most page
     * 
     * @requires none
     * @returns the page that was removed or null if non was removed.
     * @modifies this.pages
     * @effects If the position is within bounds, then remove
     * 			the page located at the specified position.
     * 			Do nothing if the position is out of bounds.
     */
    public Page removePage(int position) {
    	if(this.hasPageAt(position)){
    		return removePage(pages.get(position));
    	}else{
    		return null;
    	}
    }
    
    /**
     * @param page the desired page to switch view to
     * 
     * @requries page != null
     * @modifies the ghorizontal boundedrangemodel of this blockcanvas
     * @effects Switches the canvas view to the specified page.
     */
    public void switchViewToPage(Page page){
        scrollPane.getHorizontalModel().setValue(page.getJComponent().getX());
    }
    
    /**
     * @param oldName - the original name of the page
     * @param newName - the String name to rename the page to
     * 
     * @requires oldName != null && newName != null
     * @return 	If a matching page was found, return the renamed Page.
     * 			Otherwise, return null.
     * @modifies the page with the matching oldName
     * @effects Renames the page with the specified oldName to the newName.
     */
    public Page renamePage(String oldName, String newName){
        for(Page page : pages){
            if(page.getPageName().equals(oldName)){
                page.setPageName(newName);
                update();
                return page;
            }
        }
        return null;
    }
    
	////////////////////////////////
	//PageChangeListener Interface//
	////////////////////////////////
    
    /** @override PageChangeListener.update() */
    public void update() {
    	this.reformBlockCanvas(); // just repaint and it'll all look right again
    }
    /**
     * @modifies every page in this blockcanvas as well as the canvas
     * @effects resynchronize model and view, resize, reposition, and set color
     * 			of EVERY page in the BlockCanvas.  Then move very divider to
     * 			the far right side of it's corresponding page.  Note that
     * 			reforming must perform ALL FIVE ACTIONS when invoked.
     */
    public void reformBlockCanvas(){
		int widthCounter = 0;
 		for (int i = 0; i<pages.size() ; i++) {
 			Page p = pages.get(i);
 			if(p.getDefaultPageColor() == null){
 	            if (i % 2 == 1) {
 	                p.setPageColor(new Color(30,30,30));
 	            } else {
 	            	p.setPageColor(new Color(40,40,40));
 	            }
 			}else{
 				p.setPageColor(p.getDefaultPageColor());
 			}
    		widthCounter = widthCounter + p.reformBounds(widthCounter);
 		}
 		for (PageDivider d: dividers) {
 			d.setBounds(
 					d.getLeftPage().getJComponent().getX()+d.getLeftPage().getJComponent().getWidth()-3,
 					0,
 					5,
 					d.getLeftPage().getJComponent().getHeight());
 		}
 		canvas.setPreferredSize(new Dimension(widthCounter,(int)(Page.DEFAULT_ABSTRACT_HEIGHT*Page.getZoomLevel())));
		scrollPane.revalidate();
		scrollPane.repaint();
    }
    
	//////////////////////////////
	//Saving and Loading		//
	//////////////////////////////

    /**
     * Returns an XML String describing all the blocks and pages within 
     * the BlockCanvas
     */
    public String getSaveString(){
        StringBuffer saveString = new StringBuffer();
        
        //get save string of all pages
        if(pages.size() > 0){  //TODO ria just do BLOCKS, CHECK OUT HOW SAVING WILL BE LIKE WITH REFACTORING
            saveString.append("<Pages>"); //should we include drawer-with-page flag?
            for(Page page : pages){
                saveString.append(page.getSaveString());
            }
            saveString.append("</Pages>");
        }
        return saveString.toString();
    }
    
    /**
     * Loads all the RenderableBlocks and their associated Blocks that 
     * reside within the block canvas.  All blocks will have their nessary
     * data populated including connection information, stubs, etc.
     * Note: This method should only be called if this language only uses the 
     * BlockCanvas to work with blocks and no pages. Otherwise, workspace live blocks 
     * are loaded from Pages.
     * @param root the Document Element containing the desired information
     */
    protected void loadSaveString(Element root){
        //Extract canvas blocks and load

        //load pages, page drawers, and their blocks from save file
        //PageDrawerManager.loadPagesAndDrawers(root);
    	PageDrawerLoadingUtils.loadPagesAndDrawers(root, Workspace.getInstance().getFactoryManager());
        int screenWidth = java.awt.Toolkit.getDefaultToolkit().getScreenSize().width;
        int canvasWidth = canvas.getPreferredSize().width;
        if(canvasWidth<screenWidth){
        	Page p = pages.get(pages.size()-1);
        	p.addPixelWidth(screenWidth-canvasWidth);
        	PageChangeEventManager.notifyListeners();
        }
	}
    
	//////////////////////////////
	//REDO/UNOD					//
	//////////////////////////////
	
	/** @override ISupportMomento.getState */
	public Object getState(){
		Map<String, Object> pageStates = new HashMap<String, Object>();
		for(Page page : pages){
			pageStates.put(page.getPageName(), page.getState());
        }
		return pageStates;
	}
	/** @override ISupportMomento.loadState() */
	@SuppressWarnings("unchecked")
	public void loadState(Object memento){
		assert (memento instanceof HashMap) : "ISupportMemento contract violated in BlockCanvas";
		if(memento instanceof HashMap){
			Map<String, Object> pageStates = (HashMap<String, Object>) memento;
			List<String> unloadedPages = new LinkedList<String>();
			List<String> loadedPages = new LinkedList<String>();
			
			for(String name : pageStates.keySet()){
				unloadedPages.add(name);
			}
			
			//First, load all the pages that are in the state to be loaded
			//against all the pages that already exist.
			for(Page existingPage : this.pages){
				String existingPageName = existingPage.getPageName();
				
				if(pageStates.containsKey(existingPageName)){
					existingPage.loadState(pageStates.get(existingPageName));
					unloadedPages.remove(existingPageName);
					loadedPages.add(existingPageName);
				}
			}
			
			//Now, remove all the pages that don't exist in the save state
			for(Page existingPage : this.pages){
				String existingPageName = existingPage.getPageName();
				
				if(!loadedPages.contains(existingPageName)){
					this.pages.remove(existingPage);
				}
			}
			
			//Finally, add all the remaining pages that weren't there before
			for(String newPageName : unloadedPages){
				Page newPage = new Page(newPageName);
				newPage.loadState(pageStates.get(newPageName));
				pages.add(newPage);
			}
		}
	} 
    /**
     * The graphical representation of the block canvas's Swng Container of pages.
     * Note that this is not the graphical scrollable JComponent that represents
     * the BlockCanvas.
     */
    public class Canvas extends JLayeredPane implements MouseListener, MouseMotionListener{
    	private static final long serialVersionUID = 438974092314L;
    	private Point p;
    	public Canvas() {
    		super();
    		this.p = null;
    		this.addMouseListener(this);
    		this.addMouseMotionListener(this);
    	}
    	public void mousePressed(MouseEvent e) {
    		p=e.getPoint();
    	}
    	public void mouseClicked(MouseEvent e) {
    		if(SwingUtilities.isRightMouseButton(e) || e.isControlDown()){
                //pop up context menu
                PopupMenu popup = ContextMenu.getContextMenuFor(BlockCanvas.this);
                this.add(popup);
                popup.show(this, e.getX(), e.getY());
            }
    	}
    	public void mouseDragged(MouseEvent e){
    		if(p == null){
    			//do nothing
    		}else{
    			BoundedRangeModel hModel = scrollPane.getHorizontalModel();
    			BoundedRangeModel vModel = scrollPane.getVerticalModel();
    			hModel.setValue(hModel.getValue()+(p.x-e.getX()));
    			vModel.setValue(vModel.getValue()+(p.y-e.getY()));
    		}
    	}
    	public void mouseReleased(MouseEvent e) {
    		this.p=null;
    	}
    	public void mouseMoved(MouseEvent e){}
    	public void mouseEntered(MouseEvent e) {}
    	public void mouseExited(MouseEvent e) {}
    }
}