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
    
    private void runJaccard(int authorId1) {
        for (int authorId2 : _graph.keySet()) {
            if (authorId1 != authorId2) {
                Set<Integer> neighborsOfAuthor1 = _graph.get(authorId1).keySet();
                Set<Integer> neighborsOfAuthor2 = _graph.get(authorId2).keySet();
                ArrayList<Integer> sharedNeighbors = new ArrayList<>();
                for (int authorId : neighborsOfAuthor1) {
                    if (neighborsOfAuthor2.contains(authorId)) {
                        sharedNeighbors.add(authorId);
                    }
                }
                ArrayList<Integer> totalNeighbors = new ArrayList<>();
                totalNeighbors.addAll(neighborsOfAuthor1);
                for (int authorId : neighborsOfAuthor2) {
                    if (!totalNeighbors.contains(authorId)) {
                        totalNeighbors.add(authorId);
                    }
                }

                float value = (float) sharedNeighbors.size() / (float) totalNeighbors.size();
                if (value > 0f) {
                    HashMap<Integer, Float> listJaccard = _jaccardData.get(authorId1);
                    if (listJaccard == null) {
                        listJaccard = new HashMap<>();
                    }
                    listJaccard.put(authorId2, value);
                    _jaccardData.put(authorId1, listJaccard);
                }
            }
        }
    }
    
    /**
     * 
     * @param graph
     * @param listAuthor
     * @return 
     */
    public HashMap<Integer, HashMap<Integer, Float>> process(HashMap<Integer, HashMap<Integer, Float>> graph,
            HashMap<Integer, String> listAuthor) {
        _jaccardData = new HashMap<>();
        _graph = graph;

        Runtime runtime = Runtime.getRuntime();
        int numOfProcessors = runtime.availableProcessors();

        ExecutorService executor = Executors.newFixedThreadPool(numOfProcessors - 1);
        for (final int authorId : listAuthor.keySet()) {
            executor.submit(new Runnable() {
                @Override
                public void run() {
                    runJaccard(authorId);
                }
            });
        }

        executor.shutdown();
        while (!executor.isTerminated()) {
        }
        return _jaccardData;
    }
}
