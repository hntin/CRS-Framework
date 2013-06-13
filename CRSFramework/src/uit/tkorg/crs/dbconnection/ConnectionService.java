/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uit.tkorg.crs.dbconnection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

/**
 *
 * @author TinHuynh
 */
public class ConnectionService {
    public static String dbURL;
    public static String dbUserName;
    public static String dbPassword;
            
    protected static void loadJDBCDriver() throws Exception {
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (java.lang.ClassNotFoundException e) {
            throw new Exception("SQL JDBC Driver not found ...");
        }
    }

    public static Connection getConnection() throws Exception {
        Connection connect = null;
        if (connect == null) {
            loadJDBCDriver();
            try {
                connect = DriverManager.getConnection(dbURL, dbUserName, dbPassword);
            } catch (java.sql.SQLException e) {
                throw new Exception("Can not access to Database Server ..." + dbURL + e.getMessage());
            }
        }
        return connect;
    }
}
