package net.jonathanherr.gmu.minimax;

import java.util.ArrayList;

import net.jonathanherr.gmu.hnefatafl.BoardState;
import net.jonathanherr.gmu.hnefatafl.Move;

public class TreeNode{
	private int[][] board;
	private ArrayList<TreeLink> links;
	private double score;
	private BoardState state;
	static int nodeid=0;
	private int id;
	private String color;
	boolean max; //every node is either a max or a min node from the perspective of the current player
	public void setColor(String c) {
		this.color=c;
	}
	public String getColor() {
		return color;
	}
	public TreeNode(BoardState state) {
		setLinks(new ArrayList<TreeLink>());
		setBoard(state.getBoard());
		this.state=state;
		nodeid+=1;
		id=nodeid;
		max=true;

	}
	public Move getMove(){
		return state.getMove();
	}
	public void addChild(TreeNode node) {
		TreeLink link=new TreeLink(this,node);
		getLinks().add(link);
		if(this.max)
			node.max=false;
		else
			node.max=true;
	}
	public ArrayList<TreeLink> getChildren() {
		return this.getLinks();
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
	public double getScore() {
		return score;
	}
	public void setScore(double score) {
		this.score = score;
	}
	public int[][] getBoard() {
		return board;
	}
	public void setBoard(int[][] board) {
		this.board = board;
	}
	public ArrayList<TreeLink> getLinks() {
		return links;
	}
	public void setLinks(ArrayList<TreeLink> links) {
		this.links = links;
	}
}