package uit.tkorg.crs.graph;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import uit.tkorg.crs.common.PageRank;
import uit.tkorg.utility.TextFileUtility;

/**
 *
 * @author TinHuynh
 */
public class CitationGraph {
    // <AuthorID, <AuthorID_Cited, NumberOfCitation>>
    // <AuthorID, <AuthorID_RefTo, NumberOfRef>>

    private HashMap<Integer, HashMap<Integer, Integer>> _referenceNumberGraph;
    private HashMap<Integer, HashMap<Integer, Float>> _referenceRSSGraph;
    private HashMap<Integer, HashMap<Integer, Integer>> _citationNumberGraph;
    private HashMap<Integer, HashMap<Integer, Float>> _citationRSSGraph;
    private HashMap<Integer, ArrayList<Integer>> _paperID_RefID_List;
    private HashMap<Integer, ArrayList<Integer>> _paperID_CitedID_List;
    private HashMap<Integer, ArrayList<Integer>> _paperID_AuthorID_List;
    private HashMap<Integer, ArrayList<Integer>> _authorID_PaperID_List;

    private void load_AuthorID_PaperID(String file_AuthorID_PaperID) throws Exception {
        try {
            _authorID_PaperID_List = new HashMap<>();
            _paperID_AuthorID_List = new HashMap<>();
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
                if (tokens.length == 2) {
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
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void load_PaperID_RefID(String file_PaperID_RefID) throws Exception {
        try {
            _paperID_RefID_List = new HashMap<>();
            _paperID_CitedID_List = new HashMap<>();

            FileInputStream fis = new FileInputStream(file_PaperID_RefID);
            Reader reader = new InputStreamReader(fis, "UTF8");
            BufferedReader bufferReader = new BufferedReader(reader);
            bufferReader.readLine();
            String line = null;
            String[] tokens;
            int paperId;
            int year;
            int refId;
            while ((line = bufferReader.readLine()) != null) {
                tokens = line.split(",");
                if (tokens.length == 3) {
                    paperId = Integer.parseInt(tokens[0]);
                    year = Integer.parseInt(tokens[1]);
                    refId = Integer.parseInt(tokens[2]);

                    ArrayList<Integer> refIDList = _paperID_RefID_List.get(paperId);
                    if (refIDList == null) {
                        refIDList = new ArrayList<>();
                    }
                    refIDList.add(refId);
                    _paperID_RefID_List.put(paperId, refIDList);

                    ArrayList<Integer> citedIDList = _paperID_CitedID_List.get(refId);
                    if (citedIDList == null) {
                        citedIDList = new ArrayList<>();
                    }
                    citedIDList.add(paperId);
                    _paperID_CitedID_List.put(refId, citedIDList);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void buildRefGraph() throws Exception {
        _referenceNumberGraph = new HashMap<>();
        for (int paperID : _paperID_RefID_List.keySet()) {
            ArrayList<Integer> refIDList = _paperID_RefID_List.get(paperID);
            ArrayList<Integer> authorIDList = _paperID_AuthorID_List.get(paperID);
            for (int paperIDRef : refIDList) {
                ArrayList<Integer> refAuthorIDList = _paperID_AuthorID_List.get(paperIDRef);
                for (int authorID : authorIDList) {
                    HashMap<Integer, Integer> refHM = _referenceNumberGraph.get(authorID);
                    if (refHM == null) {
                        refHM = new HashMap<>();
                    }

                    for (int refAuthorID : refAuthorIDList) {
                        int numberOfRef = 0;
                        if (refHM.containsKey(refAuthorID)) {
                            numberOfRef = refHM.get(refAuthorID);
                        }

                        numberOfRef++;
                        refHM.put(refAuthorID, numberOfRef);
                    }

                    _referenceNumberGraph.put(authorID, refHM);
                }
            }
        }
        
        for (int authorID : _authorID_PaperID_List.keySet()){
            if (!_referenceNumberGraph.containsKey(authorID)) {
                _referenceNumberGraph.put(authorID, new HashMap<Integer, Integer>());
            }
        }
    }

    private HashMap<Integer, HashMap<Integer, Float>> buildRefRSSGraph() throws Exception {
        _referenceRSSGraph = new HashMap<>();
        for (int authorID : _referenceNumberGraph.keySet()) {
            HashMap<Integer, Integer> refIDHM = _referenceNumberGraph.get(authorID);
            int TotalNumberOfRef = 0;
            for (int refID : refIDHM.keySet()) {
                TotalNumberOfRef += refIDHM.get(refID);
            }

            float refRSSValue = 0f;
            HashMap<Integer, Float> rssRefIDHM = new HashMap<>();
            for (int refID : refIDHM.keySet()) {
                refRSSValue = (float) refIDHM.get(refID) / (float) TotalNumberOfRef;
                rssRefIDHM.put(refID, refRSSValue);
            }
            _referenceRSSGraph.put(authorID, rssRefIDHM);
        }
        
        return _referenceRSSGraph;
    }

    private void buildCitationGraph() {
    }

    private void buildCitationRSSGraph() {
    }

    public static void main(String args[]) {
        try {
            System.out.println("START...");
            CitationGraph citedGraph = new CitationGraph();
            citedGraph.load_AuthorID_PaperID("C:\\CRS-Experiment\\Sampledata\\Input\\Link-Net\\[Training]AuthorId_PaperID_Before_2005.txt");
            citedGraph.load_PaperID_RefID("C:\\CRS-Experiment\\Sampledata\\Input\\Link-Net\\[Training]PaperID_Year_RefID_Before_2005.txt");
            citedGraph.buildRefGraph();
            HashMap<Integer, HashMap<Integer, Float>> refRSSGraph = citedGraph.buildRefRSSGraph();

            PageRank pr = new PageRank(refRSSGraph, 1000, 0.85f);
            HashMap<Integer, Float> authorID_PageRank_HM = pr.calculatePR();

            System.out.println("PAGE RANK RESULT ...");
            StringBuffer strBuff = new StringBuffer();
            strBuff.append("AuthorID" + "\t" + "ImportantRate(PageRank)" + "\n");
            for (int authorID : authorID_PageRank_HM.keySet()) {
                strBuff.append(authorID + "\t" + authorID_PageRank_HM.get(authorID) + "\n");
            }
            TextFileUtility.writeTextFile("C:\\CRS-Experiment\\Sampledata\\Output\\ImportantRate\\pagerank.txt", strBuff.toString());
            System.out.println("END...");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
