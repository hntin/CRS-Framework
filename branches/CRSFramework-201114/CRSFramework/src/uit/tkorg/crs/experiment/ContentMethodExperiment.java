package uit.tkorg.crs.experiment;

import java.io.BufferedReader;
import java.io.File;
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
import uit.tkorg.crs.graph.AuthorGraph;
import uit.tkorg.crs.method.content.ParallelLDA;
import uit.tkorg.crs.method.content.TFIDF;
import uit.tkorg.utility.TextFileUtility;

/**
 *
 * @author TinHuynh
 */
public class ContentMethodExperiment {

    private AuthorGraph _graph = AuthorGraph.getInstance();
    private boolean _isKLDivergence = false;
    private boolean _isTFIDF = false;
    private boolean _isContentMethodPredictionNewLink;
    private boolean _isContentMethodPredictionExistAndNewLink;
    private HashMap<Integer, String> _listAuthorRandom;
    private String _LDA_InputFile;
    private String _training_AuthorId_PaperId_Path;
    private String _training_PaperId_YearPath;
    private String _testing_AuthorId_PaperId_NFPath;
    private String _testing_AuthorId_PaperId_FFPath;
    private String _existing_List_AuthorPath;
    private String _resultPath;
    private StringBuffer _nfContentPredictionBuffer = new StringBuffer();
    private StringBuffer _ffContentPredictionBuffer = new StringBuffer();
    HashMap<Integer, HashMap<Integer, Float>> topSimilarity;
    int topN = 20;

    public ContentMethodExperiment() {
    }

    public ContentMethodExperiment(String LDAInputFile, String Training_PaperId_AuthorIdPath, String Training_PaperId_YearPath,
            String Testing_PaperId_Year_NFPath, String Testing_PaperId_Year_FFPath,
            String Existing_List_AuthorPath, // empty or null if use radom author
            String ResultPath, boolean isKLDivergence, boolean isTFIDF,
            boolean isContentMethodPredictionNewLink, boolean isContentMethodPredictionExistAndNewLink) {

        _LDA_InputFile = LDAInputFile;
        _training_AuthorId_PaperId_Path = Training_PaperId_AuthorIdPath;
        _training_PaperId_YearPath = Training_PaperId_YearPath;
        _testing_AuthorId_PaperId_NFPath = Testing_PaperId_Year_NFPath;
        _testing_AuthorId_PaperId_FFPath = Testing_PaperId_Year_FFPath;
        _existing_List_AuthorPath = Existing_List_AuthorPath;

        _isKLDivergence = isKLDivergence;
        _isTFIDF = isTFIDF;

        _isContentMethodPredictionNewLink = isContentMethodPredictionNewLink;
        _isContentMethodPredictionExistAndNewLink = isContentMethodPredictionExistAndNewLink;

        _resultPath = ResultPath;
    }

    public void runContentMethodExperiment() throws Exception {
        // Loading traning and testing data
        System.out.println("START LOADING TRAINING DATA");
        _graph.LoadTrainingData(_training_AuthorId_PaperId_Path, _training_PaperId_YearPath);
        _graph.LoadTestingData(_testing_AuthorId_PaperId_NFPath, _testing_AuthorId_PaperId_FFPath);

        // Building Graphs
        _graph.BuildingRSSGraph();
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

        //<editor-fold defaultstate="collapsed" desc="Run for Topic Model - KL Divergence, out to File">
        HashMap<Integer, HashMap<Integer, Float>> klDivergenceResult = null;
        ParallelLDA ldaMethod = new ParallelLDA();
        if (_isKLDivergence) {
            klDivergenceResult = ldaMethod.process(_LDA_InputFile, _listAuthorRandom);
            if (klDivergenceResult != null) {
                for (int i = 1; i <= topN; i++) {
                    if (_isContentMethodPredictionNewLink) {
                        topSimilarity = TopNSimilarity.findTopNSimilarityForKLDivergenceForNewLinkOnly(i, klDivergenceResult, _graph.rssGraph);
                    } else {
                        topSimilarity = TopNSimilarity.findTopNSimilarityForKLDivergence(i, klDivergenceResult);
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
        }
        // </editor-fold>

        //<editor-fold defaultstate="collapsed" desc="Calculating Similarity based on TFIDF Method, out to File">
        HashMap<Integer, HashMap<Integer, Float>> tfidfResult = null;
        TFIDF tfidfMethod = new TFIDF();
        if (_isTFIDF) {
            tfidfResult = tfidfMethod.process(_LDA_InputFile, _listAuthorRandom);
            if (tfidfResult != null) {
                for (int i = 1; i <= topN; i++) {
                    if (_isContentMethodPredictionNewLink) {
                        topSimilarity = TopNSimilarity.findTopNSimilarityForNewLinkOnly(i, tfidfResult, _graph.rssGraph);
                    } else {
                        topSimilarity = TopNSimilarity.findTopNSimilarity(i, tfidfResult);
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
        }
        TextFileUtility.writeTextFile(_resultPath,
                _nfContentPredictionBuffer.toString() + "\n\n" + _ffContentPredictionBuffer.toString());
        // </editor-fold>

        //<editor-fold defaultstate="collapsed" desc="Writting Pair of LinkedAuthors (True Positive) to file for checking">
        StringBuffer truePositiveBuffer = new StringBuffer();
        truePositiveBuffer.append("TRUE POSITIVE - PAIRS OF LINKED AUTHORS IN THE TESTING NETWORK \n");
        for (int authorID1 : EvaluationMetric.authorHasLinkHM.keySet()) {
            for (int authorID2 : EvaluationMetric.authorHasLinkHM.get(authorID1)) {
                truePositiveBuffer.append("(" + authorID1 + ", " + authorID2 + ")" + "\n");
                if (_isKLDivergence) {
                    truePositiveBuffer.append(ldaMethod.getPublicationFromAuthorID(authorID1) + "\n");
                    truePositiveBuffer.append(ldaMethod.getPublicationFromAuthorID(authorID2) + "\n");
                }
                if (_isTFIDF) {
                    truePositiveBuffer.append(tfidfMethod.getPublicationFromAuthorID(authorID1) + "\n");
                    truePositiveBuffer.append(tfidfMethod.getPublicationFromAuthorID(authorID2) + "\n");
                }
                truePositiveBuffer.append("\n");
            }
        }
        String pathFile = (new File(_resultPath)).getParent();
        TextFileUtility.writeTextFile(pathFile + "\\TruePostiveCase.txt", truePositiveBuffer.toString());
        // </editor-fold>
    }

    private void selectAuthorsForExperiment() {
        try {
            if (_listAuthorRandom == null || _listAuthorRandom.size() == 0) {
                _listAuthorRandom = new HashMap<>();
                // <editor-fold defaultstate="collapsed" desc="Load Author">
                try {
                    FileInputStream fis = new FileInputStream(_existing_List_AuthorPath);
                    Reader reader = new InputStreamReader(fis, "UTF8");
                    BufferedReader bufferReader = new BufferedReader(reader);
                    bufferReader.readLine();
                    String line = null;
                    String[] tokens;
                    String groupLMD;
                    int authorId;
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
}
