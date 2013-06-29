/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uit.tkorg.crs.method.content;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author TinHuynh
 */
public class TFIDF {

    HashMap<Integer, Integer> _AuthorInstanceHM = new HashMap<>();
    HashMap<Integer, Integer> _InstanceAuthorHM = new HashMap<>();
    private static HashMap<Integer, HashMap<Integer, Float>> _tfidfHM = new HashMap<>();
    private HashMap<Integer, String> _InstancePublicationHM = new HashMap<>();

    public HashMap<Integer, HashMap<Integer, Float>> process(String inputFile, ArrayList<Integer> listAuthorID) {
        System.out.println("START PROCESSING TFIDF");
        loadInstancePublication(inputFile);
        try {
            int currentAuthorID;
            String pathFile = (new File(inputFile)).getParent();
            loadMappingInstanceIDAuthorID(pathFile + "/CRS-AuthorIDAndInstance.txt");
            for (int inputAuthorID : listAuthorID) {
                System.out.println("CURRENT INSTANCE IS:" + inputAuthorID);
                int instanceID = getInstanceFromAuthorID(inputAuthorID);

                HashMap<Integer, Float> similarityHM = new HashMap<Integer, Float>();
                for (int otherInstanceID = 0; otherInstanceID < _InstancePublicationHM.size(); otherInstanceID++) {
                    if (instanceID != otherInstanceID) {
                        // calculate  similarity using TF only                   
                        DocumentSimilarityTF similarityUsingTF = new DocumentSimilarityTF(
                                _InstancePublicationHM.get(instanceID), _InstancePublicationHM.get(otherInstanceID));
                        float simValue = (float) similarityUsingTF.getCosineSimilarity();
                        currentAuthorID = getAuthorIDFromInstanceID(otherInstanceID);
                        similarityHM.put(currentAuthorID, simValue);

                        // calculate  similarity using TFIDF
                        /*
                        DocumentSimilarityTFIDF similarityUsingTF = new DocumentSimilarityTFIDF(_InstancePublicationHM.get(inputAuthorID),_InstancePublicationHM.get(index));
                        HashMap <Integer, Float> indexAuthor = new HashMap<Integer, Float>();
                        indexAuthor.put(index, (float)similarityUsingTF.getCosineSimilarity());
                        */
                    }
                }

                _tfidfHM.put(inputAuthorID, similarityHM);
                System.out.println("Similarity Author:" +inputAuthorID);
                Iterator it = similarityHM.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry pairs = (Map.Entry)it.next();
                    System.out.println("other ID:" +pairs.getKey() + " Value " + pairs.getValue());
                    it.remove(); // avoids a ConcurrentModificationException
                }
            }
            
            System.out.println("FINISH TFIDF.process()");
            
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        System.out.println("FINISH PROCESSING TFIDF");
        return _tfidfHM;
    }

    private int getInstanceFromAuthorID(int authorID) {
        return _AuthorInstanceHM.get(authorID);
    }

    private int getAuthorIDFromInstanceID(int instanceID) {
        return _InstanceAuthorHM.get(instanceID);
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
//                tokens = line.split("X\t");
//                if (tokens.length != 2) {
//                    continue;
//                }
//                String publications = tokens[1];
                line = StringUtils.substringAfter(line,"X");
                _InstancePublicationHM.put(instanceID, line);
                instanceID++;
            }
            bufferReader.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}