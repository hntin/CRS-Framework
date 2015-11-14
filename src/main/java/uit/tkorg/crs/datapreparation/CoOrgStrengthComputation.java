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
import uit.tkorg.crs.model.Pair;
import uit.tkorg.crs.method.link.RSS;
import uit.tkorg.crs.model.OrganizationGraph;
import uit.tkorg.crs.model.Sample;

/**
 * Tinh CoOrgStrength (Org_RSS+) cho tung cap tac gia trong mau am (-) va mau
 * duong (+)
 *
 * @author thucnt
 */
public class CoOrgStrengthComputation extends FeatureComputation {

    private final OrganizationGraph _orgGraph;

    public CoOrgStrengthComputation(String postiveSampleFile, String negativeSampleFile,
            String inputFile_AuthorID_paperID_OrgID_Ti, int firstYear, int lastYear) {

        this._positiveSample = Sample.readSampleFile(postiveSampleFile);
        this._negativeSample = Sample.readSampleFile(negativeSampleFile);
        // Building the Collaborative Organization Graph 
        _orgGraph = new OrganizationGraph(inputFile_AuthorID_paperID_OrgID_Ti, firstYear, lastYear);
    }

    @Override
    public void computeFeatureValues(String outputFile, int typeOfSample) throws IOException {
        // Step 1: Getting all distinct juniorAuthorID (firstAuthorID in a pair)
        HashMap<Integer, String> firstAuthorIDList;
        ArrayList<Pair> pairs = null;

        if (typeOfSample == 1) {
            firstAuthorIDList = this._positiveSample.readAllFirstAuthorID();
            pairs = this._positiveSample.getPairOfAuthor();
        } else {
            firstAuthorIDList = this._negativeSample.readAllFirstAuthorID();
            pairs = this._negativeSample.getPairOfAuthor();
        }

        // Step 2: Getting all distinct orgID associated with firstAuthorID
        HashMap<Integer, String> orgIDList = new HashMap<>();
        for (int firstAuthorID : firstAuthorIDList.keySet()) {
            int orgID = _orgGraph._authorID_OrgID.get(firstAuthorID);
            if (!orgIDList.containsKey(orgID)) {
                orgIDList.put(orgID, "Org");
            }
        }

        // Step 3: Calculating OrgRSS values for input OrgIDList to other OrgID in 3-hub in the OrganizationGraph by using RSS
        RSS rssMethod = new RSS();
        HashMap<Integer, HashMap<Integer, Float>> OrgRSSResult = rssMethod.process(_orgGraph._rssOrgGraph, orgIDList);

        // Step 4: Getting RSS value between two orgs associated with pairs (+) or (-) and write to text file
        try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(outputFile)))) {
            out.println("AuthorID, Another-AuthorID, orgRSSValue");
            out.flush();
            for (Pair p : pairs) {
                int firstAuthorID = p.getFirst();
                int secondAuthorID = p.getSecond();

                int orgID_Of_FirstAuthorID = 0;
                int orgID_Of_SecondAuthorID = 0;
                if (_orgGraph._authorID_OrgID.containsKey(firstAuthorID))
                    orgID_Of_FirstAuthorID = _orgGraph._authorID_OrgID.get(firstAuthorID);               
                if (_orgGraph._authorID_OrgID.containsKey(secondAuthorID))
                    orgID_Of_SecondAuthorID = _orgGraph._authorID_OrgID.get(secondAuthorID) ;
                
                float orgRSSValue = 0;
                if ((orgID_Of_FirstAuthorID != 0) && (orgID_Of_SecondAuthorID != 0) 
                        && (orgID_Of_FirstAuthorID == orgID_Of_SecondAuthorID)) {
                    orgRSSValue = 1;
                } else {
                    if (OrgRSSResult.containsKey(orgID_Of_FirstAuthorID) && 
                            OrgRSSResult.get(orgID_Of_FirstAuthorID).containsKey(orgID_Of_SecondAuthorID)) {
                        orgRSSValue = OrgRSSResult.get(orgID_Of_FirstAuthorID).get(orgID_Of_SecondAuthorID);
                    } else {
                        if (_orgGraph._rssOrgGraph.containsKey(orgID_Of_FirstAuthorID) && 
                                _orgGraph._rssOrgGraph.get(orgID_Of_FirstAuthorID).containsKey(orgID_Of_SecondAuthorID)) {
                            orgRSSValue = _orgGraph._rssOrgGraph.get(orgID_Of_FirstAuthorID).get(orgID_Of_SecondAuthorID);
                        } else {
                            orgRSSValue = 0;
                        }
                    }
                }
                out.println("(" + firstAuthorID + "," + secondAuthorID + ")\t" + orgRSSValue);
                out.flush();
            }
        }
    }

    public static void main(String args[]) throws Exception {
            CoOrgStrengthComputation obj;
            // For Training samples
            obj = new CoOrgStrengthComputation(
                    "/2.CRS-ExperimetalData/SampleData/Training_PositiveSamples.txt",
                    "/2.CRS-ExperimetalData/SampleData/Training_NegativeSamples.txt",
                    "/2.CRS-ExperimetalData/SampleData/AuthorID_PaperID_OrgID_Before_Include_2005.txt",
                    0, 2005);
            obj.computeFeatureValues("/2.CRS-ExperimetalData/SampleData/Training_PositiveSampleOrgRSS.txt", 1);
            obj.computeFeatureValues("/2.CRS-ExperimetalData/SampleData/Training_NegativeSampleOrgRSS.txt", 0);
            
            // For testing samples
            obj = new CoOrgStrengthComputation(
                    "/2.CRS-ExperimetalData/SampleData/Testing_PositiveSamples.txt",
                    "/2.CRS-ExperimetalData/SampleData/Testing_NegativeSamples.txt",
                    "/2.CRS-ExperimetalData/SampleData/AuthorID_PaperID_OrgID_Before_Include_2008.txt",
                    0, 2008);
            obj.computeFeatureValues("/2.CRS-ExperimetalData/SampleData/Testing_PositiveSampleOrgRSS.txt", 1);
            obj.computeFeatureValues("/2.CRS-ExperimetalData/SampleData/Testing_NegativeSampleOrgRSS.txt", 0);
        
        
//            CoOrgStrengthComputation obj;
//            // For Training samples
//            obj = new CoOrgStrengthComputation(
//                    "/2.CRS-ExperimetalData/TrainingData/Training_PositiveSamples.txt",
//                    "/2.CRS-ExperimetalData/TrainingData/Training_NegativeSamples.txt",
//                    "/2.CRS-ExperimetalData/TrainingData/AuthorID_PaperID_OrgID_Before_Include_2003.txt",
//                    0, 2003);
//            obj.computeFeatureValues("/2.CRS-ExperimetalData/TrainingData/Training_PositiveSampleOrgRSS.txt", 1);
//            obj.computeFeatureValues("/2.CRS-ExperimetalData/TrainingData/Training_NegativeSampleOrgRSS.txt", 0);
//            
//            // For testing samples
//            obj = new CoOrgStrengthComputation(
//                    "/2.CRS-ExperimetalData/TrainingData/Testing_PositiveSamples.txt",
//                    "/2.CRS-ExperimetalData/TrainingData/Testing_NegativeSamples.txt",
//                    "/2.CRS-ExperimetalData/TrainingData/AuthorID_PaperID_OrgID_Before_Include_2006.txt",
//                    0, 2006);
//            obj.computeFeatureValues("/2.CRS-ExperimetalData/TrainingData/Testing_PositiveSampleOrgRSS.txt", 1);
//            obj.computeFeatureValues("/2.CRS-ExperimetalData/TrainingData/Testing_NegativeSampleOrgRSS.txt", 0);
        
//            CoOrgStrengthComputation obj;
//            obj = new CoOrgStrengthComputation(
//                    "D:\\1.CRS-Experiment\\MLData\\TrainingData\\Training_PositiveSamples.txt",
//                    "D:\\1.CRS-Experiment\\MLData\\TrainingData\\Training_NegativeSamples.txt",
//                    "D:\\1.CRS-Experiment\\MLData\\TrainingData\\AuthorID_PaperID_OrgID_Before_Include_2003.txt",
//                    0, 2003);
//            obj.computeFeatureValues("D:\\1.CRS-Experiment\\MLData\\TrainingData\\Training_PositiveSampleOrgRSS.txt", 1);
//            obj.computeFeatureValues("D:\\1.CRS-Experiment\\MLData\\TrainingData\\Training_NegativeSampleOrgRSS.txt", 0);
//            
//            obj = new CoOrgStrengthComputation(
//                    "D:\\1.CRS-Experiment\\MLData\\TrainingData\\Testing_PositiveSamples.txt",
//                    "D:\\1.CRS-Experiment\\MLData\\TrainingData\\Testing_NegativeSamples.txt",
//                    "D:\\1.CRS-Experiment\\MLData\\TrainingData\\AuthorID_PaperID_OrgID_Before_Include_2006.txt",
//                    0, 2006);
//            obj.computeFeatureValues("D:\\1.CRS-Experiment\\MLData\\TrainingData\\Testing_PositiveSampleOrgRSS.txt", 1);
//            obj.computeFeatureValues("D:\\1.CRS-Experiment\\MLData\\TrainingData\\Testing_NegativeSampleOrgRSS.txt", 0);
            
            System.out.println("DONE DONE DONE");
        
    }
}
