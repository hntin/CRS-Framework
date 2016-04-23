/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package weka.visualize;
import java.util.*;

class ArraySort {

    public static Integer[][] mysort(Integer[][] ar) {
        Arrays.sort(ar, new Comparator<Integer[]>() {
            @Override
            public int compare(Integer[] int1, Integer[] int2) {
                Integer numOfKeys1 = int1[1];
                Integer numOfKeys2 = int2[1];
                return numOfKeys1.compareTo(numOfKeys2);
            }
        });
        return ar;
    }

    public static void main(String[] s) {
        Integer[][] myarr = {{0, 10}, {1, 9}, {2, 9}, {3, 9}, {4, 15}, {5, 10}, {6, 4}};

        for (Integer[] i : myarr) {
            System.out.println(i[0] + "," + i[1]);
        }

        myarr = mysort(myarr);
        System.out.println("After");

        for (Integer[] i : myarr) {
            System.out.println(i[0] + "," + i[1]);
        }
    }
}
