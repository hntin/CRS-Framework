/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uit.tkorg.crs.method.content;

import java.io.File;
import java.util.HashMap;
import jgibblda.Estimator;
import jgibblda.LDACmdOption;
import uit.tkorg.utility.TextFileUtility;

/**
 *
 * @author tin
 */
public class JGibbLDA {
    static StringBuffer buffInputFormatLDA = new StringBuffer();
    static StringBuffer buffAuthorIDIndex = new StringBuffer();

    public void process() {
        LDACmdOption ldaCmdOption = new LDACmdOption();
        ldaCmdOption.setEst(true);
        ldaCmdOption.setEstc(false);
        ldaCmdOption.setInf(false);
        ldaCmdOption.setDir("C:\\CRS-Experiment\\Test\\OutStem\\New folder");
        ldaCmdOption.setDfile("OutStem_ap1.dat");
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
        File mainFolder = new File(rootPath); // C:\CRS-Experiment\OutStem
        File[] subFolderList = mainFolder.listFiles();
        int idx = 0;
        for (int i = 0; i < subFolderList.length; i++) {
            if (subFolderList[i].isDirectory()) {
                File[] fList = subFolderList[i].listFiles();
                for (int j = 0; j < fList.length; j++) {
                    if (fList[j].isFile()) {
                        // Doc va bo vao buffer
                        buffInputFormatLDA.append(TextFileUtility.readTextFile(fList[j].getAbsolutePath()));
                        buffAuthorIDIndex.append(fList[j].getName() + "\t" + (idx++) + "\n");
                    }
                    buffInputFormatLDA.append("\n");
                }
            }
        }

        buffInputFormatLDA.insert(0, idx + "\n");

        TextFileUtility.writeTextFile(rootPath + "\\CRS-InputDataLDA.dat", buffInputFormatLDA.toString());
        TextFileUtility.writeTextFile(rootPath + "\\CRS-AuthorIDIdx.dat", buffAuthorIDIndex.toString());
    }
}