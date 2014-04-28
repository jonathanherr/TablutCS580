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
	boolean gameOver=false;
	String winner="";
	int[][] board;
	private Move move; //the move that created this state
	
	public BoardState(int[][] board, ArrayList<Piece> blackPieces,
			ArrayList<Piece> whitePieces) {
		super();
		this.board = board;
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
		for(int row=0;row<board.length;row++) {
			for(int col=0;col<board[row].length;col++) {
				if(board[row][col]==(int)'k') {
					pos[0]=row;
					pos[1]=col;
				}
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
	
}
