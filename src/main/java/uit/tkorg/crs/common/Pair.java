/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uit.tkorg.crs.common;

import java.util.Objects;

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
    
    public boolean equals(Object obj){
        boolean result = false;
        if (this == obj)
            result = true;
        else if ((obj == null) || (obj.getClass() != this.getClass()))
            result = false;
        else{
            Pair p = (Pair)obj;
            if ((p.first.equals(this.first)) && (p.second.equals(this.second)))
                result = true;
            else
                result = false;
        }
        
        return result;
    }
    
    public int hashCode(){
        return Objects.hash(this.first,this.second);
    }

}
