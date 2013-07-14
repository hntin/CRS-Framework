/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uit.tkorg.utility;

/**
 *
 * @author TinHuynh
 */
public class SplitTextFile_MainClass {
    public static void main(String ards[]) {
        try {
            TextFileProcessor.splitTextFile("C:\\CRS-Experiment\\MAS\\Content\\LDA\\Stemming\\CRS-InputParallelLDA.txt");
            System.out.printf("DONE");
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
