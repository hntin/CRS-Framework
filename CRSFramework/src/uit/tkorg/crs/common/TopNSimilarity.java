/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uit.tkorg.crs.common;

import java.util.HashMap;
import uit.tkorg.crs.experiment.ContentMethodExperiment;
import uit.tkorg.crs.graph.AuthorGraph;

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
                // Put ONLY idRecommend THAT HAVE NO LINK TO authorID BEFORE IN THE TRANING COAUTHOR NET to the listAuthorRecommend, 
                // and check if (the size of the recommended list) > topN --> remove item with the minimum Value
                // Finally we got the topN authorID with higest value<Float>
                boolean hasLink = false;
                if (rssGraph.containsKey(authorId)) {
                    if (rssGraph.get(authorId).containsKey(idRecommend)) {
                        hasLink = true;
                    }
                }
                if (rssGraph.containsKey(idRecommend)) {
                    if (rssGraph.get(idRecommend).containsKey(authorId)) {
                        hasLink = true;
                    }
                }
                // Put to the list if NO LINK BEFORE
                if (hasLink == false) {
                    listAuthorRecommend.put(idRecommend, data.get(authorId).get(idRecommend));
                }

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

    public static HashMap<Integer, HashMap<Integer, Float>> findTopNSimilarityForKLDivergenceForNewLinkOnly(
            int topN, HashMap<Integer, HashMap<Integer, Float>> data, HashMap<Integer, HashMap<Integer, Float>> rssGraph) {

        HashMap<Integer, HashMap<Integer, Float>> result = new HashMap<>();
        for (int authorId : data.keySet()) {
            HashMap<Integer, Float> listAuthorRecommend = new HashMap<>();
            for (int idRecommend : data.get(authorId).keySet()) {
                // Put ONLY idRecommend THAT HAVE NO LINK TO authorID BEFORE IN THE TRANING COAUTHOR NET to the listAuthorRecommend, 
                // and check if (the size of the recommended list) > topN --> remove item with the minimum Value
                // Finally we got the topN authorID with higest value<Float>
                boolean hasLink = false;
                if (rssGraph.containsKey(authorId)) {
                    if (rssGraph.get(authorId).containsKey(idRecommend)) {
                        hasLink = true;
                    }
                }
                if (rssGraph.containsKey(idRecommend)) {
                    if (rssGraph.get(idRecommend).containsKey(authorId)) {
                        hasLink = true;
                    }
                }
                // Put to the list if NO LINK BEFORE
                if (hasLink == false) {
                    listAuthorRecommend.put(idRecommend, data.get(authorId).get(idRecommend));
                }

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

//    public static void main(String args[]) {
//        int topN = 0;
//        HashMap<Integer, HashMap<Integer, Float>> data = new HashMap<>();
//
//        HashMap<Integer, Float> valueHM = new HashMap<>();
//        valueHM.put(2, 0.2f);
//        valueHM.put(3, 0.3f);
//        valueHM.put(4, 0.4f);
//        valueHM.put(5, 0.5f);
//        valueHM.put(6, 0.6f);
//        valueHM.put(7, 0.7f);
//        valueHM.put(8, 0.8f);
//        data.put(1, valueHM);
//
//        valueHM = new HashMap<>();
//        valueHM.put(1, 0.1f);
//        valueHM.put(3, 0.3f);
//        valueHM.put(4, 0.4f);
//        valueHM.put(5, 0.5f);
//        valueHM.put(6, 0.6f);
//        valueHM.put(7, 0.7f);
//        valueHM.put(8, 0.8f);
//        data.put(2, valueHM);
//
//        AuthorGraph _graph = AuthorGraph.getInstance();
//        _graph.LoadTrainingData(
//                "C:\\CRS-Experiment\\Sampledata\\[Training]AuthorId_PaperID.txt",
//                "C:\\CRS-Experiment\\Sampledata\\[Training]PaperID_Year.txt");
//        _graph.LoadTestingData(
//                "C:\\CRS-Experiment\\Sampledata\\[NearTesting]AuthorId_PaperID.txt",
//                "C:\\CRS-Experiment\\Sampledata\\[FarTesting]AuthorId_PaperID.txt");
//
//        // Building Graphs
//        _graph.BuidCoAuthorGraph();
//        _graph.BuildingRSSGraph();
//
//        HashMap<Integer, HashMap<Integer, Float>> result1 = TopNSimilarity.findTopNSimilarity(2, data);
//        HashMap<Integer, HashMap<Integer, Float>> result2 = TopNSimilarity.findTopNSimilarityForKLDivergence(2, data);
//        HashMap<Integer, HashMap<Integer, Float>> result3 = TopNSimilarity.findTopNSimilarityForNewLinkOnly(2, data, _graph.rssGraph);
//        HashMap<Integer, HashMap<Integer, Float>> result4 = TopNSimilarity.findTopNSimilarityForKLDivergenceForNewLinkOnly(2, data, _graph.rssGraph);
//        System.out.println("DONE...");
//    }
}
