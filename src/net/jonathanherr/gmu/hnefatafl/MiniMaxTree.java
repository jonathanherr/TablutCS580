package net.jonathanherr.gmu.hnefatafl;

import java.util.ArrayList;

class Node{
	int[][] board;
	ArrayList<Link> links;
	double blackScore;
	double whiteScore;
	private BoardState state;
	static int nodeid=0;
	private int id;
	private String color;
	public void setColor(String c) {
		this.color=c;
	}
	public String getColor() {
		return color;
	}
	public Node(BoardState state) {
		links=new ArrayList<>();
		board=state.board;
		this.state=state;
		nodeid+=1;
		id=nodeid;

	}
	public void addChild(Node node) {
		Link link=new Link(this,node);
		links.add(link);
	}
	public ArrayList<Link> getChildren() {
		return this.links;
	}
	public void setName(int id) {
		//this.setNodeid(id);
	}
	public Integer getNodeid() {
		return id;
	}
	public void setNodeid(int id) {
		this.id = id;
	}
	public BoardState getState() {
		return state;
	}
}
class Link{
	public Link(Node parent,Node child){
		this.child=child;
		this.parent=parent;
	}
	Node parent;
	Node child;
	double blackScore;
	double whiteScore;
	
	public Node getParent() {
		return parent;
	}
	public void setParent(Node parent) {
		this.parent = parent;
	}
	public Node getChild() {
		return child;
	}
	public void setChild(Node child) {
		this.child = child;
	}
	public double getBlackScore() {
		return blackScore;
	}
	public void setBlackScore(double blackScore) {
		this.blackScore = blackScore;
	}
	public double getWhiteScore() {
		return whiteScore;
	}
	public void setWhiteScore(double whiteScore) {
		this.whiteScore = whiteScore;
	}
}

public class MiniMaxTree {
	public Node root;
	public MiniMaxTree() {
		
	}
	public void setRoot(Node node) {
		this.root=node;
		this.root.setName(0);
		
	}
	/**
	 * Score the leaves and propagate scores upward based on minimax
	 */
	public void choose(MiniMaxPlayer player,String color) {
		
		Node leafParent=findLeaf(root);
		for(Link leaflink:leafParent.links) {
			Node leaf=leaflink.getChild();
			//TODO: side recognition clearly broken
			if(player.getName().equals("white"))
				leaf.whiteScore=((Player) player).evaluate(leaf.getState());
			else
				leaf.blackScore=((Player) player).evaluate(leaf.getState());
		}
	}
	private Node findLeaf(Node node) {
		//DFS for leaf node, propagate back up
		for(Link link:root.getChildren()) {
			if(link.getChild().getChildren().size()>0) { //not a leafnode
				findLeaf(link.getChild());
			}
			else
				return link.getParent();
		}
		return null;
	}
	
}
	
