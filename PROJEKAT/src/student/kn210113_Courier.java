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
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import rs.etf.sab.operations.CourierOperations;

/**
 *
 * @author Nikola
 */
public class kn210113_Courier implements CourierOperations{

    @Override
    public boolean insertCourier(String courierUserName, String licencePlateNumber) {
        Connection conn = kn210113_DB.getInstance().getConnection();
        String sql1 = "select IdKor from Korisnik where KorisnickoIme = ? ";
        String sql2 = "select IdKor from Kurir where IdKor = ?";
        String sql3 = "select IdVoz from Vozilo where RegBroj = ?";
        String sql4 = "select IdVoz from Kurir where IdVoz = ?";
        String insert = "insert into Kurir (IdKor, IdVoz, BrojIsporucenihPaketa, Profit, Status) values (?, ?, 0, 0, 0)";
        try(PreparedStatement ps1 = conn.prepareStatement(sql1);
            PreparedStatement ps2 = conn.prepareStatement(sql2);
            PreparedStatement ps3 = conn.prepareStatement(sql3);
            PreparedStatement ps4 = conn.prepareStatement(sql4);
            PreparedStatement ps5 = conn.prepareStatement(insert);
                ){
            ps1.setString(1, courierUserName);
            int IdKor;
            try(ResultSet rs1 = ps1.executeQuery();){
                if(!rs1.next()){
                    return false;
                }
                IdKor = rs1.getInt(1);
            }
            
            ps2.setInt(1, IdKor);
            
            try(ResultSet rs2 = ps2.executeQuery();){
                if(rs2.next()){
                    return false;
                }
            }
            
            ps3.setString(1, licencePlateNumber);
            int IdVoz;
            try(ResultSet rs3 = ps3.executeQuery()){
                if(!rs3.next()){
                    return false;
                }
                IdVoz = rs3.getInt(1);
            }
            
            ps4.setInt(1, IdVoz);
            
            try(ResultSet rs4 = ps4.executeQuery();){
                if(rs4.next()){
                    return false;
                }
                
            }
            
            ps5.setInt(1, IdKor);
            ps5.setInt(2, IdVoz);
            
            int affectedRows = ps5.executeUpdate();
            
            return affectedRows>0;
            
            
        } catch (SQLException ex) {
            return false;
        }
        

    }

    @Override
    public boolean deleteCourier(String courierUserName) {
        Connection conn = kn210113_DB.getInstance().getConnection();
        String sql1 = "select IdKor from Korisnik where KorisnickoIme = ?";
        String sql2 = "select IdKor from Korisnik where IdKor = ?";
        String sql3 = "delete from Ponuda where IdKor = ?";
        String sql4 = "delete from Kurir where IdKor = ?";
        try(PreparedStatement ps1 = conn.prepareStatement(sql1);
            PreparedStatement ps2 = conn.prepareStatement(sql2);
            PreparedStatement ps3 = conn.prepareStatement(sql3);
            PreparedStatement ps4 = conn.prepareStatement(sql4);){
            
            ps1.setString(1, courierUserName);
            int IdKor;
            try(ResultSet rs1 = ps1.executeQuery();){
                if(!rs1.next()){
                    return false;
                }
                IdKor = rs1.getInt(1);
            }
            
            ps2.setInt(1, IdKor);
            try(ResultSet rs2 = ps2.executeQuery();){
                if(!rs2.next()){
                    return false;
                }
            }
            conn.setAutoCommit(false);
            ps3.setInt(1, IdKor);
            ps3.executeUpdate();
            
            ps4.setInt(1, IdKor);
            int affectedRows = ps4.executeUpdate();
            conn.commit();
            return affectedRows > 0;
        } catch (SQLException ex) {
            Logger.getLogger(kn210113_Courier.class.getName()).log(Level.SEVERE, null, ex);
            try {
                conn.rollback();
            } catch (SQLException ex1) {
                return false;
            }
            return false;
        }finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException ex) {
                Logger.getLogger(kn210113_User.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @Override
    public List<String> getCouriersWithStatus(int statusOfCourier) {
        List<String> kuriri = new ArrayList<>();
        Connection conn = kn210113_DB.getInstance().getConnection();
        String sql = "select Kor.KorisnickoIme \n" +
            "from Kurir Kur join Korisnik Kor on Kur.IdKor=Kor.IdKor \n" +
            "where Kur.Status=?";
        
        try(PreparedStatement ps = conn.prepareStatement(sql);){
            ps.setInt(0, statusOfCourier);
            try(ResultSet rs = ps.executeQuery();){
                while(rs.next()){
                    kuriri.add(rs.getString("KorisnickoIme"));
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(kn210113_Courier.class.getName()).log(Level.SEVERE, null, ex);
        }
        return kuriri;
    }

    @Override
    public List<String> getAllCouriers() {
        List<String> kuriri = new ArrayList<>();
        Connection conn = kn210113_DB.getInstance().getConnection();
        try(Statement st = conn.createStatement();
                ResultSet rs = st.executeQuery("select Kor.KorisnickoIme \n" +
                "from Kurir Kur join Korisnik Kor on Kur.IdKor=Kor.IdKor \n"+
                "order by Kur.Profit desc")){
            
            while(rs.next()){
                kuriri.add(rs.getString("KorisnickoIme"));
            }
            
        } catch (SQLException ex) {
            Logger.getLogger(kn210113_Courier.class.getName()).log(Level.SEVERE, null, ex);
        }
        return kuriri;
    }

    @Override
    public BigDecimal getAverageCourierProfit(int numberOfDeliveries) {
        Connection conn = kn210113_DB.getInstance().getConnection();
        String sql = "select avg(Profit) as prosecanProfit \n" +
                    "from Kurir \n" +
                    "where BrojIsporucenihPaketa>=?";
        
        try(PreparedStatement ps = conn.prepareStatement(sql);){
            
            ps.setInt(1, numberOfDeliveries);
            try(ResultSet rs=ps.executeQuery()){
                if(rs.next()){
                    BigDecimal avgProfit = rs.getBigDecimal("prosecanProfit");
                    System.out.println("Prosecan profit: " + avgProfit);
                    return avgProfit != null ? avgProfit : BigDecimal.ZERO;
                }
            }
            
            
        } catch (SQLException ex) {
            return BigDecimal.ZERO;
        }
        
        return BigDecimal.ZERO;
    }
    
}
