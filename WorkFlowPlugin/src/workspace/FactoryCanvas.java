package workspace;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.ArrayList;

import javax.swing.JComponent;
import javax.swing.JPanel;

import renderable.RenderableBlock;
import codeblocks.Block;
import codeblockutil.CGraphite;
import codeblockutil.Canvas;

/**
 * A Canvas that acts as the parent of all blocks.
 * A FactoryCanvas is the actually graphical "drawer".
 * As a canvas is must support all the name/color
 * accessor methods.  In addition it also supports
 * various methods for blocks such as searching,
 * highlighting, etc.
 */
class FactoryCanvas extends JPanel implements Canvas, SearchableContainer, RBParent, ComponentListener{
	private static final long serialVersionUID = 328149080291L;
	private static final int BORDER_WIDTH = 10;
	/** The highlight of this canvas */
	private Color highlight = null;
	/** The color of this canvas */
	private Color color;
	/**
	 * Constructs a new FactoryCanvas
	 * @param name
	 * @param color
	 */
	FactoryCanvas(String name, Color color){
		super();
		this.setBackground(CGraphite.gray);
		this.setName(name);
		this.setColor(color);
		this.setLayout(null);
	}
	FactoryCanvas(String name){
		this(name, CGraphite.blue);
	}
	public Iterable<? extends SearchableElement> getSearchableElements(){
		return this.getBlocks();
	}
	ArrayList<RenderableBlock> getBlocks() {
		ArrayList<RenderableBlock> list = new ArrayList<RenderableBlock> ();
		for(Component comp : this.getComponents()){
			if(comp instanceof RenderableBlock){
				list.add((RenderableBlock)comp);
			}
		}
		return list;
    }
	public void updateContainsSearchResults(boolean containsSearchResults){
		Color previousHighlight = this.highlight;
		if(containsSearchResults){
			this.setHighlight(Color.yellow);
		}else{
			this.setHighlight(null);
		}
		this.firePropertyChange(Canvas.LABEL_CHANGE, this.highlight, previousHighlight);
	}
	void setHighlight(Color highlight){
		this.highlight = highlight;
	}
	public Color getHighlight(){
		return this.highlight;
	}
	public Color getColor(){
		return color;
	}
	public void setColor(Color color){
		if (color == null) {
			this.color = CGraphite.blue;
		}else{
			this.color = color;
		}
	}
	public JComponent getJComponent(){
		return this;
	}
	public void setName(String name){
		super.setName(name);
		this.repaint();
	}
    void addBlock(RenderableBlock block) {
    	//make sure block isn't a null instance
    	if(block == null || Block.NULL.equals(block.getBlockID())) return;
        addToBlockLayer(block);
        block.setHighlightParent(this);
		block.addComponentListener(this);
	}
    void removeBlock(RenderableBlock block) {
    	//make sure block isn't a null instance
    	if(block == null || Block.NULL.equals(block.getBlockID())) return;
        //remove from canvas graphically
    	this.remove(block);
        block.setHighlightParent(this);
		block.removeComponentListener(this);
    }
    void layoutBlocks(){
    	RenderableBlock rb;
    	int maxWidth = 20;
    	int tx=BORDER_WIDTH;
    	int ty=BORDER_WIDTH;
    	for(Component c : this.getComponents()){
    		if(c instanceof RenderableBlock){
    			rb = (RenderableBlock)c;
        		rb.setBounds(tx, ty, rb.getBlockWidth(), rb.getBlockHeight());
        		ty = ty + BORDER_WIDTH + rb.getBlockHeight();
        		rb.repaint();
        		if (maxWidth < rb.getBlockWidth() + BORDER_WIDTH) {
        			maxWidth = rb.getBlockWidth() + BORDER_WIDTH;
        		}
    		}
    	}
    	this.setPreferredSize(new Dimension(maxWidth, ty));
    }
    private final Integer BLOCK_HIGHLIGHT_LAYER = new Integer(0);
    private final Integer BLOCK_LAYER = new Integer(1);
    public void addToBlockLayer(Component c) {
        this.add(c, BLOCK_LAYER);
    }
    public void addToHighlightLayer(Component c) {
        this.add(c, BLOCK_HIGHLIGHT_LAYER);
    }
    public void componentResized(ComponentEvent e){
    	this.layoutBlocks();
    }
	public void componentHidden(ComponentEvent e){}
	public void componentMoved(ComponentEvent e){}
    public void componentShown(ComponentEvent e){}
}
