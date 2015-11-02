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
public class MPRSPlus {

    private HashMap<Integer, HashMap<Integer, Float>> _mprsPlusData;
    private HashMap<Integer, HashMap<Integer, Float>> _graph;

    private void runMPRSPlus(int authorId1) {
        Set<Integer> listAuthorFirstHop = _graph.get(authorId1).keySet();
        HashMap<Integer, Float> listMPBVSPlus = new HashMap<>();
        for (int authorId_FirstHop : listAuthorFirstHop) {
            Float weight = _graph.get(authorId1).get(authorId_FirstHop);
            if (weight == null) {
                weight = 0f;
            }
            listMPBVSPlus.put(authorId_FirstHop, weight);
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
                        Float totalWeight = listMPBVSPlus.get(authorId_SecondHop);
                        if (totalWeight == null) {
                            totalWeight = 0f;
                        }
                        if (weight1 > totalWeight) {
                            totalWeight = weight1;
                        }
                        listMPBVSPlus.put(authorId_SecondHop, totalWeight);
                    }
                }
            }
        }
        _mprsPlusData.put(authorId1, listMPBVSPlus);
    }

    /**
     *
     * @param graph
     * @param listAuthor
     * @return
     */
    public HashMap<Integer, HashMap<Integer, Float>> process(HashMap<Integer, HashMap<Integer, Float>> graph,
            HashMap<Integer, String> listAuthor) {
        _mprsPlusData = new HashMap<>();
        _graph = graph;

        Runtime runtime = Runtime.getRuntime();
        int numOfProcessors = runtime.availableProcessors();
        ExecutorService executor = Executors.newFixedThreadPool(numOfProcessors - 1);

        for (final int authorId : listAuthor.keySet()) {
            executor.submit(new Runnable() {
                @Override
                public void run() {
                    runMPRSPlus(authorId);
                }
            });
        }

        executor.shutdown();
        while (!executor.isTerminated()) {
        }
        return _mprsPlusData;
    }
}
