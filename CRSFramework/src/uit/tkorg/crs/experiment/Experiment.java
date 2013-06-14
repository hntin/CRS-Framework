package uit.tkorg.crs.experiment;

import uit.tkorg.crs.evaluation.EvaluationMeasure;
import uit.tkorg.crs.graph.Graph;
import uit.tkorg.crs.method.AdamicAdar;
import uit.tkorg.crs.method.Cosine;
import uit.tkorg.crs.method.Jaccard;
import uit.tkorg.crs.method.MPBVS;
import uit.tkorg.crs.method.MPBVSPlus;
import uit.tkorg.crs.method.RSS;
import uit.tkorg.crs.method.RSSPlus;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.Vector;
import uit.tkorg.crs.method.ParallelLDA;

/**
 *
 * @author daolv
 */
public class Experiment {

    private Graph _graph = Graph.getInstance();
    private String _training_PaperId_AuthorIdPath;
    private String _training_PaperId_YearPath;
    private String _testing_PaperId_Year_NFPath;
    private String _testing_PaperId_Year_FFPath;
    private String _existing_List_AuthorPath;
    private ArrayList<Integer> _listAuthorRandom;
    private ArrayList<Float> _kArray;
    private ArrayList<Integer> _yearArray;
    private ArrayList<Integer> _topNArray;
    private String _resultPath;
    private String _training_LDA_InputFile;
    private int _numberOfRandomAuthor;
    private boolean _isCosineMethod;
    private boolean _isJaccardMethod;
    private boolean _isAdarMethod;
    private boolean _isRSSMethod;
    private boolean _isRSSPlusMethod;
    private boolean _isMPVSMethod;
    private boolean _isMVVSPlusMethod;
    private boolean _isKLDivergence;
    private StringBuffer _nfAdamicAdarBuffer;
    private StringBuffer _nfCosineBuffer;
    private StringBuffer _nfJaccardBuffer;
    private StringBuffer _nfRSSBuffer;
    private StringBuffer _nfRSSPlusBuffer;
    private StringBuffer _nfMPBVSBuffer;
    private StringBuffer _nfMPBVSPlusBuffer;
    private StringBuffer _ffAdamicAdarBuffer;
    private StringBuffer _ffCosineBuffer;
    private StringBuffer _ffJaccardBuffer;
    private StringBuffer _ffRSSBuffer;
    private StringBuffer _ffRSSPlusBuffer;
    private StringBuffer _ffMPBVSBuffer;
    private StringBuffer _ffMPBVSPlusBuffer;

    public Experiment(String Training_PaperId_AuthorIdPath, String Training_PaperId_YearPath,
            String Testing_PaperId_Year_NFPath, String Testing_PaperId_Year_FFPath,
            String Existing_List_AuthorPath, // empty or null if use radom author
            String K, String Year, String TopNList,
            String ResultPath,
            String Training_LDA_InputFile,
            int numberOfRandomAuthor,
            boolean isCosineMethod, boolean isJaccardMethod, boolean isAdarMethod, boolean isRSSMethod,
            boolean isRSSPlusMethod, boolean isMPVSMethod, boolean isMVVSPlusMethod, boolean isKLDivergence) {

        _training_PaperId_AuthorIdPath = Training_PaperId_AuthorIdPath;
        _training_PaperId_YearPath = Training_PaperId_YearPath;
        _testing_PaperId_Year_NFPath = Testing_PaperId_Year_NFPath;
        _testing_PaperId_Year_FFPath = Testing_PaperId_Year_FFPath;
        _existing_List_AuthorPath = Existing_List_AuthorPath;

        String[] kArray = K.split(";");
        _kArray = new ArrayList<>();
        for (String k : kArray) {
            _kArray.add(Float.parseFloat(k));
        }

        String[] yearArray = Year.split(";");
        _yearArray = new ArrayList<>();
        for (String year : yearArray) {
            _yearArray.add(Integer.parseInt(year));
        }

        String[] topNArray = TopNList.split(";");
        _topNArray = new ArrayList<>();
        for (String topN : topNArray) {
            _topNArray.add(Integer.parseInt(topN));
        }

        _training_LDA_InputFile = Training_LDA_InputFile;
        _numberOfRandomAuthor = numberOfRandomAuthor;
        _isCosineMethod = isCosineMethod;
        _isJaccardMethod = isJaccardMethod;
        _isAdarMethod = isAdarMethod;
        _isRSSMethod = isRSSMethod;
        _isRSSPlusMethod = isRSSPlusMethod;
        _isMPVSMethod = isMPVSMethod;
        _isMVVSPlusMethod = isMVVSPlusMethod;
        _isKLDivergence = isKLDivergence;

        _resultPath = ResultPath;
    }

    public void Run() throws Exception {
        _graph.LoadTrainingData(_training_PaperId_AuthorIdPath, _training_PaperId_YearPath);
        _graph.LoadTestingData(_testing_PaperId_Year_NFPath, _testing_PaperId_Year_FFPath);

        AdamicAdar measureAdamicAdar = new AdamicAdar();
        Cosine measureCosine = new Cosine();
        Jaccard measureJaccard = new Jaccard();
        MPBVS measureMPBVS = new MPBVS();
        MPBVSPlus measureMPBVSPlus = new MPBVSPlus();
        RSS measureRSS = new RSS();
        RSSPlus measureRSSPlus = new RSSPlus();

        HashMap<Integer, HashMap<Integer, Float>> topSimilarity;
        //<editor-fold defaultstate="collapsed" desc="Run for different K and Year">
        for (int year : _yearArray) {
            for (float k : _kArray) {
                _graph.BuildAllGraph(k, year);
                selectAuthorsForExperiment();
                
                //<editor-fold defaultstate="collapsed" desc="Execute different methods">
                HashMap<Integer, HashMap<Integer, Float>> cosineResult = null;
                HashMap<Integer, HashMap<Integer, Float>> jaccardResult = null;
                HashMap<Integer, HashMap<Integer, Float>> adamicAdarResult = null;
                HashMap<Integer, HashMap<Integer, Float>> rssResult = null;
                HashMap<Integer, HashMap<Integer, Float>> mpbvsResult = null;
                HashMap<Integer, HashMap<Integer, Float>> rssplusResult = null;
                HashMap<Integer, HashMap<Integer, Float>> mpbvsplusResult = null;
                if (_isCosineMethod) {
                    cosineResult = measureCosine.Process(_graph.rssGraph, _listAuthorRandom);
                }
                if (_isJaccardMethod) {
                    jaccardResult = measureJaccard.Process(_graph.rssGraph, _listAuthorRandom);
                }
                if (_isAdarMethod) {
                    adamicAdarResult = measureAdamicAdar.Process(_graph.rssGraph, _listAuthorRandom);
                }
                if (_isRSSMethod) {
                    rssResult = measureRSS.Process(_graph.rssGraph, _listAuthorRandom);
                }
                if (_isMPVSMethod) {
                    mpbvsResult = measureMPBVS.Process(_graph.rssGraph, _listAuthorRandom);
                }
                if (_isRSSPlusMethod) {
                    mpbvsplusResult = measureRSSPlus.Process(_graph.rtbvsGraph, _listAuthorRandom);
                }
                if (_isMVVSPlusMethod) {
                    rssplusResult = measureMPBVSPlus.Process(_graph.rtbvsGraph, _listAuthorRandom);
                }
                //</editor-fold>

                //<editor-fold defaultstate="collapsed" desc="Init buffer for writing result to Txt file">
                _nfAdamicAdarBuffer = new StringBuffer();
                _nfCosineBuffer = new StringBuffer();
                _nfJaccardBuffer = new StringBuffer();
                _nfRSSBuffer = new StringBuffer();
                _nfRSSPlusBuffer = new StringBuffer();
                _nfMPBVSBuffer = new StringBuffer();
                _nfMPBVSPlusBuffer = new StringBuffer();
                _ffAdamicAdarBuffer = new StringBuffer();
                _ffCosineBuffer = new StringBuffer();
                _ffJaccardBuffer = new StringBuffer();
                _ffRSSBuffer = new StringBuffer();
                _ffRSSPlusBuffer = new StringBuffer();
                _ffMPBVSBuffer = new StringBuffer();
                _ffMPBVSPlusBuffer = new StringBuffer();
                //</editor-fold>

                int topN = 50;
                for (int i = 1; i <= topN; i++) {
                    //<editor-fold defaultstate="collapsed" desc="Cosine">
                    topSimilarity = FindTopNSimilarity(i, cosineResult);
                    float precisionNear = EvaluationMeasure.Mean_Precision_TopN(topSimilarity, _graph.nearTestingData);
                    float precisionFar = EvaluationMeasure.Mean_Precision_TopN(topSimilarity, _graph.farTestingData);
                    bufferingExperimentResult(true, "Cosine", precisionNear);
                    bufferingExperimentResult(false, "Cosine", precisionFar);

                    float recall = EvaluationMeasure.Mean_Recall_TopN(topSimilarity, _graph.nearTestingData);
                    bufferingExperimentResult(true, "Cosine", recall);
                    recall = EvaluationMeasure.Mean_Recall_TopN(topSimilarity, _graph.farTestingData);
                    bufferingExperimentResult(false, "Cosine", recall);
                    //</editor-fold>

                    //<editor-fold defaultstate="collapsed" desc="Jaccard">
                    topSimilarity = FindTopNSimilarity(i, jaccardResult);
                    precisionNear = EvaluationMeasure.Mean_Precision_TopN(topSimilarity, _graph.nearTestingData);
                    precisionFar = EvaluationMeasure.Mean_Precision_TopN(topSimilarity, _graph.farTestingData);
                    bufferingExperimentResult(true, "Jaccard", precisionNear);
                    bufferingExperimentResult(false, "Jaccard", precisionFar);

                    recall = EvaluationMeasure.Mean_Recall_TopN(topSimilarity, _graph.nearTestingData);
                    bufferingExperimentResult(true, "Jaccard", recall);
                    recall = EvaluationMeasure.Mean_Recall_TopN(topSimilarity, _graph.farTestingData);
                    bufferingExperimentResult(false, "Jaccard", recall);
                    //</editor-fold>

                    //<editor-fold defaultstate="collapsed" desc="AdamicAdar">
                    topSimilarity = FindTopNSimilarity(i, adamicAdarResult);
                    precisionNear = EvaluationMeasure.Mean_Precision_TopN(topSimilarity, _graph.nearTestingData);
                    precisionFar = EvaluationMeasure.Mean_Precision_TopN(topSimilarity, _graph.farTestingData);
                    bufferingExperimentResult(true, "AdamicAdar", precisionNear);
                    bufferingExperimentResult(false, "AdamicAdar", precisionFar);

                    recall = EvaluationMeasure.Mean_Recall_TopN(topSimilarity, _graph.nearTestingData);
                    bufferingExperimentResult(true, "AdamicAdar", recall);
                    recall = EvaluationMeasure.Mean_Recall_TopN(topSimilarity, _graph.farTestingData);
                    bufferingExperimentResult(false, "AdamicAdar", recall);
                    //</editor-fold>
                    
                    //<editor-fold defaultstate="collapsed" desc="RSS">
                    topSimilarity = FindTopNSimilarity(i, rssResult);
                    precisionNear = EvaluationMeasure.Mean_Precision_TopN(topSimilarity, _graph.nearTestingData);
                    precisionFar = EvaluationMeasure.Mean_Precision_TopN(topSimilarity, _graph.farTestingData);
                    bufferingExperimentResult(true, "RSS", precisionNear);
                    bufferingExperimentResult(false, "RSS", precisionFar);

                    recall = EvaluationMeasure.Mean_Recall_TopN(topSimilarity, _graph.nearTestingData);
                    bufferingExperimentResult(true, "RSS", recall);
                    recall = EvaluationMeasure.Mean_Recall_TopN(topSimilarity, _graph.farTestingData);
                    bufferingExperimentResult(false, "RSS", recall);
                    //</editor-fold>

                    //<editor-fold defaultstate="collapsed" desc="RSSPlus">
                    topSimilarity = FindTopNSimilarity(i, rssplusResult);
                    precisionNear = EvaluationMeasure.Mean_Precision_TopN(topSimilarity, _graph.nearTestingData);
                    precisionFar = EvaluationMeasure.Mean_Precision_TopN(topSimilarity, _graph.farTestingData);
                    bufferingExperimentResult(true, "RSSPlus", precisionNear);
                    bufferingExperimentResult(false, "RSSPlus", precisionFar);

                    recall = EvaluationMeasure.Mean_Recall_TopN(topSimilarity, _graph.nearTestingData);
                    bufferingExperimentResult(true, "RSSPlus", recall);
                    recall = EvaluationMeasure.Mean_Recall_TopN(topSimilarity, _graph.farTestingData);
                    bufferingExperimentResult(false, "RSSPlus", recall);
                    //</editor-fold>

                    //<editor-fold defaultstate="collapsed" desc="MPBVS">
                    topSimilarity = FindTopNSimilarity(i, mpbvsResult);
                    precisionNear = EvaluationMeasure.Mean_Precision_TopN(topSimilarity, _graph.nearTestingData);
                    precisionFar = EvaluationMeasure.Mean_Precision_TopN(topSimilarity, _graph.farTestingData);
                    bufferingExperimentResult(true, "MPBVS", precisionNear);
                    bufferingExperimentResult(false, "MPBVS", precisionFar);

                    recall = EvaluationMeasure.Mean_Recall_TopN(topSimilarity, _graph.nearTestingData);
                    bufferingExperimentResult(true, "MPBVS", recall);
                    recall = EvaluationMeasure.Mean_Recall_TopN(topSimilarity, _graph.farTestingData);
                    bufferingExperimentResult(false, "MPBVS", recall);
                    //</editor-fold>

                    //<editor-fold defaultstate="collapsed" desc="MPBVSPlus">
                    topSimilarity = FindTopNSimilarity(i, mpbvsplusResult);
                    precisionNear = EvaluationMeasure.Mean_Precision_TopN(topSimilarity, _graph.nearTestingData);
                    precisionFar = EvaluationMeasure.Mean_Precision_TopN(topSimilarity, _graph.farTestingData);
                    bufferingExperimentResult(true, "MPBVSPlus", precisionNear);
                    bufferingExperimentResult(false, "MPBVSPlus", precisionFar);

                    recall = EvaluationMeasure.Mean_Recall_TopN(topSimilarity, _graph.nearTestingData);
                    bufferingExperimentResult(true, "MPBVSPlus", recall);
                    recall = EvaluationMeasure.Mean_Recall_TopN(topSimilarity, _graph.farTestingData);
                    bufferingExperimentResult(false, "MPBVSPlus", recall);
                    //</editor-fold>
                }

                writeToTxtFile(k, year, topN);
            }
        }
        //</editor-fold>
        
        //<editor-fold defaultstate="collapsed" desc="Run for Topic Model - KL Divergence">
        if (_isKLDivergence) {
            ParallelLDA ldaParallelTool = new ParallelLDA();
            HashMap<Integer, HashMap<Integer, Float>> klDivergenceOfInstances = 
            ldaParallelTool.process(_training_LDA_InputFile);
        }
        //</editor-fold>
    }

    private void selectAuthorsForExperiment() throws Exception{
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
                }
            }
            // </editor-fold>
        }
    }

    private HashMap<Integer, HashMap<Integer, Float>> FindTopNSimilarity(int topN, HashMap<Integer, HashMap<Integer, Float>> data) {
        HashMap<Integer, HashMap<Integer, Float>> result = new HashMap<>();
        for (int authorId : data.keySet()) {
            HashMap<Integer, Float> listAuthorRecommend = new HashMap<>();
            for (int idRecommend : data.get(authorId).keySet()) {
                listAuthorRecommend.put(idRecommend, data.get(authorId).get(idRecommend));
                if (listAuthorRecommend.size() > topN) {
                    int keyMinValue = 0;
                    float minValue = Integer.MAX_VALUE;
                    for (int id : listAuthorRecommend.keySet()) {
                        if (listAuthorRecommend.get(id) < minValue) {
                            minValue = listAuthorRecommend.get(id);
                            keyMinValue = id;
                        }
                    }

                    listAuthorRecommend.remove(keyMinValue);
                }
            }
            result.put(authorId, listAuthorRecommend);
        }
        return result;
    }

    private void bufferingExperimentResult(boolean isNFResult, String predictMethod, float value) {
        //<editor-fold defaultstate="collapsed" desc="buffering Experiment Result">
        try {
            DecimalFormat df = new DecimalFormat("0.#####");

            if (predictMethod.equalsIgnoreCase("AdamicAdar")) {
                if (isNFResult == true) {
                    _nfAdamicAdarBuffer.append("\t" + df.format(value));
                } else {
                    _ffAdamicAdarBuffer.append("\t" + df.format(value));
                }
            }

            if (predictMethod.equalsIgnoreCase("Cosine")) {
                if (isNFResult == true) {
                    _nfCosineBuffer.append("\t" + df.format(value));
                } else {
                    _ffCosineBuffer.append("\t" + df.format(value));
                }
            }

            if (predictMethod.equalsIgnoreCase("Jaccard")) {
                if (isNFResult == true) {
                    _nfJaccardBuffer.append("\t" + df.format(value));
                } else {
                    _ffJaccardBuffer.append("\t" + df.format(value));
                }
            }

            if (predictMethod.equalsIgnoreCase("RSS")) {
                if (isNFResult == true) {
                    _nfRSSBuffer.append("\t" + df.format(value));
                } else {
                    _ffRSSBuffer.append("\t" + df.format(value));
                }
            }

            if (predictMethod.equalsIgnoreCase("RSSPlus")) {
                if (isNFResult == true) {
                    _nfRSSPlusBuffer.append("\t" + df.format(value));
                } else {
                    _ffRSSPlusBuffer.append("\t" + df.format(value));
                }
            }

            if (predictMethod.equalsIgnoreCase("MPBVS")) {
                if (isNFResult == true) {
                    _nfMPBVSBuffer.append("\t" + df.format(value));
                } else {
                    _ffMPBVSBuffer.append("\t" + df.format(value));
                }
            }

            if (predictMethod.equalsIgnoreCase("MPBVSPlus")) {
                if (isNFResult == true) {
                    _nfMPBVSPlusBuffer.append("\t" + df.format(value));
                } else {
                    _ffMPBVSPlusBuffer.append("\t" + df.format(value));
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
//</editor-fold>  
    }

    private void writeToTxtFile(float k, int year, int topN) {
        //<editor-fold defaultstate="collapsed" desc="Write result into file">
        try {
            FileOutputStream fos = new FileOutputStream(_resultPath + "/" + String.valueOf(k) + "_" + String.valueOf(year) + ".txt");
            Writer file = new OutputStreamWriter(fos, "UTF8");
            //<editor-fold defaultstate="collapsed" desc="Near future testing">

            // Creating the header of the output text file
            file.write("Near Future Testing" + "\n");
            for (int i = 1; i <= topN; i++) {
                file.write("\t" + "P@" + i + "\t" + "R@" + i);
            }
            //file.write("\t" + "Recall@" + topN);
            //file.write("\t" + "MAP");
            file.write("\n");

            file.write("AdamicAdar" + _nfAdamicAdarBuffer.toString() + "\n");
            file.write("Cosine" + _nfCosineBuffer.toString() + "\n");
            file.write("Jaccard" + _nfJaccardBuffer.toString() + "\n");
            file.write("RSS" + _nfRSSBuffer.toString() + "\n");
            file.write("RSSPlus" + _nfRSSPlusBuffer.toString() + "\n");
            file.write("MPBVS" + _nfMPBVSBuffer.toString() + "\n");
            file.write("MPBVSPlus" + _nfMPBVSPlusBuffer.toString() + "\n");
            file.write("\n");
            //</editor-fold>     

            //<editor-fold defaultstate="collapsed" desc="Far future testing">
            file.write("Far Future Testing" + "\n");
            for (int i = 1; i <= topN; i++) {
                file.write("\t" + "P@" + i + "\t" + "R@" + i);
            }
            //file.write("\t" + "Recall@" + topN);
            //file.write("\t" + "MAP");
            file.write("\n");

            file.write("AdamicAdar" + _ffAdamicAdarBuffer.toString() + "\n");
            file.write("Cosine" + _ffCosineBuffer.toString() + "\n");
            file.write("Jaccard" + _ffJaccardBuffer.toString() + "\n");
            file.write("RSS" + _ffRSSBuffer.toString() + "\n");
            file.write("RSSPlus" + _ffRSSPlusBuffer.toString() + "\n");
            file.write("MPBVS" + _ffMPBVSBuffer.toString() + "\n");
            file.write("MPBVSPlus" + _ffMPBVSPlusBuffer.toString() + "\n");
            file.write("\n");

            //</editor-fold>
            file.close();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        //</editor-fold>
    }
}
