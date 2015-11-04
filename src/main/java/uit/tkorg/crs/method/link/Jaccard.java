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
public class Jaccard {

    private HashMap<Integer, HashMap<Integer, Float>> _jaccardData;
    private HashMap<Integer, HashMap<Integer, Float>> _graph;

    private void runJaccard(int nodeId1) {
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

                ArrayList<Integer> totalNeighbors = new ArrayList<>();
                totalNeighbors.addAll(neighborsOfNode1);
                for (int nodeId : neighborsOfNode2) {
                    if (!totalNeighbors.contains(nodeId)) {
                        totalNeighbors.add(nodeId);
                    }
                }

                float value = (float) sharedNeighbors.size() / (float) totalNeighbors.size();
                if (value > 0f) {
                    HashMap<Integer, Float> listJaccard = _jaccardData.get(nodeId1);
                    if (listJaccard == null) {
                        listJaccard = new HashMap<>();
                    }
                    listJaccard.put(nodeId2, value);
                    _jaccardData.put(nodeId1, listJaccard);
                }
            }
        }
    }

    /**
     *
     * @param graph
     * @param listNode
     * @return
     */
    public HashMap<Integer, HashMap<Integer, Float>> process(HashMap<Integer, HashMap<Integer, Float>> graph,
            HashMap<Integer, String> listNode) {

        _jaccardData = new HashMap<>();
        _graph = graph;

        Runtime runtime = Runtime.getRuntime();
        int numOfProcessors = runtime.availableProcessors();

        ExecutorService executor = Executors.newFixedThreadPool(numOfProcessors - 1);
        for (final int nodeId : listNode.keySet()) {
            executor.submit(new Runnable() {
                @Override
                public void run() {
                    runJaccard(nodeId);
                }
            });
        }

        executor.shutdown();
        while (!executor.isTerminated()) {
        }
        return _jaccardData;
    }
}
