package net.jonathanherr.gmu.hnefatafl;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * Draws board on update for easy debugging of game board situations.
 * @author jonathan
 *
 */
class GameBoardPanel extends JPanel{
	int[][] board;
	public void setBoard(int[][] board) {
		this.board = board;
	}
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public GameBoardPanel(int[][] board){
		this.board=board;
	}
	@Override
	protected void paintComponent(Graphics g) {
		// TODO Auto-generated method stub
		super.paintComponent(g);
		
		drawBoard(g);
	}
	
	@Override
	public void update(Graphics g) {
		// TODO Auto-generated method stub
		super.update(g);
		drawBoard(g);
	}
	private void drawBoard(Graphics g) {
		g.setColor(Color.black);
		for(int i=0;i<9;i++){
			int x=i*50;
			for(int j=0;j<9;j++){
				int y=j*50;
				g.drawRect(x, y, 50, 50);
				g.drawString(String.valueOf((char)board[i][j]).replace("_", ""),x+24,y+26);
			}
		}
	}
	
}
/**
 * Simple JFrame for containing GameBoardPanel
 * @author jonathan
 *
 */
public class GUI extends JFrame {
	GameBoardPanel gamePanel;
	private GUI(int[][] boardstate){
		 super ("Hnefatafl");     // title of the frame you want to show
         // title will be from graphic that will be shown
		 setSize(1024, 768); // (Width, Height)
		 setResizable(true); //user can change the size of the frame
		 setDefaultCloseOperation(EXIT_ON_CLOSE);
		 setBackground(Color.black);
		 gamePanel=new GameBoardPanel(boardstate);
		 add(gamePanel);
	}
	
	

	/**
	 * 
	 */
	private static final long serialVersionUID = 2133684957175802599L;
	
	public static void main (String[] args) throws InterruptedException {
		Hnefatafl game=new Hnefatafl();
		GUI gui=new GUI(game.getBoard().board);
		gui.setVisible(true);
		game.setGUI(gui);
		System.out.println(game.getBoard().toStateString());
		RandomPlayer black=new RandomPlayer(game, game.getBoard().getBlackpieces());
		RandomPlayer white = new RandomPlayer(game,game.getBoard().getWhitepieces());
		game.play(white, black, 2000, 5000,200);
	    
	    // Show the frame
	}
	public void updateBoard(int[][] board) {
		gamePanel.setBoard(board);
		this.repaint();
		
	}

}
