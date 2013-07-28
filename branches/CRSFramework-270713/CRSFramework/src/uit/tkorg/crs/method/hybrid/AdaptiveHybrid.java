package uit.tkorg.crs.method.hybrid;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import uit.tkorg.crs.graph.Graph;
import uit.tkorg.crs.method.content.CollectionDocument;
import uit.tkorg.crs.method.content.SimilarityTF;
import static uit.tkorg.crs.method.content.TFIDF._AuthorInstanceHM;
import static uit.tkorg.crs.method.content.TFIDF._InstanceAuthorHM;
import static uit.tkorg.crs.method.content.TFIDF._InstancePublicationHM;

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
    CollectionDocument indexAllDocument;

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

    private void runTF(int inputAuthorID) {
        try {
            int currentAuthorID;
            System.out.println("Running TF:" + inputAuthorID);
            int instanceID = getInstanceFromAuthorID(inputAuthorID);
            HashMap<Integer, Float> similarityHM = new HashMap<Integer, Float>();
            for (int otherInstanceID = 0; otherInstanceID < _InstancePublicationHM.size(); otherInstanceID++) {
                if (instanceID != otherInstanceID) {
                    currentAuthorID = getAuthorIDFromInstanceID(otherInstanceID);
                    SimilarityTF similarityUsingTF = new SimilarityTF();
                    float simValue = (float) similarityUsingTF.getCosineSimilarityWhenIndexAllDocument(
                            indexAllDocument.getTermWithAuthorID(instanceID), indexAllDocument.getTermWithAuthorID(otherInstanceID));
                    similarityHM.put(currentAuthorID, simValue);
                }
            }

            _adaptiveHybridHM.put(inputAuthorID, similarityHM);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void runRSS(int authorId1) {
        System.out.println("Running RSS:" + authorId1);
        Set<Integer> listAuthorFirstHop = _rssGraph.get(authorId1).keySet();
        HashMap<Integer, Float> listRSS = new HashMap<>();
        for (int authorId_FirstHop : listAuthorFirstHop) {
            listRSS.put(authorId1, 0f);
            Set<Integer> listAuthorSecondHop = _rssGraph.get(authorId_FirstHop).keySet();
            for (int authorId_SecondHop : listAuthorSecondHop) {
                if (authorId1 != authorId_SecondHop) {
                    Float weight = _rssGraph.get(authorId1).get(authorId_FirstHop);
                    Float weight2 = _rssGraph.get(authorId_FirstHop).get(authorId_SecondHop);
                    if (weight != null && weight2 != null) {
                        weight *= weight2;
                    } else {
                        weight = 0f;
                    }

                    if (weight > 0f) {
                        Float totalWeight = listRSS.get(authorId_SecondHop);
                        if (totalWeight == null) {
                            totalWeight = 0f;
                        }
                        totalWeight += weight;
                        listRSS.put(authorId_SecondHop, totalWeight);
                    }
                }
            }
        }
        Set<Integer> listId = listRSS.keySet();
        for (int aid : listId) {
            Float weight = _rssGraph.get(authorId1).get(aid);
            if (weight != null && weight > 0f) {
                Float totalWeight = listRSS.get(aid);
                totalWeight += weight;
                listRSS.put(aid, totalWeight);
            }
        }

        _adaptiveHybridHM.put(authorId1, listRSS);
    }

    private void runAdaptiveHybrid(int authorId, String degreeGroup) {
        if (_rssGraph.get(authorId) != null && _rssGraph.get(authorId).size() == 0) {
            // author HAVE NO any connection
            // apply content based approach
            runTF(authorId);
        } else {
            if (degreeGroup.equalsIgnoreCase("L")) {
                // apply content based approach
                runTF(authorId);
            } else {
                if (degreeGroup.equalsIgnoreCase("M")) {
                    // APPLY link based approach
                    runRSS(authorId);
                } else { // H = High
                    // APPLY link based approach
                    runRSS(authorId);
                }
            }
        }
    }

    public HashMap<Integer, HashMap<Integer, Float>> process(String inputFile, 
                                                            HashMap<Integer, HashMap<Integer, Float>> graph,
                                                            HashMap<Integer, String> listAuthorID) {

        System.out.println("START PROCESSING AdaptiveHybrid");
        try {
            _rssGraph = graph;
            loadInstancePublication(inputFile);
            String pathFile = (new File(inputFile)).getParent();
            loadMappingInstanceIDAuthorID(pathFile + "/CRS-AuthorIDAndInstance.txt");
            indexAllDocument = new CollectionDocument();
            indexAllDocument.indexAllDocument(_InstancePublicationHM);
            indexAllDocument.openReader();

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
            
            indexAllDocument.closeReader();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        System.out.println("FINISH PROCESSING AdaptiveHybrid");
        return _adaptiveHybridHM;
    }
}
