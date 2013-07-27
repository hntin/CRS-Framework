package uit.tkorg.crs.method.hybrid;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 * @author TinHuynh
 */
public class AdaptiveHybrid {
    public static HashMap<Integer, Integer> _AuthorInstanceHM = new HashMap<>();
    public static HashMap<Integer, Integer> _InstanceAuthorHM = new HashMap<>();
    public static HashMap<Integer, String> _InstancePublicationHM = new HashMap<>();
    private static HashMap<Integer, HashMap<Integer, Float>> _adaptiveHybridHM = new HashMap<>();

    public HashMap<Integer, HashMap<Integer, Float>> process(ArrayList<Integer> listAuthorID) {
        System.out.println("START PROCESSING TFIDF");
        try {
            Runtime runtime = Runtime.getRuntime();
            int numOfProcessors = runtime.availableProcessors();
            ExecutorService executor = Executors.newFixedThreadPool(numOfProcessors - 1);
            for (final int authorId : listAuthorID) {
                executor.submit(new Runnable() {
                    @Override
                    public void run() {
                        //...
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
