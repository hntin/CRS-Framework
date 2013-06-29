/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uit.tkorg.crs.method.content;

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
}