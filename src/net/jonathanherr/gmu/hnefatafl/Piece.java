package net.jonathanherr.gmu.hnefatafl;

import net.jonathanherr.gmu.hnefatafl.Board.Direction;

/**
 * Represents a single piece on the board, a white, black or king piece. Contains position, ascii value for display, and side 'name' value.
 * Can also check with board to see where it can be moved to
 * @author jonathan
 *
 */
public class Piece {

	int row;
	int col;
	int value; //ascii value for display
	/*
	 * black/white/king 
	 */
	private String name; 
	
	Piece(int row, int col, int value){
		this.row=row;
		this.col=col;
		this.value=value;
		if((char)value=='b')
			setName("black");
		else if((char)value=='w')
			setName("white");
		else
			setName("king");
	}
	public Piece copy() {
		Piece p = new Piece(row,col,value);
		return p;
	}
	public int getRow(){
		return row;
	}
	public int getCol(){
		return col;
	}
	public void setPosition(int row, int col) {
		this.row=row;
		this.col=col;
	}
	public int toChar(){
		return ((char)value);
	}
	public int availLength(Direction dir, Hnefatafl game,int[][] curboard){
		int avail=0;
		if(dir==Direction.UP || dir==Direction.DOWN){
			for(int i=1;i<9;i++){
				if(game.getBoard().isValid(this,row,col,dir,row+(dir.value*i),col,curboard)){
					avail+=1;
				}
			}
		}
		else if(dir==Direction.LEFT || dir==Direction.RIGHT){
			for(int i=1;i<9;i++){
				if(game.getBoard().isValid(this,row,col,dir,row,col+(dir.value*i),curboard)){
					avail+=1;
				}
			}
		}
		return avail;
	}
	public String getName() {
		return name;
	}
	void setName(String name) {
		this.name = name;
	}
}
