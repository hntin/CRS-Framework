package uit.tkorg.crs.common;

import java.util.HashMap;

/**
 *
 * @author TinHuynh
 */
public class PageRank {

    HashMap<Integer, HashMap<Integer, Float>> _graph = null;
    HashMap<Integer, HashMap<Integer, Float>> _inLinkHM = new HashMap<>();
    HashMap<Integer, HashMap<Integer, Float>> _outLinkHM = new HashMap<>(); // rssGraph is exactly outLinkHM
    int numberNode;
    public float dampingFactor;
    public int iterationNumber;

    public PageRank() {
    }

    public void initPageRank() {
        _graph = null; //?
        numberNode = _graph.size();
        dampingFactor = 0.85f;
        iterationNumber = 1000;
    }

    /**
     * @return HashMap<NodeID, ImportantRate>
     */
    public HashMap<Integer, Float> calculatePR() {

        return null;
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
}
