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
		if(root!=null && root.getChildren()!=null)
			root.getChildren().clear();
		this.root=node;
		
	}
	/**
	 * Score the leaves and propagate scores upward based on minimax
	 */
	public Move choose(MiniMaxPlayer player,String color, int turnNumber) {
		this.player=player;
		player.found=0;
		scoreTree(root);
		
		System.out.println("states found in table:" + player.found);
		//DrawTree.write(this,turnNumber);
		//player.printTree(root);
		System.out.println("best node score is " + root.getBestChild().getScore());
		System.out.println(player.getColor() + " is moving " + root.getBestChild().getMove().getLength() + " steps " + root.getBestChild().getMove().getDirection() + " from " + root.getBestChild().getMove().getPiece().getRow() + "," + root.getBestChild().getMove().getPiece().getCol());
		return root.getBestChild().getMove();
	}
	private double alphabeta(TreeNode node, double alpha, double beta) {
		if(node.getChildren().size()==0) {
			return evaluate(node);
		}
		if(node.isMax()) {
			for(TreeLink link:node.getChildren()) {
				
			}
			return alpha;
		}
		else {
		
			return beta;
		}
		
			
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
				if(link.getChild().getScore()>maxScore){
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
	private double evaluate(TreeNode node) {
		if(this.player.getColor().equals(node.getMove().getPiece().getName())){
			double score=this.player.evaluate(node.getState());
			node.setScore(score);
			return score;
		}
		else{
			double score=this.player.evaluateOpponent(node.getState());
			node.setScore(score);
			return score;
		}
		
	}
}
	
