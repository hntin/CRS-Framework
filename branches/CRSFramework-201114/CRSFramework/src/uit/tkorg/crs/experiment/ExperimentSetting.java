package uit.tkorg.crs.experiment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import uit.tkorg.crs.model.AuthorGraph;
import uit.tkorg.utility.common.TextFileUtility;

/**
 *
 * @author TinHuynh
 */
public class ExperimentSetting {

    private AuthorGraph _graph = AuthorGraph.getInstance();
    private HashMap<Integer, Integer> _authorID_OrgID = new HashMap<>();
    private HashMap<Integer, String> _listAuthorRandom = new HashMap<>();
    private HashMap<Integer, Integer> _authorDegree = new HashMap<>(); // <AuthorID, AuthorDegree>
    private HashMap<Integer, String> _authorGroup = new HashMap<>();
    private ArrayList<Integer> _listAuthorIdInLow = new ArrayList<>();
    private ArrayList<Integer> _listAuthorIdInMid = new ArrayList<>();
    private ArrayList<Integer> _listAuthorIdInHigh = new ArrayList<>();
    private HashSet<Integer> _listAuthorNearTesting;
    private HashSet<Integer> _listAuthorFarTesting;
    private String _file_AuthorID_OrgID;
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
        ISOLATED,
    }

    public ExperimentSetting(int numberOfAuthor, String file_AuthorID_OrgID, String file_TraingAuthorIDPaperID, String file_TraingPaperID_Year,
            String file_NF_AuthorIDPaperID, String file_FF_AuthorIDPaperID, String file_SaveTo, boolean checkIncludedGroupDegree) {
        _numberOfAuthor = numberOfAuthor;
        _file_AuthorID_OrgID = file_AuthorID_OrgID;
        _file_TraingAuthorIDPaperID = file_TraingAuthorIDPaperID;
        _file_TraingPaperID_Year = file_TraingPaperID_Year;
        _file_NF_AuthorIDPaperID = file_NF_AuthorIDPaperID;
        _file_FF_AuthorIDPaperID = file_FF_AuthorIDPaperID;
        _fileSaveTo = file_SaveTo;
        _checkIncludedGroupDegree = checkIncludedGroupDegree;
    }

    private void load_AuthorID_OrgID() {
        try {
            FileInputStream fis = new FileInputStream(_file_AuthorID_OrgID);
            Reader reader = new InputStreamReader(fis, "UTF8");
            BufferedReader bufferReader = new BufferedReader(reader);
            bufferReader.readLine();
            String line = null;
            String[] tokens;
            int authorId;
            int orgId;
            while ((line = bufferReader.readLine()) != null) {
                tokens = line.split(",");
                authorId = Integer.parseInt(tokens[0]);
                if (tokens.length == 2)
                    orgId = Integer.parseInt(tokens[1]);
                else 
                    orgId = -1;

                _authorID_OrgID.put(authorId, orgId);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void loadDataFromTextFile() {
        try {
            load_AuthorID_OrgID();
            _graph.LoadTrainingData(_file_TraingAuthorIDPaperID, _file_TraingPaperID_Year);
            _graph.LoadTestingData(_file_NF_AuthorIDPaperID, _file_FF_AuthorIDPaperID);
            _graph.BuildCoAuthorGraph();
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
        _listAuthorNearTesting = _graph.GetAllAuthorNearTest();
        _listAuthorFarTesting = _graph.GetAllAuthorFarTest();

        for (int authorId : _authorDegree.keySet()) {
            if (_authorDegree.get(authorId) < _maxLowDegree) {
                _authorGroup.put(authorId, "L");
                if (_listAuthorNearTesting.contains(authorId) && _listAuthorFarTesting.contains(authorId)) {
                    _listAuthorIdInLow.add(authorId);
                }
            } else if (_authorDegree.get(authorId) < _maxMidDegree) {
                _authorGroup.put(authorId, "M");
                if (_listAuthorNearTesting.contains(authorId) && _listAuthorFarTesting.contains(authorId)) {
                    _listAuthorIdInMid.add(authorId);
                }
            } else {
                _authorGroup.put(authorId, "H");
                if (_listAuthorNearTesting.contains(authorId) && _listAuthorFarTesting.contains(authorId)) {
                    _listAuthorIdInHigh.add(authorId);
                }
            }
        }
        // End of Clustering into 3 sets author of their degree
    }

    private HashMap<Integer, String> generatePotentialIsolatedAuthor() {
        HashMap<Integer, String> resultList = new HashMap<>();
        int count1 = 0;
        for (int authorID1 : _graph.rssGraph.keySet()) {
            count1++;
            if (count1 % 100 == 0) {
                System.out.println(count1);
            }

            if (_graph.rssGraph.get(authorID1).size() == 0) {
                // If it have organization's information.
                if (_authorID_OrgID.get(authorID1) == -1)
                    continue;
                        
                // if it has a new link in the near future
                boolean hasNewLinkNF = false;
                for (int authorID2 : _graph.nearTestingData.keySet()) {
                    if (((authorID2 == authorID1) && _graph.nearTestingData.get(authorID2).size() > 0)
                            || (_graph.nearTestingData.get(authorID2) != null
                            && _graph.nearTestingData.get(authorID2).contains(authorID1))) {
                        hasNewLinkNF = true;
                        break;
                    }
                }

                // If it has a new link in the far future
                boolean hasNewLinkFF = false;
                for (int authorID2 : _graph.farTestingData.keySet()) {
                    if (((authorID2 == authorID1) && _graph.farTestingData.get(authorID2).size() > 0)
                            || (_graph.farTestingData.get(authorID2) != null
                            && _graph.farTestingData.get(authorID2).contains(authorID1))) {
                        hasNewLinkFF = true;
                        break;
                    }
                }

                if (hasNewLinkNF && hasNewLinkFF) {
                    resultList.put(authorID1, _authorGroup.get(authorID1));
                }
            }
        }

        return resultList;
    }

    private void generateAllIsolatedAuthor() {
        ArrayList<Integer> listAllIsolatedAuthor = new ArrayList<>();
        StringBuffer buffAllIsolatedAuthor = new StringBuffer();
        int count1 = 0;
        buffAllIsolatedAuthor.append("All Isolated authors" + "\n");
        for (int authorID1 : _graph.rssGraph.keySet()) {
            count1++;
            if (count1 % 100 == 0) {
                System.out.println(count1);
            }

            if (_graph.rssGraph.get(authorID1).size() == 0) {
                listAllIsolatedAuthor.add(authorID1);
                buffAllIsolatedAuthor.append(authorID1 + "\t" + _authorGroup.get(authorID1) + "\n");
            }
        }

        String parentDir = (new File(_fileSaveTo)).getParent();
        TextFileUtility.writeTextFile(parentDir + "\\AllIsolatedAuthor.txt", buffAllIsolatedAuthor.toString());
    }

    public void generateAuthorList(GeneratingOption generatingOption) {
        try {
            loadDataFromTextFile();
            clusterLowMedHigh();
            if (generatingOption == GeneratingOption.ISOLATED) {
                _listAuthorRandom = generatePotentialIsolatedAuthor();
                generateAllIsolatedAuthor();
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

    public static void main(String args[]) {
        System.out.println("START");
        ExperimentSetting experimentSetting = new ExperimentSetting(
                0,
                "C:\\CRS-Experiment\\MAS\\ColdStart\\Input\\Input1\\[TrainingData]AuthorID_OrgID_2001_2005.txt",
                "C:\\CRS-Experiment\\MAS\\ColdStart\\Input\\Input1\\[TrainingData]AuthorID_PaperID_2001_2005.txt",
                "C:\\CRS-Experiment\\MAS\\ColdStart\\Input\\Input1\\[TrainingData]PaperID_Year_2001_2005.txt",
                "C:\\CRS-Experiment\\MAS\\ColdStart\\Input\\Input1\\[TestingData]AuthorID_PaperID_2006_2008.txt",
                "C:\\CRS-Experiment\\MAS\\ColdStart\\Input\\Input1\\[TestingData]AuthorID_PaperID_2009_2011.txt",
                "C:\\CRS-Experiment\\MAS\\ColdStart\\Input\\Input1\\PotentialIsolatedAuthorList.txt",
                true);

//        ExperimentSetting experimentSetting = new ExperimentSetting(
//                2,
//                "C:\\CRS-Experiment\\Sampledata\\Input\\Link-Net\\[Training]AuthorId_PaperID.txt",
//                "C:\\CRS-Experiment\\Sampledata\\Input\\Link-Net\\[Training]PaperID_Year.txt",
//                "C:\\CRS-Experiment\\Sampledata\\Input\\Link-Net\\[NearTesting]AuthorId_PaperID.txt",
//                "C:\\CRS-Experiment\\Sampledata\\Input\\Link-Net\\[FarTesting]AuthorId_PaperID.txt",
//                "C:\\CRS-Experiment\\Sampledata\\Input\\PotentialIsolatedAuthor.txt",
//                true);

        experimentSetting.generateAuthorList(GeneratingOption.ISOLATED);
        System.out.println("END");
    }
}