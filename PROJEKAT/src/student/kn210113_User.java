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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import rs.etf.sab.operations.UserOperations;

/**
 *
 * @author Nikola
 */
public class kn210113_User implements UserOperations{
    
    private boolean isValidPassword(String password) {
        if (password.length() < 8) {
            return false;
        }
        
        Pattern letter = Pattern.compile("[a-zA-Z]");
        Pattern digit = Pattern.compile("[0-9]");
        Matcher hasLetter = letter.matcher(password);
        Matcher hasDigit = digit.matcher(password);
        
        return hasLetter.find() && hasDigit.find();
    }

    @Override
    public boolean insertUser(String userName, String firstName, String lastName, String password) {
        if (!Character.isUpperCase(firstName.charAt(0)) || !Character.isUpperCase(lastName.charAt(0))) {
            return false;
        }
        if (!isValidPassword(password)) {
            return false;
        }
        Connection conn = kn210113_DB.getInstance().getConnection();
        String sql = "insert into Korisnik (KorisnickoIme, Ime, Prezime, Sifra , BrojPoslatihPaketa)"
                + " values (?, ?, ?, ? , ?)";
        try(PreparedStatement ps = conn.prepareStatement(sql);){
            ps.setString(1, userName);
            ps.setString(2, firstName);
            ps.setString(3, lastName);
            ps.setString(4, password);
            ps.setInt(5,0);
            
            int affectedRows = ps.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException ex) {
            return false;
        }
        
    }

    @Override
    public int declareAdmin(String userName) {
        Connection conn = kn210113_DB.getInstance().getConnection();
        String sql1 = "select IdKor from Korisnik where KorisnickoIme = ?";
        String sql2 = "select IdKor from Administrator where IdKor = ? ";
        String insert = "insert into Administrator values(?)";
        try(PreparedStatement ps1 = conn.prepareStatement(sql1);){
            
            ps1.setString(1, userName);
            try(ResultSet rs = ps1.executeQuery()){
                if(rs.next()){
                    int IdKor = rs.getInt(1);
                    try(PreparedStatement ps2 = conn.prepareStatement(sql2);){
                        ps2.setInt(1, IdKor);
                        
                        try(ResultSet rs2 = ps2.executeQuery();){
                            if(rs2.next()){
                                return 1;
                            }
                            else{
                                try(PreparedStatement ps3 = conn.prepareStatement(insert);){
                                    ps3.setInt(1, IdKor);
                                    
                                    int affectedRows = ps3.executeUpdate();
                                    if(affectedRows>0){
                                        return 0;
                                    }
                                }
                            }
                        }
                    }
                }
                else{
                    return 2;
                }
            }
            
        } catch (SQLException ex) {
            return 2;
        }
        return 2;
    }

    @Override
    public Integer getSentPackages(String... userNames) {
        if (userNames == null || userNames.length == 0) {
            return null;
        }
        Connection conn = kn210113_DB.getInstance().getConnection();
        StringBuilder sql = new StringBuilder("select sum(BrojPoslatihPaketa) as UkupnoPoslatihPaketa from Korisnik where KorisnickoIme in(");
        for(int i=0;i<userNames.length;i++){
            sql.append("?");
            if(i < userNames.length -1 ){
                sql.append(", ");
            }
        }
        sql.append(")");
        try(PreparedStatement ps = conn.prepareStatement(sql.toString());){
            for(int i=0;i<userNames.length;i++){
                ps.setString(i + 1, userNames[i]);
            }
            try(ResultSet rs = ps.executeQuery()){
                if(rs.next()){
                    int ukupnoPoslatihPaketa = rs.getInt("UkupnoPoslatihPaketa");
                    if (rs.wasNull()) {
                        return null;
                    }
                    return ukupnoPoslatihPaketa;
                }
                
            }
        } catch (SQLException ex) {
            return null;
        }
        return null;
    }

    @Override
    public int deleteUsers(String... userNames) {
        if (userNames == null || userNames.length == 0) {
            return 0; 
        }
        Connection conn = kn210113_DB.getInstance().getConnection();
        
        StringBuilder sql = new StringBuilder("delete from Korisnik where KorisnickoIme in(");
        for (int i = 0; i < userNames.length; i++) {
            sql.append("?");
            if (i < userNames.length - 1) {
                sql.append(", ");
            }
        }
        sql.append(")");
        
        try(PreparedStatement ps = conn.prepareStatement(sql.toString());){
            for (int i = 0; i < userNames.length; i++) {
                ps.setString(i + 1, userNames[i]);
            }
            int affectedRows = ps.executeUpdate();
            return affectedRows;
        } catch (SQLException ex) {
            return 0;
        }
        
    }

    @Override
    public List<String> getAllUsers() {
        List<String> korisnici = new ArrayList<>();
        Connection conn = kn210113_DB.getInstance().getConnection();
        try(Statement st = conn.createStatement();
                ResultSet rs = st.executeQuery("select KorisnickoIme from Korisnik");){
            while(rs.next()){
                korisnici.add(rs.getString(1));
            }
        } catch (SQLException ex) {
            Logger.getLogger(kn210113_User.class.getName()).log(Level.SEVERE, null, ex);
        }
        return korisnici;
    }
    
    public static void main(String[] args) {
//        kn210113_User user = new kn210113_User();
//        
//        // Test insertUser
//        boolean successInsert = user.insertUser("kova", "Nikola", "Kovacevic", "password123");
//        if (successInsert) {
//            System.out.println("kn210113_User inserted successfully.");
//        } else {
//            System.out.println("Failed to insert user.");
//        }


        kn210113_User user = new kn210113_User();
        
        // Test declareAdmin
        int status = user.declareAdmin("kova");
        switch (status) {
            case 0:
                System.out.println("User declared as admin successfully.");
                break;
            case 1:
                System.out.println("User is already an admin.");
                break;
            case 2:
                System.out.println("Failed to declare user as admin (user not found or error).");
                break;
        }
    }
    
    
    
}
