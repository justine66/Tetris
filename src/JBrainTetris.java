import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
 

public class JBrainTetris extends JTetris {

 //instance de Defaultbrain
 private Brain brain;
 private Brain.Move move;
 private int lastcount;
 
 private JSlider slider;
 private JCheckBox brainMode;

  /**
  * Constructeur de la classe JBrainTetris
  * @param pixels
  */

 public JBrainTetris (int pixels){
 	 super(pixels);
 	 this.brain = new DefaultBrain();
 	 this.move = null;
 	 this.lastcount = -1;
 }

 

 

 /** @Overrides
  * Creates the panel of UI controls -- controls wired up to call methods on
  * the JTetris. This code is very repetitive.
  */

 public JComponent createControlPanel() {
 	 JComponent panel = super.createControlPanel();
 
 	 //BRAIN
 	 panel.add(new JLabel ("Brain:"));
 	 brainMode = new JCheckBox ("Brain active");
 	 panel.add(brainMode); 
 	 JPanel row = new JPanel ();
 	 
 	 //ADVERSAIRE
 	 row.add(new JLabel ("Adversaire:"));
 	 row.add(Box.createVerticalStrut(12));
 	 slider = new JSlider(0, 100, 100); // min, max, current
 	 slider.setPreferredSize(new Dimension(100, 15));
 	 row.add(slider);
 	 panel.add(row);
 	 return panel;
 }

 
 @Override

 public void tick (int Vb)
 {
      if(Vb==DOWN && brainMode.isSelected() && lastcount!=super.count && super.currentPiece != null)
      {
     	 super.board.undo();
          lastcount=super.count;
          Brain.Move m = brain.bestMove(board, super.currentPiece, super.board.getHeight() - TOP_SPACE);
          if(m != null)
              setCurrent(m.piece, m.x, m.y);
          else
         	 Vb = DROP;
      }
      super.tick(Vb);
 }
	 

 

 

 // Surcharger la  méthode pickNextPiece

 @Override

 public Piece pickNextPiece () {
 	 int rand = (int) (random.nextDouble()*100);
 	 if (rand > slider.getValue()) {
 	 	 // Recherche de la pire piéce (celle au score le plus élevé)
 	 	 Brain.Move res = brain.bestMove(board, pieces[0], HEIGHT);
 	 	 for (Piece p : pieces) {
 	 	 	 Brain.Move temp = brain.bestMove(board, p, HEIGHT);
 	 	 	 if (res.score < temp.score) {
 	 	 	 	 res.score = temp.score;
 	 	 	 	 res = temp;
 	 	 	 }
 	 	 }
 	 	 if (res != null)
 	 	 	 return res.piece;
 	 }
 	 return super.pickNextPiece();

 }
 
 public static void main (String[] args){
 	 try {
 	 	 UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
 	 } catch (Exception ignored) {

 	 }

  	 JBrainTetris tetris = new JBrainTetris(16);
 	 JFrame frame = JBrainTetris.createFrame(tetris);
 	 frame.setVisible(true);
 }
}

 

 

