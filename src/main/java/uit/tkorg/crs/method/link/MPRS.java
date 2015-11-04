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
public class MPRS {

    private HashMap<Integer, HashMap<Integer, Float>> _mprsData;
    private HashMap<Integer, HashMap<Integer, Float>> _graph;

    private void runMPRS(int nodeId1) {
        Set<Integer> listNodeFirstHop = _graph.get(nodeId1).keySet();
        HashMap<Integer, Float> listMPRS = new HashMap<>();
        for (int nodeId_FirstHop : listNodeFirstHop) {
            Float weight = _graph.get(nodeId1).get(nodeId_FirstHop);
            if (weight == null) {
                weight = 0f;
            }
            listMPRS.put(nodeId_FirstHop, weight);
            Set<Integer> listNodeSecondHop = _graph.get(nodeId_FirstHop).keySet();

            for (int nodeId_SecondHop : listNodeSecondHop) {
                if (nodeId1 != nodeId_SecondHop) {
                    Float weight1 = _graph.get(nodeId1).get(nodeId_FirstHop);
                    Float weight2 = _graph.get(nodeId_FirstHop).get(nodeId_SecondHop);
                    if (weight1 != null && weight2 != null) {
                        weight1 *= weight2;
                    } else {
                        weight1 = 0f;
                    }

                    if (weight1 > 0f) {
                        Float totalWeight = listMPRS.get(nodeId_SecondHop);
                        if (totalWeight == null) {
                            totalWeight = 0f;
                        }
                        if (weight1 > totalWeight) {
                            totalWeight = weight1;
                        }
                        listMPRS.put(nodeId_SecondHop, totalWeight);
                    }
                }
            }
        }

        _mprsData.put(nodeId1, listMPRS);
    }

    /**
     *
     * @param graph
     * @param listNode
     * @return
     */
    public HashMap<Integer, HashMap<Integer, Float>> process(HashMap<Integer, HashMap<Integer, Float>> graph,
            HashMap<Integer, String> listNode) {
        _mprsData = new HashMap<>();
        _graph = graph;

        Runtime runtime = Runtime.getRuntime();
        int numOfProcessors = runtime.availableProcessors();
        ExecutorService executor = Executors.newFixedThreadPool(numOfProcessors - 1);

        for (final int nodeId : listNode.keySet()) {
            executor.submit(new Runnable() {
                @Override
                public void run() {
                    runMPRS(nodeId);
                }
            });
        }

        executor.shutdown();
        while (!executor.isTerminated()) {
        }
        return _mprsData;
    }
}
