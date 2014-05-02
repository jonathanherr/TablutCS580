package net.jonathanherr.gmu.minimax;

import java.util.ArrayList;
import java.util.Random;

import net.jonathanherr.gmu.hnefatafl.MiniMaxPlayer;
import net.jonathanherr.gmu.hnefatafl.Move;


public class MiniMaxTree {
	public TreeNode root;
	private MiniMaxPlayer player;
	public boolean useAlphaBeta=true;
	private int nodesScored=0;
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
	public Move choose(MiniMaxPlayer player, int turnNumber, int depth) {
		this.player=player;
		nodesScored=0;
		if(!useAlphaBeta)
			minimax(root,depth,true);
		else
			alphabeta(root,depth,Double.NEGATIVE_INFINITY,Double.POSITIVE_INFINITY,true);
		System.out.println("Scored " + nodesScored + " nodes");
		ArrayList<TreeNode> bestChoices=new ArrayList<>();
		for(TreeLink link:root.getChildren()) {
			if(link.getChild().getScore()==root.getScore())
				bestChoices.add(link.getChild());
		}
		TreeNode bestChoice=bestChoices.get(new Random().nextInt(bestChoices.size()));
		
		
		//DrawTree.write(this,turnNumber);
		//player.printTree(root);
		System.out.println("best node score is " + bestChoice.getScore());
		System.out.println(player.getColor() + " is moving " + bestChoice.getMove().getLength() + 
				" steps " + bestChoice.getMove().getDirection() + " from " + bestChoice.getMove().getPiece().getRow() +
				"," + bestChoice.getMove().getPiece().getCol());
		return bestChoice.getMove();
	}
	/**
	 * alphabeta pruning implementation of minimax
	 * @param node - current node
	 * @param depth - cutoff
	 * @param alpha - alpha value
	 * @param beta - beta value
	 * @param maximizingPlayer - whether we are on a max or min node
	 * @return best score
	 */
	private double alphabeta(TreeNode node,int depth, double alpha, double beta, boolean maximizingPlayer) {
		if(depth==0 || node.getChildren().size()==0) {
			double bestValue=evaluate(node);
			node.setScore(bestValue);
			nodesScored+=1;
			return bestValue;
		}
		if(maximizingPlayer) {
			for(TreeLink link:node.getChildren()) {
				nodesScored+=1;
				alpha=Math.max(alpha,alphabeta(link.getChild(),depth-1,alpha,beta,false));
				if(beta<=alpha)
					break;
			}
			node.setScore(alpha);
			return alpha;
		}
		else {
			for(TreeLink link:node.getChildren()) {
				nodesScored+=1;
				beta=Math.min(beta,alphabeta(link.getChild(),depth-1,alpha,beta,true));	
				if(beta<=alpha)
					break;
			}
			node.setScore(beta);
			return beta;
		}
		
			
	}
	/**
	 * Standard minimax
	 * @param node - the node to be evaluated
	 * @param depth - the depth at which to stop for evaluation - cutoff
	 * @param maximizingPlayer - whether or not the caller is the maximizing player
	 * @return the preferred node which represents the best score from tree
	 */
	private double minimax(TreeNode node, int depth, boolean maximizingPlayer) {
		if(depth==0 || node.getChildren().size()==0) {
			double bestValue=evaluate(node);
			node.setScore(bestValue);
			nodesScored+=1;
			return node.getScore();
		}
		if(maximizingPlayer) {
			double bestValue=Double.NEGATIVE_INFINITY;
			for(TreeLink link:node.getChildren()) {
				nodesScored+=1;
				double val=minimax(link.getChild(),depth-1,false);
				bestValue=Math.max(bestValue,val);
			}
			node.setScore(bestValue);
			
			return bestValue;
		}
		else {
			double bestValue=Double.POSITIVE_INFINITY;
			for(TreeLink link:node.getChildren()) {
				nodesScored+=1;
				double val=minimax(link.getChild(),depth-1,true);
				bestValue=Math.min(bestValue,val);	
			}
			node.setScore(bestValue);
			return bestValue;
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
	
