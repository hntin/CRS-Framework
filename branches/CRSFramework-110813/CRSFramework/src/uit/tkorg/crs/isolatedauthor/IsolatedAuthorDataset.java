package uit.tkorg.crs.isolatedauthor;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import uit.tkorg.utility.XMLFileUtility;

/**
 *
 * @author TinHuynh
 */
public class IsolatedAuthorDataset {

    private HashMap<Integer, ArrayList<Integer>> _paperID_AuthorID_List;
    private HashMap<Integer, ArrayList<Integer>> _authorID_PaperID_List;
    private HashMap<Integer, HashMap<Integer, Integer>> _coAuthorTrainingNet;
    private HashMap<Integer, HashMap<Integer, Integer>> _coAuthorNF;
    private HashMap<Integer, HashMap<Integer, Integer>> _coAuthorFF;
    private HashMap<Integer, ArrayList<Integer>> _paperAuthorNF;
    private HashMap<Integer, ArrayList<Integer>> _paperAuthorFF;
    private HashMap<Integer, String> _authorID_AuthorName;
    private HashMap<Integer, Integer> _authorID_OrgID;
    private HashMap<Integer, String> _orgID_OrgName;
    private HashMap<Integer, Float> _authorID_ActiveScore;
    private HashMap<Integer, Float> _authorID_ImportantRate;
    private HashMap<Integer, HashMap<Integer, Float>> _ContentSimHM;
    private HashMap<Integer, HashMap<Integer, Float>> _OrgRSSHM;
    private String _file_Isolated_Researcher_List;
    private String _file_TrainingNet;
    private String _file_CoAuthor_NF;
    private String _file_CoAuthor_FF;
    private String _file_ContentSim;
    private String _file_OrgRSS;
    private String _file_ImportantRate;
    private String _file_ActiveScore;

    public IsolatedAuthorDataset(String file_Isolated_Researcher_List, String file_TrainingNet, 
                                    String file_CoAuthor_NF, String file_CoAuthor_FF,
                                    String file_ContentSim, String file_OrgRSS, String file_ImportantRate, String file_ActiveScore) {

        _file_Isolated_Researcher_List = file_Isolated_Researcher_List;
        _file_TrainingNet = file_TrainingNet;
        _file_CoAuthor_NF = file_CoAuthor_NF;
        _file_CoAuthor_FF = file_CoAuthor_FF;
        _file_ContentSim = file_ContentSim;
        _file_OrgRSS = file_OrgRSS;
        _file_ImportantRate = file_ImportantRate;
        _file_ActiveScore = file_ActiveScore;
    }

    private HashMap<Integer, String> loadInputAuthorList(String input_Author_List_File) {
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

    private void load_Training_Networkdata() {
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

    private void load_NF_FF_NetworkData() {
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

    private void build_NF_FF_Graph() {
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

    private HashMap<Integer, ArrayList<Integer>> build_FalseCollaborationPairs(HashMap<Integer, String> listIsolatedAuthor) {
        HashMap<Integer, ArrayList<Integer>> falsePairHM = new HashMap<>();
        for (int authorID : listIsolatedAuthor.keySet()) {
        }

        return falsePairHM;
    }

    private void writePairOfAuthorToXMLFile(String fileName, HashMap<Integer, ArrayList<Integer>> pairOfAuthorHM, boolean tag) {
        try {
            Document document = DocumentHelper.createDocument();
            Element root = document.addElement("Pairs_Of_Author");

            int count = 0;
            for (int isolatedAuthorID : pairOfAuthorHM.keySet()) {
                ArrayList<Integer> coAuthorList = pairOfAuthorHM.get(isolatedAuthorID);
                for (int coAuthorID : coAuthorList) {
                    Element pair = root.addElement("pair").addAttribute("id", String.valueOf(count++) );
                    Element isolatedElement = pair.addElement("IsolatedAuthor");
                    isolatedElement.addElement("AuthorID").addText(String.valueOf(isolatedAuthorID));
                    isolatedElement.addElement("AuthorName").addText(String.valueOf(isolatedAuthorID));
                    isolatedElement.addElement("OrgID").addText(String.valueOf(isolatedAuthorID));
                    isolatedElement.addElement("ActiveScore").addText(String.valueOf(isolatedAuthorID));
                    isolatedElement.addElement("ImportantRate").addText(String.valueOf(isolatedAuthorID));
                            
                    Element coAuthorElement = pair.addElement("CoAuthor");
                    coAuthorElement.addElement("AuthorID").addText(String.valueOf(coAuthorID));
                    coAuthorElement.addElement("AuthorName").addText(String.valueOf(coAuthorID));
                    coAuthorElement.addElement("OrgID").addText(String.valueOf(coAuthorID));
                    coAuthorElement.addElement("ActiveScore").addText(String.valueOf(coAuthorID));
                    coAuthorElement.addElement("ImportantRate").addText(String.valueOf(coAuthorID));
                    
                    pair.addElement("OrgRSS").addText("Org RSS Value");
                    pair.addElement("ContentSim").addText("ContentSim Value");

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
                "C:\\CRS-Experiment\\MAS\\ColdStart\\Input\\PotentialIsolatedAuthorList.txt",
                "C:\\CRS-Experiment\\MAS\\ColdStart\\Input\\[TrainingData]AuthorID_PaperID_1995_2005.txt",
                "C:\\CRS-Experiment\\MAS\\ColdStart\\Input\\[TestingData]AuthorID_PaperID_2006_2008.txt",
                "C:\\CRS-Experiment\\MAS\\ColdStart\\Input\\[TestingData]AuthorID_PaperID_2009_2011.txt",
                "C:\\CRS-Experiment\\MAS\\ColdStart\\Input\\",
                "C:\\CRS-Experiment\\MAS\\ColdStart\\Input\\",
                "C:\\CRS-Experiment\\MAS\\ColdStart\\Input\\",
                "C:\\CRS-Experiment\\MAS\\ColdStart\\Input\\");

//        IsolatedAuthorDataset isolatedDataset = new IsolatedAuthorDataset(
//                "C:\\CRS-Experiment\\Sampledata\\Input\\Link-Net\\IsolatedAuthor.txt",
//                "C:\\CRS-Experiment\\Sampledata\\Input\\Link-Net\\[Training]AuthorId_PaperID.txt",
//                "C:\\CRS-Experiment\\Sampledata\\Input\\Link-Net\\[NearTesting]AuthorId_PaperID.txt",
//                "C:\\CRS-Experiment\\Sampledata\\Input\\Link-Net\\[FarTesting]AuthorId_PaperID.txt",
//                "C:\\CRS-Experiment\\MAS\\ColdStart\\Input\\",
//                "C:\\CRS-Experiment\\MAS\\ColdStart\\Input\\",
//                "C:\\CRS-Experiment\\MAS\\ColdStart\\Input\\",
//                "C:\\CRS-Experiment\\MAS\\ColdStart\\Input\\");

        isolatedDataset.load_Training_Networkdata();
        isolatedDataset.load_NF_FF_NetworkData();
        isolatedDataset.build_NF_FF_Graph();
        isolatedDataset.build_CoAuthorGraph();
        HashMap<Integer, String> isolatedAuthorList = isolatedDataset.loadInputAuthorList(
                "C:\\CRS-Experiment\\MAS\\ColdStart\\Input\\PotentialIsolatedAuthorList.txt");
        HashMap<Integer, ArrayList<Integer>> truePairHM = isolatedDataset.build_TrueCollaborationPairs(isolatedAuthorList);
        
        isolatedDataset.writePairOfAuthorToXMLFile(
                "C:\\CRS-Experiment\\MAS\\ColdStart\\Input\\truepair.xml", truePairHM, true);

        System.out.println("END");
    }
}
