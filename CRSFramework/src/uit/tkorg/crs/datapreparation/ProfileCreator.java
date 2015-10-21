/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uit.tkorg.crs.datapreparation;

import ir.vsr.HashMapVector;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import uit.tkorg.crs.constant.Constant;
import uit.tkorg.crs.datapreparation.CBFAuthorFVComputation;
import uit.tkorg.crs.model.Author;
import uit.tkorg.crs.model.Paper;
import uit.tkorg.utility.common.BinaryFileUtility;
import uit.tkorg.utility.common.DatabaseTool;
import uit.tkorg.utility.common.NumericUtility;

public class ProfileCreator {

    // Prevent instantiation.
    private ProfileCreator() {

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
    
    public static void createTestDatabase(){
        String filePath = "input/3.txt";
        String sql = "INSERT INTO paper (title, abstract, year) values (?, ?, ?)";
        try{ 
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/test","root","thuc1980");
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setString(1,"Paper 3");
            InputStream inputStream = new FileInputStream(new File(filePath));
            statement.setBlob(2, inputStream);
            statement.setInt(3,2015);
            int row = statement.executeUpdate();
            if (row > 0) {
                System.out.println("A paper was inserted");
            }
            conn.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
    /**
     * Test.
     * 
     * @param args
     * @throws Exception 
     */
    public static void main(String[] args) throws Exception {
//        createTestDatabase();
        HashMap<String,Paper> papers = readPaperList(1);
        List<String> paperList = new ArrayList<String>(papers.keySet());
        Author author = new Author();
        author.setAuthorId("1");
        author.setPaperList(paperList);
        HashMapVector fv = CBFAuthorFVComputation.computeAuthorFV(author,papers,1,0.5);
        author.setFeatureVector(fv);

        System.out.println("Tac gia 1: " + fv.toString());
        
        papers = readPaperList(1);
        paperList = new ArrayList<String>(papers.keySet());
        Author author2 = new Author();
        author2.setAuthorId("1");
        author2.setPaperList(paperList);
        fv = CBFAuthorFVComputation.computeAuthorFV(author2,papers,1,0.5);
        author2.setFeatureVector(fv);

        System.out.println("Tac gia 2: " + fv.toString());
    }
}
