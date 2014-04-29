package net.jonathanherr.gmu.minimax;

import java.awt.Container;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JDialog;

import net.jonathanherr.gmu.treelayout.GameTreeFactory;
import net.jonathanherr.gmu.treelayout.SVGForTextInBoxTree;
import net.jonathanherr.gmu.treelayout.TextInBox;
import net.jonathanherr.gmu.treelayout.TextInBoxNodeExtentProvider;
import net.jonathanherr.gmu.treelayout.TextInBoxTreePane;

import org.abego.treelayout.TreeForTreeLayout;
import org.abego.treelayout.TreeLayout;
import org.abego.treelayout.util.DefaultConfiguration;


public class DrawTree {
	 private static void showInDialog(JComponent panel) {
         JDialog dialog = new JDialog();
         Container contentPane = dialog.getContentPane();
         ((JComponent) contentPane).setBorder(BorderFactory.createEmptyBorder(
                         10, 10, 10, 10));
         contentPane.add(panel);
         dialog.pack();
         dialog.setLocationRelativeTo(null);
         dialog.setVisible(true);
 }

 public static void write(MiniMaxTree tree,int turn){
	 // Generate the SVG and write it to System.out
	 TreeForTreeLayout<TextInBox> gameTreeLayout = GameTreeFactory.createGameTree(tree);
	 
     // setup the tree layout configuration
     double gapBetweenLevels = 50;
     double gapBetweenNodes = 10;
     DefaultConfiguration<TextInBox> configuration = new DefaultConfiguration<TextInBox>(
                     gapBetweenLevels, gapBetweenNodes);

     // create the NodeExtentProvider for TextInBox nodes
     TextInBoxNodeExtentProvider nodeExtentProvider = new TextInBoxNodeExtentProvider();

     // create the layout
     TreeLayout<TextInBox> treeLayout = new TreeLayout<TextInBox>(gameTreeLayout,
                     nodeExtentProvider, configuration);
	 SVGForTextInBoxTree generator = new SVGForTextInBoxTree(treeLayout);
    BufferedWriter bw;
	try {
		bw = new BufferedWriter(new FileWriter(new File("out_"+turn+".svg")));
		bw.write(generator.getSVG());
		bw.close();
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
 }
 public static void show(MiniMaxTree tree){
	 TreeForTreeLayout<TextInBox> treelayout = GameTreeFactory.createGameTree(tree);
     
     // setup the tree layout configuration
     double gapBetweenLevels = 50;
     double gapBetweenNodes = 10;
     DefaultConfiguration<TextInBox> configuration = new DefaultConfiguration<TextInBox>(
                     gapBetweenLevels, gapBetweenNodes);

     // create the NodeExtentProvider for TextInBox nodes
     TextInBoxNodeExtentProvider nodeExtentProvider = new TextInBoxNodeExtentProvider();

     // create the layout
     TreeLayout<TextInBox> treeLayout = new TreeLayout<TextInBox>(treelayout,
                     nodeExtentProvider, configuration);

     // Create a panel that draws the nodes and edges and show the panel
     TextInBoxTreePane panel = new TextInBoxTreePane(treeLayout);
     showInDialog(panel);

 }
}
