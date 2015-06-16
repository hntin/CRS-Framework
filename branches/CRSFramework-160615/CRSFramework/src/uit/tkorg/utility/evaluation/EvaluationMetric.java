/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uit.tkorg.utility.evaluation;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author Tin Huynh
 */
public class EvaluationMetric {

    public static HashMap<Integer, ArrayList<Integer>> authorHasLinkHM = new HashMap();

    public static float Recall_TopN(HashMap<Integer, HashMap<Integer, Float>> topNData,
            HashMap<Integer, ArrayList<Integer>> testingData) {
        return 0;
    }

    public static float Precision_TopN(HashMap<Integer, HashMap<Integer, Float>> topNData,
            HashMap<Integer, ArrayList<Integer>> testingData) {

        return 0;
    }

    public static float Mean_Precision_TopN(HashMap<Integer, HashMap<Integer, Float>> topNData,
            HashMap<Integer, ArrayList<Integer>> testingData) {
        int tp_fp = 0;
        int tp = 0;
        for (int aid1 : topNData.keySet()) {
            for (int aid2 : topNData.get(aid1).keySet()) {
                tp_fp++;

                if (testingData.containsKey(aid1) && testingData.get(aid1).contains(aid2)
                        || testingData.containsKey(aid2) && testingData.get(aid2).contains(aid1)) {
                    tp++;

                    // Save true case (pair of coauthor) to text file for checking
                    ArrayList linkedAuthorList = null;
                    if (!authorHasLinkHM.containsKey(aid1)) {
                        linkedAuthorList = new ArrayList();
                        linkedAuthorList.add(aid2);                     
                    }
                    else {
                        linkedAuthorList = authorHasLinkHM.get(aid1);
                        if (!linkedAuthorList.contains(aid2)) {
                            linkedAuthorList.add(aid2);
                        }
                    }
                    authorHasLinkHM.put(aid1, linkedAuthorList);
                }
            }
        }
        return (float) tp / (float) tp_fp;
    }

    public static float Mean_Recall_TopN(HashMap<Integer, HashMap<Integer, Float>> topNData,
            HashMap<Integer, ArrayList<Integer>> testingData) {

        float recallTopN = 0f;
        int tp_fn = 0;
        for (int aid1 : topNData.keySet()) {
            if (testingData.get(aid1) != null) {
                tp_fn += testingData.get(aid1).size();
            }
            for (int aid2 : testingData.keySet()) {
                if ((aid1 != aid2) && testingData.get(aid2).contains(aid1)) {
                    tp_fn++;
                }
            }
        } // tinh kieu nay dung, nhung ket qua sai do cach luu tru cua mang testingData

        int tp = 0;
        for (int aid1 : topNData.keySet()) {
            for (int aid2 : topNData.get(aid1).keySet()) {
                if (testingData.containsKey(aid1) && testingData.get(aid1).contains(aid2)
                        || testingData.containsKey(aid2) && testingData.get(aid2).contains(aid1)) {
                    tp++;
                }
            }
        }

        if (tp_fn != 0) {
            recallTopN = (float) tp / (float) tp_fn;
        }

        return recallTopN;
    }

    public static float AP(HashMap<Integer, HashMap<Integer, Float>> topNData,
            HashMap<Integer, ArrayList<Integer>> testingData) {
        float result = 0f;

        return result;
    }

    public static float MAP(HashMap<Integer, HashMap<Integer, Float>> data,
            HashMap<Integer, ArrayList<Integer>> testingData) {
        float result = 0f;

        return result;
    }
}
