/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uit.tkorg.crs.model;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.regex.Pattern;

/**
 *
 * @author thucnt
 */
public class Sample {

    private ArrayList<Pair> pairOfAuthors;

    private Sample() {
    }

    /**
     * Get the value of pairOfAuthor
     *
     * @return the value of pairOfAuthor
     */
    public ArrayList<Pair> getPairOfAuthor() {
        return pairOfAuthors;
    }

    /**
     * Set the value of pairOfAuthor
     *
     * @param pairOfAuthor new value of pairOfAuthor
     */
    public void setPairOfAuthor(ArrayList<Pair> pairOfAuthor) {
        this.pairOfAuthors = pairOfAuthor;
    }

    /**
     * readSampleFile
     *
     * @param sampleFile
     * @return
     */
    public static Sample readSampleFile(String sampleFile) {
        final String REGEX = "\\D";
        Pattern p = Pattern.compile(REGEX);
        ArrayList<Pair> listOfPairs;
        listOfPairs = new ArrayList<Pair>();

        try {
            FileInputStream fis = new FileInputStream(sampleFile);
            Reader reader = new InputStreamReader(fis, "UTF8");
            BufferedReader bufferReader = new BufferedReader(reader);
            bufferReader.readLine(); // skip the first line
            String line = null;

            while ((line = bufferReader.readLine()) != null) {
                String[] elements = p.split(line.trim());

                if (elements.length > 3 || elements.length < 2) {
                    continue;
                }
                int author1 = Integer.parseInt(elements[1]);
                int author2 = Integer.parseInt(elements[2]);
                Pair pair = new Pair(new Integer(author1), new Integer(author2));
                listOfPairs.add(pair);
            }
            bufferReader.close();
            fis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Sample s = new Sample();
        s.setPairOfAuthor(listOfPairs);
        return s;
    }

    /**
     * read author list from posivtive/negative sample
     *
     * @return list of author existing in the positive/negative sample
     */
    public LinkedHashSet<Integer> readAllAuthorIDList() {

        LinkedHashSet<Integer> authorList = new LinkedHashSet<Integer>();

        for (int i = 0; i < this.pairOfAuthors.size(); i++) {
            Pair p = pairOfAuthors.get(i);
            Integer idAuthor1 = (Integer) p.getFirst();
            Integer idAuthor2 = (Integer) p.getSecond();
            authorList.add(idAuthor1);
            authorList.add(idAuthor2);
        }
        return authorList;
    }

    /**
     * readAllFirstAuthorID
     * @return 
     */
    public HashMap<Integer, String> readAllFirstAuthorID() {
        HashMap<Integer, String> firstAuthorIDList = new HashMap<Integer, String>();

        for (int i = 0; i < this.pairOfAuthors.size(); i++) {
            Pair p = pairOfAuthors.get(i);
            Integer firstAuthorID = (Integer) p.getFirst();
            if (!firstAuthorIDList.containsKey(firstAuthorID))
                firstAuthorIDList.put(firstAuthorID, "J"); // J means Junior
        }
        return firstAuthorIDList;
    }
    
    /**
     * readAllFirstAuthorID
     * @return 
     */
    public LinkedHashSet<Integer> readAllSecondAuthorID() {
        LinkedHashSet<Integer> secondAuthorIDList = new LinkedHashSet<Integer>();

        for (int i = 0; i < this.pairOfAuthors.size(); i++) {
            Pair p = pairOfAuthors.get(i);
            Integer secondAuthorID = (Integer) p.getSecond();
            secondAuthorIDList.add(secondAuthorID);
        }
        return secondAuthorIDList;
    }
}
