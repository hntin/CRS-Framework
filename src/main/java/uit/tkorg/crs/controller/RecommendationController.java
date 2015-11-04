package uit.tkorg.crs.controller;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.DecimalFormat;
import java.util.HashMap;
import uit.tkorg.crs.common.TopNSimilarity;
import uit.tkorg.crs.experiment.LinkMethodExperiment;
import uit.tkorg.crs.gui.MainFrameCRS;
import uit.tkorg.crs.method.link.AdamicAdar;
import uit.tkorg.crs.method.link.Cosine;
import uit.tkorg.crs.method.link.Jaccard;
import uit.tkorg.crs.method.link.MPRS;
import uit.tkorg.crs.method.link.RSS;
import uit.tkorg.crs.model.AuthorGraph;
import uit.tkorg.crs.utility.EvaluationMetric;

/**
 *
 * @author Huynh Ngoc Tin
 */
public class RecommendationController {

    //<editor-fold defaultstate="collapsed" desc="Member Variables">
    private AuthorGraph _authorGraph = AuthorGraph.getInstance();
    private boolean _isCosineMethod;
    private boolean _isJaccardMethod;
    private boolean _isAdarMethod;
    private boolean _isRSSMethod;
    private boolean _isRSSPlusMethod;
    private boolean _isMPRSMethod;
    private boolean _isMPRSPlusMethod;
    private boolean _isTrustBasedMethod;
    private boolean _isTFIDFMethod;
    private boolean _isLDAMethod;
    private boolean _isLinearHybrid;
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

    public RecommendationController(boolean isCosine, boolean isJaccard, boolean isAdar,
            boolean isRSS, boolean isRSSPlus, boolean isMPRS,
            boolean isMPRSPlus, boolean isTrustBased, boolean isTFIDF,
            boolean isLDA, boolean isLinearHyb, int valueTopN, int trendYear, float weightTrend, 
            boolean isPredictionOnlyNewLink, String resultFile) {
        
        _isCosineMethod = isCosine;
        _isJaccardMethod = isJaccard;
        _isAdarMethod = isAdar;
        _isRSSMethod = isRSS;
        _isRSSPlusMethod = isRSSPlus;
        _isMPRSMethod = isMPRS;
        _isMPRSPlusMethod = isMPRSPlus;
        _isTrustBasedMethod = isTrustBased;
        _isTFIDFMethod = isTFIDF;
        _isLDAMethod = isLDA;
        _isLinearHybrid = isLinearHyb;
        _topN = valueTopN;
        _year = trendYear;
        _k = weightTrend;
        
        _isPredictionOnlyNewLink = isPredictionOnlyNewLink;
        _resultFile = resultFile;
    }

    public void runLinkBasedMethods() {
        LinkMethodExperiment experimentLinkMethod = new LinkMethodExperiment(
                _isCosineMethod, _isJaccardMethod, _isAdarMethod, 
                _isRSSMethod, _isRSSPlusMethod, _isMPRSMethod, 
                _isMPRSMethod, _isTrustBasedMethod, _topN, _year, _k, 
                _isPredictionOnlyNewLink, _resultFile);
               
        experimentLinkMethod.runLinkMethodExperiment();
    }

    public void runContentBasedMethods() {

    }

    public void runHybridMethods() {

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
}
