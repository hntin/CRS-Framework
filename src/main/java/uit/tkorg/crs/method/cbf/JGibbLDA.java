/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uit.tkorg.crs.method.cbf;

import java.io.File;
import java.util.HashMap;
import jgibblda.Estimator;
import jgibblda.LDACmdOption;
import uit.tkorg.utility.common.TextFileUtility;

/**
 *
 * @author tin
 */
public class JGibbLDA {

    private static StringBuffer buffAuthorIDAndDocMapping = new StringBuffer();
    private static StringBuffer buffInputJGibbLDA = new StringBuffer();

    public void process() {
        LDACmdOption ldaCmdOption = new LDACmdOption();
        ldaCmdOption.setEst(true);
        ldaCmdOption.setEstc(false);
        ldaCmdOption.setInf(false);
        ldaCmdOption.setDir("C:\\CRS-Experiment\\Sampledata\\LDATest\\JGibbLDA\\Output\\NotStemming");
        ldaCmdOption.setDfile("JGibbLDASample.dat");
        ldaCmdOption.setAlpha(0.01);
        ldaCmdOption.setBeta(0.01);
        ldaCmdOption.setK(100);
        ldaCmdOption.setNiters(1000);
        ldaCmdOption.setSavestep(100);
        ldaCmdOption.setTwords(10);

        Estimator estimator = new Estimator();
        estimator.init(ldaCmdOption);
        estimator.estimate();
    }

    public void formatInputForLDA(String rootPath) {
        File mainFolder = new File(rootPath);
        File[] subFolderList = mainFolder.listFiles();
        int instanceID = 0;
        int numberOfDoc = 0;
        
        buffAuthorIDAndDocMapping.append("AuthorID" + "\t" + "InstanceID" + "\n");
        for (int i = 0; i < subFolderList.length; i++) {
            if (subFolderList[i].isDirectory()) {
                File[] fList = subFolderList[i].listFiles();
                for (int j = 0; j < fList.length; j++) {
                    if (fList[j].isFile()) {
                        // Doc va bo vao buffer
                        String fileName = fList[j].getName();
                        buffInputJGibbLDA.append(TextFileUtility.readTextFile(fList[j].getAbsolutePath()));
                        buffAuthorIDAndDocMapping.append(
                                fileName.substring(fileName.lastIndexOf("_") + 1, fileName.lastIndexOf("."))
                                + "\t" + instanceID + "\n");
                        instanceID++;

                        buffInputJGibbLDA.append(TextFileUtility.readTextFile(fList[j].getAbsolutePath()));
                        numberOfDoc++;
                    }
                    buffInputJGibbLDA.append("\n");
                }
            }
        }

        buffInputJGibbLDA.insert(0, numberOfDoc + "\n");
        TextFileUtility.writeTextFile(rootPath + "\\CRS-InputJGibbLDA.dat", buffInputJGibbLDA.toString());
        TextFileUtility.writeTextFile(rootPath + "\\CRS-AuthorIDAndInstance.txt", buffAuthorIDAndDocMapping.toString());
    }

    public static void main(String args[]) {
        (new JGibbLDA()).process();

        System.out.println("DONE");
    }
}