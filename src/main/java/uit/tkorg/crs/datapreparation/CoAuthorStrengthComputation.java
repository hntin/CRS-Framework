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
import uit.tkorg.crs.common.Pair;
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

        // Step 2: Calculating RSSDoublePlus values for all juniorAuthorID with all others in the CoAuthorGraph
        RSS methodRSS = new RSS();
        HashMap<Integer, HashMap<Integer, Float>> rssDoublePlus_FirstAuthorID_ToAllOthers = methodRSS.process(
                _coAuthorGraph._rssDoublePlusGraph, firstAuthorIDList);
 
        // Step 3: Extracting RSSDoublePlus Values for all pairs of PositveSamples, rssDoublePlus_PostiveSamples_HM
        HashMap<Integer, HashMap<Integer, Float>> rssDoublePlus_Samples_HM = new HashMap<>();
        for (Pair p : pairs) {
            int firstAuthorID = p.getFirst();
            int secondAuthorID = p.getSecond();

            float rssDoublePlusValue = 0;
            if (rssDoublePlus_FirstAuthorID_ToAllOthers.containsKey(firstAuthorID)
                    && rssDoublePlus_FirstAuthorID_ToAllOthers.get(firstAuthorID).containsKey(secondAuthorID)) {
                rssDoublePlusValue = rssDoublePlus_FirstAuthorID_ToAllOthers.get(firstAuthorID).get(secondAuthorID);
            }

            HashMap<Integer, Float> hm = rssDoublePlus_Samples_HM.get(firstAuthorID);
            if (hm == null || hm.isEmpty()) {
                hm = new HashMap<>();
            }

            hm.put(secondAuthorID, rssDoublePlusValue);
            rssDoublePlus_Samples_HM.put(firstAuthorID, hm);
        }
        
        // Step 4: Storing values which describe CoAuthorStrength (RSSDoublePlus) for all of PositiveSamples/NegativeSamples to the output file
        try ( 
            PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(outputFile)))) {
            out.println("AuthorID, CoAuthorID, rssDoublePlusValue");
            out.flush();
            for (int authorID : rssDoublePlus_Samples_HM.keySet()) {
                for (int anotherAuthorID : rssDoublePlus_Samples_HM.get(authorID).keySet()) {
                    float rssDoublePlusValue = rssDoublePlus_Samples_HM.get(authorID).get(anotherAuthorID);
                    out.println("(" + authorID + "," + anotherAuthorID + ")\t" + rssDoublePlusValue );
                    out.flush();
                }
            }
        }
    }

    public static void main(String args[]) {
        try {
            CoAuthorStrengthComputation obj = new CoAuthorStrengthComputation(
                    "/1.CRS-ExperimetalData/SampleData/PositiveSamples.txt",
                    "/1.CRS-ExperimetalData/SampleData/NegativeSamples.txt",
                    "/1.CRS-ExperimetalData/SampleData/AuthorID_PaperID_2003_2005.txt",
                    "/1.CRS-ExperimetalData/SampleData/PaperID_Year_2003_2005.txt", 2003, 2005);
            obj.computeFeatureValues("/1.CRS-ExperimetalData/SampleData/PositiveSampleCoAuthorRSS.txt", 1);
            obj.computeFeatureValues("/1.CRS-ExperimetalData/SampleData/NegativeSampleCoAuthorRSS.txt", 0);
        } catch (Exception ex) {
        }
    }

    
}
