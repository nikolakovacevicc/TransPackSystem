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
import rs.etf.sab.operations.DistrictOperations;

/**
 *
 * @author Nikola
 */
public class kn210113_District implements DistrictOperations{

    @Override
    public int insertDistrict(String name, int cityId, int xCord, int yCord) {
        Connection conn = kn210113_DB.getInstance().getConnection();
        String insert = "insert into Opstina (naziv, IdGra, Xkoordinata, YKoordinata) VALUES (?, ?, ?, ?)";
        
        try(PreparedStatement ps = conn.prepareStatement(insert, PreparedStatement.RETURN_GENERATED_KEYS);){
            ps.setString(1, name);
            ps.setInt(2, cityId);
            ps.setInt(3, xCord);
            ps.setInt(4, yCord);
            
            int affectedRows = ps.executeUpdate();
            
            if(affectedRows == 0 ){
                return -1;
            }
            
            try(ResultSet key = ps.getGeneratedKeys();){
                if (key.next()) {
                    return key.getInt(1);
                }
                else{
                    return -1;
                }
            }
            
        } catch (SQLException ex) {
            return -1;
        }
        
    }

    @Override
    public int deleteDistricts(String... names) {
        if (names == null || names.length == 0) {
            return 0;
        }
        Connection conn = kn210113_DB.getInstance().getConnection();
        
        StringBuilder delete = new StringBuilder("delete from Opstina where naziv in (");
        
        for (int i = 0; i < names.length; i++) {
            delete.append("?");
            if (i < names.length - 1) {
                delete.append(",");
            }
        }
        delete.append(")");
        
        try(PreparedStatement ps = conn.prepareStatement(delete.toString());){
            
            for (int i = 0; i < names.length; i++) {
                ps.setString(i + 1, names[i]);
            }
            
            int deletedRows = ps.executeUpdate();
            return deletedRows;
            
        } catch (SQLException ex) {
            return 0;
        }
        
    }

    @Override
    public boolean deleteDistrict(int i) {
        Connection conn = kn210113_DB.getInstance().getConnection();
        String delete = "delete from Opstina where IdOps= ?";
        
        try(PreparedStatement ps = conn.prepareStatement(delete);){
            
            ps.setInt(1, i);
            
            int deletedRows = ps.executeUpdate();
            
            return deletedRows>0;
            
            
        } catch (SQLException ex) {
            return false;
        }
        
    }

    @Override
    public int deleteAllDistrictsFromCity(String nameOfTheCity) {
        Connection conn = kn210113_DB.getInstance().getConnection();
        String sql = "delete from Opstina where IdGra=(select IdGra from Grad where Naziv = ?)";
       
        try(PreparedStatement ps = conn.prepareStatement(sql);){
            
            ps.setString(1, nameOfTheCity);
            
            int affectedRows = ps.executeUpdate();

            return affectedRows;
            
        } catch (SQLException ex) {
            return 0;
        }
        
        
    }

    @Override
    public List<Integer> getAllDistrictsFromCity(int idCity) {
        Connection conn = kn210113_DB.getInstance().getConnection();
        
        String sql = "select IdOps from Opstina where IdGra = ?";
        List<Integer> opstine = new ArrayList<>();
        
        try(PreparedStatement ps = conn.prepareStatement(sql);){
            
            ps.setInt(1, idCity);
            
            try(ResultSet rs = ps.executeQuery();){
                
                while(rs.next()){
                    opstine.add(rs.getInt(1));
                }
                
            }
            
            if (opstine.isEmpty()) {
                return null;
            }
            
        } catch (SQLException ex) {
            return null;
        }
        return opstine;
    }

    @Override
    public List<Integer> getAllDistricts() {
        Connection conn = kn210113_DB.getInstance().getConnection();
        
        List<Integer> opstine = new ArrayList<>();
        
        try(Statement s = conn.createStatement();
            ResultSet rs = s.executeQuery("select IdOps from Opstina");){
            
            
            
            while(rs.next()){
                opstine.add(rs.getInt(1));
            }
            
            if (opstine.isEmpty()) {
                return null;
            }
            
        } catch (SQLException ex) {
            return null;
        }
        return opstine;
    }
    
}
