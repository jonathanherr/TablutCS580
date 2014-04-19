package net.jonathanherr.gmu.hnefatafl;

import net.jonathanherr.gmu.hnefatafl.Player.Result;
/**
 * Tracks game outcomes, winder, players, gameid, moves. For stat tracking. 
 * TODO: enhance
 * @author jonathan
 *
 */
public class Outcome{
	public int moveCount=0;
	public double gameLength=0; //NS
	public Result type;
	public boolean win;
	private String side;
	private String playerName;
	private long gameid;
	
	
	public Outcome(long gameid,String playerName, String sideName, boolean win,int moves, double length, Result result){
		this.moveCount=moves;
		this.gameLength=length;
		this.type=result;
		this.win=win;
		this.side=sideName;
		this.playerName=playerName;
		this.gameid=gameid;
	}
	@Override
	public String toString(){
		String state="";
			
		state+=gameid+"\t"+playerName+"\t"+side+"\t"+moveCount+"\t"+gameLength+"\t"+String.valueOf(win)+"\t"+type+"\n";
		return state;
	}
}