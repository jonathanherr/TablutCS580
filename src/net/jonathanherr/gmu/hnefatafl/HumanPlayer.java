package net.jonathanherr.gmu.hnefatafl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import net.jonathanherr.gmu.hnefatafl.Board.Direction;

import org.apache.commons.lang3.math.NumberUtils;

public class HumanPlayer extends Player {

	public HumanPlayer(Hnefatafl game, ArrayList<Piece> pieces, String name) {
		super(game, pieces, name);
		// TODO Auto-generated constructor stub
	}
	private Move move;
	
	public Move turn(int turnnum) {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        System.out.print("Enter piece to move:");
        String piece="",cell="";
        try {
			piece = br.readLine();
			System.out.println("Moving " + piece);
			System.out.println("Enter square to move to:");
			cell=br.readLine();
			System.out.println("Moving " + piece + " to " + cell);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return translateMove(piece,cell);
		
	}
	/**
	 * Expects input in the
	 * @param piece
	 * @param cell
	 * @return
	 */
	public Move translateMove(String piece, String cell) {
		
		Move move=null;
		int[] pieceDest=new int[] {0,0};
		int[] pieceLoc=new int[] {0,0};
		Direction dir;
		int dist=0;
		
		if(piece.length()>0 && cell.length()>0) {
			
			String piecex = String.valueOf(piece.charAt(0));
			String piecey = String.valueOf(piece.charAt(1));
			if(NumberUtils.isNumber(piecex) && NumberUtils.isNumber(piecey)) {
				pieceLoc[0]=Integer.parseInt(piecex);
				pieceLoc[1]=Integer.parseInt(piecey);
			}
			if(NumberUtils.isNumber(String.valueOf(cell.charAt(0))) && NumberUtils.isNumber(String.valueOf(cell.charAt(1)))) {
				pieceDest[0]=Integer.parseInt(String.valueOf(cell.charAt(0)));
				pieceDest[1]=Integer.parseInt(String.valueOf(cell.charAt(1)));
			}
			
			if(pieceLoc[0]==pieceDest[0]) { //moving in y direction
				dist=Math.abs(pieceDest[1]-pieceLoc[1]);
				if(pieceDest[1]>pieceLoc[1]) {
					dir=Direction.RIGHT;
				}
				else
					dir=Direction.LEFT;
			}
			else { //moving in x direction
				dist=Math.abs(pieceDest[0]-pieceLoc[0]);
				if(pieceDest[0]>pieceLoc[0])
					dir=Direction.DOWN;
				else
					dir=Direction.UP;
			}
			move=new Move(game.getBoard().getPieceAt(pieceLoc[0], pieceLoc[1]),dir,dist);
			if(game.getBoard().isValid(move, pieceDest[0], pieceDest[1])) {
				return move;
			}
			else
				System.out.println("Invalid move.");
		}
		
		return null;
	}

}
