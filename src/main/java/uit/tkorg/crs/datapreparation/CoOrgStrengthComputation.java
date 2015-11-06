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
        }
        else {
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

        // Step 3: Calculating OrgRSS values for input OrgIDList for others OrgID in the OrganizationGraph by using RSS
        RSS rssMethod = new RSS();
        HashMap<Integer, HashMap<Integer, Float>> OrgRSSResult = rssMethod.process(_orgGraph._rssOrgGraph, orgIDList);
        
        // Step 4: Getting RSS value between two orgs associated with pairs (+) or (-) and write to text file
        try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(outputFile)))) {
            out.println("AuthorID, Another-AuthorID, orgRSSValue");
            out.flush();
            for (Pair p : pairs) {
                int firstAuthorID = p.getFirst();
                int secondAuthorID = p.getSecond();
                
                int orgID_Of_FirstAuthorID = _orgGraph._authorID_OrgID.get(firstAuthorID);
                int orgID_Of_SecondAuthorID = _orgGraph._authorID_OrgID.get(secondAuthorID);
                float orgRSSValue = OrgRSSResult.get(orgID_Of_FirstAuthorID).get(orgID_Of_SecondAuthorID);
                out.println("(" + firstAuthorID + "," + secondAuthorID + ")\t" + orgRSSValue );
                out.flush();
            }
        }
    }

    public static void main(String args[]) {
        try {
            CoOrgStrengthComputation obj = new CoOrgStrengthComputation(
                    "/1.CRS-ExperimetalData/SampleData/PositiveSamples.txt", 
                    "/1.CRS-ExperimetalData/SampleData/NegativeSamples.txt", 
                    "/1.CRS-ExperimetalData/SampleData/AuthorID_PaperID_OrgID_2003_2005.txt", 
                    2003, 2005);
            obj.computeFeatureValues("/1.CRS-ExperimetalData/SampleData/PositiveSampleOrgRSS.txt", 1);
            obj.computeFeatureValues("/1.CRS-ExperimetalData/SampleData/NegativeSampleOrgRSS.txt", 0);
        }
        catch (Exception ex) {
        }
    }
}
