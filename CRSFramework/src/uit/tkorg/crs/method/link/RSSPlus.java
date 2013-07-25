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
public class RSSPlus {

    private void Run(int authorId1) {
        Set<Integer> listAuthorFirstHop = _graph.get(authorId1).keySet();
        HashMap<Integer, Float> listRTBVS = new HashMap<>();
        for (int authorId_FirstHop : listAuthorFirstHop) {
            listRTBVS.put(authorId1, 0f);
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
                        Float totalWeight = listRTBVS.get(authorId_SecondHop);
                        if (totalWeight == null) {
                            totalWeight = 0f;
                        }
                        totalWeight += weight;
                        listRTBVS.put(authorId_SecondHop, totalWeight);
                    }
                }
            }
        }
        Set<Integer> listId = listRTBVS.keySet();
        for (int aid : listId) {
            Float weight = _graph.get(authorId1).get(aid);
            if (weight != null && weight > 0f) {
                Float totalWeight = listRTBVS.get(aid);
                totalWeight += weight;
                listRTBVS.put(aid, totalWeight);
            }
        }
        _rtbvsData.put(authorId1, listRTBVS);
    }
    private HashMap<Integer, HashMap<Integer, Float>> _rtbvsData;
    private HashMap<Integer, HashMap<Integer, Float>> _graph;

    public HashMap<Integer, HashMap<Integer, Float>> Process(HashMap<Integer, HashMap<Integer, Float>> graph,
            ArrayList<Integer> listAuthor) {
        _rtbvsData = new HashMap<>();
        _graph = graph;

        Runtime runtime = Runtime.getRuntime();
        int numOfProcessors = runtime.availableProcessors();

        ExecutorService executor = Executors.newFixedThreadPool(numOfProcessors / 2);
        for (final int authorId : listAuthor) {
            executor.submit(new Runnable() {
                @Override
                public void run() {
                    Run(authorId);
                }
            });
        }

        executor.shutdown();
        while (!executor.isTerminated()) {
        }
        return _rtbvsData;
    }
}
