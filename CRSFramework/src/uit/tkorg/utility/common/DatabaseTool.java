/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uit.tkorg.utility.common;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author thucnt
 */
public class DatabaseTool {
    
    
    private final String dbDriver = "com.mysql.jdbc.Driver";
    private final String dbURL = "jdbc:mysql://localhost:3306/mas";
    private final String dbUsername = "root";
    private final String dbPassword = "root";
    private final String dataDir = "C:\\CRSData";
    private Connection con;
    private PreparedStatement stmt;
    
    public static void main(String[] args){
        DatabaseTool dbTool = new DatabaseTool();
        dbTool.connect();
        dbTool.getAuthorsProfiles();
        dbTool.disconnect();
    }
    
    public Connection connect(){
        try {
            con = DriverManager.getConnection(dbURL, dbUsername, dbPassword);
        } catch (SQLException ex) {
            System.out.println("Cannot get database connection");
        }
        return con;
    }
    public void disconnect(){
        try{
            con.close();
        }
        catch(SQLException e){
            System.out.println("Cannot close database connection");
        }
    }
    
    //tao cac file luu tru profile cua tung tac gia; ten file la idAuthor
    //cau truc file
    //dong 1: idAuthor
    //dong i: idPaper#Title#Abstract#Year
    public void getAuthorsProfiles(){
        String sql = "SELECT p.idPaper, p.title, p.abstract, p.year FROM Author_Paper ap " + 
                        "INNER JOIN paper p ON ap.idPaper = p.idPaper WHERE ap.idAuthor = ?";
        PreparedStatement stmt;
        ResultSet rs;
        int numOfAuthors = 10;//lay so luong tu database sau
        try {
            stmt = con.prepareStatement(sql);
            for (int i = 0; i < numOfAuthors;i++){
                stmt.setInt(1,i);
                rs = stmt.executeQuery();
                TextFileUtility.writeTextFile(dataDir,i,rs);
            }

        } catch (SQLException ex) {
            System.out.println(ex);
        }
    }
    
    //
    public void getAuthorByTime(int fromYear, int toYear){
        
    }
}
