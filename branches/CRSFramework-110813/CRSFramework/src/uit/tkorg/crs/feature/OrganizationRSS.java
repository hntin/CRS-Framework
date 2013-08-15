/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uit.tkorg.crs.feature;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import uit.tkorg.crs.graph.OrganizationGraph;
import uit.tkorg.utility.TextFileUtility;

/**
 *
 * @author TinHuynh
 */
public class OrganizationRSS {

    private HashMap<Integer, Integer> _authorID_OrgID;
    private HashMap<Integer, HashMap<Integer, Float>> _rssOrgData;
    private HashMap<Integer, HashMap<Integer, Float>> _rssOrgGraph;

    public OrganizationRSS() {
        _rssOrgData = new HashMap<>();
    }

    private HashMap<Integer, String> loadInputAuthorList(String input_Author_List_File) {
        HashMap<Integer, String> listAuthorRandom = new HashMap<>();
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
                    listAuthorRandom.put(authorId, groupLMD);
                }
            }
            bufferReader.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        // </editor-fold>
        return listAuthorRandom;
    }

    private void load_All_AuthorID_OrgID(String file_AuthorID_OrgID) {
        try {
            _authorID_OrgID = new HashMap<>();
            FileInputStream fis = new FileInputStream(file_AuthorID_OrgID);
            Reader reader = new InputStreamReader(fis, "UTF8");
            BufferedReader bufferReader = new BufferedReader(reader);
            // Skip the first line (the header)
            bufferReader.readLine();
            String line = null;
            String[] tokens;
            int authorId;
            int orgId;
            while ((line = bufferReader.readLine()) != null && !line.equals("")) {
                tokens = line.split(",");
                if (tokens.length > 0 && tokens[0] != null && !tokens[0].equals("")) {
                    authorId = Integer.parseInt(tokens[0]);
                } else {
                    authorId = -1;
                }
                if (tokens.length > 1 && tokens[1] != null && !tokens[1].equals("")) {
                    orgId = Integer.parseInt(tokens[1]);
                } else {
                    orgId = -1;
                }

                if (_authorID_OrgID.get(authorId) == null) {
                    _authorID_OrgID.put(authorId, orgId);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void runOrgRSS(int inputAuthorId) {
        int inputAuthorOrgId = _authorID_OrgID.get(inputAuthorId);
        HashMap<Integer, Float> listRSS = new HashMap<>();
        listRSS.put(inputAuthorOrgId, 1f);
        Set<Integer> listOrgFirstHop = _rssOrgGraph.get(inputAuthorOrgId).keySet();
        for (int orgId_FirstHop : listOrgFirstHop) {
            Set<Integer> listOrgSecondHop = _rssOrgGraph.get(orgId_FirstHop).keySet();
            for (int orgId_SecondHop : listOrgSecondHop) {
                if (inputAuthorOrgId != orgId_SecondHop) {
                    Float weight = _rssOrgGraph.get(inputAuthorOrgId).get(orgId_FirstHop);
                    Float weight2 = _rssOrgGraph.get(orgId_FirstHop).get(orgId_SecondHop);
                    if (weight != null && weight2 != null) {
                        weight *= weight2;
                    } else {
                        weight = 0f;
                    }

                    if (weight > 0f) {
                        Float totalWeight = listRSS.get(orgId_SecondHop);
                        if (totalWeight == null) {
                            totalWeight = 0f;
                        }
                        totalWeight += weight;
                        listRSS.put(orgId_SecondHop, totalWeight);
                    }
                }
            }
        }

        Set<Integer> listId = listRSS.keySet();
        for (int orgId : listId) {
            Float weight = _rssOrgGraph.get(inputAuthorOrgId).get(orgId);
            if (weight != null && weight > 0f) {
                Float totalWeight = listRSS.get(orgId);
                totalWeight += weight;
                listRSS.put(orgId, totalWeight);
            }
        }

        for (int otherOrgId : _rssOrgGraph.get(inputAuthorOrgId).keySet()) {
            if (!listId.contains(otherOrgId)) {
                listRSS.put(otherOrgId, _rssOrgGraph.get(inputAuthorOrgId).get(otherOrgId));
            }
        }
        _rssOrgData.put(inputAuthorOrgId, listRSS);
    }

    public HashMap<Integer, HashMap<Integer, Float>> processOrgRSS(HashMap<Integer, HashMap<Integer, Float>> rssOrgGraph,
            HashMap<Integer, String> listAuthor) {

        _rssOrgGraph = rssOrgGraph;
        Runtime runtime = Runtime.getRuntime();
        int numOfProcessors = runtime.availableProcessors();

        ExecutorService executor = Executors.newFixedThreadPool(numOfProcessors - 1);
        for (final int authorId : listAuthor.keySet()) {
            executor.submit(new Runnable() {
                @Override
                public void run() {
                    runOrgRSS(authorId);
                }
            });
        }

        executor.shutdown();
        while (!executor.isTerminated()) {
        }
        return _rssOrgData;
    }

    public void writeOrgRSSofInputAuthorToFile(String pathName, HashMap<Integer, String> listInputAuthor) {
        HashMap<Integer, HashMap<Integer, Float>> authorOrgRSSResult = new HashMap<>();
        for (int authorID : listInputAuthor.keySet()) {
            int orgID = _authorID_OrgID.get(authorID);
            HashMap<Integer, Float> rssOrgHM = _rssOrgData.get(orgID);
            HashMap<Integer, Float> authorSim_OrgRSS_HM = authorOrgRSSResult.get(authorID);
            for (int otherAuthorID : _authorID_OrgID.keySet()) {
                if (authorID != otherAuthorID) {
                    int otherOrgID = _authorID_OrgID.get(otherAuthorID);

                    float orgRSSValue = 0.f;
                    if (rssOrgHM != null && rssOrgHM.size() > 0) {
                        if (rssOrgHM.containsKey(otherOrgID)) {
                            orgRSSValue = rssOrgHM.get(otherOrgID);
                        }
                    }

                    if (authorSim_OrgRSS_HM == null) {
                        authorSim_OrgRSS_HM = new HashMap<>();
                    }
                    authorSim_OrgRSS_HM.put(otherAuthorID, orgRSSValue);
                }
            }
            authorOrgRSSResult.put(authorID, authorSim_OrgRSS_HM);
        }

        // Write the result to the text file
        for (int authorID : authorOrgRSSResult.keySet()) {
            StringBuffer strBuff = new StringBuffer();
            HashMap<Integer, Float> authorOrgRSSValueHM = authorOrgRSSResult.get(authorID);
            TextFileUtility.writeTextFile(pathName + "\\" + authorID + ".txt", authorOrgRSSValueHM);
        }
    }

    public static void main(String args[]) {
        try {
            OrganizationRSS orgRSS = new OrganizationRSS();
            OrganizationGraph orgGraph = OrganizationGraph.getInstance();
            orgGraph.load_AuthorID_PaperID_OrgID("C:\\CRS-Experiment\\Sampledata\\Input\\Link-Net\\[Training]AuthorId_PaperID_OrgID.txt");
            orgRSS.load_All_AuthorID_OrgID("C:\\CRS-Experiment\\Sampledata\\Input\\Link-Net\\[Training]AuthorId_OrgID.txt");
            HashMap<Integer, String> listInputAuthor = orgRSS.loadInputAuthorList("C:\\CRS-Experiment\\Sampledata\\Input\\ListRandomAuthor_.txt");

            orgGraph.buildCollaborativeOrgGraph();
            orgGraph.buildRSSOrgGraph();

            orgRSS.processOrgRSS(orgGraph._rssORGGraph, listInputAuthor);
            orgRSS.writeOrgRSSofInputAuthorToFile(
                    "C:\\CRS-Experiment\\Sampledata\\Output\\OrgRSS",
                    listInputAuthor);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
