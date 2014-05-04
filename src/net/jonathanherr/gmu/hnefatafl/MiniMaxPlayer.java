package net.jonathanherr.gmu.hnefatafl;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;

import net.jonathanherr.gmu.hnefatafl.Board.Direction;
import net.jonathanherr.gmu.minimax.MiniMaxTree;
import net.jonathanherr.gmu.minimax.TreeLink;
import net.jonathanherr.gmu.minimax.TreeNode;

import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
/**
 * Player generates a decision tree and applies minimax algorithm to states based on evaluation of tree value at depth defined by searchDepth property. 
 * 
 * @author herrjr
 *
 */
public class MiniMaxPlayer extends Player {
	MiniMaxTree tree;
	@Expose
	int searchDepth=3; //plies to search
	@Expose
	int minMoveSize=1; //mechanism to restrict game to large movements, for testing. Set to 1 for normal movement.
	int currentDepth=0;
	public MiniMaxPlayer(Hnefatafl game, ArrayList<Piece> pieces) {
		super(game, pieces, "Minimax");
		tree=new MiniMaxTree();
		featureWeights=new HashMap<String,Double>();
	}
	
	/**
	 * Obey player interface, provide a Move object to game controller representing the player's choice of action. 
	 */
	public Move turn(int turnNumber) {
		long start=System.nanoTime();
		BoardState currentState=new BoardState(game.getBoard().board, game.getBoard().getBlackpieces(), game.getBoard().getWhitepieces());
		TreeNode node=new TreeNode(currentState);
		node.setColor(this.pieces.get(0).getName());
		tree.setRoot(node);		
		totalMoves=0;
		treeGenerationDuration=0;
		generateSearchTree(currentState,this.pieces,tree.root,Double.NEGATIVE_INFINITY,Double.POSITIVE_INFINITY,searchDepth,true);
		double bf=countBranches(tree.root)/nodeCount;
		System.out.println("Avg. Branching Factor:"+bf);
		System.out.println("Generated states:" + totalMoves);
		int treeHeight=tree.getHeight(tree.root);
		System.out.println("Tree Height:" + treeHeight);
		//printTree(node);
		if(treeHeight==0){
			System.out.println("White cannot move, forfeiting");
			game.getBoard().win(null,game.getBoard().getBlackpieces().get(0),Result.ALLCAP);
			return null;
		}
		BoardState bestState= tree.getBestState(this,turnNumber,treeHeight);
		Move chosenMove=bestState.getMove();
		long end=System.nanoTime();
		System.out.println(this.getColor() + " turn time:"+((end-start)/1000000000.0d)+"(s)");
		this.moves.add(chosenMove);
		this.states.add(bestState);
		return chosenMove;
	}
	int nodeCount=0;
	private double countBranches(TreeNode node) {
		int branches=node.getChildren().size();
		if(branches>0)
			nodeCount+=1;
		double avgBF=0.0d;
		for(TreeLink link:node.getChildren()){
			branches+=countBranches(link.getChild());
		}
		return branches;
	}

	/**
	 * Print out the tree for debugging
	 * @param node
	 */
	public void printTree(TreeNode node) {
		
		//System.out.println(game.getStateString("",node.board));
		ArrayList<String> outlines=new ArrayList<String>();
		for(int i=0;i<=Board.boardheight+1;i++) {
			outlines.add("");
		}
		
		int rownum=0;
		game.getBoard();
		for(String line:Board.getStateString("",node.getBoard()).split("\n")) {
			outlines.set(rownum,outlines.get(rownum)+"\t" + line);
			rownum+=1;
		}
		if(node.getState().getMove()!=null)
			outlines.set(rownum,""+node.getState().getMove().getPiece().getRow()+","+node.getState().getMove().getPiece().getCol() + "->" + node.getState().getMove().getDirection().toString() + " " + node.getState().getMove().getLength() + " Score:"+String.format("%.3f",node.getScore()));
		game.getBoard();
		for(int i=0;i<=Board.boardheight+1;i++) {
			if(i==5)
				outlines.set(i,outlines.get(i)+"---------->");
			else
				outlines.set(i,outlines.get(i)+"           ");
				
		}
		int totalLevelStates=node.getLinks().size();
		if(totalLevelStates>0) {
			
			for(TreeLink link:node.getLinks()) {
				rownum=0;
				game.getBoard();
				for(String line:Board.getStateString("",link.getChild().getBoard()).split("\n")) {
					outlines.set(rownum,outlines.get(rownum)+"\t" + line);
					rownum+=1;
				}
				if(node.getState().getMove()!=null) {
					Move childMove = link.getChild().getState().getMove();
					outlines.set(rownum,outlines.get(rownum)+"  "+childMove.getPiece().getRow()+","+childMove.getPiece().getCol() + "->" + childMove.getDirection().toString() + " " + childMove.getLength() + " Score:"+String.format("%.2f",link.getChild().getScore()));
				}
			
			}
			System.out.println(totalLevelStates + " states this level");
			for(String line:outlines)
				System.out.println(line);
			for(TreeLink link:node.getLinks()) {
				printTree(link.getChild());
			}
			
		}		
	}
	
	/**
	 * Generate the next level of a decision tree by looking at the available moves for each piece on the board owned by this player
	 * @param state
	 * @param pieces
	 * @param parent
	 */
	private int totalMoves=0;
	private long treeGenerationDuration=0L;
	private double generateSearchTree(BoardState state, ArrayList<Piece> pieces, TreeNode node,double alpha, double beta, int depth,boolean maximizingplayer){
		if(depth==0){
			double score=evaluate(node.getState());
			node.setScore(score);
			return score;
		}
		if(maximizingplayer){
			//generate all children for this node = all the possible moves from here
			generateFrontier(state, pieces, node);
			for(TreeLink link:node.getChildren()){
				alpha=Math.max(alpha,generateSearchTree(state,link.getChild().getState().getBlackpieces(),link.getChild(),alpha,beta,depth-1,false));
				if(beta<=alpha){
					break;
				}
			}
			node.setScore(alpha);
			return alpha;
		}
		else{
			generateFrontier(state, pieces, node);
			for(TreeLink link:node.getChildren()){
				beta=Math.min(beta,generateSearchTree(state,link.getChild().getState().getBlackpieces(),link.getChild(),alpha,beta,depth-1,true));
				if(beta<=alpha){
					break;
				}
			}
			node.setScore(beta);
			return beta;
		}
	}
	/**
	 * Generate all children, all legal moves, from the current state
	 * @param state
	 * @param pieces
	 * @param node
	 */
	private void generateFrontier(BoardState state, ArrayList<Piece> pieces,
			TreeNode node) {
		BoardState gameState;
		int availMoves;
		int pieceIndex=0;
		for(Piece piece:pieces) {			
			if(piece!=null) {
				for(Direction moveDir:Direction.values()) {
					availMoves=piece.availLength(moveDir,game,state.board);
					totalMoves+=availMoves;
					while (availMoves>=minMoveSize) {
						gameState = generateNextState(state, availMoves,pieceIndex, piece, moveDir);	
						TreeNode newnode=new TreeNode(gameState);
						newnode.setColor(piece.getName());
						newnode.setLevel(node.getLevel()+1);
						node.addChild(newnode);
						availMoves-=1;
					}
				}
			}
			pieceIndex+=1;
		}
	}
	
	/**
	 * Generates entire search tree without pruning. costly. 
	 * @param state
	 * @param pieces
	 * @param parent
	 */
	private void generateStates(BoardState state, ArrayList<Piece> pieces, TreeNode parent) {
		BoardState gameState=null;
		int availMoves=0;
		int pieceIndex=0;
		long start=System.nanoTime();
		for(Piece piece:pieces) {			
			if(piece!=null) {
				
				for(Direction moveDir:Direction.values()) {
					availMoves=piece.availLength(moveDir,game,state.board);
					totalMoves+=availMoves;
					
					while (availMoves>=minMoveSize) {
						//generate a state for each step in this direction
						int[][] boardGrid = new int[Board.boardwidth][Board.boardheight];
						ArrayList<Piece> blackPieces=new ArrayList<Piece>();
						ArrayList<Piece> whitePieces=new ArrayList<Piece>();
						
						Board.copyBoard(state.getBoard(), boardGrid);
						//when we do the simulated move, use a copy of the piece so that we don't update it's position in the 'real' set since we don't know which node the player will choose
						Move move=null;
						Board simBoard=new Board();
						simBoard.debug=false; //don't want output from simulated moves
						simBoard.setBoard(boardGrid);
						Board.deepCopy(state.getBlackpieces(), blackPieces);
						Board.deepCopy(state.getWhitepieces(), whitePieces);
						simBoard.setBlackpieces(blackPieces);
						simBoard.setWhitepieces(whitePieces);
					
						if(piece.getName().equals(Board.BLACK)) {
							move=new Move(blackPieces.get(pieceIndex),moveDir,availMoves);
						}
						else {
							move=new Move(whitePieces.get(pieceIndex),moveDir,availMoves);							
						}
						
						simBoard.move(this, move);
						
						gameState=new BoardState(simBoard.getBoardGrid(), simBoard.getBlackpieces(), simBoard.getWhitepieces());
						gameState.setMove(new Move(piece,moveDir,availMoves)); //when we create the state's move object based on what was selected, use the piece that is the original piece so that it won't have been updated and is the object from the game board's state.
						gameState.winner=simBoard.winner;
						gameState.setGameOver(simBoard.gameOver);
						if(simBoard.debug) {
							System.out.println(move.toString());		
							System.out.println("move score:" +this.evaluate(gameState) );
						}	
						TreeNode node=new TreeNode(gameState);
						node.setColor(piece.getName());
						node.setLevel(parent.getLevel()+1);
						parent.addChild(node);
						availMoves-=1;
					}
				}
			}
			pieceIndex+=1;
		}
		long end=System.nanoTime();
		treeGenerationDuration+=end-start;
		/**
		 * Call self recursively to generate more levels
		 */
		for(TreeLink link:parent.getChildren()) {
			start=System.nanoTime();
			if(link.getChild().getLevel()<searchDepth || belowTimeThreshold(treeGenerationDuration)){
				BoardState childState=link.getChild().getState();
				if(link.getChild().getColor().equals(Board.WHITE))
					generateStates(childState,childState.getBlackpieces(),link.getChild());
				else
					generateStates(childState,childState.getWhitepieces(),link.getChild());
			}
			treeGenerationDuration+=System.nanoTime()-start;
		}
		
		
	}

	/**
	 * Simulate the given move and create a boardstate to represent the outcome of the move
	 * @param state
	 * @param availMoves
	 * @param pieceIndex
	 * @param piece
	 * @param moveDir
	 * @return
	 */
	private BoardState generateNextState(BoardState state, int availMoves,
			int pieceIndex, Piece piece, Direction moveDir) {
		BoardState gameState;
		int[][] boardGrid = new int[Board.boardwidth][Board.boardheight];
		ArrayList<Piece> blackPieces=new ArrayList<Piece>();
		ArrayList<Piece> whitePieces=new ArrayList<Piece>();
		
		Board.copyBoard(state.getBoard(), boardGrid);
		//when we do the simulated move, use a copy of the piece so that we don't update it's position in the 'real' set since we don't know which node the player will choose
		Move move=null;
		Board simBoard=new Board();
		simBoard.debug=false; //don't want output from simulated moves
		simBoard.setBoard(boardGrid);
		Board.deepCopy(state.getBlackpieces(), blackPieces);
		Board.deepCopy(state.getWhitepieces(), whitePieces);
		simBoard.setBlackpieces(blackPieces);
		simBoard.setWhitepieces(whitePieces);

		if(piece.getName().equals(Board.BLACK)) {
			move=new Move(blackPieces.get(pieceIndex),moveDir,availMoves);
		}
		else {
			move=new Move(whitePieces.get(pieceIndex),moveDir,availMoves);							
		}
		
		simBoard.move(this, move);
		
		gameState=new BoardState(simBoard.getBoardGrid(), simBoard.getBlackpieces(), simBoard.getWhitepieces());
		gameState.setMove(new Move(piece,moveDir,availMoves)); //when we create the state's move object based on what was selected, use the piece that is the original piece so that it won't have been updated and is the object from the game board's state.
		gameState.winner=simBoard.winner;
		gameState.setGameOver(simBoard.gameOver);
		if(simBoard.debug) {
			System.out.println(move.toString());		
			System.out.println("move score:" +this.evaluate(gameState) );
		}
		return gameState;
	}
		
	private boolean belowTimeThreshold(long totalTime) {
		if(totalTime>50000L)
			return false;
		else
			return true;
	}

	public static Player openPlayer(String path) throws IOException{
		Gson gson=new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
		String json = Files.toString(new File(path), Charset.forName("UTF-8"));
		return gson.fromJson(json, MiniMaxPlayer.class);		
	}
	/**
	 * For the time being, call the basic evaluation method
	 */
	public double evaluate(BoardState board) {
		return super.evaluate(board);
	}
	public double evaluateOpponent(BoardState state) {
		return super.evaluateOpponent(state);
	}

	public void addFeature(String feature, Double weight) {
		featureWeights.put(feature, weight);
	}

	public void readFeatures(String featurefile) {
		try {
			for(String feature:Files.readLines(new File(featurefile), Charset.forName("UTF8"))){
				String name=feature.split("=")[0];
				Double weight=Double.valueOf(feature.split("=")[1]);
				this.addFeature(name, weight);
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	

}
