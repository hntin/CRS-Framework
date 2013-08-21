/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uit.tkorg.crs.isolatedauthor.feature;

import java.util.HashMap;
import uit.tkorg.crs.common.PageRank;
import uit.tkorg.crs.graph.CitationGraph;
import uit.tkorg.utility.TextFileUtility;

/**
 *
 * @author TinHuynh
 */
public class ImportantRate {

    public void processImportantRate() {
        try {
            System.out.println("START...");
            CitationGraph citedGraph = new CitationGraph(
                    "C:\\CRS-Experiment\\MAS\\Input\\Input2\\[TrainingData]AuthorID_PaperID_All.txt",
                    "C:\\CRS-Experiment\\MAS\\Input\\Input2\\[TrainingData]PaperID_Year_ReferenceID_1995_2005.txt");
            citedGraph.calculateImportantRate("C:\\CRS-Experiment\\MAS\\Output\\pagerank.txt");
            System.out.println("END...");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    public static void main(String args[]){
        (new ImportantRate()).processImportantRate();
    }
}
