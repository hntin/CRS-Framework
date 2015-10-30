/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uit.tkorg.crs.model;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import uit.tkorg.crs.common.Pair;

/**
 *
 * @author thucnt
 */
public class Sample {
    
    private ArrayList<Pair> pairOfAuthors;

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

}
