package uit.tkorg.crs.graph;

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
public class OrganizationGraph {

    public HashMap<Integer, HashMap<Integer, Float>> _rssORGGraph;
    public HashMap<Integer, HashMap<Integer, Integer>> _collaborativeOrgGraph;
    public HashMap<Integer, Integer> _authorID_OrgID;
    public HashMap<Integer, ArrayList<Integer>> _paperOrganization;
    public HashMap<Integer, ArrayList<Integer>> _organizationPaper;
    
    public OrganizationGraph() {
        _collaborativeOrgGraph = new HashMap<>();
        _rssORGGraph = new HashMap<>();
    }

    public void loadDataFromTextFile(String file_AuthorID_PubID_OrgID) {
        try {
            _paperOrganization = new HashMap<>();
            _organizationPaper = new HashMap<>();
            FileInputStream fis = new FileInputStream(file_AuthorID_PubID_OrgID);
            Reader reader = new InputStreamReader(fis, "UTF8");
            BufferedReader bufferReader = new BufferedReader(reader);
            // Skip the first line (the header)
            bufferReader.readLine();
            String line = null;
            String[] tokens;
            int authorId;
            int paperId;
            int orgId;
            while ((line = bufferReader.readLine()) != null && !line.equals("")) {
                tokens = line.split(",");
                if (tokens[0] != null && !tokens[0].equals("")) {
                    authorId = Integer.parseInt(tokens[0]);
                } else {
                    authorId = -1;
                }
                if (tokens[1] != null && !tokens[1].equals("")) {
                    paperId = Integer.parseInt(tokens[1]);
                } else {
                    paperId = -1;
                }
                if (tokens[2] != null && !tokens[2].equals("")) {
                    orgId = Integer.parseInt(tokens[2]);
                } else {
                    orgId = -1;
                }

                ArrayList<Integer> listPaper = _organizationPaper.get(orgId);
                if (listPaper == null) {
                    listPaper = new ArrayList<>();
                }
                listPaper.add(paperId);
                _organizationPaper.put(orgId, listPaper);

                ArrayList<Integer> listOrg = _paperOrganization.get(paperId);
                if (listOrg == null) {
                    listOrg = new ArrayList<>();
                }
                listOrg.add(orgId);
                _paperOrganization.put(paperId, listOrg);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Build graphs CollaborativeOrgGraph (weight is number of collaborations)
     */
    public void buildCollaborativeOrgGraph() {
        for (int pubId : _paperOrganization.keySet()) {
            ArrayList<Integer> listOrgs = _paperOrganization.get(pubId);
            if (listOrgs.size() == 1 && !_collaborativeOrgGraph.containsKey(listOrgs.get(0))) {
                _collaborativeOrgGraph.put(listOrgs.get(0), new HashMap<Integer, Integer>());
            } else {
                for (int org1 : listOrgs) {
                    for (int org2 : listOrgs) {
                        if (org1 != org2) {
                            HashMap<Integer, Integer> collaboration;
                            collaboration = _collaborativeOrgGraph.get(org1);
                            if (collaboration == null) {
                                collaboration = new HashMap<>();
                            }

                            Integer numofPaper = collaboration.get(org2);
                            if (numofPaper == null) {
                                numofPaper = 0;
                            }
                            numofPaper++;
                            collaboration.put(org2, numofPaper);
                            _collaborativeOrgGraph.put(org1, collaboration);
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Build RSSOrgGraph (weight is relation strength of different organizations.)
     */
    public void buildRSSOrgGraph() {
        
    }

    public static void main(String args[]) {
        OrganizationGraph orgGraph = new OrganizationGraph();
        orgGraph.loadDataFromTextFile("C:\\CRS-Experiment\\Sampledata\\Input\\Link-Net\\[Training]AuthorId_PaperID_OrgID.txt");
        orgGraph.buildCollaborativeOrgGraph();
    }
}
