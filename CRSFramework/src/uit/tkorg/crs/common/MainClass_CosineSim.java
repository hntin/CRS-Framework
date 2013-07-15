/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uit.tkorg.crs.common;

import uit.tkorg.crs.common.CosineSimilarity;
import cc.mallet.util.Maths;
import java.text.DecimalFormat;

/**
 *
 * @author TinHuynh
 */
public class MainClass_CosineSim {
    public static void main(String args[]) {
        try {
            DecimalFormat df = new DecimalFormat("0.0000000000");
            double[] v1 = new double[]{1,0.5};
            double[] v2 = new double[]{0.5,1};
            double[] v3 = new double[]{1,0.5};
            double[] v4 = new double[]{0,0};
            CosineSimilarity cosSim = new CosineSimilarity();
            System.out.println("COSINE(v1, v2):" + cosSim.computeSimilarity(v1, v2));
            System.out.println("COSINE(v1, v3):" + cosSim.computeSimilarity(v1, v3));
            System.out.println("COSINE(v1, v4):" + cosSim.computeSimilarity(v1, v4));
            
            System.out.println("KLDivergence(v1, v2):" + Maths.klDivergence(v1, v2));
            System.out.println("KLDivergence(v1, v3):" + Maths.klDivergence(v1, v3));
            System.out.println("KLDivergence(v1, v4):" + Maths.klDivergence(v1, v4));
            System.out.printf("DONE");
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
