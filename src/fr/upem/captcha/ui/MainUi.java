package fr.upem.captcha.ui;

import fr.upem.captcha.Logic;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;

public class MainUi {

  private static List<URL> allImages = new ArrayList<URL>();
  private static List<URL> selectedImages = new ArrayList<URL>();
  private static JFrame frame;

  public static void main(String[] args) throws IOException {
    init();
  }

  /**
   * initiate the application
   */
  private static void init() throws IOException {
    Logic.init();
    resetDisplay();
  }

  /**
   * reset the application display (without reseting the logic)
   */
  private static void resetDisplay() {
    if (frame != null) {
      frame.dispose();
    }
    selectedImages.clear();
    frame = new JFrame("Capcha"); // Cr�ation de la fen�tre principale
    GridLayout layout = createLayout();  // Cr�ation d'un layout de type Grille avec 4 lignes et 3 colonnes
    JButton okButton = createOkButton();

    frame.setLayout(layout);  // affection du layout dans la fen�tre.
    frame.setSize(1024, 768); // d�finition de la taille
    frame.setResizable(false);  // On d�finit la fen�tre comme non redimentionnable
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Lorsque l'on ferme la fen�tre on quitte le programme. 

    allImages = Logic.getImages();

    for (URL url : allImages) {
//    	System.out.println(url.getFile());
      try {
        frame.add(createLabelImage(url));
      } catch (IOException e) {
        System.err.println("err : tried to load an invalid image");
        e.printStackTrace();
      }
    }

    frame.add(new JTextArea(Logic.getMessage()));
    frame.add(okButton);
    frame.setVisible(true);
  }

  private static GridLayout createLayout() {
    return new GridLayout(4, 3);
  }

  private static JButton createOkButton() {
    return new JButton(new AbstractAction("V�rifier") { //ajouter l'action du bouton

      @Override
      public void actionPerformed(ActionEvent arg0) {
        EventQueue.invokeLater(new Runnable() { // faire des choses dans l'interface donc appeler cela dans la queue des �v�nements

          @Override
          public void run() { // c'est un runnable
            validateSelection();
          }
        });
      }
    });
  }

  private static JLabel createLabelImage(URL url) throws IOException {
    BufferedImage img = ImageIO.read(url); //lire l'image
    Image sImage = img.getScaledInstance(1024 / 3, 768 / 4, Image.SCALE_SMOOTH); //redimentionner l'image

    final JLabel label = new JLabel(new ImageIcon(sImage)); // cr�er le composant pour ajouter l'image dans la fen�tre

    label.addMouseListener(new MouseListener() { //Ajouter le listener d'�venement de souris
      private boolean isSelected = false;

      @Override
      public void mouseReleased(MouseEvent arg0) {
      }

      @Override
      public void mousePressed(MouseEvent arg0) {

      }

      @Override
      public void mouseExited(MouseEvent arg0) {

      }

      @Override
      public void mouseEntered(MouseEvent arg0) {

      }

      @Override
      public void mouseClicked(MouseEvent arg0) { //ce qui nous int�resse c'est lorsqu'on clique sur une image, il y a donc des choses � faire ici
        EventQueue.invokeLater(new Runnable() {

          @Override
          public void run() {
            if (!isSelected) {
              label.setBorder(BorderFactory.createLineBorder(Color.RED, 3));
              isSelected = true;
              selectedImages.add(url);
            } else {
              label.setBorder(BorderFactory.createEmptyBorder());
              isSelected = false;
              selectedImages.remove(url);
            }

          }
        });

      }
    });

    return label;
  }

  public static void validateSelection() {
    if (Logic.checkImages(selectedImages)) {
      JOptionPane.showMessageDialog(null, "c'est valid� !");
      Logic.resetDifficulty();
    } else {
      JOptionPane.showMessageDialog(null, "c'est rat�... le prochain sera plus difficile !");
      try {
        Logic.increaseDifficulty();
      } catch (ClassNotFoundException e) {
        JOptionPane.showMessageDialog(null, "profondeur maximale atteinte : retour � la racine de l'arbre");
        Logic.resetDifficulty();
      }
    }
    resetDisplay();
  }
}
