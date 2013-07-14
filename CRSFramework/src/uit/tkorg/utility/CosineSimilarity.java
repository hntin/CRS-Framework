/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uit.tkorg.utility;

import Jama.Matrix;

/**
 *
 * @author TinHuynh
 */
public class CosineSimilarity extends AbstractSimilarity {

    public double computeSimilarity(double[] v1, double[] v2) {
        Matrix sourceDoc = new Matrix(v1, v1.length);
        Matrix targetDoc = new Matrix(v2, v2.length);
        double dotProduct = sourceDoc.arrayTimes(targetDoc).norm1();
        double eucledianDist = sourceDoc.normF() * targetDoc.normF();
        return dotProduct / eucledianDist;
    }
    
    public double computeSimilarity(Matrix sourceDoc, Matrix targetDoc) {
        double dotProduct = sourceDoc.arrayTimes(targetDoc).norm1();
        double eucledianDist = sourceDoc.normF() * targetDoc.normF();
        return dotProduct / eucledianDist;
    }
}
