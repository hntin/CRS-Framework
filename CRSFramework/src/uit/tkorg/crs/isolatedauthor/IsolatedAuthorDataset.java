package uit.tkorg.crs.isolatedauthor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import uit.tkorg.utility.XMLFileUtility;

/**
 *
 * @author TinHuynh
 */
public class IsolatedAuthorDataset {

    public static HashMap<Integer, HashMap<Integer, Integer>> _coAuthorTrainingNet;
    public static HashMap<Integer, HashMap<Integer, Integer>> _coAuthorNF;
    public static HashMap<Integer, HashMap<Integer, Integer>> _coAuthorFF;
    private HashMap<Integer, ArrayList<Integer>> _paperID_AuthorID_List;
    private HashMap<Integer, ArrayList<Integer>> _authorID_PaperID_List;
    private HashMap<Integer, ArrayList<Integer>> _paperAuthorNF;
    private HashMap<Integer, ArrayList<Integer>> _paperAuthorFF;
    private HashMap<Integer, String> _authorID_AuthorName;
    private HashMap<Integer, Integer> _authorID_OrgID;
    private HashMap<Integer, Integer> _authorID_OrgID_All;
    private HashMap<Integer, String> _orgID_OrgName;
    private HashMap<Integer, Float> _authorID_ActiveScore;
    private HashMap<Integer, Float> _authorID_ImportantRate;
    private HashMap<Integer, HashMap<Integer, Float>> _ContentSimHM;
    private HashMap<Integer, HashMap<Integer, Float>> _OrgRSSHM;
    private String _file_TrainingNet;
    private String _file_CoAuthor_NF;
    private String _file_CoAuthor_FF;

    public IsolatedAuthorDataset(String file_TrainingNet, String file_CoAuthor_NF, String file_CoAuthor_FF) {
        _file_TrainingNet = file_TrainingNet;
        _file_CoAuthor_NF = file_CoAuthor_NF;
        _file_CoAuthor_FF = file_CoAuthor_FF;
    }

    public HashMap<Integer, String> loadInputAuthorList(String input_Author_List_File) {
        HashMap<Integer, String> listIsolatedAuthor = new HashMap<>();
        // <editor-fold defaultstate="collapsed" desc="Load Author">
        try {
            FileInputStream fis = new FileInputStream(input_Author_List_File);
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
                    listIsolatedAuthor.put(authorId, groupLMD);
                }
            }
            bufferReader.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        // </editor-fold>
        return listIsolatedAuthor;
    }

    public void load_Training_NetworkData() {
        try {
            _authorID_PaperID_List = new HashMap<>();
            _paperID_AuthorID_List = new HashMap<>();
            FileInputStream fis = new FileInputStream(_file_TrainingNet);
            Reader reader = new InputStreamReader(fis, "UTF8");
            BufferedReader bufferReader = new BufferedReader(reader);
            bufferReader.readLine();
            String line = null;
            String[] tokens;
            int authorId;
            int paperId;
            while ((line = bufferReader.readLine()) != null) {
                tokens = line.split("\t");
                authorId = Integer.parseInt(tokens[0]);
                paperId = Integer.parseInt(tokens[1]);

                ArrayList<Integer> listPaper = _authorID_PaperID_List.get(authorId);
                if (listPaper == null) {
                    listPaper = new ArrayList<>();
                }
                listPaper.add(paperId);
                _authorID_PaperID_List.put(authorId, listPaper);

                ArrayList<Integer> listAuthor = _paperID_AuthorID_List.get(paperId);
                if (listAuthor == null) {
                    listAuthor = new ArrayList<>();
                }
                listAuthor.add(authorId);
                _paperID_AuthorID_List.put(paperId, listAuthor);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void load_NF_FF_NetworkData() {
        try {
            // Loading PaperID_AuthorID for NF Graph
            _paperAuthorNF = new HashMap<>();
            FileInputStream fis = new FileInputStream(_file_CoAuthor_NF);
            Reader reader = new InputStreamReader(fis, "UTF8");
            BufferedReader bufferReader = new BufferedReader(reader);
            bufferReader.readLine();
            String line = null;
            String[] tokens;
            int authorId;
            int paperId;
            while ((line = bufferReader.readLine()) != null) {
                tokens = line.split("\t");
                authorId = Integer.parseInt(tokens[0]);
                paperId = Integer.parseInt(tokens[1]);

                ArrayList<Integer> listAuthor = _paperAuthorNF.get(paperId);
                if (listAuthor == null) {
                    listAuthor = new ArrayList<>();
                }
                listAuthor.add(authorId);
                _paperAuthorNF.put(paperId, listAuthor);
            }

            // Loading PaperID_AuthorID for FF Graph
            _paperAuthorFF = new HashMap<>();
            fis = new FileInputStream(_file_CoAuthor_FF);
            reader = new InputStreamReader(fis, "UTF8");
            bufferReader = new BufferedReader(reader);
            bufferReader.readLine();
            while ((line = bufferReader.readLine()) != null) {
                tokens = line.split("\t");
                authorId = Integer.parseInt(tokens[0]);
                paperId = Integer.parseInt(tokens[1]);

                ArrayList<Integer> listAuthor = _paperAuthorFF.get(paperId);
                if (listAuthor == null) {
                    listAuthor = new ArrayList<>();
                }
                listAuthor.add(authorId);
                _paperAuthorFF.put(paperId, listAuthor);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void load_All_AuthorID_OrgID(String file_All_AuthorID_OrgID) {
        try {
            _authorID_OrgID_All = new HashMap<>();
            FileInputStream fis = new FileInputStream(file_All_AuthorID_OrgID);
            Reader reader = new InputStreamReader(fis, "UTF8");
            BufferedReader bufferReader = new BufferedReader(reader);
            bufferReader.readLine();
            String line = null;
            String[] tokens;
            int authorID;
            int orgID;
            while ((line = bufferReader.readLine()) != null) {
                tokens = line.split(",");
                authorID = Integer.parseInt(tokens[0]);
                if (tokens.length == 2) {
                    orgID = Integer.parseInt(tokens[1]);
                } else {
                    orgID = -1;
                }

                _authorID_OrgID_All.put(authorID, orgID);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void load_AuthorID_AuthorName_OrgID(String file_AuthorID_Name_OrgID) {
        try {
            _authorID_AuthorName = new HashMap<>();
            _authorID_OrgID = new HashMap<>();
            FileInputStream fis = new FileInputStream(file_AuthorID_Name_OrgID);
            Reader reader = new InputStreamReader(fis, "UTF8");
            BufferedReader bufferReader = new BufferedReader(reader);
            bufferReader.readLine();
            String line = null;
            String[] tokens;
            int authorID;
            String authorName;
            int orgID;
            while ((line = bufferReader.readLine()) != null) {
                tokens = line.split(",");
                if (tokens.length >= 2 && tokens.length <= 3) {
                    authorID = Integer.parseInt(tokens[0]);
                    authorName = tokens[1];
                    if (tokens.length == 3) {
                        orgID = Integer.parseInt(tokens[2]);
                    } else {
                        orgID = -1;
                    }

                    _authorID_AuthorName.put(authorID, authorName);
                    _authorID_OrgID.put(authorID, orgID);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void load_All_OrgID_OrgName(String file_OrgID_OrgName) {
        try {
            _orgID_OrgName = new HashMap<>();
            FileInputStream fis = new FileInputStream(file_OrgID_OrgName);
            Reader reader = new InputStreamReader(fis, "UTF8");
            BufferedReader bufferReader = new BufferedReader(reader);
            bufferReader.readLine();
            String line = null;
            String[] tokens;
            int orgID;
            String orgName;
            while ((line = bufferReader.readLine()) != null) {
                tokens = line.split(",");
                orgID = Integer.parseInt(tokens[0]);
                orgName = tokens[1];
                _orgID_OrgName.put(orgID, orgName);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void load_ActiveScore(String file_ActiveScore) {
        _authorID_ActiveScore = new HashMap<>();
        try {
            FileInputStream fis = new FileInputStream(file_ActiveScore);
            Reader reader = new InputStreamReader(fis, "UTF8");
            BufferedReader bufferReader = new BufferedReader(reader);
            bufferReader.readLine();
            String line = null;
            String[] tokens;
            int authorID;
            float activeScore;
            while ((line = bufferReader.readLine()) != null) {
                tokens = line.split("\t");
                authorID = Integer.parseInt(tokens[0]);
                activeScore = Float.valueOf(tokens[1]);
                _authorID_ActiveScore.put(authorID, activeScore);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void load_ImportantRate(String file_ImportantRate) {
        _authorID_ImportantRate = new HashMap<>();
        try {
            FileInputStream fis = new FileInputStream(file_ImportantRate);
            Reader reader = new InputStreamReader(fis, "UTF8");
            BufferedReader bufferReader = new BufferedReader(reader);
            bufferReader.readLine();
            String line = null;
            String[] tokens;
            int authorID;
            float importantRate;
            while ((line = bufferReader.readLine()) != null) {
                tokens = line.split("\t");
                authorID = Integer.parseInt(tokens[0]);
                importantRate = Float.valueOf(tokens[1]);
                _authorID_ImportantRate.put(authorID, importantRate);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void load_ContentSim(String dirPath_ContentSim) {
        _ContentSimHM = new HashMap<>();
        File mainFolder = new File(dirPath_ContentSim);
        File[] fileList = mainFolder.listFiles();
        for (int i = 0; i < fileList.length; i++) {
            if (fileList[i].isFile()) {
                String fileName = fileList[i].getName();
                System.out.println("fileName:" + fileName);
                int iSolatedAuthorID = Integer.parseInt(fileName.substring(0, fileName.indexOf(".txt")));

                HashMap<Integer, Float> contentSimList = new HashMap<>();
                try {
                    FileInputStream fis = new FileInputStream(fileList[i]);
                    Reader reader = new InputStreamReader(fis, "UTF8");
                    BufferedReader bufferReader = new BufferedReader(reader);
                    bufferReader.readLine();
                    String line = null;
                    String[] tokens;
                    int otherAuthorID;
                    float contentSim;
                    while ((line = bufferReader.readLine()) != null && !line.equals("")) {
                        tokens = line.split("\t");
                        otherAuthorID = Integer.parseInt(tokens[0]);
                        contentSim = Float.valueOf(tokens[1]);
                        contentSimList.put(otherAuthorID, contentSim);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                _ContentSimHM.put(iSolatedAuthorID, contentSimList);
            }
        }
    }

    private void load_OrgRSS(String dirPath_OrgRSS) {
        _OrgRSSHM = new HashMap<>();
        File mainFolder = new File(dirPath_OrgRSS);
        File[] fileList = mainFolder.listFiles();
        for (int i = 0; i < fileList.length; i++) {
            if (fileList[i].isFile()) {
                String fileName = fileList[i].getName();
                System.out.println("fileName:" + fileName);
                int iSolatedAuthorID = Integer.parseInt(fileName.substring(0, fileName.indexOf(".txt")));
                HashMap<Integer, Float> orgRSSList = new HashMap<>();
                try {
                    FileInputStream fis = new FileInputStream(fileList[i]);
                    Reader reader = new InputStreamReader(fis, "UTF8");
                    BufferedReader bufferReader = new BufferedReader(reader);
                    bufferReader.readLine();
                    String line = null;
                    String[] tokens;
                    int otherAuthorID;
                    float orgRSSValue;
                    while ((line = bufferReader.readLine()) != null && !line.equals("")) {
                        tokens = line.split("\t");
                        otherAuthorID = Integer.parseInt(tokens[0]);
                        orgRSSValue = Float.valueOf(tokens[1]);
                        orgRSSList.put(otherAuthorID, orgRSSValue);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                _OrgRSSHM.put(iSolatedAuthorID, orgRSSList);
            }
        }
    }

    public void build_NF_FF_Graph() {
        _coAuthorNF = new HashMap<>();
        _coAuthorFF = new HashMap<>();

        // Build the Co-AuthorNet for NF
        for (int pubId : _paperAuthorNF.keySet()) {
            ArrayList<Integer> listAuthors = _paperAuthorNF.get(pubId);
            if (listAuthors.size() == 1 && !_coAuthorNF.containsKey(listAuthors.get(0))) {
                _coAuthorNF.put(listAuthors.get(0), new HashMap<Integer, Integer>());
            } else {
                for (int author1 : listAuthors) {
                    for (int author2 : listAuthors) {
                        if (author1 != author2) {
                            HashMap<Integer, Integer> collaboration;
                            collaboration = _coAuthorNF.get(author1);
                            if (collaboration == null) {
                                collaboration = new HashMap<>();
                            }

                            Integer numofPaper = collaboration.get(author2);
                            if (numofPaper == null) {
                                numofPaper = 0;
                            }
                            numofPaper++;
                            collaboration.put(author2, numofPaper);
                            _coAuthorNF.put(author1, collaboration);
                        }
                    }
                }
            }
        }

        // Build the Co-AuthorNet for FF
        for (int pubId : _paperAuthorFF.keySet()) {
            ArrayList<Integer> listAuthors = _paperAuthorFF.get(pubId);
            if (listAuthors.size() == 1 && !_coAuthorFF.containsKey(listAuthors.get(0))) {
                _coAuthorFF.put(listAuthors.get(0), new HashMap<Integer, Integer>());
            } else {
                for (int author1 : listAuthors) {
                    for (int author2 : listAuthors) {
                        if (author1 != author2) {
                            HashMap<Integer, Integer> collaboration;
                            collaboration = _coAuthorFF.get(author1);
                            if (collaboration == null) {
                                collaboration = new HashMap<>();
                            }

                            Integer numofPaper = collaboration.get(author2);
                            if (numofPaper == null) {
                                numofPaper = 0;
                            }
                            numofPaper++;
                            collaboration.put(author2, numofPaper);
                            _coAuthorFF.put(author1, collaboration);
                        }
                    }
                }
            }
        }
    }

    public void build_CoAuthorGraph() {
        _coAuthorTrainingNet = new HashMap<>();
        for (int pubId : _paperID_AuthorID_List.keySet()) {
            ArrayList<Integer> listAuthors = _paperID_AuthorID_List.get(pubId);
            if (listAuthors.size() == 1 && !_coAuthorTrainingNet.containsKey(listAuthors.get(0))) {
                _coAuthorTrainingNet.put(listAuthors.get(0), new HashMap<Integer, Integer>());
            } else {
                for (int author1 : listAuthors) {
                    for (int author2 : listAuthors) {
                        if (author1 != author2) {
                            HashMap<Integer, Integer> collaboration;
                            collaboration = _coAuthorTrainingNet.get(author1);
                            if (collaboration == null) {
                                collaboration = new HashMap<>();
                            }

                            Integer numofPaper = collaboration.get(author2);
                            if (numofPaper == null) {
                                numofPaper = 0;
                            }
                            numofPaper++;
                            collaboration.put(author2, numofPaper);
                            _coAuthorTrainingNet.put(author1, collaboration);
                        }
                    }
                }
            }
        }
    }

    private HashMap<Integer, ArrayList<Integer>> build_TrueCollaborationPairs(HashMap<Integer, String> listIsolatedAuthor) {
        HashMap<Integer, ArrayList<Integer>> truePairHM = new HashMap<>();
        for (int isolatedAuthorID : listIsolatedAuthor.keySet()) {
            ArrayList<Integer> trueCoAuthorList = new ArrayList<>();

            if (_coAuthorNF.containsKey(isolatedAuthorID)) {
                HashMap<Integer, Integer> coAuthorOfIsolatedAuthor_NF = _coAuthorNF.get(isolatedAuthorID);
                for (int coAuthorID : coAuthorOfIsolatedAuthor_NF.keySet()) {
                    if (_coAuthorTrainingNet.containsKey(coAuthorID)) {
                        trueCoAuthorList.add(coAuthorID);
                    }
                }
            }

            if (_coAuthorFF.containsKey(isolatedAuthorID)) {
                HashMap<Integer, Integer> coAuthorOfIsolatedAuthor_FF = _coAuthorFF.get(isolatedAuthorID);
                for (int coAuthorID : coAuthorOfIsolatedAuthor_FF.keySet()) {
                    if (_coAuthorTrainingNet.containsKey(coAuthorID)) {
                        if (!trueCoAuthorList.contains(coAuthorID)) {
                            trueCoAuthorList.add(coAuthorID);
                        }
                    }
                }
            }

            truePairHM.put(isolatedAuthorID, trueCoAuthorList);
        }

        return truePairHM;
    }

    // Getting about 300 x 5 False cases
    public HashMap<Integer, ArrayList<Integer>> build_FalseCollaborationPairs(HashMap<Integer, String> listIsolatedAuthor) {
        HashMap<Integer, ArrayList<Integer>> falsePairHM = new HashMap<>();
        Random randomGenerator = new Random();
        int count = 0; // get 1263 false pairs
        for (int isolatedAuthorID : listIsolatedAuthor.keySet()) {
            ArrayList<Integer> falseCoAuthorList = new ArrayList<>();

            // For each Isolated author, Select out the list of authors 
            // who exist in the training and testing net, have Org's Infor but no connection with Isolated
            int numberFalseForEachIsolatedAuthor = 5;
            for (int i = 0; i < numberFalseForEachIsolatedAuthor; i++) {
                int randomAuthorID = Integer.MAX_VALUE;
                boolean found = false;
                while (!found) {
                    randomAuthorID = randomGenerator.nextInt(1000000);
                    if (randomAuthorID != isolatedAuthorID && _authorID_OrgID_All.containsKey(randomAuthorID)
                            && _authorID_OrgID_All.get(randomAuthorID) != -1) {
                        // Exist in 'Testing Nets' and HAVE NO ANY connections with 'Isolated author'
                        if (_coAuthorTrainingNet.containsKey(randomAuthorID) && _coAuthorNF.containsKey(randomAuthorID) && _coAuthorFF.containsKey(randomAuthorID)) {
                            if (!_coAuthorNF.get(randomAuthorID).containsKey(isolatedAuthorID)
                                    && !_coAuthorFF.get(randomAuthorID).containsKey(isolatedAuthorID)) {

                                falseCoAuthorList.add(randomAuthorID);
                                found = true;
                                count++;
                            }
                        }
                    }
                }
            }

            falsePairHM.put(isolatedAuthorID, falseCoAuthorList);
            if (count == 1263) break;
        }

        return falsePairHM;
    }

    public void writePairOfAuthorToXMLFile(String fileName, HashMap<Integer, ArrayList<Integer>> pairOfAuthorHM, boolean tag) {
        try {
            Document document = DocumentHelper.createDocument();
            Element root = document.addElement("Pairs_Of_Author");

            int count = 0;
            System.out.println("Size:" + pairOfAuthorHM.size());
            for (int isolatedAuthorID : pairOfAuthorHM.keySet()) {
                System.out.println("Pair No:" + count);
                ArrayList<Integer> coAuthorList = pairOfAuthorHM.get(isolatedAuthorID);
                for (int coAuthorID : coAuthorList) {
                    System.out.println("coAuthorList" + coAuthorList.size());
                    int orgID;
                    String orgName;
                    Element pair = root.addElement("pair").addAttribute("id", String.valueOf(count++));
                    Element isolatedElement = pair.addElement("IsolatedAuthor");
                    isolatedElement.addElement("AuthorID").addText(String.valueOf(isolatedAuthorID));
                    isolatedElement.addElement("AuthorName").addText(_authorID_AuthorName.get(isolatedAuthorID));
                    orgID = _authorID_OrgID.get(isolatedAuthorID);
                    isolatedElement.addElement("OrgID").addText(String.valueOf(orgID));
                    if (orgID != -1) {
                        orgName = _orgID_OrgName.get(orgID);
                        isolatedElement.addElement("OrgName").addText(orgName);
                    } else {
                        isolatedElement.addElement("OrgName").addText("NULL");
                    }
                    isolatedElement.addElement("ActiveScore").addText(String.valueOf(_authorID_ActiveScore.get(isolatedAuthorID)));
                    isolatedElement.addElement("ImportantRate").addText(String.valueOf(_authorID_ImportantRate.get(isolatedAuthorID)));

                    Element coAuthorElement = pair.addElement("CoAuthor");
                    coAuthorElement.addElement("AuthorID").addText(String.valueOf(coAuthorID));
                    coAuthorElement.addElement("AuthorName").addText(_authorID_AuthorName.get(coAuthorID));
                    orgID = _authorID_OrgID.get(coAuthorID);
                    coAuthorElement.addElement("OrgID").addText(String.valueOf(orgID));
                    if (orgID != -1) {
                        orgName = _orgID_OrgName.get(orgID);
                        coAuthorElement.addElement("OrgName").addText(orgName);
                    } else {
                        coAuthorElement.addElement("OrgName").addText("NULL");
                    }
                    coAuthorElement.addElement("ActiveScore").addText(String.valueOf(_authorID_ActiveScore.get(coAuthorID)));
                    coAuthorElement.addElement("ImportantRate").addText(String.valueOf(_authorID_ImportantRate.get(coAuthorID)));

                    // Chua kiem tra NULL pointer Exception
                    float orgRSSValue = _OrgRSSHM.get(isolatedAuthorID).get(coAuthorID);
                    float contentSim = _ContentSimHM.get(isolatedAuthorID).get(coAuthorID);
                    pair.addElement("OrgRSS").addText(String.valueOf(orgRSSValue));
                    pair.addElement("ContentSim").addText(String.valueOf(contentSim));

                    if (tag) {
                        pair.addElement("tag").addText(String.valueOf(1));
                    } else {
                        pair.addElement("tag").addText(String.valueOf(-1));
                    }
                }
            }

            XMLFileUtility.writeXMLFile(fileName, document);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String args[]) {
        System.out.println("START");
        IsolatedAuthorDataset isolatedDataset = new IsolatedAuthorDataset(
                "C:\\CRS-Experiment\\MAS\\ColdStart\\Input\\Input1\\[TrainingData]AuthorID_PaperID_2001_2005.txt",
                "C:\\CRS-Experiment\\MAS\\ColdStart\\Input\\Input1\\[TestingData]AuthorID_PaperID_2006_2008.txt",
                "C:\\CRS-Experiment\\MAS\\ColdStart\\Input\\Input1\\[TestingData]AuthorID_PaperID_2009_2011.txt");

//        IsolatedAuthorDataset isolatedDataset = new IsolatedAuthorDataset(
//                "C:\\CRS-Experiment\\Sampledata\\Input\\Link-Net\\IsolatedAuthor.txt",
//                "C:\\CRS-Experiment\\Sampledata\\Input\\Link-Net\\[Training]AuthorId_PaperID.txt",
//                "C:\\CRS-Experiment\\Sampledata\\Input\\Link-Net\\[NearTesting]AuthorId_PaperID.txt",
//                "C:\\CRS-Experiment\\Sampledata\\Input\\Link-Net\\[FarTesting]AuthorId_PaperID.txt",
//                "C:\\CRS-Experiment\\MAS\\ColdStart\\Input\\",
//                "C:\\CRS-Experiment\\MAS\\ColdStart\\Input\\",
//                "C:\\CRS-Experiment\\MAS\\ColdStart\\Input\\",
//                "C:\\CRS-Experiment\\MAS\\ColdStart\\Input\\");

        System.out.println("Loading & Building Networks");
        isolatedDataset.load_Training_NetworkData();
        isolatedDataset.load_NF_FF_NetworkData();
        isolatedDataset.build_NF_FF_Graph();
        isolatedDataset.build_CoAuthorGraph();

        System.out.println("load_All_AuthorID_OrgID");
        isolatedDataset.load_All_AuthorID_OrgID("C:\\CRS-Experiment\\MAS\\ColdStart\\Input\\Input1\\[TrainingData]AuthorID_OrgID_All.txt");

        System.out.println("load_All_OrgID_OrgName");
        isolatedDataset.load_All_OrgID_OrgName(
                "C:\\CRS-Experiment\\MAS\\ColdStart\\Input\\Input1\\[TrainingData]OrgID_OrgName_All.txt");

        System.out.println("load_AuthorID_AuthorName_OrgID");
        isolatedDataset.load_AuthorID_AuthorName_OrgID(
                "C:\\CRS-Experiment\\MAS\\ColdStart\\Input\\Input1\\[TrainingData]AuthorID_AuthorName_OrgID_2001_2005.txt");

        System.out.println("load_ActiveScore");
        isolatedDataset.load_ActiveScore(
                "C:\\CRS-Experiment\\MAS\\ColdStart\\Input\\Input1\\ActiveScore\\ActiveScore.txt");

        System.out.println("load_ImportantRate");
        isolatedDataset.load_ImportantRate(
                "C:\\CRS-Experiment\\MAS\\ColdStart\\Input\\Input1\\ImportantRate\\pagerank.txt");

        System.out.println("load_ContentSim");
        isolatedDataset.load_ContentSim("C:\\CRS-Experiment\\MAS\\ColdStart\\Input\\Input1\\ContentSim");

        System.out.println("load_OrgRSS");
        isolatedDataset.load_OrgRSS("C:\\CRS-Experiment\\MAS\\ColdStart\\Input\\Input1\\OrgRSS");

        HashMap<Integer, String> isolatedAuthorList = isolatedDataset.loadInputAuthorList(
                "C:\\CRS-Experiment\\MAS\\ColdStart\\Input\\Input1\\PotentialIsolatedAuthorList_1_300.txt");

        System.out.println("build_FalseCollaborationPairs");
        HashMap<Integer, ArrayList<Integer>> truePairHM = isolatedDataset.build_TrueCollaborationPairs(isolatedAuthorList);
        //HashMap<Integer, ArrayList<Integer>> falsePairHM = isolatedDataset.build_FalseCollaborationPairs(isolatedAuthorList);


        System.out.println("writePairOfAuthorToXMLFile1");
        isolatedDataset.writePairOfAuthorToXMLFile(
                "C:\\CRS-Experiment\\MAS\\ColdStart\\Input\\Input1\\TruePair1.xml", truePairHM, true);
//        System.out.println("writePairOfAuthorToXMLFile2");
//        isolatedDataset.writePairOfAuthorToXMLFile(
//                "C:\\CRS-Experiment\\MAS\\ColdStart\\Input\\Input1\\FalsePair1.xml", falsePairHM, false);

        System.out.println("END");
    }
}
