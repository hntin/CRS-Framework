package uit.tkorg.crs.experiment;

//<editor-fold defaultstate="collapsed" desc="Import Lib">
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import uit.tkorg.crs.isolatedauthor.IsolatedAuthorDataset;
import uit.tkorg.crs.model.CoAuthorGraph;
import uit.tkorg.crs.utility.HashMapUtility;
import uit.tkorg.crs.utility.HashMapUtility;
import uit.tkorg.crs.utility.TextFileUtility;
import uit.tkorg.crs.utility.TextFileUtility;
//</editor-fold>

/**
 *
 * @author TinHuynh
 */
public class CollaborativeQualityEvaluation {

    public HashMap<Integer, Float> decisionValHM = new HashMap<>(); // <InstanceID, DecisionValue>
    public HashMap<Integer, Integer[]> instanceID_Pair_HM = new HashMap<>();

    /**
     * load_Decsion_Value
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
     * load_InstanceID_Pair
     *
     * @param fileName
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
     * collaborativeQualityTopN_Metric1
     *
     * @param authorID
     * @param potentialCoAuthorList_TopN
     * @param graph
     * @return
     */
    public static double collaborativeQualityTopN_Metric1(int authorID, List<Integer> potentialCoAuthorList_TopN, CoAuthorGraph graph) {

        double collaborativeQualityTopN = 0;
        // i la vi tri xep hang cua potentialCoAuthorID trong danh sach?
        for (int i = 0; i < potentialCoAuthorList_TopN.size(); i++) {
            Integer potentialCoAuthorID = potentialCoAuthorList_TopN.get(i);
            // Kiem tra su ton tai cua authorID va potentialCoAuthorID trong T3
            // Dem so bai dong tac gia cua (authorID, potentialCoAuthorID) trong T3. 
            // Tinh chat luong cong tac dua tren so bai dong tac gia cua (authorID, potentialCoAuthorID)
            // Cong don vao ket qua Quality_Top_N(r(i))
            if (graph._coAuthorGraph.containsKey(authorID)) {
                if ((graph._coAuthorGraph.get(authorID)).containsKey(potentialCoAuthorID)) {
                    int numOfCollaboration = graph._coAuthorGraph.get(authorID).get(potentialCoAuthorID);
                    collaborativeQualityTopN += numOfCollaboration * 1 / (Math.exp(i));
                }

            }
        }

        return collaborativeQualityTopN;
    }

    /**
     * collaborativeQualityTopN_Metric2
     *
     * @param authorID
     * @param potentialCoAuthorList_TopN
     * @param pastGraph
     * @param currentGraph
     * @return
     */
    public static double collaborativeQualityTopN_Metric2(int authorID, List<Integer> potentialCoAuthorList_TopN, CoAuthorGraph pastGraph, CoAuthorGraph currentGraph) {
        double collaborativeQualityTopN = 0;
        for (int i = 0; i < potentialCoAuthorList_TopN.size(); i++) {
            int potentialAuthorID = potentialCoAuthorList_TopN.get(i);
            ArrayList<Integer> newCollaborators = getNewCollaborations(authorID, potentialAuthorID, pastGraph, currentGraph);
            if (newCollaborators != null && newCollaborators.size() > 0) {
                collaborativeQualityTopN += newCollaborators.size() * 1 / Math.exp(i);
            }
        }

        //collaborativeQualityTopN = collaborativeQualityTopN / potentialCoAuthorList_TopN.size();
        return collaborativeQualityTopN;
    }

    /**
     * getNewCollaborations
     *
     * @param authorID
     * @param potentialCoAuthorID
     * @param pastGraph: T2?
     * @param currentGraph: T3?
     * @return
     */
    public static ArrayList<Integer> getNewCollaborations(int authorID, int potentialCoAuthorID, CoAuthorGraph pastGraph, CoAuthorGraph currentGraph) {
        ArrayList<Integer> listNewCoAuthorID = new ArrayList<>();
        for (int paperID : currentGraph._paperAuthor.keySet()) {
            ArrayList<Integer> authorListOfPaper = currentGraph._paperAuthor.get(paperID);
            if (authorListOfPaper.contains(authorID) && authorListOfPaper.contains(potentialCoAuthorID) && authorListOfPaper.size() > 2) {
                for (int j = 0; j < authorListOfPaper.size(); j++) {
                    int newCoAuthorID = authorListOfPaper.get(j);
                    if (newCoAuthorID != authorID && newCoAuthorID != potentialCoAuthorID) {
                        // Kiem tra xem neu (authorID, newCoAuthorID) chua co dong tac gia trong pastGraph thi them vao danh sach listNewCoAuthorID
                        if (!(pastGraph._coAuthorGraph.get(authorID).containsKey(newCoAuthorID))) {
                            if (!listNewCoAuthorID.contains(newCoAuthorID)) {
                                listNewCoAuthorID.add(newCoAuthorID);
                            }
                        }

                    }
                }
            }
        }
        return listNewCoAuthorID;
    }

    /**
     *
     * @param fileName: Testing_PositiveSamples.txt
     * @return
     */
    public static ArrayList<Integer> getDistinctAuthorIDFromPositiveSamples(String fileName) {
        ArrayList<Integer> authorIDFromPositiveSample = new ArrayList<>();

        try {
            FileInputStream fis = new FileInputStream(fileName);
            Reader reader = new InputStreamReader(fis, "UTF8");
            BufferedReader bufferReader = new BufferedReader(reader);
            bufferReader.readLine(); // skip the first line
            String line = null;
            while ((line = bufferReader.readLine()) != null) {
                String delim = "(,)"; //insert here all delimitators
                StringTokenizer st = new StringTokenizer(line, delim);
                Integer authorID = new Integer(st.nextToken());
                if (!authorIDFromPositiveSample.contains(authorID)) {
                    authorIDFromPositiveSample.add(authorID);
                }
            }
            bufferReader.close();
            fis.close();
        } catch (Exception e) {
            System.out.println(e);
        }

        return authorIDFromPositiveSample;
    }

    public static HashMap<Integer, HashMap<Integer, Double>> readEvaluationFile(String dataFile, String type) {
        HashMap<Integer, HashMap<Integer, Double>> list = new HashMap();
        try {
            Scanner input = new Scanner(new FileInputStream(dataFile));
            while (input.hasNext()) {
                String line = input.nextLine();
                String[] temp = line.split(",");
                if (type.equalsIgnoreCase(temp[2])) {
                    String[] ids = temp[0].split("_");
                    Integer key = Integer.parseInt(ids[0]);
                    if (list.containsKey(key)) {
                        HashMap<Integer, Double> h = list.get(key);
                        h.put(new Integer(ids[1]), new Double(temp[1]));
                        list.replace(key, h);
                    } else {
                        HashMap<Integer, Double> h = new HashMap();
                        h.put(new Integer(ids[1]), new Double(temp[1]));
                        list.put(key, h);
                    }
                }
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(CollaborativeQualityEvaluation.class.getName()).log(Level.SEVERE, null, ex);
        }
        return list;
    }

    public static List<Integer> getTopN(int idAuthor, int n, HashMap<Integer, HashMap<Integer, Double>> hash) {
        HashMap<Integer, Double> h = hash.get(new Integer(idAuthor));
        h = HashMapUtility.getSortedMapDescending(h);
        HashMap<Integer, Double> ret = null;
        if (n > h.size()) {
            ret = h;
        } else {
            ret = new HashMap();
            Set<Integer> key = h.keySet();
            int count = 0;
            for (Integer k : key) {
                ret.put(k, h.get(k));
                count++;
                if (count == n) {
                    break;
                }
            }
        }
        //convert HashtMap to ArrayList
        Set<Integer> key = ret.keySet();
        List<Integer> list = new ArrayList<Integer>();
        for (Integer k : key)
            list.add(k);
        return list;
    }

    public static double runCollaborativeQualityEvaluation(CoAuthorGraph pastGraph, CoAuthorGraph currentGraph,
            String positiveSampleFileName, String predictedResultFileName,
            int topN, String type, int metric) {

        double collaborativeQualityValue = 0;
        HashMap<Integer, HashMap<Integer, Double>> hash = readEvaluationFile(predictedResultFileName, "Positive");
        ArrayList<Integer> authorIDListFromPositiveSamples = getDistinctAuthorIDFromPositiveSamples(positiveSampleFileName);
        for (int i = 0; i < authorIDListFromPositiveSamples.size(); i++) {
            int inputAuthorID = authorIDListFromPositiveSamples.get(i);
            List<Integer> topN_HM = getTopN(inputAuthorID, topN, hash);
            if (metric == 1) {
                //collaborativeQualityValue += collaborativeQualityTopN_Metric1(inputAuthorID, topN_HM.keySet());
            } 
            if (metric == 2) {
                //collaborativeQualityValue += collaborativeQualityTopN_Metric2(inputAuthorID, topN_HM.keySet(), pastGraph, currentGraph);
            }           
        }

        collaborativeQualityValue = collaborativeQualityValue / authorIDListFromPositiveSamples.size();
        return collaborativeQualityValue;
    }

    public static void main(String args[]) {

        System.out.println("START");
//        int topN = 50;
//
//        //<editor-fold defaultstate="collapsed" desc="For Isolated Researchers">
////        IsolatedAuthorDataset isolatedDataset = new IsolatedAuthorDataset(
////                "C:\\CRS-Experiment\\MAS\\ColdStart\\Input\\Input1\\[TrainingData]AuthorID_PaperID_2001_2005.txt",
////                "C:\\CRS-Experiment\\MAS\\ColdStart\\Input\\Input1\\[TestingData]AuthorID_PaperID_2006_2008.txt",
////                "C:\\CRS-Experiment\\MAS\\ColdStart\\Input\\Input1\\[TestingData]AuthorID_PaperID_2009_2011.txt");
////        
////        System.out.println("Loading & Building Networks");
////        isolatedDataset.load_Training_NetworkData();
////        isolatedDataset.load_NF_FF_NetworkData();
////        isolatedDataset.build_NF_FF_Graph();
////        isolatedDataset.build_CoAuthorGraph();
////        
////        CollaborativeQualityEvaluation goodnessEvaluation = new CollaborativeQualityEvaluation();
////        goodnessEvaluation.load_Decsion_Value("C:\\CRS-Experiment\\MAS\\ColdStart\\Output\\TestDataset_2Features_OrgRS_ActiveScore_results.txt");
////        
////        goodnessEvaluation.load_InstanceID_Pair("C:\\CRS-Experiment\\MAS\\ColdStart\\Input\\Input1\\TestDatasetMapping.txt");
////        HashMap<Integer, Float> topN_HM = goodnessEvaluation.getTopNOfDecisionValue(topN);
////        topN_HM = HashMapUtility.getSortedMapDescending(topN_HM);
////        double goodnessValueMetric1 = goodnessEvaluation.getCoAuthorshipGoodness_Metric1(
////                topN_HM, "C:\\CRS-Experiment\\MAS\\ColdStart\\Output\\GoodnessResult_2Features_OrgRS_ActiveScore_Top50.txt");
////        
////        System.out.println("Goodness Value for top" + topN + " is: " + goodnessValueMetric1);
//        //</editor-fold>
//        CoAuthorGraph currentGraph = new CoAuthorGraph(
//                "/2.CRS-ExperimetalData/SampleData/AuthorID_PaperID_2009_2011.txt",
//                "/2.CRS-ExperimetalData/SampleData/PaperID_Year_2009_2011.txt");
//        CoAuthorGraph pastGraph = new CoAuthorGraph("/2.CRS-ExperimetalData/SampleData/AuthorID_PaperID_2006_2008.txt",
//                "/2.CRS-ExperimetalData/SampleData/PaperID_Year_2006_2008.txt");
//
//        getDistinctAuthorIDFromPositiveSamples("/2.CRS-ExperimetalData/SampleData/Seniors/Testing_PositiveSamples_Senior.txt");
        HashMap<Integer, HashMap<Integer, Double>> hash = readEvaluationFile("D:\\1.CRS-Experiment\\MLData\\3-Hub\\Senior\\TestingData\\test.txt", "Positive");
        List<Integer> top = getTopN(198037, 2, hash);
        for (int i = 0; i < top.size(); i++) {
            System.out.println(top.get(i));
        }
        System.out.println("END");
    }
}
