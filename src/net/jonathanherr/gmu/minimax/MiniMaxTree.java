package net.jonathanherr.gmu.minimax;

import net.jonathanherr.gmu.hnefatafl.MiniMaxPlayer;
import net.jonathanherr.gmu.hnefatafl.Move;


public class MiniMaxTree {
	public TreeNode root;
	private MiniMaxPlayer player;
	public MiniMaxTree() {
		
	}
	public void setRoot(TreeNode node) {
		this.root=node;
		this.root.setName(0);
		
	}
	/**
	 * Score the leaves and propagate scores upward based on minimax
	 */
	public Move choose(MiniMaxPlayer player,String color) {
		this.player=player;
		scoreTree(root);
		DrawTree.write(this);
		player.printTree(root);
		Move bestMove=null;
		
		
		return bestMove;
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
		for(TreeLink link:node.getChildren()) {
			if(node.max){
				if(link.getChild().getScore()>maxScore)
					maxScore=link.getChild().getScore();
			}
			else
				if(link.getChild().getScore()<minScore)
					minScore=link.getChild().getScore();
		}
		if(node.max)
			node.setScore(maxScore);
		else
			node.setScore(minScore);
	
	}
	private void evaluate(TreeNode node) {
		System.out.println("Evaluating " + node.getColor() + " node for " + player.getColor() + " player,  which is a max node?" + node.max);
		if(this.player.getColor().equals("black") && node.max)
			node.setScore(this.player.evaluate(node.getState()));
		else if(this.player.getColor().equals("black") && !node.max)
			node.setScore(this.player.evaluateOpponent(node.getState()));
		else if(this.player.getColor().equals("white") && node.max)
			node.setScore(this.player.evaluate(node.getState()));
		else if(this.player.getColor().equals("white") && !node.max)			
			node.setScore(this.player.evaluateOpponent(node.getState()));
	}
}
	
