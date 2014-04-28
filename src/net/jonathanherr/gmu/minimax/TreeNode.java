package net.jonathanherr.gmu.minimax;

import java.util.LinkedList;

import net.jonathanherr.gmu.hnefatafl.BoardState;
import net.jonathanherr.gmu.hnefatafl.Move;

public class TreeNode{
	static int nodeid=0;
	
	private LinkedList<TreeLink> links;
	private double score;
	private BoardState state;
	private int id;
	private String color;
	private int level=0;
	private boolean max; //every node is either a max or a min node from the perspective of the current player
	private TreeNode bestChild; //highest/lowest scored child for following best path through tree
	
	public TreeNode(BoardState state) {
		setLinks(new LinkedList<TreeLink>());
		this.state=state;
		nodeid+=1;
		setId(nodeid);
		setMax(true);
	}
	
	public void addChild(TreeNode node) {
		TreeLink link=new TreeLink(this,node);
		getLinks().add(link);
		if(this.isMax())
			node.setMax(false);
		else
			node.setMax(true);
	}
	
	
	public LinkedList<TreeLink> getChildren() {
		return this.getLinks();
	}
	public Move getMove(){
		return state.getMove();
	}
	public Integer getNodeid() {
		return getId();
	}
	public void setNodeid(int id) {
		this.setId(id);
	}
	public BoardState getState() {
		return state;
	}
	public double getScore() {
		return score;
	}
	public void setScore(double score) {
		this.score = score;
	}
	public int[][] getBoard() {
		return state.getBoard();
	}
	public void setColor(String c) {
		this.color=c;
	}
	public String getColor() {
		return color;
	}
	public LinkedList<TreeLink> getLinks() {
		return links;
	}
	public void setLinks(LinkedList<TreeLink> links) {
		this.links = links;
	}
	public int getLevel() {
		return level;
	}
	public void setLevel(int level) {
		this.level = level;
	}
	public boolean isMax() {
		return max;
	}
	public void setMax(boolean max) {
		this.max = max;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public void setBestChild(TreeNode bestChild) {
		this.bestChild=bestChild;
	}

	public TreeNode getBestChild() {
		return bestChild;
	}
}