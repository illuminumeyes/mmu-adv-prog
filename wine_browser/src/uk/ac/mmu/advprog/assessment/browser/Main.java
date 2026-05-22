package uk.ac.mmu.advprog.assessment.browser;

//Main.java

import javax.swing.*;

public class Main {

 public static void main(String[] args) {

     SwingUtilities.invokeLater(() -> {

         MainWindow window = new MainWindow();

         window.setVisible(true);
     });
 }
}
