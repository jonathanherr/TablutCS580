package net.jonathanherr.gmu.hnefatafl;

import net.jonathanherr.gmu.hnefatafl.Hnefatafl.Direction;

public class Move {
	private Piece piece;
	private Direction direction;
	private int length;
	
	public Piece getPiece() {
		return piece;
	}

	public Direction getDirection() {
		return direction;
	}

	public int getLength() {
		return length;
	}
	public Move(Piece piece, Direction dir, int length){
		this.piece=piece;
		this.direction=dir;
		this.length=length;
	}
	
}
