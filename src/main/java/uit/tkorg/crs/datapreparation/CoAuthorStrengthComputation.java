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
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.regex.Pattern;
import uit.tkorg.crs.method.link.RSSDoublePlus;
import uit.tkorg.crs.model.CoAuthorGraph;
import uit.tkorg.crs.model.Sample;
import uit.tkorg.crs.common.Pair;
import uit.tkorg.crs.utility.TextFileUtility;

/**
 * Calculating CoAuthorStrength (RSS+) for each pair<authorID, authorID> in
 * Positive/Negative sample files Input: Positive and Negative Samples from
 * files, file to build CoAuthor Graph in the specified period Ti Output: File
 * storing values of CoAuthor_RSS of all pairs in the sample files
 *
 * @author thucnt
 */
public class CoAuthorStrengthComputation extends FeatureComputation {

    private CoAuthorGraph _coAuthorGraph;

    public CoAuthorStrengthComputation(String postiveSampleFile, String negativeSampleFile,
            String authorID_paperID_FileName_Ti, String paperID_Year_FileName_Ti, int firstYear, int lastYear) {
        this._positiveSample = Sample.readSampleFile(postiveSampleFile);
        this._negativeSample = Sample.readSampleFile(negativeSampleFile);
        // Building the CoAuthorGraph 
        _coAuthorGraph = new CoAuthorGraph(authorID_paperID_FileName_Ti, paperID_Year_FileName_Ti, firstYear, lastYear);
    }

    @Override
    public void computeFeatureValues(String outputFile) {

    }

    @Override
    public void computeFeatureValues(String postiveOutputFile, String negativeOutputFile) throws Exception {
        // Step 1: Getting all distinct juniorAuthorID (firstAuthorID in a pair)
        HashMap<Integer, String> firstAuthorIDOfPositiveSample = this._positiveSample.readAllFirstAuthorID();
        HashMap<Integer, String> firstAuthorIDOfNegativeSample = this._negativeSample.readAllFirstAuthorID();

        // Step 2: Calculating RSSDoublePlus for all juniorID with all others in the CoAuthorGraph
        RSSDoublePlus methodRSSDoublePLus = new RSSDoublePlus();
        HashMap<Integer, HashMap<Integer, Float>> rssDoublePlus_FirstAuthorID_PostiveSample_ToAllOthers = methodRSSDoublePLus.process(
                _coAuthorGraph._rssDoublePlusGraph, firstAuthorIDOfPositiveSample);
        HashMap<Integer, HashMap<Integer, Float>> rssDoublePlus_FirstAuthorID_NegativeSample_ToAllOthers = methodRSSDoublePLus.process(
                _coAuthorGraph._rssDoublePlusGraph, firstAuthorIDOfNegativeSample);

        // Step 3: Extracting RSSSoublePlus Values for all pairs of PositveSamples, rssDoublePlus_PostiveSamples_HM
        HashMap<Integer, HashMap<Integer, Float>> rssDoublePlus_PostiveSamples_HM = new HashMap<>();
        for (int i = 0; i < this._positiveSample.getPairOfAuthor().size(); i++) {
            Pair p = this._positiveSample.getPairOfAuthor().get(i);
            int firstAuthorID = p.getFirst();
            int secondAuthorID = p.getSecond();

            float rssDoublePlusValue = 0;
            if (rssDoublePlus_FirstAuthorID_PostiveSample_ToAllOthers.containsKey(firstAuthorID)
                    && rssDoublePlus_FirstAuthorID_PostiveSample_ToAllOthers.get(firstAuthorID).containsKey(secondAuthorID)) {
                rssDoublePlusValue = rssDoublePlus_FirstAuthorID_PostiveSample_ToAllOthers.get(firstAuthorID).get(secondAuthorID);
            }

            HashMap<Integer, Float> hm = rssDoublePlus_PostiveSamples_HM.get(firstAuthorID);
            if (hm == null || hm.isEmpty()) {
                hm = new HashMap<>();
            }

            hm.put(secondAuthorID, rssDoublePlusValue);
            rssDoublePlus_PostiveSamples_HM.put(firstAuthorID, hm);
        }

        // Step 4: Extracting RSSSoublePlus Values for all pairs of NegativeSamples
        HashMap<Integer, HashMap<Integer, Float>> rssDoublePlus_NegativeSamples_HM = new HashMap<>();
        for (int i = 0; i < this._negativeSample.getPairOfAuthor().size(); i++) {
            Pair p = this._negativeSample.getPairOfAuthor().get(i);
            int firstAuthorID = p.getFirst();
            int secondAuthorID = p.getSecond();

            float rssDoublePlusValue = 0;
            if (rssDoublePlus_FirstAuthorID_NegativeSample_ToAllOthers.containsKey(firstAuthorID)
                    && rssDoublePlus_FirstAuthorID_NegativeSample_ToAllOthers.get(firstAuthorID).containsKey(secondAuthorID)) {
                rssDoublePlusValue = rssDoublePlus_FirstAuthorID_NegativeSample_ToAllOthers.get(firstAuthorID).get(secondAuthorID);
            }

            HashMap<Integer, Float> hm = rssDoublePlus_NegativeSamples_HM.get(firstAuthorID);
            if (hm == null || hm.isEmpty()) {
                hm = new HashMap<>();
            }

            hm.put(secondAuthorID, rssDoublePlusValue);
            rssDoublePlus_NegativeSamples_HM.put(firstAuthorID, hm);
        }

        // Step 5: Storing values which describe CoAuthorStrength (RSSSoublePlus) for all of PositveSamples to the output file 
        PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(postiveOutputFile)));
        out.println("AuthorID, CoAuthorID, rssDoublePlusValue");
        out.flush();
        for (int authorID : rssDoublePlus_PostiveSamples_HM.keySet()) {
            for (int anotherAuthorID : rssDoublePlus_PostiveSamples_HM.get(authorID).keySet()) {
                float rssDoublePlusValue = rssDoublePlus_PostiveSamples_HM.get(authorID).get(anotherAuthorID);
                out.println("(" + authorID + "," + anotherAuthorID + "," + rssDoublePlusValue + ")");
                out.flush();
            }
        }
        out.close();
        
        // Step 6: Storing values which describe CoAuthorStrength (RSSSoublePlus) for all of NegativeSamples to the output file 
        out = new PrintWriter(new BufferedWriter(new FileWriter(negativeOutputFile)));
        out.println("AuthorID, None-CoAuthorID, rssDoublePlusValue");
        out.flush();
        for (int authorID : rssDoublePlus_NegativeSamples_HM.keySet()) {
            for (int anotherAuthorID : rssDoublePlus_NegativeSamples_HM.get(authorID).keySet()) {
                float rssDoublePlusValue = rssDoublePlus_NegativeSamples_HM.get(authorID).get(anotherAuthorID);
                out.println("(" + authorID + "," + anotherAuthorID + "," + rssDoublePlusValue + ")");
                out.flush();
            }
        }
        out.close();
    }

    public static void main(String args[]) {
        try {
            CoAuthorStrengthComputation obj = new CoAuthorStrengthComputation(
                    "/1.CRS-ExperimetalData/SampleData/PositiveSamples.txt",
                    "/1.CRS-ExperimetalData/SampleData/NegativeSamples.txt",
                    "/1.CRS-ExperimetalData/SampleData/AuthorID_PaperID_2003_2005.txt",
                    "/1.CRS-ExperimetalData/SampleData/PaperID_Year_2003_2005.txt", 2003, 2005);
            obj.computeFeatureValues(
                    "/1.CRS-ExperimetalData/SampleData/PositiveSampleWithCoAuthorRSS.txt",
                    "/1.CRS-ExperimetalData/SampleData/NegativeSampleWithCoAuthorRSS.txt");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
