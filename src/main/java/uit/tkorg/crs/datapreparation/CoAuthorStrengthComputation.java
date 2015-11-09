/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uit.tkorg.crs.datapreparation;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import uit.tkorg.crs.model.CoAuthorGraph;
import uit.tkorg.crs.model.Sample;
import uit.tkorg.crs.model.Pair;
import uit.tkorg.crs.method.link.RSS;

/**
 * Calculating CoAuthorStrength (RSS+) for each pair<authorID, authorID> in
 * Positive/Negative sample files Input: Positive and Negative Samples from
 * files, file to build CoAuthor Graph in the specified period Ti Output: File
 * storing values of CoAuthor_RSS of all pairs in the sample files
 *
 * @author thucnt
 */
public class CoAuthorStrengthComputation extends FeatureComputation {

    private final CoAuthorGraph _coAuthorGraph;

    public CoAuthorStrengthComputation(String postiveSampleFile, String negativeSampleFile,
            String authorID_paperID_FileName_Ti, String paperID_Year_FileName_Ti, int firstYear, int lastYear) {
        this._positiveSample = Sample.readSampleFile(postiveSampleFile);
        this._negativeSample = Sample.readSampleFile(negativeSampleFile);
        // Building the CoAuthorGraph 
        _coAuthorGraph = new CoAuthorGraph(authorID_paperID_FileName_Ti, paperID_Year_FileName_Ti, firstYear, lastYear);
    }

    /**
     * 
     * @param outputFile
     * @param typeOfSample: 1 is positive; 0 is negative
     * @throws java.io.IOException
     */
    @Override
    public void computeFeatureValues(String outputFile, int typeOfSample) throws IOException {
        // Step 1: Getting all distinct juniorAuthorID (firstAuthorID in a pair)
        HashMap<Integer, String> firstAuthorIDList;
        ArrayList<Pair> pairs = null;

        if (typeOfSample == 1) {
            firstAuthorIDList = this._positiveSample.readAllFirstAuthorID();
            pairs = this._positiveSample.getPairOfAuthor();
        }
        else {
            firstAuthorIDList = this._negativeSample.readAllFirstAuthorID();
            pairs = this._negativeSample.getPairOfAuthor();
        }

        // Step 2: Calculating RSSDoublePlus values for all juniorAuthorID with all others 
        // WHICH HO NO LINK in 3-hub in the CoAuthorGraph
        RSS methodRSS = new RSS();
        HashMap<Integer, HashMap<Integer, Float>> rssDoublePlus_FirstAuthorID_Nodes_NoDirectedLink_In3Hub = methodRSS.process(
                _coAuthorGraph._rssDoublePlusGraph, firstAuthorIDList);
 
        // Step 3: Extracting RSSDoublePlus Values for all pairs of PositveSamples
        //HashMap<Integer, HashMap<Integer, Float>> rssDoublePlus_Samples_HM = new HashMap<>();

        try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(outputFile)))) {
            out.println("AuthorID, CoAuthorID, rssDoublePlusValue");
            out.flush();
            for (Pair p : pairs) {
                int firstAuthorID = p.getFirst();
                int secondAuthorID = p.getSecond();                
                float rssDoublePlusValue = 0;
                if (rssDoublePlus_FirstAuthorID_Nodes_NoDirectedLink_In3Hub.containsKey(firstAuthorID)
                        && rssDoublePlus_FirstAuthorID_Nodes_NoDirectedLink_In3Hub.get(firstAuthorID).containsKey(secondAuthorID)) {
                    rssDoublePlusValue = rssDoublePlus_FirstAuthorID_Nodes_NoDirectedLink_In3Hub.get(firstAuthorID).get(secondAuthorID);
                } 
//                else {
//                    if (_coAuthorGraph._rssDoublePlusGraph.get(firstAuthorID).containsKey(secondAuthorID)) {
//                        rssDoublePlusValue = _coAuthorGraph._rssDoublePlusGraph.get(firstAuthorID).get(secondAuthorID);
//                    }
//                    System.out.println("NO DAY NE.." + rssDoublePlusValue);
//                }
                
                out.println("(" + firstAuthorID + "," + secondAuthorID + ")\t" + rssDoublePlusValue);
                out.flush();
                
//            HashMap<Integer, Float> hm = rssDoublePlus_Samples_HM.get(firstAuthorID);
//            if (hm == null || hm.isEmpty()) {
//                hm = new HashMap<>();
//            }
//
//            hm.put(secondAuthorID, rssDoublePlusValue);
//            rssDoublePlus_Samples_HM.put(firstAuthorID, hm);
            }
        }
    }

    public static void main(String args[]) {
        try {
//            CoAuthorStrengthComputation obj = new CoAuthorStrengthComputation(
//                    "D:\\1.CRS-Experiment\\MLData\\TrainingData\\PositiveSamples.txt",
//                    "D:\\1.CRS-Experiment\\MLData\\TrainingData\\NegativeSamples.txt",
//                    "D:\\1.CRS-Experiment\\MLData\\TrainingData\\AuthorID_PaperID_2001_2003.txt",
//                    "D:\\1.CRS-Experiment\\MLData\\TrainingData\\PaperID_Year_2001_2003.txt", 2001, 2003);
//            obj.computeFeatureValues("D:\\1.CRS-Experiment\\MLData\\TrainingData\\PositiveSampleCoAuthorRSS.txt", 1);
//            obj.computeFeatureValues("D:\\1.CRS-Experiment\\MLData\\TrainingData\\NegativeSampleCoAuthorRSS.txt", 0);

            CoAuthorStrengthComputation obj = new CoAuthorStrengthComputation(
                    "/2.CRS-ExperimetalData/SampleData/PositiveSamples.txt",
                    "/2.CRS-ExperimetalData/SampleData/NegativeSamples.txt",
                    "/2.CRS-ExperimetalData/SampleData/AuthorID_PaperID_2003_2005.txt",
                    "/2.CRS-ExperimetalData/SampleData/PaperID_Year_2003_2005.txt", 2003, 2005);
            obj.computeFeatureValues("/2.CRS-ExperimetalData/SampleData/PositiveSampleCoAuthorRSS.txt", 1);
            obj.computeFeatureValues("/2.CRS-ExperimetalData/SampleData/NegativeSampleCoAuthorRSS.txt", 0);
            System.out.println("CoAuthorStrengthComputation ... DONE DONE DONE");
        } catch (Exception ex) {
        }
    }

    
}
