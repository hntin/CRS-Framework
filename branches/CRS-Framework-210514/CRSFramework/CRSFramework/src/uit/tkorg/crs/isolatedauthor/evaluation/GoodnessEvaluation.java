package uit.tkorg.crs.isolatedauthor.evaluation;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Set;
import uit.tkorg.crs.isolatedauthor.IsolatedAuthorDataset;
import uit.tkorg.utility.HashMapUtility;
import uit.tkorg.utility.TextFileUtility;

/**
 *
 * @author TinHuynh
 */
public class GoodnessEvaluation {

    public HashMap<Integer, Float> decisionValHM = new HashMap<>(); // <InstanceID, DecisionValue>
    public HashMap<Integer, Integer[]> instanceID_Pair_HM = new HashMap<>();

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
     * Getting goodness of coauthor-ship based on period of collaboration
     *
     * @param topN
     * @return: goodness metric 2
     */
    public float getCoAuthorshipGoodness_Metric2(int topN) {
        float goodnessVal = 0.f;

        return goodnessVal;
    }

    /**
     * Getting goodness of coauthor-ship based on ability to extend new
     * coauthor-ship
     *
     * @param topN
     * @return: goodness metric 2
     */
    public float getCoAuthorshipGoodness_Metric3(int topN) {
        float goodnessVal = 0.f;

        return goodnessVal;
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

        GoodnessEvaluation goodnessEvaluation = new GoodnessEvaluation();
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
