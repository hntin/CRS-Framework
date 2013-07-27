package uit.tkorg.crs.experiment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import uit.tkorg.crs.graph.Graph;
import uit.tkorg.utility.TextFileUtility;

/**
 *
 * @author TinHuynh
 */
public class ExperimentSetting {

    private Graph _graph = Graph.getInstance();
    private HashMap<Integer, String> _listAuthorRandom = new HashMap<>();
    private HashMap<Integer, Integer> _authorDegree = new HashMap<>(); // <AuthorID, AuthorDegree>
    private HashMap<Integer, String> _authorGroup = new HashMap<>();
    private ArrayList<Integer> _listAuthorIdInLow = new ArrayList<>();
    private ArrayList<Integer> _listAuthorIdInMid = new ArrayList<>();
    private ArrayList<Integer> _listAuthorIdInHigh = new ArrayList<>();
    private String _file_TraingAuthorIDPaperID;
    private String _file_TraingPaperID_Year;
    private String _file_NF_AuthorIDPaperID;
    private String _file_FF_AuthorIDPaperID;
    private String _fileSaveTo;
    private int _numberOfAuthor;
    private boolean _checkIncludedGroupDegree;
    int _maxLowDegree;
    int _maxMidDegree;
    int _maxHighDegree;

    public static enum GeneratingOption {

        NONE,
        LOWEST,
        HIGHEST,
        LOWMIDHIGH,
        POTENTIALLINK,
    }

    public ExperimentSetting(int numberOfAuthor, String file_TraingAuthorIDPaperID, String file_TraingPaperID_Year,
            String file_NF_AuthorIDPaperID, String file_FF_AuthorIDPaperID, String file_SaveTo, boolean checkIncludedGroupDegree) {
        _numberOfAuthor = numberOfAuthor;
        _file_TraingAuthorIDPaperID = file_TraingAuthorIDPaperID;
        _file_TraingPaperID_Year = file_TraingPaperID_Year;
        _file_NF_AuthorIDPaperID = file_NF_AuthorIDPaperID;
        _file_FF_AuthorIDPaperID = file_FF_AuthorIDPaperID;
        _fileSaveTo = file_SaveTo;
        _checkIncludedGroupDegree = checkIncludedGroupDegree;
    }

    private void loadDataFromTextFile() {
        try {
            _graph.LoadTrainingData(_file_TraingAuthorIDPaperID, _file_TraingPaperID_Year);
            _graph.LoadTestingData(_file_NF_AuthorIDPaperID, _file_FF_AuthorIDPaperID);
            _graph.BuidCoAuthorGraph();
            _graph.BuildingRSSGraph();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void clusterLowMedHigh() {
        for (int authorId : _graph.rssGraph.keySet()) {
            _authorDegree.put(authorId, _graph.rssGraph.get(authorId).size());
        }

        // <Degree X, Number of Authors who have the Degree X>
        HashMap<Integer, Integer> degreeCounter = new HashMap<>();
        for (int authorId : _authorDegree.keySet()) {
            Integer counter = degreeCounter.get(_authorDegree.get(authorId));
            if (counter == null) {
                counter = 0;
            }
            counter++;
            degreeCounter.put(_authorDegree.get(authorId), counter);
        }

        // degreeArray index
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

        // Clustering into 3 sets author of their degree (Low Degree Group, Medium Degree Group, High Degree Group)
        int numberOfDegree, numberOfLowDegree, numberOfMidDegree, numberOfHighDegree;
        numberOfDegree = numberOfLowDegree = numberOfMidDegree = numberOfHighDegree = (int) degreeArray.length / 3;
        if (degreeArray.length - numberOfDegree * 3 == 1) {
            numberOfLowDegree += 1;
        } else if (degreeArray.length - numberOfDegree * 3 == 2) {
            numberOfLowDegree += 1;
            numberOfMidDegree += 1;
        }

        _maxLowDegree = degreeArray[numberOfLowDegree - 1];
        _maxMidDegree = degreeArray[numberOfLowDegree + numberOfMidDegree - 1];
        _maxHighDegree = degreeArray[numberOfLowDegree + numberOfMidDegree + numberOfHighDegree - 1];

        // Just get authors who really exist in the future network for the experiment
        HashSet<Integer> listAuthorNearTesting = _graph.GetAllAuthorNearTest();
        HashSet<Integer> listAuthorFarTesting = _graph.GetAllAuthorFarTest();

        for (int authorId : _authorDegree.keySet()) {
            if (_authorDegree.get(authorId) < _maxLowDegree) {
                _authorGroup.put(authorId, "L");
                if (listAuthorNearTesting.contains(authorId) && listAuthorFarTesting.contains(authorId)) {
                    _listAuthorIdInLow.add(authorId);
                }
            } else if (_authorDegree.get(authorId) < _maxMidDegree) {
                _authorGroup.put(authorId, "M");
                if (listAuthorNearTesting.contains(authorId) && listAuthorFarTesting.contains(authorId)) {
                    _listAuthorIdInMid.add(authorId);
                }
            } else {
                _authorGroup.put(authorId, "H");
                if (listAuthorNearTesting.contains(authorId) && listAuthorFarTesting.contains(authorId)) {
                    _listAuthorIdInHigh.add(authorId);
                }
            }
        }
        // End of Clustering into 3 sets author of their degree
    }

    /**
     * Generating the list of authors for the potential link option Get LINKS in
     * the testing net, but not in the training net authorID1 & authorID2 HAVE
     * NO link in the training net authorID1 & authorID2 HAS a link in the near
     * future net
     *
     * @param generatingOption
     * @return
     */
    private HashMap<Integer, String> generatePotentialAuthor() {
        HashMap<Integer, String> listPotentialAuthor = new HashMap<>();
        for (int authorID1 : _graph.nearTestingData.keySet()) {
            if (_graph.rssGraph.containsKey(authorID1) && _graph.farTestingData.containsKey(authorID1)) {
                ArrayList<Integer> listCoAuthor = _graph.nearTestingData.get(authorID1);
                for (int i = 0; i < listCoAuthor.size(); i++) {
                    int authorID2 = listCoAuthor.get(i);
                    if (_graph.rssGraph.containsKey(authorID2) && _graph.farTestingData.containsKey(authorID2)) {
                        // authorID1 & authorID2 HAVE NO link in the training net
                        if (!_graph.isLinkExistInRSSGraph(_graph.rssGraph, authorID1, authorID2)) {

                            boolean foundAuthorID1 = false;
                            boolean foundAuthorID2 = false;
                            if (listPotentialAuthor != null) {
                                for (int currentID : listPotentialAuthor.keySet()) {
                                    if (currentID == authorID1) {
                                        foundAuthorID1 = true;
                                    }
                                    if (currentID == authorID2) {
                                        foundAuthorID2 = true;
                                    }
                                }
                                if (!foundAuthorID1) {
                                    listPotentialAuthor.put(authorID1, _authorGroup.get(authorID1));
                                }
                                if (!foundAuthorID2) {
                                    listPotentialAuthor.put(authorID2, _authorGroup.get(authorID2));
                                }
                            }
                        }
                    }
                }
            }
        }

        while (listPotentialAuthor.size() > _numberOfAuthor) {
            listPotentialAuthor.remove(0);
        }

        //<editor-fold defaultstate="collapsed" desc="Authors HAVE NO ANY Connection in the traning net, but they HAVE in the testing net">
        ArrayList<Integer> potentialAuthorList = new ArrayList<>();
        StringBuffer buffAuthorWithoutAnyLink = new StringBuffer();
        String parentDir = (new File(_fileSaveTo)).getParent();
        buffAuthorWithoutAnyLink.append("AuthorID" + "\t" + "Group" + "\n");
        for (int authorID1 : _graph.rssGraph.keySet()) {
            // authorID1 HAS NO any connection in the training net
            if (_graph.rssGraph.get(authorID1).size() == 0) {
                // If authorID1 HAS any link in the future?
                boolean hasNewLink = false;
                for (int authorID2 : _graph.nearTestingData.keySet()) {
                    if (((authorID2 == authorID1) && _graph.nearTestingData.get(authorID2).size() > 0)
                            || (_graph.nearTestingData.get(authorID2) != null
                            && _graph.nearTestingData.get(authorID2).contains(authorID1))) {
                        hasNewLink = true;
                        break;
                    }
                }

                if (hasNewLink) {
                    potentialAuthorList.add(authorID1);
                    buffAuthorWithoutAnyLink.append(authorID1 + "\t" + _authorGroup.get(authorID1) + "\n");
                }
            }
        }
        TextFileUtility.writeTextFile(parentDir + "\\ListAuthorNoAnyLink.txt", buffAuthorWithoutAnyLink.toString());
        // </editor-fold>

        return listPotentialAuthor;
    }

    public void generateAuthorList(GeneratingOption generatingOption) {
        try {
            loadDataFromTextFile();
            clusterLowMedHigh();
            if (generatingOption == GeneratingOption.POTENTIALLINK) {
                _listAuthorRandom = generatePotentialAuthor();
            } else {
                if (generatingOption == GeneratingOption.LOWEST) {
                    _listAuthorRandom.putAll(randomAuthorIdFromList(_numberOfAuthor, _listAuthorIdInLow));
                } else if (generatingOption == GeneratingOption.HIGHEST) {
                    _listAuthorRandom.putAll(randomAuthorIdFromList(_numberOfAuthor, _listAuthorIdInHigh));
                } else if (generatingOption == GeneratingOption.LOWMIDHIGH) {
                    if (_numberOfAuthor % 3 == 1) {
                        _listAuthorRandom.putAll(randomAuthorIdFromList(_numberOfAuthor / 3 + 1, _listAuthorIdInLow));
                    } else {
                        if (_numberOfAuthor % 3 == 2) {
                            _listAuthorRandom.putAll(randomAuthorIdFromList(_numberOfAuthor / 3 + 2, _listAuthorIdInLow));
                        } else {
                            _listAuthorRandom.putAll(randomAuthorIdFromList(_numberOfAuthor / 3, _listAuthorIdInLow));
                        }
                    }
                    _listAuthorRandom.putAll(randomAuthorIdFromList(_numberOfAuthor / 3, _listAuthorIdInMid));
                    _listAuthorRandom.putAll(randomAuthorIdFromList(_numberOfAuthor / 3, _listAuthorIdInHigh));
                }
            }

            StringBuffer listAuthorBuff = new StringBuffer();
            listAuthorBuff.append("AuthorID" + "\n");
            for (int authorId : _listAuthorRandom.keySet()) {
                if (_checkIncludedGroupDegree) {
                    listAuthorBuff.append(authorId + "\t" + _authorGroup.get(authorId) + "\n");
                } else {
                    listAuthorBuff.append(authorId + "\n");
                }
            }
            TextFileUtility.writeTextFile(_fileSaveTo, listAuthorBuff.toString());

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private HashMap<Integer, String> randomAuthorIdFromList(int numberOfAuthorId, ArrayList<Integer> listId) {
        HashMap<Integer, String> result = new HashMap<>();
        if (listId.size() < numberOfAuthorId) {
            numberOfAuthorId = listId.size();
        }

        int counter = 0;
        Random rd = new Random();
        while (counter < numberOfAuthorId) {
            int index;
            if (listId.size() > 1) {
                index = rd.nextInt(listId.size() - 1);
            } else {
                index = 0;
            }

            result.put(listId.get(index), _authorGroup.get(listId.get(index)));
            listId.remove(index);
            counter++;
        }

        return result;
    }

//    public static void main(String args[]) {
//        System.out.println("START");
//        ExperimentSetting experimentSetting = new ExperimentSetting(
//                10,
//                "C:\\CRS-Experiment\\Sampledata\\[Training]AuthorId_PaperID.txt",
//                "C:\\CRS-Experiment\\Sampledata\\[Training]PaperID_Year.txt",
//                "C:\\CRS-Experiment\\Sampledata\\[NearTesting]AuthorId_PaperID.txt",
//                "C:\\CRS-Experiment\\Sampledata\\[FarTesting]AuthorId_PaperID.txt",
//                "C:\\CRS-Experiment\\Sampledata\\Output\\PotentialAuthorList.txt",
//                true);
//
//        experimentSetting.generateAuthorList(GeneratingOption.HIGHEST);
//        System.out.println("END");
//    }
}