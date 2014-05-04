package net.jonathanherr.gmu.hnefatafl;

import java.util.ArrayList;

import org.neuroph.core.NeuralNetwork;
import org.neuroph.core.data.DataSet;
import org.neuroph.core.data.DataSetRow;
import org.neuroph.nnet.learning.BackPropagation;
import org.neuroph.util.TransferFunctionType;

public class ANN_Player extends MiniMaxPlayer {

	double[] lastoutput={0};
	public ANN_Player(Hnefatafl game, ArrayList<Piece> pieces) {
		super(game, pieces);
		this.type="ANN";
		this.searchDepth=1;
	}
	public double evaluate(BoardState state){
		//input neurons - 
		//one for each cell, 
		//one for if it's white's turn, 
		//one for if its black's turn, 
		//one for difference between white and black piece counts
		
		int inputsize=game.getBoard().getBoardheight()*game.getBoard().getBoardwidth()+3;
		DataSet inputdata=new DataSet(inputsize);
		double[] input=new double[inputsize];
	
		int index=0;
		for(int[] row:state.board){
			for(int val:row){
				if(val==(int)game.getBoard().blank)
					input[index++]=0.0d;
				else if(val==(int)game.getBoard().black)
					input[index++]=1.0d;
				else if(val==(int)game.getBoard().white)
					input[index++]=-1.0d;
				else if(val==(int)game.getBoard().king)
					input[index++]=-2.0d;
			}
		}
		
		if(state.getMove().getPiece().getName().equals("black")){
			input[inputsize-3]=1;
			input[inputsize-2]=0;
			input[inputsize-1]=game.blackCaptures;
		}
		else{
			input[inputsize-3]=0;
			input[inputsize-2]=1;
			input[inputsize-1]=game.whiteCaptures;
		}
		int reward=0;
		if(game.getBoard().gameOver && game.getBoard().winner.equals(this.getColor()))
			reward=1;
		inputdata.addRow(new DataSetRow(input,new double[]{lastoutput[reward]}));
		NeuralNetwork<BackPropagation> nn=new org.neuroph.nnet.MultiLayerPerceptron(TransferFunctionType.SIGMOID,Board.getBoardheight()*game.getBoard().getBoardwidth()+3,50,1);
		nn.setLearningRule(new org.neuroph.nnet.learning.BackPropagation());
		nn.learn(inputdata);
		double[] prevoutput=lastoutput;
		lastoutput=nn.getOutput();
		
		return nn.getOutput()[0];
	}

}
