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
 * Tinh CoAuthorStrength (RSS+) cho tung cap tac gia trong mau am (-) va mau duong (+)
 * @author thucnt
 */
public class CoAuthorStrengthComputation {
    
    /**
     * read positive/negative samples from file
     * @param dataFile
     */
    public static ArrayList<Pair<Integer,Integer>> readSample(String dataFile){
        final String REGEX = "\\D";
        Pattern p = Pattern.compile(REGEX);
        ArrayList<Pair<Integer,Integer>> listOfPairs = new ArrayList<Pair<Integer,Integer>>();
        
        try {
            FileInputStream fis = new FileInputStream(dataFile);
            Reader reader = new InputStreamReader(fis, "UTF8");
            BufferedReader bufferReader = new BufferedReader(reader);
            bufferReader.readLine(); // skip the first line
            String line = null;
            
            while ((line = bufferReader.readLine()) != null) {
                String[] elements = p.split(line.trim());

                if (elements.length > 3 || elements.length < 2) {
                    continue;
                }
                int author1 = Integer.parseInt(elements[1]);
                int author2 = Integer.parseInt(elements[2]);
                Pair pair = new Pair(new Integer(author1),new Integer(author2));
                listOfPairs.add(pair);
            }
            bufferReader.close();
            fis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return listOfPairs;
    }
    
    public static void main(String args[]) {
        // Step 1: Xay dung mang dong tac gia CoAuthor_Net
        // Step 2: Tinh toan trong so RSS cho tung cap tac gia trong mang
        // Step 3: Doc file mau am (-), mau duong (+) de load tung cap <authorID, authorID>
        // Step 4: Tra ve gia tri RSS+ cho tung cap (+) va (-)
        // Step 5: Ghi ket qua gia tri CoAuthor_RSS xuong file 
        
    }
    
}
