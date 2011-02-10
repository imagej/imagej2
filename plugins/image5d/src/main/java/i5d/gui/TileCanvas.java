package i5d.gui;

import ij.*;

public class TileCanvas extends Image5DCanvas {
    private static final long serialVersionUID = 8368953085462080966L;
    protected int channel;	
    
	public TileCanvas(ImagePlus imp, int channel) {
		super(imp);
        this.channel = channel;
	}
    
    public TileCanvas(ImagePlus imp) {
        this(imp, 1);
    }
	



}
