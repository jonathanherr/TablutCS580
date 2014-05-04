package net.jonathanherr.gmu.hnefatafl;

import java.util.ArrayList;

public class SimplePlayer extends MiniMaxPlayer {

	
	public SimplePlayer(Hnefatafl game, ArrayList<Piece> pieces) {
		super(game, pieces);
		this.name="Simple";
		this.searchDepth=2;
		// TODO Auto-generated constructor stub
	}
	

	@Override
	public double evaluate(BoardState board) {
		if(this.getColor().equals("white"))
		{
			if(game.getBoard().getPieceAt(board.getKingLocation()[0],board.getKingLocation()[1])!=null)
				return (board.getWhitepieces().size()-board.getBlackpieces().size()) + .1*distanceFromCorner(game.getBoard().getPieceAt(board.getKingLocation()[0],board.getKingLocation()[1]));
			else{
				return 0;
			}
		}
		else{
			if(game.getBoard().getPieceAt(board.getKingLocation()[0],board.getKingLocation()[1])!=null)
				return (board.getBlackpieces().size()-board.getWhitepieces().size()) - .1*distanceFromCorner(game.getBoard().getPieceAt(board.getKingLocation()[0],board.getKingLocation()[1]));
			else{
				return 0;
			}
		}		
	}
}
