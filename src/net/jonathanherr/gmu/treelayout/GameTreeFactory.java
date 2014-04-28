package net.jonathanherr.gmu.treelayout;

import org.abego.treelayout.TreeForTreeLayout;
import org.abego.treelayout.util.DefaultTreeForTreeLayout;

import net.jonathanherr.gmu.hnefatafl.*;
import net.jonathanherr.gmu.minimax.MiniMaxTree;
import net.jonathanherr.gmu.minimax.TreeLink;
import net.jonathanherr.gmu.minimax.TreeNode;

public class GameTreeFactory {
	static int width=165;
	static int height=185;

	public static TreeForTreeLayout<TextInBox> createGameTree(MiniMaxTree gametree){
		
		TextInBox root = new TextInBox(Board.getStateString("  ",gametree.root.getBoard())+"\n"+String.format("%.3f",gametree.root.getScore()) + " max?" + String.valueOf(gametree.root.isMax()), width, height);
		DefaultTreeForTreeLayout<TextInBox> tree = new DefaultTreeForTreeLayout<TextInBox>(root);
		addChildren(gametree.root, root, tree);
		return tree;
	}

	private static void addChildren(TreeNode node, TextInBox nodeBox,
			DefaultTreeForTreeLayout<TextInBox> tree) {
		for(TreeLink link:node.getChildren()){
			TextInBox box=new TextInBox(Board.getStateString("  ",link.getChild().getBoard())+"\n"+String.format("%.6f",link.getChild().getScore())+"\n"+link.getChild().getColor() + " max?" + String.valueOf(link.getChild().isMax())+"\n"+link.getChild().getState().getMove().getPiece().getRow()+","+link.getChild().getState().getMove().getPiece().getCol() + "->" + link.getChild().getState().getMove().getDirection().toString() + " " + link.getChild().getState().getMove().getLength()+"\nchildren:"+link.getChild().getChildren().size() , width, height);
			tree.addChild(nodeBox, box);
			addChildren(link.getChild(),box,tree);
		}
	}
}
