package net.jonathanherr.gmu.hnefatafl;

import java.util.ArrayList;

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
	protected int featureCount=4;
	protected int captures;
	private int wins;
	private String type;
	private String color;
	
	
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
		moves=new ArrayList<Move>();
		games=new ArrayList<Outcome>();
		featureWeights=new ArrayList<Double>();
		for(int feature=0;feature<featureCount;feature++) {
			featureWeights.add(1.0d);
		}
		featureWeights.set(3, 2.0d);
	}
	public Move turn(){
		return null;
	}
	public Outcome getLastOutcome(){
		return games.get(games.size()-1);
	}
	public void addPlayedGame(long gameid,boolean wonGame,String side, int moveCount, double gameTime, Result result){
		if(wonGame)
			this.wins+=1;
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
	 * Black:
	 *  Distance from Opponent - boards where black is nearest white pieces are higher value b/c of increased captures
	 *  Distance from King - boards where black piece is near king are higher value for capturing king
	 *  Distance from corner - black blocks corners for higher value boards
	 * @param player
	 * @return
	 */
	public double evaluate(BoardState board) {
		
		ArrayList<Double> pieceScores=new ArrayList<>(); //for debug/tracking, not necessary
		if(this.getPieceColor().equals(Hnefatafl.WHITE_NAME)) {
			evaluateWhite(board, pieceScores);
		}
		else if(this.getPieceColor().equals(Hnefatafl.BLACK_NAME)) {
			evaluateBlack(board, pieceScores);
		}
		double totalScore=0.0d;
		for(Double score:pieceScores) {
			totalScore+=score;
		}
		//System.out.println("Score for board below:" + totalScore);
		//System.out.println(game.getStateString("", board.board));
		
		return totalScore;
	}
	private void evaluateBlack(BoardState board, ArrayList<Double> pieceScores) {
		for(Piece piece:board.getBlackpieces()) {
			double score=0.0d;
			score+=(1.0d/(double) distanceFromCorner(piece))*featureWeights.get(0);
			score+=(1.0d/(double) distanceFromKing(board, piece))*featureWeights.get(1);
			score+=(1.0d/(double) distanceFromOpponent(board,game.getWhitepieces(), piece))*featureWeights.get(2);
			pieceScores.add(score);
		}
	}
	private void evaluateWhite(BoardState board, ArrayList<Double> pieceScores) {
		//for white, each piece, except king, accumulates points based on several factors, proximity to king, proximity to exit nodes and proximity to opponents
		//total score for board is sum of white piece scores. all scores normalized by board width.
		for(Piece piece:board.getWhitepieces()) {
			if(!piece.name.equals(Hnefatafl.KING_NAME)) {
				double score=0.0d;
				score+=(1.0d/(double) distanceFromCorner(piece))*featureWeights.get(0);
				score+=(1.0d/(double) distanceFromKing(board, piece))*featureWeights.get(1);
				score+=(1.0d/(double) distanceFromOpponent(board,game.getBlackpieces(), piece))*featureWeights.get(2);
				pieceScores.add(score);
			}
			else {
				pieceScores.add((1.0d/(double)distanceFromCorner(piece))*featureWeights.get(3));
			}
		}
	}
	protected int distanceFromOpponent(BoardState board,ArrayList<Piece> opponentPieces, Piece piece) {
		int oppDistTotal=0;
		for(Piece opppiece:opponentPieces) {
			oppDistTotal+=game.getManhattanDistance(piece.getRow(), piece.getCol(), opppiece.getRow(), opppiece.getCol());
		}
		return oppDistTotal;
	}
	protected int distanceFromCorner(Piece piece) {
		int nearestCorner=game.boardwidth+1;
		for(int[] escNode:game.escapeNodes) {
			int dist=game.getManhattanDistance(piece.getRow(), piece.getCol(), escNode[0], escNode[1]);
			if(dist<nearestCorner)
				nearestCorner=dist;
		}
		return nearestCorner;
	}
	protected int distanceFromKing(BoardState board, Piece piece) {
		int distFromKing;
		distFromKing=game.getManhattanDistance(piece.getRow(), piece.getCol(), board.getKingLocation()[0], board.getKingLocation()[1]);
		return distFromKing;
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
		return totalScore;
	}
}
