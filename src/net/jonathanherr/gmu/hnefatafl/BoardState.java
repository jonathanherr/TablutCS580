package net.jonathanherr.gmu.hnefatafl;

import java.util.ArrayList;

/**
 * Everything needed from the main game to score a board state.
 * @author jonathan
 *
 */
public class BoardState {
	ArrayList<Piece> blackPieces;
	ArrayList<Piece> whitePieces;
	private boolean gameOver=false;
	String winner="";
	int[][] board;
	private Move move; //the move that created this state
	public String statestring;
	
	
	public BoardState(int[][] board, ArrayList<Piece> blackPieces,
			ArrayList<Piece> whitePieces) {
		super();
		this.board = board;
		//statestring=Board.getStateString("", board);
		//TODO: possible memory saving change - calculate piece positions upon request by reading board state - slower probably
		this.blackPieces = blackPieces;
		this.whitePieces = whitePieces;
	}
	
	public int[][] getBoard() {
		return board;
	}
	public ArrayList<Piece> getBlackpieces() {

		return blackPieces;
	}
	public ArrayList<Piece> getWhitepieces() {
		return whitePieces;
	}
	public int[] getKingLocation() {
		int[] pos=new int[2];
		for(Piece piece:whitePieces) {
			if(piece.value==(int)'k') {
				pos[0]=piece.row;
				pos[1]=piece.col;
			}
		}
		return pos;
	}
	public Move getMove() {
		return move;
	}
	public void setMove(Move move) {
		this.move=move;
	}

	public boolean isGameOver() {
		return gameOver;
	}

	public void setGameOver(boolean gameOver) {
		this.gameOver = gameOver;
	}
	
}
