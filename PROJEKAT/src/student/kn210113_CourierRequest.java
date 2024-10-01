/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package student;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import rs.etf.sab.operations.CourierRequestOperation;

/**
 *
 * @author Nikola
 */
public class kn210113_CourierRequest implements CourierRequestOperation{
    private static Map<String, String> zahtevi = new HashMap<>();
    @Override
    public boolean insertCourierRequest(String userName, String licencePlateNumber) {
        Connection conn = kn210113_DB.getInstance().getConnection();
        String sql = "select KorisnickoIme from Korisnik where KorisnickoIme = ?";
        String sql2 = "select RegBroj from Vozilo where RegBroj = ?";
        try(PreparedStatement ps1 = conn.prepareStatement(sql);
                PreparedStatement ps2 = conn.prepareStatement(sql2);){
            ps1.setString(1, userName);
            try(ResultSet rs1 = ps1.executeQuery();){
                if(!rs1.next()){
                    return false;
                }
            }
            
            ps2.setString(1, licencePlateNumber);
            try(ResultSet rs2 = ps2.executeQuery();){
                if(!rs2.next()){
                    return false;
                }
            }
            
        } catch (SQLException ex) {
            Logger.getLogger(kn210113_CourierRequest.class.getName()).log(Level.SEVERE, null, ex);
        }
        if(zahtevi.containsKey(userName)){
            return false;
        }
        zahtevi.put(userName, licencePlateNumber);
        return true;
    }

    @Override
    public boolean deleteCourierRequest(String userName) {
        if (zahtevi.containsKey(userName)) {
            zahtevi.remove(userName);
            return true;
        }
        return false;
    }

    @Override
    public boolean changeVehicleInCourierRequest(String userName, String licencePlateNumber) {
        Connection conn = kn210113_DB.getInstance().getConnection();
        String sql2 = "select RegBroj from Vozilo where RegBroj = ?";
        
        try(PreparedStatement ps2 = conn.prepareStatement(sql2);){
           
            ps2.setString(1, licencePlateNumber);
            try(ResultSet rs2 = ps2.executeQuery();){
                if(!rs2.next()){
                    return false;
                }
            }
            
        } catch (SQLException ex) {
            Logger.getLogger(kn210113_CourierRequest.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        if (zahtevi.containsKey(userName)) {
            zahtevi.remove(userName);
            zahtevi.put(userName, licencePlateNumber);
            return true;
        }
        return false;
    }

    @Override
    public List<String> getAllCourierRequests() {
        List<String> requests = new ArrayList<>();
        for (Map.Entry<String, String> entry : zahtevi.entrySet()) {
            requests.add(entry.getKey() + ": " + entry.getValue());
        }
        return requests;
    }

    @Override
    public boolean grantRequest(String userName) {
        String licencePlateNumber = zahtevi.remove(userName);
        if (licencePlateNumber == null) {
            return false;
        }
        Connection conn = kn210113_DB.getInstance().getConnection();
        String sql = "{call DodajKurira(?, ?)}";
        String user = "select IdKor from Korisnik where KorisnickoIme = ?";
        String car = "select IdVoz from Vozilo where RegBroj = ?";
        try(CallableStatement cs = conn.prepareCall(sql);
                PreparedStatement ps1 = conn.prepareStatement(user);
                PreparedStatement ps2 = conn.prepareStatement(car);){
            
            ps1.setString(1, userName);
            int IdKor;
            try(ResultSet rs1 = ps1.executeQuery();){
                if(!rs1.next()){
                    return false;
                }
                IdKor = rs1.getInt("IdKor");
            }
            
            ps2.setString(1, licencePlateNumber);
            int IdVoz;
            try(ResultSet rs2 = ps2.executeQuery();){
                if(!rs2.next()){
                    return false;
                }
                IdVoz = rs2.getInt("IdVoz");
            }
            
            cs.setInt(1, IdKor);
            cs.setInt(2, IdVoz);
            cs.execute();
            return true;
        } catch (SQLException ex) {
            Logger.getLogger(kn210113_CourierRequest.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }
    
    public static void eraseAll() {
        zahtevi.clear();
    }
    
    
    public static void main(String[] args) {
        kn210113_CourierRequest courierRequest = new kn210113_CourierRequest();
        System.out.println("Testiranje insertCourierRequest:");
        System.out.println("Inserting request 1: " + courierRequest.insertCourierRequest("user1", "ABC123"));
        System.out.println("Inserting request 2: " + courierRequest.insertCourierRequest("user2", "XYZ789"));
        System.out.println("Inserting request 1 again: " + courierRequest.insertCourierRequest("user1", "DEF456"));
        System.out.println();
        
        System.out.println("Testiranje getAllCourierRequests:");
        List<String> allRequests = courierRequest.getAllCourierRequests();
        for (String request : allRequests) {
            System.out.println("Request: " + request);
        }
        System.out.println();
        
        System.out.println("Testiranje changeVehicleInCourierRequest:");
        System.out.println("Changing vehicle for user1: " + courierRequest.changeVehicleInCourierRequest("user1", "GHI789"));
        System.out.println("Changing vehicle for user3 (non-existent): " + courierRequest.changeVehicleInCourierRequest("user3", "JKL012"));
        System.out.println();
        
        
        System.out.println("Testiranje grantRequest:");
        System.out.println("Granting request for user2: " + courierRequest.grantRequest("user2"));
        System.out.println("Granting request for user3 (non-existent): " + courierRequest.grantRequest("user3"));
        System.out.println();
        
        System.out.println("Testiranje deleteCourierRequest:");
        System.out.println("Deleting request for user1: " + courierRequest.deleteCourierRequest("user1"));
        System.out.println("Deleting request for user3 (non-existent): " + courierRequest.deleteCourierRequest("user3"));
        System.out.println();
        
        System.out.println("Testiranje getAllCourierRequests nakon brisanja:");
        allRequests = courierRequest.getAllCourierRequests();
        for (String request : allRequests) {
            System.out.println("Request: " + request);
        }
        System.out.println();
    }
}
