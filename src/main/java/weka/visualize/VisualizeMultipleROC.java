/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package weka.visualize;

import java.awt.*;
import java.io.*;
import javax.swing.*;
import weka.core.*;
import weka.gui.visualize.*;

/**
 * Visualizes previously saved ROC curves.
 *
 * @author FracPete
 */
public class VisualizeMultipleROC {
  
  /**
   * takes arbitraty number of arguments: 
   * previously saved ROC curve data (ARFF file)
   */
  public static void main(String[] args) throws Exception {
    String[] files = {"/Users/thucnt/Desktop/ROC/j48.arff",
                        "/Users/thucnt/Desktop/ROC/RandomForest.arff",
                        "/Users/thucnt/Desktop/ROC/libSVM.arff",
                        };
    boolean first = true;
    ThresholdVisualizePanel vmc = new ThresholdVisualizePanel();
    for (int i = 0; i < files.length; i++) {
      Instances result = new Instances(
                            new BufferedReader(
                              new FileReader(files[i])));
      result.setClassIndex(result.numAttributes() - 1);
      // method visualize
      PlotData2D tempd = new PlotData2D(result);
      tempd.setPlotName(result.relationName());
//      tempd.addInstanceNumberAttribute();
    
      // specify which points are connected
      int numInstances = result.numInstances();
      boolean[] cp = new boolean[result.numInstances()];
      for (int n = 1; n < cp.length; n++){
          Instance instance = result.instance(n);
          instance.
          if ((numInstances > 1000) && (result.))
              
          cp[n] = true;
      }
      tempd.setConnectPoints(cp);
      // add plot
      if (first)
        vmc.setMasterPlot(tempd);
      else
        vmc.addPlot(tempd);
      first = false;
    }
    // method visualizeClassifierErrors
    final javax.swing.JFrame jf = 
      new javax.swing.JFrame("Weka Classifier ROC");
    jf.setSize(500,400);
    jf.getContentPane().setLayout(new BorderLayout());

    jf.getContentPane().add(vmc, BorderLayout.CENTER);
    jf.addWindowListener(new java.awt.event.WindowAdapter() {
      public void windowClosing(java.awt.event.WindowEvent e) {
        jf.dispose();
      }
    });

    jf.setVisible(true);
  }
}
