/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uit.tkorg.crs.datapreparation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import uit.tkorg.crs.model.Pair;
import uit.tkorg.crs.utility.TextFileUtility;
import uit.tkorg.crs.model.CoAuthorGraph;

/**
 *
 * @author TinHuynh
 */
public class MLDataExtraction {

    /**
     * Loc ra cac junior researchers ton tai trong G1, nhung chua ton tai trong
     * G0. Luc moi bat dau nghien cuu va lan dau xuat hien trong cong dong. Co
     * so bai bao trong trong G1 < n (n=3?)
     *
     * @param G0
     * @param G1
     * @param outFileName
     */
    private static void getListofJuniorFromCoAuthorGraph(CoAuthorGraph G0, CoAuthorGraph G1,
            String outFileName) {
        StringBuffer listOfJunior = new StringBuffer();
        listOfJunior.append("AuthorID, NumOfPub \n");
        for (int authorID : G1._coAuthorGraph.keySet()) {
            // authorID khong ton tai trong G0
            if (!G0.getAuthorPaper().containsKey(authorID)) {
                // So bai bao trong G1 < 3
                if (G1.getAuthorPaper().get(authorID).size() < 3) {
                    listOfJunior.append(authorID + "," + G1.getAuthorPaper().get(authorID).size() + "\n");
                }
            }
        }
        TextFileUtility.writeTextFile(outFileName, listOfJunior.toString());
    }

    /**
     * getListofSeniorFromCoAuthorGraph
     *
     * @param G0
     * @param G1
     * @param outFileName
     */
    private static void getListofSeniorFromCoAuthorGraph(CoAuthorGraph G1, String outFileName) {
        StringBuffer listOfSenior = new StringBuffer();
        listOfSenior.append("AuthorID, NumOfPub \n");
        for (int authorID : G1._coAuthorGraph.keySet()) {
            // So bai bao trong G1 >= 3
            if (G1.getAuthorPaper().get(authorID).size() >= 3) {
                listOfSenior.append(authorID + "," + G1.getAuthorPaper().get(authorID).size() + "\n");
            }
        }
        TextFileUtility.writeTextFile(outFileName, listOfSenior.toString());
    }

    /**
     *
     * @param inputFileName
     * @return
     */
    private static HashMap<Integer, Integer> loadAuthorIDFromTextFile(String inputFileName) {
        HashMap<Integer, Integer> listOfAuthorHM = new HashMap<>();
        try {
            FileInputStream fis = new FileInputStream(inputFileName);
            Reader reader = new InputStreamReader(fis, "UTF8");
            BufferedReader bufferReader = new BufferedReader(reader);
            bufferReader.readLine(); // skip the first line
            String line = null;
            String[] tokens;
            while ((line = bufferReader.readLine()) != null) {
                tokens = line.split(",");
                if (tokens.length > 2) {
                    continue;
                }
                int authorID = Integer.parseInt(tokens[0]);
                int numOfPub = Integer.parseInt(tokens[1]);
                if (!listOfAuthorHM.containsKey(authorID)) {
                    listOfAuthorHM.put(authorID, numOfPub);
                }
            }
            bufferReader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return listOfAuthorHM;
    }

    /**
     * loading authorID of junior researchers from the PostiveSample file
     *
     * @param positiveSampleFile
     * @return
     */
    private static HashMap<Integer, Integer> loadAuthorIDFromPositiveSample(String positiveSampleFile) {
        final String REGEX = "\\W+";
        Pattern p = Pattern.compile(REGEX);

        HashMap<Integer, Integer> authorIDInPositiveSampleHM = new HashMap<>();
        try {
            FileInputStream fis = new FileInputStream(positiveSampleFile);
            Reader reader = new InputStreamReader(fis, "UTF8");
            BufferedReader bufferReader = new BufferedReader(reader);
            bufferReader.readLine(); // skip the first line
            String line = null;
            String[] tokens;
            while ((line = bufferReader.readLine()) != null) {
                String[] elements = p.split(line.trim());

                if (elements.length > 3) {
                    continue;
                }
                int authorID = Integer.parseInt(elements[1]);
                int numOfPub = Integer.parseInt(elements[2]);
                if (!authorIDInPositiveSampleHM.containsKey(authorID)) {
                    authorIDInPositiveSampleHM.put(authorID, numOfPub);
                }
            }
            bufferReader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return authorIDInPositiveSampleHM;
    }

    /**
     * Loc ra nhung link(+) cho danh sach AuthorID: ton tai trong G2, nhung
     * khong ton tai trong G1 --> luu xuong file outFileName (2 dinh deu ton tai
     * trong G1 va G2)
     *
     * @param listOfAuthorID
     * @param G0
     * @param G1
     * @param outFileName
     */
    private static int getPositiveSampleFromCoAuthorGraph(HashMap<Integer, Integer> listOfAuthors, CoAuthorGraph G1, CoAuthorGraph G2,
            String outFileName) {
        int numOfPositiveSample = 0;
        StringBuffer strBuffer = new StringBuffer();
        strBuffer.append("(AuthorID, CoAuthorID) \n");
        for (int authorID : listOfAuthors.keySet()) {
            if (G2._coAuthorGraph.containsKey(authorID)) {
                for (int coAuthorID : G2._coAuthorGraph.get(authorID).keySet()) {
                    if (G1._coAuthorGraph.containsKey(authorID) && G1._coAuthorGraph.containsKey(coAuthorID)) {
                        if (!CoAuthorGraph.isLinkExistInCoAuthorGraph(G1._coAuthorGraph, authorID, coAuthorID)) {
                            strBuffer.append("(" + authorID + "," + coAuthorID + ")\n");
                            numOfPositiveSample++;
                        }
                    }
                }
            }
        }
        TextFileUtility.writeTextFile(outFileName, strBuffer.toString());
        return numOfPositiveSample;
    }

    /**
     *
     * @param listOfAuthors
     * @param numOfNegativeSample
     * @param G1
     * @param G2
     * @param outFileName
     */
    private static double getRandomlyNegativeSampleFromCoAuthorGraph(HashMap<Integer, Integer> listOfAuthors,
            int numOfNegativeSample,
            CoAuthorGraph G1, CoAuthorGraph G2,
            String outFileName) {

        double numOfSelectedNegativeSample = 0;
        StringBuffer strBuffer = new StringBuffer();
        strBuffer.append("(AuthorID, ID-NoneCoAuthor)\n");

        int numOfNegativeSamplePerOneAuthor = numOfNegativeSample / listOfAuthors.size();

        for (int authorID : listOfAuthors.keySet()) {
            int count = 0;
            for (int anotherAuthorID : G2._coAuthorGraph.keySet()) {
                if (count < numOfNegativeSamplePerOneAuthor) {
                    if ((authorID != anotherAuthorID) && G1._coAuthorGraph.containsKey(authorID)
                            && G1._coAuthorGraph.containsKey(anotherAuthorID)
                            && G2._coAuthorGraph.containsKey(authorID)) {
                        if (!CoAuthorGraph.isLinkExistInCoAuthorGraph(G2._coAuthorGraph, authorID, anotherAuthorID)
                                && !CoAuthorGraph.isLinkExistInCoAuthorGraph(G1._coAuthorGraph, authorID, anotherAuthorID)) {
                            strBuffer.append("(" + authorID + "," + anotherAuthorID + ")\n");
                            count++;
                            numOfSelectedNegativeSample++;
                        }
                    }
                } else {
                    break;
                }
            }

        }
        TextFileUtility.writeTextFile(outFileName, strBuffer.toString());
        return numOfSelectedNegativeSample;
    }

    /**
     * getAllNegativeSampleFromCoAuthorGraph In 2 Hubs
     *
     * @param listOfAuthors
     * @param G1
     * @param G2
     * @param outFileName
     * @return
     */
    private static int getAllNegativeSampleFromCoAuthorGraphIn2Hub(HashMap<Integer, Integer> listOfAuthors,
            CoAuthorGraph G1, CoAuthorGraph G2, String outFileName) {
        HashMap<Integer, ArrayList<Integer>> pairsOfNegativeSample = new HashMap<>();
        int count = 0;
        try {
            for (int authorID : listOfAuthors.keySet()) {
                ArrayList<Integer> negativeSetForAnAuthorID = pairsOfNegativeSample.get(authorID);
                if (G2._coAuthorGraph.containsKey(authorID) && G1._coAuthorGraph.containsKey(authorID)) {
                    Set<Integer> coAuthorSetInHub1 = G1._coAuthorGraph.get(authorID).keySet();
                    for (int authorIDInHub1 : coAuthorSetInHub1) {
                        Set<Integer> coAuthorSetInHub2 = G1._coAuthorGraph.get(authorIDInHub1).keySet();
                        for (int authorIDInHub2 : coAuthorSetInHub2) {
                            if (authorIDInHub2 != authorID) {
                                if (G2._coAuthorGraph.containsKey(authorIDInHub2) && G1._coAuthorGraph.containsKey(authorIDInHub2)
                                        && !CoAuthorGraph.isLinkExistInCoAuthorGraph(G2._coAuthorGraph, authorID, authorIDInHub2)
                                        && !CoAuthorGraph.isLinkExistInCoAuthorGraph(G1._coAuthorGraph, authorID, authorIDInHub2)) {
                                    if (negativeSetForAnAuthorID == null) {
                                        negativeSetForAnAuthorID = new ArrayList<Integer>();
                                    }

                                    if (!negativeSetForAnAuthorID.contains(authorIDInHub2)) {
                                        negativeSetForAnAuthorID.add(authorIDInHub2);
                                        count++;
                                    }
                                }
                            }
                        }
                    }
                }

                if (negativeSetForAnAuthorID != null && !negativeSetForAnAuthorID.isEmpty()) {
                    pairsOfNegativeSample.put(authorID, negativeSetForAnAuthorID);
                }
            }

            // Write the HashMap to file
            PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(outFileName)));
            out.println("AuthorID, ID-NoneCoAuthor");
            out.flush();
            for (int authorID : pairsOfNegativeSample.keySet()) {
                for (int anotherAuthorID : pairsOfNegativeSample.get(authorID)) {
                    out.println("(" + authorID + "," + anotherAuthorID + ")");
                    out.flush();
                }
            }

            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return count;
    }

    /**
     * getAllNegativeSampleFromCoAuthorGraph In 3 Hubs
     *
     * @param listOfAuthors
     * @param G1
     * @param G2
     * @param outFileName
     * @return
     */
    private static int getAllNegativeSampleFromCoAuthorGraphIn3Hub(HashMap<Integer, Integer> listOfAuthors,
            CoAuthorGraph G1, CoAuthorGraph G2, String outFileName) {
        HashMap<Integer, ArrayList<Integer>> pairsOfNegativeSample = new HashMap<>();
        int count = 0;
        try {
            for (int authorID : listOfAuthors.keySet()) {
                ArrayList<Integer> negativeSetForAnAuthorID = pairsOfNegativeSample.get(authorID);
                if (G2._coAuthorGraph.containsKey(authorID) && G1._coAuthorGraph.containsKey(authorID)) {
                    Set<Integer> coAuthorSetInHub1 = G1._coAuthorGraph.get(authorID).keySet();
                    for (int authorIDInHub1 : coAuthorSetInHub1) {
                        Set<Integer> coAuthorSetInHub2 = G1._coAuthorGraph.get(authorIDInHub1).keySet();
                        for (int authorIDInHub2 : coAuthorSetInHub2) {
                            if (authorIDInHub2 != authorID) {
                                if (G2._coAuthorGraph.containsKey(authorIDInHub2) && G1._coAuthorGraph.containsKey(authorIDInHub2)
                                        && !CoAuthorGraph.isLinkExistInCoAuthorGraph(G2._coAuthorGraph, authorID, authorIDInHub2)
                                        && !CoAuthorGraph.isLinkExistInCoAuthorGraph(G1._coAuthorGraph, authorID, authorIDInHub2)) {
                                    if (negativeSetForAnAuthorID == null) {
                                        negativeSetForAnAuthorID = new ArrayList<Integer>();
                                    }

                                    if (!negativeSetForAnAuthorID.contains(authorIDInHub2)) {
                                        negativeSetForAnAuthorID.add(authorIDInHub2);
                                        count++;
                                    }
                                }
                            }

                            Set<Integer> coAuthorSetInHub3 = G1._coAuthorGraph.get(authorIDInHub2).keySet();
                            for (int authorIDInHub3 : coAuthorSetInHub3) {
                                if (authorIDInHub3 != authorID && authorIDInHub3 != authorIDInHub1) {
                                    if (G2._coAuthorGraph.containsKey(authorIDInHub3) && G1._coAuthorGraph.containsKey(authorIDInHub3)
                                            && !CoAuthorGraph.isLinkExistInCoAuthorGraph(G2._coAuthorGraph, authorID, authorIDInHub3)
                                            && !CoAuthorGraph.isLinkExistInCoAuthorGraph(G1._coAuthorGraph, authorID, authorIDInHub3)) {
                                        if (negativeSetForAnAuthorID == null) {
                                            negativeSetForAnAuthorID = new ArrayList<Integer>();
                                        }

                                        if (!negativeSetForAnAuthorID.contains(authorIDInHub3)) {
                                            negativeSetForAnAuthorID.add(authorIDInHub3);
                                            count++;
                                        }
                                    }
                                }
                            }

                        }
                    }
                }

                if (negativeSetForAnAuthorID != null && !negativeSetForAnAuthorID.isEmpty()) {
                    pairsOfNegativeSample.put(authorID, negativeSetForAnAuthorID);
                }
            }

            // Write the HashMap to file
            PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(outFileName)));
            out.println("AuthorID, ID-NoneCoAuthor");
            out.flush();
            for (int authorID : pairsOfNegativeSample.keySet()) {
                for (int anotherAuthorID : pairsOfNegativeSample.get(authorID)) {
                    out.println("(" + authorID + "," + anotherAuthorID + ")");
                    out.flush();
                }
            }

            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return count;
    }

    /**
     *
     * @param listOfAuthors
     * @param numOfNegativeSample
     * @param G1
     * @param G2
     * @param outFileName
     */
    private static int getAllNegativeSampleFromCoAuthorGraph(HashMap<Integer, Integer> listOfAuthors,
            CoAuthorGraph G1, CoAuthorGraph G2,
            String outFileName) {

        int numOfSelectedNegativeSample = 0;
        try {
            PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(outFileName)));
            out.println("AuthorID, ID-NoneCoAuthor");
            out.flush();

            for (int authorID : listOfAuthors.keySet()) {
                for (int anotherAuthorID : G2._coAuthorGraph.keySet()) {
                    if ((authorID != anotherAuthorID) && G1._coAuthorGraph.containsKey(authorID)
                            && G1._coAuthorGraph.containsKey(anotherAuthorID)
                            && G2._coAuthorGraph.containsKey(authorID)) {
                        if (!CoAuthorGraph.isLinkExistInCoAuthorGraph(G2._coAuthorGraph, authorID, anotherAuthorID)
                                && !CoAuthorGraph.isLinkExistInCoAuthorGraph(G1._coAuthorGraph, authorID, anotherAuthorID)) {
                            out.println("(" + authorID + "," + anotherAuthorID + ")");
                            out.flush();
                            numOfSelectedNegativeSample++;
                        }
                    }
                }
            }
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return numOfSelectedNegativeSample;
    }

    private static void getTrainingData(String authorID_paperID_T0_FileName, String paperID_Year_T0_FileName,
            String authorID_paperID_T1_FileName, String paperID_Year_T1_FileName,
            String authorID_paperID_T2_FileName, String paperID_Year_T2_FileName,
            String authorID_paperID_T3_FileName, String paperID_Year_T3_FileName,
            String outFile_IDList, String outFile_PositiveSample, String outFile_NegativeSample, int forTrainingOrTesting, boolean isForJunior) {

        //<editor-fold defaultstate="collapsed" desc="For The RealData">
        // G0 in T0
        CoAuthorGraph G0 = new CoAuthorGraph(authorID_paperID_T0_FileName, paperID_Year_T0_FileName);
        // G1 in T1
        CoAuthorGraph G1 = new CoAuthorGraph(authorID_paperID_T1_FileName, paperID_Year_T1_FileName, 2001, 2003);

        // G2 in T2
        CoAuthorGraph G2 = new CoAuthorGraph(authorID_paperID_T2_FileName, paperID_Year_T2_FileName, 2004, 2006);

        // G3 in T3
        CoAuthorGraph G3 = new CoAuthorGraph(authorID_paperID_T3_FileName, paperID_Year_T3_FileName, 2007, 2009);

        // Extracting juniors/seniors for Training for Testing
        int numOfPositiveSample = 0;
        int numOfSelectedNagativeSample = 0;
        
        if (forTrainingOrTesting == 1) { // For Training
             System.out.println("----------TRAINING DATA------------");
            // Loc ra cac junior researchers ton tai trong G1, nhung chua ton tai trong G0.
            // Tuc moi bat dau nghien cuu va lan dau xuat hien trong cong dong trong G1. So bai bao trong trong G1 < 3, citation = 0?
            if (isForJunior == true) {
                getListofJuniorFromCoAuthorGraph(G0, G1, outFile_IDList);
            } else {
                // Loc ra cac senior researchers trong G1. So bai bao >= 3, citation >= ?
                getListofSeniorFromCoAuthorGraph(G1, outFile_IDList);
            }

            // Loc ra cac link(+) xuat hien trong G2 cua cac junior/senior (xuat hien trong G1)
            HashMap<Integer, Integer> authorIDHM = loadAuthorIDFromTextFile(outFile_IDList);
            numOfPositiveSample = getPositiveSampleFromCoAuthorGraph(authorIDHM, G1, G2, outFile_PositiveSample);

            // Chi chon mau am (-) trong G2 cho nhung junior/senior ma co xuat hien mau (+) trong G2. 
            // Khong can xet nhung junior/senior ma khong co mau (+)
            // Chon cac cap author khong link trong G2 va G1 cho cac junior/senior (xuat hien trong G1), quet mang CoAuthor ban kinh la 3 (3-Hops)
            HashMap<Integer, Integer> authorsInPositveSample = loadAuthorIDFromPositiveSample(outFile_PositiveSample);
            System.out.println("Tong so Researchers trong G2:" + G2._coAuthorGraph.size());
            if (isForJunior == true) {
                System.out.println("So Juniors lien quan mau (+):" + authorsInPositveSample.size());
            }
            else {
                System.out.println("So Seniors lien quan mau (+):" + authorsInPositveSample.size());
            }
            
            //numOfSelectedNagativeSample = getAllNegativeSampleFromCoAuthorGraphIn2Hub(authorsInPositveSample, G1, G2, outFile_NegativeSample);
            numOfSelectedNagativeSample = getAllNegativeSampleFromCoAuthorGraphIn3Hub(authorsInPositveSample, G1, G2, outFile_NegativeSample);
        }
        
        
        if (forTrainingOrTesting == 2) { // For Testing
            System.out.println("----------TESTING DATA------------");
            // Loc ra cac juniors ton tai trong G2, nhung chua ton tai trong G1.
            // Tuc moi bat dau nghien cuu va lan dau xuat hien trong cong dong trong G2. So bai bao trong trong G2 < 3
            if (isForJunior == true) { 
                getListofJuniorFromCoAuthorGraph(G1, G2, outFile_IDList);
            }
            else {
                // Loc ra cac seniors trong G2. So bai bao >= 3, citation >= ?
                getListofSeniorFromCoAuthorGraph(G2, outFile_IDList);
            }
            
            // Loc ra cac link(+) xuat hien trong G3 cua cac junior/senior (xuat hien trong G2)
            HashMap<Integer, Integer> authorIDHM = loadAuthorIDFromTextFile(outFile_IDList);
            numOfPositiveSample = getPositiveSampleFromCoAuthorGraph(authorIDHM, G2, G3, outFile_PositiveSample);

            // Chi chon mau am (-) trong G3 cho nhung junior/senior ma co xuat hien mau (+) trong G3. 
            // Khong can xet nhung junior/senior ma khong co mau (+)
            // Chon cac cap author khong link trong G3 va G2 cho cac junior/senior (xuat hien trong G2), quet mang CoAuthor ban kinh la 3 (3-Hub)
            HashMap<Integer, Integer> authorsInPositveSample = loadAuthorIDFromPositiveSample(outFile_PositiveSample);
            System.out.println("Tong so Researchers trong G3:" + G3._coAuthorGraph.size());
            if (isForJunior == true) { 
                System.out.println("So Juniors lien quan mau (+):" + authorsInPositveSample.size());
            } else {
                System.out.println("So seniors lien quan mau (+):" + authorsInPositveSample.size());
            }

            //numOfSelectedNagativeSample = getAllNegativeSampleFromCoAuthorGraphIn2Hub(authorsInPositveSample, G2, G3, outFile_NegativeSample);
            numOfSelectedNagativeSample = getAllNegativeSampleFromCoAuthorGraphIn3Hub(authorsInPositveSample, G2, G3, outFile_NegativeSample);
        }

        System.out.println("So mau (+):" + numOfPositiveSample);
        System.out.println("So mau (-):" + numOfSelectedNagativeSample);
        //</editor-fold>
    }

    /**
     *
     * @param featureFile, need to be formated as
     * PositiveSample/NegativeSample_<FeatureName>.txt
     * @param features
     */
    private static HashMap<Pair, HashMap<String, Double>> readFeatureFile(String featureFile,
            HashMap<Pair, HashMap<String, Double>> features) {
        try {
            int start = featureFile.lastIndexOf('_') + 1;
            if (start <= 8)// skip file only contains pair of author
            {
                return features;
            }

            int end = featureFile.lastIndexOf('.');
            String featureName = featureFile.substring(start, end);
            Scanner input = new Scanner(new FileReader(featureFile));
            input.nextLine();//skip file header
            Pattern r1 = Pattern.compile("\\s");
            Pattern r2 = Pattern.compile("\\D");
            while (input.hasNext()) {
                String line = input.nextLine().trim();
                String[] tokens = r1.split(line);
                if (tokens.length != 2)//content is wrong format
                {
                    break;
                }
                Double featureValue = new Double(tokens[1]);
                tokens = r2.split(tokens[0]);
                Pair authorPair = new Pair();
                authorPair.setFirst(new Integer(tokens[1]));
                authorPair.setSecond(new Integer(tokens[2]));

                if (features.containsKey(authorPair)) {
                    HashMap<String, Double> values = features.get(authorPair);
                    values.put(featureName, featureValue);
                    features.replace(authorPair, values);
                } else {
                    HashMap<String, Double> values = new HashMap<String, Double>();
                    values.put(featureName, featureValue);
                    features.put(authorPair, values);
//                    Logger.getLogger(MLDataExtraction.class.getName()).log(Level.INFO, "Create new author pair");
                }
            }
            input.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(MLDataExtraction.class.getName()).log(Level.SEVERE, null, ex);
        }
        return features;
    }

    /**
     *
     * @param features
     * @param featureFile, need to be formated as
     */
    private static void writeFeatureFile(HashMap<Pair, HashMap<String, Double>> features,
            String featureFile) {
        try {
            FileWriter out = new FileWriter(featureFile);
            Map.Entry<Pair, HashMap<String, Double>> aEntry = features.entrySet().iterator().next();
            Set<String> featureSet = aEntry.getValue().keySet();
            String header = "";
            for (String name : featureSet) {
                header += (name + ",");
            }
            header += "TypeOfSample";
            out.write(header + "\n");
            int lines = 0;
            for (Map.Entry<Pair, HashMap<String, Double>> entry : features.entrySet()) {
                lines++;
                Pair p = entry.getKey();
                StringBuilder line = new StringBuilder();
//                line.append(p.toString());
                HashMap<String, Double> f = entry.getValue();
                for (Double d : f.values()) {
                    line.append(d.doubleValue() + ",");
                }
                if (featureFile.contains("Positive")) {
                    line.append("Positive\n");
                } else {
                    line.append("Negative\n");
                }
                out.append(line.toString());
            }
            out.close();
            System.out.println("So dong cua tap tin: " + lines);
        } catch (Exception ex) {
            Logger.getLogger(MLDataExtraction.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     *
     * @param featuresFolder, folder contains feature files.
     * @param typeOfSample
     */
    private static HashMap<Pair, HashMap<String, Double>> aggregateFeatures(String featuresFolder, int typeOfSample) {
        HashMap<Pair, HashMap<String, Double>> result = new HashMap<>();
        String fileNamePattern;
        if (typeOfSample == 1) {
            fileNamePattern = "PositiveSample";
        } else {
            fileNamePattern = "NegativeSample";
        }

        try {
            List<String> featureFiles = TextFileUtility.getPathFile(new File(featuresFolder));
            for (String fileName : featureFiles) {
                if (fileName.contains(fileNamePattern)) {
                    readFeatureFile(fileName, result);
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(MLDataExtraction.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    public static void main(String args[]) {
        
        //<editor-fold defaultstate="collapsed" desc="sample data">
        // For Juniors
//        System.out.println("----------JUNIORS DATA------------");
//        getTrainingData("/2.CRS-ExperimetalData/SampleData/AuthorID_PaperID_Before_2003.txt",
//                "/2.CRS-ExperimetalData/SampleData/PaperID_Year_Before_2003.txt",
//                "/2.CRS-ExperimetalData/SampleData/AuthorID_PaperID_2003_2005.txt",
//                "/2.CRS-ExperimetalData/SampleData/PaperID_Year_2003_2005.txt",
//                "/2.CRS-ExperimetalData/SampleData/AuthorID_PaperID_2006_2008.txt",
//                "/2.CRS-ExperimetalData/SampleData/PaperID_Year_2006_2008.txt",
//                "/2.CRS-ExperimetalData/SampleData/AuthorID_PaperID_2009_2011.txt",
//                "/2.CRS-ExperimetalData/SampleData/PaperID_Year_2009_2011.txt",
//                "/2.CRS-ExperimetalData/SampleData/Training_JuniorIDList.txt",
//                "/2.CRS-ExperimetalData/SampleData/Training_PositiveSamples_Junior.txt",
//                "/2.CRS-ExperimetalData/SampleData/Training_NegativeSamples_Junior.txt",
//                1, true);
//        
//        getTrainingData("/2.CRS-ExperimetalData/SampleData/AuthorID_PaperID_Before_2003.txt",
//                "/2.CRS-ExperimetalData/SampleData/PaperID_Year_Before_2003.txt",
//                "/2.CRS-ExperimetalData/SampleData/AuthorID_PaperID_2003_2005.txt",
//                "/2.CRS-ExperimetalData/SampleData/PaperID_Year_2003_2005.txt",
//                "/2.CRS-ExperimetalData/SampleData/AuthorID_PaperID_2006_2008.txt",
//                "/2.CRS-ExperimetalData/SampleData/PaperID_Year_2006_2008.txt",
//                "/2.CRS-ExperimetalData/SampleData/AuthorID_PaperID_2009_2011.txt",
//                "/2.CRS-ExperimetalData/SampleData/PaperID_Year_2009_2011.txt",
//                "/2.CRS-ExperimetalData/SampleData/Testing_JuniorIDList.txt",
//                "/2.CRS-ExperimetalData/SampleData/Testing_PositiveSamples_Junior.txt",
//                "/2.CRS-ExperimetalData/SampleData/Testing_NegativeSamples_Junior.txt",
//                2, true);
//        
//        // For Seniors
//        System.out.println("----------SENIORS DATA------------");
//        getTrainingData("/2.CRS-ExperimetalData/SampleData/AuthorID_PaperID_Before_2003.txt",
//                "/2.CRS-ExperimetalData/SampleData/PaperID_Year_Before_2003.txt",
//                "/2.CRS-ExperimetalData/SampleData/AuthorID_PaperID_2003_2005.txt",
//                "/2.CRS-ExperimetalData/SampleData/PaperID_Year_2003_2005.txt",
//                "/2.CRS-ExperimetalData/SampleData/AuthorID_PaperID_2006_2008.txt",
//                "/2.CRS-ExperimetalData/SampleData/PaperID_Year_2006_2008.txt",
//                "/2.CRS-ExperimetalData/SampleData/AuthorID_PaperID_2009_2011.txt",
//                "/2.CRS-ExperimetalData/SampleData/PaperID_Year_2009_2011.txt",
//                "/2.CRS-ExperimetalData/SampleData/Training_SeniorIDList.txt",
//                "/2.CRS-ExperimetalData/SampleData/Training_PositiveSamples_Senior.txt",
//                "/2.CRS-ExperimetalData/SampleData/Training_NegativeSamples_Senior.txt",
//                1, false);
//        
//        getTrainingData("/2.CRS-ExperimetalData/SampleData/AuthorID_PaperID_Before_2003.txt",
//                "/2.CRS-ExperimetalData/SampleData/PaperID_Year_Before_2003.txt",
//                "/2.CRS-ExperimetalData/SampleData/AuthorID_PaperID_2003_2005.txt",
//                "/2.CRS-ExperimetalData/SampleData/PaperID_Year_2003_2005.txt",
//                "/2.CRS-ExperimetalData/SampleData/AuthorID_PaperID_2006_2008.txt",
//                "/2.CRS-ExperimetalData/SampleData/PaperID_Year_2006_2008.txt",
//                "/2.CRS-ExperimetalData/SampleData/AuthorID_PaperID_2009_2011.txt",
//                "/2.CRS-ExperimetalData/SampleData/PaperID_Year_2009_2011.txt",
//                "/2.CRS-ExperimetalData/SampleData/Testing_SeniorIDList.txt",
//                "/2.CRS-ExperimetalData/SampleData/Testing_PositiveSamples_Senior.txt",
//                "/2.CRS-ExperimetalData/SampleData/Testing_NegativeSamples_Senior.txt",
//                2, false);
//        //</editor-fold>
        
        //<editor-fold defaultstate="collapsed" desc="Real data">
        // For juniors
        getTrainingData("D:\\1.CRS-Experiment\\MLData\\3-Hub\\TrainingData\\AuthorID_PaperID_Before_2001.txt",
                "D:\\1.CRS-Experiment\\MLData\\3-Hub\\TrainingData\\PaperID_Year_Before_2001.txt",
                "D:\\1.CRS-Experiment\\MLData\\3-Hub\\TrainingData\\AuthorID_PaperID_2001_2003.txt",
                "D:\\1.CRS-Experiment\\MLData\\3-Hub\\TrainingData\\PaperID_Year_2001_2003.txt",
                "D:\\1.CRS-Experiment\\MLData\\3-Hub\\TrainingData\\AuthorID_PaperID_2004_2006.txt",
                "D:\\1.CRS-Experiment\\MLData\\3-Hub\\TrainingData\\PaperID_Year_2004_2006.txt",
                "D:\\1.CRS-Experiment\\MLData\\3-Hub\\TrainingData\\AuthorID_PaperID_2007_2009.txt",
                "D:\\1.CRS-Experiment\\MLData\\3-Hub\\TrainingData\\PaperID_Year_2007_2009.txt",
                "D:\\1.CRS-Experiment\\MLData\\3-Hub\\TrainingData\\Training_JuniorIDList.txt",
                "D:\\1.CRS-Experiment\\MLData\\3-Hub\\TrainingData\\Training_PositiveSamples_Junior.txt",
                "D:\\1.CRS-Experiment\\MLData\\3-Hub\\TrainingData\\Training_NegativeSamples_Junior.txt",
                1, true);
        getTrainingData("D:\\1.CRS-Experiment\\MLData\\3-Hub\\TrainingData\\AuthorID_PaperID_Before_2001.txt",
                "D:\\1.CRS-Experiment\\MLData\\3-Hub\\TrainingData\\PaperID_Year_Before_2001.txt",
                "D:\\1.CRS-Experiment\\MLData\\3-Hub\\TrainingData\\AuthorID_PaperID_2001_2003.txt",
                "D:\\1.CRS-Experiment\\MLData\\3-Hub\\TrainingData\\PaperID_Year_2001_2003.txt",
                "D:\\1.CRS-Experiment\\MLData\\3-Hub\\TrainingData\\AuthorID_PaperID_2004_2006.txt",
                "D:\\1.CRS-Experiment\\MLData\\3-Hub\\TrainingData\\PaperID_Year_2004_2006.txt",
                "D:\\1.CRS-Experiment\\MLData\\3-Hub\\TrainingData\\AuthorID_PaperID_2007_2009.txt",
                "D:\\1.CRS-Experiment\\MLData\\3-Hub\\TrainingData\\PaperID_Year_2007_2009.txt",
                "D:\\1.CRS-Experiment\\MLData\\3-Hub\\TrainingData\\Testing_JuniorIDList.txt",
                "D:\\1.CRS-Experiment\\MLData\\3-Hub\\TrainingData\\Testing_PositiveSamples_Junior.txt",
                "D:\\1.CRS-Experiment\\MLData\\3-Hub\\TrainingData\\Testing_NegativeSamples_Junior.txt",
                2, true);
        
        // For seniors
        getTrainingData("D:\\1.CRS-Experiment\\MLData\\3-Hub\\TrainingData\\AuthorID_PaperID_Before_2001.txt",
                "D:\\1.CRS-Experiment\\MLData\\3-Hub\\TrainingData\\PaperID_Year_Before_2001.txt",
                "D:\\1.CRS-Experiment\\MLData\\3-Hub\\TrainingData\\AuthorID_PaperID_2001_2003.txt",
                "D:\\1.CRS-Experiment\\MLData\\3-Hub\\TrainingData\\PaperID_Year_2001_2003.txt",
                "D:\\1.CRS-Experiment\\MLData\\3-Hub\\TrainingData\\AuthorID_PaperID_2004_2006.txt",
                "D:\\1.CRS-Experiment\\MLData\\3-Hub\\TrainingData\\PaperID_Year_2004_2006.txt",
                "D:\\1.CRS-Experiment\\MLData\\3-Hub\\TrainingData\\AuthorID_PaperID_2007_2009.txt",
                "D:\\1.CRS-Experiment\\MLData\\3-Hub\\TrainingData\\PaperID_Year_2007_2009.txt",
                "D:\\1.CRS-Experiment\\MLData\\3-Hub\\TrainingData\\Training_SeniorIDList.txt",
                "D:\\1.CRS-Experiment\\MLData\\3-Hub\\TrainingData\\Training_PositiveSamples_Senior.txt",
                "D:\\1.CRS-Experiment\\MLData\\3-Hub\\TrainingData\\Training_NegativeSamples_Senior.txt",
                1, false);
        getTrainingData("D:\\1.CRS-Experiment\\MLData\\3-Hub\\TrainingData\\AuthorID_PaperID_Before_2001.txt",
                "D:\\1.CRS-Experiment\\MLData\\3-Hub\\TrainingData\\PaperID_Year_Before_2001.txt",
                "D:\\1.CRS-Experiment\\MLData\\3-Hub\\TrainingData\\AuthorID_PaperID_2001_2003.txt",
                "D:\\1.CRS-Experiment\\MLData\\3-Hub\\TrainingData\\PaperID_Year_2001_2003.txt",
                "D:\\1.CRS-Experiment\\MLData\\3-Hub\\TrainingData\\AuthorID_PaperID_2004_2006.txt",
                "D:\\1.CRS-Experiment\\MLData\\3-Hub\\TrainingData\\PaperID_Year_2004_2006.txt",
                "D:\\1.CRS-Experiment\\MLData\\3-Hub\\TrainingData\\AuthorID_PaperID_2007_2009.txt",
                "D:\\1.CRS-Experiment\\MLData\\3-Hub\\TrainingData\\PaperID_Year_2007_2009.txt",
                "D:\\1.CRS-Experiment\\MLData\\3-Hub\\TrainingData\\Testing_SeniorIDList.txt",
                "D:\\1.CRS-Experiment\\MLData\\3-Hub\\TrainingData\\Testing_PositiveSamples_Senior.txt",
                "D:\\1.CRS-Experiment\\MLData\\3-Hub\\TrainingData\\Testing_NegativeSamples_Senior.txt",
                2, false);
        //</editor-fold>
        
//        HashMap<Pair, HashMap<String, Double>> model = aggregateFeatures("D:\\1.CRS-Experiment\\MLData\\3-Hub\\TrainingData\\", 1);
//        writeFeatureFile(model, "D:\\1.CRS-Experiment\\MLData\\3-Hub\\TrainingData\\Training_PositiveSample_AllFeatures.txt");
//
//        model = aggregateFeatures("D:\\1.CRS-Experiment\\MLData\\3-Hub\\TrainingData\\", 0);
//        writeFeatureFile(model, "D:\\1.CRS-Experiment\\MLData\\3-Hub\\TrainingData\\Training_NegativeSample_AllFeatures.txt");
//
//        model = aggregateFeatures("D:\\1.CRS-Experiment\\MLData\\3-Hub\\TestingData\\", 1);
//        writeFeatureFile(model, "D:\\1.CRS-Experiment\\MLData\\3-Hub\\TestingData\\Testing_PositiveSample_AllFeatures.txt");
//
//        model = aggregateFeatures("D:\\1.CRS-Experiment\\MLData\\3-Hub\\TestingData\\", 0);
//        writeFeatureFile(model, "D:\\1.CRS-Experiment\\MLData\\3-Hub\\TestingData\\Testing_NegativeSample_AllFeatures.txt");
        
        System.out.println("DONE");
    }
}
