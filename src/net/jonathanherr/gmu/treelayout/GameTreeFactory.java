package net.jonathanherr.gmu.treelayout;

import org.abego.treelayout.TreeForTreeLayout;
import org.abego.treelayout.util.DefaultTreeForTreeLayout;

import net.jonathanherr.gmu.hnefatafl.*;
import net.jonathanherr.gmu.minimax.MiniMaxTree;
import net.jonathanherr.gmu.minimax.TreeLink;
import net.jonathanherr.gmu.minimax.TreeNode;

public class GameTreeFactory {
	static int width=175;
	static int height=175;

	public static TreeForTreeLayout<TextInBox> createGameTree(MiniMaxTree gametree){
		
		TextInBox root = new TextInBox(Hnefatafl.getStateString("  ",gametree.root.getBoard())+"\n"+String.format("%.3f",gametree.root.getScore()), width, height);
		DefaultTreeForTreeLayout<TextInBox> tree = new DefaultTreeForTreeLayout<TextInBox>(root);
		addChildren(gametree.root, root, tree);
		return tree;
	}

	private static void addChildren(TreeNode node, TextInBox nodeBox,
			DefaultTreeForTreeLayout<TextInBox> tree) {
		for(TreeLink link:node.getChildren()){
			TextInBox box=new TextInBox(Hnefatafl.getStateString("  ",link.getChild().getBoard())+"\n"+String.format("%.3f",link.getChild().getScore()), width, height);
			tree.addChild(nodeBox, box);
			addChildren(link.getChild(),box,tree);
		}
	}
}
