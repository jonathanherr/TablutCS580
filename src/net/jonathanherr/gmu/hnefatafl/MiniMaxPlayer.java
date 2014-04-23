package net.jonathanherr.gmu.hnefatafl;

import java.util.ArrayList;

import net.jonathanherr.gmu.hnefatafl.Hnefatafl.Direction;
import net.jonathanherr.gmu.minimax.MiniMaxTree;
import net.jonathanherr.gmu.minimax.TreeLink;
import net.jonathanherr.gmu.minimax.TreeNode;
/**
 * Player generates a decision tree and applies minimax algorithm to states based on evaluation of tree value at depth defined by searchDepth property. 
 * 
 * @author herrjr
 *
 */
public class MiniMaxPlayer extends Player {
	MiniMaxTree tree;
	int searchDepth=1; //plies to search
	int minMoveSize=4; //mechanism to restrict game to large movements, for testing. Set to 1 for normal movement.
	int currentDepth=0;
	public MiniMaxPlayer(Hnefatafl game, ArrayList<Piece> pieces) {
		super(game, pieces, "Minimax");
		tree=new MiniMaxTree();
	}
	/**
	 * Obey player interface, provide a Move object to game controller representing the player's choice of action. 
	 */
	public Move turn() {
		BoardState currentState=new BoardState(game.board, game.getBlackpieces(), game.getWhitepieces());
		TreeNode node=new TreeNode(currentState);
		node.setColor(this.pieces.get(0).name);
		tree.setRoot(node);
		
		generateStates(currentState,currentState.getWhitepieces(),tree.root);
		//printTree(node);
		tree.choose(this,this.getPieceColor());
		return null;
	}
	/**
	 * Print out the tree for debugging
	 * @param node
	 */
	public void printTree(TreeNode node) {
		
		//System.out.println(game.getStateString("",node.board));
		ArrayList<String> outlines=new ArrayList<String>();
		for(int i=0;i<=game.boardheight+1;i++) {
			outlines.add("");
		}
		
		int rownum=0;
		for(String line:game.getStateString("",node.getBoard()).split("\n")) {
			outlines.set(rownum,outlines.get(rownum)+"\t" + line);
			rownum+=1;
		}
		if(node.getState().getMove()!=null)
			outlines.set(rownum,""+node.getState().getMove().getPiece().getRow()+","+node.getState().getMove().getPiece().getCol() + "->" + node.getState().getMove().getDirection().toString() + " " + node.getState().getMove().getLength() + " Score:"+String.format("%.3f",node.getScore()));
		for(int i=0;i<=game.boardheight+1;i++) {
			if(i==5)
				outlines.set(i,outlines.get(i)+"---------->");
			else
				outlines.set(i,outlines.get(i)+"           ");
				
		}
		int totalLevelStates=node.getLinks().size();
		if(totalLevelStates>0) {
			
			for(TreeLink link:node.getLinks()) {
				rownum=0;
				for(String line:game.getStateString("",link.getChild().getBoard()).split("\n")) {
					outlines.set(rownum,outlines.get(rownum)+"\t" + line);
					rownum+=1;
				}
				if(node.getState().getMove()!=null) {
					Move childMove = link.getChild().getState().getMove();
					outlines.set(rownum,outlines.get(rownum)+"  "+childMove.getPiece().getRow()+","+childMove.getPiece().getCol() + "->" + childMove.getDirection().toString() + " " + childMove.getLength() + " Score:"+String.format("%.2f",link.getChild().getScore()));
				}
			
			}
			System.out.println(totalLevelStates + " states this level");
			for(String line:outlines)
				System.out.println(line);
			for(TreeLink link:node.getLinks()) {
				printTree(link.getChild());
			}
			
		}		
	}
	
	/**
	 * Generate the next level of a decision tree by looking at the available moves for each piece on the board owned by this player
	 * @param state
	 * @param pieces
	 * @param parent
	 */
	private void generateStates(BoardState state, ArrayList<Piece> pieces, TreeNode parent) {
		BoardState movestate=null;
		int totalMoves=0;
		for(Piece piece:pieces) {
			for(Direction moveDir:Direction.values()) {
				int availMoves=piece.availLength(moveDir,game,state.board);
				
				//System.out.println(availMoves + " for " + piece.name + " piece at " + piece.getRow()+","+piece.getCol() + " in direction " + moveDir.toString());
				totalMoves+=availMoves;
				while (availMoves>=minMoveSize) {
					//generate a state for each step in this direction
					Move move=new Move(piece,moveDir,availMoves);
					
					movestate=game.simMove(this, move,state);
					movestate.setMove(move);
					//Double stateScore=game.scoreBoard(this, movestate);
					TreeNode node=new TreeNode(movestate);
					node.setColor(piece.name);
					
					parent.addChild(node);
					availMoves-=1;
				}
			}
		}
		/**
		 * Call self recursively to generate more levels
		 */
		if(searchDepth>0) {
			searchDepth-=1;
			for(TreeLink link:parent.getChildren()) {
				BoardState childState=link.getChild().getState();
				if(link.getChild().getColor().equals(game.WHITE_NAME))
					generateStates(childState,childState.getBlackpieces(),link.getChild());
				else
					generateStates(childState,childState.getWhitepieces(),link.getChild());
					
			}
		}
	}
	/**
	 * For the time being, call the basic evaluation method
	 */
	public double evaluate(BoardState board) {
		return super.evaluate(board);
	}
	public double evaluateOpponent(BoardState state) {
		return super.evaluateOpponent(state);
	}
	

}
