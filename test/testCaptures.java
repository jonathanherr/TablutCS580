import net.jonathanherr.gmu.hnefatafl.Black;
import net.jonathanherr.gmu.hnefatafl.Board.Direction;
import net.jonathanherr.gmu.hnefatafl.Hnefatafl;
import net.jonathanherr.gmu.hnefatafl.Move;
import net.jonathanherr.gmu.hnefatafl.Piece;
import net.jonathanherr.gmu.hnefatafl.RandomPlayer;
import net.jonathanherr.gmu.hnefatafl.White;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


public class testCaptures {
	Hnefatafl game;
	String board;
	RandomPlayer black;
	RandomPlayer white;
	@Before
	public void setup(){
		System.out.println("setup");
		game=new Hnefatafl();
		black=new RandomPlayer(game, game.getBoard().getBlackpieces());
		white = new RandomPlayer(game,game.getBoard().getWhitepieces());
		game.setBlackPlayer(black);
		game.setWhitePlayer(white);
	}
	
	@Test
	public void testWhiteCaptureWithCorner() throws InterruptedException {
		 board=
				  "x _ _ _ _ _ _ _ x \n"
				+ "_ _ _ _ _ _ _ _ b \n"
				+ "_ _ _ _ _ _ _ _ _ \n"
				+ "_ _ _ _ _ _ _ _ w \n"
				+ "_ _ _ _ _ _ _ _ _ \n"
				+ "_ _ _ _ _ _ _ _ _ \n"
				+ "_ _ _ _ _ _ _ _ _ \n"
				+ "_ _ _ _ _ _ _ _ _ \n"
				+ "x _ _ _ _ _ _ _ x \n";
		 	
		Move move=new Move(new White(3,8), Direction.UP, 1);
		testMove(move);
		System.out.println("white captures:"+white.getCaptures());
		System.out.println("black captures:"+black.getCaptures());
		Assert.assertTrue("White should have 1 capture!", white.getCaptures()==1);
		
	}
	@Test
	public void testWhiteCapture() throws InterruptedException {
		 board=
				  "x _ _ _ _ _ _ _ x \n"
				+ "_ _ _ _ _ _ _ _ _ \n"
				+ "_ _ _ _ _ _ _ _ _ \n"
				+ "_ _ _ _ _ _ _ _ _ \n"
				+ "_ _ _ _ _ _ _ _ _ \n"
				+ "_ _ _ _ w _ b w _ \n"
				+ "_ _ _ _ _ _ _ _ _ \n"
				+ "_ _ _ _ _ _ _ _ _ \n"
				+ "x _ _ _ _ _ _ _ x \n";
		 	
		Move move=new Move(new White(5,4), Direction.RIGHT, 1);
		testMove(move);
		System.out.println("white captures:"+white.getCaptures());
		System.out.println("black captures:"+black.getCaptures());
		Assert.assertTrue("White should have 1 capture!", white.getCaptures()==1);
		
	}
	@Test
	public void testMultiCaptureRight() throws InterruptedException{
		 board=
				  "x _ _ _ _ _ _ _ x \n"
				+ "_ _ _ _ _ _ _ _ _ \n"
				+ "_ _ _ _ _ _ _ _ _ \n"
				+ "_ _ _ _ _ _ _ _ _ \n"
				+ "_ _ _ _ _ _ _ _ _ \n"
				+ "_ _ _ _ b _ w w b \n"
				+ "_ _ _ _ _ _ _ _ _ \n"
				+ "_ _ _ _ _ _ _ _ _ \n"
				+ "x _ _ _ _ _ _ _ x \n";
		 Move move=new Move(new Black(5,4), Direction.RIGHT, 1);
		 testMove(move);
		 System.out.println("white captures:"+white.getCaptures());
		 System.out.println("black captures:"+black.getCaptures());
		 Assert.assertTrue("black should have 2 captures!", black.getCaptures()==2);

	}
	@Test
	public void testMultiCaptureLeft() throws InterruptedException{
		 board=
				  "x _ _ _ _ _ _ _ x \n"
				+ "_ _ _ _ _ _ _ _ _ \n"
				+ "_ _ _ _ _ _ _ _ _ \n"
				+ "_ _ _ _ _ _ _ _ _ \n"
				+ "_ _ _ _ _ _ _ _ _ \n"
				+ "_ _ _ _ b w w _ b \n"
				+ "_ _ _ _ _ _ _ _ _ \n"
				+ "_ _ _ _ _ _ _ _ _ \n"
				+ "x _ _ _ _ _ _ _ x \n";
		 Move move=new Move(new Black(5,8), Direction.LEFT, 1);
		 testMove(move);
		 System.out.println("white captures:"+white.getCaptures());
		 System.out.println("black captures:"+black.getCaptures());
		 Assert.assertTrue("black should have 2 captures!", black.getCaptures()==2);

	}
	@Test
	public void testMultiCaptureUp() throws InterruptedException{
		 board=
				  "x _ _ _ _ _ _ _ x \n"
				+ "_ _ _ b _ _ _ _ _ \n"
				+ "_ _ _ w _ _ _ _ _ \n"
				+ "_ _ _ w _ _ _ _ _ \n"
				+ "_ _ _ _ _ _ _ _ _ \n"
				+ "_ _ _ b _ _ _ _ _ \n"
				+ "_ _ _ _ _ _ _ _ _ \n"
				+ "_ _ _ _ _ _ _ _ _ \n"
				+ "x _ _ _ _ _ _ _ x \n";
		 Move move=new Move(new Black(5,3), Direction.UP, 1);
		 testMove(move);
		 System.out.println("white captures:"+white.getCaptures());
		 System.out.println("black captures:"+black.getCaptures());
		 Assert.assertTrue("black should have 2 captures!", black.getCaptures()==2);

	}
	@Test
	public void testMultiCaptureDown() throws InterruptedException{
		 board=
				  "x _ _ _ _ _ _ _ x \n"
				+ "_ _ b _ _ _ _ _ _ \n"
				+ "_ _ _ _ _ _ _ _ _ \n"
				+ "_ _ w _ _ _ _ _ _ \n"
				+ "_ _ w _ _ _ _ _ _ \n"
				+ "_ _ b _ _ _ _ _ _ \n"
				+ "_ _ _ _ _ _ _ _ _ \n"
				+ "_ _ _ _ _ _ _ _ _ \n"
				+ "x _ _ _ _ _ _ _ x \n";
		 Move move=new Move(new Black(1,2), Direction.DOWN, 1);
		 testMove(move);
		 System.out.println("white captures:"+white.getCaptures());
		 System.out.println("black captures:"+black.getCaptures());
		 Assert.assertTrue("black should have 2 captures!", black.getCaptures()==2);

	}
	@Test
	public void testKingEscape() throws InterruptedException {
		 board=
				  "x _ _ _ _ _ _ _ x \n"
				+ "_ _ _ _ _ _ _ _ _ \n"
				+ "_ _ _ _ _ _ _ _ _ \n"
				+ "_ _ _ _ _ _ _ _ _ \n"
				+ "_ _ _ _ _ _ _ _ _ \n"
				+ "_ _ _ _ b _ _ b _ \n"
				+ "_ _ _ _ _ _ _ _ _ \n"
				+ "_ _ _ _ _ _ _ _ _ \n"
				+ "x _ _ _ _ _ _ k x \n";
		 	game.getBoard().loadState(board);
		 	Piece piece=game.getBoard().getPieceAt(8, 7);
		    Move move=new Move(piece, Direction.RIGHT, 1);
		    game.getBoard().move(white, move);
			Assert.assertTrue("white should win game.",game.isGameOver());
		
		
	}
	@Test
	public void testKingHelpCapture() throws InterruptedException {
		 board=
				  "x _ _ _ _ _ _ _ x \n"
				+ "_ _ _ _ _ _ _ _ _ \n"
				+ "_ _ _ _ _ _ _ _ _ \n"
				+ "_ _ _ _ _ _ _ _ _ \n"
				+ "_ _ _ _ _ _ _ _ _ \n"
				+ "_ _ _ _ w _ b k _ \n"
				+ "_ _ _ _ _ _ _ _ _ \n"
				+ "_ _ _ _ _ _ _ _ _ \n"
				+ "x _ _ _ _ _ _ _ x \n";
		    Move move=new Move(new White(5,4), Direction.RIGHT, 1);
		 	testMove(move);
			System.out.println("white captures:"+white.getCaptures());
			System.out.println("black captures:"+black.getCaptures());
			Assert.assertTrue("white should have 1 capture!", white.getCaptures()==1);
			
		
		
	}
	@Test
	public void testKingCapture() throws InterruptedException {
		 board=
				  "x _ _ _ _ _ _ _ x \n"
				+ "_ _ _ _ _ _ _ _ _ \n"
				+ "_ _ _ _ _ _ _ _ _ \n"
				+ "_ _ _ _ _ _ _ _ _ \n"
				+ "_ _ _ _ _ _ k _ _ \n"
				+ "_ _ _ _ b _ _ b _ \n"
				+ "_ _ _ _ _ _ _ _ _ \n"
				+ "_ _ _ _ _ _ _ _ _ \n"
				+ "x _ _ _ _ _ _ _ x \n";
		    game.getBoard().loadState(board);
		    Move move=new Move(game.getBoard().getPieceAt(5, 4), Direction.RIGHT, 1);
		    Move kingMove=new Move(game.getBoard().getPieceAt(4,6),Direction.DOWN,1);
		    
		    System.out.println(game.getBoard().toStateString());			
			game.getBoard().move(white, kingMove);
			System.out.println(game.getBoard().toStateString());			
			game.getBoard().move(black, move);
			System.out.println(game.getBoard().toStateString());			
			
			System.out.println("white captures:"+white.getCaptures());
			System.out.println("black captures:"+black.getCaptures());
			Assert.assertTrue("black should have 1 capture!", black.getCaptures()==1);
			Assert.assertTrue("black should win game.",game.isGameOver());
		
		
	}


	private void testMove(Move move) {
		game.getBoard().loadState(board);
		System.out.println(game.getBoard().toStateString());
		
		game.getBoard().move(black, move);
	}
	@Test
	public void testBlackCapture() throws InterruptedException {
		 board=
				  "x _ _ _ _ _ _ _ x \n"
				+ "_ _ _ _ _ _ _ _ _ \n"
				+ "_ _ _ _ _ _ _ _ _ \n"
				+ "_ _ _ _ _ _ _ _ _ \n"
				+ "_ _ _ _ _ _ _ _ _ \n"
				+ "_ _ _ _ b _ w b _ \n"
				+ "_ _ _ _ _ _ _ _ _ \n"
				+ "_ _ _ _ _ _ _ _ _ \n"
				+ "x _ _ _ _ _ _ _ x \n";
		 	Move move=new Move(new Black(5,4), Direction.RIGHT, 1);
		 	testMove(move);
			
			System.out.println("white captures:"+white.getCaptures());
			System.out.println("black captures:"+black.getCaptures());
			Assert.assertTrue("black should have 1 capture!", black.getCaptures()==1);
		
		
	}

}
