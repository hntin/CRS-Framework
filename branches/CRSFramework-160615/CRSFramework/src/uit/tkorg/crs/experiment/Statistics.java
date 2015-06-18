/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uit.tkorg.crs.experiment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import uit.tkorg.crs.model.AuthorGraph;
import uit.tkorg.utility.common.TextFileUtility;

/**
 *
 * @author TinHuynh
 */
public class Statistics {
//    private AuthorGraph _graph = AuthorGraph.getInstance();
//    private String _file_TraingAuthorIDPaperID;
//    private String _file_TraingPaperID_Year;
//    private String _file_NF_AuthorIDPaperID;
//    private String _file_FF_AuthorIDPaperID;
//    private String _file_DregreeDistribution;
//    
//    private HashMap<Integer, Integer> _authorDegree = new HashMap<>(); // <AuthorID, AuthorDegree>
//    private HashMap<Integer, String> _authorGroup = new HashMap<>();
//    HashMap<Integer, Integer> _degreeCounter;
//    private ArrayList<Integer> _listAuthorIdInLow = new ArrayList<>();
//    private ArrayList<Integer> _listAuthorIdInMid = new ArrayList<>();
//    private ArrayList<Integer> _listAuthorIdInHigh = new ArrayList<>();
//    private HashSet<Integer> _listAuthorNearTesting;
//    private HashSet<Integer> _listAuthorFarTesting;
//    int _maxLowDegree;
//    int _maxMidDegree;
//    int _maxHighDegree;
//    
//    public Statistics(String file_TraingAuthorIDPaperID, String file_TraingPaperID_Year,
//            String file_NF_AuthorIDPaperID, String file_FF_AuthorIDPaperID, 
//            String file_DregreeDistribution) {
//
//        _file_TraingAuthorIDPaperID = file_TraingAuthorIDPaperID;
//        _file_TraingPaperID_Year = file_TraingPaperID_Year;
//        _file_NF_AuthorIDPaperID = file_NF_AuthorIDPaperID;
//        _file_FF_AuthorIDPaperID = file_FF_AuthorIDPaperID;
//        _file_DregreeDistribution = file_DregreeDistribution;
//    }
//    
//    private void loadDataFromTextFile() {
//        try {
//            _graph.LoadTrainingData(_file_TraingAuthorIDPaperID, _file_TraingPaperID_Year);
//            _graph.LoadTestingData(_file_NF_AuthorIDPaperID, _file_FF_AuthorIDPaperID);
//            _graph.BuildCoAuthorGraph();
//            _graph.BuildingRSSGraph();
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//    }
//
//    private void clusterLowMedHigh() {
//        for (int authorId : _graph.rssGraph.keySet()) {
//            _authorDegree.put(authorId, _graph.rssGraph.get(authorId).size());
//        }
//
//        // <Degree X, Number of Authors who have the Degree X>
//        _degreeCounter = new HashMap<>();
//        for (int authorId : _authorDegree.keySet()) {
//            Integer counter = _degreeCounter.get(_authorDegree.get(authorId));
//            if (counter == null) {
//                counter = 0;
//            }
//            counter++;
//            _degreeCounter.put(_authorDegree.get(authorId), counter);
//        }
//
//        // degreeArray index
//        int[] degreeArray = new int[_degreeCounter.keySet().size()];
//        int index = 0;
//        for (int degree : _degreeCounter.keySet()) {
//            degreeArray[index] = degree;
//            index++;
//        }
//
//        //sort degree Array
//        for (int i = 0; i < degreeArray.length - 1; i++) {
//            for (int j = i + 1; j < degreeArray.length; j++) {
//                if (degreeArray[i] > degreeArray[j]) {
//                    int temp = degreeArray[i];
//                    degreeArray[i] = degreeArray[j];
//                    degreeArray[j] = temp;
//                }
//            }
//        }
//
//        // Clustering into 3 sets author of their degree (Low Degree Group, Medium Degree Group, High Degree Group)
//        int numberOfDegree, numberOfLowDegree, numberOfMidDegree, numberOfHighDegree;
//        numberOfDegree = numberOfLowDegree = numberOfMidDegree = numberOfHighDegree = (int) degreeArray.length / 3;
//        if (degreeArray.length - numberOfDegree * 3 == 1) {
//            numberOfLowDegree += 1;
//        } else if (degreeArray.length - numberOfDegree * 3 == 2) {
//            numberOfLowDegree += 1;
//            numberOfMidDegree += 1;
//        }
//
//        _maxLowDegree = degreeArray[numberOfLowDegree - 1];
//        _maxMidDegree = degreeArray[numberOfLowDegree + numberOfMidDegree - 1];
//        _maxHighDegree = degreeArray[numberOfLowDegree + numberOfMidDegree + numberOfHighDegree - 1];
//
//        // Just get authors who really exist in the future network for the experiment
//        _listAuthorNearTesting = _graph.GetAllAuthorNearTest();
//        _listAuthorFarTesting = _graph.GetAllAuthorFarTest();
//
//        for (int authorId : _authorDegree.keySet()) {
//            if (_authorDegree.get(authorId) < _maxLowDegree) {
//                _authorGroup.put(authorId, "L");
//                if (_listAuthorNearTesting.contains(authorId) && _listAuthorFarTesting.contains(authorId)) {
//                    _listAuthorIdInLow.add(authorId);
//                }
//            } else if (_authorDegree.get(authorId) < _maxMidDegree) {
//                _authorGroup.put(authorId, "M");
//                if (_listAuthorNearTesting.contains(authorId) && _listAuthorFarTesting.contains(authorId)) {
//                    _listAuthorIdInMid.add(authorId);
//                }
//            } else {
//                _authorGroup.put(authorId, "H");
//                if (_listAuthorNearTesting.contains(authorId) && _listAuthorFarTesting.contains(authorId)) {
//                    _listAuthorIdInHigh.add(authorId);
//                }
//            }
//        }
//        // End of Clustering into 3 sets author of their degree
//    }
//    
//    private void writeDegreeDistribution() {
//        StringBuffer buffDegreeDistribution = new StringBuffer();
//        buffDegreeDistribution.append("Degree" + "\t" + "Number of Degree" + "\n");
//        for (int degree : _degreeCounter.keySet()) {
//            buffDegreeDistribution.append(degree + "\t" + _degreeCounter.get(degree) + "\n");
//        }
//         TextFileUtility.writeTextFile(_file_DregreeDistribution, buffDegreeDistribution.toString());
//    }
//    
//    private void runStatistics(){
//        loadDataFromTextFile();
//        clusterLowMedHigh();
//        writeDegreeDistribution();
//    }
//    
//    public static void main(String args[]){
//        System.out.println("START STATISTIS");
//        Statistics statistics = new Statistics(
//                "C:\\CRS-Experiment\\MAS\\Input\\Input2\\[TrainingData]AuthorID_PaperID_1995_2005.txt",
//                "C:\\CRS-Experiment\\MAS\\Input\\Input2\\[TrainingData]PaperID_Year_1995_2005.txt",
//                "C:\\CRS-Experiment\\MAS\\Input\\Input2\\[TestingData]AuthorID_PaperID_2006_2008.txt",
//                "C:\\CRS-Experiment\\MAS\\Input\\Input2\\[TestingData]AuthorID_PaperID_2009_2011.txt",
//                "C:\\CRS-Experiment\\MAS\\Input\\Input2\\file_DregreeDistribution.txt");
//
////        Statistics statistics = new Statistics(
////                "C:\\CRS-Experiment\\Sampledata\\Input\\Link-Net\\[Training]AuthorId_PaperID.txt",
////                "C:\\CRS-Experiment\\Sampledata\\Input\\Link-Net\\[Training]PaperID_Year.txt",
////                "C:\\CRS-Experiment\\Sampledata\\Input\\Link-Net\\[NearTesting]AuthorId_PaperID.txt",
////                "C:\\CRS-Experiment\\Sampledata\\Input\\Link-Net\\[FarTesting]AuthorId_PaperID.txt",
////                "C:\\CRS-Experiment\\Sampledata\\Input\\Sample_Statistics.txt");
//        
//        statistics.runStatistics();
//        System.out.println("END STATISTIS");
//    }
}
