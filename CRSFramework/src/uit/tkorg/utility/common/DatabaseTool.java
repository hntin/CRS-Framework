/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uit.tkorg.utility.common;

import java.sql.Connection;
import java.sql.DriverManager;
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
    private Connection con;
    
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
}
