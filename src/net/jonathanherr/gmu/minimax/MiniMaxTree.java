package net.jonathanherr.gmu.minimax;

import java.util.ArrayList;
import java.util.Random;

import net.jonathanherr.gmu.hnefatafl.MiniMaxPlayer;
import net.jonathanherr.gmu.hnefatafl.Move;


public class MiniMaxTree {
	public TreeNode root;
	private MiniMaxPlayer player;
	public MiniMaxTree() {
		
	}
	public void setRoot(TreeNode node) {
		this.root=node;
		
	}
	/**
	 * Score the leaves and propagate scores upward based on minimax
	 */
	public Move choose(MiniMaxPlayer player,String color) {
		this.player=player;
		scoreTree(root);
		//DrawTree.write(this);
		//player.printTree(root);
		System.out.println(player.getColor() + " is moving " + root.getBestChild().getMove().getLength() + " steps " + root.getBestChild().getMove().getDirection() + " from " + root.getBestChild().getMove().getPiece().getRow() + "," + root.getBestChild().getMove().getPiece().getCol());
		return root.getBestChild().getMove();
	}
		
	private void scoreTree(TreeNode node) {
		//DFS for leaf node, propagate back up
		for(TreeLink link:node.getChildren()) {
			if(link.getChild().getChildren().size()>0) { //not a leafnode
				scoreTree(link.getChild());
			}
			else
				evaluate(link.getChild());
		}
		double maxScore=Double.NEGATIVE_INFINITY,minScore=Double.POSITIVE_INFINITY;
		TreeNode bestChild=null;
		ArrayList<TreeNode> bestChildren=new ArrayList<TreeNode>();
		for(TreeLink link:node.getChildren()) {
			if(node.isMax()){
				if(link.getChild().getScore()>=maxScore){
					maxScore=link.getChild().getScore();
					bestChild=link.getChild();
					bestChildren.clear();
					bestChildren.add(link.getChild());
				}
				else if(link.getChild().getScore()==maxScore){
					bestChildren.add(link.getChild());
				}
			}
			else
				if(link.getChild().getScore()<minScore){
					minScore=link.getChild().getScore();
					bestChild=link.getChild();
					bestChildren.clear();
					bestChildren.add(link.getChild());
				}
				else if(link.getChild().getScore()==minScore){
					bestChildren.add(link.getChild());
				}
		}
		//if we had a lot of equal
		if(bestChildren.size()>1)
		{
			bestChild=bestChildren.get(new Random().nextInt(bestChildren.size()));
		}
		node.setBestChild(bestChild);
		if(node.isMax())
			node.setScore(maxScore);
		else
			node.setScore(minScore);
	
	}
	private void evaluate(TreeNode node) {
		if(this.player.getColor().equals(node.getMove().getPiece().getName())){
			node.setScore(this.player.evaluate(node.getState()));
		}
		else{
			node.setScore(this.player.evaluateOpponent(node.getState()));
		}
		
	}
}
	
