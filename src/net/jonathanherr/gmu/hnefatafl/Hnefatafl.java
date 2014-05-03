package net.jonathanherr.gmu.hnefatafl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import net.jonathanherr.gmu.hnefatafl.Player.Result;

import com.google.common.collect.ImmutableMap;

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
	private String setupfile;
	
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
		board=new Board();
		setupfile="setup7x7.cfg";
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
		board.gameOver=false;
		board.winner="";
		board.winResult=null;
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
				BufferedReader props=new BufferedReader(new FileReader(new File(this.setupfile)));
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
		getBoard().debug=debug;
		white.setColor(Board.WHITE);
		black.setColor(Board.BLACK);
		this.setWhitePlayer(white);
		this.setBlackPlayer(black);
		HashMap<String, Double> featureWeights=new HashMap<String,Double>();
		
		for(int gameNum=0;gameNum<games;gameNum++){
			int turns=0;
			reset(false);
			gameid=new Random().nextLong();
			double start=System.nanoTime();
			while(turns<maxturns){
				System.out.println("White's turn. Turn " + (turns+1));
				Move whitemove=white.turn(turns);
				if(whitemove!=null)
					getBoard().move(white,whitemove);
				this.whitePlayer.captures+=getBoard().getWhiteCaptures();
				gameOver=getBoard().gameOver;
				winner=getBoard().winner;
				winResult=getBoard().winResult;
				if(!gameOver) {
					System.out.println("Black's turn. Turn " + (turns+1));
					Move blackmove=black.turn(turns);
					if(blackmove!=null)
						getBoard().move(black,blackmove);
					blackPlayer.captures+=getBoard().getBlackCaptures();
					gameOver=getBoard().gameOver;
					winner=getBoard().winner;
					winResult=getBoard().winResult;
					if(winner.equals("black") && gameOver && winResult.equals("KINGCAP")){
						System.out.println("blah");
						System.out.println(getBoard().getStateString("", getBoard().board));
					}
				}
				if(gameOver){
					double end=System.nanoTime();
					
					if(winner.equals(Board.BLACK)){
						white.addPlayedGame(gameid,false,Board.WHITE, turns, end-start,Result.LOSS);
						black.addPlayedGame(gameid,true,Board.BLACK,turns,end-start,winResult);
					}
					else{
						
						black.addPlayedGame(gameid,false,Board.BLACK, turns, end-start,Result.LOSS);
						white.addPlayedGame(gameid,true,Board.WHITE,turns,end-start,winResult);						
					}
					break;
				}
				turns+=1;
				
				if(gui!=null)
					gui.updateBoard(getBoard().getBoardGrid());			
				Thread.sleep(delay);
				if(debug){
					
					System.out.println("Game:" + gameNum + "\tturn:"+turns);
					System.out.println(getBoard().toStateString());
				}				
			}
			if(!gameOver){ //we ran out of turns
				double end=System.nanoTime();
				black.addPlayedGame(gameid, false, Board.BLACK, turns, end-start, Result.DRAW);
				white.addPlayedGame(gameid, false, Board.WHITE, turns, end-start, Result.DRAW);
			}
			
			//this.getBoard().saveState();
		}
		this.getWhitePlayer().save("minimax_white.plr");
		this.getBlackPlayer().save("minimax_black.plr");
		try {
			this.setWhitePlayer(Player.openPlayer("minimax_white.plr"));
			this.setBlackPlayer(Player.openPlayer("minimax_black.plr"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	/**
	 * tournament runner runs games and tracks statistics. 
	 * @param rounds
	 */
	public void tournament(){
		int games=100;
		int turns=100;
		
		ArrayList<String> playerConfigs=null;
		HashMap<String,MiniMaxPlayer> players=new HashMap<String,MiniMaxPlayer>();
		try {
			playerConfigs=(ArrayList<String>) Files.readAllLines(FileSystems.getDefault().getPath("cannedplayers.cfg"),Charset.forName("UTF8"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for(String config:playerConfigs){
			if(!config.trim().equals("")){
				String[] kv=config.split("=");
				String[] parts=kv[0].split("_");
				Double weight=Double.valueOf(kv[1]);
				String side=parts[0];
				String name=parts[1];
				String feature=parts[2];
				ArrayList<Piece> pieces=null;
				if(side.equals("white"))
					pieces=getBoard().getWhitepieces();
				else
					pieces=getBoard().getBlackpieces();
				
				if(players.containsKey(name)){
					players.get(name).addFeature(feature, weight);
				}
				else{
					MiniMaxPlayer player=new MiniMaxPlayer(this, pieces);
					player.setColor(side);
					player.name=name;
					player.addFeature(feature,weight);
					players.put(name,player);
				}
			}
			for(MiniMaxPlayer player:players.values()){
				for(MiniMaxPlayer player2:players.values()){
					if(!player.name.equals(player2.name)){
						if(player.getColor().equals("white"))
							try {
								this.play(player, player2, games, turns, 0);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
					}
				}	
			}
		}
	}
	
	/**
	 * Main entry, runs a default number of games between two RandomPlayer players. 
	 * @param args
	 * @throws InterruptedException
	 */
	public static void main(String... args) throws InterruptedException{
		
		Hnefatafl game=new Hnefatafl();
		int searchDepth=3;
		if(args.length>0)
			searchDepth=Integer.valueOf(args[0]);
		System.out.println("Playing minimax vs random with minimax depth of " + searchDepth);
		//playMinimax(game,searchDepth);
		game.tournament();
	}
	/**
	 * Start a game with one minimax player and one random player. 
	 * @param game
	 * @throws InterruptedException
	 */
	private static void playMinimax(Hnefatafl game, int depth) throws InterruptedException {
		System.out.println(game.getBoard().toStateString());
		RandomPlayer black=new RandomPlayer(game, game.getBoard().blackpieces);
		//NoopPlayer black = new NoopPlayer(game, game.getBoard().blackpieces,"noop");
		//MiniMaxPlayer black = new MiniMaxPlayer(game, game.getBoard().blackpieces);
		MiniMaxPlayer white = new MiniMaxPlayer(game,game.getBoard().whitepieces);
		white.searchDepth=depth;
		
		
		//white.setFeatureWeights(featureWeights);
		//black.setFeatureWeights(featureWeights);
		
		int games=100;
		int turns=100;
		int delay=0;
		game.play(white, black, games, turns,delay);
		
		try {
			BufferedWriter bw=new BufferedWriter(new FileWriter(new File("results_minimax_"+games+"_"+turns+".txt")));
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
