package net.jonathanherr.gmu.hnefatafl;

import java.util.ArrayList;

import org.neuroph.core.NeuralNetwork;
import org.neuroph.core.data.DataSet;
import org.neuroph.core.data.DataSetRow;
import org.neuroph.nnet.learning.BackPropagation;
import org.neuroph.util.TransferFunctionType;

public class ANN_Player extends MiniMaxPlayer {

	double[] lastoutput={0};
	NeuralNetwork<BackPropagation> nn;
	public boolean training;
	public ANN_Player(Hnefatafl game, ArrayList<Piece> pieces) {
		super(game, pieces);
		this.type="ANN";
		this.searchDepth=1;
		training=true;
		nn=new org.neuroph.nnet.MultiLayerPerceptron(TransferFunctionType.SIGMOID,Board.getBoardheight()*Board.getBoardwidth()+3,50,1);
	}
	/**
	 * evaluate current state using backprop neural network and fully incremented td learning. 
	 */
	public double evaluate(BoardState state){
		//input neurons - 
		//one for each cell, 
		//one for if it's white's turn, 
		//one for if its black's turn, 
		//one for difference between white and black piece counts
		
		int inputsize=Board.getBoardheight()*Board.getBoardwidth()+3;
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
		//exepected output is previous state output if not end of game, else one at end of game if game won, else 0. 
		double reward=lastoutput[0];
		if(game.getBoard().gameOver && game.getBoard().winner.equals(this.getColor()))
			reward=1.0d;
		else if(game.getBoard().gameOver)
			reward=0.0d;
		inputdata.addRow(new DataSetRow(input,new double[]{reward}));
		nn.setLearningRule(new org.neuroph.nnet.learning.BackPropagation());
		if(training)
			nn.learn(inputdata);
		else{
			nn.setInput(inputdata.getRowAt(0).getInput());
			nn.calculate();
		}
		lastoutput=nn.getOutput();
		
		
		return nn.getOutput()[0];
	}
	public void gameover(){
		if(training){
			int games=this.games.size();
			if(games%25==0 || games==1 || games==0){
				save("ANN_"+this.getColor()+"_"+games+".nn");
			}
		}
	}
	public void save(String path){
		nn.save(path);
	}
	public void readNeuralNet(String path){
		System.out.println("Loading net from " + path);
		nn=NeuralNetwork.createFromFile(path);
		System.out.println("Done loading net.");
	}
	
	

}
