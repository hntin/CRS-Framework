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
    public void computeFeatureValues(String postiveOutputFile, String negativeOutputFile) {
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

            float rssSoublePlusValue = -1;
            if (rssDoublePlus_FirstAuthorID_PostiveSample_ToAllOthers.containsKey(firstAuthorID)
                    && rssDoublePlus_FirstAuthorID_PostiveSample_ToAllOthers.get(firstAuthorID).containsKey(secondAuthorID)) {
                rssSoublePlusValue = rssDoublePlus_FirstAuthorID_PostiveSample_ToAllOthers.get(firstAuthorID).get(secondAuthorID);
            }

            HashMap<Integer, Float> hm = rssDoublePlus_PostiveSamples_HM.get(firstAuthorID);
            if (hm == null || hm.isEmpty()) {
                hm = new HashMap<>();
            }

            hm.put(secondAuthorID, rssSoublePlusValue);
            rssDoublePlus_PostiveSamples_HM.put(firstAuthorID, hm);
        }
        
        // Step 4: Extracting RSSSoublePlus Values for all pairs of NegativeSamples
        HashMap<Integer, HashMap<Integer, Float>> rssDoublePlus_NegativeSamples_HM = new HashMap<>();
        

        // Step 5: Storing values which describe CoAuthorStrength (RSSSoublePlus) for all of PositveSamples and NegativeSamples 
        // to the output file 
        TextFileUtility.writeTextFileFromHM(postiveOutputFile, rssDoublePlus_PostiveSamples_HM);
        TextFileUtility.writeTextFileFromHM(negativeOutputFile, rssDoublePlus_NegativeSamples_HM);
    }

    public static void main(String args[]) {
        CoAuthorStrengthComputation obj = new CoAuthorStrengthComputation(
                "/1.CRS-ExperimetalData/SampleData/PositiveSamples.txt",
                "/1.CRS-ExperimetalData/SampleData/NegativeSamples.txt",
                "/1.CRS-ExperimetalData/SampleData/AuthorID_PaperID_2001_2003.txt", 
                "/1.CRS-ExperimetalData/SampleData/PaperID_Year_2001_2003.txt", 2001, 2003);
        obj.computeFeatureValues(
                "/1.CRS-ExperimetalData/SampleData/PositiveSampleWithCoAuthorRSS.txt", 
                "/1.CRS-ExperimetalData/SampleData/NegativeSampleWithCoAuthorRSS.txt");
    }

}
