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
	int searchDepth=3; //plies to search
	int minMoveSize=1; //mechanism to restrict game to large movements, for testing. Set to 1 for normal movement.
	int currentDepth=0;
	public MiniMaxPlayer(Hnefatafl game, ArrayList<Piece> pieces) {
		super(game, pieces, "Minimax");
		tree=new MiniMaxTree();
	}
	
	/**
	 * Obey player interface, provide a Move object to game controller representing the player's choice of action. 
	 */
	public Move turn(int turnNumber) {
		long start=System.nanoTime();
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
		totalMoves=0;
		generateStates(currentState,pieces,tree.root);
		System.out.println("Generated states:" + totalMoves);
		//printTree(node);
		Move chosenMove= tree.choose(this,this.getPieceColor(),turnNumber);
		long end=System.nanoTime();
		System.out.println(this.getColor() + " turn time:"+((end-start)/1000000000.0d)+"(s)");
		
		return chosenMove;
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
	private int totalMoves=0;
	private void generateStates(BoardState state, ArrayList<Piece> pieces, TreeNode parent) {
		BoardState gameState=null;
		int availMoves=0;
		//TODO: calculate branching factor and record time spent on each turn
		for(Piece piece:pieces) {
			if(piece!=null)
				for(Direction moveDir:Direction.values()) {
					//TODO: figure out why availMoves is wrong(gave 4 instead of 3)
					availMoves=piece.availLength(moveDir,game,state.board);
					totalMoves+=availMoves;
					
					while (availMoves>=minMoveSize) {
						//generate a state for each step in this direction
						int[][] board = new int[Board.boardwidth][Board.boardheight];
						ArrayList<Piece> blackPieces=new ArrayList<Piece>();
						ArrayList<Piece> whitePieces=new ArrayList<Piece>();
						if(piece.getName().equals(Board.BLACK))
							Board.deepCopy(state.getBlackpieces(), blackPieces);
						else
							Board.deepCopy(state.getWhitepieces(), whitePieces);
						
						Board.copyBoard(state.getBoard(), board);
						
						Piece statePiece=null;
						ArrayList<Piece> statePieces=null;
						/**
						 * Get a copy of the piece from the state list of pieces, which is a reference back to the real set of pieces and must be the one ultimately passed to the treenode with the move object and state.
						 */
						if(piece.getName().equals(Board.BLACK))
							statePieces=state.getBlackpieces();
						else
							statePieces=state.getWhitepieces();
						for(Piece bpiece:statePieces){
							if(bpiece.row==piece.row && bpiece.col==piece.col)
								statePiece=bpiece;
						}
						//when we do the simulated move, use a copy of the piece so that we don't update it's position in the 'real' set since we don't know which node the player will choose
						Move move=new Move(piece.copy(),moveDir,availMoves);
						
						Board simBoard=new Board(game);
						simBoard.debug=false; //don't want output from simulated moves
						simBoard.setBoard(board);
						//System.out.println("before state move from " + piece.getRow()+","+piece.getCol() + " " + move.getDirection() + " " + move.getLength());
						//System.out.println(simBoard.toStateString());
						if(piece.getName().equals(Board.BLACK)) {
							simBoard.setBlackpieces(blackPieces);
							simBoard.setWhitepieces(state.getWhitepieces());
						}
						else {
							simBoard.setWhitepieces(whitePieces);
							simBoard.setBlackpieces(state.getBlackpieces());
						}
						simBoard.move(this, move);
						//System.out.println("after state move to " + statePiece.getRow()+","+statePiece.getCol() + " " + move.getDirection() + " " + move.getLength());
						//System.out.println(simBoard.toStateString());
						
						gameState=new BoardState(simBoard.getBoardGrid(), simBoard.getBlackpieces(), simBoard.getWhitepieces());
						gameState.setMove(new Move(statePiece,moveDir,availMoves)); //when we create the state's move object based on what was selected, use the piece that is the original piece so that it won't have been updated and is the object from the game board's state.
						gameState.winner=simBoard.winner;
						gameState.setGameOver(simBoard.gameOver);
						
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
