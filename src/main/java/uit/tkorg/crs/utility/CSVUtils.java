/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uit.tkorg.crs.utility;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 *
 * @author thucnt
 */
public class CSVUtils {
    public static void main(String[] args) {

        String csvFile = "input/testing.csv";

        CSVReader reader = null;
        CSVWriter writer = null;
        try {
            reader = new CSVReader(new FileReader(csvFile));
            writer = new CSVWriter(new FileWriter("input/testingData.csv"));
            String[] line;
            while ((line = reader.readNext()) != null) {
                if (line[line.length-1].equals("Positive"))
                    line[line.length-1] = "1";
                if (line[line.length-1].equals("Negative"))
                    line[line.length-1] = "0";
                writer.writeNext(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Finished");
    }
}
