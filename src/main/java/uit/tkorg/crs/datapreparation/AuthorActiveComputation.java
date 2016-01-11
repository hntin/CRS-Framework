/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uit.tkorg.crs.datapreparation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import uit.tkorg.crs.model.Pair;
import uit.tkorg.crs.model.Sample;

/**
 *
 * @author TinHuynh
 */
public class AuthorActiveComputation extends FeatureComputation {

    private HashMap<Integer, Integer> _paperID_Year;
    private HashMap<Integer, ArrayList<Integer>> _paperID_AuthorID_List;
    private HashMap<Integer, ArrayList<Integer>> _authorID_PaperID_List;
    private final String _file_AuthorID_PaperID;
    private final String _file_PaperID_Year;
    private final int _startYear;
    private final int _currentYear;

    public AuthorActiveComputation(String file_AuthorID_PaperID, String file_PaperID_Year, int startYear, int currentYear) {
        _file_AuthorID_PaperID = file_AuthorID_PaperID;
        _file_PaperID_Year = file_PaperID_Year;
        _startYear = startYear;
        _currentYear = currentYear;
        this.load_PaperID_Year();
        this.load_AuthorID_PaperID();
    }

    public AuthorActiveComputation(String postiveSampleFile, String negativeSampleFile,
            String file_AuthorID_PaperID, String file_PaperID_Year, int startYear, int currentYear) {
        _file_AuthorID_PaperID = file_AuthorID_PaperID;
        _file_PaperID_Year = file_PaperID_Year;
        _startYear = startYear;
        _currentYear = currentYear;
        this.load_PaperID_Year();
        this.load_AuthorID_PaperID();
        this._positiveSample = Sample.readSampleFile(postiveSampleFile);
        this._negativeSample = Sample.readSampleFile(negativeSampleFile);
    }

    /**
     * load_PaperID_Year
     */
    private void load_PaperID_Year() {
        try {
            _paperID_Year = new HashMap<>();
            FileInputStream fis = new FileInputStream(_file_PaperID_Year);
            Reader reader = new InputStreamReader(fis, "UTF8");
            BufferedReader bufferReader = new BufferedReader(reader);
            bufferReader.readLine();
            String line = null;
            String[] tokens;
            int paperId;
            Integer year;
            while ((line = bufferReader.readLine()) != null) {
                tokens = line.split(",");
                paperId = Integer.parseInt(tokens[0]);
                if (tokens.length <= 1) {
                    year = 0;
                } else {
                    year = Integer.parseInt(tokens[1]);
                }
                _paperID_Year.put(paperId, year);
            }
            bufferReader.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * load_AuthorID_PaperID
     */
    private void load_AuthorID_PaperID() {
        try {
            _authorID_PaperID_List = new HashMap<>();
            _paperID_AuthorID_List = new HashMap<>();
            FileInputStream fis = new FileInputStream(_file_AuthorID_PaperID);
            Reader reader = new InputStreamReader(fis, "UTF8");
            BufferedReader bufferReader = new BufferedReader(reader);
            bufferReader.readLine();
            String line = null;
            String[] tokens;
            int authorId;
            int paperId;
            while ((line = bufferReader.readLine()) != null) {
                tokens = line.split(",");
                if (tokens.length == 2) {
                    authorId = Integer.parseInt(tokens[0]);
                    paperId = Integer.parseInt(tokens[1]);

                    ArrayList<Integer> listPaper = _authorID_PaperID_List.get(authorId);
                    if (listPaper == null) {
                        listPaper = new ArrayList<>();
                    }
                    listPaper.add(paperId);
                    _authorID_PaperID_List.put(authorId, listPaper);

                    ArrayList<Integer> listAuthor = _paperID_AuthorID_List.get(paperId);
                    if (listAuthor == null) {
                        listAuthor = new ArrayList<>();
                    }
                    listAuthor.add(authorId);
                    _paperID_AuthorID_List.put(paperId, listAuthor);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * calculateActiveScore
     *
     * @return
     */
    private HashMap<Integer, Float> calculateActiveScore() {
        HashMap<Integer, Float> activeScoreHM = new HashMap<>();
        int count = 0;
        for (int authorID : _authorID_PaperID_List.keySet()) {
            count++;
            if (count % 100 == 0) {
                System.out.println(count);
            }

            int minYear = Integer.MAX_VALUE;
            ArrayList<Integer> paperIDList = _authorID_PaperID_List.get(authorID);

            HashMap<Integer, Integer> year_NumOfPub = new HashMap<>();
            for (int paperID : paperIDList) {
                if (_paperID_Year.containsKey(paperID)) {
                    int paperYear = _paperID_Year.get(paperID);
                    if (paperYear != 0) {
                        int numOfPub = 0;
                        if (year_NumOfPub.containsKey(paperYear)) {
                            numOfPub = year_NumOfPub.get(paperYear);
                        }
                        numOfPub++;
                        year_NumOfPub.put(paperYear, numOfPub);

                        if (paperYear < minYear) {
                            minYear = paperYear;
                        }
                    }
                }
            }

            float activeScore = 0.f;
            for (int startYear = minYear; startYear <= _currentYear; startYear++) {
                if (year_NumOfPub.containsKey(startYear)) {
                    int num_Pub_In_Year = year_NumOfPub.get(startYear);
                    activeScore += (float) num_Pub_In_Year * (1 / Math.exp(_currentYear - startYear));
                }
            }

            activeScoreHM.put(authorID, activeScore);
        }

        return activeScoreHM;
    }

    /**
     * normalizeActiveScore
     *
     * @param activeScoreHM
     * @return
     */
    private HashMap<Integer, Float> normalizeActiveScore(HashMap<Integer, Float> activeScoreHM) {
        HashMap<Integer, Float> activeScoreNormalizedHM = new HashMap<>();
        float minValue = Float.MAX_VALUE;
        float maxValue = Float.MIN_VALUE;
        float activeValue = 0.f;
        for (int authorID : activeScoreHM.keySet()) {
            activeValue = activeScoreHM.get(authorID);
            if (activeValue < minValue) {
                minValue = activeValue;
            }

            if (activeValue > maxValue) {
                maxValue = activeValue;
            }
        }

        float normalizedValue = 0.f;
        for (int authorID : activeScoreHM.keySet()) {
            activeValue = activeScoreHM.get(authorID);
            normalizedValue = (activeValue - minValue) / (maxValue - minValue);
            activeScoreNormalizedHM.put(authorID, normalizedValue);
        }

        return activeScoreNormalizedHM;
    }

    /**
     * Calculating active score for second authorID in positive/negative pairs
     * and out to the specified file
     *
     * @param outputFile
     * @param typeOfSample
     * @throws IOException
     */
    @Override
    public void computeFeatureValues(String outputFile, int typeOfSample) throws IOException {
        ArrayList<Pair> pairs = null;
        if (typeOfSample == 1) {
            pairs = this._positiveSample.getPairOfAuthor();
        } else {
            pairs = this._negativeSample.getPairOfAuthor();
        }

        HashMap<Integer, Float> activeScoreHM = this.calculateActiveScore();
        //activeScoreHM = this.normalizeActiveScore(activeScoreHM);
        try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(outputFile)))) {
            out.println("AuthorID, Another-AuthorID, ActiveScore");
            out.flush();
            for (Pair p : pairs) {
                int firstAuthorID = p.getFirst();
                int secondAuthorID = p.getSecond();
                float activeScore = activeScoreHM.get(secondAuthorID);
                out.println("(" + firstAuthorID + "," + secondAuthorID + ")\t" + activeScore);
                out.flush();
            }
        }

    }

    public static void main(String args[]) throws IOException {
        //<editor-fold defaultstate="collapsed" desc="Sample Data">
        // For Training Data
//        AuthorActiveComputation authorActiveComputation;
//        authorActiveComputation = new AuthorActiveComputation(
//                "/2.CRS-ExperimetalData/SampleData/Training_PositiveSamples.txt",
//                "/2.CRS-ExperimetalData/SampleData/Training_NegativeSamples.txt",
//                "/2.CRS-ExperimetalData/SampleData/AuthorID_PaperID_Before_Include_2005.txt",
//                "/2.CRS-ExperimetalData/SampleData/PaperID_Year_Before_Include_2005.txt",
//                0, 2005);
//        
//        authorActiveComputation.computeFeatureValues("/2.CRS-ExperimetalData/SampleData/Training_PositiveSampleActiveScore.txt", 1);
//        authorActiveComputation.computeFeatureValues("/2.CRS-ExperimetalData/SampleData/Training_NegativeSampleActiveScore.txt", 0);
//        
//        // For Testing Data
//        authorActiveComputation = new AuthorActiveComputation(
//                "/2.CRS-ExperimetalData/SampleData/Testing_PositiveSamples.txt",
//                "/2.CRS-ExperimetalData/SampleData/Testing_NegativeSamples.txt",
//                "/2.CRS-ExperimetalData/SampleData/AuthorID_PaperID_Before_Include_2008.txt",
//                "/2.CRS-ExperimetalData/SampleData/PaperID_Year_Before_Include_2008.txt",
//                0, 2008);
//        
//        authorActiveComputation.computeFeatureValues("/2.CRS-ExperimetalData/SampleData/Testing_PositiveSampleActiveScore.txt", 1);
//        authorActiveComputation.computeFeatureValues("/2.CRS-ExperimetalData/SampleData/Testing_NegativeSampleActiveScore.txt", 0);
        //</editor-fold>

//            //<editor-fold defaultstate="collapsed" desc="Real Data">
//            // For Training Data
            AuthorActiveComputation authorActiveComputation;
//            authorActiveComputation = new AuthorActiveComputation(
//                "D:\\1.CRS-Experiment\\MLData\\3-Hub\\Senior\\TrainingData\\Training_PositiveSamples.txt",
//                "D:\\1.CRS-Experiment\\MLData\\3-Hub\\Senior\\TrainingData\\Training_NegativeSamples.txt",
//                "D:\\1.CRS-Experiment\\MLData\\AuthorID_PaperID_Before_Include_2003.txt",
//                "D:\\1.CRS-Experiment\\MLData\\PaperID_Year_Before_Include_2003.txt",
//                0, 2003);
//
//            authorActiveComputation.computeFeatureValues("D:\\1.CRS-Experiment\\MLData\\3-Hub\\Senior\\TrainingData\\Training_PositiveSampleActiveScore.txt", 1);
//            authorActiveComputation.computeFeatureValues("D:\\1.CRS-Experiment\\MLData\\3-Hub\\Senior\\TrainingData\\Training_NegativeSampleActiveScore.txt", 0);

            // For Testing Data
            authorActiveComputation = new AuthorActiveComputation(
                "D:\\1.CRS-Experiment\\MLData\\3-Hub\\Senior\\TestingData\\Testing_PositiveSamples.txt",
                "D:\\1.CRS-Experiment\\MLData\\3-Hub\\Senior\\TestingData\\Testing_NegativeSamples.txt",
                "D:\\1.CRS-Experiment\\MLData\\AuthorID_PaperID_Before_Include_2006.txt",
                "D:\\1.CRS-Experiment\\MLData\\PaperID_Year_Before_Include_2006.txt",
                0, 2006);

            authorActiveComputation.computeFeatureValues("D:\\1.CRS-Experiment\\MLData\\3-Hub\\Senior\\TestingData\\Testing_PositiveSample_ActiveScore.txt", 1);
            authorActiveComputation.computeFeatureValues("D:\\1.CRS-Experiment\\MLData\\3-Hub\\Senior\\TestingData\\Testing_NegativeSample_ActiveScore.txt", 0);
        //</editor-fold>
        System.out.println("AuthorActiveComputation .... DONE DONE DONE");
    }

}
