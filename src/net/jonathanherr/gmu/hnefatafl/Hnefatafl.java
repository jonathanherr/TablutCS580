package net.jonathanherr.gmu.hnefatafl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
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
	
	int whiteCaptures=0;
	int blackCaptures=0;
	private boolean gameOver;
	private String winner;
	private Result winResult;
	private long gameid;
	private GUI gui;
	private String initstate;
	private Player whitePlayer;
	private Player blackPlayer;
	private Board board;
	
	/**
	 * Accessors
	 * @return
	 */
	
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
	 * Constructor - reset the board to initial state as read from config file. 
	 */
	public Hnefatafl(){
		board=new Board(this);
		reset(true);
	}

	/**
	 * reset board to defaults
	 * @param reloadConfig
	 */
	private void reset(boolean reloadConfig) {
		gameOver=false;
		winner="";
		board.reset();
		readConfig(reloadConfig);
		
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
					if(key.equals("width")) {
						this.getBoard();
						Board.boardwidth=Integer.valueOf(value);
					}
					if(key.equals("height")) {
						this.getBoard();
						Board.boardheight=Integer.valueOf(value);
					}
				}
				for(int row=0;row<Board.boardheight;row++){
					boardstate+=props.readLine()+"\n";
				}
				props.close();
				board.initstate=boardstate;
			}
			else
				boardstate=board.initstate;
			
			getBoard().loadState(boardstate);
			
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
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
		white.setColor(getBoard().WHITE);
		black.setColor(getBoard().BLACK);
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
					getBoard().move(white,whitemove);
				gameOver=getBoard().gameOver;
				winner=getBoard().winner;
				winResult=getBoard().winResult;
				if(!gameOver) {
					System.out.println("Black's turn. Turn " + (turns+1));
					Move blackmove=black.turn();
					if(blackmove!=null)
						getBoard().move(black,blackmove);
					gameOver=getBoard().gameOver;
					winner=getBoard().winner;
					winResult=getBoard().winResult;
				}
				if(gameOver){
					double end=System.nanoTime();
					if(turns<3){
						System.out.println(this.getBoard().toStateString());
					}
					if(winner.equals(getBoard().BLACK)){
						white.addPlayedGame(gameid,false,getBoard().WHITE, turns, end-start,Result.LOSS);
						black.addPlayedGame(gameid,true,getBoard().BLACK,turns,end-start,winResult);
					}
					else{
						black.addPlayedGame(gameid,false,getBoard().BLACK, turns, end-start,Result.LOSS);
						white.addPlayedGame(gameid,true,getBoard().WHITE,turns,end-start,winResult);						
					}
					break;
				}
				turns+=1;
				
				if(gui!=null)
					gui.updateBoard(getBoard().getBoardGrid());			
				Thread.sleep(delay);
				if(debug){
					
					System.out.println("Game:" + gameNum + "\tturn:"+turns);
					System.out.println("Board Score White:" + white.evaluate(new BoardState(getBoard().getBoardGrid(),getBoard().getBlackpieces(),getBoard().getWhitepieces())));
					System.out.println("Board Score Black:" + black.evaluate(new BoardState(getBoard().getBoardGrid(),getBoard().getBlackpieces(),getBoard().getWhitepieces())));
					System.out.println(getBoard().toStateString());
				}				
			}
			if(!gameOver){ //we ran out of turns
				double end=System.nanoTime();
				black.addPlayedGame(gameid, false, getBoard().BLACK, turns, end-start, Result.DRAW);
				white.addPlayedGame(gameid, false, getBoard().WHITE, turns, end-start, Result.DRAW);
			}
			
			//this.getBoard().saveState();
		}
		
	}
	/**
	 * tournament runner runs games and tracks statistics. 
	 * @param rounds
	 */
	public void tournament(int rounds){
		int blackWins=0;
		int whiteWins=0;
		for(int round=0;round<rounds;round++){
			
		}
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
		System.out.println(game.getBoard().toStateString());
		RandomPlayer black=new RandomPlayer(game, game.getBoard().blackpieces);
		MiniMaxPlayer white = new MiniMaxPlayer(game,game.getBoard().whitepieces);
		game.play(white, black, 10, 150,0);
		
		for(Outcome result:black.games){
			System.out.println(result);
		}
		for(Outcome result:white.games){
			System.out.println(result);
		}
	}
	private static void playRandom(Hnefatafl game) throws InterruptedException {
		System.out.println(game.getBoard().toStateString());
		RandomPlayer black=new RandomPlayer(game, game.getBoard().getBlackpieces());
		RandomPlayer white = new RandomPlayer(game,game.getBoard().whitepieces);
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
	public Board getBoard() {
		return board;
	}
	public void setBoard(Board board) {
		this.board = board;
	}
	
	
	
}
