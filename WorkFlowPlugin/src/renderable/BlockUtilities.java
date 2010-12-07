package renderable;

import java.awt.Container;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import workspace.Workspace;
import workspace.WorkspaceEvent;
import workspace.WorkspaceWidget;
import codeblocks.Block;
import codeblocks.BlockConnector;
import codeblocks.BlockLink;
import codeblocks.BlockLinkChecker;
import codeblocks.BlockStub;

public class BlockUtilities {
	private static final Map<String, Integer> instanceCounter = new HashMap<String, Integer>();
	private static double zoom = 1.0;

	public static void reset(){
		zoom = 1.0;
		instanceCounter.clear();
	}
	public static void setZoomLevel(double newZoom) {
		zoom = newZoom;
	}

	/**
	 * Returns true if the specified label is valid according to the specifications 
	 * of this block's genus. For example, if this block's label must be unique 
	 * (as specified from its genus), this method verifies that its label is 
	 * unique relative to the other instances present.
	 * @param label the String block label to test
	 * @return true if the specified label is valid according to the specifications 
	 * of this block's genus. 
	 */
	public static boolean isLabelValid(Long blockID, String label){
		return isLabelValid(Block.getBlock(blockID), label);
	}
	public static boolean isLabelValid(Block block, String label){
		if(block == null || label == null){
			return false;
		}else if(block.labelMustBeUnique()){
			//search through the current block instances active in the workspace
			for(RenderableBlock rb : Workspace.getInstance().getRenderableBlocksFromGenus(block.getGenusName())){
				if(label.equals(rb.getBlock().getBlockLabel()))
					return false;
			}
		}
		//either label doesn't have to be unique or 
		//label was found to be unique in the search
		return true;
	}

	public static void deleteBlock(RenderableBlock block){
		block.setLocation(0,0);

		WorkspaceWidget widget = block.getParentWidget();
		if(widget != null){
			widget.removeBlock(block);
		}

		Container parent = block.getParent();
		if(parent != null){
			parent.remove(block);
			parent.validate();
		}

		block.setParentWidget(null);
		Workspace.getInstance().notifyListeners(new WorkspaceEvent(widget, block.getBlockID(), WorkspaceEvent.BLOCK_REMOVED));

	}
	public static RenderableBlock cloneBlock(Block myblock){
		String mygenusname = myblock.getGenusName();
		String label = myblock.getBlockLabel();

		//sometimes the factory block will have an assigned label different
		//from its genus label.
		if(!myblock.getInitialLabel().equals(myblock.getBlockLabel())){
			//acquire prefix and suffix length from myblock label  
			int prefixLength = myblock.getLabelPrefix().length();
			int suffixLength = myblock.getLabelSuffix().length();
			//we need to set the block label without the prefix and suffix attached because those 
			//values are automatically concatenated to the string specified in setBlockLabel.  I know its
			//weird, but its the way block labels were designed.
			if(prefixLength > 0 || suffixLength > 0)  //TODO we could do this outside of this method, even in constructor
				label = myblock.getBlockLabel().substring(prefixLength, myblock.getBlockLabel().length()-suffixLength);
		}

		//check genus instance counter and if label unique - change label accordingly
		//also check if label already has a value at the end, if so update counter to have the max value
		//TODO ria need to make this smarter
		//some issues to think about: 
		// - what if they throw out an instance, such as setup2? should the next time they take out 
		//   a setup block, should it have setup2 on it?  but wouldn't that be confusing?
		// - when we load up a new project with some instances with numbered labels, how do we keep 
		//   track of new instances relative to these old ones?
		// - the old implementation just iterated through all the instances of a particular genus in the 
		//   workspace and compared a possible label to the current labels of that genus.  if there wasn't
		//   any current label that matched the possible label, it returned that label.  do we want to do this? 
		//   is there something more efficient?

		String labelWithIndex = label;  //labelWithIndex will have the instance value

		int value;
		//initialize value that will be appended to the end of the label
		if(instanceCounter.containsKey(mygenusname))
			value = instanceCounter.get(mygenusname).intValue();
		else
			value = 0;
		//begin check for validation of label
		//iterate until label is valid
		while(!isLabelValid(myblock, labelWithIndex)){
			value++;
			labelWithIndex = labelWithIndex + value;
		}

		//set valid label and save current instance number
		instanceCounter.put(mygenusname, new Integer(value));
		if(!labelWithIndex.equals(label))  //only set it if the label actually changed...
			label = labelWithIndex;

		Block block;
		if(myblock instanceof BlockStub){
			Block parent = ((BlockStub)myblock).getParent();
			block = new BlockStub(parent.getBlockID(), parent.getGenusName(), parent.getBlockLabel(), myblock.getGenusName());
		}
		else
			block = new Block(myblock.getGenusName(), label);

		// TODO - djwendel - create a copy of the RB properties too, using an RB copy constructor.  Don't just use the genus.
		//RenderableBlock renderable = new RenderableBlock(this.getParentWidget(), block.getBlockID());
		RenderableBlock renderable = new RenderableBlock(null, block.getBlockID());
		renderable.setZoomLevel(BlockUtilities.zoom);
		renderable.redrawFromTop();
		renderable.repaint();
		return renderable;
	}



	/**
	 * Creates a string representation for the given RenderableBlock that
	 * is disambiguated from string representations for blocks with the same label
	 * by appending socket information to the end of the block's label.  The 
	 * created string is in the form:
	 * BlockLabel [socketLabel1, ..., socketLabelN]
	 * @param block the FactoryRenderableBlock to create a string representation of
	 * @return a String containing the given block's keyword with a 
	 * list of its socket labels appended to the end.
	 */
	public static String disambiguousStringRep(RenderableBlock block) {
		String rep = block.getKeyword();
		String genus = block.getGenus();
		Iterator<BlockConnector> sockets = block.getBlock().getSockets().iterator();
		if (sockets.hasNext()) {
			String socketLabels = " [";
			while (sockets.hasNext()) {
				if (socketLabels.length() > 2) 
					socketLabels += ", ";
				socketLabels += sockets.next().getLabel();
			}
			socketLabels += "]";
			//HACK!!! TODO: rewriting typeblocking
			if (genus.equals("sum")) {
				return rep + " [number]";
			} else if (genus.equals("string-append")){
				return rep + " [text]";
			}
			return rep + socketLabels;
		}
		return rep;
	}


	/**
	 * @param keyword
	 * 
	 * @requires keyword != null
	 * @return List of TextualFactoryBlocks, {T}, such that:
	 * 				T.toString contains keyword
	 *    		    T == null if no matching blocks were found
	 *    			T.toString is unique for each T
	 */
	public static List<TextualFactoryBlock> getAllMatchingBlocks(String keyword){
		//Use Set such that we don't get any repeats
		Set<TextualFactoryBlock> matchingBlocks = new TreeSet<TextualFactoryBlock>(new MatchingComparator(keyword));

		//find all FactoryRenderableBlocks and check for a match
		for(RenderableBlock renderable : Workspace.getInstance().getFactoryManager().getBlocks()){

			// TODO: don't assume they're all FactoryRenderableBlocks!  Collisions aren't...
			if(renderable==null || renderable.getBlockID().equals(Block.NULL) || !(renderable instanceof FactoryRenderableBlock)){
				continue;
			}

			// first, check if query matches block keyword
			if(renderable.getKeyword().toLowerCase().contains(keyword.toLowerCase())){
				matchingBlocks.add(new TextualFactoryBlock((FactoryRenderableBlock)renderable, renderable.getBlock().getBlockLabel()));
			}

			// grabs the quote block needed TODO: needs to be independent!
			if(keyword.startsWith("\"") && renderable.getBlock().getGenusName().equalsIgnoreCase("string")){
				String[] quote = keyword.split("\"");
				// makes sure that there is text after the " so that it can be placed onto the block
				if(quote.length > 1) {
					matchingBlocks.add(new TextualFactoryBlock((FactoryRenderableBlock)renderable, "\"" + quote[1] + "\""));
				}
			}

			// otherwise, if the keyword is too long, check to see if 
			// the user is trying to type extra info for disambiguation
			else if (keyword.length() > renderable.getKeyword().length()) {
				if(disambiguousStringRep((FactoryRenderableBlock)renderable).toLowerCase().contains(keyword.toLowerCase())){
					matchingBlocks.add(new TextualFactoryBlock((FactoryRenderableBlock)renderable,disambiguousStringRep((FactoryRenderableBlock)renderable)));
				}				
			}

			/////////////////////////////////////
			//TODO: Add code here for nicknames//
			/////////////////////////////////////

		}

		/* if blocks have the same labels, the search results will be ambiguous.
		 * the following expands the string representation of the TFB if needed
		 * to disambiguate the blocks. */
		ArrayList<TextualFactoryBlock> disambiguatedMatches = new ArrayList<TextualFactoryBlock>(matchingBlocks);
		TextualFactoryBlock t1, t2;
		for (int i = 0; i < disambiguatedMatches.size(); i++) {
			t1 = disambiguatedMatches.get(i);
			if (i > 0) {
				t2 = disambiguatedMatches.get(i-1);
				if (t1.toString().equals(t2.toString())) {
					disambiguatedMatches.set(i, new TextualFactoryBlock(t1.getfactoryBlock(), disambiguousStringRep(t1.getfactoryBlock())));
					disambiguatedMatches.set(i-1, new TextualFactoryBlock(t2.getfactoryBlock(), disambiguousStringRep(t2.getfactoryBlock())));
				}
			}
			if (i < disambiguatedMatches.size() - 1) {
				t2 = disambiguatedMatches.get(i+1);
				if (t1.toString().equals(t2.toString())) {
					disambiguatedMatches.set(i, new TextualFactoryBlock(t1.getfactoryBlock(), disambiguousStringRep(t1.getfactoryBlock())));
					disambiguatedMatches.set(i+1, new TextualFactoryBlock(t2.getfactoryBlock(), disambiguousStringRep(t2.getfactoryBlock())));
				}				
			}
		}
		//List<TextualFactoryBlock> f = new ArrayList<TextualFactoryBlock>();
		return disambiguatedMatches;
	}

	/**
	 * 
	 * @param plus
	 * 
	 * @requires plus != null
	 * @return List containing the two "+" TextualFactoryBlocks
	 * 			and any other blocks containing "+"
	 */
	public static List<TextualFactoryBlock> getPlusBlocks(String plus){
		Set<TextualFactoryBlock> matchingBlocks = new HashSet<TextualFactoryBlock>();
		// looks through the factory blocks
		for(RenderableBlock renderable : Workspace.getInstance().getFactoryManager().getBlocks()){
			if(renderable==null || renderable.getBlockID().equals(Block.NULL) || !(renderable instanceof FactoryRenderableBlock)){
				continue;
			}
			//TODO genus names are based from TNG, need to figure out a workaround 
			// grabs the "+" number and text blocks
			if(renderable.getBlock().getGenusName().equalsIgnoreCase("sum")){
				// changes the label so that the search result will not be ambiguous
				matchingBlocks.add(new TextualFactoryBlock((FactoryRenderableBlock)renderable, "+ [number]"));
			}
			if(renderable.getBlock().getGenusName().equalsIgnoreCase("string-append")){
				// changes the label so that the search result will not be ambiguous
				matchingBlocks.add(new TextualFactoryBlock((FactoryRenderableBlock)renderable, "+ [text]"));
			}
			// selects any other block that contains the number (for variables that contains the number)
			if(renderable.getKeyword().toLowerCase().contains(plus.toLowerCase())){
				matchingBlocks.add(new TextualFactoryBlock((FactoryRenderableBlock)renderable, renderable.getBlock().getBlockLabel()));
			}
		}

		return new ArrayList<TextualFactoryBlock>(matchingBlocks);
	}

	/**
	 * 
	 * @param digits
	 * 
	 * @requires digits != null
	 * @return List containing a number TextualFactoryBlock
	 * 			and any other blocks containing the numbers
	 */
	public static List<TextualFactoryBlock> getDigits(String digits){
		Set<TextualFactoryBlock> matchingBlocks = new TreeSet<TextualFactoryBlock>(new MatchingComparator(digits));
		// looks through the factory blocks
		for(RenderableBlock renderable : Workspace.getInstance().getFactoryManager().getBlocks()){
			if(renderable==null || renderable.getBlockID().equals(Block.NULL) || !(renderable instanceof FactoryRenderableBlock)){
				continue;
			}

			//TODO genus name are based from TNG, need to figure out a workaround 
			// selects the number block
			if(renderable.getBlock().getGenusName().equalsIgnoreCase("number")){
				matchingBlocks.add(new TextualFactoryBlock((FactoryRenderableBlock)renderable, digits));
			}
			// selects any other block that contains the number (for variables that contains the number)
			if(renderable.getKeyword().toLowerCase().contains(digits.toLowerCase())){
				matchingBlocks.add(new TextualFactoryBlock((FactoryRenderableBlock)renderable, renderable.getBlock().getBlockLabel()));
			}
		}
		return new ArrayList<TextualFactoryBlock>(matchingBlocks);
	}



	/**
	 * Comparator used by getAllMatchingBlocks() to sort according to the position in the word that the match occurs,
	 * then according to the sort order of TextualFactoryBlocks
	 * @author Daniel
	 */
	private static class MatchingComparator implements Comparator<TextualFactoryBlock>{
		private String keyword;
		public MatchingComparator(String keyword) {
			this.keyword = keyword.toLowerCase();
		}

		public int compare(TextualFactoryBlock t1, TextualFactoryBlock t2){
			if (t1.compareTo(t2) == 0) return 0;
			if (t1.toString().toLowerCase().indexOf(keyword) == t2.toString().toLowerCase().indexOf(keyword))
				return t1.compareTo(t2);
			return t1.toString().toLowerCase().indexOf(keyword) > t2.toString().toLowerCase().indexOf(keyword) ? 1 : -1;
		}		
	}


	/**
	 * Returns a new RenderableBlock instance with the matching genusName.
	 * New block will also have matching label is label is not-null. May return null.
	 * 
	 * @param genusName
	 * @param label
	 * 
	 * @requires if block associated with genusName has a non editable
	 * 			 or unique block label, then "label" MUST BE NULL.
	 * @return  A new RenderableBlock with matching genusName and label (if label is not-null).
	 * 			If no matching blocks were found, return null.
	 */
	public static RenderableBlock getBlock(String genusName, String label){
		if(genusName == null) return null;

//		find all blocks on the page and look for any match
		for(Block block : Workspace.getInstance().getBlocks()){
			//make sure we're not dealing with null blocks
			if(block==null || block.getBlockID() == null || block.getBlockID().equals(Block.NULL)){
				continue;
			}
			//find the block with matching genus and either a matching label or an editable label
			if(block.getGenusName().equals(genusName) && (block.isLabelEditable() || block.getBlockLabel().equals(label) 
					|| block.isInfix())){
				//for block stubs, need to make sure that the label matches because stubs of the same kind 
				//(i.e. global var getters, agent var setters, etc.) have the same genusName
				//but stubs of different parents do not share the same label
				if (block instanceof BlockStub && !block.getBlockLabel().equals(label)) {
					continue;
				}
				//create new renderable block instance
				RenderableBlock renderable = BlockUtilities.cloneBlock(block);
				//make sure renderable block is not a null instance of a block
				if(renderable == null || renderable.getBlockID().equals(Block.NULL)){
					throw new RuntimeException("Invariant Violated: a valid non null blockID just" +
					"returned a null instance of RenderableBlock");
					//please throw an exception here because it wouldn't make any sense
					//if the Block is valid but it's associated RenderableBlock is not
				}
				//do not drop down default arguments
				renderable.ignoreDefaultArguments();
				//get corresponding block
				Block newblock = Block.getBlock(renderable.getBlockID());
				//make sure corresponding block is not a null instance of block
				if(newblock == null || newblock.getBlockID().equals(Block.NULL)){
					throw new RuntimeException("Invariant Violated: a valid non null blockID just" +
					"returned a null instance of Block");
					//please throw an exception here because it wouldn't make any sense
					//if the Block is valid but it's associated RenderableBlock is not
				}
				//attempt to set the label text if possible as defined by the specs
				//should not set the labels of block stubs because their labels are determined by their parent
				if((block.isLabelEditable() || block.getBlockLabel().equals(label))){
					if(label != null && !(block instanceof BlockStub)){
						if(newblock.isLabelEditable() && !newblock.labelMustBeUnique()){
							newblock.setBlockLabel(label);
						}
					}
				}
				//return renderable block
				return renderable;
			}



			/////////////////////////////////////
			//TODO: Add code here for nicknames//
			/////////////////////////////////////



		}
		//TODO: the part below is a hack. If there are other types of blocks, we need to account for them
		return null;
	}

	private static boolean isNullBlockInstance(Long blockID){
		if(blockID == null){
			return true;
		}else if(blockID.equals(Block.NULL)){
			return true;
		}else if(Block.getBlock(blockID) == null){
			return true;
		}else if(Block.getBlock(blockID).getBlockID() == null){
			return true;
		}else if(Block.getBlock(blockID).getBlockID().equals(Block.NULL)){
			return true;
		}else if(RenderableBlock.getRenderableBlock(blockID)==null){
			return true;
		}else if(RenderableBlock.getRenderableBlock(blockID).getBlockID() == null){
			return true;
		}else if(RenderableBlock.getRenderableBlock(blockID).getBlockID().equals(Block.NULL)){
			return true;
		}else{
			return false;
		}
	}
	public static BlockNode makeNodeWithChildren(Long blockID){
		if(isNullBlockInstance(blockID)) return null;
		Block block = Block.getBlock(blockID);
		String genus = block.getGenusName();
		String parentGenus = block instanceof BlockStub ? ((BlockStub)block).getParentGenus() : null;
		String label;
		if (!block.labelMustBeUnique() || block instanceof BlockStub) {
			label = block.getBlockLabel();
		} else {
			label = null;
		}
		BlockNode node = new BlockNode(genus, parentGenus, label);
		for(BlockConnector socket : block.getSockets()){
			if(socket.hasBlock()){
				node.addChild(makeNodeWithStack(socket.getBlockID()));
			}
		}
		return node;
	}
	public static BlockNode makeNodeWithStack(Long blockID){
		if(isNullBlockInstance(blockID)) return null;
		Block block = Block.getBlock(blockID);
		String genus = block.getGenusName();
		String parentGenus = block instanceof BlockStub ? ((BlockStub)block).getParentGenus() : null;
		String label;
		if (!block.labelMustBeUnique() || block instanceof BlockStub) {
			label = block.getBlockLabel();
		} else {
			label = null;
		}
		BlockNode node = new BlockNode(genus, parentGenus, label);
		for(BlockConnector socket : block.getSockets()){
			if(socket.hasBlock()){
				node.addChild(makeNodeWithStack(socket.getBlockID()));
			}
		}
		if(block.hasAfterConnector()){
			node.setAfter(makeNodeWithStack(block.getAfterBlockID()));
		}
		return node;
	}
	
	/**
	 * Checks to see if the block still exists
	 * @return True if renderable block is still there, False otherwise
	 */
	public static boolean blockExists(BlockNode node){
		String genusName = node.getGenusName(); //genusName may not be null
		RenderableBlock renderable = BlockUtilities.getBlock(genusName, node.getLabel());
		if(renderable == null){
			return false;
		}
		else{
			return true;
		}
	}
	
	public static RenderableBlock makeRenderable(BlockNode node, WorkspaceWidget widget){
		String genusName = node.getGenusName(); //genusName may not be null
		RenderableBlock renderable = BlockUtilities.getBlock(genusName, node.getLabel());
		if(renderable == null) throw new RuntimeException("No children block exists for this genus: " +genusName);
		Block block = Block.getBlock(renderable.getBlockID()); //assume not null
		widget.blockDropped(renderable);
		for(int i = 0; i<node.getChildren().size(); i++){
			BlockConnector socket = block.getSocketAt(i);
			BlockNode child = node.getChildren().get(i);
			RenderableBlock childRenderable = makeRenderable(child, widget);
			Block childBlock = Block.getBlock(childRenderable.getBlockID());

			//link blocks
			BlockLink link;
			if(childBlock.hasPlug()){
				link = BlockLinkChecker.canLink(block, childBlock, socket, childBlock.getPlug());
			}else if(childBlock.hasBeforeConnector()){
				link = BlockLinkChecker.canLink(block, childBlock,socket, childBlock.getBeforeConnector());
			}else{
				link=null;
			}//assume link is not null
			link.connect();
			Workspace.getInstance().notifyListeners(new WorkspaceEvent(RenderableBlock.getRenderableBlock(link.getPlugBlockID()).getParentWidget(),link, WorkspaceEvent.BLOCKS_CONNECTED));

		}
		if(node.getAfterNode() != null){
			BlockConnector socket = block.getAfterConnector(); //assume has after connector
			BlockNode child = node.getAfterNode();
			RenderableBlock childRenderable = makeRenderable(child, widget);
			Block childBlock = Block.getBlock(childRenderable.getBlockID());

			//link blocks
			BlockLink link;
			if(childBlock.hasPlug()){
				link = BlockLinkChecker.canLink(block, childBlock, socket, childBlock.getPlug());
			}else if(childBlock.hasBeforeConnector()){
				link = BlockLinkChecker.canLink(block, childBlock,socket, childBlock.getBeforeConnector());
			}else{
				link=null;
			}//assume link is not null
			link.connect();
			Workspace.getInstance().notifyListeners(new WorkspaceEvent(RenderableBlock.getRenderableBlock(link.getPlugBlockID()).getParentWidget(),link, WorkspaceEvent.BLOCKS_CONNECTED));

		}
		return renderable;
	}

}