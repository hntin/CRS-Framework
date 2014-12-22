package uit.tkorg.crs.experiment;

import uit.tkorg.utility.evaluation.EvaluationMetric;
import uit.tkorg.crs.model.AuthorGraph;
import uit.tkorg.crs.method.link.AdamicAdar;
import uit.tkorg.crs.method.link.Cosine;
import uit.tkorg.crs.method.link.Jaccard;
import uit.tkorg.crs.method.link.MPBVS;
import uit.tkorg.crs.method.link.MPBVSPlus;
import uit.tkorg.crs.method.link.RSS;
import uit.tkorg.crs.method.link.RSSPlus;
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
import uit.tkorg.crs.common.TopNSimilarity;
import uit.tkorg.crs.method.cbf.ParallelLDA;
import uit.tkorg.utility.common.TextFileUtility;

/**
 *
 * @author daolv
 */
public class LinkMethodExperiment {
    //<editor-fold defaultstate="collapsed" desc="Class variables">

    private AuthorGraph _graph = AuthorGraph.getInstance();
    private String _training_PaperId_AuthorIdPath;
    private String _training_PaperId_YearPath;
    private String _testing_PaperId_Year_NFPath;
    private String _testing_PaperId_Year_FFPath;
    private String _existing_List_AuthorPath;
    private HashMap<Integer, String> _listAuthorRandom;
    private ArrayList<Float> _kArray;
    private ArrayList<Integer> _yearArray;
    private String _resultPath;
    private boolean _isCosineMethod;
    private boolean _isJaccardMethod;
    private boolean _isAdarMethod;
    private boolean _isRSSMethod;
    private boolean _isRSSPlusMethod;
    private boolean _isMPVSMethod;
    private boolean _isMVVSPlusMethod;
    private boolean _isPredictionOnlyNewLink;
    private boolean _isPredictionExistAndNewLink;
    private boolean _isRandomPrediction;
    private StringBuffer _nfAdamicAdarBuffer = new StringBuffer();
    private StringBuffer _nfCosineBuffer = new StringBuffer();
    private StringBuffer _nfJaccardBuffer = new StringBuffer();
    private StringBuffer _nfRSSBuffer = new StringBuffer();
    private StringBuffer _nfRSSPlusBuffer = new StringBuffer();
    private StringBuffer _nfMPBVSBuffer = new StringBuffer();
    private StringBuffer _nfMPBVSPlusBuffer = new StringBuffer();
    private StringBuffer _ffAdamicAdarBuffer = new StringBuffer();
    private StringBuffer _ffCosineBuffer = new StringBuffer();
    private StringBuffer _ffJaccardBuffer = new StringBuffer();
    private StringBuffer _ffRSSBuffer = new StringBuffer();
    private StringBuffer _ffRSSPlusBuffer = new StringBuffer();
    private StringBuffer _ffMPBVSBuffer = new StringBuffer();
    private StringBuffer _ffMPBVSPlusBuffer = new StringBuffer();
    //</editor-fold>

    public LinkMethodExperiment(String Training_PaperId_AuthorIdPath, String Training_PaperId_YearPath,
            String Testing_PaperId_Year_NFPath, String Testing_PaperId_Year_FFPath,
            String Existing_List_AuthorPath, // empty or null if use radom author
            String K, String Year,
            String ResultPath,
            boolean isCosineMethod, boolean isJaccardMethod, boolean isAdarMethod, boolean isRSSMethod,
            boolean isRSSPlusMethod, boolean isMPVSMethod, boolean isMVVSPlusMethod,
            boolean isPredictionOnlyNewLink, boolean isPredictionExistAndNewLink,
            boolean isRandomPrediction) {

        _training_PaperId_AuthorIdPath = Training_PaperId_AuthorIdPath;
        _training_PaperId_YearPath = Training_PaperId_YearPath;
        _testing_PaperId_Year_NFPath = Testing_PaperId_Year_NFPath;
        _testing_PaperId_Year_FFPath = Testing_PaperId_Year_FFPath;
        _existing_List_AuthorPath = Existing_List_AuthorPath;
        _isRandomPrediction = isRandomPrediction;

        String str = ";";
        if (K.contains(",")) {
            str = ",";
        } else if (K.contains("-")) {
            str = "-";
        }
        String[] kArray = K.split(str);
        _kArray = new ArrayList<>();
        for (String k : kArray) {
            _kArray.add(Float.parseFloat(k));
        }

        if (Year.contains(",")) {
            str = ",";
        } else if (Year.contains("-")) {
            str = "-";
        }
        String[] yearArray = Year.split(";");
        _yearArray = new ArrayList<>();
        for (String year : yearArray) {
            _yearArray.add(Integer.parseInt(year));
        }

        _isCosineMethod = isCosineMethod;
        _isJaccardMethod = isJaccardMethod;
        _isAdarMethod = isAdarMethod;
        _isRSSMethod = isRSSMethod;
        _isRSSPlusMethod = isRSSPlusMethod;
        _isMPVSMethod = isMPVSMethod;
        _isMVVSPlusMethod = isMVVSPlusMethod;
        _isPredictionOnlyNewLink = isPredictionOnlyNewLink;
        _isPredictionExistAndNewLink = isPredictionExistAndNewLink;

        _resultPath = ResultPath;
    }
    
    private ArrayList<Integer> getRSSGraphNodeID(HashMap<Integer, HashMap<Integer, Float>> rssGraph) {
        ArrayList<Integer> nodeIDList = new ArrayList<>();
        for (int nodeID : rssGraph.keySet()) {
            nodeIDList.add(nodeID);
        }
        return nodeIDList;
    }

    private HashMap<Integer, HashMap<Integer, Float>> getTopNRandomly(int n) {
        //<InputAuthorID, <TopN, SimValue>>
        HashMap<Integer, HashMap<Integer, Float>> randomResult = new HashMap<>();
        ArrayList<Integer> nodeIDList = getRSSGraphNodeID(_graph.rssGraph);
        Random randomGenerator = new Random();

        for (int authorID : _listAuthorRandom.keySet()) {
            HashMap<Integer, Float> topNSimilarHM = new HashMap<>();
            for (int i=0; i<n; i++) {
                int sizeOfList = nodeIDList.size();
                int randomIndex = randomGenerator.nextInt(sizeOfList-1);
                int randomAuthorID = nodeIDList.get(randomIndex);
                if (!topNSimilarHM.containsKey(randomAuthorID)) {
                    topNSimilarHM.put(randomAuthorID, 0f);
                    nodeIDList.remove(randomIndex);
                }
            }
            randomResult.put(authorID, topNSimilarHM);
        }
        return randomResult;         
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

    private void bufferingExperimentResult(boolean isNFResult, String predictMethod, float value) {
        //<editor-fold defaultstate="collapsed" desc="buffering LinkMethodExperiment Result">
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

    private void writeToTxtFileForLinkMethods(float k, int year, int topN) {
        //<editor-fold defaultstate="collapsed" desc="Write result into file">
        try {
            //FileOutputStream fos = new FileOutputStream(_resultPath + "/" + String.valueOf(k) + "_" + String.valueOf(year) + ".txt");
            FileOutputStream fos = new FileOutputStream(_resultPath);

            Writer file = new OutputStreamWriter(fos, "UTF8");
            //<editor-fold defaultstate="collapsed" desc="Near future testing">

            // Creating the header of the output text file
            file.write("Near Future Testing" + "\n");
            for (int i = 1; i <= topN; i++) {
                file.write("\t" + "P@" + i);
            }
            
//            for (int i = 1; i <= topN; i++) {
//                file.write("\t" + "P@" + i + "\t" + "R@" + i);
//            }
            
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
                file.write("\t" + "P@" + i);
            }
            
//            for (int i = 1; i <= topN; i++) {
//                file.write("\t" + "P@" + i + "\t" + "R@" + i);
//            }
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
    
    public void runLinkMethodExperiment() throws Exception {
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
        int topN = 20;
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
                    cosineResult = measureCosine.process(_graph.rssGraph, _listAuthorRandom);
                }
                if (_isJaccardMethod) {
                    jaccardResult = measureJaccard.process(_graph.rssGraph, _listAuthorRandom);
                }
                if (_isAdarMethod) {
                    adamicAdarResult = measureAdamicAdar.process(_graph.rssGraph, _listAuthorRandom);
                }
                if (_isRSSMethod) {
                    rssResult = measureRSS.process(_graph.rssGraph, _listAuthorRandom);
                }
                if (_isMPVSMethod) {
                    mpbvsResult = measureMPBVS.process(_graph.rssGraph, _listAuthorRandom);
                }
                if (_isRSSPlusMethod) {
                    mpbvsplusResult = measureRSSPlus.process(_graph.rtbvsGraph, _listAuthorRandom);
                }
                if (_isMVVSPlusMethod) {
                    rssplusResult = measureMPBVSPlus.Process(_graph.rtbvsGraph, _listAuthorRandom);
                }
                //</editor-fold>

                for (int i = 1; i <= topN; i++) {
                    //<editor-fold defaultstate="collapsed" desc="Cosine">
                    if (_isPredictionOnlyNewLink) {
                        topSimilarity = TopNSimilarity.findTopNSimilarityForNewLinkOnly(i, cosineResult, _graph.rssGraph);
                    } else {
                        topSimilarity = TopNSimilarity.findTopNSimilarity(i, cosineResult);
                    }
                    
                    // Randomly select similar authors in the training net for link prediction
                    if (_isRandomPrediction) {
                        topSimilarity = getTopNRandomly(i);
                    }
                    
                    float precisionNear = EvaluationMetric.Mean_Precision_TopN(topSimilarity, _graph.nearTestingData);
                    float precisionFar = EvaluationMetric.Mean_Precision_TopN(topSimilarity, _graph.farTestingData);
                    bufferingExperimentResult(true, "Cosine", precisionNear);
                    bufferingExperimentResult(false, "Cosine", precisionFar);

//                    float recall = EvaluationMetric.Mean_Recall_TopN(topSimilarity, _graph.nearTestingData);
//                    bufferingExperimentResult(true, "Cosine", recall);
//                    recall = EvaluationMetric.Mean_Recall_TopN(topSimilarity, _graph.farTestingData);
//                    bufferingExperimentResult(false, "Cosine", recall);
//                    System.out.println("Cosine PrecisionNear - Top:" + i + "  is :" + precisionNear);
                    //</editor-fold>

                    //<editor-fold defaultstate="collapsed" desc="Jaccard">
                    if (_isPredictionOnlyNewLink) {
                        topSimilarity = TopNSimilarity.findTopNSimilarityForNewLinkOnly(i, jaccardResult, _graph.rssGraph);
                    } else {
                        topSimilarity = TopNSimilarity.findTopNSimilarity(i, jaccardResult);
                    }
                    // Randomly select similar authors in the training net for link prediction
                    if (_isRandomPrediction) {
                        topSimilarity = getTopNRandomly(topN);
                    }
                    
                    precisionNear = EvaluationMetric.Mean_Precision_TopN(topSimilarity, _graph.nearTestingData);
                    precisionFar = EvaluationMetric.Mean_Precision_TopN(topSimilarity, _graph.farTestingData);
                    bufferingExperimentResult(true, "Jaccard", precisionNear);
                    bufferingExperimentResult(false, "Jaccard", precisionFar);

//                    recall = EvaluationMetric.Mean_Recall_TopN(topSimilarity, _graph.nearTestingData);
//                    bufferingExperimentResult(true, "Jaccard", recall);
//                    recall = EvaluationMetric.Mean_Recall_TopN(topSimilarity, _graph.farTestingData);
//                    bufferingExperimentResult(false, "Jaccard", recall);
                    //</editor-fold>

                    //<editor-fold defaultstate="collapsed" desc="AdamicAdar">
                    if (_isPredictionOnlyNewLink) {
                        topSimilarity = TopNSimilarity.findTopNSimilarityForNewLinkOnly(i, adamicAdarResult, _graph.rssGraph);
                    } else {
                        topSimilarity = TopNSimilarity.findTopNSimilarity(i, adamicAdarResult);
                    }
                    // Randomly select similar authors in the training net for link prediction
                    if (_isRandomPrediction) {
                        topSimilarity = getTopNRandomly(topN);
                    }
                    
                    precisionNear = EvaluationMetric.Mean_Precision_TopN(topSimilarity, _graph.nearTestingData);
                    precisionFar = EvaluationMetric.Mean_Precision_TopN(topSimilarity, _graph.farTestingData);
                    bufferingExperimentResult(true, "AdamicAdar", precisionNear);
                    bufferingExperimentResult(false, "AdamicAdar", precisionFar);

//                    recall = EvaluationMetric.Mean_Recall_TopN(topSimilarity, _graph.nearTestingData);
//                    bufferingExperimentResult(true, "AdamicAdar", recall);
//                    recall = EvaluationMetric.Mean_Recall_TopN(topSimilarity, _graph.farTestingData);
//                    bufferingExperimentResult(false, "AdamicAdar", recall);
                    //</editor-fold>

                    //<editor-fold defaultstate="collapsed" desc="RSS">
                    if (_isPredictionOnlyNewLink) {
                        topSimilarity = TopNSimilarity.findTopNSimilarityForNewLinkOnly(i, rssResult, _graph.rssGraph);
                    } else {
                        topSimilarity = TopNSimilarity.findTopNSimilarity(i, rssResult);
                    }
                    // Randomly select similar authors in the training net for link prediction
                    if (_isRandomPrediction) {
                        topSimilarity = getTopNRandomly(topN);
                    }
                    
                    precisionNear = EvaluationMetric.Mean_Precision_TopN(topSimilarity, _graph.nearTestingData);
                    precisionFar = EvaluationMetric.Mean_Precision_TopN(topSimilarity, _graph.farTestingData);
                    bufferingExperimentResult(true, "RSS", precisionNear);
                    bufferingExperimentResult(false, "RSS", precisionFar);

//                    recall = EvaluationMetric.Mean_Recall_TopN(topSimilarity, _graph.nearTestingData);
//                    bufferingExperimentResult(true, "RSS", recall);
//                    recall = EvaluationMetric.Mean_Recall_TopN(topSimilarity, _graph.farTestingData);
//                    bufferingExperimentResult(false, "RSS", recall);
                    //</editor-fold>

                    //<editor-fold defaultstate="collapsed" desc="RSSPlus">
                    if (_isPredictionOnlyNewLink) {
                        topSimilarity = TopNSimilarity.findTopNSimilarityForNewLinkOnly(i, rssplusResult, _graph.rssGraph);
                    } else {
                        topSimilarity = TopNSimilarity.findTopNSimilarity(i, rssplusResult);
                    }
                    // Randomly select similar authors in the training net for link prediction
                    if (_isRandomPrediction) {
                        topSimilarity = getTopNRandomly(topN);
                    }
                    
                    precisionNear = EvaluationMetric.Mean_Precision_TopN(topSimilarity, _graph.nearTestingData);
                    precisionFar = EvaluationMetric.Mean_Precision_TopN(topSimilarity, _graph.farTestingData);
                    bufferingExperimentResult(true, "RSSPlus", precisionNear);
                    bufferingExperimentResult(false, "RSSPlus", precisionFar);

//                    recall = EvaluationMetric.Mean_Recall_TopN(topSimilarity, _graph.nearTestingData);
//                    bufferingExperimentResult(true, "RSSPlus", recall);
//                    recall = EvaluationMetric.Mean_Recall_TopN(topSimilarity, _graph.farTestingData);
//                    bufferingExperimentResult(false, "RSSPlus", recall);
                    //</editor-fold>

                    //<editor-fold defaultstate="collapsed" desc="MPBVS">
                    if (_isPredictionOnlyNewLink) {
                        topSimilarity = TopNSimilarity.findTopNSimilarityForNewLinkOnly(i, mpbvsResult, _graph.rssGraph);
                    } else {
                        topSimilarity = TopNSimilarity.findTopNSimilarity(i, mpbvsResult);
                    }
                    // Randomly select similar authors in the training net for link prediction
                    if (_isRandomPrediction) {
                        topSimilarity = getTopNRandomly(topN);
                    }
                    
                    precisionNear = EvaluationMetric.Mean_Precision_TopN(topSimilarity, _graph.nearTestingData);
                    precisionFar = EvaluationMetric.Mean_Precision_TopN(topSimilarity, _graph.farTestingData);
                    bufferingExperimentResult(true, "MPBVS", precisionNear);
                    bufferingExperimentResult(false, "MPBVS", precisionFar);

//                    recall = EvaluationMetric.Mean_Recall_TopN(topSimilarity, _graph.nearTestingData);
//                    bufferingExperimentResult(true, "MPBVS", recall);
//                    recall = EvaluationMetric.Mean_Recall_TopN(topSimilarity, _graph.farTestingData);
//                    bufferingExperimentResult(false, "MPBVS", recall);
                    //</editor-fold>

                    //<editor-fold defaultstate="collapsed" desc="MPBVSPlus">
                    if (_isPredictionOnlyNewLink) {
                        topSimilarity = TopNSimilarity.findTopNSimilarityForNewLinkOnly(i, mpbvsplusResult, _graph.rssGraph);
                    } else {
                        topSimilarity = TopNSimilarity.findTopNSimilarity(i, mpbvsplusResult);
                    }
                    // Randomly select similar authors in the training net for link prediction
                    if (_isRandomPrediction) {
                        topSimilarity = getTopNRandomly(topN);
                    }
                    
                    precisionNear = EvaluationMetric.Mean_Precision_TopN(topSimilarity, _graph.nearTestingData);
                    precisionFar = EvaluationMetric.Mean_Precision_TopN(topSimilarity, _graph.farTestingData);
                    bufferingExperimentResult(true, "MPBVSPlus", precisionNear);
                    bufferingExperimentResult(false, "MPBVSPlus", precisionFar);

//                    recall = EvaluationMetric.Mean_Recall_TopN(topSimilarity, _graph.nearTestingData);
//                    bufferingExperimentResult(true, "MPBVSPlus", recall);
//                    recall = EvaluationMetric.Mean_Recall_TopN(topSimilarity, _graph.farTestingData);
//                    bufferingExperimentResult(false, "MPBVSPlus", recall);
                    //</editor-fold>
                }

                writeToTxtFileForLinkMethods(k, year, topN);
            }
        }
        //</editor-fold>

    }
    
    public static void main(String args[]) {
        boolean isCosineMethod = true;
        boolean isJaccardMethod = true;
        boolean isAdarMethod = true;
        boolean isRSSMethod = true;
        boolean isRSSPlusMethod = true;
        boolean isMPVSMethod = true;
        boolean isMVVSPlusMethod = true;
        boolean isPredictionOnlyNewLink = true;
        boolean isPredictionExistAndNewLink = false;
        boolean isRandomPrediction = true;

        final LinkMethodExperiment experiment = new LinkMethodExperiment(
                "C:\\CRS-Experiment\\MAS\\Input\\Input2\\[TrainingData]AuthorID_PaperID_1995_2005.txt",
                "C:\\CRS-Experiment\\MAS\\Input\\Input2\\[TrainingData]PaperID_Year_1995_2005.txt", 
                "C:\\CRS-Experiment\\MAS\\Input\\Input2\\[TestingData]AuthorID_PaperID_2006_2008.txt",
                "C:\\CRS-Experiment\\MAS\\Input\\Input2\\[TestingData]AuthorID_PaperID_2009_2011.txt", 
                "C:\\CRS-Experiment\\MAS\\Input\\RandonAuthorListWithDegree\\ListAuthorNoAnyLink_300_WithGroup.txt",
                "0.9", 
                "2005",
                "C:\\CRS-Experiment\\MAS\\Output\\OnlyNewLink\\LinkBasedMethod_300AuthorNoAnyLink.txt",
                isCosineMethod, isJaccardMethod, isAdarMethod, isRSSMethod,
                isRSSPlusMethod, isMPVSMethod, isMVVSPlusMethod, 
                isPredictionOnlyNewLink, 
                isPredictionExistAndNewLink, 
                isRandomPrediction);
        
        try {
            experiment.runLinkMethodExperiment();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
