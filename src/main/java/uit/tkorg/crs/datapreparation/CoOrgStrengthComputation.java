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
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import uit.tkorg.crs.common.Pair;
import uit.tkorg.crs.method.link.RSS;
import uit.tkorg.crs.model.CoAuthorGraph;
import uit.tkorg.crs.model.OrganizationGraph;
import uit.tkorg.crs.model.Sample;

/**
 * Tinh CoOrgStrength (Org_RSS+) cho tung cap tac gia trong mau am (-) va mau
 * duong (+)
 *
 * @author thucnt
 */
public class CoOrgStrengthComputation extends FeatureComputation {
    private OrganizationGraph _orgGraph;
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
        PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(outputFile)));
        out.println("AuthorID, Another-AuthorID, orgRSSValue");
        out.flush();
        for (int i = 0; i < pairs.size(); i++) {
            Pair p = pairs.get(i);
            int firstAuthorID = p.getFirst();
            int secondAuthorID = p.getSecond();
            
            int orgID_Of_FirstAuthorID = _orgGraph._authorID_OrgID.get(firstAuthorID);
            int orgID_Of_SecondAuthorID = _orgGraph._authorID_OrgID.get(secondAuthorID);
            float orgRSSValue = OrgRSSResult.get(orgID_Of_FirstAuthorID).get(orgID_Of_SecondAuthorID);
            out.println("(" + firstAuthorID + "," + secondAuthorID + ")\t" + orgRSSValue );
            out.flush();
        }
        
        out.close();
    }

    public static void main(String args[]) {
        
    }
}
