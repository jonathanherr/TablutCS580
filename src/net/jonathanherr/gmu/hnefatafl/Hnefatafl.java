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
	public Board getBoard() {
		return board;
	}
	public void setBoard(Board board) {
		this.board = board;
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
	 * Constructor - reset the board to initial state as read from config file. 
	 */
	public Hnefatafl(){
		board=new Board();
		setupfile="config/setup7x7.cfg";
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
		blackCaptures=0;
		whiteCaptures=0;
		board.setBlackCaptures(0);
		board.setWhiteCaptures(0);
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
				this.whitePlayer.captures=getBoard().getWhiteCaptures();
				gameOver=getBoard().gameOver;
				winner=getBoard().winner;
				winResult=getBoard().winResult;
				if(!gameOver) {
					System.out.println("Black's turn. Turn " + (turns+1));
					Move blackmove=black.turn(turns);
					if(blackmove!=null)
						getBoard().move(black,blackmove);
					blackPlayer.captures=getBoard().getBlackCaptures();
					gameOver=getBoard().gameOver;
					winner=getBoard().winner;
					winResult=getBoard().winResult;
					
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
					black.gameover();
					white.gameover();
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
			
		}
		
		
	}
	/**
	 * tournament runner runs games and tracks statistics. 
	 * @param rounds
	 */
	public void tournament(){
		int games=100;
		int turns=100;
		
		HashMap<String, MiniMaxPlayer> players = readPlayerConfigs("cannedplayers.cfg");
		for(MiniMaxPlayer player:players.values()){
			for(MiniMaxPlayer player2:players.values()){
				if(!player.name.equals(player2.name)){
					try {
						System.out.println(player.name + " vs " + player2.name);
						this.play(player, player2, games, turns, 0);
						try {
							BufferedWriter bw=new BufferedWriter(new FileWriter(new File("results_minimax_"+player.name + "_" + player2.name+"_"+games+"_"+turns+".txt")));
							for(Outcome result:player.games){
								bw.write(result.toString());
							}
							for(Outcome result:player2.games){
								bw.write(result.toString());
							}
							bw.close();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}	
		}
	}
	/**
	 * read player definitions from config file
	 * @return
	 */
	private HashMap<String, MiniMaxPlayer> readPlayerConfigs(String configfilename) {
		ArrayList<String> playerConfigs=null;
		HashMap<String,MiniMaxPlayer> players=new HashMap<String,MiniMaxPlayer>();
		try {
			playerConfigs=(ArrayList<String>) Files.readAllLines(FileSystems.getDefault().getPath(configfilename),Charset.forName("UTF8"));
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
		}
		return players;
	}
	
	/**
	 * Main entry, runs a default number of games between two of the requested types. 
	 * Currently only games played between the same type of algorithm are supported at the commandline. 
	 * @param args
	 * @throws InterruptedException
	 */
	public static void main(String... args) throws InterruptedException{
		
		Hnefatafl game=new Hnefatafl();
		String type="ANN"; //valid types - ANN, MM, RAND
		int games=100;
		int turns=150;
		int depth=3;
		if(args.length>0)
			type=args[0];
		if(args.length>1)
			games=Integer.valueOf(args[1]);
		if(args.length>2)
			turns=Integer.valueOf(args[2]);
		if(args.length>3){
			depth=Integer.valueOf(args[3]);
			if(depth>3)
				depth=3;
		}
		System.out.println("\nusage:java -jar Tablut.jar type=<ANN|MM|RAND> NumGames=int NumTurns=int searchdepth=int(<4) - \nCurrently only games played between the same type of algorithm are supported at the commandline\nRunning without arguments runs with type ANN(neural net) using the 1000 game trained nets for 100 games of 150 turns and depth 1\nNote - be careful with outputs, we clobber liberally right now.\n\n");
		
		
		if(type.equals("ANN")){
			depth=1;
			ANN_Player white=new ANN_Player(game,game.getBoard().whitepieces);
			white.readNeuralNet("savednets/white/ANN_1000.nn");
			white.searchDepth=depth;
			ANN_Player black=new ANN_Player(game,game.getBoard().blackpieces);
			black.readNeuralNet("savednets/black/ANN_black_1000.nn");
			white.searchDepth=depth;
			game.playMatch(white,black,games,turns);
		}
		else if(type.equals("MM")){
			game.playMinimax(depth, games, turns);
		}
		else if(type.equals("RAND")){
			game.playRandom(games, turns);
		}
		else if(type.equals("TEST")){
			//for testing only, do not use. 
			depth=1;
			MiniMaxPlayer white=new MiniMaxPlayer(game,game.getBoard().whitepieces);
			white.readFeatures("config/whitefeature1.txt");
			white.searchDepth=3;
			ANN_Player black=new ANN_Player(game,game.getBoard().blackpieces);
			black.readNeuralNet("savednets/black/ANN_black_1000.nn");
			black.searchDepth=depth;
			game.playMatch(white,black,games,turns);
			
		}
	}
	/**
	 * play any two players. no special considerations.
	 * @param white
	 * @param black
	 * @param games
	 * @param turns
	 * @throws InterruptedException
	 */
	private void playMatch(Player white, Player black, int games, int turns) throws InterruptedException{
		int delay=0;
		this.play(white, black, games, turns,delay);
		try {
			BufferedWriter bw=new BufferedWriter(new FileWriter(new File("reports/results_"+white.type+"_"+black.type+"_"+games+"_"+turns+".txt")));
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
	/**
	 * Play the white trained neural net against any black player
	 * @param black
	 * @param games
	 * @param turns
	 * @throws InterruptedException
	 */
	private void playANN(Player black, int games, int turns) throws InterruptedException {
		ANN_Player white=new ANN_Player(this, this.getBoard().whitepieces);
		white.training=false;
		white.readNeuralNet("savednets/white/ANN_1000.nn");
		playMatch(white,black,games,turns);
	}
	/**
	 * 
	 * @param game
	 * @throws InterruptedException
	 */
	private void playSimple() throws InterruptedException {
		SimplePlayer white=new SimplePlayer(this, this.getBoard().whitepieces);
		RandomPlayer black=new RandomPlayer(this, this.getBoard().blackpieces);
		white.readFeatures("config/whitefeature1.txt");
		int games=100;
		int turns=1000;
		int delay=0;
		this.play(white, black, games, turns,delay);
		
		try {
			BufferedWriter bw=new BufferedWriter(new FileWriter(new File("reports/results_simple_random_"+games+"_"+turns+".txt")));
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
	/**
	 * Play neural nets trained on each side(black/white) against each other, testing neural nets from different points in their
	 * development to see where learning plateaued, if at all. Reads neural nets from ./savednets/white and ./savednets/black. 
	 * Please each player against each other 100 times with max turns set to 150. 
	 * @param game
	 * @param depth
	 */
	private void tournamentOfNeuralNets(int depth){
		ArrayList<ANN_Player> whiteplayers=new ArrayList<ANN_Player>();
		ArrayList<ANN_Player> blackplayers=new ArrayList<ANN_Player>();
		
		for(File net:new File("./savednets/white").listFiles()){
			ANN_Player ann=new ANN_Player(this, this.getBoard().whitepieces);
			ann.type="white_"+net.getName().replace(".nn","");
			ann.readNeuralNet(net.getAbsolutePath());
			ann.searchDepth=depth;
			whiteplayers.add(ann);
		}
		for(File net:new File("./savednets/black").listFiles()){
			ANN_Player ann=new ANN_Player(this, this.getBoard().blackpieces);
			ann.type="black_"+net.getName().replace(".nn","");
			ann.readNeuralNet(net.getAbsolutePath());
			ann.searchDepth=depth;
			blackplayers.add(ann);
		}
		for(ANN_Player white:whiteplayers){
			for(ANN_Player black:blackplayers){
				try {
					this.playMatch(white,black,100,150);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	/**
	 * Start a game with one minimax player and one random player. 
	 * @param game
	 * @throws InterruptedException
	 */
	private void playMinimax(int depth, int games, int turns) throws InterruptedException {
		System.out.println(this.getBoard().toStateString());
		
		MiniMaxPlayer black = new MiniMaxPlayer(this, this.getBoard().blackpieces);
		black.readFeatures("blackfeature1.txt");
		MiniMaxPlayer white = new MiniMaxPlayer(this,this.getBoard().whitepieces);
		white.readFeatures("whitefeature1.txt");
		white.searchDepth=depth;
		black.searchDepth=depth;
		
		int delay=0;
		this.play(white, black, games, turns,delay);
		
		try {
			BufferedWriter bw=new BufferedWriter(new FileWriter(new File("reports/results_minimax_"+games+"_"+turns+".txt")));
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
	/**
	 * Start a game with 2 random players and run for games games with turns max turns. write results to 'results_random_games_turns.txt'
	 * @param game
	 * @throws InterruptedException
	 */
	private void playRandom(int games, int turns) throws InterruptedException {
		System.out.println(this.getBoard().toStateString());
		RandomPlayer black=new RandomPlayer(this, this.getBoard().getBlackpieces());
		RandomPlayer white = new RandomPlayer(this,this.getBoard().whitepieces);
		this.play(white, black, games, turns,0);
		try {
			BufferedWriter bw=new BufferedWriter(new FileWriter(new File("reports/results_random_"+games+"_"+turns+".txt")));
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
