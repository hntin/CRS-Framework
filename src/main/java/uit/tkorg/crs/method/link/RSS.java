/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uit.tkorg.crs.method.link;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 * @author daolv
 */
public class RSS {

    private HashMap<Integer, HashMap<Integer, Float>> _rssData;
    private HashMap<Integer, HashMap<Integer, Float>> _graph;

    private void runRSS(int nodeId1) {
        Set<Integer> listNodeFirstHop = _graph.get(nodeId1).keySet();
        HashMap<Integer, Float> listRSS = new HashMap<>();
        for (int nodeId_FirstHop : listNodeFirstHop) {
            listRSS.put(nodeId1, 0f);
            Set<Integer> listNodeSecondHop = _graph.get(nodeId_FirstHop).keySet();
            for (int nodeId_SecondHop : listNodeSecondHop) {
                if (nodeId1 != nodeId_SecondHop) {
                    Float weight = _graph.get(nodeId1).get(nodeId_FirstHop);
                    Float weight2 = _graph.get(nodeId_FirstHop).get(nodeId_SecondHop);
                    if (weight != null && weight2 != null) {
                        weight *= weight2;
                    } else {
                        weight = 0f;
                    }

                    if (weight > 0f) {
                        Float totalWeight = listRSS.get(nodeId_SecondHop);
                        if (totalWeight == null) {
                            totalWeight = 0f;
                        }
                        totalWeight += weight;
                        listRSS.put(nodeId_SecondHop, totalWeight);
                    }
                }
            }
        }
        
        Set<Integer> listId = listRSS.keySet();
        for (int id : listId) {
            Float weight = _graph.get(nodeId1).get(id);
            if (weight != null && weight > 0f) {
                Float totalWeight = listRSS.get(id);
                totalWeight += weight;
                listRSS.put(id, totalWeight);
            }
        }
        
        _rssData.put(nodeId1, listRSS);
    }

    /**
     *
     * @param graph
     * @param listNode
     * @return
     */
    public HashMap<Integer, HashMap<Integer, Float>> process(HashMap<Integer, HashMap<Integer, Float>> graph,
            HashMap<Integer, String> listNode) {
        _rssData = new HashMap<>();
        _graph = graph;

        Runtime runtime = Runtime.getRuntime();
        int numOfProcessors = runtime.availableProcessors();

        ExecutorService executor = Executors.newFixedThreadPool(numOfProcessors - 1);
        for (final int nodeId : listNode.keySet()) {
            executor.submit(new Runnable() {
                @Override
                public void run() {
                    runRSS(nodeId);
                }
            });
        }

        executor.shutdown();
        while (!executor.isTerminated()) {
        }
        
        return _rssData;
    }
}
