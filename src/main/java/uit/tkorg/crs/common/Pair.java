/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uit.tkorg.crs.common;

/**
 *
 * @author thucnt
 */
public class Pair {
    
    private Integer first;
    private Integer second;

    public Pair() {
        first = null;
        second = null;
    }

    public Pair(Integer first, Integer second) {
        this.first = first;
        this.second = second;
    }

    /**
     * Get the value of second
     *
     * @return the value of second
     */
    public Integer getSecond() {
        return second;
    }

    /**
     * Set the value of second
     *
     * @param second new value of second
     */
    public void setSecond(Integer second) {
        this.second = second;
    }


    /**
     * Get the value of first
     *
     * @return the value of first
     */
    public Integer getFirst() {
        return first;
    }

    /**
     * Set the value of first
     *
     * @param first new value of first
     */
    public void setFirst(Integer first) {
        this.first = first;
    }

}
