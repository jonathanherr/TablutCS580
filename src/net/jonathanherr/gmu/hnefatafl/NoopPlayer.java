package net.jonathanherr.gmu.hnefatafl;

import java.util.ArrayList;

public class NoopPlayer extends Player {

	public NoopPlayer(Hnefatafl game, ArrayList<Piece> pieces, String name) {
		super(game, pieces, name);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public Move turn() {
		return null;
	}

}
