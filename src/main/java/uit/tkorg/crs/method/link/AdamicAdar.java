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
public class AdamicAdar {

    private HashMap<Integer, HashMap<Integer, Float>> _adamicadarData;
    private HashMap<Integer, HashMap<Integer, Float>> _graph;
    
    private void runAdamicAdar(int nodeId1) {
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
                for (int nodeId : sharedNeighbors) {
                    Set<Integer> neighborsOfSharedNode = _graph.get(nodeId).keySet();
                    value += 1.0f / (float) Math.log10(neighborsOfSharedNode.size());
                }

                if (value > 0f) {
                    HashMap<Integer, Float> listAdamicAdar = _adamicadarData.get(nodeId1);
                    if (listAdamicAdar == null) {
                        listAdamicAdar = new HashMap<>();
                    }
                    listAdamicAdar.put(nodeId2, value);
                    _adamicadarData.put(nodeId1, listAdamicAdar);
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
        _adamicadarData = new HashMap<>();
        _graph = graph;

        Runtime runtime = Runtime.getRuntime();
        int numOfProcessors = runtime.availableProcessors();

        ExecutorService executor = Executors.newFixedThreadPool(numOfProcessors - 1);
        for (final int nodeId : listNode.keySet()) {
            executor.submit(new Runnable() {
                @Override
                public void run() {
                    runAdamicAdar(nodeId);
                }
            });
        } 

        executor.shutdown();
        while (!executor.isTerminated()) {
        }
        return _adamicadarData;
    }
}
