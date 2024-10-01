/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package student;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import rs.etf.sab.operations.VehicleOperations;

/**
 *
 * @author Nikola
 */
public class kn210113_Vehicle implements VehicleOperations{

    @Override
    public boolean insertVehicle(String licencePlateNumber, int fuelType, BigDecimal fuelConsumption) {
        Connection conn = kn210113_DB.getInstance().getConnection();
        String sql = "insert into Vozilo (RegBroj, TipGoriva, Potrosnja) values (?, ?, ?)";
        try(PreparedStatement ps = conn.prepareStatement(sql);){
            ps.setString(1, licencePlateNumber);
            ps.setInt(2, fuelType);
            ps.setBigDecimal(3, fuelConsumption);
            
            int affectedRows= ps.executeUpdate();
            return affectedRows>0;
        } catch (SQLException ex) {
            return false;
        }
        
    }

    @Override
    public int deleteVehicles(String... licencePlateNumbers) {
        Connection conn = kn210113_DB.getInstance().getConnection();
        if (licencePlateNumbers == null || licencePlateNumbers.length == 0) {
            return 0;
        }
        StringBuilder sql = new StringBuilder("delete from Vozilo where RegBroj in(");
        for(int i=0;i<licencePlateNumbers.length;i++){
            sql.append("?");
            if(i<licencePlateNumbers.length -1 ){
                sql.append(", ");
            }
        }
        sql.append(")");
        
        try(PreparedStatement ps = conn.prepareStatement(sql.toString());){
            for (int i = 0; i < licencePlateNumbers.length; i++) {
                ps.setString(i + 1, licencePlateNumbers[i]);
            }
            int affectedRows = ps.executeUpdate();
            return affectedRows;
        } catch (SQLException ex) {
            return 0;
        }
    }

    @Override
    public List<String> getAllVehichles() {
        List<String> vozila = new LinkedList<>();
        Connection conn = kn210113_DB.getInstance().getConnection();
        try(Statement st = conn.createStatement();
                ResultSet rs = st.executeQuery("select * from Vozilo");){
            
            while(rs.next()){
                vozila.add(rs.getString("RegBroj"));
            }
            
        } catch (SQLException ex) {
            Logger.getLogger(kn210113_Vehicle.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return vozila;
    }

    @Override
    public boolean changeFuelType(String licensePlateNumber, int fuelType) {
       Connection conn = kn210113_DB.getInstance().getConnection();
       String sql = "update Vozilo set TipGoriva = ? where RegBroj = ?";
       try(PreparedStatement ps = conn.prepareStatement(sql);){
           ps.setInt(1, fuelType);
           ps.setString(2, licensePlateNumber);
           
           int affectedRows = ps.executeUpdate();
           
           return affectedRows>0;
       } catch (SQLException ex) {
            return false;
        }
       
    }

    @Override
    public boolean changeConsumption(String licensePlateNumber, BigDecimal fuelConsumption) {
        Connection conn = kn210113_DB.getInstance().getConnection();
        String sql = "update Vozilo set Potrosnja = ? where RegBroj = ?";
        try(PreparedStatement ps = conn.prepareStatement(sql);){
            ps.setBigDecimal(1, fuelConsumption);
            ps.setString(2, licensePlateNumber);
            
            int affectedRows = ps.executeUpdate();
            
            return affectedRows>0;
            
        } catch (SQLException ex) {
            return false;
        }
        
    }
    
}
