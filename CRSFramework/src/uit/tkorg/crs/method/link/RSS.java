/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uit.tkorg.crs.method.link;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 * @author daolv
 */
public class RSS {

    private void runRSS(int authorId1) {
        Set<Integer> listAuthorFirstHop = _graph.get(authorId1).keySet();
        HashMap<Integer, Float> listRSS = new HashMap<>();
        for (int authorId_FirstHop : listAuthorFirstHop) {
            listRSS.put(authorId1, 0f);
            Set<Integer> listAuthorSecondHop = _graph.get(authorId_FirstHop).keySet();
            for (int authorId_SecondHop : listAuthorSecondHop) {
                if (authorId1 != authorId_SecondHop) {
                    Float weight = _graph.get(authorId1).get(authorId_FirstHop);
                    Float weight2 = _graph.get(authorId_FirstHop).get(authorId_SecondHop);
                    if (weight != null && weight2 != null) {
                        weight *= weight2;
                    } else {
                        weight = 0f;
                    }

                    if (weight > 0f) {
                        Float totalWeight = listRSS.get(authorId_SecondHop);
                        if (totalWeight == null) {
                            totalWeight = 0f;
                        }
                        totalWeight += weight;
                        listRSS.put(authorId_SecondHop, totalWeight);
                    }
                }
            }
        }
        Set<Integer> listId = listRSS.keySet();
        for (int aid : listId) {
            Float weight = _graph.get(authorId1).get(aid);
            if (weight != null && weight > 0f) {
                Float totalWeight = listRSS.get(aid);
                totalWeight += weight;
                listRSS.put(aid, totalWeight);
            }
        }
        _rssData.put(authorId1, listRSS);
    }
    private HashMap<Integer, HashMap<Integer, Float>> _rssData;
    private HashMap<Integer, HashMap<Integer, Float>> _graph;

    public HashMap<Integer, HashMap<Integer, Float>> process(HashMap<Integer, HashMap<Integer, Float>> graph,
            HashMap<Integer, String> listAuthor) {
        _rssData = new HashMap<>();
        _graph = graph;

        Runtime runtime = Runtime.getRuntime();
        int numOfProcessors = runtime.availableProcessors();

        ExecutorService executor = Executors.newFixedThreadPool(numOfProcessors - 1);
        for (final int authorId : listAuthor.keySet()) {
            executor.submit(new Runnable() {
                @Override
                public void run() {
                    runRSS(authorId);
                }
            });
        }

        executor.shutdown();
        while (!executor.isTerminated()) {
        }
        return _rssData;
    }
}
