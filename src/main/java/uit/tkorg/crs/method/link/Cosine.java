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
public class Cosine {

    private HashMap<Integer, HashMap<Integer, Float>> _cosineData;
    private HashMap<Integer, HashMap<Integer, Float>> _graph;

    /**
     * runCosine
     *
     * @param nodeId1
     */
    private void runCosine(int nodeId1) {
        for (int nodeId2 : _graph.keySet()) {
            if (nodeId1 != nodeId2) {
                Set<Integer> neighborsOfNode1 = _graph.get(nodeId1).keySet();
                Set<Integer> neighborsOfNode2 = _graph.get(nodeId2).keySet();
                ArrayList<Integer> sharedNeighbors = new ArrayList<>();
                for (int nodeId : neighborsOfNode1) {
                    if (neighborsOfNode2.contains(nodeId)) {
                        sharedNeighbors.add(nodeId);
                    }
                }

                float value = 0f;
                if (neighborsOfNode1.size() * neighborsOfNode2.size() > 0f) {
                    value = (float) sharedNeighbors.size() / (float) Math.sqrt(neighborsOfNode1.size() * neighborsOfNode2.size());
                }
                if (value > 0f) {
                    HashMap<Integer, Float> listCosine = _cosineData.get(nodeId1);
                    if (listCosine == null) {
                        listCosine = new HashMap<>();
                    }
                    listCosine.put(nodeId2, value);
                    _cosineData.put(nodeId1, listCosine);
                }
            }
        }
    }

    /**
     * process
     *
     * @param graph
     * @param listNode
     * @return
     */
    public HashMap<Integer, HashMap<Integer, Float>> process(HashMap<Integer, HashMap<Integer, Float>> graph,
            HashMap<Integer, String> listNode) {
        _cosineData = new HashMap<>();
        _graph = graph;

        Runtime runtime = Runtime.getRuntime();
        int numOfProcessors = runtime.availableProcessors();

        ExecutorService executor = Executors.newFixedThreadPool(numOfProcessors - 1);
        
        for (final int nodeId : listNode.keySet()) {
            executor.submit(new Runnable() {
                @Override
                public void run() {
                    runCosine(nodeId);
                }
            });
        }

        executor.shutdown();
        while (!executor.isTerminated()) {
        }
        return _cosineData;
    }
}
