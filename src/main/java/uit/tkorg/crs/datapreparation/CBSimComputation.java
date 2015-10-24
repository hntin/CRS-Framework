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
import java.util.logging.Level;
import java.util.logging.Logger;
import uit.tkorg.crs.constant.Constant;
import uit.tkorg.crs.datapreparation.CBFAuthorFVComputation;
import uit.tkorg.crs.model.Author;
import uit.tkorg.crs.model.Paper;
import uit.tkorg.utility.common.BinaryFileUtility;
import uit.tkorg.utility.common.DatabaseTool;
import uit.tkorg.utility.common.NumericUtility;

public class CBSimComputation {

    // Prevent instantiation.
    private CBSimComputation() {

    }
    
    public static HashMap<String, Paper> readPaperList(int authorID, int year) throws Exception {
        HashMap<String, Paper> papers = new HashMap();
        DatabaseTool db = new DatabaseTool();
        db.connect();
        ResultSet rs = db.getPapersByAuthor(authorID,year);
        while (rs.next()) {
            Paper paper = new Paper();
            String paperId = rs.getInt(1) + "";
            paper.setPaperId(paperId);
            paper.setPaperTitle(rs.getString(2));
            paper.setYear(rs.getInt(3));
            papers.put(paperId, paper);
        }
        db.disconnect();
        return papers;
    }

    public static void createTestDatabase() {
        String filePath = "input/4.txt";
        String sql = "INSERT INTO paper (idPaper, title, abstract, year) values (?, ?, ?, ?)";
        try {
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/test", "root", "thuc1980");
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setInt(1, 4);
            statement.setString(2, "Paper 4");
            InputStream inputStream = new FileInputStream(new File(filePath));
            statement.setBlob(3, inputStream);
            statement.setInt(4, 2015);
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

    public static void buildAuthorProfile(int authorId,int year) {
        HashMap<String, Paper> papers = null;
        HashMapVector fv = null;
        try {
            papers = readPaperList(authorId,year);
        } catch (Exception ex) {
            Logger.getLogger(CBSimComputation.class.getName()).log(Level.SEVERE, null, ex);
        }
        List<String> paperList = new ArrayList<String>(papers.keySet());
        Author author = new Author();
        author.setAuthorId("1");
        author.setPaperList(paperList);
        try {
            fv = CBFAuthorFVComputation.computeAuthorFV(author, papers, 1, 0.5);
        } catch (Exception ex) {
            Logger.getLogger(CBSimComputation.class.getName()).log(Level.SEVERE, null, ex);
        }
        author.setFeatureVector(fv);
    }

    /**
     * Test.
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
//        createTestDatabase();
//        CBFPaperFVComputation.vectorzie("input/", "output/TFIDF/");
        CBFPaperFVComputation.vectorzie(2005, "output/TFIDF/");

        HashMap<String, Paper> papers = readPaperList(1,2005);
        List<String> paperList = new ArrayList<String>(papers.keySet());

        Author author1 = new Author();
        author1.setAuthorId("1");
        author1.setPaperList(paperList);
        HashMapVector fv = CBFAuthorFVComputation.computeAuthorFV(author1, papers, 1, 0.5);
        author1.setFeatureVector(fv);

        papers = readPaperList(2,2005);
        paperList = new ArrayList<String>(papers.keySet());
        Author author2 = new Author();
        author2.setAuthorId("2");
        author2.setPaperList(paperList);
        fv = CBFAuthorFVComputation.computeAuthorFV(author2, papers, 1, 0.5);
        author2.setFeatureVector(fv);

        papers = readPaperList(3,2005);
        paperList = new ArrayList<String>(papers.keySet());
        Author author3 = new Author();
        author3.setAuthorId("3");
        author3.setPaperList(paperList);
        fv = CBFAuthorFVComputation.computeAuthorFV(author3, papers, 1, 0.5);
        author3.setFeatureVector(fv);

        System.out.println("Tac gia 1: " + author1.getFeatureVector());
        System.out.println("Tac gia 2: " + author2.getFeatureVector());
        System.out.println("Tac gia 3: " + author3.getFeatureVector());
    }
}
