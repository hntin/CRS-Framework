/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uit.tkorg.crs.utility;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;

/**
 *
 * @author Administrator
 */
public class Testor {

    public static void main(String args[]) {
        try {
            FileInputStream fis = new FileInputStream("D:\\1.CRS-Experiment\\MLData\\TrainingData\\PositiveSampleCoAuthorRSS_.txt");
            Reader reader = new InputStreamReader(fis, "UTF8");
            BufferedReader bufferReader = new BufferedReader(reader);
            // Skip the first line (the header)
            bufferReader.readLine();
            String line = null;
            String[] tokens;
            int authorId;
            int paperId;
            int orgId;
            int count = 0;
            while ((line = bufferReader.readLine()) != null && !line.equals("")) {
                tokens = line.split("\t");
                if (tokens.length == 2) {
                    float value = Float.parseFloat(tokens[1]) ;
                    if (value != 0)
                        count++;
                }
            }
            System.out.println("So dong 0:" + count);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
