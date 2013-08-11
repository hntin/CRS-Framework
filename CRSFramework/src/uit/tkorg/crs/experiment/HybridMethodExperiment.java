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
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import uit.tkorg.crs.common.EvaluationMetric;
import uit.tkorg.crs.common.TopNSimilarity;
import uit.tkorg.crs.graph.Graph;
import uit.tkorg.crs.method.content.TFIDF;
import uit.tkorg.crs.method.hybrid.AdaptiveHybrid;
import uit.tkorg.crs.method.hybrid.LinearHybrid;
import uit.tkorg.crs.method.link.RSS;
import uit.tkorg.utility.TextFileUtility;

/**
 *
 * @author TinHuynh
 */
public class HybridMethodExperiment {

    private Graph _graph = Graph.getInstance();
    private HashMap<Integer, String> _listAuthorRandom;
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
    boolean _isAdaptiveHybrid;
    boolean _isHybridMethodPredictionNewLink;
    boolean _isHybridMethodPredictionExistAndNewLink;
    int topN = 20;

    public HybridMethodExperiment(String inputFileLDA, String training_AuthorId_PaperIdPath, String training_PaperId_YearPath,
            String testing_AuthorId_PaperId_NFPath, String testing_AuthorId_PaperId_FFPath,
            String existing_ListAuthorPath,
            String ResultPath, boolean isLinearHybrid, boolean isAdaptiveHybrid,
            boolean isHybridMethodPredictionNewLink, boolean isHybridMethodPredictionExistAndNewLink) {

        _LDA_InputFile = inputFileLDA;
        _training_AuthorId__PaperIdPath = training_AuthorId_PaperIdPath;
        _training_PaperId_YearPath = training_PaperId_YearPath;
        _testing_AuthorId_PaperId_NFPath = testing_AuthorId_PaperId_NFPath;
        _testing_AuthorId_PaperId_FFPath = testing_AuthorId_PaperId_FFPath;
        _existing_List_AuthorPath = existing_ListAuthorPath;
        _resultPath = ResultPath;
        _isLinearHybrid = isLinearHybrid;
        _isAdaptiveHybrid = isAdaptiveHybrid;
        _isHybridMethodPredictionNewLink = isHybridMethodPredictionNewLink;
        _isHybridMethodPredictionExistAndNewLink = isHybridMethodPredictionExistAndNewLink;
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
            System.out.println("START calculating RSS for Linear Combination");
            HashMap<Integer, HashMap<Integer, Float>> rssResult = null;
            RSS rssMethod = new RSS();
            rssResult = rssMethod.process(_graph.rssGraph, _listAuthorRandom);
            System.out.println("END calculating RSS for Linear Combination");
            // </editor-fold>

            System.out.println("START Linear Hybrid");
            HashMap<Integer, HashMap<Integer, Float>> linearHybridResult = null;
            LinearHybrid linearHybridMethod = new LinearHybrid();
            linearHybridResult = linearHybridMethod.calculatingLinearHybriđ(rssResult, tfidfResult);
            if (linearHybridResult != null) {
                for (int i = 1; i <= topN; i++) {
                    System.out.println("TopN:" + topN);
                    if (_isHybridMethodPredictionNewLink) {
                        topSimilarity = TopNSimilarity.findTopNSimilarityForNewLinkOnly(i, linearHybridResult, _graph.rssGraph);
                    } else {
                        topSimilarity = TopNSimilarity.findTopNSimilarity(i, linearHybridResult);
                    }
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
            System.out.println("END Linear Hybrid");
        }
        // </editor-fold>

        //<editor-fold defaultstate="collapsed" desc="Adaptive Hybrid">
        if (_isAdaptiveHybrid) {
            System.out.println("START Adaptive Hybrid");
            HashMap<Integer, HashMap<Integer, Float>> adaptiveHybridResult = null;
            AdaptiveHybrid adaptiveHybridMethod = new AdaptiveHybrid();
            adaptiveHybridResult = adaptiveHybridMethod.process(_LDA_InputFile, _graph.rssGraph, _listAuthorRandom);

            if (adaptiveHybridResult != null) {
                for (int i = 1; i <= topN; i++) {
                    if (_isHybridMethodPredictionNewLink) {
                        topSimilarity = TopNSimilarity.findTopNSimilarityForNewLinkOnly(i, adaptiveHybridResult, _graph.rssGraph);
                    } else {
                        topSimilarity = TopNSimilarity.findTopNSimilarity(i, adaptiveHybridResult);
                    }
                    float precisionNear = EvaluationMetric.Mean_Precision_TopN(topSimilarity, _graph.nearTestingData);
                    float precisionFar = EvaluationMetric.Mean_Precision_TopN(topSimilarity, _graph.farTestingData);
                    _nfContentPredictionBuffer.append(df.format(precisionNear) + "\t");
                    _ffContentPredictionBuffer.append(df.format(precisionFar) + "\t");

//                    float recallNear = EvaluationMetric.Mean_Recall_TopN(topSimilarity, _graph.nearTestingData);
//                    float recallFar = EvaluationMetric.Mean_Recall_TopN(topSimilarity, _graph.farTestingData);
//                    _nfContentPredictionBuffer.append(df.format(recallNear) + "\t");
//                    _ffContentPredictionBuffer.append(df.format(recallFar) + "\t");
                }
            }
            System.out.println("END Adaptive Hybrid");
        }
        // </editor-fold>

        TextFileUtility.writeTextFile(_resultPath,
                _nfContentPredictionBuffer.toString() + "\n\n" + _ffContentPredictionBuffer.toString());
    }

    private void selectAuthorsForExperiment() {
        try {
            if (_listAuthorRandom == null || _listAuthorRandom.size() == 0) {
                _listAuthorRandom = new HashMap<>();
                // <editor-fold defaultstate="collapsed" desc="Load Author">
                try {
                    _listAuthorRandom = new HashMap<>();
                    FileInputStream fis = new FileInputStream(_existing_List_AuthorPath);
                    Reader reader = new InputStreamReader(fis, "UTF8");
                    BufferedReader bufferReader = new BufferedReader(reader);
                    bufferReader.readLine();
                    String line = null;
                    int authorId;
                    String[] tokens;
                    String groupLMD;
                    while ((line = bufferReader.readLine()) != null) {
                        if (!line.equals("")) {
                            tokens = line.split("\t");
                            authorId = Integer.parseInt(tokens[0]);
                            if (tokens.length <= 1) {
                                groupLMD = "";
                            } else {
                                groupLMD = tokens[1];
                            }
                            _listAuthorRandom.put(authorId, groupLMD);
                        }
                    }
                    bufferReader.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                // </editor-fold>
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
//    public static void main(String args[]) {
//        try {
//            final HybridMethodExperiment experiment = new HybridMethodExperiment(
//                    "C:\\CRS-Experiment\\Sampledata\\Input\\CRS-InputParallelLDA.txt",
//                    "C:\\CRS-Experiment\\Sampledata\\Input\\Link-Net\\[Training]AuthorId_PaperID.txt",
//                    "C:\\CRS-Experiment\\Sampledata\\Input\\Link-Net\\[Training]PaperID_Year.txt",
//                    "C:\\CRS-Experiment\\Sampledata\\Input\\Link-Net\\[NearTesting]AuthorId_PaperID.txt",
//                    "C:\\CRS-Experiment\\Sampledata\\Input\\Link-Net\\[FarTesting]AuthorId_PaperID.txt",
//                    "C:\\CRS-Experiment\\Sampledata\\Input\\SampleInput_LowMedHigh_GroupDegree.txt",
//                    "C:\\CRS-Experiment\\Sampledata\\Output\\AdaptiveHybridResult.txt",
//                    false, true,
//                    true, false);
//
//            final DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
//            Date date = new Date();
//            System.out.println("Started Time : " + dateFormat.format(date) + "\n");
//            System.out.println("START PROCESSING HYBRID METHOD ..." + "\n");
//            experiment.runHybridMethodExperiment();
//            System.out.println("END PROCESSING HYBRID METHOD ..." + "\n");
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//    }
}