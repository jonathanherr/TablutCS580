package net.jonathanherr.gmu.hnefatafl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import net.jonathanherr.gmu.hnefatafl.Player.Result;

/**
 * Container for all functionality related to knowing and changing the state of the game board
 * @author herrjr
 *
 */
public class Board {
	static final String WHITE = "white";
	static final String BLACK = "black";
	static final String KING_NAME = "king";
	static int boardwidth;
	static int boardheight;
	char blank='_';
	char black='b';
	char white='w';
	char king='k';
	char escape='x';
	ArrayList<Piece> whitepieces=new ArrayList<Piece>();
	ArrayList<Piece> blackpieces=new ArrayList<Piece>();
	int[] throne={4,4};
	int[][] board;
	ArrayList<int[]> escapeNodes=new ArrayList<int[]>();
	private int[] kingLocation=new int[2];	
	String initstate;
	String winner;
	Result winResult;
	boolean gameOver;
	public boolean debug=false;
	private int blackCaptures;
	private int whiteCaptures;
	
	public Board(){
		board=new int[][]{};
		initstate="";
	}
	
	public ArrayList<Piece> getWhitepieces() {
		return whitepieces;
	}
	public ArrayList<Piece> getBlackpieces() {
		return blackpieces;
	}
	/**
	 * Enum to define direction of movement on each axis
	 * @author jonathan
	 *
	 */
	public enum Direction {
		UP(-1), DOWN(1), LEFT(-1), RIGHT(1);
	public int value;
	Direction(final int dir){
		this.value=dir;
	}
	public static Direction getDirection(final int dirVal){
		if(dirVal==0)
			return Direction.UP;
		else if(dirVal==1)
			return Direction.DOWN;
		else if(dirVal==2)
			return Direction.LEFT;
		else if(dirVal==3)
			return Direction.RIGHT;
		return null;
		
	}
	};
	public static int getBoardwidth() {
		return boardwidth;
	}
	public static void setBoardwidth(int boardwidth) {
		Board.boardwidth = boardwidth;
	}
	public static int getBoardheight() {
		return boardheight;
	}
	public static void setBoardheight(int boardheight) {
		Board.boardheight = boardheight;
	}
	public int[][] getBoardGrid() {
		return board;
	}
	public void setBoard(int[][] board) {
		this.board = board;
	}
	public int[] getKingLocation() {
		return kingLocation;
	}
	public void setKingLocation(int[] kingLocation) {
		this.kingLocation = kingLocation;
	}
	public void setWhitepieces(ArrayList<Piece> whitepieces) {
		this.whitepieces = whitepieces;
	}
	public void setBlackpieces(ArrayList<Piece> blackpieces) {
		this.blackpieces = blackpieces;
	}
	/**
	 * Read in a stringified matrix of board state. Assumes space seperated columns and newline seperated rows. 
	 * Reads 'b' as black, 'w' as white and 'k' as king. Use 'x' for escape/exit nodes. 
	 * Throne is assumed to be under king at start. 
	 * @param state
	 */
	public void loadState(String state){
		blackpieces.clear();
		whitepieces.clear();
		escapeNodes.clear();
		board=new int[boardwidth][boardheight];
		
		initstate=state;
		String[] rows=state.split("\n");
		int rownum=0,colnum=0;
		for(String row:rows){
			colnum=0;
			for(String cell:row.split(" ")){
				char cellcharacter = cell.toCharArray()[0];
				if(cellcharacter==black){
					blackpieces.add(new Black(rownum,colnum));
				}
				else if(cellcharacter==white){
					whitepieces.add(new White(rownum,colnum));
				}
				else if(cellcharacter==king){
					whitepieces.add(new King(rownum,colnum));
					throne=new int[2];
					throne[0]=rownum;
					throne[1]=colnum;
				}
				board[rownum][colnum]=cellcharacter;
				if(cellcharacter==escape) {
					int[] loc=new int[2];
					loc[0]=rownum;
					loc[1]=colnum;
					escapeNodes.add(loc);
				}
				
				colnum+=1;
			}
			rownum+=1;	
		}
	}
	/**
	 * Write out state file in same format as config file read by loadconfig method. Writes to 'state.cfg'. 
	 */
	public void saveState(){
		File state=new File("state.cfg");
		try {
			BufferedWriter bw=new BufferedWriter(new FileWriter(state));
			bw.write("height="+String.valueOf(Board.boardheight)+",width="+String.valueOf(Board.boardwidth));
			bw.write(this.toStateString());
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	/**
	 * reset board to defaults
	 * @param reloadConfig
	 */
	void reset() {
		blackpieces.clear();
		whitepieces.clear();
		for(int[] row:board){
			for(int col=0;col<row.length;col++){
				row[col]=blank;
			}
		}
	}

	/**
	 * Given a player and their move object, attempt to move the piece moved to the location defined in the move object. 
	 * Update the board, check for captures associated with the move(captures can only occur actively, landing between two opponents does not constitute capture).
	 * Check for win conditions.
	 * @param player - moving player's Player object
	 * @param move - player's Move object
	 * @return true if move was valid, false otherwise
	 */
	public boolean move(Player player,Move move){
		
		int newrow=move.getPiece().getRow(),newcol=move.getPiece().getCol();
		if(move.getDirection()==Direction.DOWN || move.getDirection()==Direction.UP){
			newrow=move.getPiece().getRow()+(move.getDirection().value*move.getLength());
		}
		else{
			newcol=move.getPiece().getCol()+(move.getDirection().value*move.getLength());
		}
		if(this.isValid(move,newrow,newcol)){
			board[move.getPiece().getRow()][move.getPiece().getCol()]=blank;
			//this.getPieceAt(move.getPiece().getRow(), move.getPiece().getCol()).setPosition(newrow, newcol);
			move.getPiece().setPosition(newrow,newcol);
			board[newrow][newcol]=move.getPiece().toChar();
			if(move.getPiece().getName().equals(KING_NAME)) {
				kingLocation[0]=newrow;
				kingLocation[1]=newcol;
			}
			//System.out.println(this.toStateString());
			takeCaptures(move);
			checkKingWin();
			return true;
		}
		else
			return false;
		
	}
	/**
	 * Return the character at the given spot or '!' if outside game board
	 * @param row
	 * @param col
	 * @return
	 */
	public char getStateAt(int row, int col) {
		if(row<boardheight && row>=0 && col<boardwidth && col>=0)
			return (char) board[row][col];
		return '!';
	}
	/**
	 * return the piece at the given spot on the board. (row,col) from top left starting at 0,0. return null if no piece at that location.
	 * @param row
	 * @param col
	 * @return
	 */
	public Piece getPieceAt(int row, int col){
		
		for(Piece piece:whitepieces){
			if(piece.getRow()==row && piece.getCol()==col){
				return piece;
			}
		}
		for(Piece piece:blackpieces){
			if(piece.getRow()==row && piece.getCol()==col)
				return piece;
		}
		return null;
	}
	
	/**
	 * 
	 * @param row
	 * @param col
	 * @param otherRow
	 * @param otherCol
	 * @return
	 */
	int getManhattanDistance(int row, int col, int otherRow, int otherCol) {
		int rowDist=0,colDist=0;
		//shift both points into first quad
		
		rowDist=Math.abs(otherRow-row);
		colDist=Math.abs(otherCol-col);
		return rowDist+colDist;
		
	}
	/**
	 * Simulate a move by updating a copy of the game board and returning it if the move is valid. Used by AI to generate
	 * state space. 
	 * @return
	 */
	public BoardState simMove(Player player, Move move,BoardState state){
		ArrayList<Piece> simBlackPieces=new ArrayList<>();
		ArrayList<Piece> simWhitePieces=new ArrayList<>();
		int[][] simBoard=new int[boardwidth][boardheight];
		
		deepCopy(state.getBlackpieces(),simBlackPieces);
		deepCopy(state.getWhitepieces(),simWhitePieces);		
		copyBoard(state.board,simBoard);
		BoardState simstate=new BoardState(simBoard, simBlackPieces, simWhitePieces);
		
		int newrow=move.getPiece().getRow(),newcol=move.getPiece().getCol();
		if(move.getDirection()==Direction.DOWN || move.getDirection()==Direction.UP){
			newrow=move.getPiece().getRow()+(move.getDirection().value*move.getLength());
		}
		else{
			newcol=move.getPiece().getCol()+(move.getDirection().value*move.getLength());
		}
		
		if(this.isValid(move.getPiece(),move.getPiece().getRow(),move.getPiece().getCol(),move.getDirection(),newrow,newcol,state.board)){
			simBoard[move.getPiece().getRow()][move.getPiece().getCol()]=blank;
			for(Piece piece:simWhitePieces) {
				if(piece.getCol()==move.getPiece().getCol() && piece.getRow()==move.getPiece().getRow())
					piece.setPosition(newrow, newcol);
			}
			for(Piece piece:simBlackPieces) {
				if(piece.getCol()==move.getPiece().getCol() && piece.getRow()==move.getPiece().getRow())
					piece.setPosition(newrow, newcol);
			}
			//System.out.println("setting " + newrow + ","+newcol + " to " + String.valueOf((char)move.getPiece().value));
			simBoard[newrow][newcol]=String.valueOf((char)move.getPiece().value).toUpperCase().toCharArray()[0];
			
		}
		return simstate;
	}
	/**
	 * Copy one board to another
	 * @param curBoard
	 * @param simBoard
	 */
	public static void copyBoard(int[][] curBoard,int[][] simBoard) {
		for(int row=0;row<boardheight;row++) {
			for(int col=0;col<boardwidth;col++) {
				simBoard[row][col]=curBoard[row][col];
			}
		}
		
	}
	/**
	 * Copy pieces from one array to another
	 * @param pieces
	 * @param simPieces
	 */
	public static void deepCopy(ArrayList<Piece> pieces,
			ArrayList<Piece> simPieces) {
		for(Piece piece:pieces) {
			simPieces.add(new Piece(piece.row,piece.col,piece.value));
		}
		
	}
	
	/**
	 * Check to see if the king has reached an escape node. 
	 * TODO - notice when king has a move to one or two exits and declare to opponent(like check/checkmate).
	 */
	private void checkKingWin(){
		for(Piece piece:whitepieces){
			if(piece.value==king){
				int[] kingLoc = new int[2];
				kingLoc[0]=piece.getRow();
				kingLoc[1]=piece.getCol();
				for(int[] node:escapeNodes){
					if(node[0]==kingLoc[0] && node[1]==kingLoc[1]){
						if(debug)
							System.out.println("White Wins");
						win(piece,null,Result.KINGESCAPE);	
					}
				}				
			}
		}
		
	}
	/**
	 * Look at board and determine if there is a path to escape.
	 * @param piece
	 * @return
	 */
	public boolean kingHasPathToEscape(Piece piece) {
		if(piece.getName().equals(KING_NAME)) {
			for(int[] node:escapeNodes){
				if(node[0]==piece.getRow()) { //in same row together, just check if empty in between columns
					
				}
				else if(node[1]==piece.getCol()) { //in same col togheter, check if empty between rows
					
				}
			}
		}
		return false;
	}
	/**
	 * Cleanup after win condition is met, declare winner, set winner field and gameover flag. output final board state. 
	 * @param takingPiece
	 * @param takenPiece
	 * @param result
	 */
	void win(Piece takingPiece,Piece takenPiece, Result result) {
		
		if(takenPiece!=null){
			if(takenPiece.getName().equals(KING_NAME)){
				if(debug)
					System.out.println(takingPiece.getName() + " wins by capturing white's king");
				getBoardGrid();
				winner=Board.BLACK;
				winResult=Result.KINGCAP;				
			}
		}
		else{
			if(takingPiece.getName().equals(WHITE) || takingPiece.getName().equals(KING_NAME)){
				if(debug)
					System.out.println("White wins by " + result);
				getBoardGrid();
				this.winner=Board.WHITE;
				winResult=result;
			}
			else{
				if(debug)
					System.out.println("Black wins by " + result);
				getBoardGrid();
				this.winner=Board.BLACK;
				winResult=result;
			}
		}
		//System.out.println(toStateString());
		this.gameOver=true;
		
		
				
	}
	/**
	 * Search for captures and return list of pieces to be captured
	 * @param piece
	 * @return
	 */
	public ArrayList<Piece> findCaptures(Piece piece) {
		ArrayList<Piece> neighbors=new ArrayList<>();
		ArrayList<Piece> captures=new ArrayList<>();
		//look around piece to see if any piece nearby is an opponent, if so, go in that direction until a wall or another piece of the same color is found
		//looking for a capture situation.
		for(Direction dir:Direction.values()){
			Piece neighbor=null;
			neighbors.clear();
			int rowInc=0,colInc=0,startrow=piece.getRow(),startcol=piece.getCol();
			if(dir==Direction.UP || dir==Direction.DOWN)
				rowInc=dir.value;
			else if(dir==Direction.LEFT || dir==Direction.RIGHT)
				colInc=dir.value;
			
			neighbor=this.getPieceAt(startrow+=rowInc, startcol+=colInc);
			while(neighbor!=null && !neighbor.getName().equals(piece.getName()) &&  (!(piece.getName().equals(WHITE) 
					&& neighbor.getName().equals(KING_NAME))) && !(neighbor.getName().equals(KING_NAME) 
							&& neighbor.row==throne[0] && neighbor.col==throne[1])){
				if(neighbor.getName().equals(KING_NAME) && neighbor.row==throne[0] && neighbor.col==throne[1])
					System.out.println("catching king at home");
				neighbors.add(neighbor);
				neighbor=this.getPieceAt(startrow+=rowInc, startcol+=colInc); //returns null if no piece exists at given point. 	
			}
			//capture in two cases, we hit an empty square, and that square is a corner(escape) cell or we have surrounded opponents on two sides
			if((neighbor!=null && 
					((neighbor.getName().equals(piece.getName()) || (neighbor.getName().equals(WHITE) && piece.getName().equals(KING_NAME))) 
					|| neighbor.getName().equals(KING_NAME) && piece.getName().equals(WHITE))) 
					|| (neighbor==null && startrow<boardwidth && startcol<boardheight && getStateAt(startrow,startcol)==escape) 
					&& !neighbors.isEmpty())
			{
				for(Piece opponentPiece: neighbors){
					captures.add(opponentPiece);						
				}
			}
		}
		return captures;
	}
	/**
	 * Captures must be deliberate so we only look for captures on a move.
	 * Capture is determined by looking around the piece just moved for opponents, 
	 * then continuing in that direction to look for a matching piece to use in a pin. 
	 * If found, all opponents between the two are captured.
	 * Kings don't take part in captures, but corners(escape nodes) can be part of a capture. 
	 * 
	 * Note:
	 * Several rules some people use aren't captured here - captures against a wall, and captures of a player that repeatedly moves in and out of the same several spaces to hem in an opponent and delay.  
	 * 
	 * @param move
	 */
	private void takeCaptures(Move move){
		Piece movedPiece=move.getPiece();
		ArrayList<Piece> captures=findCaptures(movedPiece);
		
		for(Piece opponentPiece: captures){
			if(debug)
				System.out.println(movedPiece.getName() + " takes " + opponentPiece.getName() + " at " + opponentPiece.getRow()+","+opponentPiece.getCol());
			take(movedPiece,opponentPiece);							
		}
	
		
	}

	/**
	 * Call to take a piece, do cleanup
	 * @param row
	 * @param col
	 */
	private void take(Piece attackingPiece,Piece takenPiece) {
		
		this.getBoardGrid()[takenPiece.getRow()][takenPiece.getCol()]=this.blank;
		
		if(this.getBlackpieces().contains(takenPiece))
			this.getBlackpieces().remove(takenPiece);
		else
			this.getWhitepieces().remove(takenPiece);
		
		if(attackingPiece.value==this.black){
			if(takenPiece.value==this.king){
				win(attackingPiece,takenPiece,Result.KINGCAP);
			}
			this.setBlackCaptures(this.getBlackCaptures() + 1);				
		}
		else{
			this.setWhiteCaptures(this.getWhiteCaptures() + 1);
		}
		
	}
	
	/**
	 * check that the given board will allow the given move
	 * @param piece
	 * @param currow
	 * @param curcol
	 * @param dir
	 * @param destrow
	 * @param destcol
	 * @param curboard
	 * @return
	 */
	public boolean isValid(Piece piece, int currow, int curcol, Direction dir, int destrow, int destcol,int[][] curboard) {
		boolean valid=true;
		//check that destination is not the throne(legal to pass through the throne)
		if(this.throne[0]==destrow && this.throne[1]==destcol && !piece.getName().equals(KING_NAME) )
			valid=false;
		//check that destination is not a corner square
		if(destrow==0 && destcol==0 && !piece.getName().equals(KING_NAME))
			valid=false;
		if(destrow==0 && destcol==boardwidth-1 && !piece.getName().equals(KING_NAME))
			valid=false;
		if(destrow==boardheight-1 && destcol==0 && !piece.getName().equals(KING_NAME))
			valid=false;
		if(destrow==boardheight-1 && destcol==boardwidth-1 && !piece.getName().equals(KING_NAME))
			valid=false;
		//check that spaces between current and dest spots are empty
		if( destrow>=0 && destcol>=0 && destrow<curboard.length && destcol<curboard[destrow].length){
			if(dir==Direction.UP){
				for(int row=currow+dir.value;row>=destrow;row+=dir.value){
					int cellValue=curboard[row][curcol];
					if(cellValue!=blank && !(cellValue==escape && piece.getName().equals(KING_NAME))){						
						valid=false;
					}
				}
			}
			else if(dir==Direction.DOWN){
				for(int row=currow+dir.value;row<=destrow;row+=dir.value){
					int cellValue = curboard[row][curcol];
					if(cellValue!=blank && !(cellValue==escape && piece.getName().equals(KING_NAME))){
						valid=false;
					}
				}
			}
			else if(dir==Direction.LEFT){
				for(int col=curcol+dir.value;col>=destcol;col+=dir.value){
					int cellValue = curboard[currow][col];
					if(cellValue!=blank && !(cellValue==escape && piece.getName().equals(KING_NAME))){
						valid=false;
					}
				}
			}
			else if(dir==Direction.RIGHT){
				for(int col=curcol+dir.value;col<=destcol;col+=dir.value){
					int cellValue = curboard[currow][col];
					if(cellValue!=blank && !(cellValue==escape && piece.getName().equals(KING_NAME))){
						valid=false;
					}
				}
			}
		}
		else
			valid=false;
		return valid;
	}
	/**
	 * Verify that the given piece can move from his current place to the given destination without breaking any rules
	 * @param piece
	 * @param currow
	 * @param curcol
	 * @param dir
	 * @param destrow
	 * @param destcol
	 * @return
	 */
	public boolean isValid(Piece piece, int currow, int curcol, Direction dir, int destrow, int destcol) {
		return isValid(piece,currow,curcol,dir,destrow,destcol,board);
	}
	/**
	 * overridden isValid method to simplify some calls to isvalid, when Move object is avaialble. 
	 * @param move
	 * @param destrow
	 * @param destcol
	 * @return
	 */
	public boolean isValid(Move move,int destrow, int destcol) {
		Piece piece=move.getPiece();
		Direction dir=move.getDirection();
		int currow=piece.getRow();
		int curcol=piece.getCol();
		return this.isValid(piece, currow, curcol, dir, destrow, destcol);
		
	}
	public static String getStateString(String padding,int[][] board) {
		StringBuilder state=new StringBuilder();
		state.append(padding+"  ");
		
		for(int i=0;i<boardwidth;i++){
			state.append(i+" ");
		}
		state.append("\n"+padding+"0 ");
		
		for(int row=0;row<boardwidth;row++){
			for(int col=0;col<boardheight;col++){
				state.append((char)board[row][col]);
				state.append(" ");
			}
			if(row+1<boardwidth)
			state.append("\n"+padding+(row+1)+" ");
		}
		state.append("\n");
		return state.toString();
	}
	/**
	 * Output current board state as ascii grid of board matrix
	 * @return
	 */
	public String toStateString(){
		return getStateString("",this.board);
	}

	public int getWhiteCaptures() {
		return whiteCaptures;
	}

	public void setWhiteCaptures(int whiteCaptures) {
		this.whiteCaptures = whiteCaptures;
	}

	public int getBlackCaptures() {
		return blackCaptures;
	}

	public void setBlackCaptures(int blackCaptures) {
		this.blackCaptures = blackCaptures;
	}
}
