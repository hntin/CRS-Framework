/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uit.tkorg.crs.model;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author TinHuynh
 */
public class CoAuthorGraph {

    //<editor-fold defaultstate="collapsed" desc="Member Variables">
    private static CoAuthorGraph _instance;
    // weighted, non-directed graph, <AuthorID, <CoAuthorID, NumOfCollaboration>> 
    public HashMap<Integer, HashMap<Integer, Integer>> _coAuthorGraph; 
    // Weighted, directed graph, <AuthorID, <PotentialCoAuthorID, NormalizedWeight>>
    public HashMap<Integer, HashMap<Integer, Float>> _rssGraph;  
    // Weighted, directed graph, <AuthorID, <PotentialCoAuthorID, NormalizedWeight>>, delta(t)
    public HashMap<Integer, HashMap<Integer, Float>> _rssPlusGraph; 
    //weighted, directed graph, <AuthorID, <PotentialCoAuthorID, NormalizedWeight>>, exp(delta(t))
    public HashMap<Integer, HashMap<Integer, Float>> _rssDoublePlusGraph; 
    // <AuthorID, <CoAuthorID, <Delta(t), NumOfCollaboration>>>
    private HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>> _expFunctBasedCoAuthorGraph; 
    private HashMap<Integer, Integer> _paperId_Year;
    private HashMap<Integer, ArrayList<Integer>> _authorPaper;
    private HashMap<Integer, ArrayList<Integer>> _paperAuthor;
    // </editor-fold>

    public HashMap<Integer, Integer> getPaperId_Year() {
        return _paperId_Year;
    }

    public void setPaperId_Year(HashMap<Integer, Integer> _paperId_Year) {
        this._paperId_Year = _paperId_Year;
    }

    public HashMap<Integer, ArrayList<Integer>> getAuthorPaper() {
        return _authorPaper;
    }

    public void setAuthorPaper(HashMap<Integer, ArrayList<Integer>> _authorPaper) {
        this._authorPaper = _authorPaper;
    }

    public HashMap<Integer, ArrayList<Integer>> getPaperAuthor() {
        return _paperAuthor;
    }

    public void setPaperAuthor(HashMap<Integer, ArrayList<Integer>> _paperAuthor) {
        this._paperAuthor = _paperAuthor;
    }

    public static CoAuthorGraph getInstance() {
        if (_instance == null) {
            _instance = new CoAuthorGraph();
        }
        return _instance;
    }

    public CoAuthorGraph() {
        _coAuthorGraph = new HashMap<>();
        _expFunctBasedCoAuthorGraph = new HashMap<>();
    }

    public CoAuthorGraph(String file_AuthorID_PaperID, String file_PaperID_Year) {
        _coAuthorGraph = new HashMap<>();
        this.load_AuthorID_PaperID_File(file_AuthorID_PaperID);
        this.load_PaperID_Year_File(file_PaperID_Year);
        this.buildCoAuthorGraph();
        this.buildRSSGraph();
    }

    public CoAuthorGraph(String file_AuthorID_PaperID, String file_PaperID_Year, int firstYear, int lastYear) {
        _coAuthorGraph = new HashMap<>();
        this.load_AuthorID_PaperID_File(file_AuthorID_PaperID);
        this.load_PaperID_Year_File(file_PaperID_Year);
        this.buildCoAuthorGraph();        
        _expFunctBasedCoAuthorGraph = new HashMap<>();
        this.buildExpFuntBasedCoAuthorGraph(firstYear, lastYear);
        this.buildRSSDoublePlusGraph();
    }

    public void load_AuthorID_PaperID_File(String file_AuthorID_PaperID) {
        try {
            _authorPaper = new HashMap<>();
            _paperAuthor = new HashMap<>();
            FileInputStream fis = new FileInputStream(file_AuthorID_PaperID);
            Reader reader = new InputStreamReader(fis, "UTF8");
            BufferedReader bufferReader = new BufferedReader(reader);
            bufferReader.readLine();
            String line = null;
            String[] tokens;
            int authorId;
            int paperId;
            while ((line = bufferReader.readLine()) != null) {
                tokens = line.split(",");
                authorId = Integer.parseInt(tokens[0]);
                paperId = Integer.parseInt(tokens[1]);

                ArrayList<Integer> listPaper = _authorPaper.get(authorId);
                if (listPaper == null) {
                    listPaper = new ArrayList<>();
                }
                listPaper.add(paperId);
                _authorPaper.put(authorId, listPaper);

                ArrayList<Integer> listAuthor = _paperAuthor.get(paperId);
                if (listAuthor == null) {
                    listAuthor = new ArrayList<>();
                }
                listAuthor.add(authorId);
                _paperAuthor.put(paperId, listAuthor);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void load_PaperID_Year_File(String file_PaperID_Year) {
        try {
            _paperId_Year = new HashMap<>();
            FileInputStream fis = new FileInputStream(file_PaperID_Year);
            Reader reader = new InputStreamReader(fis, "UTF8");
            BufferedReader bufferReader = new BufferedReader(reader);
            bufferReader.readLine();
            String line = null;
            String[] tokens;
            int paperId;
            Integer year;
            while ((line = bufferReader.readLine()) != null) {
                tokens = line.split(",");
                paperId = Integer.parseInt(tokens[0]);
                if (tokens.length <= 1) {
                    year = 0;
                } else {
                    year = Integer.parseInt(tokens[1]);
                }
                _paperId_Year.put(paperId, year);
            }
            bufferReader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * coAuthorGraph (Weight of links are number of collaborations of two
     * neighbor author).
     */
    public void buildCoAuthorGraph() {
        for (int pubId : _paperAuthor.keySet()) {
            ArrayList<Integer> listAuthors = _paperAuthor.get(pubId);
            if (listAuthors.size() == 1 && !_coAuthorGraph.containsKey(listAuthors.get(0))) {
                _coAuthorGraph.put(listAuthors.get(0), new HashMap<Integer, Integer>());
            } else {
                for (int author1 : listAuthors) {
                    for (int author2 : listAuthors) {
                        if (author1 != author2) {
                            HashMap<Integer, Integer> collaboration;
                            collaboration = _coAuthorGraph.get(author1);
                            if (collaboration == null) {
                                collaboration = new HashMap<>();
                            }

                            Integer numofPaper = collaboration.get(author2);
                            if (numofPaper == null) {
                                numofPaper = 0;
                            }
                            numofPaper++;
                            collaboration.put(author2, numofPaper);
                            _coAuthorGraph.put(author1, collaboration);
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
        _rssGraph = new HashMap<>();
        for (int authorId1 : _coAuthorGraph.keySet()) {
            if (_coAuthorGraph.get(authorId1).size() == 0) {
                _rssGraph.put(authorId1, new HashMap<Integer, Float>());
            } else {
                int totalCollaborationOfAuthor1 = 0;
                for (int authorId2 : _coAuthorGraph.get(authorId1).keySet()) {
                    totalCollaborationOfAuthor1 += _coAuthorGraph.get(authorId1).get(authorId2);
                }

                for (int authorId2 : _coAuthorGraph.get(authorId1).keySet()) {
                    if (authorId1 != authorId2) {
                        float t = 0;
                        float weight = ((float) _coAuthorGraph.get(authorId1).get(authorId2))
                                / ((float) totalCollaborationOfAuthor1);
                        HashMap<Integer, Float> rssWeight = _rssGraph.get(authorId1);
                        if (rssWeight == null) {
                            rssWeight = new HashMap<>();
                        }

                        Float existedWeight = rssWeight.get(authorId2);
                        if (existedWeight == null) {
                            existedWeight = weight;
                            rssWeight.put(authorId2, existedWeight);
                        }
                        _rssGraph.put(authorId1, rssWeight);
                    }
                }
            }
        }
    }
    
    /**
     *
     * @param firstYear
     * @param lastYear
     */
    public void buildExpFuntBasedCoAuthorGraph(int firstYear, int lastYear) {
        for (int paperID : _paperAuthor.keySet()) {
            int yearOfPaper = _paperId_Year.get(paperID);
            int deltaTime = lastYear - yearOfPaper;
            ArrayList<Integer> listAuthors = _paperAuthor.get(paperID);
            if (listAuthors.size() == 1 && !_expFunctBasedCoAuthorGraph.containsKey(listAuthors.get(0))) {
                _expFunctBasedCoAuthorGraph.put(listAuthors.get(0), new HashMap<Integer, HashMap<Integer, Integer>>());
            } else {
                for (int author1 : listAuthors) {
                    for (int author2 : listAuthors) {
                        if (author1 != author2) {
                            HashMap<Integer, HashMap<Integer, Integer>> timeCollabHM_1 = _expFunctBasedCoAuthorGraph.get(author1);
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
                            _expFunctBasedCoAuthorGraph.put(author1, timeCollabHM_1);
                        }
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
        _rssDoublePlusGraph = new HashMap<>();
        for (int authorID : _expFunctBasedCoAuthorGraph.keySet()) {
            if (_expFunctBasedCoAuthorGraph.get(authorID).size() == 0) {
                _rssDoublePlusGraph.put(authorID, new HashMap<Integer, Float>());
            } else {
                // Mau so
                HashMap<Integer, Integer> deltaTimeCollaborationHM;
                double mauso = 0;
                for (int coAuthorID : _expFunctBasedCoAuthorGraph.get(authorID).keySet()) {
                    deltaTimeCollaborationHM = _expFunctBasedCoAuthorGraph.get(authorID).get(coAuthorID);
                    double tuso = 0;
                    for (int deltaTime : deltaTimeCollaborationHM.keySet()) {
                        int numberOfCollaboration = deltaTimeCollaborationHM.get(deltaTime);
                        tuso += (double) numberOfCollaboration * (Math.exp(-deltaTime));
                    }
                    mauso += tuso;
                }

                HashMap<Integer, Float> rssDoublePlusWeightHM = new HashMap<>();
                for (int coAuthorID : _expFunctBasedCoAuthorGraph.get(authorID).keySet()) {
                    deltaTimeCollaborationHM = _expFunctBasedCoAuthorGraph.get(authorID).get(coAuthorID);
                    double tuso = 0;
                    for (int deltaTime : deltaTimeCollaborationHM.keySet()) {
                        int numberOfCollaboration = deltaTimeCollaborationHM.get(deltaTime);
                        tuso += (double) numberOfCollaboration * (Math.exp(-deltaTime));
                    }

                    double rssDoublePlusWeight = 0;
                    if (mauso != 0) {
                        rssDoublePlusWeight = tuso / mauso;
                    }
                    rssDoublePlusWeightHM.put(coAuthorID, (float) rssDoublePlusWeight);
                }
                _rssDoublePlusGraph.put(authorID, rssDoublePlusWeightHM);
            }
        }
    }

    public static boolean isLinkExistInCoAuthorGraph(HashMap<Integer, HashMap<Integer, Integer>> coAuthorGraph, int authorID1, int authorID2) {
        boolean found = false;
        if (coAuthorGraph.containsKey(authorID1)) {
            if (coAuthorGraph.get(authorID1).containsKey(authorID2)) {
                found = true;
            }
        }
        if (coAuthorGraph.containsKey(authorID2)) {
            if (coAuthorGraph.get(authorID2).containsKey(authorID1)) {
                found = true;
            }
        }

        return found;
    }

    // For tesing mathods in CoAuthorGraph
    public static void main(String args[]) {
        // Testing Data
        CoAuthorGraph G0 = new CoAuthorGraph("/1.CRS-ExperimetalData/SampleData/AuthorID_PaperID_Before_2003.txt",
                "/1.CRS-ExperimetalData/SampleData/PaperID_Year_Before_2003.txt");
        
        CoAuthorGraph G1 = new CoAuthorGraph("/1.CRS-ExperimetalData/SampleData/AuthorID_PaperID_2003_2005.txt",
                "/1.CRS-ExperimetalData/SampleData/PaperID_Year_2003_2005.txt", 2003, 2005);
        
        System.out.println("DONE");

    }
}
