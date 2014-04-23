package net.jonathanherr.gmu.minimax;

public class TreeLink{
	public TreeLink(TreeNode parent,TreeNode child){
		this.child=child;
		this.parent=parent;
	}
	TreeNode parent;
	TreeNode child;
	
	public TreeNode getParent() {
		return parent;
	}
	public void setParent(TreeNode parent) {
		this.parent = parent;
	}
	public TreeNode getChild() {
		return child;
	}
	public void setChild(TreeNode child) {
		this.child = child;
	}
}