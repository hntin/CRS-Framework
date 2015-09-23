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
    
    
    private final String dbDriver = "";
    private final String dbURL = "";
    private final String dbUsername ="";
    private final String dbPassword = "";
    private final String dataDir = "";
    private Connection con;
    private PreparedStatement stmt;
    
    public Connection getConnection(){
        try {
            con = DriverManager.getConnection(dbURL, dbURL, dbPassword);
        } catch (SQLException ex) {
            System.out.println("Cannot get database connection");
        }
        return con;
    }
    public void close(){
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
        String sql = "SELECT ap.idPaper FROM Author_Paper ap WHERE ap.idAuthor = ?" + 
                        "INNER JOIN paper p ON ap.idPaper = p.idPaper";
        PreparedStatement stmt;
        ResultSet rs;
        int numOfAuthors = 1222;//lay tu database
        try {
            stmt = con.prepareStatement(sql);
            for (int i = 0; i <= numOfAuthors;i++){
                stmt.setInt(1,i);
                rs = stmt.executeQuery();
            }
            rs = stmt.executeQuery();
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseTool.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    //
    public void getAuthorByTime(int fromYear, int toYear){
        String sql = "SELECT ap.idPaper from Author_Paper ap, Paper p where ap.idAuthor = ?";
    }
}
