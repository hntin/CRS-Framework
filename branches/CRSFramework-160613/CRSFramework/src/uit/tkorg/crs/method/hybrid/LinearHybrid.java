/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uit.tkorg.crs.method.hybrid;

import java.util.HashMap;

/**
 *
 * @author TinHuynh
 */
public class LinearHybrid {
    private float k = (float) 0.5;

    HashMap<Integer, HashMap<Integer, Float>> linearHybridResult = null;
    public  HashMap<Integer, HashMap<Integer, Float>> calculatingLinearHybriđ(
                                    HashMap<Integer, HashMap<Integer, Float>> rssResult, 
                                    HashMap<Integer, HashMap<Integer, Float>> tfidfResult) {
        
        linearHybridResult = new HashMap<>();
        for (int authorID1 :  rssResult.keySet()) {
             for (int authorID2 : rssResult.get(authorID1).keySet()) {
                 float linkValue = rssResult.get(authorID1).get(authorID2);
                 float contentValue = tfidfResult.get(authorID1).get(authorID2);
                 float hybridValue = (float) k*linkValue + (float)(1-k)*contentValue;
                 
                 HashMap<Integer, Float> hybridStrengthHM = linearHybridResult.get(authorID1);
                 if (hybridStrengthHM == null) {
                     hybridStrengthHM =  new HashMap();
                 }
                 
                 hybridStrengthHM.put(authorID2, hybridValue);
                 linearHybridResult.put(authorID1, hybridStrengthHM);
             }
        }
        
        return linearHybridResult;
    }
            
}
