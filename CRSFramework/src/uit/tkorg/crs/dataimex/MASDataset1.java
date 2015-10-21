/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uit.tkorg.crs.dataimex;

import ir.vsr.HashMapVector;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.ResultSet;
import java.util.*;
import uit.tkorg.crs.constant.PRConstant;
import uit.tkorg.crs.datapreparation.CBFAuthorFVComputation;
import uit.tkorg.crs.model.Author;
import uit.tkorg.crs.model.Paper;
import uit.tkorg.utility.general.BinaryFileUtility;
import uit.tkorg.utility.general.DatabaseTool;
import uit.tkorg.utility.general.NumericUtility;

public class MASDataset1 {

    // Prevent instantiation.
    private MASDataset1() {

    }
    
    public static HashMap<String, Paper> readPaperList(int authorID) throws Exception{
        HashMap<String, Paper> papers = new HashMap();
        DatabaseTool db = new DatabaseTool();
        db.connect();
        ResultSet rs = db.getPapersByAuthor(authorID);
        while (rs.next()){
            Paper paper = new Paper();
            String paperId = rs.getInt(1) + "";
            paper.setPaperId(paperId);
            paper.setPaperTitle(rs.getString(2));
            paper.setYear(rs.getInt(3));
            papers.put(paperId,paper);
        }
        db.disconnect();
      
//        Paper paper = new Paper();
//        String paperId = "1967";
//        paper.setPaperId(paperId);
//        paper.setPaperTitle("Paper ");
//        //paper.setPaperAbstract(paperAbstract);
//        paper.setYear(2000);
//        papers.put(paperId, paper);
//        
//        paper = new Paper();
//        paperId = "202";
//        paper.setPaperId(paperId);
//        paper.setPaperTitle("Paper 202");
//        //paper.setPaperAbstract(paperAbstract);
//        paper.setYear(2000);
//        papers.put(paperId, paper);
//        
//        paper = new Paper();
//        paperId = "2033";
//        paper.setPaperId(paperId);
//        paper.setPaperTitle("Paper 2033");
//        //paper.setPaperAbstract(paperAbstract);
//        paper.setYear(2000);
//        papers.put(paperId, paper);
        
        return papers;
    }
    
    /**
     * Test.
     * 
     * @param args
     * @throws Exception 
     */
    public static void main(String[] args) throws Exception {
        HashMap<String,Paper> papers = readPaperList(10000);
        List<String> paperList = new ArrayList<String>(papers.keySet());
        Author author = new Author();
        author.setAuthorId("10000");
        author.setPaperList(paperList);
        HashMapVector fv = CBFAuthorFVComputation.computeAuthorFV(author,papers,0,0.5);
        author.setFeatureVector(fv);

        System.out.println("Ket qua: " + fv.toString());
    }
}
