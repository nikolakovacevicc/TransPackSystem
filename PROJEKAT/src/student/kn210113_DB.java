/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package student;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Nikola
 */
public class kn210113_DB {
    private static final String username = "sa";
    private static final String password = "123";
    private static final String database = "TransportPaketa";
    private static final int port = 1433;
    private static final String server = "localhost";
    
    private static final String connectionUrl
            = "jdbc:sqlserver://" + server + ":" + port
            + ";databaseName=" + database
            + ";encrypt=true"
            + ";trustServerCertificate=true";
    
    private static kn210113_DB db = null;
    
    private Connection connection;
    
    public Connection getConnection() {
        return connection;
    }
    
    private kn210113_DB(){
        try {
            connection = DriverManager.getConnection(connectionUrl, username, password);
        } catch (SQLException ex) {
            Logger.getLogger(kn210113_DB.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static kn210113_DB getInstance(){
        if(db==null){
            db = new kn210113_DB();
        }
        return db;
    }
}
