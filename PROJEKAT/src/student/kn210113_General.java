/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package student;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import rs.etf.sab.operations.GeneralOperations;

/**
 *
 * @author Nikola
 */
public class kn210113_General implements GeneralOperations{

    @Override
    public void eraseAll() {
        Connection conn = kn210113_DB.getInstance().getConnection();
        String[] sqls = {
            "DELETE FROM Voznja",
            "DELETE FROM Ponuda",
            "DELETE FROM Paket",
            "DELETE FROM Kurir",
            "DELETE FROM Administrator",
            "DELETE FROM Korisnik",
            "DELETE FROM Vozilo",
            "DELETE FROM Opstina",
            "DELETE FROM Grad"
        };
        try(Statement st = conn.createStatement();){
            
            for (String sql : sqls) {
                st.executeUpdate(sql);
            }
            
            
            kn210113_CourierRequest.eraseAll();
        } catch (SQLException ex) {
            Logger.getLogger(kn210113_General.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
    }
    
}
