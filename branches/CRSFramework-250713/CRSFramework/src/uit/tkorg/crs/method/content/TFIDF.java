package uit.tkorg.crs.method.content;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author TinHuynh
 */
public class TFIDF {

    public static HashMap<Integer, Integer> _AuthorInstanceHM = new HashMap<>();
    public static HashMap<Integer, Integer> _InstanceAuthorHM = new HashMap<>();
    public static HashMap<Integer, String> _InstancePublicationHM = new HashMap<>();
    private static HashMap<Integer, HashMap<Integer, Float>> _tfidfHM = new HashMap<>();
    DocumentSimilarityTF similarityUsingTF;
    DocumentSimilarityTFIDF similarityUsingTFIDF;
    private Object lock = new Object();

    private void Run(int inputAuthorID, boolean isTF, boolean isTFIDF) {
        try {
            int currentAuthorID;
            System.out.println("CURRENT INSTANCE IS:" + inputAuthorID);
            int instanceID = getInstanceFromAuthorID(inputAuthorID);
            HashMap<Integer, Float> similarityHM = new HashMap<Integer, Float>();
            if (isTF == true) {
                synchronized (lock) {
                    for (int otherInstanceID = 0; otherInstanceID < _InstancePublicationHM.size(); otherInstanceID++) {
                        if (instanceID != otherInstanceID) {
                            currentAuthorID = getAuthorIDFromInstanceID(otherInstanceID);
                            float simValue = (float) similarityUsingTF.getCosineSimilarityWhenIndexAllDocument(instanceID, otherInstanceID);
                            similarityHM.put(currentAuthorID, simValue);
                        }
                    }
                }
            }
            if (isTFIDF == true) {
                synchronized (lock) {
                    for (int otherInstanceID = 0; otherInstanceID < _InstancePublicationHM.size(); otherInstanceID++) {
                        if (instanceID != otherInstanceID) {
                            currentAuthorID = getAuthorIDFromInstanceID(otherInstanceID);
                            float simValue = (float) similarityUsingTFIDF.getCosineSimilarityWhenIndexAllDocument(instanceID, otherInstanceID);
                            similarityHM.put(currentAuthorID, simValue);
                        }
                    }
                }
            }
            _tfidfHM.put(inputAuthorID, similarityHM);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private int getInstanceFromAuthorID(int authorID) {
        return _AuthorInstanceHM.get(authorID);
    }

    private int getAuthorIDFromInstanceID(int instanceID) {
        return _InstanceAuthorHM.get(instanceID);
    }

    public String getPublicationFromAuthorID(int authorID) {
        int instanceID = getInstanceFromAuthorID(authorID);
        return (_InstancePublicationHM.get(instanceID));
    }

    private void loadMappingInstanceIDAuthorID(String mapFile) {
        try {
            FileInputStream fis = new FileInputStream(mapFile);
            Reader reader = new InputStreamReader(fis, "UTF8");
            BufferedReader bufferReader = new BufferedReader(reader);
            bufferReader.readLine(); // skip the header line
            String line = null;
            String[] tokens;
            while ((line = bufferReader.readLine()) != null) {
                tokens = line.split("\t");
                if (tokens.length != 2) {
                    continue;
                }

                int authorID = Integer.parseInt(tokens[0]);
                int instanceID = Integer.parseInt(tokens[1]);
                _AuthorInstanceHM.put(authorID, instanceID);
                _InstanceAuthorHM.put(instanceID, authorID);
            }
            bufferReader.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void loadInstancePublication(String inputFile) {
        try {
            FileInputStream fis = new FileInputStream(inputFile);
            Reader reader = new InputStreamReader(fis, "UTF8");
            BufferedReader bufferReader = new BufferedReader(reader);
            String line = null;
            String[] tokens;
            int instanceID = 0;
            while ((line = bufferReader.readLine()) != null) {
                _InstancePublicationHM.put(instanceID, line);
                instanceID++;
            }
            bufferReader.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * tiendv Input for run real TF
     */
    public HashMap<Integer, HashMap<Integer, Float>> process(String inputFile, ArrayList<Integer> listAuthorID, final boolean isTF, final boolean isTFIDF) {
        System.out.println("START PROCESSING TF/TFIDF");
        try {
            loadInstancePublication(inputFile);
            String pathFile = (new File(inputFile)).getParent();
            loadMappingInstanceIDAuthorID(pathFile + "/CRS-AuthorIDAndInstance.txt");
            if (isTF == true) {
                similarityUsingTF = new DocumentSimilarityTF();
                similarityUsingTF.indexAllDocument(_InstancePublicationHM);
            }
            if (isTFIDF == true) {
                similarityUsingTFIDF = new DocumentSimilarityTFIDF();
                similarityUsingTFIDF.indexAllDocument(_InstancePublicationHM);
            }

            Runtime runtime = Runtime.getRuntime();
            int numOfProcessors = runtime.availableProcessors();
            ExecutorService executor = Executors.newFixedThreadPool(numOfProcessors - 2);
            for (final int authorId : listAuthorID) {
                executor.submit(new Runnable() {

                    @Override
                    public void run() {
                        Run(authorId, isTF, isTFIDF);
                    }
                });
            }
            executor.shutdown();
            while (!executor.isTerminated()) {
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        System.out.println("FINISH PROCESSING TF/TFIDF");
        return _tfidfHM;
    }
}