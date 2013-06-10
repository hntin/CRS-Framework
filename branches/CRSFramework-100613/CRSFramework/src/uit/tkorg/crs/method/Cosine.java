/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uit.tkorg.crs.method;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 * @author daolv
 */
public class Cosine {

    private void Run(int authorId1) {
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

                float value = 0f;
                if (neighborsOfAuthor1.size() * neighborsOfAuthor2.size() > 0f) {
                    value = (float) sharedNeighbors.size() / (float) Math.sqrt(neighborsOfAuthor1.size() * neighborsOfAuthor2.size());
                }
                if (value > 0f) {
                    HashMap<Integer, Float> listCosine = _cosineData.get(authorId1);
                    if (listCosine == null) {
                        listCosine = new HashMap<>();
                    }
                    listCosine.put(authorId2, value);
                    _cosineData.put(authorId1, listCosine);
                }
            }
        }
    }
    private HashMap<Integer, HashMap<Integer, Float>> _cosineData;
    private HashMap<Integer, HashMap<Integer, Float>> _graph;

    public HashMap<Integer, HashMap<Integer, Float>> Process(HashMap<Integer, HashMap<Integer, Float>> graph,
            ArrayList<Integer> listAuthor) {
        _cosineData = new HashMap<>();
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
        return _cosineData;
    }
}
