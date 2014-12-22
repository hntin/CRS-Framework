/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
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

    private static AuthorGraph _instance;

    public static AuthorGraph getInstance() {
        if (_instance == null) {
            _instance = new AuthorGraph();
        }
        return _instance;
    }
    
    public HashMap<Integer, HashMap<Integer, Integer>> coAuthorGraph;
    private HashMap<Integer, HashMap<Integer, Integer>> coAuthorGraphNear;
    private HashMap<Integer, HashMap<Integer, Integer>> coAuthorGraphFar;
    public HashMap<Integer, HashMap<Integer, Float>> rssGraph; //weighted, directed graph
    public HashMap<Integer, HashMap<Integer, Float>> rtbvsGraph; //weighted, directed graph
    public HashMap<Integer, ArrayList<Integer>> nearTestingData; //non-weighted, non-directed graph <authorID, <Lis of CoAuthorID>>
    public HashMap<Integer, ArrayList<Integer>> farTestingData; //non-weighted, non-directed graph

    private AuthorGraph() {
        coAuthorGraph = new HashMap<>();
        coAuthorGraphNear = new HashMap<>();
        coAuthorGraphFar = new HashMap<>();
    }
    public HashMap<Integer, Integer> paperId_year;
    public HashMap<Integer, ArrayList<Integer>> authorPaper;
    public HashMap<Integer, ArrayList<Integer>> paperAuthor;

    /**
     * Load Training data from 2 text files are AuthorID_PaperID.txt and
     * PaperID_Year.txt and put into HashMaps are paperId_year, authorPaper,
     * paperAuthor
     *
     * @param fileAuthorIdPubId
     * @param filePubIdYear
     */
    public void LoadTrainingData(String fileAuthorIdPubId, String filePubIdYear) {
        try {
            paperId_year = new HashMap<>();
            FileInputStream fis = new FileInputStream(filePubIdYear);
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

        try {
            authorPaper = new HashMap<>();
            paperAuthor = new HashMap<>();
            FileInputStream fis = new FileInputStream(fileAuthorIdPubId);
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

    /**
     * Load testing data from 2 text files [NearTesting]AuthorId_PaperID.txt and
     * [FarTesting]AuthorId_PaperID.txt put into HashMaps are: nearTestingData,
     * farTestingData
     *
     * @param fileNearTestingData
     * @param fileFarTestingData
     */
    public void LoadTestingData(String fileNearTestingData, String fileFarTestingData) {
        HashMap<Integer, ArrayList<Integer>> paperAuthorTmp = new HashMap<>();
        try {
            nearTestingData = new HashMap<>();
            FileInputStream fis = new FileInputStream(fileNearTestingData);
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

        // Loading for the FarFuture Network
        paperAuthorTmp.clear();
        try {
            farTestingData = new HashMap<>();
            FileInputStream fis = new FileInputStream(fileFarTestingData);
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
        }

        for (int paperId : paperAuthorTmp.keySet()) {
            ArrayList<Integer> listAuthorId = paperAuthorTmp.get(paperId);
            for (int authorId1 : listAuthorId) {
                for (int authorId2 : listAuthorId) {
                    if (authorId2 > authorId1) {
                        ArrayList<Integer> listCollaboration = farTestingData.get(authorId1);
                        if (listCollaboration == null) {
                            listCollaboration = new ArrayList<>();
                        }
                        if (!listCollaboration.contains(authorId2)) {
                            listCollaboration.add(authorId2);
                            farTestingData.put(authorId1, listCollaboration);
                        }
                    }
                }
            }
        }
    }

    /**
     * Building all graphs
     *
     * @param k
     * @param year
     */
    public void BuildAllGraph(float k, int year) {
        BuildCoAuthorGraph();
        BuildNearFarCoAuthorGraph(year);
        BuildingRSSGraph();
        BuildingTrendGraph(k, year);
    }

    /**
     * Build graphs coAuthorGraph (weight is number of collations), rssGraph
     */
    public void BuildCoAuthorGraph() {
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

    /**
     * Building Near and Far Collaboration for the training CoAuthorGraph
     *
     * @param year
     */
    public void BuildNearFarCoAuthorGraph(int year) {
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

    public void BuildingRSSGraph() {
        rssGraph = new HashMap<>();
        for (int authorId1 : coAuthorGraph.keySet()) {
            if (coAuthorGraph.get(authorId1).size() == 0) {
                rssGraph.put(authorId1, new HashMap<Integer, Float>());
            } else {
                int totalPaperOfAuthor1 = 0;
                for (int authorId2 : coAuthorGraph.get(authorId1).keySet()) {
                    totalPaperOfAuthor1 += coAuthorGraph.get(authorId1).get(authorId2);
                }

                for (int authorId2 : coAuthorGraph.get(authorId1).keySet()) {
                    if (authorId1 != authorId2) {
                        float t = 0;
                        float weight = ((float) coAuthorGraph.get(authorId1).get(authorId2)) / ((float) totalPaperOfAuthor1);
                        HashMap<Integer, Float> rssWeight = rssGraph.get(authorId1);
                        if (rssWeight == null) {
                            rssWeight = new HashMap<>();
                        }

                        Float _weight = rssWeight.get(authorId2);
                        if (_weight == null) {
                            _weight = weight;
                            rssWeight.put(authorId2, _weight);
                        }
                        rssGraph.put(authorId1, rssWeight);
                    }
                }
            }
        }
    }

    public void BuildingTrendGraph(float k, int year) {
        rtbvsGraph = new HashMap<>();
        for (int authorId1 : coAuthorGraph.keySet()) {
            if (coAuthorGraph.get(authorId1).size() == 0) {
                rtbvsGraph.put(authorId1, new HashMap<Integer, Float>());
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
                        HashMap<Integer, Float> rtbvsWeight = rtbvsGraph.get(authorId1);
                        if (rtbvsWeight == null) {
                            rtbvsWeight = new HashMap<>();
                        }

                        Float _weight = rtbvsWeight.get(authorId2);
                        if (_weight == null) {
                            _weight = weight;
                            rtbvsWeight.put(authorId2, _weight);
                        }
                        rtbvsGraph.put(authorId1, rtbvsWeight);
                    }
                }
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

    public HashSet<Integer> GetAllAuthorFarTest() {
        HashSet<Integer> listAuthor = new HashSet<>();
        for (int aid : farTestingData.keySet()) {
            if (!listAuthor.contains(aid)) {
                listAuthor.add(aid);
            }
            for (int aid2 : farTestingData.get(aid)) {
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
            for (int i=0; i<listCoAuthor.size(); i++){
                int coAuthorID = listCoAuthor.get(i);
                if (coAuthorID == authorID2) {
                    found = true;
                }
            }
        }
        if (futureGraph.containsKey(authorID2)) {
            ArrayList<Integer> listCoAuthor = futureGraph.get(authorID2);
            for (int i=0; i<listCoAuthor.size(); i++){
                int coAuthorID = listCoAuthor.get(i);
                if (coAuthorID == authorID1) {
                    found = true;
                }
            }
        }
        
        return found;
    }

//    // Testing Functions of AuthorGraph
//    public static void main(String args[]) {
//        System.out.println("START LOADING TRAINING DATA");
//        AuthorGraph _graph = AuthorGraph.getInstance();
//        
//        _graph.LoadTrainingData("C:\\CRS-Experiment\\Sampledata\\[Training]AuthorId_PaperID.txt", 
//                "C:\\CRS-Experiment\\Sampledata\\[Training]PaperID_Year.txt");
//
//        // Building Graphs
//        _graph.BuidCoAuthorGraph();
//        _graph.BuildingRSSGraph();
//        
//        HashMap temp1 = _graph.coAuthorGraph;
//        HashMap temp2 = _graph.rssGraph;
//        
//        PageRank pr = new PageRank();
//        HashMap<Integer, HashMap<Integer, Float>> inLinkHM = pr.initInLinkHMFromGraph(temp2);
//        
//        System.out.println("DONE");
//    }
}
