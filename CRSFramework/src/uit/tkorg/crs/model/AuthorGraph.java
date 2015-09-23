package uit.tkorg.crs.model;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.StringTokenizer;
import uit.tkorg.utility.algorithm.PageRank;

/**
 *
 * @author daolv
 */
public class AuthorGraph {

    //<editor-fold defaultstate="collapsed" desc="Member Variables">
    private static AuthorGraph _instance;
    public HashMap<Integer, HashMap<Integer, Integer>> coAuthorGraph;
    private HashMap<Integer, HashMap<Integer, Integer>> coAuthorGraphNear;
    private HashMap<Integer, HashMap<Integer, Integer>> coAuthorGraphFar;
    public HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>> expFunctBasedCoAuthorGraph;
    public HashMap<Integer, HashMap<Integer, Float>> rssGraph; //weighted, directed graph
    public HashMap<Integer, HashMap<Integer, Float>> rssPlusGraph; //weighted, directed graph
    public HashMap<Integer, HashMap<Integer, Float>> rssDoublePlusGraph; //weighted, directed graph
    public HashMap<Integer, ArrayList<Integer>> nearTestingData; //non-weighted, non-directed graph <authorID, <Lis of CoAuthorID>>
    public HashMap<Integer, Integer> paperId_year;
    public HashMap<Integer, ArrayList<Integer>> authorPaper;
    public HashMap<Integer, ArrayList<Integer>> paperAuthor;
    public HashMap<Integer, String> listRandomAuthor;
    // </editor-fold>

    public static AuthorGraph getInstance() {
        if (_instance == null) {
            _instance = new AuthorGraph();
        }
        return _instance;
    }

    private AuthorGraph() {
        coAuthorGraph = new HashMap<>();
        expFunctBasedCoAuthorGraph = new HashMap<>();
        coAuthorGraphNear = new HashMap<>();
        coAuthorGraphFar = new HashMap<>();
    }

    public void loadTrainingData_AuthorID_PaperID_File(String file_AuthorID_PaperID) {
        try {
            authorPaper = new HashMap<>();
            paperAuthor = new HashMap<>();
            FileInputStream fis = new FileInputStream(file_AuthorID_PaperID);
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

                ArrayList<Integer> listPaper = authorPaper.get(authorId);
                if (listPaper == null) {
                    listPaper = new ArrayList<>();
                }
                listPaper.add(paperId);
                authorPaper.put(authorId, listPaper);

                ArrayList<Integer> listAuthor = paperAuthor.get(paperId);
                if (listAuthor == null) {
                    listAuthor = new ArrayList<>();
                }
                listAuthor.add(authorId);
                paperAuthor.put(paperId, listAuthor);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadTrainingData_PaperID_Year_File(String file_PaperID_Year) {
        try {
            paperId_year = new HashMap<>();
            FileInputStream fis = new FileInputStream(file_PaperID_Year);
            Reader reader = new InputStreamReader(fis, "UTF8");
            BufferedReader bufferReader = new BufferedReader(reader);
            bufferReader.readLine();
            String line = null;
            String[] tokens;
            int paperId;
            Integer year;
            while ((line = bufferReader.readLine()) != null) {
                tokens = line.split("\t");
                paperId = Integer.parseInt(tokens[0]);
                if (tokens.length <= 1) {
                    year = 0;
                } else {
                    year = Integer.parseInt(tokens[1]);
                }
                paperId_year.put(paperId, year);
            }
            bufferReader.close();
        } catch (Exception e) {
        }
    }

    public void loadTestingData_GroundTruthFile(String fileGroundTruth) {
        HashMap<Integer, ArrayList<Integer>> paperAuthorTmp = new HashMap<>();
        try {
            nearTestingData = new HashMap<>();
            FileInputStream fis = new FileInputStream(fileGroundTruth);
            Reader reader = new InputStreamReader(fis, "UTF8");
            BufferedReader bufferReader = new BufferedReader(reader);
            bufferReader.readLine();
            String line = null;
            String[] tokens;
            int authorId;
            int paperId;
            while ((line = bufferReader.readLine()) != null) {
                tokens = line.split("\t");
                if (tokens.length != 2) {
                    continue;
                }

                authorId = Integer.parseInt(tokens[0]);
                paperId = Integer.parseInt(tokens[1]);

                ArrayList<Integer> listAuthor = paperAuthorTmp.get(paperId);
                if (listAuthor == null) {
                    listAuthor = new ArrayList<>();
                }
                listAuthor.add(authorId);
                paperAuthorTmp.put(paperId, listAuthor);
            }
            bufferReader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        for (int paperId : paperAuthorTmp.keySet()) {
            ArrayList<Integer> listAuthorId = paperAuthorTmp.get(paperId);
            for (int authorId1 : listAuthorId) {
                for (int authorId2 : listAuthorId) {
                    if (authorId2 > authorId1) {
                        ArrayList<Integer> listCollaboration = nearTestingData.get(authorId1);
                        if (listCollaboration == null) {
                            listCollaboration = new ArrayList<>();
                        }
                        if (!listCollaboration.contains(authorId2)) {
                            listCollaboration.add(authorId2);
                            nearTestingData.put(authorId1, listCollaboration);
                        }
                    }
                }
            }
        }
    }

    /**
     * building all graphs
     *
     * @param k
     * @param year
     */
    public void buildAllCoAuthorGraph(float k, int year) {
        buildExpFuntBasedCoAuthorGraph(2001, 2005);
        buildCoAuthorGraph();
        buildNearFarCoAuthorGraph(year);
        buildRSSGraph();
        buildRSSPlusGraph(k, year);
        buildRSSDoublePlusGraph();
    }

    /**
     * coAuthorGraph (Weight of links are number of collaborations of two
     * neighbor author).
     */
    public void buildCoAuthorGraph() {
        for (int pubId : paperAuthor.keySet()) {
            ArrayList<Integer> listAuthors = paperAuthor.get(pubId);
            if (listAuthors.size() == 1 && !coAuthorGraph.containsKey(listAuthors.get(0))) {
                coAuthorGraph.put(listAuthors.get(0), new HashMap<Integer, Integer>());
            } else {
                for (int author1 : listAuthors) {
                    for (int author2 : listAuthors) {
                        if (author1 != author2) {
                            HashMap<Integer, Integer> collaboration;
                            collaboration = coAuthorGraph.get(author1);
                            if (collaboration == null) {
                                collaboration = new HashMap<>();
                            }

                            Integer numofPaper = collaboration.get(author2);
                            if (numofPaper == null) {
                                numofPaper = 0;
                            }
                            numofPaper++;
                            collaboration.put(author2, numofPaper);
                            coAuthorGraph.put(author1, collaboration);
                        }
                    }
                }
            }
        }
    }

    public void buildExpFuntBasedCoAuthorGraph(int firstYear, int lastYear) {
        for (int paperID : paperAuthor.keySet()) {
            int yearOfPaper = paperId_year.get(paperID);
            int deltaTime = lastYear - yearOfPaper;
            ArrayList<Integer> listAuthors = paperAuthor.get(paperID);
            if (listAuthors.size() == 1 && !expFunctBasedCoAuthorGraph.containsKey(listAuthors.get(0))) {
                expFunctBasedCoAuthorGraph.put(listAuthors.get(0), new HashMap<Integer, HashMap<Integer, Integer>>());
            } else {
                for (int author1 : listAuthors) {
                    for (int author2 : listAuthors) {
                        if (author1 != author2) {
                            HashMap<Integer, HashMap<Integer, Integer>> timeCollabHM_1 = expFunctBasedCoAuthorGraph.get(author1);
                            HashMap<Integer, Integer> timeCollabHM_2;
                            if (timeCollabHM_1 == null) { // author1 has no any coAuthors with others
                                timeCollabHM_1 = new HashMap<>();
                                timeCollabHM_2 = new HashMap<>();
                                timeCollabHM_2.put(deltaTime, 1);
                            } else { // timeCollabHM_1 != null --> author1 has some coAuthors with some others
                                timeCollabHM_2 = timeCollabHM_1.get(author2);
                                if (timeCollabHM_2 == null) { // author1 has no any coAuthor with author2
                                    timeCollabHM_2 = new HashMap<>();
                                    timeCollabHM_2.put(deltaTime, 1);
                                } else {
                                    // timeCollabHM_2 != null 
                                    // --> author1 has some coAuthor with author2, update Year and NumberOfCoAuthor
                                    if (timeCollabHM_2.get(deltaTime) != null) {
                                        timeCollabHM_2.put(deltaTime, timeCollabHM_2.get(deltaTime) + 1);
                                    } else {
                                        timeCollabHM_2.put(deltaTime, 1);
                                    }
                                }
                            }
                            timeCollabHM_1.put(author2, timeCollabHM_2);
                            expFunctBasedCoAuthorGraph.put(author1, timeCollabHM_1);
                        }
                    }
                }
            }
        }
    }

    /**
     * Building Near and Far Collaboration for the training CoAuthorGraph
     * (forRSSPlusGraph)
     *
     * @param year
     */
    public void buildNearFarCoAuthorGraph(int year) {
        for (int pubId : paperAuthor.keySet()) {
            ArrayList<Integer> listAuthors = paperAuthor.get(pubId);
            if (listAuthors.size() == 1 && !coAuthorGraph.containsKey(listAuthors.get(0))) {
                coAuthorGraphNear.put(listAuthors.get(0), new HashMap<Integer, Integer>());
                coAuthorGraphFar.put(listAuthors.get(0), new HashMap<Integer, Integer>());
            } else {
                for (int author1 : listAuthors) {
                    for (int author2 : listAuthors) {
                        if (author1 != author2) {
                            if (paperId_year.get(pubId) >= year) {
                                HashMap<Integer, Integer> collaboratorsNear;
                                collaboratorsNear = coAuthorGraphNear.get(author1);
                                if (collaboratorsNear == null) {
                                    collaboratorsNear = new HashMap<>();
                                }

                                Integer numberOfPaper;
                                numberOfPaper = collaboratorsNear.get(author2);
                                if (numberOfPaper == null) {
                                    numberOfPaper = 0;
                                }
                                numberOfPaper++;
                                collaboratorsNear.put(author2, numberOfPaper);
                                coAuthorGraphNear.put(author1, collaboratorsNear);
                            }

                            if (paperId_year.get(pubId) < year) {
                                HashMap<Integer, Integer> collaboratorsFar;
                                collaboratorsFar = coAuthorGraphFar.get(author1);
                                if (collaboratorsFar == null) {
                                    collaboratorsFar = new HashMap<>();
                                }

                                Integer numberOfPaper;
                                numberOfPaper = collaboratorsFar.get(author2);
                                if (numberOfPaper == null) {
                                    numberOfPaper = 0;
                                }
                                numberOfPaper++;
                                collaboratorsFar.put(author2, numberOfPaper);
                                coAuthorGraphFar.put(author1, collaboratorsFar);
                            }

                        }
                    }
                }
            }
        }
    }

    /**
     * rssGraph (Weight of links are normalized number of collaborations of two
     * neighbor author).
     */
    public void buildRSSGraph() {
        rssGraph = new HashMap<>();
        for (int authorId1 : coAuthorGraph.keySet()) {
            if (coAuthorGraph.get(authorId1).size() == 0) {
                rssGraph.put(authorId1, new HashMap<Integer, Float>());
            } else {
                int totalCollaborationOfAuthor1 = 0;
                for (int authorId2 : coAuthorGraph.get(authorId1).keySet()) {
                    totalCollaborationOfAuthor1 += coAuthorGraph.get(authorId1).get(authorId2);
                }

                for (int authorId2 : coAuthorGraph.get(authorId1).keySet()) {
                    if (authorId1 != authorId2) {
                        float t = 0;
                        float weight = ((float) coAuthorGraph.get(authorId1).get(authorId2))
                                / ((float) totalCollaborationOfAuthor1);
                        HashMap<Integer, Float> rssWeight = rssGraph.get(authorId1);
                        if (rssWeight == null) {
                            rssWeight = new HashMap<>();
                        }

                        Float existedWeight = rssWeight.get(authorId2);
                        if (existedWeight == null) {
                            existedWeight = weight;
                            rssWeight.put(authorId2, existedWeight);
                        }
                        rssGraph.put(authorId1, rssWeight);
                    }
                }
            }
        }
    }

    /**
     * rssPlusGraph (Weight of links are number of collaborations of two
     * neighbor authors considering trend factor based on (k, year)).
     *
     * @param k
     * @param year
     */
    public void buildRSSPlusGraph(float k, int year) {
        rssPlusGraph = new HashMap<>();
        for (int authorId1 : coAuthorGraph.keySet()) {
            if (coAuthorGraph.get(authorId1).size() == 0) {
                rssPlusGraph.put(authorId1, new HashMap<Integer, Float>());
            } else {
                int totalPaperOfAuthor1 = 0;
                for (int authorId2 : coAuthorGraph.get(authorId1).keySet()) {
                    totalPaperOfAuthor1 += coAuthorGraph.get(authorId1).get(authorId2);
                }

                float m = 0;
                boolean isContainKey1 = coAuthorGraphNear.containsKey(authorId1);
                boolean isContainKey2 = coAuthorGraphFar.containsKey(authorId1);
                if (isContainKey1) {
                    for (int authorId2 : coAuthorGraphNear.get(authorId1).keySet()) {
                        m += k * coAuthorGraphNear.get(authorId1).get(authorId2);
                    }
                }
                if (isContainKey2) {
                    for (int authorId2 : coAuthorGraphFar.get(authorId1).keySet()) {
                        m += (1 - k) * coAuthorGraphFar.get(authorId1).get(authorId2);
                    }
                }

                for (int authorId2 : coAuthorGraph.get(authorId1).keySet()) {
                    if (authorId1 != authorId2) {
                        float t = 0;
                        if (isContainKey1 && coAuthorGraphNear.get(authorId1).containsKey(authorId2)) {
                            t += k * coAuthorGraphNear.get(authorId1).get(authorId2);
                        }
                        if (isContainKey2 && coAuthorGraphFar.get(authorId1).containsKey(authorId2)) {
                            t += (1 - k) * coAuthorGraphFar.get(authorId1).get(authorId2);
                        }

                        float weight = t / m;
                        HashMap<Integer, Float> rtbvsWeight = rssPlusGraph.get(authorId1);
                        if (rtbvsWeight == null) {
                            rtbvsWeight = new HashMap<>();
                        }

                        Float _weight = rtbvsWeight.get(authorId2);
                        if (_weight == null) {
                            _weight = weight;
                            rtbvsWeight.put(authorId2, _weight);
                        }
                        rssPlusGraph.put(authorId1, rtbvsWeight);
                    }
                }
            }
        }
    }

    /**
     * rssDoublePlusGraph (Weight of links are number of collaborations of two
     * neighbor authors considering trend factor based on exponential function).
     */
    public void buildRSSDoublePlusGraph() {
        rssDoublePlusGraph = new HashMap<>();
        for (int authorID : expFunctBasedCoAuthorGraph.keySet()) {
            if (expFunctBasedCoAuthorGraph.get(authorID).size() == 0) {
                rssDoublePlusGraph.put(authorID, new HashMap<Integer, Float>());
            } else {
                // Mau so
                HashMap<Integer, Integer> deltaTimeCollaborationHM;
                double mauso = 0;
                for (int coAuthorID : expFunctBasedCoAuthorGraph.get(authorID).keySet()) {
                    deltaTimeCollaborationHM = expFunctBasedCoAuthorGraph.get(authorID).get(coAuthorID);
                    double tuso = 0;
                    for (int deltaTime : deltaTimeCollaborationHM.keySet()) {
                        int numberOfCollaboration = deltaTimeCollaborationHM.get(deltaTime);
                        tuso += (double) numberOfCollaboration * (Math.exp(-deltaTime));
                    }
                    mauso += tuso;
                }

                HashMap<Integer, Float> rssDoublePlusWeightHM = new HashMap<>();
                for (int coAuthorID : expFunctBasedCoAuthorGraph.get(authorID).keySet()) {
                    deltaTimeCollaborationHM = expFunctBasedCoAuthorGraph.get(authorID).get(coAuthorID);
                    double tuso = 0;
                    for (int deltaTime : deltaTimeCollaborationHM.keySet()) {
                        int numberOfCollaboration = deltaTimeCollaborationHM.get(deltaTime);
                        tuso += (double) numberOfCollaboration * (Math.exp(-deltaTime));
                    }

                    double rssDoublePlusWeight = 0;
                    if (mauso != 0) rssDoublePlusWeight = tuso / mauso;
                    rssDoublePlusWeightHM.put(coAuthorID, (float) rssDoublePlusWeight);
                }
                rssDoublePlusGraph.put(authorID, rssDoublePlusWeightHM);
            }
        }
    }

    public HashSet<Integer> GetAllAuthorNearTest() {
        HashSet<Integer> listAuthor = new HashSet<>();

        for (int aid : nearTestingData.keySet()) {
            if (!listAuthor.contains(aid)) {
                listAuthor.add(aid);
            }
            for (int aid2 : nearTestingData.get(aid)) {
                if (!listAuthor.contains(aid2)) {
                    listAuthor.add(aid2);
                }
            }
        }
        return listAuthor;
    }

    public boolean isLinkExistInRSSGraph(HashMap<Integer, HashMap<Integer, Float>> rssGraph, int authorID1, int authorID2) {
        boolean found = false;
        if (rssGraph.containsKey(authorID1)) {
            if (rssGraph.get(authorID1).containsKey(authorID2)) {
                found = true;
            }
        }
        if (rssGraph.containsKey(authorID2)) {
            if (rssGraph.get(authorID2).containsKey(authorID1)) {
                found = true;
            }
        }

        return found;
    }

    public boolean isLinkExistInFutureNet(HashMap<Integer, ArrayList<Integer>> futureGraph, int authorID1, int authorID2) {
        boolean found = false;
        if (futureGraph.containsKey(authorID1)) {
            ArrayList<Integer> listCoAuthor = futureGraph.get(authorID1);
            for (int i = 0; i < listCoAuthor.size(); i++) {
                int coAuthorID = listCoAuthor.get(i);
                if (coAuthorID == authorID2) {
                    found = true;
                }
            }
        }
        if (futureGraph.containsKey(authorID2)) {
            ArrayList<Integer> listCoAuthor = futureGraph.get(authorID2);
            for (int i = 0; i < listCoAuthor.size(); i++) {
                int coAuthorID = listCoAuthor.get(i);
                if (coAuthorID == authorID1) {
                    found = true;
                }
            }
        }

        return found;
    }

    // Testing Functions of AuthorGraph
    public static void main(String args[]) {
//        System.out.println("START LOADING TRAINING DATA");
//        AuthorGraph _graph = AuthorGraph.getInstance();
//        
//        _graph.LoadTrainingData("C:\\CRS-Experiment\\Sampledata\\[Training]AuthorId_PaperID.txt", 
//                "C:\\CRS-Experiment\\Sampledata\\[Training]PaperID_Year.txt");
//
//        // Building Graphs
//        _graph.BuidCoAuthorGraph();
//        _graph.buildRSSGraph();
//        
//        HashMap temp1 = _graph.coAuthorGraph;
//        HashMap temp2 = _graph.rssGraph;
//        
//        PageRank pr = new PageRank();
//        HashMap<Integer, HashMap<Integer, Float>> inLinkHM = pr.initInLinkHMFromGraph(temp2);
//        
//        System.out.println("DONE");
    }
}
