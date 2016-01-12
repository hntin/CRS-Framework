package uit.tkorg.crs.experiment;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import uit.tkorg.crs.isolatedauthor.IsolatedAuthorDataset;
import uit.tkorg.crs.model.CoAuthorGraph;
import uit.tkorg.crs.utility.HashMapUtility;
import uit.tkorg.crs.utility.HashMapUtility;
import uit.tkorg.crs.utility.TextFileUtility;
import uit.tkorg.crs.utility.TextFileUtility;

/**
 *
 * @author TinHuynh
 */
public class CollaborativeQualityEvaluation {

    public HashMap<Integer, Float> decisionValHM = new HashMap<>(); // <InstanceID, DecisionValue>
    public HashMap<Integer, Integer[]> instanceID_Pair_HM = new HashMap<>();
    public CoAuthorGraph _graph;
    
    public CollaborativeQualityEvaluation() {
    }
    
    public CollaborativeQualityEvaluation(String file_AuthorID_PaperID, String file_PaperID_Year, int startYear, int endYear) {
        _graph = new CoAuthorGraph(file_AuthorID_PaperID, file_PaperID_Year, startYear, endYear);
    }

    /**
     * return: decisionValHM
     *
     * @param fileName
     */
    public void load_Decsion_Value(String fileName) {
        try {
            FileInputStream fis = new FileInputStream(fileName);
            Reader reader = new InputStreamReader(fis, "UTF8");
            BufferedReader bufferReader = new BufferedReader(reader);
            bufferReader.readLine(); // skip line 'Recall'
            bufferReader.readLine(); // skip line 'Precision'
            bufferReader.readLine(); // skip line 'F'
            bufferReader.readLine(); // skip line 'AP'
            String line = null;
            float decisionValue = 0.f;
            int instanceID = 0;
            while ((line = bufferReader.readLine()) != null) {
                if (!line.equals("")) {
                    decisionValue = Float.parseFloat(line);
                }
                decisionValHM.put(instanceID, decisionValue);
                instanceID++;
            }
            bufferReader.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * return instanceID_Pair_HM <InstanceID, <IsolatedID, CoAuthorID>> @para m
     *
     *
     *
     * fileName
     */
    public void load_InstanceID_Pair(String fileName) {
        try {
            FileInputStream fis = new FileInputStream(fileName);
            Reader reader = new InputStreamReader(fis, "UTF8");
            BufferedReader bufferReader = new BufferedReader(reader);
            bufferReader.readLine();
            String line = null;
            String[] tokens;
            int instanceID;
            int isolatedAuthorID;
            int coAuthorID;
            while ((line = bufferReader.readLine()) != null) {
                if (!line.equals("")) {
                    tokens = line.split("\t");
                    instanceID = Integer.parseInt(tokens[0]);
                    isolatedAuthorID = Integer.parseInt(tokens[1]);
                    coAuthorID = Integer.parseInt(tokens[2]);

                    Integer[] pair = new Integer[2];
                    pair[0] = isolatedAuthorID;
                    pair[1] = coAuthorID;
                    instanceID_Pair_HM.put(instanceID, pair);
                }
            }
            bufferReader.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public HashMap<Integer, Float> getTopNOfDecisionValue(int topN) {
        HashMap<Integer, Float> decisionValTopN = new HashMap<>();
        HashMap<Integer, Float> decisionValSortedHM = null;
        decisionValSortedHM = HashMapUtility.getSortedMapDescending(decisionValHM);

        Set<Integer> keySet = decisionValSortedHM.keySet();
        Iterator<Integer> keySetIter = keySet.iterator();

        int count = 0;
        // < 631 means that that pair/instance is a true pair (linked pair with the label +1)
        while (keySetIter.hasNext() && count < topN) {
            int instanceID = keySetIter.next();
            if (instanceID < 631) {
                decisionValTopN.put(instanceID, decisionValSortedHM.get(instanceID));
                count++;
            }
        }

        return decisionValTopN;
    }

    /**
     * Getting goodness of coauthor-ship based on number of collaborations
     *
     * @param topN
     * @return: goodness metric 1
     */
    public double getCoAuthorshipGoodness_Metric1(HashMap<Integer, Float> decisionValTopN, String outFileName) {
        double goodnessVal = 0.f;
        Set<Integer> keySet = decisionValTopN.keySet();

        StringBuffer strBuff = new StringBuffer();
        strBuff.append("TopN \t DecisionValue \t NumberOfCollaboration \n");
        int topIdx = 1;

        for (int key : keySet) {
            Integer[] pairAuthorID = instanceID_Pair_HM.get(key);
            int isolatedID = pairAuthorID[0];
            int coAuthorID = pairAuthorID[1];
            int numberOfCollaboration = 0;
            if (IsolatedAuthorDataset._coAuthorNF.containsKey(isolatedID)) {
                if (IsolatedAuthorDataset._coAuthorNF.get(isolatedID).containsKey(coAuthorID)) {
                    numberOfCollaboration += IsolatedAuthorDataset._coAuthorNF.get(isolatedID).get(coAuthorID);
                    //goodnessVal += ((double)decisionValTopN.get(key)) * ((double)Math.exp(numberOfCollaboration));
                }
            }

            if (IsolatedAuthorDataset._coAuthorFF.containsKey(isolatedID)) {
                if (IsolatedAuthorDataset._coAuthorFF.get(isolatedID).containsKey(coAuthorID)) {
                    numberOfCollaboration += IsolatedAuthorDataset._coAuthorFF.get(isolatedID).get(coAuthorID);
                }
            }

            if (decisionValTopN.get(key) >= 0) {
                goodnessVal += (double) decisionValTopN.get(key) * numberOfCollaboration;
                //goodnessVal += ((double)decisionValTopN.get(key)) * ((double)Math.exp(numberOfCollaboration));
            }
            strBuff.append(topIdx + "\t" + decisionValTopN.get(key) + "\t" + numberOfCollaboration + "\n");
            topIdx++;
        }

        strBuff.append(goodnessVal);
        TextFileUtility.writeTextFile(outFileName, strBuff.toString());
        return goodnessVal;
    }

    /**
     * 
     * @param authorID
     * @param potentialCoAuthorList
     * @param file_AuthorID_PaperID
     * @param file_PaperID_Year
     * @param startYear
     * @param endYear
     * @return 
     */
    public double calculate_CollaborativeQualityTopN_Metric1(String authorID, List<String> potentialCoAuthorList_TopN, 
            String file_AuthorID_PaperID, String file_PaperID_Year, int startYear, int endYear) {
        
        double collaborativeQualityTopN = 0;        
        // i la vi tri xep hang cua potentialCoAuthorID trong danh sach?
        for (int i=0; i<potentialCoAuthorList_TopN.size(); i++) {
            String potentialCoAuthorID = potentialCoAuthorList_TopN.get(i);          
            // Kiem tra su ton tai cua authorID va potentialCoAuthorID trong T3
            // Dem so bai dong tac gia cua (authorID, potentialCoAuthorID) trong T3. 
            // Tinh chat luong cong tac dua tren so bai dong tac gia cua (authorID, potentialCoAuthorID)
            // Cong don vao ket qua Quality_Top_N(r(i))
            if (_graph._coAuthorGraph.containsKey(authorID) && _graph._coAuthorGraph.get(authorID).containsKey(potentialCoAuthorID)) {
                int numOfCollaboration = _graph._coAuthorGraph.get(authorID).get(potentialCoAuthorID);
                collaborativeQualityTopN += numOfCollaboration*1/(Math.exp(i));
            }
        }
        
        // Average value
        collaborativeQualityTopN = collaborativeQualityTopN/potentialCoAuthorList_TopN.size();        
        return collaborativeQualityTopN;        
    }

    public static void main(String args[]) {
        int topN = 50;
        System.out.println("START");
        IsolatedAuthorDataset isolatedDataset = new IsolatedAuthorDataset(
                "C:\\CRS-Experiment\\MAS\\ColdStart\\Input\\Input1\\[TrainingData]AuthorID_PaperID_2001_2005.txt",
                "C:\\CRS-Experiment\\MAS\\ColdStart\\Input\\Input1\\[TestingData]AuthorID_PaperID_2006_2008.txt",
                "C:\\CRS-Experiment\\MAS\\ColdStart\\Input\\Input1\\[TestingData]AuthorID_PaperID_2009_2011.txt");

        System.out.println("Loading & Building Networks");
        isolatedDataset.load_Training_NetworkData();
        isolatedDataset.load_NF_FF_NetworkData();
        isolatedDataset.build_NF_FF_Graph();
        isolatedDataset.build_CoAuthorGraph();

        CollaborativeQualityEvaluation goodnessEvaluation = new CollaborativeQualityEvaluation();
        goodnessEvaluation.load_Decsion_Value("C:\\CRS-Experiment\\MAS\\ColdStart\\Output\\TestDataset_2Features_OrgRS_ActiveScore_results.txt");

        goodnessEvaluation.load_InstanceID_Pair("C:\\CRS-Experiment\\MAS\\ColdStart\\Input\\Input1\\TestDatasetMapping.txt");
        HashMap<Integer, Float> topN_HM = goodnessEvaluation.getTopNOfDecisionValue(topN);
        topN_HM = HashMapUtility.getSortedMapDescending(topN_HM);
        double goodnessValueMetric1 = goodnessEvaluation.getCoAuthorshipGoodness_Metric1(
                topN_HM, "C:\\CRS-Experiment\\MAS\\ColdStart\\Output\\GoodnessResult_2Features_OrgRS_ActiveScore_Top50.txt");

        System.out.println("Goodness Value for top" + topN + " is: " + goodnessValueMetric1);
        System.out.println("END");
    }
}

/**
 * Input: List of researchers, TopN
 * Output: Quality of Recommendation for inputted researchers = Sum(Quality_TopN(r(i)))/Total_Input_Researchers
 * Step 1: ForAll r(i)
 * Step 2: Begin
 *          Doc file Output cua Model?, lay TopN nguoi cong tac voi r(i) co xac xuat cao nhat
 *          (Xay dung lai file Output cua Model: (AuthorID, CoAuthorID, Prob, True/False))
 *          Tinh Quality_TopN(r(i), TopN_Potential_List)
 * Step 3: End
 * Step 4: Tinh Average_Quality_TopN cho tat ca cac input researchers = Sum(Quality_TopN(r(i)))/Total_Input_Researchers
 * Step 5: Output ra file?
 */

/**
 * Tinh Metric 1: Quality_Top_N(r(i))
 * Input: r(i), TopN_Potential_List
 * Output: Gia tri Quality_Top_N(r(i))
 * Step 1: Build G3 tuong ung voi T3
 * Step 2: ForAll Coauthor(j) in TopN_Potential_List
 * Step 3:  Kiem tra su ton tai cua r(i) va CoAuthor(j) trong T3
 *          Dem so bai bao dong tac gia r(i) va CoAuthor(j) in T3.
 * Step 4:  Tinh theo cong thuc Quality dua tren vi tri cua j trong TopN.
 *          Cong don vao ket qua Quality_Top_N(r(i))
 * Step 5: End 
 * Step 6: Return ket qua
 */