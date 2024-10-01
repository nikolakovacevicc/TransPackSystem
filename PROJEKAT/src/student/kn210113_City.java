/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package student;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import rs.etf.sab.operations.CityOperations;


/**
 *
 * @author Nikola
 */
public class kn210113_City implements CityOperations{

    @Override
    public int insertCity(String name, String postalCode) {
        Connection conn = kn210113_DB.getInstance().getConnection();
        String sql = "insert into Grad (Naziv, PostanskiBroj) values (?, ?)";
        String provera = "select IdGra from Grad where Naziv=? and PostanskiBroj=?";
        ResultSet generatedKeys = null;
        try(PreparedStatement ps = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
                PreparedStatement ps2 = conn.prepareStatement(provera);){
            
            ps2.setString(1, name);
            ps2.setString(2, postalCode);
            try(ResultSet rs = ps2.executeQuery();){
                if(rs.next()){
                    return -1;
                }
            }
            
            ps.setString(1, name);
            ps.setString(2, postalCode);
            
            int affectedRows = ps.executeUpdate();
            
            if(affectedRows > 0){
                generatedKeys = ps.getGeneratedKeys();
                if(generatedKeys.next()){
                    return generatedKeys.getInt(1);
                }
            }
            
            return -1;
            
        } catch (SQLException ex) {
            return -1;
        }
        
    }

    @Override
    public int deleteCity(String... names) {
        if (names == null || names.length == 0) {
            return 0; 
        }
        Connection conn = kn210113_DB.getInstance().getConnection();
        
        StringBuilder upit = new StringBuilder("DELETE FROM GRAD WHERE Naziv IN (");
        for (int i = 0; i < names.length; i++) {
            upit.append("?");
            if (i < names.length - 1) {
                upit.append(",");
            }
        }
        upit.append(")");
        
        try(PreparedStatement ps = conn.prepareStatement(upit.toString());){
            
            for (int i = 0; i < names.length; i++) {
                ps.setString(i + 1, names[i]);
            }
            
            return ps.executeUpdate();
            
        } catch (SQLException ex) {
            return -1;
        }
        
        
    }

    @Override
    public boolean deleteCity(int i) {
        Connection conn = kn210113_DB.getInstance().getConnection();
        String sql = "DELETE FROM Grad WHERE IdGra = ?";
        try(PreparedStatement ps = conn.prepareStatement(sql);){
            
            ps.setInt(1, i);
            
            int affectedRows = ps.executeUpdate();
            
            return affectedRows > 0;
            
        } catch (SQLException ex) {
            return false;
        }
        
    }

    @Override
    public List<Integer> getAllCities() {
        Connection conn = kn210113_DB.getInstance().getConnection();
        List<Integer> gradovi = new ArrayList<>();
        
        try(Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("select * from Grad");){
            
            while (rs.next()) {
                gradovi.add(rs.getInt("IdGra"));
            }
            
        } catch (SQLException ex) {
            Logger.getLogger(kn210113_City.class.getName()).log(Level.SEVERE, null, ex);
        }
        return gradovi;
    }
    
    
    
}
