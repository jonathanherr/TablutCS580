package net.jonathanherr.gmu.hnefatafl;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * basic player class. supports basic operations for controlling any piece on the board. 
 * @author jonathan
 *
 */
public abstract class 
Player {
	enum Result {KINGCAP, KINGESCAPE, ALLCAP, LOSS, WIN, DRAW};
	protected ArrayList<Move> moves;
	protected Hnefatafl game;
	protected ArrayList<Piece> pieces;
	protected ArrayList<Outcome> games; 
	protected ArrayList<Double> featureWeights;
	
	protected HashMap<String,ArrayList<Double>> transposeTable;
	protected int featureCount=7; //dist from king, dist from corner, dist from opponent, number pieces remaining, part of a capture, king can see escape
	protected int captures;
	private int wins;
	private String type;
	private String color;
	
	public ArrayList<Double> getFeatureWeights() {
		return featureWeights;
	}
	public void setFeatureWeights(ArrayList<Double> featureWeights) {
		this.featureWeights = featureWeights;
	}
	public ArrayList<Move> getMoves() {
		return moves;
	}
	public Hnefatafl getGame() {
		return game;
	}
	public ArrayList<Piece> getPieces() {
		return pieces;
	}
	public ArrayList<Outcome> getGames() {
		return games;
	}
	public int getWins() {
		return wins;
	}
	public String getType() {
		return type;
	}
	
	
	public Player(Hnefatafl game, ArrayList<Piece> pieces, String name){
		this.game=game;
		this.pieces=pieces;
		this.type=name;
		transposeTable=new HashMap<String,ArrayList<Double>>();
		moves=new ArrayList<Move>();
		games=new ArrayList<Outcome>();
		featureWeights=new ArrayList<Double>();
		for(int feature=0;feature<featureCount;feature++) {
			featureWeights.add(1.0d);
		}
	}
	public Move turn(){
		return null;
	}
	public Outcome getLastOutcome(){
		return games.get(games.size()-1);
	}
	public void addPlayedGame(long gameid,boolean wonGame,String side, int moveCount, double gameTime, Result result){
		Outcome outcome=new Outcome(gameid,type,side,wonGame,moveCount,gameTime,result);
		games.add(outcome);
		
	}
	/**
	 * return a score for this board for the given player. 
	 * Uses several simple features, all weighted at 1 here
	 * White:
	 * 	Distance from Corner - boards where more white pieces are heading toward the corner should be more valuable
	 *  Distance from Opponent - boards where white pieces are near black pieces have more chance of capture
	 *  Distance from King - boards where white pieces are defending the king are more valuable
	 *  taking part in a capture
	 *  Number of Pieces
	 * Black:
	 *  Distance from Opponent - boards where black is nearest white pieces are higher value b/c of increased captures
	 *  Distance from King - boards where black piece is near king are higher value for capturing king
	 *  Distance from corner - black blocks corners for higher value boards
	 *  taking part in a capture
	 *  Number of Pieces
	 *  Additionally, any board won by the other player, is given neg infinity score for that player and positive for the winner.
	 * @param player
	 * @return
	 */
	public int found=0;
	private boolean debug=false;
	public double evaluate(BoardState board) {
		
		ArrayList<Double> pieceScores=new ArrayList<>(); //for debug/tracking, not necessary
		if(this.getPieceColor().equals(Board.WHITE)) {
			evaluateWhite(board, pieceScores);
		}
		else if(this.getPieceColor().equals(Board.BLACK)) {
			evaluateBlack(board, pieceScores);
		}
		
		double totalScore=0.0d;
		for(Double score:pieceScores) {
			totalScore+=score;
		}
		
		return totalScore;
	}
	/**
	 * Evaluate opponent score
	 * @param state
	 * @return
	 */
	public double evaluateOpponent(BoardState state) {
		ArrayList<Double> pieceScores=new ArrayList<>(); //for debug/tracking, not necessary
		if(this.getColor().equals("white"))
			evaluateBlack(state, pieceScores);
		else
			evaluateWhite(state,pieceScores);
		double totalScore=0.0d;
		
		for(Double score:pieceScores) {
			totalScore+=score;
		}
		//System.out.println("Score:" + totalScore);
		//System.out.println(game.getStateString("", state.board));
		
		return totalScore;
	}
	private void evaluateBlack(BoardState board, ArrayList<Double> pieceScores) {
		
		if(board.isGameOver() && board.winner.equals("white"))
		{
			pieceScores.add(Double.NEGATIVE_INFINITY);
			return;
		}
		else if(board.isGameOver() && board.winner.equals("black"))
		{
			pieceScores.add(Double.POSITIVE_INFINITY);
			return;
		}
		
		for(Piece piece:board.getBlackpieces()) {
			double score=0.0d;
			score+=(1.0d/(double) distanceFromCorner(piece))*featureWeights.get(0);
			score+=(1.0d/(double) distanceFromKing(board, piece))*featureWeights.get(1);
			score+=(1.0d/(double) distanceFromOpponent(board,game.getBoard().getWhitepieces(), piece))*featureWeights.get(2);
			pieceScores.add(score);
		}
		pieceScores.add(board.getBlackpieces().size()*featureWeights.get(3)); //overall board position scores go in the last field of the piecescores list
		
		
		
		
	}
	private void evaluateWhite(BoardState board, ArrayList<Double> pieceScores) {
		//for white, each piece, except king, accumulates points based on several factors, proximity to king, proximity to exit nodes and proximity to opponents
		//total score for board is sum of white piece scores. all scores normalized by board width.
		if(board.isGameOver() && board.winner.equals("white"))
		{
			pieceScores.add(Double.POSITIVE_INFINITY);
			return;
		}
		else if(board.isGameOver() && board.winner.equals("black"))
		{
			pieceScores.add(Double.NEGATIVE_INFINITY);
			return;
		}
		if(this.debug)
			System.out.println("scoring this:\n"+ Board.getStateString("", board.board));
		for(Piece piece:board.getWhitepieces()) {
			if(!piece.getName().equals(Board.KING_NAME)) {
				double score=0.0d;
				if(this.debug) {
				System.out.println(piece.getRow() + ","+piece.getCol()+ " distance from corner:" + distanceFromCorner(piece));
				System.out.println(piece.getRow() + ","+piece.getCol()+ " distance from king:" + distanceFromKing(board, piece));
				System.out.println(piece.getRow() + ","+piece.getCol()+ " distance from opponent:" + distanceFromOpponent(board,game.getBoard().getBlackpieces(), piece));
				}
				score+=(1.0d/(double) distanceFromCorner(piece))*featureWeights.get(0);
				score+=(1.0d/(double) distanceFromKing(board, piece))*featureWeights.get(1);
				score+=(1.0d/(double) distanceFromOpponent(board,game.getBoard().getBlackpieces(), piece))*featureWeights.get(2);
				pieceScores.add(score);
			}
			else {
				pieceScores.add((1.0d/(double)distanceFromCorner(piece))*featureWeights.get(4));
				if(pathToExit(board, piece)) {
					pieceScores.add(1*featureWeights.get(6));
				}
			}
			if(inCapture(board,piece)) {
				pieceScores.add(1*featureWeights.get(5));
			}
		}
		pieceScores.add(board.getWhitepieces().size()*featureWeights.get(3)); //overall board position scores go in the last field of the piecescores list
		
	}
	/**
	 * Manhattan distance from all opponents - sum of distance from each opponent
	 * @param board
	 * @param opponentPieces
	 * @param piece
	 * @return
	 */
	protected int distanceFromOpponent(BoardState board,ArrayList<Piece> opponentPieces, Piece piece) {
		int oppDistTotal=0;
		for(Piece opppiece:opponentPieces) {
			oppDistTotal+=game.getBoard().getManhattanDistance(piece.getRow(), piece.getCol(), opppiece.getRow(), opppiece.getCol());
		}
		return oppDistTotal;
	}
	/**
	 * Manhattan distance from nearest corner
	 * @param piece
	 * @return
	 */
	protected int distanceFromCorner(Piece piece) {
		int nearestCorner=game.getBoard().boardwidth+1;
		for(int[] escNode:game.getBoard().escapeNodes) {
			int dist=game.getBoard().getManhattanDistance(piece.getRow(), piece.getCol(), escNode[0], escNode[1]);
			if(dist<nearestCorner)
				nearestCorner=dist;
		}
		return nearestCorner;
	}
	/**
	 * Manhattan Distance from King
	 * @param board
	 * @param piece
	 * @return
	 */
	protected int distanceFromKing(BoardState board, Piece piece) {
		int distFromKing;
		distFromKing=game.getBoard().getManhattanDistance(piece.getRow(), piece.getCol(), board.getKingLocation()[0], board.getKingLocation()[1]);
		return distFromKing;
	}
	/**
	 * Determine if the piece is part of a capture move
	 * @param board
	 * @param piece
	 * @return
	 */
	protected boolean inCapture(BoardState board, Piece piece) {
		Board simBoard=new Board(game);
		simBoard.setBlackpieces(board.getBlackpieces());
		simBoard.setWhitepieces(board.getWhitepieces());
		simBoard.setBoard(board.board);
		if(simBoard.findCaptures(piece).size()>0)
			return true;
		return false;
	}
	/**
	 * Applies only to king, determine if the king has a path to any exit
	 * @param board
	 * @param piece
	 * @return
	 */
	protected boolean pathToExit(BoardState board, Piece piece) {
		return game.getBoard().kingHasPathToEscape(piece);
	}
	
	public String getPieceColor(){
		return getColor();
	}
	public void setColor(String color) {
		this.color=color;
		
	}
	public String getColor() {
		return color;
	}
	public Move turn(int turnNumber) {
		// TODO Auto-generated method stub
		return null;
	}
	
}
