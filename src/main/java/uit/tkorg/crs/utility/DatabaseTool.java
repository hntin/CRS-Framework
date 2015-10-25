/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uit.tkorg.crs.utility;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author thucnt
 */
public class DatabaseTool {

    private final String dbDriver = "com.mysql.jdbc.Driver";
    private String dbURL = "jdbc:mysql://localhost:3306/mas";
    private String dbUsername = "root";
    private String dbPassword = "thuc1980";
    private String dataDir = "/1.CRS-ExperimetalData/TrainingData/";
    private Connection con;
    private PreparedStatement stmt;
    ResultSet rs;

    public static void main(String[] args) {
        DatabaseTool dbTool = new DatabaseTool();
        dbTool.connect();
        //ArrayList ls = dbTool.getAuthorByTime(2004, 2006);
        //dbTool.getAuthorsProfiles(ls);
        //dbTool.getAuthorIDPaperIDbyTime(0, 2000, "AuthorID_PaperID_Before_2001.txt");
        //dbTool.getPaperIDYearbyTime(0, 2000, "PaperID_Year_Before_2001.txt");
        dbTool.disconnect();
        System.out.println("DONE!");
    }
    
    public DatabaseTool(){
    }
    public DatabaseTool(String url, String username, String password){
        dbURL = url;
        dbUsername = username;
        dbPassword = password;
    }
    
    public Connection connect() {
        try {
            con = DriverManager.getConnection(dbURL, dbUsername, dbPassword);
        } catch (SQLException ex) {
            System.out.println("Cannot get database connection");
        }
        return con;
    }
   
    public void disconnect() {
        try {
            con.close();
        } catch (SQLException e) {
            System.out.println("Cannot close database connection");
        }
    }

    //tao cac file luu tru profile cua tung tac gia; ten file la idAuthor
    //cau truc file
    //dong 1: idAuthor
    //dong i: idPaper#Title#Abstract#Year
    public void getAuthorsProfiles(ArrayList list) {
        String sql = "SELECT p.idPaper, p.title, p.abstract, p.year FROM Author_Paper ap "
                + "INNER JOIN paper p ON ap.idPaper = p.idPaper WHERE ap.idAuthor = ?";
        PreparedStatement stmt;
        ResultSet rs;
        int numOfAuthors = list.size();
        System.out.println(numOfAuthors);
        try {
            stmt = con.prepareStatement(sql);
            File dir = new File(dataDir + "\\0");
            dir.mkdir();
            for (int i = 0; i < numOfAuthors; i++) {
                if ((i % 10000) == 0) {
                    dir = new File(dataDir + "\\" + i);
                    dir.mkdir();
                }
                int idAuthor = (Integer) list.get(i);
                System.out.println(idAuthor + "\t" + i + " < " + numOfAuthors);
                stmt.setInt(1, idAuthor);
                rs = stmt.executeQuery();
                TextFileUtility.writeTextFile(dir, idAuthor, rs);
            }

        } catch (SQLException ex) {
            System.out.println(ex);
        }
    }

    //
    public ArrayList getAuthorByTime(int fromYear, int toYear) {
        String sql = "SELECT DISTINCT author_paper.idAuthor "
                + "FROM paper,author_paper "
                + "WHERE paper.idPaper = author_paper.idPaper and "
                + "paper.year >= ? and paper.year <= ? ORDER BY author_paper.idAuthor ASC";

        ArrayList<Integer> listOfAuthors = new ArrayList<Integer>();
        PreparedStatement stmt;
        ResultSet rs;
        try {
            stmt = con.prepareStatement(sql);
            stmt.setInt(1, fromYear);
            stmt.setInt(2, toYear);
            rs = stmt.executeQuery();
            while (rs.next()) {
                listOfAuthors.add(rs.getInt(1));
            }
        } catch (SQLException ex) {
            System.out.println(ex);
        }
        return listOfAuthors;
    }

    /**
     * Lay danh sach AuthorID_PaperID trong khoang fromYear den toYear
     * @param fromYear
     * @param toYear
     */
    public void getAuthorIDPaperIDbyTime(int fromYear, int toYear, String outFileName) {
        String sql = "Select author_paper.idAuthor, paper.idPaper"
                + " From author_paper, paper"
                + " Where paper.idPaper = author_paper.idPaper and "
                + " paper.year >= ? and paper.year <= ? ORDER BY paper.idPaper";

        PreparedStatement stmt;
        ResultSet rs;
        try {
            stmt = con.prepareStatement(sql);
            stmt.setInt(1, fromYear);
            stmt.setInt(2, toYear);
            rs = stmt.executeQuery();
            StringBuffer strBuff = new StringBuffer();
            strBuff.append("AuthorID \t PaperID \n");
            while (rs != null && rs.next()) {
                strBuff.append(rs.getString("idAuthor") + "\t");
                strBuff.append(rs.getString("idPaper") + "\n");
            }
            TextFileUtility.writeTextFile(dataDir + outFileName, strBuff.toString());
        } catch (SQLException ex) {
            System.out.println(ex);
        }
    }
    
    public void getPaperIDYearbyTime(int fromYear, int toYear, String outFileName) {
        String sql = " Select p.idPaper, p.year " +
                        " From mas.paper p" +
                        " Where p.year >= ? and p.year <= ?";

        PreparedStatement stmt;
        ResultSet rs;
        try {
            stmt = con.prepareStatement(sql);
            stmt.setInt(1, fromYear);
            stmt.setInt(2, toYear);
            rs = stmt.executeQuery();
            StringBuffer strBuff = new StringBuffer();
            strBuff.append("PaperID \t Year \n");
            while (rs != null && rs.next()) {
                strBuff.append(rs.getString("idPaper") + "\t");
                strBuff.append(rs.getString("year") + "\n");
            }
            TextFileUtility.writeTextFile(dataDir + outFileName, strBuff.toString());
        } catch (SQLException ex) {
            System.out.println(ex);
        }
    }
    
    public ResultSet getPapersByYear(int year){
        String sql = "SELECT paper.idPaper, paper.abstract " +
                     "FROM paper " +
                     "WHERE paper.year <= ? " +
                     "ORDER BY paper.idPaper ASC";
        try {
            stmt = con.prepareStatement(sql);
            stmt.setInt(1,year);
            rs = stmt.executeQuery();
        } catch (SQLException ex) {
            System.out.println(ex);
        }
        return rs;
    }
    
    public ResultSet getPapersByAuthor(int authorId, int year){
        String sql = "SELECT p.idPaper, p.title, p.year " +
                     "FROM paper p, author_paper ap " +
                     "WHERE p.idPaper= ap.idPaper and ap.idAuthor = ? and p.year <= ?";
//        System.out.println(sql);
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement(sql);
            stmt.setInt(1, authorId);
            stmt.setInt(2, year);
            rs = stmt.executeQuery();
        } catch (SQLException ex) {
            Logger.getLogger(uit.tkorg.crs.utility.DatabaseTool.class.getName()).log(Level.SEVERE, null, ex);
        }
        return rs;
    }
}
