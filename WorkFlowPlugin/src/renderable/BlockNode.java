package renderable;

import java.util.ArrayList;
import java.util.List;

public class BlockNode {
	private List<BlockNode> children = new ArrayList<BlockNode>();
	private BlockNode afterNode = null;
	private String genusName = null;
	private String parentGenus = null;
	private String label = null;
	/**
	 * genusName != null
	 * @param genusName
	 * @param parentGenus
	 * @param label
	 */
	BlockNode (String genusName, String parentGenus, String label){
		if(genusName == null ) throw new RuntimeException("Requirement Clause violdated: genus name and label may not be null");
		this.genusName = genusName;
		this.parentGenus = parentGenus;
		this.label = label;
	}
	void addChild(BlockNode child){
		if(child == null) return;
		else children.add(child);
	}
	void setAfter(BlockNode after){
		afterNode = after;
	}
	BlockNode getAfterNode() {
		return afterNode;
	}
	List<BlockNode> getChildren() {
		return children;
	}
	String getGenusName() {
		return genusName;
	}
	String getParentGenusName() {
		return parentGenus;
	}
	String getLabel() {
		return label;
	}
}