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
public class MPBVS {

    private void Run(int authorId1) {
        Set<Integer> listAuthorFirstHop = _graph.get(authorId1).keySet();
        HashMap<Integer, Float> listMPBVS = new HashMap<>();
        for (int authorId_FirstHop : listAuthorFirstHop) {
            Float weight = _graph.get(authorId1).get(authorId_FirstHop);
            if (weight == null) {
                weight = 0f;
            }
            listMPBVS.put(authorId_FirstHop, weight);
            Set<Integer> listAuthorSecondHop = _graph.get(authorId_FirstHop).keySet();

            for (int authorId_SecondHop : listAuthorSecondHop) {
                if (authorId1 != authorId_SecondHop) {
                    Float weight1 = _graph.get(authorId1).get(authorId_FirstHop);
                    Float weight2 = _graph.get(authorId_FirstHop).get(authorId_SecondHop);
                    if (weight1 != null && weight2 != null) {
                        weight1 *= weight2;
                    } else {
                        weight1 = 0f;
                    }

                    if (weight1 > 0f) {
                        Float totalWeight = listMPBVS.get(authorId_SecondHop);
                        if (totalWeight == null) {
                            totalWeight = 0f;
                        }
                        if (weight1 > totalWeight) {
                            totalWeight = weight1;
                        }
                        listMPBVS.put(authorId_SecondHop, totalWeight);
                    }
                }
            }
        }
        _mpbvsData.put(authorId1, listMPBVS);
    }
    private HashMap<Integer, HashMap<Integer, Float>> _mpbvsData;
    private HashMap<Integer, HashMap<Integer, Float>> _graph;

    public HashMap<Integer, HashMap<Integer, Float>> Process(HashMap<Integer, HashMap<Integer, Float>> graph,
            ArrayList<Integer> listAuthor) {
        _mpbvsData = new HashMap<>();
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
        return _mpbvsData;
    }
}
