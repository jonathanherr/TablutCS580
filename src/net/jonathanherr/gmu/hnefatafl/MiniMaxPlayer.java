package net.jonathanherr.gmu.hnefatafl;

import java.util.ArrayList;

import net.jonathanherr.gmu.hnefatafl.Hnefatafl.Direction;

public class MiniMaxPlayer extends Player {
	MiniMaxTree tree;
	int searchDepth=3;
	int minMoveSize=1; //mechanism to restrict game to large movements, for testing. Set to 0 for normal movement.
	int currentDepth=0;
	public MiniMaxPlayer(Hnefatafl game, ArrayList<Piece> pieces) {
		super(game, pieces, "Minimax");
		tree=new MiniMaxTree();
	}
	public Move turn() {
		BoardState currentState=new BoardState(game.board, game.getBlackpieces(), game.getWhitepieces());
		Node node=new Node(currentState);
		node.setColor(this.pieces.get(0).name);
		tree.setRoot(node);
		
		generateStates(currentState,currentState.getWhitepieces(),tree.root);
		printTree(node);
		tree.choose(this,this.getPieceColor());
		return null;
	}
	private void printTree(Node node) {
		
		//System.out.println(game.getStateString("",node.board));
		ArrayList<String> outlines=new ArrayList<String>();
		for(int i=0;i<=game.boardheight+1;i++) {
			outlines.add("");
		}
		
		int rownum=0;
		for(String line:game.getStateString("",node.board).split("\n")) {
			outlines.set(rownum,outlines.get(rownum)+"\t" + line);
			rownum+=1;
		}
		if(node.getState().getMove()!=null)
			outlines.set(rownum,"\t    "+node.getState().getMove().getPiece().getRow()+","+node.getState().getMove().getPiece().getCol() + "->" + node.getState().getMove().getDirection().toString() + " " + node.getState().getMove().getLength());
		for(int i=0;i<=game.boardheight+1;i++) {
			if(i==5)
				outlines.set(i,outlines.get(i)+"---------->");
			else
				outlines.set(i,outlines.get(i)+"           ");
				
		}
		int totalLevelStates=node.links.size();
		if(totalLevelStates>0) {
			System.out.println("current state - level " + currentDepth + " " + node.getColor());
			
			for(Link link:node.links) {
				rownum=0;
				for(String line:game.getStateString("",link.child.board).split("\n")) {
					outlines.set(rownum,outlines.get(rownum)+"\t" + line);
					rownum+=1;
				}
				if(node.getState().getMove()!=null) {
					Move childMove = link.child.getState().getMove();
					outlines.set(rownum,outlines.get(rownum)+"     \t      "+childMove.getPiece().getRow()+","+childMove.getPiece().getCol() + "->" + childMove.getDirection().toString() + " " + childMove.getLength());
				}
			
			}
			System.out.println(totalLevelStates + " states this level");
			for(String line:outlines)
				System.out.println(line);
			for(Link link:node.links) {
				printTree(link.child);
			}
			currentDepth+=1;
		}		
	}
	
	private void generateStates(BoardState state, ArrayList<Piece> pieces, Node parent) {
		BoardState movestate=null;
		int totalMoves=0;
		
		for(Piece piece:pieces) {
			
			for(Direction moveDir:Direction.values()) {
				int availMoves=piece.availLength(moveDir,game,state.board);
				
				//System.out.println(availMoves + " for " + piece.name + " piece at " + piece.getRow()+","+piece.getCol() + " in direction " + moveDir.toString());
				totalMoves+=availMoves;
				while (availMoves>=minMoveSize) {
					//generate a state for each step in this direction
					Move move=new Move(piece,moveDir,availMoves);
					
					movestate=game.simMove(this, move,state);
					movestate.setMove(move);
					//Double stateScore=game.scoreBoard(this, movestate);
					Node node=new Node(movestate);
					node.setColor(piece.name);
					
					parent.addChild(node);
					availMoves-=1;
				}
			}
		}
		if(searchDepth>0) {
			searchDepth-=1;
			for(Link link:parent.getChildren()) {
				BoardState childState=link.getChild().getState();
				if(link.child.getColor().equals(game.WHITE_NAME))
					generateStates(childState,childState.getBlackpieces(),link.getChild());
				else
					generateStates(childState,childState.getWhitepieces(),link.getChild());
					
			}
		}
		
		
		
	}
	public double evaluate(BoardState board) {
		return super.evaluate(board);
	}
	

}
