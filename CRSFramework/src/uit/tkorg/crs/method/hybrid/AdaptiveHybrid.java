package uit.tkorg.crs.method.hybrid;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import uit.tkorg.crs.graph.Graph;

/**
 *
 * @author TinHuynh
 */
public class AdaptiveHybrid {

    private HashMap<Integer, HashMap<Integer, Float>> _rssGraph;
    public static HashMap<Integer, Integer> _AuthorInstanceHM = new HashMap<>();
    public static HashMap<Integer, Integer> _InstanceAuthorHM = new HashMap<>();
    public static HashMap<Integer, String> _InstancePublicationHM = new HashMap<>();
    private static HashMap<Integer, HashMap<Integer, Float>> _adaptiveHybridHM = new HashMap<>();

    private void runAdaptiveHybrid(int authorId, String degreeGroup) {
        if (_rssGraph.get(authorId) != null && _rssGraph.get(authorId).size() == 0) {
            // author HAVE NO any connection
            // apply content based approach
        } else {
            if (degreeGroup.equalsIgnoreCase("L")) {
                // apply content based approach
            } else {
                if (degreeGroup.equalsIgnoreCase("M")) {
                    // APPLY link based approach
                } else { // H = High
                    // APPLY link based approach
                }
            }
        }

    }

    public HashMap<Integer, HashMap<Integer, Float>> process(HashMap<Integer, HashMap<Integer, Float>> graph,
            HashMap<Integer, String> listAuthorID) {

        _rssGraph = graph;
        System.out.println("START PROCESSING TFIDF");
        try {
            Runtime runtime = Runtime.getRuntime();
            int numOfProcessors = runtime.availableProcessors();
            ExecutorService executor = Executors.newFixedThreadPool(numOfProcessors - 1);
            for (final int authorId : listAuthorID.keySet()) {
                final String degreeGroup = listAuthorID.get(authorId);
                executor.submit(new Runnable() {
                    @Override
                    public void run() {
                        runAdaptiveHybrid(authorId, degreeGroup);
                    }
                });
            }

            executor.shutdown();
            while (!executor.isTerminated()) {
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        System.out.println("FINISH PROCESSING TFIDF");
        return _adaptiveHybridHM;
    }
}
