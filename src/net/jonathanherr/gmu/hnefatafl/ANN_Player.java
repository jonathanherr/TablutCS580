package net.jonathanherr.gmu.hnefatafl;

import java.util.ArrayList;

import org.neuroph.core.NeuralNetwork;
import org.neuroph.core.data.DataSet;
import org.neuroph.util.TransferFunctionType;

public class ANN_Player extends MiniMaxPlayer {

	double[] lastoutput;
	public ANN_Player(Hnefatafl game, ArrayList<Piece> pieces) {
		super(game, pieces);
		// TODO Auto-generated constructor stub
	}
	public double evaluate(BoardState state){
		//input neurons - 
		//one for each cell, 
		//one for if it's white's turn, 
		//one for if its black's turn, 
		//one for difference between white and black piece counts
		
		int inputsize=game.getBoard().getBoardheight()*game.getBoard().getBoardwidth()+3;
		DataSet inputdata=new DataSet(inputsize,1);
		double[] input=new double[inputsize];
		int rownum=0,colnum=0;
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
				colnum+=1;
			}
			rownum+=1;
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
		
		inputdata.addRow(input);
		
		NeuralNetwork nn=new org.neuroph.nnet.MultiLayerPerceptron(TransferFunctionType.SIGMOID,game.getBoard().getBoardheight()*game.getBoard().getBoardwidth()+3,50,1);
		nn.setLearningRule(new org.neuroph.nnet.learning.BackPropagation());
		nn.learn(inputdata);
		double[] prevoutput=lastoutput;
		lastoutput=nn.getOutput();
		
		return 0.0d;
	}

}
