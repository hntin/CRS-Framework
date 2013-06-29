/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uit.tkorg.crs.method.content;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author TinHuynh
 */
public class TFIDF {
    HashMap<Integer, Integer> AuthorInstanceHM = new HashMap<>();
    HashMap<Integer, Integer> InstanceAuthorHM = new HashMap<>();
    private static HashMap<Integer, HashMap<Integer, Float>> _tfidfHM;

    public HashMap<Integer, HashMap<Integer, Float>> process(String inputFile, ArrayList<Integer> listAuthorID) {
        System.out.println("START PROCESSING TFIDF");
        try {
            
            
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        
        System.out.println("FINISH PROCESSING TFIDF");
        return _tfidfHM;
    }
    
    private int getInstanceFromAuthorID(int authorID) {
        return AuthorInstanceHM.get(authorID);
    }
    
    private int getAuthorIDFromInstanceID(int instanceID) {
        return InstanceAuthorHM.get(instanceID);
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
                AuthorInstanceHM.put(authorID, instanceID);
                InstanceAuthorHM.put(instanceID, authorID);
            }
            bufferReader.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}