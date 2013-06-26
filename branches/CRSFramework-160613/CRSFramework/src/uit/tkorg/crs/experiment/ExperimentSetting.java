/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uit.tkorg.crs.experiment;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import uit.tkorg.crs.graph.Graph;

/**
 *
 * @author TinHuynh
 */
public class ExperimentSetting {

    private Graph _graph = Graph.getInstance();
    private ArrayList<Integer> _listAuthorRandom;

    public ExperimentSetting() {
    }

    public void generateAuthorList(int numberOfAuthor, String savePath, String generatingOption) {
        try {
            _listAuthorRandom = new ArrayList<>();
            _graph.BuildingRSSGraph();

            // <AuthorID, AuthorDegree>
            HashMap<Integer, Integer> authorDegree = new HashMap<>();
            for (int authorId : _graph.rssGraph.keySet()) {
                authorDegree.put(authorId, _graph.rssGraph.get(authorId).size());
            }

            // <Degree X, Number of Authors who have the Degree X>
            HashMap<Integer, Integer> degreeCounter = new HashMap<>();
            for (int authorId : authorDegree.keySet()) {
                Integer counter = degreeCounter.get(authorDegree.get(authorId));
                if (counter == null) {
                    counter = 0;
                }
                counter++;
                degreeCounter.put(authorDegree.get(authorId), counter);
            }

            int[] degreeArray = new int[degreeCounter.keySet().size()];
            int index = 0;
            for (int degree : degreeCounter.keySet()) {
                degreeArray[index] = degree;
                index++;
            }

            //sort degree Array
            for (int i = 0; i < degreeArray.length - 1; i++) {
                for (int j = i + 1; j < degreeArray.length; j++) {
                    if (degreeArray[i] > degreeArray[j]) {
                        int temp = degreeArray[i];
                        degreeArray[i] = degreeArray[j];
                        degreeArray[j] = temp;
                    }
                }
            }

            int numberOfDegree, numberOfLowDegree, numberOfMidDegree, numberOfHighDegree;
            numberOfDegree = numberOfLowDegree = numberOfMidDegree = numberOfHighDegree = (int) degreeArray.length / 3;
            if (degreeArray.length - numberOfDegree * 3 == 1) {
                numberOfLowDegree += 1;
            } else if (degreeArray.length - numberOfDegree * 3 == 2) {
                numberOfLowDegree += 1;
                numberOfMidDegree += 1;
            }

            int maxLowDegree = degreeArray[numberOfLowDegree - 1];
            int maxMidDegree = degreeArray[numberOfLowDegree + numberOfMidDegree - 1];
            int maxHighDegree = degreeArray[numberOfLowDegree + numberOfMidDegree + numberOfHighDegree - 1];

            ArrayList<Integer> listAuthorIdInLow = new ArrayList<>();
            ArrayList<Integer> listAuthorIdInMid = new ArrayList<>();
            ArrayList<Integer> listAuthorIdInHigh = new ArrayList<>();

            ArrayList<Integer> listAuthorIdInLowForStatic = new ArrayList<>();
            ArrayList<Integer> listAuthorIdInMidForStatic = new ArrayList<>();
            ArrayList<Integer> listAuthorIdInHighForStatic = new ArrayList<>();

            // Just get authors who really exist in the future network for the experiment
            HashSet<Integer> listAuthorNearTesting = _graph.GetAllAuthorNearTest();
            HashSet<Integer> listAuthorFarTesting = _graph.GetAllAuthorFarTest();

            for (int authorId : authorDegree.keySet()) {
                if (authorDegree.get(authorId) < maxLowDegree) {
                    listAuthorIdInLowForStatic.add(authorId);
                    if (listAuthorNearTesting.contains(authorId)
                            && listAuthorFarTesting.contains(authorId)) {
                        listAuthorIdInLow.add(authorId);
                    }
                } else if (authorDegree.get(authorId) < maxMidDegree) {
                    listAuthorIdInMidForStatic.add(authorId);
                    if (listAuthorNearTesting.contains(authorId)
                            && listAuthorFarTesting.contains(authorId)) {
                        listAuthorIdInMid.add(authorId);
                    }
                } else {
                    listAuthorIdInHighForStatic.add(authorId);
                    if (listAuthorNearTesting.contains(authorId)
                            && listAuthorFarTesting.contains(authorId)) {
                        listAuthorIdInHigh.add(authorId);
                    }
                }
            }

            Random rd = new Random();
            ArrayList<Integer> listRandomAuthorIdInLow = new ArrayList<>();
            int counter = 0;
            
            if (listAuthorIdInLow.size() > 200) {
                while (counter < 100) {
                    counter++;
                    int aid;
                    while (listRandomAuthorIdInLow.contains(aid = listAuthorIdInLow.get(rd.nextInt(listAuthorIdInLow.size())))) ;
                    listRandomAuthorIdInLow.add(aid);
                }
            } else {
                int length = listAuthorIdInLow.size() > 100 ? 100 : listAuthorIdInLow.size();
                for (int i = 0; i < length; i++) {
                    listRandomAuthorIdInLow.add(listAuthorIdInLow.get(i));
                }
            }

            ArrayList<Integer> listRandomAuthorIdInMid = new ArrayList<>();
            counter = 0;
            if (listAuthorIdInMid.size() > 200) {
                while (counter < 100) {
                    counter++;
                    int aid;
                    while (listRandomAuthorIdInMid.contains(aid = listAuthorIdInMid.get(rd.nextInt(listAuthorIdInMid.size())))) ;
                    listRandomAuthorIdInMid.add(aid);
                }
            } else {
                int length = listAuthorIdInMid.size() > 100 ? 100 : listAuthorIdInMid.size();
                for (int i = 0; i < length; i++) {
                    listRandomAuthorIdInMid.add(listAuthorIdInMid.get(i));
                }
            }

            ArrayList<Integer> listRandomAuthorIdInHigh = new ArrayList<>();
            counter = 0;
            if (listAuthorIdInHigh.size() > 200) {
                while (counter < 100) {
                    counter++;
                    int aid;
                    while (listRandomAuthorIdInHigh.contains(aid = listAuthorIdInHigh.get(rd.nextInt(listAuthorIdInHigh.size())))) ;
                    listRandomAuthorIdInHigh.add(aid);
                }
            } else {
                int length = listAuthorIdInHigh.size() > 100 ? 100 : listAuthorIdInHigh.size();
                for (int i = 0; i < length; i++) {
                    listRandomAuthorIdInHigh.add(listAuthorIdInHigh.get(i));
                }
            }

            _listAuthorRandom.addAll(listRandomAuthorIdInLow);
            _listAuthorRandom.addAll(listRandomAuthorIdInMid);
            _listAuthorRandom.addAll(listRandomAuthorIdInHigh);

            FileOutputStream fos = new FileOutputStream(savePath + "/" + "ListRandomAuthor.txt");
            Writer file = new OutputStreamWriter(fos, "UTF8");
            file.write("AuthorID" + "\n");
            for (int authorId : _listAuthorRandom) {
                file.write(String.valueOf(authorId) + "\n");
            }           
            file.close();
            
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}