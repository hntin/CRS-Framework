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
    
     /**
     * loadJDBCDriver
     * @throws Exception 
     */
    protected static void loadJDBCDriver() throws Exception {
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (java.lang.ClassNotFoundException e) {
            throw new Exception("SQL JDBC Driver not found ...");
        }
    }

    /**
     * Return the connection.
     *
     * @return Connection to the database
     * @throws NamingException
     * @throws SQLException
     */
    public static Connection getConnection() throws Exception {
        Connection connect = null;
        if (connect == null) {
            loadJDBCDriver();
            String jdbcutf8 = "&useUnicode=true&characterEncoding=UTF-8";
            String url = "jdbc:mysql://" + "localhost"
                    + ":" + "3306"
                    + "/" + "MAS"
                    + "?user=" + "root"
                    + "&password=" + "root"
                    + "&autoReconnect=true"
                    + "&connectTimeout=300";

            try {
                connect = DriverManager.getConnection("jdbc:mysql://localhost:3306/MAS?useUnicode=true&characterEncoding=UTF-8","root","root");
            } catch (java.sql.SQLException e) {
                throw new Exception("Can not access to Database Server ..." + url + e.getMessage());
            }
        }
        return connect;
    }
}
