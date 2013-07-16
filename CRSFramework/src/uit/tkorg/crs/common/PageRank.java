package uit.tkorg.crs.common;

import java.util.HashMap;
import uit.tkorg.crs.graph.Graph;

/**
 *
 * @author TinHuynh
 */
public class PageRank {
    // _PageRankResult<NodeID, RankOfNode>
    HashMap<Integer, Float> _PageRankResult = new HashMap<>();
    HashMap<Integer, HashMap<Integer, Float>> _graph = null;
    HashMap<Integer, HashMap<Integer, Float>> _inLinkHM = new HashMap<>();
    HashMap<Integer, HashMap<Integer, Float>> _outLinkHM = new HashMap<>(); // rssGraph is exactly outLinkHM
    int N; // Number of Page/Node
    public float d; // damping factor
    public int iterationNumber;

    public PageRank(HashMap<Integer, HashMap<Integer, Float>> graph, int iterationNum, float df) {
        _graph = graph;
        N = _graph.size();
        d = df;
        iterationNumber = iterationNum;
    }

    public void initPageRank() {
        float initValuePR = (float) 1 / (float) N;
        for (int id : _graph.keySet()) {
            _PageRankResult.put(id, initValuePR);
        }
    }

    /**
     * @return HashMap<NodeID, ImportantRate>
     */
    public HashMap<Integer, Float> calculatePR() {
        HashMap<Integer, Float> npg = new HashMap<>();
        initInLinkHMFromGraph(_graph);
        initOutLinkHMFromGraph(_graph);
        initPageRank();

        float currentPR = 0f;
        while (iterationNumber > 0) {
            float dp = 0;

            for (int id : _graph.keySet()) {
                // get Pageank from random jump
                currentPR = dp + (float)(1 - d)/(float)N;
                if (_inLinkHM.get(id) != null) {
                    for (int inLinkID : _inLinkHM.get(id).keySet()) {
                        // get PageRank from inlinks
                        currentPR = currentPR + ((float)d*_PageRankResult.get(inLinkID))/_outLinkHM.get(inLinkID).size();
                    }
                }
                npg.put(id, currentPR);
            }

            // update PageRank
            for (int id : _graph.keySet()) {
                _PageRankResult.put(id, npg.get(id));
            }
            
            iterationNumber = iterationNumber - 1;
        }

        return _PageRankResult;
    }

    /**
     * initInLinkHMFromGraph
     *
     * @param graph: is exactly rssGraph
     */
    public HashMap<Integer, HashMap<Integer, Float>> initInLinkHMFromGraph(HashMap<Integer, HashMap<Integer, Float>> graph) {
        HashMap<Integer, Float> inLinkEachNode = null;
        for (int id1 : graph.keySet()) {
            for (int id2 : graph.get(id1).keySet()) {
                // Inlink to id2 from id1
                inLinkEachNode = _inLinkHM.get(id2);
                if (inLinkEachNode == null) {
                    inLinkEachNode = new HashMap<>();
                }
                inLinkEachNode.put(id1, graph.get(id1).get(id2));

                _inLinkHM.put(id2, inLinkEachNode);
            }
        }

        return _inLinkHM;
    }

    public void initOutLinkHMFromGraph(HashMap<Integer, HashMap<Integer, Float>> graph) {
        _outLinkHM = graph;
    }

    // Testing Functions of Graph
    public static void main(String args[]) {
        System.out.println("START LOADING TRAINING DATA");
        HashMap<Integer, HashMap<Integer, Float>> graph = new HashMap<>();
        
        HashMap<Integer, Float> link_Node1 = new HashMap<>();
        link_Node1.put(2, 1f);
        link_Node1.put(3, 1f);
        link_Node1.put(6, 1f);
        graph.put(1, link_Node1);
        
        HashMap<Integer, Float> link_Node2 = new HashMap<>();
        link_Node2.put(3, 1f);
        link_Node2.put(4, 1f);
        link_Node2.put(5, 1f);
        link_Node2.put(6, 1f);
        graph.put(2, link_Node2);
        
        HashMap<Integer, Float> link_Node3 = new HashMap<>();
        link_Node3.put(4, 1f);
        link_Node3.put(5, 1f);
        graph.put(3, link_Node3);
        
        HashMap<Integer, Float> link_Node4 = new HashMap<>();
        link_Node4.put(1, 1f);
        link_Node4.put(3, 1f);
        link_Node4.put(5, 1f);
        link_Node4.put(6, 1f);
        graph.put(4, link_Node4);
        
        HashMap<Integer, Float> link_Node5 = new HashMap<>();
        link_Node5.put(1, 1f);
        graph.put(5, link_Node5);
        
        HashMap<Integer, Float> link_Node6 = new HashMap<>();
        link_Node6.put(1, 1f);
        link_Node6.put(2, 1f);
        link_Node6.put(5, 1f);
        graph.put(6, link_Node6);
        
        PageRank pr = new PageRank(graph, 100000, 0.85f);
        HashMap<Integer, Float> resultPR = pr.calculatePR();

        System.out.println("PAGE RANK RESULT ...");
        for (int id : resultPR.keySet()) {
            System.out.println(resultPR.get(id));
        }
        System.out.println("DONE");
    }
}
