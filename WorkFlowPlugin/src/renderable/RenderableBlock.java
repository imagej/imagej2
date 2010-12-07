package renderable;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.PopupMenu;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JToolTip;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import renderable.BlockImageIcon.ImageLocation;
import workspace.ContextMenu;
import workspace.FactoryManager;
import workspace.ISupportMemento;
import workspace.MiniMap;
import workspace.RBParent;
import workspace.SearchableElement;
import workspace.Workspace;
import workspace.WorkspaceEvent;
import workspace.WorkspaceWidget;
import codeblocks.Block;
import codeblocks.BlockConnector;
import codeblocks.BlockConnectorShape;
import codeblocks.BlockLink;
import codeblocks.BlockLinkChecker;
import codeblocks.BlockShape;
import codeblocks.BlockStub;
import codeblocks.InfixBlockShape;
import codeblocks.JComponentDragHandler;
import codeblocks.rendering.BlockShapeUtil;
import codeblockutil.CToolTip;
import codeblockutil.GraphicsManager;


/**
 * RenderableBlock is responsible for all graphical rendering of a code Block.  This class is also 
 * responsible for consuming all mouse and key events on itself.  Each RenderableBlock object is 
 * coupled with its associated Block object, and uses information maintained in Block to 
 * render the graphical block accordingly.
 */
public class RenderableBlock extends JComponent implements SearchableElement, MouseListener, MouseMotionListener, ISupportMemento, CommentSource {
	
	private static final long serialVersionUID = 1L;
	
	/*
	 * 
	 *The following may be null: parent, lastdragwidget, comment 
	 */
	////////////////////
	//STATIC FIELDS
	/** True if in debug mode */
	private static final boolean DEBUG = false;
	/** The maximum distance between blocks that are still considered nearby enough to link */
	private static final double NEARBY_RADIUS = 20.0;
	/** The alpha level while dragging - lower means more transparent */
	private static final  float DRAGGING_ALPHA = 0.66F;
	/** Mapping from blockID to the corresponding RenderableBlock instance */
	private static final Map<Long, RenderableBlock> ALL_RENDERABLE_BLOCKS= new HashMap<Long,RenderableBlock>();


	///////////////////////
	//COMPONENT FIELDS
	/** BlockID of this.  MAY BE Block.NULL */
	private final Long blockID;
	/** Parent workspace widget.  May be null */
	private WorkspaceWidget parent;
	/** The previous known workspacewidget this block was dragged over.  May be null */
	private WorkspaceWidget lastDragWidget = null;
	/** The comment of this.  May be null */
	private Comment comment=null;
	/**  set true when comment is added or removed from this block */
	private boolean commentLabelChanged = false;
	/**
	 * An internal JComponent whose functionality is independant of any other
	 * functionality. If the block widget is the largest component in the
	 * block, then the renderableblock's Shape is determined form the dimensions
	 * of this widget.  They should not be related to starlogo or codeblocks.  MAY BE NULL*/
	private JComponent blockWidget = null;

	/////////////////////////////////
	//RENDERING RELATED FIELDS
	/** Shape components used to draw this block's geometrical shape.  Includes:
	 * (1) the block shape which is an abstract outline of the shape,
	 * (2) the abstractBlockArea which is a filled in abstract shape,
	 * (3) blockArea which is a filled in pixel shape,
	 * (4) and the popupIconShape which is the popupicon of this block*/
	private BlockShape blockShape;
	private Area abstractBlockArea;
	private Area blockArea;
	/** static drawing area for unstable blocks.  MAY BE NULL */
	private BufferedImage buffImg = null;

	//////////////////////////////////////
	//Internal Managers
	/** HighlightManager that manages drawing of highlights around this block */
	private RBHighlightHandler highlighter;
	/** dragHandler keeps the block within the workspace area. It manages relocating the block. */
	private JComponentDragHandler dragHandler;

	////////////////////////
	//ATTRIBUTE FIELDS
	/** Binary atttributes of this RenderableBlocks:
	 * (1) popupIconVisible is true if the popup icon is visible,
	 * (2) isSearchResult is true if this block is being queried by search
	 * (3) isPickedUp is true if mousePressed was performed on this block,
	 * (4) dragging is true if mouseDragged was performed on this block at least once,
	 * (5) linkedDefArgsBefore is any default arguments were never attached
	 * (6) isLoading is true if RenderableBlock is still loading- Though its data may
	 * 				have loaded completely, it still may need other connected
	 * 				RenderableBlocks to finish loading as well.  In this
	 * 				case, isLoading would still be false */
	private boolean isSearchResult = false;
	private boolean pickedUp = false;
	private boolean dragging = false;
	private boolean linkedDefArgsBefore = false;
	private boolean isLoading = false;

	///////////////////////////
	//Sockets and Labels
	/** TODO: Documentation does not exist for these components.  Consult author*/
	private final NameLabel blockLabel;
	private final PageLabel pageLabel;
	private final ConnectorTag plugTag;
	private final ConnectorTag afterTag;
	private final ConnectorTag beforeTag;
	private List<ConnectorTag> socketTags = new ArrayList<ConnectorTag>();

	////////////////////////////////
	// Collapse Label
	private CollapseLabel collapseLabel;

	//////////////////////////////////
	//TO BE DEPRECATED
	private HashMap<ImageLocation, BlockImageIcon> imageMap = new HashMap<ImageLocation, BlockImageIcon>();

	// the values of the x and y coordinates of block when zoom = 1.0
    private double unzoomedX;
    private double unzoomedY;

	/**
	 * Constructs a new RenderableBlock instance with the specified parent WorkspaceWidget and 
	 * Long blockID of its associated Block
	 * @param parent the WorkspaceWidget containing this
	 * @param blockID Long Block id of associated with this
	 */
	public RenderableBlock(WorkspaceWidget parent, Long blockID){    
		this(parent, blockID, false);
	}

	/**
	 * Constructs a new RenderableBlock instance with the specified parent WorkspaceWidget and 
	 * Long blockID of its associated Block
	 * @param parent the WorkspaceWidget containing this
	 * @param blockID Long Block id of associated with this
	 * @param isLoading indicates if this block is still waiting for all information 
	 * needed to properly construct it
	 */
	private RenderableBlock(WorkspaceWidget parent, Long blockID, boolean isLoading){
		super();
		/*
		 * Sets whether focus traversal keys are enabled 
		 * for this Component. Components for which focus 
		 * traversal keys are disabled receive key events 
		 * for focus traversal keys.
		 */
		this.setFocusTraversalKeysEnabled(false);

		this.parent = parent;
		this.blockID = blockID;
		ALL_RENDERABLE_BLOCKS.put(this.blockID, this);

		//initialize block image map
		//note: must do this before updateBuffImg();
		for(BlockImageIcon img : getBlock().getInitBlockImageMap().values()){
			imageMap.put(img.getImageLocation(), new BlockImageIcon(img.getImageIcon(), 
					img.getImageLocation(), img.isEditable(), img.wrapText()));
			add(imageMap.get(img.getImageLocation()));
		}
		//set null layout so as to add blockLabels where ever we want
		setLayout(null);    

		dragHandler = new JComponentDragHandler(this); // set up drag handler delegate
		addMouseListener(this);
		addMouseMotionListener(this);


		//initialize tags, labels, and sockets:
		this.plugTag = new ConnectorTag(getBlock().getPlug());
		this.afterTag = new ConnectorTag(getBlock().getAfterConnector());
		this.beforeTag = new ConnectorTag(getBlock().getBeforeConnector());
		this.blockLabel = new NameLabel(getBlock().getBlockLabel(), BlockLabel.Type.NAME_LABEL, getBlock().isLabelEditable(), blockID);
		this.pageLabel = new PageLabel(getBlock().getPageLabel(), BlockLabel.Type.PAGE_LABEL, false, blockID);
		this.add(pageLabel.getJComponent());
		this.add(blockLabel.getJComponent(), 0);
		synchronizeSockets();

		// initialize collapse label
		if(getBlock().isProcedureDeclBlock() && (parent == null || !(parent instanceof FactoryManager))) {
			this.collapseLabel = new CollapseLabel(blockID);
			this.add(collapseLabel);
		}

		//form basic shape
		if(getBlock().isInfix())
			blockShape = new InfixBlockShape(this);
		else
			blockShape = new BlockShape(this);

		if(!isLoading){
			//reformBlockShape so as to update socket points to position labels and setBounds of this rb
			reformBlockShape();

			//to cache image upon instantiation, update buffered image here:
			updateBuffImg();
		}else{
			blockArea = new Area();
		}

		highlighter = new RBHighlightHandler(this);

		String blockDescription = getBlock().getBlockDescription();
		if(blockDescription != null){
			setBlockToolTip(getBlock().getBlockDescription().trim());
		}
		setCursor(dragHandler.getDragHintCursor());
	}



	/**
	 * Returns the Long id of this
	 * @return the Long id of this
	 */
	public Long getBlockID(){
		return blockID;
	}

	/**
	 * Returns the height of the block shape of this
	 * @return the height of the block shape of this
	 */
	public int getBlockHeight(){
		return blockArea.getBounds().height;
	}

	/**
	 * Returns the dimensions of the block shape of this
	 * @return the dimensions of the block shape of this
	 */
	public Dimension getBlockSize(){
		return blockArea.getBounds().getSize(); 
	}

	/**
	 * Returns the width of the block shape of this
	 * @return the width of the block shape of this
	 */
	public int getBlockWidth(){
		return blockArea.getBounds().width;
	}

	/**
	 * Returns the BlockShape instance representing this
	 * @return the BlockShape instance representing this
	 */
	public BlockShape getBlockShape(){
		return blockShape;
	}

	/**
	 * @return the abstractBlockArea
	 */
	Area getAbstractBlockArea() {
		return abstractBlockArea;
	}

	/**
	 * @param abstractBlockArea the abstractBlockArea to set
	 */
	void setAbstractBlockArea(Area abstractBlockArea) {
		this.abstractBlockArea = abstractBlockArea;
	}
    
    /**
     * Moves this component to a new location. The top-left corner of
     * the new location is specified by the <code>x</code> and <code>y</code>
     * parameters in the coordinate space of this component's parent.
     * @param x the <i>x</i>-coordinate of the new location's 
     *          top-left corner in the parent's coordinate space
     * @param y the <i>y</i>-coordinate of the new location's 
     *          top-left corner in the parent's coordinate space
     */
    public void setLocation(int x, int y) {
		int dx, dy;
		dx = x - getX();
		dy = y - getY();
    	super.setLocation(x,y);
		
		if (hasComment() && !(dx == x && dy == y )) {
			if (getComment().getParent() != getParent()) {
				getComment().setParent(getParent(), Workspace.DRAGGED_BLOCK_LAYER);
			}
			getComment().translatePosition(dx, dy);
		}		
    }
    

    /**
     * Moves this component to a new location. The top-left corner of
     * the new location is specified by point <code>p</code>. Point
     * <code>p</code> is given in the parent's coordinate space.
     * @param p the point defining the top-left corner 
     *          of the new location, given in the coordinate space of this 
     *          component's parent
     */
    public void setLocation(Point p) {
        setLocation(p.x, p.y);
    }


	/**
	 * Returns the width of the stroke used to draw the highlight.
	 * Note that the highlight will only appear half this width,
	 * so the overall width of the block + highlight will be
	 * blockWidth + highlightStrokeWidth.
	 * @return the width of the stroke used to draw the highlight.
	 */
	public int getHighlightStrokeWidth() {
		return RBHighlightHandler.HIGHLIGHT_STROKE_WIDTH;
	}

	/**
	 * Returns the bounds of the block stack of this, where this block
	 * is at the top of its stack (in other words, it does not take the 
	 * bounds of the blocks above it into account).
	 * @return the bounds of the block stack of this, where this block
	 * is at the top of its stack.
	 */
	public Rectangle getStackBounds(){
		return new Rectangle(this.getLocation(), calcStackDimensions(this));
	}

	/**
	 * Helper method to calculate the bounds of a stack.  For now this method naively traverses 
	 * through the entire stack of the specified RenderableBlock rb and calculates the bounds.
	 * @param rb the RenderableBlock to calculate the stack bounds of
	 * @return Dimensions of the stack of the specified rb
	 */
	private Dimension calcStackDimensions(RenderableBlock rb){
		if(rb.getBlock().getAfterBlockID() != Block.NULL){
			Dimension dim = calcStackDimensions(RenderableBlock.getRenderableBlock(rb.getBlock().getAfterBlockID()));
			return new Dimension(Math.max(rb.getBlockWidth() + rb.getMaxWidthOfSockets(rb.getBlockID()), 
					dim.width), 
					rb.getBlockHeight() + dim.height);
		}else
			return new Dimension(rb.getBlockWidth() + rb.getMaxWidthOfSockets(rb.blockID), 
					rb.getBlockHeight());
	}

	/**
	 * sets the label to belonging to this renderable block to
	 * editing state == true (editing mode)
	 */
	public void switchToLabelEditingMode(boolean highlighted){
		if(getBlock().isLabelEditable()){
			if(highlighted){
				this.blockLabel.setEditingState(true);
				this.blockLabel.highlightText();
			}else{
				this.blockLabel.setEditingState(true);
			}
		}
	}

	/**
	 * returns the blockWidget for this RenderableBlock
	 * @return
	 */
	JComponent getBlockWidget() {
		return blockWidget;
	}

	/**
	 * @return the dimension of the sole block widget in this block.
	 * 		   May NOT return null.
	 */
	public Dimension getBlockWidgetDimension(){
		if (this.blockWidget == null) {
			return new Dimension(0,0);
		}else{
			return this.blockWidget.getSize();
		}
	}

	/**
	 * @param blockWidget
	 * 
	 * @requires none
	 * @modifies this.blockWidget
	 * @effects sets block widget to the input argument "blockWidget"
	 * 			and revalidates the JComponent representation of renderableblock
	 */
	public void setBlockWidget(JComponent blockWidget){
		if(this.blockWidget != null){
			this.remove(this.blockWidget);
		}
		this.blockWidget=blockWidget;
		if(blockWidget != null){
			this.add(blockWidget);
		}
		this.revalidate();

	}

	/**
	 * Clears all renderable block instances and all
	 * block instances
	 */
	public static void reset(){
		//System.out.println("reseting all renderable blocks");
		ALL_RENDERABLE_BLOCKS.clear();
		BlockUtilities.reset();
		Block.reset();
		BlockStub.reset();
		System.gc();
	} 

	public JComponentDragHandler getDragHandler() {
		return dragHandler;
	}

	/**
	 * Returns the BlockImageIcon instance at the specified location; null if 
	 * no BlockImageIcon exists at that location
	 * @param location the ImageLocation of the desired BlockImageIcon
	 * @return the BlockImageIcon instance at the specified location; null if 
	 * no BlockImageIcon exists at that location
	 */
	public BlockImageIcon getImageIconAt(ImageLocation location){
		return imageMap.get(location);
	}

	///////////////////
	// LABEL METHODS //
	///////////////////

	/**
	 * Synchronizes this RenderableBlock's socket components (including tags, labels)
	 * with the associated Block's list of sockets.
	 * @effects for every socket in Block:
	 * 				(1) check/add corresponding tag structure,
	 * 				(2) check/add block label
	 * 			for every tag in Renderable:
	 * 				(1) delete any sockets not in Block
	 * @complexity 	Running time for n Block sockets
	 * 				and m Renderable tags: O(m+nm)=O(nm)
	 */
	private boolean synchronizeSockets(){
		boolean changed = false;
		List<ConnectorTag> newSocketTags = new ArrayList<ConnectorTag>();
		for(ConnectorTag tag : socketTags){
			if(tag.getLabel() != null){
				this.remove(tag.getLabel().getJComponent());
			}
		}
		for(int i = 0; i< getBlock().getNumSockets(); i++) {
			BlockConnector socket = getBlock().getSocketAt(i);
			ConnectorTag tag = this.getConnectorTag(socket);
			if(tag == null){
				tag = new ConnectorTag(socket);
				if( SocketLabel.ignoreSocket(socket) ) {
					tag.setLabel(null); //ignored sockets have no labels
				}else{
					SocketLabel label = new SocketLabel(socket, socket.getLabel(),BlockLabel.Type.PORT_LABEL,socket.isLabelEditable(),blockID);
					String argumentToolTip = getBlock().getArgumentDescription(i);
					if(argumentToolTip != null){
						label.setToolTipText(getBlock().getArgumentDescription(i).trim());
					}
					tag.setLabel(label);
					label.setZoomLevel(this.getZoom());
					label.setText(socket.getLabel());
					this.add(label.getJComponent());
					changed = true;
				}
			}else{
				SocketLabel label = tag.getLabel();
				if( !SocketLabel.ignoreSocket(socket)) {
					//ignored bottom sockets or sockets with label == ""
					if(label == null){
						label = new SocketLabel(socket, socket.getLabel(),BlockLabel.Type.PORT_LABEL,socket.isLabelEditable(),blockID);
						String argumentToolTip = getBlock().getArgumentDescription(i);
						if(argumentToolTip != null){
							label.setToolTipText(getBlock().getArgumentDescription(i).trim());
						}
						tag.setLabel(label);
						label.setText(socket.getLabel());
						this.add(label.getJComponent());
						changed = true;
					} else {
						label.setText(socket.getLabel());
						this.add(label.getJComponent());
						changed = true;
					}
					label.setZoomLevel(this.getZoom());
				}
			}
			newSocketTags.add(tag);
		}
		this.socketTags.clear();
		this.socketTags = newSocketTags;
		return changed;
	}

	/**
	 * Updates all the labels within this block.  Returns true if this update found any changed labels; false otherwise
	 * @return true if this update found any changed labels; false otherwise.
	 */
	private boolean synchronizeLabelsAndSockets(){
		boolean blockLabelChanged = getBlock().getBlockLabel() != null && !blockLabel.getText().equals(getBlock().getBlockLabel());
		boolean pageLabelChanged = getBlock().getPageLabel() != null && !pageLabel.getText().equals(getBlock().getPageLabel());
		boolean socketLabelsChanged = false;

		// If tag label isn't the same as socket label, synchronize.
		// If the block doesn't have an editable socket label, synchronize.
		//
		// Needed to not synchronize the socket if it is label editable so it doesn't synchronize when
		// it gains focus.
		//
		// May possibly be done better if synchronizeSockets is rewritten. It has to be written such that
		// it doesn't remove the sockets' JComponents/remake them. Currently relies on the synchronizeSockets()
		// call in getSocketPixelPoint(BlockConnector) to make sure the dimensions and number of sockets
		// are consistent.
		for(int i = 0; i< getBlock().getNumSockets(); i++) {
			BlockConnector socket = getBlock().getSocketAt(i);
			ConnectorTag tag = this.getConnectorTag(socket);
			if (tag != null) {
				if(tag.getLabel() != null){
					if (!tag.getLabel().getText().equals(socket.getLabel())){
						socketLabelsChanged = synchronizeSockets();
						break;
					}
				}
			}
			if (!socket.isLabelEditable()){
				socketLabelsChanged = synchronizeSockets();
				break;
			}
		}
		if(blockLabelChanged){
			blockLabel.setText(getBlock().getBlockLabel());
		}
		if(pageLabelChanged){
			pageLabel.setText(getBlock().getPageLabel());
		}
		if (blockLabelChanged || pageLabelChanged || socketLabelsChanged || commentLabelChanged) {
			reformBlockShape();
			commentLabelChanged = false;
		}
		if(BlockLinkChecker.hasPlugEquivalent(getBlock())){
			BlockConnector plug = BlockLinkChecker.getPlugEquivalent(getBlock());
			Block plugBlock = Block.getBlock(plug.getBlockID());
			if(plugBlock != null) {
				if (plugBlock.getConnectorTo(blockID) == null) {
					throw new RuntimeException("one-sided connection from "+getBlock().getBlockLabel()+" to "+Block.getBlock(blockID).getBlockLabel());
				}
				RenderableBlock.getRenderableBlock(plug.getBlockID()).updateSocketSpace(plugBlock.getConnectorTo(blockID), blockID, true);
			}
		}
		return false;
	}

	/**
	 * Determine the width necessary to accommodate for placed labels.  Used to 
	 * determine the minimum width of a block.
	 * @returns int pixel width needed for the labels
	 */
	public int accomodateLabelsWidth() {
		int maxSocketWidth = 0;
		int width = 0;

		for(ConnectorTag tag : socketTags){
			SocketLabel label = tag.getLabel();
			if(label != null) maxSocketWidth = Math.max(maxSocketWidth, label.getAbstractWidth());
		}
		if(getBlock().hasPageLabel()) {   
			width += Math.max(blockLabel.getAbstractWidth(), pageLabel.getAbstractWidth()) + maxSocketWidth;    
			width += getControlLabelsWidth();
		} else {
			width += blockLabel.getAbstractWidth() + maxSocketWidth;
			width += getControlLabelsWidth() + 4;
		}
		return width;
	}

	/**
	 * Returns the width of the page label on this block; if page label
	 * is not enabled and does not exist, returns 0.
	 * @return the width of the page label on this block iff page label 
	 * is enabled and exists; returns 0 otherwise.
	 */
	public int accomodatePageLabelHeight(){
		if(getBlock().hasPageLabel())
			return pageLabel.getAbstractHeight();
		else 
			return 0;
	}

	/**
	 * Sets all the labels of this block as uneditable block labels.
	 * Useful for Factory blocks.   
	 */
	public void setBlockLabelUneditable(){
		blockLabel.setEditable(false);
	}

	////////////////////////////////////////
	// BLOCK IMAGE MANAGEMENT AND METHODS //
	////////////////////////////////////////

	/**
	 * Returns the total height of all the images to draw on this block
	 * @return the total height of all the images to draw on this block
	 */
	public int accomodateImagesHeight(){
		int maxImgHt = 0;
		for(BlockImageIcon img : getBlock().getInitBlockImageMap().values()){
			maxImgHt += img.getImageIcon().getIconHeight();
		}
		return maxImgHt;
	}

	/**
	 * Returns the total width of all the images to draw on this block
	 * @return the total width of all the images to draw on this block
	 */
	public int accomodateImagesWidth(){
		int maxImgWt = 0;
		for(BlockImageIcon img : getBlock().getInitBlockImageMap().values()){
			maxImgWt += img.getImageIcon().getIconWidth();
		}
		return maxImgWt;
	}

	//////////////////////////////////////////////
	//BLOCK LINKING CHECKS ON OTHER RENDERABLES //
	//////////////////////////////////////////////

	/**
	 * Looks for links between this RenderableBlock and others.
	 * @return a BlockLink object with information on the closest possible linking between this RenderableBlock and another.
	 */ 
	public BlockLink getNearbyLink(){
		return BlockLinkChecker.getLink(this, Workspace.getInstance().getBlockCanvas().getBlocks());
	}

	///////////////////////
	/// SOCKET METHODS ////
	///////////////////////

	/**
	 * Returns the maximum width between all the socket connectors of this or 0 if this does not 
	 * have any sockets 
	 * @return the maximum width between all the socket connectors of this or 0 if this does not 
	 * have any sockets
	 */
	public int getMaxSocketShapeWidth() {
		int maxSocketWidth = 0;
		for(BlockConnector socket: getBlock().getSockets()) {
			int socketWidth = BlockConnectorShape.getConnectorDimensions(socket).width;
			if( socketWidth > maxSocketWidth) {
				maxSocketWidth = socketWidth;
			}
		}

		return maxSocketWidth;
	}

	/**
	 * Returns a new Point object that represents the pixel location of this socket's center.
	 * Mutating the new Point will not affect future calls to getSocketPoint; that is, this
	 * method clones a new Point object.  The new Point object MAY NOT BE NULL.
	 * 
	 * @param socket - the socket whose point we want.  socket MAY NOT BE NULL.
	 * @return a Point representing the socket's center
	 * @requires socket != null and socket is one of this block's socket
	 */
	public Point getSocketPixelPoint(BlockConnector socket) {
		ConnectorTag tag = this.getConnectorTag(socket);
		if (tag != null)
			return tag.getPixelLocation();
		
		System.out.println("Error, Socket has no connector tag: " + socket);
		return new Point(0,-100); //JBT hopefully this doesn't hurt anything,  this is masking a bug that needs to be tracked down, why is the connector tag missing? 
	}
	/**
	 * Returns a new Point object that represents the abstract location of this socket's center.
	 * Mutating the new Point will not affect future calls to getSocketPoint; that is, this
	 * method clones a new Point object.  The new Point object MAY NOT BE NULL.
	 * 
	 * @param socket - the socket whose point we want.  socket MAY NOT BE NULL.
	 * @return a Point representing the socket's center
	 * @requires socket != null and socket is one of this block's socket
	 */
	public Point getSocketAbstractPoint(BlockConnector socket) {
		ConnectorTag tag = this.getConnectorTag(socket);
		return tag.getAbstractLocation();
	}
	/**
	 * Updates the center point location of this socket
	 * 
	 * @param socket - the socket whose point we will update.  Socket MAY NOT BE NULL
	 * @param point - the ABSTRACT location of socket's center.  ABSTRACT LOCATION!!!
	 * 
	 * @requires socket != null and there exist a matching tag for the socket
	 */
	public void updateSocketPoint(BlockConnector socket, Point2D point) {
		ConnectorTag tag = this.getConnectorTag(socket);
		//TODO: what if tag does not exist?  should we throw exception or add new tag?
		tag.setAbstractLocation(point);
		
	}

	/**
	 * Updates the renderable block with the underlying block's before, 
	 * after, and plug connectors. 
	 */
	public void updateConnectors() {
		Block b = Block.getBlock(blockID);
		afterTag.setSocket(b.getAfterConnector());
		beforeTag.setSocket(b.getBeforeConnector());
		plugTag.setSocket(b.getPlug());
	}

	/////////////////////////////////////
	// PARENT WORKSPACE WIDGET METHODS //
	/////////////////////////////////////

	/**
	 * Returns the parent WorkspaceWidget containing this
	 * @return the parent WorkspaceWidget containing this
	 */
	public WorkspaceWidget getParentWidget(){
		return parent;
	}

	/**
	 * Sets the parent WorkspaceWidget containing this
	 * @param widget the desired WorkspaceWidget
	 */
	public void setParentWidget(WorkspaceWidget widget){
		parent = widget;
	}

	/**
	 * Overriding JComponent.contains(int x, int y) so that this component's 
	 * boundaries are defined by the actual area occupied by the Renderable
	 * Block shape.  Returns true iff the specified coordinates are contained 
	 * within the area of the BlockShape.
	 * @return true iff the specified coordinates are contained within the Area
	 * of the BlockShape
	 */
	public boolean contains(int x, int y) {
		return blockArea.contains(x, y);
	}


	//////////////////////
	// BLOCK MANAGEMENT //
	//////////////////////
	/**
	 * Shortcut to get block with current BlockID of this renderable block.
	 */
	public Block getBlock() {
		return Block.getBlock(blockID);
	}  

	public Color getBLockColor(){
		return getBlock().getColor();
	}

	/**
	 * Links the default arguments of this block if it has any and if this block has not already linked its 
	 * default args in this session.  Re-linking this block's default args everytime it gets dropped/moved 
	 * within the block canvas can get annoying.  
	 */
	/*    public void linkDefArgs(){
        if(!linkedDefArgsBefore && getBlock().hasDefaultArgs()){
            Iterator<Long> ids = getBlock().linkAllDefaultArgs().iterator();
            Long id; 
            while(ids.hasNext()){
                id = ids.next(); 
                if(!Block.NULL.equals(id)){
                    RenderableBlock arg = new RenderableBlock(this.getParentWidget(), id);
                    getParentWidget().addBlock(arg);
        			Workspace.getInstance().notifyListeners(new WorkspaceEvent(getParentWidget(), arg.getBlockID(), WorkspaceEvent.BLOCK_ADDED, true));
                }
            }
            this.moveConnectedBlocks();
            linkedDefArgsBefore = true;
        }
    }*/
	public void linkDefArgs(){
		if(!linkedDefArgsBefore && getBlock().hasDefaultArgs()){
			Iterator<Long> ids = getBlock().linkAllDefaultArgs().iterator();
			Iterator<BlockConnector> sockets = getBlock().getSockets().iterator();
			Long id; 
			BlockConnector socket;

			// Store the ids, sockets, and blocks we need to update.
			List<Long> idList = new ArrayList<Long>();
			List<BlockConnector> socketList = new ArrayList<BlockConnector>();
			List<RenderableBlock> argList = new ArrayList<RenderableBlock>();
			while(ids.hasNext() && sockets.hasNext()){
				id = ids.next(); 
				socket = sockets.next();
				if(id != Block.NULL){
					//for each block id, create a new RenderableBlock
					RenderableBlock arg = new RenderableBlock(this.getParentWidget(), id);
					arg.setZoomLevel(this.zoom);
					//getParentWidget().addBlock(arg);
					//arg.repaint();
					//this.getParent().add(arg);
					//set the location of the def arg at
					Point myLocation = getLocation();
					Point2D socketPt = getSocketPixelPoint(socket);
					Point2D plugPt = arg.getSocketPixelPoint(arg.getBlock().getPlug());
					arg.setLocation((int)(socketPt.getX()+myLocation.x-plugPt.getX()), (int)(socketPt.getY()+myLocation.y-plugPt.getY()));
					//update the socket space of at this socket
					this.getConnectorTag(socket).setDimension(new Dimension(
							arg.getBlockWidth()-(int)BlockConnectorShape.NORMAL_DATA_PLUG_WIDTH,
							arg.getBlockHeight()));
					//drop each block to this parent's widget/component
					//getParentWidget().blockDropped(arg);
					getParentWidget().addBlock(arg);

					idList.add(id);
					socketList.add(socket);
					argList.add(arg);
				}
			}

			int size = idList.size();
			for (int i = 0; i < size; i++) {
				Workspace.getInstance().notifyListeners(
						new WorkspaceEvent(this.getParentWidget(), 
								argList.get(i).getBlockID(), 
								WorkspaceEvent.BLOCK_ADDED, true));

				//must call this method to update the dimensions of this
				//TODO ria in the future would be good to just link the default args
				//but first creating a block link object and then connecting
				//something like notifying the renderableblock to update its dimensions will be 
				//take care of
				this.blockConnected(socketList.get(i), idList.get(i));
				argList.get(i).repaint();
			}
			this.redrawFromTop();
			linkedDefArgsBefore = true;
		}
	}

	/**
	 * Modifies this RenderableBlock such that default
	 * arguments are ignored.  In the future, invoking
	 * this.linkDefArgs() will trigger no action.
	 * 
	 * @requires none
	 * @modifies this.linkedDefArgsBefore;
	 * @effects sets linkedDefArgsBefore to false;
	 */
	public void ignoreDefaultArguments(){
		linkedDefArgsBefore = true;
	}


	/**
	 * Returns the Renderable specified by blockID; null if RenderableBlock does not exist
	 * @param blockID the block id of the desired RenderableBlock
	 * @return the Renderable specified by blockID; null if RenderableBlock does not exist
	 */ 
	public static RenderableBlock getRenderableBlock(Long blockID){
		return ALL_RENDERABLE_BLOCKS.get(blockID);
	}

	////////////////////////
	//// BLOCK RESIZING ////
	////////////////////////

	/**
	 * Returns the dimension associated with a socket.  If a socket dimension has not yet
	 * been set, this will return null.
	 */
	public Dimension getSocketSpaceDimension(BlockConnector socket) {
		if(this.getConnectorTag(socket)==null)
			return null;
		else
			return this.getConnectorTag(socket).getDimension();
	}
	/**
	 * Updates the socket socket space of the specified connectedSocket of this after a block
	 * connection/disconnection.  The socket space specifies the dimensions of the block
	 * with id connectedToBlockID. RenderableBlock will use these dimensions to 
	 * determine the appropriate bounds to stretch the connectedSocket by.
	 * @param connectedSocket BlockConnector which block connection/disconnection occurred
	 * @param connectedToBlockID the Long block ID of the block connected/disconnected to the specified connectedSocket
	 * @param isConnected boolean flag to determine if a block connected or disconnected to the connectedSocket
	 */
	private void updateSocketSpace(BlockConnector connectedSocket, long connectedToBlockID, boolean isConnected){
		//System.out.println("updating socket space of :" + connectedSocket.getLabel() +" of rb: "+this);
		if(!isConnected){
			//remove the mapping
			this.getConnectorTag(connectedSocket).setDimension(null);

		}else{
//			Block connectedToBlock = Block.getBlock(connectedToBlockID);
			//if no before block, then no recursion
			//if command connector with position type bottom (just a control connector socket)
			// and we have a before, then skip and recurse up
			if(getBlock().getBeforeBlockID() != Block.NULL
					&& BlockConnectorShape.isCommandConnector(connectedSocket) 
					&& connectedSocket.getPositionType() == BlockConnector.PositionType.BOTTOM) {

				//get before connector
				Long beforeID = getBlock().getBeforeBlockID();
				BlockConnector beforeSocket = Block.getBlock(beforeID).getConnectorTo(getBlockID());
				RenderableBlock.getRenderableBlock(beforeID).updateSocketSpace(beforeSocket, getBlockID(), true);
				return;
			}

			//if empty before socket, then return
			//if(getBlock().hasBeforeConnector() && getBlock().getBeforeBlockID() == Block.NULL) return;

			//add dimension to the mapping
			this.getConnectorTag(connectedSocket).setDimension(calcDimensionOfSocket(connectedSocket));
		}

		//reform shape with new socket dimension
		reformBlockShape();
		//next time, redraw with new positions and moving children blocks
		clearBufferedImage();

		//after everything on this block has been updated, recurse upward if possible
		BlockConnector plugEquiv = BlockLinkChecker.getPlugEquivalent(getBlock());
		if (plugEquiv != null && plugEquiv.hasBlock()) {
			Long plugID = plugEquiv.getBlockID();
			BlockConnector socketEquiv = Block.getBlock(plugID).getConnectorTo(getBlockID());
			//update the socket space of a connected before/parent block
			RenderableBlock.getRenderableBlock(plugID).updateSocketSpace(socketEquiv, getBlockID(), true);
		}
	}

	/**
	 * Calculates the dimensions at the specified socket
	 * @param socket BlockConnector to calculate the dimension of
	 * @return Dimension of the specified socket 
	 */
	private Dimension calcDimensionOfSocket(BlockConnector socket){
		Dimension finalDimension = new Dimension(0,0);
		long curBlockID = socket.getBlockID();
		while(curBlockID != Block.NULL) {
			Block curBlock = Block.getBlock(curBlockID);
			//System.out.println("evaluating block :" + curBlock.getBlockLabel());
			RenderableBlock curRenderableBlock = RenderableBlock.getRenderableBlock(curBlockID);
			Dimension curRBSize = curRenderableBlock.getBlockSize();

			//add height
			finalDimension.height += curRBSize.height;
			//subtract after plug
			if(curBlock.hasAfterConnector()) {
				finalDimension.height -= BlockConnectorShape.CONTROL_PLUG_HEIGHT;
			}
			//set largest width by iterating through to sockets and getting 
			//the max width ONLY if curBlockID == connectedToBlockID
			int width = curRBSize.width;

			if(curBlock.getNumSockets() > 0 && !curBlock.isInfix()){
				int maxSocWidth = getMaxWidthOfSockets(curBlockID);
				//need to add the placeholder width within bottom sockets if maxSocWidth is zero
				if(maxSocWidth == 0){
					// Adjust for zoom
					width += 2 * BlockShape.BOTTOM_SOCKET_SIDE_SPACER * curRenderableBlock.getZoom();
				}

				if(maxSocWidth > 0){
					//need to minus the data plug width, otherwise it is counted twice
					maxSocWidth -= BlockConnectorShape.NORMAL_DATA_PLUG_WIDTH;

					// Adjust for zoom
					width += maxSocWidth * curRenderableBlock.getZoom();
				}
			}

			if(width > finalDimension.width) finalDimension.width = width;

			//move down the afters
			curBlockID = Block.getBlock(curBlockID).getAfterBlockID();
		}
		return finalDimension;
	}

	/**
	 * Redraws this RenderableBlock along with the RenderableBlocks 
	 * after it, which include after and socket blocks.  In other words,
	 * this method redraws the stack of blocks that begin with this.
	 * NOTE: this is inefficient, should only use this if needed
	 * NOTE: Must call this after loading of blocks to update the socket
	 * dimensions of this and set the isLoading flag to false
	 */
	public void redrawFromTop() {
		isLoading = false;
		for (BlockConnector socket : BlockLinkChecker.getSocketEquivalents(getBlock())) {

			if (socket.hasBlock()) {
				//loop through all the afters of the connected block
				long curBlockID = socket.getBlockID();
				// TODO: this is a patch, but we need to fix the root of the problem!
				if(RenderableBlock.getRenderableBlock(curBlockID) == null) {
					System.out.println("does not exist yet, block: "+curBlockID);
					continue;
				}

				RenderableBlock.getRenderableBlock(curBlockID).redrawFromTop();

				//add dimension to the mapping
				this.getConnectorTag(socket).setDimension(calcDimensionOfSocket(socket));
			} else {
				this.getConnectorTag(socket).setDimension(null);
			}
		}

		//reform shape with new socket dimension
		reformBlockShape();
		//next time, redraw with new positions and moving children blocks
		clearBufferedImage();
	}

	/**
	 * Helper method for updateSocketSpace and calcStackDim.
	 * Returns the maximum width of the specified blockID's socket blocks
	 * @param blockID the Long blockID of the desired block
	 */
	public int getMaxWidthOfSockets(Long blockID){
		int width = 0;
		Block block = Block.getBlock(blockID);
		RenderableBlock rb = RenderableBlock.getRenderableBlock(blockID);

		for(BlockConnector socket : block.getSockets()){
			Dimension socketDim = rb.getSocketSpaceDimension(socket);
			if(socketDim != null){
				if(socketDim.width > width)
					width = socketDim.width;
			}
		}

		return width;
	}

	/////////////////////
	//BLOCK CONNECTION //
	/////////////////////

	/**
	 * Notifies this renderable block that ITS socket connectedSocket was connected to 
	 * ANOTHER block with ID connectedBlockID.
	 */
	public void blockConnected(BlockConnector connectedSocket, long connectedBlockID) {
		//notify block first so that we will only need to repaint this block once
		getBlock().blockConnected(connectedSocket, connectedBlockID);

		//synchronize sockets
		synchronizeSockets();
		
		// make sure the connected block is positioned correctly
		moveConnectedBlocks();

		updateSocketSpace(connectedSocket, connectedBlockID, true);
	}

	/**
	 * Notifies this renderable block that its socket connectedSocket had a block
	 * disconnected from it.
	 */
	public void blockDisconnected(BlockConnector disconnectedSocket) {
		//notify block first so that we will only need to repaint this block once
		getBlock().blockDisconnected(disconnectedSocket);
		
		updateSocketSpace(disconnectedSocket, Block.NULL, false);
		
		//synchronize sockets
		synchronizeSockets();
	}

	///////////////////
	//BLOCK RENDERING//
	///////////////////

	/**
	 * Clears the BufferedImage of this
	 */
	public void clearBufferedImage() {
		GraphicsManager.recycleGCCompatibleImage(buffImg);
		buffImg = null;
	}

	/**
	 * Clears the BufferedImage of this and repaint this entirely  
	 */
	public void repaintBlock() {
		clearBufferedImage();

		if(this.isVisible()){  
			//NOTE: If it's not visible, this will throw an exception.
			//as during the redraw, it will try to access location information
			//of this
			repaint();
			highlighter.repaint();
		}
	}


	/**
	 * Swing paint method for J-Component
	 * Checks to see if the buffer has been cleared (or yet to be created),
	 * if so then it redraws the buffer and then draws the image on the graphics2d or
	 * else it uses the previous buffer.
	 */
	public void paintComponent(Graphics g) { 
		Graphics2D g2 = (Graphics2D) g;
		if(!isLoading){
			// if buffImg is null, redraw block shape
			if (buffImg == null) {
				updateBuffImg();//this method also moves connected blocks
			}
			if (dragging) {
				g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,DRAGGING_ALPHA));
				g2.drawImage(buffImg, 0, 0, null);
				g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,1));
			} else {
				g2.drawImage(buffImg, 0, 0,  null);	
			}
		}
	}

	/**
	 * Reforms the blockShape of this renderableBlock and saves it into the blockArea while
	 * updating the bounds of this RenderableBlock.  Used to update the shape and socket 
	 * positions while avoiding a full updateBuffImg.
	 */
	private void reformBlockShape() {
		abstractBlockArea = blockShape.reformArea();
		//TODO for zooming, create an AffineTransform to scale the block shape
		AffineTransform at = new AffineTransform();
		at.setToScale(zoom, zoom);
		blockArea = abstractBlockArea.createTransformedArea(at);

		//note: need to add twice the highlight stroke width so that the highlight does not get cut off
		Rectangle updatedDimensionRect = new Rectangle(
				this.getX(),
				this.getY(),
				blockArea.getBounds().width,
				blockArea.getBounds().height);
		if (!this.getBounds().equals(updatedDimensionRect)) {
			moveConnectedBlocks(); // bounds have changed, so move connected blocks
		}
		this.setBounds(updatedDimensionRect);

		//////////////////////////////////////////
		//set position of block labels.
		//////////////////////////////////////////
		if(pageLabel != null && getBlock().hasPageLabel()){
			pageLabel.update();
		}
		if(blockLabel != null){
			blockLabel.update();
		}
		if (collapseLabel != null) {
			collapseLabel.update();
		}
		if (comment != null) {
			comment.update();
		}
		for(ConnectorTag tag : socketTags){
			BlockConnector  socket = tag.getSocket();
			SocketLabel label = tag.getLabel();
			if(label == null || SocketLabel.ignoreSocket(socket)){
				continue;
			}
			label.update(getSocketAbstractPoint(socket));
		}
	}

	/**
	 * returns the Area of the block
	 * @return
	 */
	public Area getBlockArea() {
		return blockArea;
	}

	/**
	 * Redraws the entire buffer on a Graphics2D, called by paintCompnent() only
	 * if the buffer has been cleared.
	 */
	private void updateBuffImg() {
		//if label text has changed, then resync labels/sockets and reform shape
		if(!synchronizeLabelsAndSockets()){
			reformBlockShape();//if updateLabels is true, we don't need to reform AGAIN
		}

		//create image
		//note: need to add twice the highlight stroke width so that the highlight does not get cut off
		GraphicsManager.recycleGCCompatibleImage(buffImg);
		buffImg = GraphicsManager.getGCCompatibleImage(
				blockArea.getBounds().width,
				blockArea.getBounds().height);
		Graphics2D buffImgG2 = (Graphics2D)buffImg.getGraphics();

		//update bounds of this renderableBlock as bounds of the shape
		Dimension updatedDimensionRect = new Dimension(blockArea.getBounds().getSize());

		//get size of block to determine size needed for bevel image
		Image bevelImage = BlockShapeUtil.getBevelImage(
				updatedDimensionRect.width, updatedDimensionRect.height, blockArea);

		//need antialiasing to remove color fill artifacts outside the bevel
		buffImgG2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		//ADD BLOCK COLOR
		Color blockColor = this.getBLockColor();
		buffImgG2.setColor(blockColor);
		buffImgG2.fill(blockArea);

		//draw the bevel on the shape -- comment this line to not apply beveling
		buffImgG2.drawImage(bevelImage, 0,0, null);

		//DRAW BLOCK IMAGES  
		repositionBlockImages(blockArea.getBounds().width, blockArea.getBounds().height);
	}

	/**
	 * Draws the BlockImageIcon instances of this onto itself
	 * @param buffImgG2 the current Graphics2D representation of this
	 * @param width the current width of the buffered image
	 * @param height the current height of the buffered image
	 */
	private void repositionBlockImages(int width, int height){
		int margin = 5;

		//TODO need to take other images into acct if we enable multiple block images
		for(BlockImageIcon img : imageMap.values()){
			ImageIcon icon = img.getImageIcon();
			Point imgLoc = new Point(0,0);
			if(img.getImageLocation() == BlockImageIcon.ImageLocation.CENTER){
				imgLoc.setLocation((width-icon.getIconWidth())/2, (height - icon.getIconHeight())/2);
			}else if(img.getImageLocation() == ImageLocation.NORTH){
				imgLoc.setLocation((width-icon.getIconWidth())/2, margin);
			}else if(img.getImageLocation() == ImageLocation.SOUTH){
				imgLoc.setLocation((width-icon.getIconWidth())/2, height-margin-icon.getIconHeight());
			}else if(img.getImageLocation() == ImageLocation.EAST){
				imgLoc.setLocation(width - margin-icon.getIconWidth(), (height - icon.getIconHeight())/2);
			}else if(img.getImageLocation() == ImageLocation.WEST){
				imgLoc.setLocation(margin, (height-icon.getIconHeight())/2);
			}else if(img.getImageLocation() == ImageLocation.NORTHEAST){
				imgLoc.setLocation(width-margin-icon.getIconWidth(), margin);
			}else if(img.getImageLocation() == ImageLocation.NORTHWEST){
				imgLoc.setLocation(margin, margin);
			}else if(img.getImageLocation() == ImageLocation.SOUTHEAST){
				imgLoc.setLocation(width-margin-icon.getIconWidth(), height-margin-icon.getIconHeight());
			}else if(img.getImageLocation() == BlockImageIcon.ImageLocation.SOUTHWEST){
				//put in southwest corner
				imgLoc.setLocation(margin, height - (icon.getIconHeight() + margin));
			}


			if(getBlock().hasPlug() && (img.getImageLocation() != ImageLocation.EAST || 
					img.getImageLocation() != ImageLocation.NORTHEAST || 
					img.getImageLocation() != ImageLocation.SOUTHEAST));
			imgLoc.x += 4; // need to nudge it a little more because of plug
			img.setLocation(imgLoc.x, imgLoc.y);
		}
	}

	/**
	 * Sets the highlight color of this block.
	 * The specified highlight may be overrided if this block has focus, 
	 * is a search result, or is "bad".  However when those states are no 
	 * longer active, the color is set back to the specified hlColor, if 
	 * resetHightlight() was not called in the meantime.
	 * @param color the desired highlight Color
	 */
	public void setBlockHighlightColor(Color color){
		highlighter.setHighlightColor(color);
	}

	/**
	 * Hides highlighting for this block.
	 */
	public void resetHighlight(){
		highlighter.resetHighlight();
	}

	/**
	 * Tells this RenderableBlock to move its highlight handler to a new parent
	 * (should be called after this RB is moved to a new parent)
	 * @param parent the RBParent that is the RB's new parent
	 */
	public void setHighlightParent(RBParent parent) {
		highlighter.setParent(parent);
	}

	/**
	 * Overridden from JComponent.
	 * Returns true iff it has a parent, its parent is visible, and itself is visible; false otherwise.
	 * @return true iff it has a parent, its parent is visible, and itself is visible; false otherwise. 
	 */
	public boolean isVisible(){
		return super.isVisible() && getParent() != null && getParent().isVisible();
	}

	////////////////////////
	// COMMENT MANAGEMENT //
	////////////////////////
	/**
	 * @return true iff this.comment !=null
	 */
	public boolean hasComment(){
		if (comment != null){return true;}
		return false;
	}

	/**
	 * @return this.comment
	 */
	public Comment getComment(){
		return comment;
	}


	/**
	 * Sets this RenderableBlock's comment
	 * @param comment
	 */
	public void setComment(Comment comment) {
		this.comment = comment;
	}

	/**
	 * If this does NOT have comment, then add a new comment to
	 * this parent Container at a point (10,-20) away from upper-right
	 * hand corner.  Modify such that this.hasComment will now return true.
	 */
	public void addComment(){
		if (hasComment()){
			//a renderable block may only have ONE comment
		}else{
			int x = this.getX() + this.getWidth() + 30;
			int y = this.getY() - 40;
			comment = new Comment("", this, this.getBlock().getColor(), zoom);
			if (this.getParentWidget() != null) {
				comment.setParent(this.getParentWidget().getJComponent());
			} else {
				comment.setParent(this.getParent());
			}
			comment.setLocation(x, y);
			commentLabelChanged = true;
    	}
    	
		revalidate();
		getHighlightHandler().revalidate();
		updateBuffImg();
		comment.getArrow().updateArrow();
		getParent().repaint();
	}

	/**
	 * remove this comment from this parent Container and modify such that
	 * this.hasComment returns false.
	 */
	public void removeComment(){
		if (hasComment()){
			comment.delete();
			comment = null;
			commentLabelChanged = true;
			reformBlockShape();
			revalidate();
			getHighlightHandler().revalidate();
			updateBuffImg();
			getParent().repaint();
		}
	}


	/**
	 * returns where the CommentArrow should draw from
	 * @return
	 */
	public Point getCommentLocation() {
		Point location = this.getLocation();

		if (comment != null) {
			CommentLabel commentLabel = comment.getCommentLabel();
			if (commentLabel != null) {
				location.translate(commentLabel.getX() - 2, commentLabel.getY() - 2);
				location.translate(commentLabel.getWidth()/2, commentLabel.getHeight()/2);
			}
		}

		return location;
	}

	//////////////////////////////////
	// MOVEMENT OF CONNECTED BLOCKS //
	//////////////////////////////////

	/**
	 * Aligns all RenderableBlocks plugged into this one with the current location of this RenderableBlock.
	 * These RenderableBlocks to move include blocks connected at sockets and the after connector.  
	 */
	public void moveConnectedBlocks() {
		if (DEBUG)
			System.out.println("move connected blocks of this: "+this);

		// if this hasn't been added anywhere, asking its location will break stuff
		if (getParent() == null) return; 

		Block b = Block.getBlock(blockID);
		Point socketLocation;
		Point plugLocation;
		RenderableBlock rb;
		Point myScreenOffset = getLocation();
		Point otherScreenOffset;
		for(BlockConnector socket : BlockLinkChecker.getSocketEquivalents(b)){
			socketLocation = getSocketPixelPoint(socket);
        		if (socket.hasBlock()) {
        			rb = getRenderableBlock(socket.getBlockID());
        			
        			// TODO: djwendel - this is a patch, but the root of the problem
        			// needs to be found and fixed!!
        			if (rb == null) {
        				System.out.println("Block doesn't exist yet: "+socket.getBlockID());
        				continue;
        			}
        			
        			plugLocation = rb.getSocketPixelPoint(BlockLinkChecker.getPlugEquivalent(Block.getBlock(socket.getBlockID())));
        			otherScreenOffset = SwingUtilities.convertPoint(rb.getParent(), rb.getLocation(), getParent());
        			otherScreenOffset.translate(-rb.getX(), -rb.getY());
        			rb.setLocation((int)Math.round((float)myScreenOffset.getX()+socketLocation.getX()-(float)otherScreenOffset.getX()-plugLocation.getX()),
        						  (int)Math.round((float)myScreenOffset.getY()+socketLocation.getY()-(float)otherScreenOffset.getY()-plugLocation.getY()));
        			
        			rb.moveConnectedBlocks();
        		}
        }
    }
    
	
    private static void startDragging(RenderableBlock renderable, WorkspaceWidget widget){
    	renderable.pickedUp = true;
    	renderable.lastDragWidget = widget;
    	if(renderable.hasComment()){
    		renderable.comment.setConstrainComment(false);
    	}
    	Component oldParent = renderable.getParent();
        Workspace.getInstance().addToBlockLayer(renderable);
    	renderable.setLocation(SwingUtilities.convertPoint(oldParent, renderable.getLocation(), Workspace.getInstance()));
        renderable.setHighlightParent(Workspace.getInstance());
        for (BlockConnector socket : BlockLinkChecker.getSocketEquivalents(Block.getBlock(renderable.blockID))) {
    		if (socket.hasBlock()) {
    			startDragging(getRenderableBlock(socket.getBlockID()), widget);
    		}
    	}
    }

	/**
	 * This method is called when this RenderableBlock is plugged into another RenderableBlock that has finished dragging.
	 * @param widget the WorkspaceWidget where this RenderableBlock is being dropped.
	 */
	public static void stopDragging(RenderableBlock renderable, WorkspaceWidget widget) {
		if (!renderable.dragging)
			throw new RuntimeException("dropping without prior dragging?");
		//notify children
		for (BlockConnector socket : BlockLinkChecker.getSocketEquivalents(renderable.getBlock())) {
			if (socket.hasBlock()) {
				stopDragging(getRenderableBlock(socket.getBlockID()), widget);
			}
		}
		// drop this block on its widget (if w is null it'll throw an exception)
		widget.blockDropped(renderable);
		// stop rendering as transparent
        renderable.dragging = false;
        //move comment
        if(renderable.hasComment()){
    		if(renderable.getParentWidget() !=null){
    			renderable.comment.setParent(renderable.getParentWidget().getJComponent(),0);
    		}else{
    			renderable.comment.setParent(null, renderable.getBounds());
    		}
    		
    		renderable.comment.setConstrainComment(true);
    		renderable.comment.setLocation(renderable.comment.getLocation());
    		renderable.comment.getArrow().updateArrow();
    	}
    }
    
    private static void drag(RenderableBlock renderable, int dx, int dy, WorkspaceWidget widget, boolean isTopLevelBlock){
		if (!renderable.pickedUp)
			throw new RuntimeException("dragging without prior pickup");
		//mark this as being dragged
		renderable.dragging = true;
		// move the block by drag amount
		if(!isTopLevelBlock){
			renderable.setLocation(renderable.getX()+dx, renderable.getY()+dy);
		}
		// send blockEntered/blockExited/blogDragged as appropriate
		if(widget != null){            
			if (!widget.equals(renderable.lastDragWidget)) {
				widget.blockEntered(renderable);
				if (renderable.lastDragWidget != null) {
					renderable.lastDragWidget.blockExited(renderable);
				}
			}	        
			widget.blockDragged(renderable);  
			renderable.lastDragWidget = widget;
		}

		// translate highlight along with the block - this would happen automatically,
		// but putting the call here takes out any lag.
		renderable.highlighter.repaint();
		// Propagate the drag event to anything plugged into this block
		for (BlockConnector socket : BlockLinkChecker.getSocketEquivalents(renderable.getBlock())) {
			if (socket.hasBlock()) {
				drag(getRenderableBlock(socket.getBlockID()),dx,dy, widget, false);
			}
		}
	}

	///////////////////
	//MOUSE EVENTS   //
	///////////////////

	/**
	 * Makes public the protected processMouseEvent() method from Component so that the children within this block
	 * may pass mouse events to this
	 */
	public void processMouseEvent(MouseEvent e){
		super.processMouseEvent(e);
	}
	public void mouseReleased(MouseEvent e) {
		if (SwingUtilities.isLeftMouseButton(e)){
			if (!pickedUp)
				throw new RuntimeException("dropping without prior dragging?");
			dragHandler.mouseReleased(e);

			//if the block was dragged before...then
			if(dragging){
				BlockLink link = getNearbyLink(); //look for nearby link opportunities
				WorkspaceWidget widget = null;

				// if a suitable link wasn't found, just drop the block
				if (link == null) {
					widget = lastDragWidget;
					stopDragging(this, widget);
				} 
				// otherwise, if a link WAS found...
				else { 

					/* Make sure that no matter who's connecting to whom, the block
					 * that's being dragged gets dropped on the parent widget of the
					 * block that's already on the canvas. 
					 */
					if (blockID.equals(link.getSocketBlockID())) {
						// dragged block is the socket block, so take plug's parent.
						widget = getRenderableBlock(link.getPlugBlockID()).getParentWidget(); 
					}
					else {
						// dragged block is the plug block, so take the socket block's parent.
						widget = getRenderableBlock(link.getSocketBlockID()).getParentWidget();
					}

					// drop the block and connect its link
					stopDragging(this, widget);
					link.connect();
					Workspace.getInstance().notifyListeners(new WorkspaceEvent(widget, link, WorkspaceEvent.BLOCKS_CONNECTED));
					getRenderableBlock(link.getSocketBlockID()).moveConnectedBlocks();
				}    	

				//set the locations for X and Y based on zoom at 1.0
				this.unzoomedX = this.calculateUnzoomedX(this.getX());
				this.unzoomedY = this.calculateUnzoomedY(this.getY());

				Workspace.getInstance().notifyListeners(new WorkspaceEvent(widget, link, WorkspaceEvent.BLOCK_MOVED, true));
				if(widget instanceof MiniMap){
					Workspace.getInstance().getMiniMap().animateAutoCenter(this);
				}
			}
		}
		pickedUp = false;
		if(e.isPopupTrigger() || SwingUtilities.isRightMouseButton(e) || e.isControlDown()){
			//add context menu at right click location to provide functionality
			//for adding new comments and removing comments
			PopupMenu popup = ContextMenu.getContextMenuFor(this);
			add(popup);
			popup.show(this, e.getX(), e.getY());
		}
		Workspace.getInstance().getMiniMap().repaint();
	}
	public void mouseDragged(MouseEvent e) { 
		if (SwingUtilities.isLeftMouseButton(e)){
			if (!pickedUp)
				throw new RuntimeException("dragging without prior pickup?");

			Point pp = SwingUtilities.convertPoint(this, e.getPoint(), Workspace.getInstance().getMiniMap());
			if(Workspace.getInstance().getMiniMap().contains(pp)){
				Workspace.getInstance().getMiniMap().blockDragged(this, e.getPoint());
				lastDragWidget=Workspace.getInstance().getMiniMap();
				return;
			}

			// drag this block if appropriate (checks bounds first)
			dragHandler.mouseDragged(e);    

			// Find the widget under the mouse
			dragHandler.myLoc.move(getX()+dragHandler.mPressedX, getY()+dragHandler.mPressedY);
			Point p = SwingUtilities.convertPoint(this.getParent(), dragHandler.myLoc, Workspace.getInstance());     
			WorkspaceWidget widget = Workspace.getInstance().getWidgetAt(p);

			//if this is the first call to mouseDragged
			if(!dragging) {
				Block block = getBlock();
				BlockConnector plug = BlockLinkChecker.getPlugEquivalent(block);
				if (plug != null && plug.hasBlock()) {
					Block parent = Block.getBlock(plug.getBlockID());
					BlockConnector socket = parent.getConnectorTo(blockID);
					BlockLink link = BlockLink.getBlockLink(block, parent, plug, socket);
					link.disconnect();
					//socket is removed internally from block's socket list if socket is expandable
					RenderableBlock.getRenderableBlock(parent.getBlockID()).blockDisconnected(socket);
				
					//NOTIFY WORKSPACE LISTENERS OF DISCONNECTION
					Workspace.getInstance().notifyListeners(new WorkspaceEvent(widget, link, WorkspaceEvent.BLOCKS_DISCONNECTED));
				}
				startDragging(this, widget);
			}

			// drag this block and all attached to it
			drag(this,dragHandler.dragDX, dragHandler.dragDY, widget, true);

			Workspace.getInstance().getMiniMap().repaint();
		}
	}
	//show the pulldown icon if hasComboPopup = true
	public void mouseEntered(MouseEvent e) {
		dragHandler.mouseEntered(e);
		//!dragging: don't redraw while dragging
		//!SwingUtilities.isLeftMouseButton: dragging mouse moves into another block because of delay
		//!popupIconVisible: only update if there is a change
		//getBlock().hasSiblings(): only deal with blocks with siblings
		if (!SwingUtilities.isLeftMouseButton(e) && !dragging && getBlock().hasSiblings()) {
			blockLabel.showMenuIcon(true);
		}
	}
	public void mouseExited(MouseEvent e) {
		dragHandler.mouseExited(e);	
		//!dragging: don't redraw while dragging
		//!SwingUtilities.isLeftMouseButton: dragging mouse moves into another block because of delay
		//popupIconVisible: only update if there is a change
		//getBlock().hasSiblings(): only deal with blocks with siblings
		if (!SwingUtilities.isLeftMouseButton(e) && !dragging && !blockArea.contains(e.getPoint()) ) {
			blockLabel.showMenuIcon(false);
		}
	}
	public void mouseMoved(MouseEvent e) {}
	public void mouseClicked(MouseEvent e) {
		if (SwingUtilities.isLeftMouseButton(e)){
			dragHandler.mouseClicked(e);
			if(e.getClickCount() == 2 && !dragging){
				Workspace.getInstance().notifyListeners(new WorkspaceEvent(this.getParentWidget(), this.getBlockID(), WorkspaceEvent.BLOCK_STACK_COMPILED));
			}
		}
	}
	public void mousePressed(MouseEvent e) {
		if (SwingUtilities.isLeftMouseButton(e)){
			dragHandler.mousePressed(e);
			pickedUp = true; //mark this block as currently being picked up
		}
	}

	////////////////
	//SEARCHABLE ELEMENT
	////////////////
	public String getKeyword() {
		return getBlock().getBlockLabel();
	}

	public String getGenus() {
		return getBlock().getGenusName();
	}

	public void updateInSearchResults(boolean inSearchResults) {
		isSearchResult = inSearchResults;
		highlighter.setIsSearchResult(isSearchResult);
		//repaintBlock();
	}

	public boolean isSearchResult(){
		return isSearchResult;
	}

	////////////////
	//SAVING AND LOADING
	////////////////

	/**
	 * Returns the save string of this
	 * @return the save string of this
	 */
	public String getSaveString(){
		if(comment != null)
			return getBlock().getSaveString(descale(this.getX()), descale(this.getY()), 
					this.comment.getSaveString(),  isCollapsed());
		else 
			return getBlock().getSaveString(descale(this.getX()), descale(this.getY()), 
					null, isCollapsed());
	}
	
	/**
	 * Returns whether or not this is still loading data.
	 * @return whether or not this is still loading data.
	 */
	public boolean isLoading(){
		return isLoading;
	}
	

	/**
	 * Loads a RenderableBlock and its related Block instance from the specified blockNode;
	 * returns null if no RenderableBlock was loaded.
	 * @param blockNode Node containing information to load into a RenderableBlock instance
	 * @param parent WorkspaceWidget to contain the block to load
	 * @return RenderableBlock instance holding the information in blockNode; null if no RenderableBlock loaded
	 */
	public static RenderableBlock loadBlockNode(Node blockNode, WorkspaceWidget parent, HashMap<Long, Long> idMapping){
		boolean isBlock = blockNode.getNodeName().equals("Block");
		boolean isBlockStub = blockNode.getNodeName().equals("BlockStub");

		if( isBlock || isBlockStub){
			RenderableBlock rb = new RenderableBlock(parent, Block.loadBlockFrom(blockNode, idMapping).getBlockID(), true);

			if(isBlockStub){
				//need to get actual block node
				NodeList stubchildren = blockNode.getChildNodes();
				for(int j=0; j<stubchildren.getLength(); j++){
					Node node = stubchildren.item(j);
					if(node.getNodeName().equals("Block")){
						blockNode = node;
						break;
					}
				}
			}


			if(rb.getBlock().labelMustBeUnique()){
				//TODO check the instance number of this block
				//and update instance checker
			}

			Point blockLoc = new Point(0,0);
			NodeList children = blockNode.getChildNodes();
			Node child;

			for(int i=0; i<children.getLength(); i++){
				child = children.item(i);
				if(child.getNodeName().equals("Location")){
					//extract location information
					extractLocationInfo(child, blockLoc);
				} else if(child.getNodeName().equals("Comment")){
					rb.comment = Comment.loadComment(child.getChildNodes(), rb);
					if (rb.comment != null) {
						rb.comment.setParent(rb.getParentWidget().getJComponent());
					}
				} else if (child.getNodeName().equals("Collapsed")) {
					rb.setCollapsed(true);
				}
			}
			//set location from info
			rb.setLocation(blockLoc.x,blockLoc.y);

			if (rb.comment != null) {
				rb.comment.getArrow().updateArrow();
			}


			return rb;
		}
		return null;
	}

	/**
	 * Read Location Node change loc to location in Node
	 * @param location
	 * @param loc
	 */
	public static void extractLocationInfo(Node location, Point loc){
		NodeList coordinates = location.getChildNodes();
		Node coor;
		for(int j = 0; j<coordinates.getLength(); j++){
			coor = coordinates.item(j);
			if(coor.getNodeName().equals("X")){
				loc.x = Integer.parseInt(coor.getTextContent());
			}else if(coor.getNodeName().equals("Y")){
				loc.y = Integer.parseInt(coor.getTextContent());
			}
		}
	}


	/**
	 * Changes Point boxSize (x,y) to the (width,height) of boxSizeNode 
	 * That is x = width and y = height
	 * @param location
	 * @param boxSize
	 */
	public static void extractBoxSizeInfo(Node boxSizeNode, Dimension boxSize){
		NodeList coordinates = boxSizeNode.getChildNodes();
		Node coor;
		for(int j = 0; j<coordinates.getLength(); j++){
			coor = coordinates.item(j);
			if(coor.getNodeName().equals("Width")){
				boxSize.width = Integer.parseInt(coor.getTextContent());
			}else if(coor.getNodeName().equals("Height")){
				boxSize.height = Integer.parseInt(coor.getTextContent());
			}
		}
	}


	public String toString(){
		StringBuffer buf = new StringBuffer();
		buf.append("RenderableBlock "+getBlockID()+": "+getBlock().getBlockLabel());
		return buf.toString();
	}

	/***********************************
	 * State Saving Stuff for Undo/Redo *
	 ***********************************/

	private class RenderableBlockState
	{
		public int x;
		public int y;
	}

	public Object getState()
	{
		RenderableBlockState blockState = new RenderableBlockState();
		blockState.x = getX();
		blockState.y = getY();
		return blockState;
	}

	public void loadState(Object memento)
	{
		assert (memento instanceof RenderableBlockState) : "ISupportMemento contract violated in RenderableBlock";
		if(memento instanceof RenderableBlockState)
		{
			RenderableBlockState state = (RenderableBlockState)memento;
			this.setLocation(state.x, state.y);
		}
	}

	/***************************************
	 * Zoom support methods
	 ***************************************/

	private double zoom = 1.0;
	public void setZoomLevel(double newZoom) {
		//create zoom transformers
		this.zoom = newZoom;

		//rescale internal components
		if(pageLabel != null && getBlock().hasPageLabel()){
			this.pageLabel.setZoomLevel(newZoom);
		}
		if(blockLabel !=null){
			this.blockLabel.setZoomLevel(newZoom);
		}
		if(collapseLabel != null) {
			collapseLabel.setZoomLevel(newZoom);
		}
		this.plugTag.setZoomLevel(newZoom);
		this.afterTag.setZoomLevel(newZoom);
		this.beforeTag.setZoomLevel(newZoom);
		for(ConnectorTag tag : socketTags){
			tag.setZoomLevel(newZoom);
		}
		if(this.hasComment()){						
			this.comment.setZoomLevel(newZoom);
		}
	}

	/**
	 * the current zoom for this RenderableBlock
	 * @return the zoom
	 */
	public double getZoom() {
		return zoom;
	}

	/**
	 * returns a new int  x based on the current zoom
	 * @param x
	 * @return
	 */
	int rescale(int x){
		return (int)(x*zoom);
	}

	/**
	 * returns a new double x position based on the current zoom
	 * @param x
	 * @return
	 */
	int rescale(double x){
		return (int)(x*zoom);
	}

	/**
	 * returns the descaled x based on the current zoom
	 * that is given a scaled x it returns what that position would be when zoom == 1
	 * @param x
	 * @return
	 */
	private int descale(int x){
		return (int)(x/zoom);
	}

	/**
	 * returns the descaled x based on the current zoom
	 * that is given a scaled x it returns what that position would be when zoom == 1
	 * @param x
	 * @return
	 */
	private int descale(double x){
		return (int)(x/zoom);
	}

	/**
	 * calculates the x when the zoom is 1.0
	 * @param x of the current position
	 * @return the x when the zoom is 1.0
	 */
	public int calculateUnzoomedX(int x){
		return (int)(x/zoom);
	}

	/**
	 * calculates the y when the zoom is 1.0
	 * @param y of the current position
	 * @return the y when the zoom is 1.0
	 */
	public int calculateUnzoomedY(int y){
		return (int)(y/zoom);
	}


	/**
	 * mutator for the initial value of x
	 * @param unzoomedX
	 */
	public void setUnzoomedX(double unzoomedX) {
		this.unzoomedX = unzoomedX;
	}

	/**
	 * mutator for the initial value of y
	 * @param unzoomedY
	 */
	public void setUnzoomedY(double unzoomedY) {
		this.unzoomedY = unzoomedY;
	}

	/**
	 * observer for the initial value of x
	 * @return initial value of x coordinate
	 */
	public double getUnzoomedX() {
		return this.unzoomedX;
	}

	/**
	 * observer for the initial value of y
	 * @return initial value of x coordinate
	 */
	public double getUnzoomedY() {
		return this.unzoomedY;
	}

	public void processKeyPressed(KeyEvent e){
		for (KeyListener l : this.getKeyListeners()){
			l.keyPressed(e);
		}
	}

	/////////////////
	//Tool Tips
	/////////////////
	public JToolTip createToolTip(){
		return new CToolTip(new Color(255,255,225));
	}
	public void setBlockToolTip(String text){
		this.setToolTipText(text);
		this.blockLabel.setToolTipText(text);
	}

	protected boolean processKeyBinding(KeyStroke ks, KeyEvent e,
			int condition, boolean pressed){
		switch(e.getKeyCode()){
		case KeyEvent.VK_UP:
			return false;
		case KeyEvent.VK_DOWN:
			return false;
		case KeyEvent.VK_LEFT:
			return false;
		case KeyEvent.VK_RIGHT:
			return false;
		case KeyEvent.VK_ENTER:
			return false;
		default:
			return super.processKeyBinding(ks, e, condition, pressed);
		}
	}

	private ConnectorTag getConnectorTag(BlockConnector socket){
		if (socket == null) throw new RuntimeException("Socket may not be null");
		if(socket.equals(plugTag.getSocket())) return plugTag;
		if(socket.equals(afterTag.getSocket())) return afterTag;
		if(socket.equals(beforeTag.getSocket())) return beforeTag;
		for(ConnectorTag tag : this.socketTags){
			if(socket.equals(tag.getSocket())) return tag;
		}
		return null;
	}

	/**
	 * Returns the collapsed state if the block has a collapseLabel otherwise false.
	 */
	public boolean isCollapsed() {
		if (collapseLabel != null) return collapseLabel.isActive();

		return false;
	}

	/**
	 * If this block can be collapsed its collapse state will be set
	 * @param collapse
	 */
	public void setCollapsed(boolean collapse) {
		if (collapseLabel != null) {
			collapseLabel.setActive(collapse);
		}
	}

	/**
	 * Sets the visibility of blocks connected to this block
	 * according to the current collapse state.
	 */
	public void updateCollapse() {
		if (collapseLabel != null) {
			collapseLabel.updateCollapse();
		}
	}

	/**
	 * Returns the width of the collapseLabel for this block if there is one, 0 otherwise
	 * @return
	 */
	public int getCollapseLabelWidth() {
		if (collapseLabel != null) return collapseLabel.getWidth();

		return 0;
	}

	/**
	 * returns the RBHighlightHandler for this RenderableBlock
	 * @return the highlighter
	 */
	RBHighlightHandler getHighlightHandler() {
		return highlighter;
	}

	/**
	 * returns the larger width of the CollapseLabel or CommentLabel if they exist for this RenderableBlock
	 * @return
	 */
	int getControlLabelsWidth() {
		int x = 0;
		if (getComment() != null) {
			x += Math.max(getComment().getCommentLabelWidth(), getCollapseLabelWidth());
		} else {
			x += getCollapseLabelWidth();
		}
		return x;
	}

}
