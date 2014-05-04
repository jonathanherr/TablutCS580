package net.jonathanherr.gmu.hnefatafl;

import org.neuroph.core.NeuralNetwork;

public class ANN_Trainer {
			public void trainNN(Board board, BoardState state){
				//input neurons - 
				//one for each cell, 
				//one for if it's white's turn, 
				//one for if its black's turn, 
				//one for difference between white and black piece counts
				NeuralNetwork nn=new org.neuroph.nnet.MultiLayerPerceptron(board.getBoardheight()*board.getBoardwidth()+3,50,1);
			}
}
