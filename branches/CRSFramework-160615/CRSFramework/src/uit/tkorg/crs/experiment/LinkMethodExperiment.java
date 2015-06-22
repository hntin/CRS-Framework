package uit.tkorg.crs.experiment;

import uit.tkorg.utility.evaluation.EvaluationMetric;
import uit.tkorg.crs.model.AuthorGraph;
import uit.tkorg.crs.method.link.AdamicAdar;
import uit.tkorg.crs.method.link.Cosine;
import uit.tkorg.crs.method.link.Jaccard;
import uit.tkorg.crs.method.link.MPRS;
import uit.tkorg.crs.method.link.MPRSPlus;
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
import uit.tkorg.crs.method.link.RSSDoublePlus;
import uit.tkorg.utility.common.TextFileUtility;

/**
 *
 * @author Tin Huynh
 */
public class LinkMethodExperiment {
    
    //<editor-fold defaultstate="collapsed" desc="Member Variables">
    private AuthorGraph _authorGraph = AuthorGraph.getInstance();
    private boolean _isCosineMethod;
    private boolean _isJaccardMethod;
    private boolean _isAdarMethod;
    private boolean _isRSSMethod;
    private boolean _isRSSPlusMethod;
    private boolean _isMPRSMethod;
    private boolean _isMPRSPlusMethod;
    private boolean _isRSSDoublePlusMethod;
    private boolean _isPredictionOnlyNewLink;
    private int _topN;
    private float _k;
    private int _year;
    private String _resultFile;
    
    private StringBuffer _nfAdamicAdarBuffer = new StringBuffer();
    private StringBuffer _nfCosineBuffer = new StringBuffer();
    private StringBuffer _nfJaccardBuffer = new StringBuffer();
    private StringBuffer _nfRSSBuffer = new StringBuffer();
    private StringBuffer _nfRSSPlusBuffer = new StringBuffer();
    private StringBuffer _nfMPRSBuffer = new StringBuffer();
    private StringBuffer _nfMPRSPlusBuffer = new StringBuffer();
    private StringBuffer _nfRSSDoublePlusBuffer = new StringBuffer();
    
    private StringBuffer _ffAdamicAdarBuffer = new StringBuffer();
    private StringBuffer _ffCosineBuffer = new StringBuffer();
    private StringBuffer _ffJaccardBuffer = new StringBuffer();
    private StringBuffer _ffRSSBuffer = new StringBuffer();
    private StringBuffer _ffRSSPlusBuffer = new StringBuffer();
    private StringBuffer _ffMPRSBuffer = new StringBuffer();
    private StringBuffer _ffMPRSPlusBuffer = new StringBuffer();
    private StringBuffer _ffRSSDoublePlusBuffer = new StringBuffer();
    //</editor-fold>

    public LinkMethodExperiment(boolean isCosine, boolean isJaccard, boolean isAdar,
            boolean isRSS, boolean isRSSPlus, boolean isMPRS,
            boolean isMPRSPlus, boolean isRSSDoublePlus, int valueTopN, int trendYear, float weightTrend,
            boolean isPredictionOnlyNewLink, String resultFile) {

        _isPredictionOnlyNewLink = isPredictionOnlyNewLink;
        _isCosineMethod = isCosine;
        _isJaccardMethod = isJaccard;
        _isAdarMethod = isAdar;
        _isRSSMethod = isRSS;
        _isRSSPlusMethod = isRSSPlus;
        _isMPRSMethod = isMPRS;
        _isMPRSPlusMethod = isMPRSPlus;
        _isRSSDoublePlusMethod = isRSSDoublePlus; 

        _topN = valueTopN;
        _year = trendYear;
        _k = weightTrend;

        _isPredictionOnlyNewLink = isPredictionOnlyNewLink;
        _resultFile = resultFile;
    }

    public void runLinkMethodExperiment() {
        Cosine methodCosine = new Cosine();
        Jaccard methodJaccard = new Jaccard();
        AdamicAdar methodAdamicAdar = new AdamicAdar();
        MPRS methodMPRS = new MPRS();
        MPRSPlus methodMPRSPlus = new MPRSPlus();
        RSS methodRSS = new RSS();
        RSSPlus methodRSSPlus = new RSSPlus();
        RSSDoublePlus methodRSSDoublePLus = new RSSDoublePlus();

        HashMap<Integer, HashMap<Integer, Float>> topSimilarity;
        //<editor-fold defaultstate="collapsed" desc="...">
        _authorGraph.buildAllCoAuthorGraph(_k, _year);

        //<editor-fold defaultstate="collapsed" desc="Execute different methods">
        HashMap<Integer, HashMap<Integer, Float>> cosineResult = null;
        HashMap<Integer, HashMap<Integer, Float>> jaccardResult = null;
        HashMap<Integer, HashMap<Integer, Float>> adamicAdarResult = null;
        HashMap<Integer, HashMap<Integer, Float>> rssResult = null;
        HashMap<Integer, HashMap<Integer, Float>> mprsResult = null;
        HashMap<Integer, HashMap<Integer, Float>> rssPlusResult = null;
        HashMap<Integer, HashMap<Integer, Float>> mprsPlusResult = null;
        HashMap<Integer, HashMap<Integer, Float>> rssDoublePlusResult = null;

        if (_isCosineMethod) {
            System.out.println("Running Cosine ... ");
            cosineResult = methodCosine.process(_authorGraph.rssGraph, _authorGraph.listRandomAuthor);
        }
        if (_isJaccardMethod) {
            System.out.println("Running Jaccard ... ");
            jaccardResult = methodJaccard.process(_authorGraph.rssGraph, _authorGraph.listRandomAuthor);
        }
        if (_isAdarMethod) {
            System.out.println("Running A.Adar ... ");
            adamicAdarResult = methodAdamicAdar.process(_authorGraph.rssGraph, _authorGraph.listRandomAuthor);
        }
        if (_isRSSMethod) {
            System.out.println("Running RSS ... ");
            rssResult = methodRSS.process(_authorGraph.rssGraph, _authorGraph.listRandomAuthor);
        }
        if (_isMPRSMethod) {
            System.out.println("Running MPRS ... ");
            mprsResult = methodMPRS.process(_authorGraph.rssGraph, _authorGraph.listRandomAuthor);
        }
        if (_isRSSPlusMethod) {
            System.out.println("Running RSSPlus ... ");
            mprsPlusResult = methodRSSPlus.process(_authorGraph.rssPlusGraph, _authorGraph.listRandomAuthor);
        }
        if (_isMPRSPlusMethod) {
            System.out.println("Running MPRSPlus ... ");
            rssPlusResult = methodMPRSPlus.process(_authorGraph.rssPlusGraph, _authorGraph.listRandomAuthor);
        }
        if (_isRSSDoublePlusMethod) {
            System.out.println("Running RSSDoublePlus ... ");
            rssDoublePlusResult = methodRSSDoublePLus.process(_authorGraph.rssDoublePlusGraph, _authorGraph.listRandomAuthor);
        }
        
        //</editor-fold>

        for (int i = 1; i <= _topN; i++) {
            //<editor-fold defaultstate="collapsed" desc="Cosine">
            if (_isPredictionOnlyNewLink) {
                topSimilarity = TopNSimilarity.findTopNSimilarityForNewLinkOnly(i, cosineResult, _authorGraph.rssGraph);
            } else { // ExistedAndNewLink
                topSimilarity = TopNSimilarity.findTopNSimilarity(i, cosineResult);
            }

            float precisionNear = EvaluationMetric.Mean_Precision_TopN(topSimilarity, _authorGraph.nearTestingData);
            bufferingExperimentResult(true, "Cosine", precisionNear);
            //</editor-fold>
            
            //<editor-fold defaultstate="collapsed" desc="Jaccard">
            if (_isPredictionOnlyNewLink) {
                topSimilarity = TopNSimilarity.findTopNSimilarityForNewLinkOnly(i, jaccardResult, _authorGraph.rssGraph);
            } else {
                topSimilarity = TopNSimilarity.findTopNSimilarity(i, jaccardResult);
            }
            precisionNear = EvaluationMetric.Mean_Precision_TopN(topSimilarity, _authorGraph.nearTestingData);
            bufferingExperimentResult(true, "Jaccard", precisionNear);
            //</editor-fold>

            //<editor-fold defaultstate="collapsed" desc="AdamicAdar">
            if (_isPredictionOnlyNewLink) {
                topSimilarity = TopNSimilarity.findTopNSimilarityForNewLinkOnly(i, adamicAdarResult, _authorGraph.rssGraph);
            } else {
                topSimilarity = TopNSimilarity.findTopNSimilarity(i, adamicAdarResult);
            }

            precisionNear = EvaluationMetric.Mean_Precision_TopN(topSimilarity, _authorGraph.nearTestingData);
            bufferingExperimentResult(true, "AdamicAdar", precisionNear);
            //</editor-fold>

            //<editor-fold defaultstate="collapsed" desc="RSS">
            if (_isPredictionOnlyNewLink) {
                topSimilarity = TopNSimilarity.findTopNSimilarityForNewLinkOnly(i, rssResult, _authorGraph.rssGraph);
            } else {
                topSimilarity = TopNSimilarity.findTopNSimilarity(i, rssResult);
            }

            precisionNear = EvaluationMetric.Mean_Precision_TopN(topSimilarity, _authorGraph.nearTestingData);
            bufferingExperimentResult(true, "RSS", precisionNear);
            //</editor-fold>

            //<editor-fold defaultstate="collapsed" desc="RSSPlus">
            if (_isPredictionOnlyNewLink) {
                topSimilarity = TopNSimilarity.findTopNSimilarityForNewLinkOnly(i, rssPlusResult, _authorGraph.rssGraph);
            } else {
                topSimilarity = TopNSimilarity.findTopNSimilarity(i, rssPlusResult);
            }

            precisionNear = EvaluationMetric.Mean_Precision_TopN(topSimilarity, _authorGraph.nearTestingData);
            bufferingExperimentResult(true, "RSSPlus", precisionNear);
            //</editor-fold>

            //<editor-fold defaultstate="collapsed" desc="MPRS">
            if (_isPredictionOnlyNewLink) {
                topSimilarity = TopNSimilarity.findTopNSimilarityForNewLinkOnly(i, mprsResult, _authorGraph.rssGraph);
            } else {
                topSimilarity = TopNSimilarity.findTopNSimilarity(i, mprsResult);
            }
            precisionNear = EvaluationMetric.Mean_Precision_TopN(topSimilarity, _authorGraph.nearTestingData);
            bufferingExperimentResult(true, "MPRS", precisionNear);
            //</editor-fold>

            //<editor-fold defaultstate="collapsed" desc="MPRSPlus">
            if (_isPredictionOnlyNewLink) {
                topSimilarity = TopNSimilarity.findTopNSimilarityForNewLinkOnly(i, mprsPlusResult, _authorGraph.rssGraph);
            } else {
                topSimilarity = TopNSimilarity.findTopNSimilarity(i, mprsPlusResult);
            }

            precisionNear = EvaluationMetric.Mean_Precision_TopN(topSimilarity, _authorGraph.nearTestingData);
            bufferingExperimentResult(true, "MPRSPlus", precisionNear);
            //</editor-fold>
            
            //<editor-fold defaultstate="collapsed" desc="RSSDoublePlus">
            if (_isPredictionOnlyNewLink) {
                topSimilarity = TopNSimilarity.findTopNSimilarityForNewLinkOnly(i, rssDoublePlusResult, _authorGraph.rssDoublePlusGraph);
            } else {
                topSimilarity = TopNSimilarity.findTopNSimilarity(i, rssDoublePlusResult);
            }
            precisionNear = EvaluationMetric.Mean_Precision_TopN(topSimilarity, _authorGraph.nearTestingData);
            bufferingExperimentResult(true, "RSSDoublePlus", precisionNear);
            //</editor-fold>
        }

        System.out.println("Output to file ... ");
        writeToTxtFileForLinkMethods(_k, _year, _topN);
        //</editor-fold>
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

            if (predictMethod.equalsIgnoreCase("MPRSS")) {
                if (isNFResult == true) {
                    _nfMPRSBuffer.append("\t" + df.format(value));
                } else {
                    _ffMPRSBuffer.append("\t" + df.format(value));
                }
            }

            if (predictMethod.equalsIgnoreCase("MPRSPlus")) {
                if (isNFResult == true) {
                    _nfMPRSPlusBuffer.append("\t" + df.format(value));
                } else {
                    _ffMPRSPlusBuffer.append("\t" + df.format(value));
                }
            }
            
            if (predictMethod.equalsIgnoreCase("RSSDoublePlus")) {
                if (isNFResult == true) {
                    _nfMPRSPlusBuffer.append("\t" + df.format(value));
                } else {
                    _ffMPRSPlusBuffer.append("\t" + df.format(value));
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
            FileOutputStream fos = new FileOutputStream(_resultFile);

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
            file.write("MPRS" + _nfMPRSBuffer.toString() + "\n");
            file.write("MPRSPlus" + _nfMPRSPlusBuffer.toString() + "\n");
            file.write("RSSDoublePlus" + _nfRSSDoublePlusBuffer.toString() + "\n");
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
            file.write("MPRS" + _ffMPRSBuffer.toString() + "\n");
            file.write("MPRSPlus" + _ffMPRSPlusBuffer.toString() + "\n");
            file.write("RSSDoublePlus" + _ffRSSDoublePlusBuffer.toString() + "\n");
            file.write("\n");

            //</editor-fold>
            file.close();

        } catch (Exception ex) {
            ex.printStackTrace();
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

//        final LinkMethodExperiment experiment = new LinkMethodExperiment(
//                "C:\\CRS-Experiment\\MAS\\Input\\Input2\\[TrainingData]AuthorID_PaperID_1995_2005.txt",
//                "C:\\CRS-Experiment\\MAS\\Input\\Input2\\[TrainingData]PaperID_Year_1995_2005.txt",
//                "C:\\CRS-Experiment\\MAS\\Input\\Input2\\[TestingData]AuthorID_PaperID_2006_2008.txt",
//                "C:\\CRS-Experiment\\MAS\\Input\\Input2\\[TestingData]AuthorID_PaperID_2009_2011.txt",
//                "C:\\CRS-Experiment\\MAS\\Input\\RandonAuthorListWithDegree\\ListAuthorNoAnyLink_300_WithGroup.txt",
//                "0.9",
//                "2005",
//                "C:\\CRS-Experiment\\MAS\\Output\\OnlyNewLink\\LinkBasedMethod_300AuthorNoAnyLink.txt",
//                isCosineMethod, isJaccardMethod, isAdarMethod, isRSSMethod,
//                isRSSPlusMethod, isMPVSMethod, isMVVSPlusMethod,
//                isPredictionOnlyNewLink,
//                isPredictionExistAndNewLink,
//                isRandomPrediction);
//
//        try {
//            experiment.runLinkMethodExperiment();
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
    }
}
