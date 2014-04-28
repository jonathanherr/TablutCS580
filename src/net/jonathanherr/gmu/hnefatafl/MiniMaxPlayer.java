package net.jonathanherr.gmu.hnefatafl;

import java.util.ArrayList;
import java.util.Arrays;

import net.jonathanherr.gmu.hnefatafl.Board.Direction;
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
	int searchDepth=2; //plies to search
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
		BoardState currentState=new BoardState(game.getBoard().board, game.getBoard().getBlackpieces(), game.getBoard().getWhitepieces());
		TreeNode node=new TreeNode(currentState);
		System.out.println(game.getBoard().toStateString());
		node.setColor(this.pieces.get(0).getName());
		tree.setRoot(node);
		ArrayList<Piece> pieces;
		if(this.getColor().equals(Board.BLACK))
			pieces=currentState.getBlackpieces();
		else
			pieces=currentState.getWhitepieces();
		
		generateStates(currentState,pieces,tree.root);
		//printTree(node);
		return tree.choose(this,this.getPieceColor());
	}
	/**
	 * Print out the tree for debugging
	 * @param node
	 */
	public void printTree(TreeNode node) {
		
		//System.out.println(game.getStateString("",node.board));
		ArrayList<String> outlines=new ArrayList<String>();
		game.getBoard();
		for(int i=0;i<=Board.boardheight+1;i++) {
			outlines.add("");
		}
		
		int rownum=0;
		game.getBoard();
		for(String line:Board.getStateString("",node.getBoard()).split("\n")) {
			outlines.set(rownum,outlines.get(rownum)+"\t" + line);
			rownum+=1;
		}
		if(node.getState().getMove()!=null)
			outlines.set(rownum,""+node.getState().getMove().getPiece().getRow()+","+node.getState().getMove().getPiece().getCol() + "->" + node.getState().getMove().getDirection().toString() + " " + node.getState().getMove().getLength() + " Score:"+String.format("%.3f",node.getScore()));
		game.getBoard();
		for(int i=0;i<=Board.boardheight+1;i++) {
			if(i==5)
				outlines.set(i,outlines.get(i)+"---------->");
			else
				outlines.set(i,outlines.get(i)+"           ");
				
		}
		int totalLevelStates=node.getLinks().size();
		if(totalLevelStates>0) {
			
			for(TreeLink link:node.getLinks()) {
				rownum=0;
				game.getBoard();
				for(String line:Board.getStateString("",link.getChild().getBoard()).split("\n")) {
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
		BoardState gameState=null;
		int totalMoves=0,availMoves=0;
		
		for(Piece piece:pieces) {
			for(Direction moveDir:Direction.values()) {
				availMoves=piece.availLength(moveDir,game,state.board);
				//TODO: reset board to parent board whenever we reivist parent. looks like we may be using same board throughout levels
				totalMoves+=availMoves;
				
				while (availMoves>=minMoveSize) {
					//generate a state for each step in this direction
					int[][] board = new int[Board.boardwidth][Board.boardheight];
					ArrayList<Piece> blackPieces=new ArrayList<Piece>();
					ArrayList<Piece> whitePieces=new ArrayList<Piece>();
					Board.deepCopy(state.getBlackpieces(), blackPieces);
					Board.deepCopy(state.getWhitepieces(), whitePieces);
					Board.copyBoard(state.getBoard(), board);
					Piece statePiece=null;
					ArrayList<Piece> statePieces=null;
					if(piece.getName().equals(Board.BLACK))
						statePieces=blackPieces;
					else
						statePieces=whitePieces;
					for(Piece bpiece:statePieces){
						if(bpiece.row==piece.row && bpiece.col==piece.col)
							statePiece=bpiece;
					}
					Move move=new Move(statePiece,moveDir,availMoves);
					
					Board simBoard=new Board(game);
					simBoard.setBoard(board);
					//System.out.println("before state move from " + piece.getRow()+","+piece.getCol() + " " + move.getDirection() + " " + move.getLength());
					//System.out.println(simBoard.toStateString());
					simBoard.setBlackpieces(blackPieces);
					simBoard.setWhitepieces(whitePieces);
					simBoard.move(this, move);
					//System.out.println("after state move to " + statePiece.getRow()+","+statePiece.getCol() + " " + move.getDirection() + " " + move.getLength());
					//System.out.println(simBoard.toStateString());
					
					gameState=new BoardState(simBoard.getBoardGrid(), simBoard.getBlackpieces(), simBoard.getWhitepieces());
					gameState.setMove(new Move(statePiece,moveDir,availMoves));
					gameState.winner=simBoard.winner;
					gameState.gameOver=simBoard.gameOver;
					
					TreeNode node=new TreeNode(gameState);
					node.setColor(piece.getName());
					node.setLevel(parent.getLevel()+1);
					parent.addChild(node);
					availMoves-=1;
				}
			}
		}
		/**
		 * Call self recursively to generate more levels
		 */
		for(TreeLink link:parent.getChildren()) {
			if(link.getChild().getLevel()<searchDepth){
				BoardState childState=link.getChild().getState();
				game.getBoard();
				if(link.getChild().getColor().equals(Board.WHITE))
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
