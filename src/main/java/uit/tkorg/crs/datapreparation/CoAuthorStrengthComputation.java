/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uit.tkorg.crs.datapreparation;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.regex.Pattern;
import org.apache.mahout.common.Pair;

/**
 * Tinh CoAuthorStrength (RSS+) cho tung cap tac gia trong mau am (-) va mau
 * duong (+)
 *
 * @author thucnt
 */
public class CoAuthorStrengthComputation extends FeatureComputation {

    @Override
    public void computeFeatureValues(String outputFile) {
        // Step 1: Xay dung mang dong tac gia CoAuthor_Net
        // Step 2: Tinh toan trong so RSS cho tung cap tac gia trong mang
        // Step 3: Doc file mau am (-), mau duong (+) de load tung cap <authorID, authorID>
        // Step 4: Tra ve gia tri RSS+ cho tung cap (+) va (-)
        // Step 5: Ghi ket qua gia tri CoAuthor_RSS xuong file 
        
        
    }

    public static void main(String args[]) {

    }

}
