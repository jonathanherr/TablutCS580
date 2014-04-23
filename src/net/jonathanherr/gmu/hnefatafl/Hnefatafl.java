package net.jonathanherr.gmu.hnefatafl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import net.jonathanherr.gmu.hnefatafl.Player.Result;

/**
 * Main class for hnefatafal game logic. Provides methods for reading and altering the game board by manipulating pieces.
 * Also contains logic for detecting captures and win conditions. Loads board state and initializes board to start game. 
 * play and tournament methods provide main entry points.
 * @author jonathan
 *
 */
public class Hnefatafl {
	/**
	 * Class fields
	 */
	static final String WHITE_NAME = "white";
	static final String BLACK_NAME = "black";
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
	int whiteCaptures=0;
	int blackCaptures=0;
	ArrayList<int[]> escapeNodes=new ArrayList<int[]>();
	private boolean gameOver;
	private String winner;
	private Result winResult;
	private long gameid;
	private GUI gui;
	private Integer capture;
	private String initstate;
	private Player whitePlayer;
	private Player blackPlayer;
	private int[] kingLocation=new int[2];
	
	/**
	 * Accessors
	 * @return
	 */
	public ArrayList<Piece> getWhitepieces() {
		return whitepieces;
	}
	public ArrayList<Piece> getBlackpieces() {
		return blackpieces;
	}
	public void setGUI(GUI gui) {
		this.gui=gui;
	}
	public Player getBlackPlayer() {
		return blackPlayer;
	}
	public void setBlackPlayer(Player blackPlayer) {
		this.blackPlayer = blackPlayer;
	}
	public Player getWhitePlayer() {
		return whitePlayer;
	}
	public void setWhitePlayer(Player whitePlayer) {
		this.whitePlayer = whitePlayer;
	}
	public boolean isGameOver() {
		return this.gameOver;
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
	
	/**
	 * Constructor - reset the board to initial state as read from config file. 
	 */
	public Hnefatafl(){
		reset(true);
	}

	/**
	 * reset board to defaults
	 * @param reloadConfig
	 */
	private void reset(boolean reloadConfig) {
		boardwidth=9;
		boardheight=9;
		blackpieces.clear();
		whitepieces.clear();
		gameOver=false;
		winner="";
		board=new int[boardwidth][boardheight];
		readConfig(reloadConfig);
		
	}
	public static String getStateString(String padding,int[][] board) {
		String state=padding+"  ";
		for(int i=0;i<boardwidth;i++){
			state+=i+" ";
		}
		state+="\n"+padding+"0 ";
		
		for(int row=0;row<boardwidth;row++){
			for(int col=0;col<boardheight;col++){
				state+=(char)board[row][col];
				state+=" ";
			}
			if(row+1<boardwidth)
			state+="\n"+padding+(row+1)+" ";
		}
		state+="\n";
		return state;
	}
	/**
	 * Output current board state as ascii grid of board matrix
	 * @return
	 */
	public String toStateString(){
		return getStateString("",this.board);
	}
	
	/**
	 * Read setup.cfg file and set class vars and load board state from file. 
	 * @param reloadConfig
	 */
	protected void readConfig(boolean reloadConfig){
		String boardstate="";
		try {
			if(reloadConfig){
				BufferedReader props=new BufferedReader(new FileReader(new File("setup.cfg")));
				String header=props.readLine();
				String[] fields=header.split(",");
				for(String field:fields){
					String[] keyvalue=field.split("=");
					String key=keyvalue[0];
					String value=keyvalue[1];
					if(key.equals("width"))
						this.boardwidth=Integer.valueOf(value);
					if(key.equals("height"))
						this.boardheight=Integer.valueOf(value);
					if(key.equals("capture"))
						this.capture=Integer.valueOf(value);
					
				}
				for(int row=0;row<this.boardheight;row++){
					boardstate+=props.readLine()+"\n";
				}
				props.close();
			}
			else
				boardstate=this.initstate;
			
			loadState(boardstate);
			
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
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
			bw.write("height="+String.valueOf(this.boardheight)+",width="+String.valueOf(this.boardwidth)+",capture="+String.valueOf(capture));
			bw.write(this.toStateString());
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
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
		for(Piece piece:blackpieces){
			if(piece.getRow()==row && piece.getCol()==col)
				return piece;
		}
		for(Piece piece:whitepieces){
			if(piece.getRow()==row && piece.getCol()==col){
				return piece;
			}
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
	private void copyBoard(int[][] curBoard,int[][] simBoard) {
		for(int row=0;row<boardheight;row++) {
			for(int col=0;col<boardwidth;col++) {
				simBoard[row][col]=curBoard[row][col];
			}
		}
		
	}
	private void deepCopy(ArrayList<Piece> pieces,
			ArrayList<Piece> simBlackPieces) {
		for(Piece piece:pieces) {
			simBlackPieces.add(piece.copy());
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
			move.getPiece().setPosition(newrow,newcol);
			board[newrow][newcol]=move.getPiece().toChar();
			if(move.getPiece().name.equals(KING_NAME)) {
				kingLocation[0]=newrow;
				kingLocation[1]=newcol;
			}
			
			findCaptures(move);
			checkKingWin();
			return true;
		}
		else
			return false;
		
	}
	/**
	 * Check to see if the king has reached an escape node. 
	 * TODO - notice when king has a move to one or two exits and declare to opponent(like check/checkmate).
	 */
	private void checkKingWin(){
		for(Piece piece:whitepieces){
			if(piece.value==(int)king){
				int[] kingLoc = new int[2];
				kingLoc[0]=piece.getRow();
				kingLoc[1]=piece.getCol();
				for(int[] node:escapeNodes){
					if(node[0]==kingLoc[0] && node[1]==kingLoc[1]){
						System.out.println("White Wins");
						win(piece,null,Result.KINGESCAPE);	
					}
				}				
			}
		}
		
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
	private void findCaptures(Move move){
		Piece movedPiece=move.getPiece();
		//look around piece to see if any piece nearby is an opponent, if so, go in that direction until a wall or another piece of the same color is found
		//looking for a capture situation.
		ArrayList<Piece> neighbors=new ArrayList<>();
		for(Direction dir:Direction.values()){
			Piece neighbor=null;
			neighbors.clear();
			int rowInc=0,colInc=0,startrow=movedPiece.getRow(),startcol=movedPiece.getCol();
			if(dir==Direction.UP || dir==Direction.DOWN)
				rowInc=dir.value;
			else if(dir==Direction.LEFT || dir==Direction.RIGHT)
				colInc=dir.value;
			
			neighbor=this.getPieceAt(startrow+=rowInc, startcol+=colInc);
			while(neighbor!=null && !neighbor.name.equals(movedPiece.name) &&  (!(movedPiece.name.equals(WHITE_NAME) && neighbor.name.equals(KING_NAME))) && !(neighbor.name.equals(KING_NAME) && neighbor.row==throne[0] && neighbor.col==throne[1])){
				neighbors.add(neighbor);
				neighbor=this.getPieceAt(startrow+=rowInc, startcol+=colInc); //returns null if no piece exists at given point. 	
			}
			
			//capture in two cases, we hit an empty square, and that square is a corner(escape) cell or we have surrounded opponents on two sides
			if((neighbor!=null && 
					((neighbor.name.equals(movedPiece.name) || (neighbor.name.equals(WHITE_NAME) && movedPiece.name.equals(KING_NAME))) 
					|| neighbor.name.equals(KING_NAME) && movedPiece.name.equals(WHITE_NAME))) 
					|| (neighbor==null && startrow<boardwidth && startcol<boardheight && getStateAt(startrow,startcol)==escape) 
					&& !neighbors.isEmpty())
			{
				for(Piece opponentPiece: neighbors){
					take(movedPiece,opponentPiece);							
				}
			}
		}
		
		
	}
	/**
	 * Cleanup after win condition is met, declare winner, set winner field and gameover flag. output final board state. 
	 * @param takingPiece
	 * @param takenPiece
	 * @param result
	 */
	private void win(Piece takingPiece,Piece takenPiece, Result result) {
		
		if(takenPiece!=null){
			if(takenPiece.name.equals(KING_NAME)){
				System.out.println(takingPiece.name + " wins by capturing white's king");
				this.winner=BLACK_NAME;
				winResult=Result.KINGCAP;				
			}
		}
		else{
			if(takingPiece.name.equals(WHITE_NAME) || takingPiece.name.equals(KING_NAME)){
				System.out.println("White wins by " + result);
				this.winner=WHITE_NAME;
				winResult=result;
			}
			else{
				System.out.println("Black wins by " + result);
				this.winner=BLACK_NAME;
				winResult=result;
			}
		}
		System.out.println(this.toStateString());
		this.gameOver=true;
		
				
	}
	
	/**
	 * Call to take a piece, do cleanup
	 * @param row
	 * @param col
	 */
	private void take(Piece attackingPiece,Piece takenPiece) {
		
		board[takenPiece.getRow()][takenPiece.getCol()]=blank;
		
		if(blackpieces.contains(takenPiece))
			blackpieces.remove(takenPiece);
		else
			whitepieces.remove(takenPiece);
		
		if(attackingPiece.value==(int)black){
			if(takenPiece.value==(int)king){
				win(attackingPiece,takenPiece,Result.KINGCAP);
			}
			blackCaptures+=1;	
			this.getBlackPlayer().captures+=1;
		}
		else{
			whiteCaptures+=1;
			this.getWhitePlayer().captures+=1;
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
		if(this.throne[0]==destrow && this.throne[1]==destcol && !piece.name.equals(KING_NAME) )
			valid=false;
		//check that destination is not a corner square
		if(destrow==0 && destcol==0 && !piece.name.equals(KING_NAME))
			valid=false;
		if(destrow==0 && destcol==boardwidth-1 && !piece.name.equals(KING_NAME))
			valid=false;
		if(destrow==boardheight-1 && destcol==0 && !piece.name.equals(KING_NAME))
			valid=false;
		if(destrow==boardheight-1 && destcol==boardwidth-1 && !piece.name.equals(KING_NAME))
			valid=false;
		//check that spaces between current and dest spots are empty
		if( destrow>=0 && destcol>=0 && destrow<curboard.length && destcol<curboard[destrow].length){
			if(dir==Direction.UP){
				for(int row=currow+dir.value;row>=destrow;row+=dir.value){
					int cellValue=curboard[row][curcol];
					if(cellValue!=(int)blank && !(cellValue==(int)escape && piece.name.equals(KING_NAME))){						
						valid=false;
					}
				}
			}
			else if(dir==Direction.DOWN){
				for(int row=currow+dir.value;row<=destrow;row+=dir.value){
					int cellValue = curboard[row][curcol];
					if(cellValue!=(int)blank && !(cellValue==(int)escape && piece.name.equals(KING_NAME))){
						valid=false;
					}
				}
			}
			else if(dir==Direction.LEFT){
				for(int col=curcol+dir.value;col>=destcol;col+=dir.value){
					int cellValue = curboard[currow][col];
					if(cellValue!=(int)blank && !(cellValue==(int)escape && piece.name.equals(KING_NAME))){
						valid=false;
					}
				}
			}
			else if(dir==Direction.RIGHT){
				for(int col=curcol+dir.value;col<=destcol;col+=dir.value){
					int cellValue = curboard[currow][col];
					if(cellValue!=(int)blank && !(cellValue==(int)escape && piece.name.equals(KING_NAME))){
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
	/**
	 * Main gameloop. Takes two player objects, assumes they implement the turn method declared in Player object. 
	 * Plays game number of Games, allowing a total of maxturns to be expended between each player. delay allows for 
	 * a display delay to be introduced via Thread.sleep for debug/tracking. 
	 * @param white - Player object
	 * @param black - 2nd Player object
	 * @param games - number of games to play
	 * @param maxturns - max turns shared between both players
	 * @param delay - artificial display for debug/display purposes
	 * @throws InterruptedException - thrown only if thread.sleep is interrupted
	 */
	public void play(Player white, Player black, int games, int maxturns, int delay) throws InterruptedException{
		boolean debug=true;
		white.setColor(WHITE_NAME);
		black.setColor(BLACK_NAME);
		this.setWhitePlayer(white);
		this.setBlackPlayer(black);
		for(int gameNum=0;gameNum<games;gameNum++){
			int turns=0;
			reset(false);
			gameid=new Random().nextLong();
			double start=System.nanoTime();
			while(turns<maxturns){
				System.out.println("White's turn. Turn " + (turns+1));
				Move whitemove=white.turn();
				if(whitemove!=null)
					move(white,whitemove);
				if(!gameOver) {
					System.out.println("Black's turn. Turn " + (turns+1));
					Move blackmove=black.turn();
					if(blackmove!=null)
						move(black,blackmove);
				}
				if(gameOver){
					double end=System.nanoTime();
					if(turns<3){
						System.out.println(this.toStateString());
					}
					if(winner.equals(BLACK_NAME)){
						white.addPlayedGame(gameid,false,WHITE_NAME, turns, end-start,Result.LOSS);
						black.addPlayedGame(gameid,true,BLACK_NAME,turns,end-start,winResult);
					}
					else{
						black.addPlayedGame(gameid,false,BLACK_NAME, turns, end-start,Result.LOSS);
						white.addPlayedGame(gameid,true,WHITE_NAME,turns,end-start,winResult);						
					}
					break;
				}
				turns+=1;
				
				if(gui!=null)
					gui.updateBoard(board);			
				Thread.sleep(delay);
				if(debug){
					
					System.out.println("Game:" + gameNum + "\tturn:"+turns);
					System.out.println("Board Score White:" + white.evaluate(new BoardState(board, getBlackpieces(), getWhitepieces())));
					System.out.println("Board Score Black:" + black.evaluate(new BoardState(board,getBlackpieces(),getWhitepieces())));
					System.out.println(this.toStateString());
				}				
			}
			if(!gameOver){ //we ran out of turns
				double end=System.nanoTime();
				black.addPlayedGame(gameid, false, BLACK_NAME, turns, end-start, Result.DRAW);
				white.addPlayedGame(gameid, false, WHITE_NAME, turns, end-start, Result.DRAW);
			}
			
			this.saveState();
		}
		
	}
	/**
	 * tournament runner runs games and tracks statistics. 
	 * @param rounds
	 */
	public void tournament(int rounds){
		
	}
	/**
	 * Main entry, runs a default number of games between two RandomPlayer players. 
	 * @param args
	 * @throws InterruptedException
	 */
	public static void main(String... args) throws InterruptedException{
		
		Hnefatafl game=new Hnefatafl();
		
		playMinimax(game);
		//playRandom(game);
		
		
	}
	private static void playMinimax(Hnefatafl game) throws InterruptedException {
		System.out.println(game.toStateString());
		RandomPlayer black=new RandomPlayer(game, game.blackpieces);
		MiniMaxPlayer white = new MiniMaxPlayer(game,game.whitepieces);
		game.play(white, black, 1, 50,0);
		
		for(Outcome result:black.games){
			System.out.println(result);
		}
		for(Outcome result:white.games){
			System.out.println(result);
		}
	}
	private static void playRandom(Hnefatafl game) throws InterruptedException {
		System.out.println(game.toStateString());
		RandomPlayer black=new RandomPlayer(game, game.blackpieces);
		RandomPlayer white = new RandomPlayer(game,game.whitepieces);
		game.play(white, black, 1000, 1000,0);
		try {
			BufferedWriter bw=new BufferedWriter(new FileWriter(new File("results.txt")));
			for(Outcome result:black.games){
				bw.write(result.toString());
			}
			for(Outcome result:white.games){
				bw.write(result.toString());
			}
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
	
}
