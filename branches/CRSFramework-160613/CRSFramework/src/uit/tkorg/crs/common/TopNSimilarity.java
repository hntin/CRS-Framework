/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uit.tkorg.crs.common;

import java.util.HashMap;
import uit.tkorg.crs.experiment.ContentMethodExperiment;

/**
 *
 * @author TinHuynh
 */
public class TopNSimilarity {
    
    public static HashMap<Integer, HashMap<Integer, Float>> findTopNSimilarity(int topN, HashMap<Integer, HashMap<Integer, Float>> data) {
        HashMap<Integer, HashMap<Integer, Float>> result = new HashMap<>();
        for (int authorId : data.keySet()) {
            HashMap<Integer, Float> listAuthorRecommend = new HashMap<>();
            for (int idRecommend : data.get(authorId).keySet()) {
                // Put all authorID to the listAuthorRecommend, 
                // and check if (the size of the recommended list) > topN --> remove item with the minimum Value
                // Finally we got the topN authorID with higest value<Float>
                listAuthorRecommend.put(idRecommend, data.get(authorId).get(idRecommend));
                if (listAuthorRecommend.size() > topN) {
                    int keyMinValue = 0;
                    float minValue = Integer.MAX_VALUE;
                    for (int id : listAuthorRecommend.keySet()) {
                        if (listAuthorRecommend.get(id) < minValue) {
                            minValue = listAuthorRecommend.get(id);
                            keyMinValue = id;
                        }
                    }

                    listAuthorRecommend.remove(keyMinValue);
                }
            }
            result.put(authorId, listAuthorRecommend);
        }
        return result;
    }
    
    public static HashMap<Integer, HashMap<Integer, Float>> findTopNSimilarityForNewLinkOnly(
            int topN, HashMap<Integer, HashMap<Integer, Float>> data, HashMap<Integer, HashMap<Integer, Float>> rssGraph) {
        
        HashMap<Integer, HashMap<Integer, Float>> result = new HashMap<>();
        for (int authorId : data.keySet()) {
            HashMap<Integer, Float> listAuthorRecommend = new HashMap<>();
            for (int idRecommend : data.get(authorId).keySet()) {
                // Put all authorID to the listAuthorRecommend, 
                // and check if (the size of the recommended list) > topN --> remove item with the minimum Value
                // Finally we got the topN authorID with higest value<Float>
                listAuthorRecommend.put(idRecommend, data.get(authorId).get(idRecommend));
                
                
                if (listAuthorRecommend.size() > topN) {
                    int keyMinValue = 0;
                    float minValue = Integer.MAX_VALUE;
                    for (int id : listAuthorRecommend.keySet()) {
                        if (listAuthorRecommend.get(id) < minValue) {
                            minValue = listAuthorRecommend.get(id);
                            keyMinValue = id;
                        }
                    }

                    listAuthorRecommend.remove(keyMinValue);
                }
            }
            result.put(authorId, listAuthorRecommend);
        }
        return result;
    }
    
    public static HashMap<Integer, HashMap<Integer, Float>> findTopNSimilarityForKLDivergence(int topN, HashMap<Integer, HashMap<Integer, Float>> data) {
        HashMap<Integer, HashMap<Integer, Float>> result = new HashMap<>();
        for (int authorId : data.keySet()) {
            HashMap<Integer, Float> listAuthorRecommend = new HashMap<>();
            for (int idRecommend : data.get(authorId).keySet()) {
                listAuthorRecommend.put(idRecommend, data.get(authorId).get(idRecommend));
                if (listAuthorRecommend.size() > topN) {
                    int keyMaxValue = 0;
                    float maxValue = Float.MIN_VALUE;
                    for (int id : listAuthorRecommend.keySet()) {
                        if (listAuthorRecommend.get(id) > maxValue) {
                            maxValue = listAuthorRecommend.get(id);
                            keyMaxValue = id;
                        }
                    }

                    listAuthorRecommend.remove(keyMaxValue);
                }
            }
            result.put(authorId, listAuthorRecommend);
        }
        return result;
    }
    
      
    ///*
    public static void main(String args[]) {
        int topN = 0;
        HashMap<Integer, HashMap<Integer, Float>> data = new HashMap<>();
        HashMap<Integer, Float> valueHM = new HashMap<>();
        
        valueHM.put(6, 0.4f);
        valueHM.put(2, 0.2f);
        valueHM.put(3, 0.3f);
        valueHM.put(4, 0.4f);
        valueHM.put(5, 0.2f);
        
        data.put(0, valueHM);
        data.put(1, valueHM);
                
        HashMap<Integer, HashMap<Integer, Float>> result1 = TopNSimilarity.findTopNSimilarity(2, data);
        HashMap<Integer, HashMap<Integer, Float>> result2 = TopNSimilarity.findTopNSimilarityForKLDivergence(2, data);
        
        System.out.println("DONE...");
    }
    //* */
}
