package net.jonathanherr.gmu.hnefatafl;

import java.util.ArrayList;
import java.util.Random;

import net.jonathanherr.gmu.hnefatafl.Board.Direction;

/**
 * Simple random player which chooses a random piece, a random direction, and a random move length until a valid move is picked. 
 * @author jonathan
 *
 */
public class RandomPlayer extends Player {
	static String name="Random";
	Random rand;
	public RandomPlayer(Hnefatafl game, ArrayList<Piece> pieces) {
		super(game, pieces,name);		
		rand=new Random(42);
	}
	/**
	 * Choose a random piece, a random length, and a random direction, and use that to move. Read board to choose valid, legal moves. 
	 */
	public Move turn(int turnNumber){
		if(pieces.size()>0){
			Piece movePiece=pieces.get(rand.nextInt(pieces.size()));
			int moveLength=0;
			int dirCount=0;
			while(moveLength==0 && dirCount<4){
				Direction moveDir=Direction.getDirection(rand.nextInt(4));
				int availLength=movePiece.availLength(moveDir, game,game.getBoard().board);
				if(availLength>0){
					moveLength=rand.nextInt(Math.max(availLength-1,1))+1;
					Move move=new Move(movePiece,moveDir,moveLength);
					System.out.println(this.getPieceColor() + " moves from " + movePiece.getRow()+","+movePiece.getCol() + " " + moveDir.toString() + " " + moveLength);
					moves.add(move);
					return move;
				}
				else
					dirCount+=1;
				
			}
			
		}
		return null;
	}
	/**
	 * Random player doesn't score the board
	 */
	public double evaluate(BoardState board) {
		return 0.0;
	}
	public int getCaptures() {
		return captures;
	}

}
