/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uit.tkorg.crs.experiment;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import uit.tkorg.crs.common.EvaluationMetric;
import uit.tkorg.crs.common.TopNSimilarity;
import uit.tkorg.crs.graph.Graph;
import uit.tkorg.crs.method.content.TFIDF;
import uit.tkorg.crs.method.hybrid.LinearHybrid;
import uit.tkorg.crs.method.link.RSS;
import uit.tkorg.utility.TextFileUtility;

/**
 *
 * @author TinHuynh
 */
public class HybridMethodExperiment {

    private Graph _graph = Graph.getInstance();
    private ArrayList<Integer> _listAuthorRandom;
    private String _LDA_InputFile;
    private String _training_AuthorId__PaperIdPath;
    private String _training_PaperId_YearPath;
    private String _testing_AuthorId_PaperId_NFPath;
    private String _testing_AuthorId_PaperId_FFPath;
    private String _existing_List_AuthorPath;
    private String _resultPath;
    private StringBuffer _nfContentPredictionBuffer = new StringBuffer();
    private StringBuffer _ffContentPredictionBuffer = new StringBuffer();
    HashMap<Integer, HashMap<Integer, Float>> topSimilarity;
    boolean _isLinearHybrid;
    int topN = 50;

    public HybridMethodExperiment(String inputFileLDA, String training_AuthorId_PaperIdPath, String training_PaperId_YearPath,
            String testing_AuthorId_PaperId_NFPath, String testing_AuthorId_PaperId_FFPath,
            String existing_ListAuthorPath,
            String ResultPath, boolean isLinearHybrid) {

        _LDA_InputFile = inputFileLDA;
        _training_AuthorId__PaperIdPath = training_AuthorId_PaperIdPath;
        _training_PaperId_YearPath = training_PaperId_YearPath;
        _testing_AuthorId_PaperId_NFPath = testing_AuthorId_PaperId_NFPath;
        _testing_AuthorId_PaperId_FFPath = testing_AuthorId_PaperId_FFPath;
        _existing_List_AuthorPath = existing_ListAuthorPath;
        _resultPath = ResultPath;
        _isLinearHybrid = isLinearHybrid;
    }

    public void runHybridMethodExperiment() throws Exception {
        // Loading traning and testing data
        System.out.println("START LOADING TRAINING DATA");
        _graph.LoadTrainingData(_training_AuthorId__PaperIdPath, _training_PaperId_YearPath);
        _graph.LoadTestingData(_testing_AuthorId_PaperId_NFPath, _testing_AuthorId_PaperId_FFPath);
        System.out.println("FINISH LOADING TRAINING DATA");

        // Building Graphs
        _graph.BuidCoAuthorGraph();
        _graph.BuildingRSSGraph();

        // Loading the list of inputed authors
        selectAuthorsForExperiment();

        DecimalFormat df = new DecimalFormat("0.#####");
        _nfContentPredictionBuffer.append("Near future \n");
        _ffContentPredictionBuffer.append("Far future \n");
        for (int i = 1; i <= topN; i++) {
            _nfContentPredictionBuffer.append("P@" + i + "\t" + "R@" + i + "\t");
            _ffContentPredictionBuffer.append("P@" + i + "\t" + "R@" + i + "\t");
        }
        _nfContentPredictionBuffer.append("\n");
        _ffContentPredictionBuffer.append("\n");

        //<editor-fold defaultstate="collapsed" desc="Linear Hybrid">
        if (_isLinearHybrid) {
            //<editor-fold defaultstate="collapsed" desc="Calculating Similarity based on TFIDF Method">
            HashMap<Integer, HashMap<Integer, Float>> tfidfResult = null;
            TFIDF tfidfMethod = new TFIDF();
            tfidfResult = tfidfMethod.process(_LDA_InputFile, _listAuthorRandom);
            // </editor-fold>

            //<editor-fold defaultstate="collapsed" desc="Calculating Similarity based on link based method - RSS">
            HashMap<Integer, HashMap<Integer, Float>> rssResult = null;
            RSS rssMethod = new RSS();
            rssResult = rssMethod.Process(_graph.rssGraph, _listAuthorRandom);
            // </editor-fold>

            HashMap<Integer, HashMap<Integer, Float>> linearHybridResult = null;
            LinearHybrid linearHybridMethod = new LinearHybrid();
            linearHybridResult = linearHybridMethod.calculatingLinearHybriđ(rssResult, tfidfResult);
            if (linearHybridResult != null) {
                for (int i = 1; i <= topN; i++) {
                    topSimilarity = TopNSimilarity.findTopNSimilarity(i, linearHybridResult);
                    float precisionNear = EvaluationMetric.Mean_Precision_TopN(topSimilarity, _graph.nearTestingData);
                    float precisionFar = EvaluationMetric.Mean_Precision_TopN(topSimilarity, _graph.farTestingData);
                    _nfContentPredictionBuffer.append(df.format(precisionNear) + "\t");
                    _ffContentPredictionBuffer.append(df.format(precisionFar) + "\t");

                    float recallNear = EvaluationMetric.Mean_Recall_TopN(topSimilarity, _graph.nearTestingData);
                    float recallFar = EvaluationMetric.Mean_Recall_TopN(topSimilarity, _graph.farTestingData);
                    _nfContentPredictionBuffer.append(df.format(recallNear) + "\t");
                    _ffContentPredictionBuffer.append(df.format(recallFar) + "\t");
                }
            }
        }
        // </editor-fold>

        TextFileUtility.writeTextFile(_resultPath,
                _nfContentPredictionBuffer.toString() + "\n\n" + _ffContentPredictionBuffer.toString());
    }

    private void selectAuthorsForExperiment() {
        try {
            if (_listAuthorRandom == null || _listAuthorRandom.size() == 0) {
                _listAuthorRandom = new ArrayList<>();
                if (_existing_List_AuthorPath == null || _existing_List_AuthorPath.isEmpty()) {
                    // <editor-fold defaultstate="collapsed" desc="Random Author">
                    HashMap<Integer, Integer> authorDegree = new HashMap<>();
                    for (int authorId : _graph.rssGraph.keySet()) {
                        authorDegree.put(authorId, _graph.rssGraph.get(authorId).size());
                    }

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
                    numberOfDegree =
                            numberOfLowDegree =
                            numberOfMidDegree =
                            numberOfHighDegree = (int) degreeArray.length / 3;
                    if (degreeArray.length - numberOfDegree * 3 == 1) {
                        numberOfLowDegree += 1;
                    } else if (degreeArray.length - numberOfDegree * 3 == 2) {
                        numberOfLowDegree += 1;
                        numberOfMidDegree += 1;
                    }

                    int maxLowDegree = degreeArray[numberOfLowDegree - 1];
                    int maxMidDegree = degreeArray[numberOfLowDegree + numberOfMidDegree - 1];
                    int maxHighDegree = degreeArray[numberOfLowDegree + numberOfMidDegree + numberOfHighDegree - 1];;

                    ArrayList<Integer> listAuthorIdInLow = new ArrayList<>();
                    ArrayList<Integer> listAuthorIdInMid = new ArrayList<>();
                    ArrayList<Integer> listAuthorIdInHigh = new ArrayList<>();

                    ArrayList<Integer> listAuthorIdInLowForStatic = new ArrayList<>();
                    ArrayList<Integer> listAuthorIdInMidForStatic = new ArrayList<>();
                    ArrayList<Integer> listAuthorIdInHighForStatic = new ArrayList<>();

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

                    FileOutputStream fos = new FileOutputStream(_resultPath + "/" + "ListRandomAuthor.txt");
                    Writer file = new OutputStreamWriter(fos, "UTF8");
                    file.write("AuthorID" + "\n");
                    for (int authorId : _listAuthorRandom) {
                        file.write(String.valueOf(authorId) + "\n");
                    }
                    file.close();
                    // </editor-fold>
                } else {
                    // <editor-fold defaultstate="collapsed" desc="Load Author">
                    try {
                        _listAuthorRandom = new ArrayList<>();
                        FileInputStream fis = new FileInputStream(_existing_List_AuthorPath);
                        Reader reader = new InputStreamReader(fis, "UTF8");
                        BufferedReader bufferReader = new BufferedReader(reader);
                        bufferReader.readLine();
                        String line = null;
                        int authorId;
                        while ((line = bufferReader.readLine()) != null) {
                            authorId = Integer.parseInt(line);
                            _listAuthorRandom.add(authorId);
                        }
                        bufferReader.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                // </editor-fold>
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
