package renderable;

import java.awt.Color;

import codeblocks.BlockConnectorShape;

/**
 * 
 * NameLabel displays the name of a RenderableBlock
 * 
 */
class NameLabel extends BlockLabel{
	private long blockID;
	
	public NameLabel(String initLabelText, BlockLabel.Type labelType, boolean isEditable, long blockID){
		super(initLabelText, labelType, isEditable, blockID, true, new Color(255,255,225));
		this.blockID = blockID;
	}
	
	void update(){
		RenderableBlock rb = RenderableBlock.getRenderableBlock(blockID);
		if (rb != null) {
	        int x = 0;
	        int y = 0;
	        if(rb.getBlock().isCommandBlock()) x+=5;
	        if(rb.getBlock().isDeclaration()) x+=12;
	        if(rb.getBlock().hasPlug()) x+=4+BlockConnectorShape.getConnectorDimensions(rb.getBlock().getPlug()).width;
	        if(rb.getBlock().isInfix()){ 
	            if(!rb.getBlock().getSocketAt(0).hasBlock()){
	                x+=30;
	            }else{
	                x+=rb.getSocketSpaceDimension(rb.getBlock().getSocketAt(0)).width;
	            }
	           
	        }
	        
	        if(rb.getBlockWidget()==null) y+=rb.getAbstractBlockArea().getBounds().height/2;
	        else y+=12;
	        
	        if(rb.getBlock().isCommandBlock()) y-=2;
	        if(rb.getBlock().hasPageLabel() && rb.getBlock().hasAfterConnector()) y-=BlockConnectorShape.CONTROL_PLUG_HEIGHT;
	        if(!rb.getBlock().hasPageLabel()) y-=getAbstractHeight()/2;
	        
	        //Comment Label and Collapse Label take up some additional amount of space
	        x += rb.getControlLabelsWidth();
	        
	        //if block is collapsed keep the name label from moving
	        y += (rb.isCollapsed()?BlockConnectorShape.CONTROL_PLUG_HEIGHT/2:0);
	        
	        x=rescale(x);
	        y=rescale(y);
	        
	        setPixelLocation(x, y);
		}
	}
	
}
