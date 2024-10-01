/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package student;

import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import rs.etf.sab.operations.PackageOperations;

/**
 *
 * @author Nikola
 */
public class kn210113_Package implements PackageOperations{

    @Override
    public int insertPackage(int districtFrom, int districtTo, String userName, int packageType, BigDecimal weight) {
         Connection conn = kn210113_DB.getInstance().getConnection();
         String insert = "insert into Paket(OpsPreuzimanja, OpsDostavljanja, IdKor, IdKur, TipPaketa, Tezina, StatusIsporuke, Cena, VremePrihvatanja) \n" +
            "values (?,?,?,Null,?,?,0,?,Null)";
         String sql = "select IdKor from Korisnik where KorisnickoIme = ?";
         String sql2 = "select Xkoordinata, Ykoordinata from Opstina where IdOps = ?";
         try(PreparedStatement ps1 = conn.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS);
                 PreparedStatement ps2 = conn.prepareStatement(sql);
                 PreparedStatement ps3 = conn.prepareStatement(sql2);){
             
             ps2.setString(1, userName);
             int IdKor;
             try(ResultSet rs = ps2.executeQuery();){
                 if(!rs.next()){
                     return -1;
                 }
                 IdKor = rs.getInt("IdKor");
             }
             
             ps3.setInt(1, districtFrom);
             int x1,y1;
             try(ResultSet rs2 = ps3.executeQuery();){
                 if(!rs2.next()){
                     return -1;
                 }
                 x1 = rs2.getInt("Xkoordinata");
                 y1 = rs2.getInt("Ykoordinata");
             }
             
             ps3.setInt(1, districtTo);
             int x2,y2;
             try(ResultSet rs3 = ps3.executeQuery();){
                 if(!rs3.next()){
                     return -1;
                 }
                 x2 = rs3.getInt("Xkoordinata");
                 y2 = rs3.getInt("Ykoordinata");
             }
             
             int osnocnaCena = 0,TezinskiFaktor = 0,cenaPoKg = 0;
             switch(packageType){
                case 0:
                     osnocnaCena=10;
                     TezinskiFaktor=0;
                     cenaPoKg=1;
                     break;
                case 1:
                     osnocnaCena=25;
                     TezinskiFaktor=1;
                     cenaPoKg=100;
                     break;
                case 2:
                     osnocnaCena=75;
                     TezinskiFaktor=2;
                     cenaPoKg=300;
                     break;
             }
            BigDecimal osnovnaCenaBD = new BigDecimal(osnocnaCena);
            BigDecimal tezinskiFaktorBD = new BigDecimal(TezinskiFaktor);
            BigDecimal cenaPoKgBD = new BigDecimal(cenaPoKg);

            BigDecimal distance = new BigDecimal(Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2)), MathContext.DECIMAL64);
            BigDecimal CenaJedneIsporuke = (osnovnaCenaBD.add(tezinskiFaktorBD.multiply(weight).multiply(cenaPoKgBD))).multiply(distance);

             ps1.setInt(1, districtFrom);
             ps1.setInt(2, districtTo);
             ps1.setInt(3, IdKor);
             ps1.setInt(4,packageType);
             ps1.setBigDecimal(5, weight);
             ps1.setBigDecimal(6, CenaJedneIsporuke);
             
             
             int affectedRows = ps1.executeUpdate();
             if (affectedRows > 0) {
                try(ResultSet generatedKeys = ps1.getGeneratedKeys();){
                    if (generatedKeys.next()) {
                        return generatedKeys.getInt(1);
                    }
                }
                
            }
             
         } catch (SQLException ex) {
            Logger.getLogger(kn210113_Package.class.getName()).log(Level.SEVERE, null, ex);
        }
         return -1;
    }
    
    private static final Random RANDOM = new Random();
    
    @Override
    public int insertTransportOffer(String couriersUserName, int packageId, BigDecimal pricePercentage) {
        Connection conn = kn210113_DB.getInstance().getConnection();
        String sql1 = "select IdKor from Korisnik where KorisnickoIme = ?";
        String sql2 = "select IdPak from Paket where IdPak = ?";
        String insert = "insert into Ponuda(IdKor, IdPak, ProcenatCeneIsporuke) values (?, ?, ?)";
        
        try(PreparedStatement ps1 = conn.prepareStatement(sql1);
            PreparedStatement ps2 = conn.prepareStatement(sql2);
            PreparedStatement ps3 = conn.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS);){
            
            ps1.setString(1, couriersUserName);
            int IdKor;
            try(ResultSet rs1 = ps1.executeQuery();){
                if(!rs1.next()){
                    return -1;
                }
                IdKor = rs1.getInt("IdKor");
            }
            
            ps2.setInt(1, packageId);
            try(ResultSet rs2 = ps2.executeQuery();){
                if(!rs2.next()){
                    return -1;
                }
            }
            
            if(pricePercentage == null){
                double procenat = (RANDOM.nextDouble()*4)+8;
                pricePercentage = BigDecimal.valueOf(procenat);
            }
            
            ps3.setInt(1, IdKor);
            ps3.setInt(2, packageId);
            ps3.setBigDecimal(3, pricePercentage);
            
            int affectedRows = ps3.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = ps3.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return generatedKeys.getInt(1);
                    }
                }
            }
            
            
        } catch (SQLException ex) {
            ex.printStackTrace();
            return -1;
        }
        
        return -1;
        
    }

    @Override
    public boolean acceptAnOffer(int offerId) {
        Connection conn = kn210113_DB.getInstance().getConnection();
        String sql1 = "select IdKor, IdPak from Ponuda where IdPon = ?";
        String update = "update Paket set StatusIsporuke = 1, IdKur = ?, VremePrihvatanja=GETDATE() where IdPak = ?";
        String sql2 = "SELECT  \n" +
                    "    COALESCE( \n" +
                    "        MAX(CASE WHEN StatusVoznje = 0 THEN IdVoznje END),  \n" +
                    "        MAX(CASE WHEN StatusVoznje <> 0 THEN IdVoznje + 1 END),  \n" +
                    "        1 \n" +
                    "    ) AS MaxIdVoznje \n" +
                    "FROM Voznja WHERE IdKor=?";
        
        String sql3 = "insert into Voznja(IdVoznje, IdKor, IdPak,StatusVoznje, Cena) \n" +
                "values (?,?,?,0,?)";
        
        String sql4 = "select Po.ProcenatCeneIsporuke, Pa.Cena from Ponuda Po join Paket Pa on Po.IdPak=Pa.IdPak where IdPon=?";
        
        String sql5 = "update Korisnik set BrojPoslatihPaketa = (select K.BrojPoslatihPaketa from Korisnik K join Paket P on P.IdKor=K.IdKor where P.IdPak = ?)+1  "
                + "where IdKor=(select K.IdKor from Korisnik K join Paket P on P.IdKor=K.IdKor where P.IdPak = ?)";
        
        try(PreparedStatement ps1 = conn.prepareStatement(sql1);
                PreparedStatement ps2 = conn.prepareStatement(update);
                PreparedStatement ps3 = conn.prepareStatement(sql2);
                PreparedStatement ps4 = conn.prepareStatement(sql3);
                PreparedStatement ps5 = conn.prepareStatement(sql4);
                PreparedStatement ps6 = conn.prepareStatement(sql5);){
            
            ps1.setInt(1,offerId);
            int IdKor,IdPak;
            try(ResultSet rs1 = ps1.executeQuery();){
                if(!rs1.next()){
                    return false;
                }
                IdKor = rs1.getInt("IdKor");
                IdPak = rs1.getInt("IdPak");
            }
            
            ps5.setInt(1, offerId);
            BigDecimal cena, procenat;
            try(ResultSet rs3 = ps5.executeQuery()){
                if(!rs3.next()){
                    return false;
                }
                cena=rs3.getBigDecimal("Cena");
                procenat = rs3.getBigDecimal("ProcenatCeneIsporuke");
            }
            
            ps2.setInt(1, IdKor);
            ps2.setInt(2, IdPak);
            int affectedRows = ps2.executeUpdate();
            if(affectedRows == 0){
                return false;
            }
            
            int IdVoznje;
            ps3.setInt(1, IdKor);
            try(ResultSet rs2 = ps3.executeQuery()){
                rs2.next();
                IdVoznje=rs2.getInt("MaxIdVoznje");
            }
            
            
            
            ps4.setInt(1, IdVoznje);
            ps4.setInt(2, IdKor);
            ps4.setInt(3, IdPak);
            ps4.setBigDecimal(4, cena.multiply(procenat).divide(new BigDecimal(100)));
            ps4.executeUpdate();
            
            ps6.setInt(1, IdPak);
            ps6.setInt(2, IdPak);
            ps6.executeUpdate();
            return true;
            
        } catch (SQLException ex) {
            Logger.getLogger(Package.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;

    }

    @Override
    public List<Integer> getAllOffers() {
        Connection conn = kn210113_DB.getInstance().getConnection();
        List<Integer> ponude = new ArrayList<>();
        
        try(Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery("select IdPon from Ponuda");){
            while(rs.next()){
                ponude.add(rs.getInt("IdPon"));
            }
        } catch (SQLException ex) {
            Logger.getLogger(kn210113_Package.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return ponude;
    }

    @Override
    public List<Pair<Integer, BigDecimal>> getAllOffersForPackage(int packageId) {
        Connection conn = kn210113_DB.getInstance().getConnection();
        List<Pair<Integer, BigDecimal>> ponude = new ArrayList<>();
        String sql = "select IdPon, ProcenatCeneIsporuke from Ponuda where IdPak = ?";
        try(PreparedStatement ps = conn.prepareStatement(sql);){
            
            ps.setInt(1, packageId);
            try(ResultSet rs = ps.executeQuery();){
                while(rs.next()){
                    ponude.add(new kn210113_OfferPair<>(rs.getInt("IdPon"), rs.getBigDecimal("ProcenatCeneIsporuke")));
                }
            }
            
        } catch (SQLException ex) {
            Logger.getLogger(kn210113_Package.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return ponude;
    }

    @Override
    public boolean deletePackage(int packageId) {
        Connection conn = kn210113_DB.getInstance().getConnection();
        String delete = "delete from Paket where IdPak = ?";
        try(PreparedStatement ps = conn.prepareStatement(delete);){
            ps.setInt(1, packageId);
            int affectedrows = ps.executeUpdate();
            return affectedrows>0;
        } catch (SQLException ex) {
            Logger.getLogger(kn210113_Package.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    @Override
    public boolean changeWeight(int packageId, BigDecimal newWeight) {
        Connection conn = kn210113_DB.getInstance().getConnection();
        String sql = "update Paket set Tezina = ?, set Cena = ? where IdPak = ?";
        String sql2 = "select Xkoordinata, Ykoordinata from Opstina where IdOps = ?";
        String sql3 = "select OpsPreuzimanja, OpsDostavljanja, TipPaketa from Paket where IdPak = ?";
        try(PreparedStatement ps = conn.prepareStatement(sql);
            PreparedStatement ps1 = conn.prepareStatement(sql2);
            PreparedStatement ps2 = conn.prepareStatement(sql3);){
            
            ps2.setInt(1, packageId);
            int opstinaOd,opstinaDo, tip;
            try(ResultSet rs1 = ps2.executeQuery()){
                if(!rs1.next()){
                    return false;
                }
                opstinaOd= rs1.getInt("OpsPreuzimanja");
                opstinaDo= rs1.getInt("OpsDostavljanja");
                tip= rs1.getInt("TipPaketa");
            }
            
            
            ps1.setInt(1, opstinaOd);
             int x1,y1;
             try(ResultSet rs2 = ps1.executeQuery();){
                 if(!rs2.next()){
                     return false;
                 }
                 x1 = rs2.getInt("Xkoordinata");
                 y1 = rs2.getInt("Ykoordinata");
             }
             
             ps1.setInt(1, opstinaDo);
             int x2,y2;
             try(ResultSet rs3 = ps1.executeQuery();){
                 if(!rs3.next()){
                     return false;
                 }
                 x2 = rs3.getInt("Xkoordinata");
                 y2 = rs3.getInt("Ykoordinata");
             }
             
             int osnocnaCena = 0,TezinskiFaktor = 0,cenaPoKg = 0;
             switch(tip){
                case 0:
                     osnocnaCena=10;
                     TezinskiFaktor=0;
                     cenaPoKg=1;
                     break;
                case 1:
                     osnocnaCena=25;
                     TezinskiFaktor=1;
                     cenaPoKg=100;
                     break;
                case 2:
                     osnocnaCena=75;
                     TezinskiFaktor=2;
                     cenaPoKg=300;
                     break;
             }
            BigDecimal osnovnaCenaBD = new BigDecimal(osnocnaCena);
            BigDecimal tezinskiFaktorBD = new BigDecimal(TezinskiFaktor);
            BigDecimal cenaPoKgBD = new BigDecimal(cenaPoKg);

            BigDecimal distance = new BigDecimal(Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2)), MathContext.DECIMAL64);
            BigDecimal CenaJedneIsporuke = (osnovnaCenaBD.add(tezinskiFaktorBD.multiply(newWeight).multiply(cenaPoKgBD))).multiply(distance);
            
            
            
            
            
            ps.setBigDecimal(1, newWeight);
            ps.setBigDecimal(2, CenaJedneIsporuke);
            ps.setInt(3, packageId);
            int affectedRows = ps.executeUpdate();
            return affectedRows>0;
        } catch (SQLException ex) {
            Logger.getLogger(Package.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    @Override
    public boolean changeType(int packageId, int newType) {
        Connection conn = kn210113_DB.getInstance().getConnection();
        String sql = "update Paket set TipPaketa = ?, set Cena = ? where IdPak = ?";
        String sql2 = "select Xkoordinata, Ykoordinata from Opstina where IdOps = ?";
        String sql3 = "select OpsPreuzimanja, OpsDostavljanja, Tezina from Paket where IdPak = ?";
        try(PreparedStatement ps = conn.prepareStatement(sql);
            PreparedStatement ps1 = conn.prepareStatement(sql2);
            PreparedStatement ps2 = conn.prepareStatement(sql3);){
            
            
            ps2.setInt(1, packageId);
            int opstinaOd,opstinaDo, tezina;
            try(ResultSet rs1 = ps2.executeQuery()){
                if(!rs1.next()){
                    return false;
                }
                opstinaOd= rs1.getInt("OpsPreuzimanja");
                opstinaDo= rs1.getInt("OpsDostavljanja");
                tezina= rs1.getInt("Tezina");
            }
            
            
            ps1.setInt(1, opstinaOd);
             int x1,y1;
             try(ResultSet rs2 = ps1.executeQuery();){
                 if(!rs2.next()){
                     return false;
                 }
                 x1 = rs2.getInt("Xkoordinata");
                 y1 = rs2.getInt("Ykoordinata");
             }
             
             ps1.setInt(1, opstinaDo);
             int x2,y2;
             try(ResultSet rs3 = ps1.executeQuery();){
                 if(!rs3.next()){
                     return false;
                 }
                 x2 = rs3.getInt("Xkoordinata");
                 y2 = rs3.getInt("Ykoordinata");
             }
             
             int osnocnaCena = 0,TezinskiFaktor = 0,cenaPoKg = 0;
             switch(newType){
                case 0:
                     osnocnaCena=10;
                     TezinskiFaktor=0;
                     cenaPoKg=1;
                     break;
                case 1:
                     osnocnaCena=25;
                     TezinskiFaktor=1;
                     cenaPoKg=100;
                     break;
                case 2:
                     osnocnaCena=75;
                     TezinskiFaktor=2;
                     cenaPoKg=300;
                     break;
             }
            BigDecimal osnovnaCenaBD = new BigDecimal(osnocnaCena);
            BigDecimal tezinskiFaktorBD = new BigDecimal(TezinskiFaktor);
            BigDecimal cenaPoKgBD = new BigDecimal(cenaPoKg);

            BigDecimal distance = new BigDecimal(Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2)), MathContext.DECIMAL64);
            BigDecimal CenaJedneIsporuke = (osnovnaCenaBD.add(tezinskiFaktorBD.multiply(new BigDecimal(tezina)).multiply(cenaPoKgBD))).multiply(distance);
            
            
            ps.setInt(1, newType);
            ps.setBigDecimal(2, CenaJedneIsporuke);
            ps.setInt(3, packageId);
            int affectedRows = ps.executeUpdate();
            return affectedRows>0;
        } catch (SQLException ex) {
            Logger.getLogger(Package.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    @Override
    public Integer getDeliveryStatus(int packageId) {
       Connection conn = kn210113_DB.getInstance().getConnection();
       String sql = "select StatusIsporuke from Paket where IdPak=?";
       try(PreparedStatement ps=conn.prepareStatement(sql);){
           ps.setInt(1, packageId);
           try(ResultSet rs = ps.executeQuery();){
               if(!rs.next()){
                   return null;
               }
               return rs.getInt("StatusIsporuke");
           }
       } catch (SQLException ex) {
            Logger.getLogger(kn210113_Package.class.getName()).log(Level.SEVERE, null, ex);
        }
       return null;
    }

    @Override
    public BigDecimal getPriceOfDelivery(int packageId) {
        Connection conn = kn210113_DB.getInstance().getConnection();
        String sql = "select Cena from Paket where IdPak = ?";
        try(PreparedStatement ps = conn.prepareStatement(sql);){
            ps.setInt(1, packageId);
            try(ResultSet rs = ps.executeQuery();){
                if(!rs.next()){
                    return null;
                }
                BigDecimal price = rs.getBigDecimal("Cena");
                return price != null ? price : null;
            }
        } catch (SQLException ex) {
            Logger.getLogger(kn210113_Package.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    @Override
    public Date getAcceptanceTime(int packageId) {
        Connection conn = kn210113_DB.getInstance().getConnection();
        String sql = "select VremePrihvatanja from Paket where IdPak = ?";
        try(PreparedStatement ps = conn.prepareStatement(sql);){
            ps.setInt(1, packageId);
            try(ResultSet rs = ps.executeQuery();){
                if(!rs.next()){
                    return null;
                }
                Date date = rs.getDate("VremePrihvatanja");
                return date != null ? date : null;
            }
        } catch (SQLException ex) {
            Logger.getLogger(kn210113_Package.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    @Override
    public List<Integer> getAllPackagesWithSpecificType(int type) {
        Connection conn = kn210113_DB.getInstance().getConnection();
        String sql = "select IdPak from Paket where TipPaketa = ?";
        
        List<Integer> paketi = new ArrayList<>();
        try(PreparedStatement ps = conn.prepareStatement(sql);){
            
            ps.setInt(1, type);
            try(ResultSet rs = ps.executeQuery();){
                while(rs.next()){
                    paketi.add(rs.getInt("IdPak"));
                }
            }
            
            
        } catch (SQLException ex) {
            Logger.getLogger(kn210113_Package.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return paketi;
    }

    @Override
    public List<Integer> getAllPackages() {
        Connection conn = kn210113_DB.getInstance().getConnection();
        List<Integer> paketi = new ArrayList<>();
        try(Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery("select IdPak from Paket");){
            
            while(rs.next()){
                paketi.add(rs.getInt("IdPak"));
            }
            
        } catch (SQLException ex) {
            Logger.getLogger(kn210113_Package.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return paketi;
    }

    @Override
    public List<Integer> getDrive(String courierUsername) {
        Connection conn = kn210113_DB.getInstance().getConnection();
        String sql1 = "select IdKor from Korisnik where KorisnickoIme=?";
        String sql2 = "select V.IdPak \n" +
                    "from Voznja V join Paket P on V.IdPak=P.IdPak \n" +
                    "where V.IdKor = ? and V.StatusVoznje=0 and P.StatusIsporuke<>3";
        List<Integer> isporuke = new ArrayList<>();
        try(PreparedStatement ps1 = conn.prepareStatement(sql1);
                PreparedStatement ps2 = conn.prepareStatement(sql2);){
            
            ps1.setString(1, courierUsername);
            int IdKor;
            try(ResultSet rs1 = ps1.executeQuery();){
                if(!rs1.next()){
                    return null;
                }
                IdKor = rs1.getInt("IdKor");
            }
            
            ps2.setInt(1, IdKor);
            try(ResultSet rs2 =ps2.executeQuery();){
                while(rs2.next()){
                    isporuke.add(rs2.getInt("IdPak"));
                }
            }
            
        } catch (SQLException ex) {
            Logger.getLogger(kn210113_Package.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return isporuke.isEmpty() ? null : isporuke;
    }
    
    
    
    
    

    @Override
    public int driveNextPackage(String courierUserName) {
        Connection conn = kn210113_DB.getInstance().getConnection();
        String sql1 = "select IdKor from Korisnik where KorisnickoIme=?";
        String sql2 = "select IdVoznje \n" +
                    "from Voznja \n" +
                    "where StatusVoznje=1 and IdKor=?";
        
        String sql3 = "select IdVoznje \n" +
                    "from Voznja \n" +
                    "where StatusVoznje=0 and IdKor=?";
        
        String sql4 = "update Voznja set StatusVoznje=1 where IdVoznje=?";
        
        String sql5 = "select top 1 V.IdPak \n" +
                    "from Voznja V join Paket P on V.IdPak=P.IdPak \n" +
                    "where IdVoznje=? and V.IdKor = ?  and P.StatusIsporuke<>3 \n"+
                    "ORDER BY P.VremePrihvatanja ";
        
        String sql6 = "select top 1 V.IdPak \n" +
                    "from Voznja V join Paket P on V.IdPak=P.IdPak \n" +
                    "where V.IdVoznje=? and V.idKor = ? and P.StatusIsporuke=2 \n"+
                    "ORDER BY P.VremePrihvatanja ";
        
        String prihvati = "update Paket set Statusisporuke=2 where IdPak=?";
        String isporuci = "update Paket set Statusisporuke=3 where IdPak=?";
        String zavrsiVoznju = "update Voznja set StatusVoznje=2 where IdVoznje=? and IdKor=?";
        
        String zaradjeno = "select\n" +
                        "sum(V.Cena + P.Cena ) as zarada  \n" +
                        "from Voznja V join Paket P on V.IdPak=P.IdPak \n" +
                        "where IdVoznje=?";
        
        
        
        String potroseno = "WITH PaketDistance AS ( \n" +
                            "    SELECT \n" +
                            "        P.IdPak, \n" +
                            "        SQRT( \n" +
                            "            POWER(op1.Xkoordinata - op2.Xkoordinata, 2) +  \n" +
                            "            POWER(op1.Ykoordinata - op2.Ykoordinata, 2) \n" +
                            "        ) AS EuklidovaDistanca \n" +
                            "    FROM  \n" +
                            "        Paket P \n" +
                            "    JOIN  \n" +
                            "        Opstina op1 ON P.OpsPreuzimanja = op1.IdOps \n" +
                            "    JOIN  \n" +
                            "        Opstina op2 ON P.OpsDostavljanja = op2.IdOps \n" +
                            ") \n" +
                            "SELECT\n" +
                            "    SUM(Voz.Potrosnja * pd.EuklidovaDistanca * \n" +
                            "    (CASE Voz.TipGoriva  \n" +
                            "        WHEN 0 THEN 15 \n" +
                            "        WHEN 1 THEN 32 \n" +
                            "        WHEN 2 THEN 36 \n" +
                            "    END)) AS potroseno \n" +
                            "FROM  \n" +
                            "    Voznja V  \n" +
                            "JOIN  \n" +
                            "    Paket P ON V.IdPak = P.IdPak  \n" +
                            "JOIN  \n" +
                            "    Kurir K ON K.IdKor = V.IdKor  \n" +
                            "JOIN  \n" +
                            "    Vozilo Voz ON Voz.IdVoz = K.IdVoz \n" +
                            "JOIN  \n" +
                            "    PaketDistance pd ON P.IdPak = pd.IdPak \n" +
                            "WHERE  \n" +
                            "    V.IdVoznje = ?;";
        
        String potroseno2="WITH PaketPairs AS ( " +
                "    SELECT " +
                "        P1.IdPak AS IdPak1, " +
                "        P2.IdPak AS IdPak2, " +
                "        Voz.Potrosnja * pd.EuklidovaDistanca * " +
                "            (CASE Voz.TipGoriva " +
                "                WHEN 0 THEN 15 " +
                "                WHEN 1 THEN 32 " +
                "                WHEN 2 THEN 36 " +
                "            END) AS Potroseno " +
                "    FROM " +
                "        Paket P1 " +
                "    JOIN " +
                "        Paket P2 ON P1.IdPak < P2.IdPak " +
                "    JOIN " +
                "        Opstina op1 ON P1.OpsDostavljanja = op1.IdOps " +
                "    JOIN " +
                "        Opstina op2 ON P2.OpsPreuzimanja = op2.IdOps " +
                "    JOIN " +
                "        Voznja V ON P1.IdPak = V.IdPak " +
                "    JOIN " +
                "        Kurir K ON K.IdKor = V.IdKor " +
                "    JOIN " +
                "        Vozilo Voz ON Voz.IdVoz = K.IdVoz " +
                "    JOIN " +
                "        (SELECT " +
                "            P1.IdPak AS IdPak1, " +
                "            P2.IdPak AS IdPak2, " +
                "            SQRT( " +
                "                POWER(op1.Xkoordinata - op2.Xkoordinata, 2) + " +
                "                POWER(op1.Ykoordinata - op2.Ykoordinata, 2) " +
                "            ) AS EuklidovaDistanca " +
                "        FROM " +
                "            Paket P1 " +
                "        JOIN " +
                "            Paket P2 ON P1.IdPak < P2.IdPak " +
                "        JOIN " +
                "            Opstina op1 ON P1.OpsDostavljanja = op1.IdOps " +
                "        JOIN " +
                "            Opstina op2 ON P2.OpsPreuzimanja = op2.IdOps " +
                "        ) pd ON P1.IdPak = pd.IdPak1 AND P2.IdPak = pd.IdPak2 " +
                "    WHERE " +
                "        V.IdVoznje = ? " +
                ") " +
                "SELECT " +
                "    SUM(Potroseno) AS UkupnoPotroseno " +
                "FROM " +
                "    PaketPairs";
        
        String profitGet = "select Profit from Kurir where IdKor = ?";
        String profitSet = "update Kurir set Profit=? where IdKor = ?";
        
        String updateKurir = "update Kurir set BrojIsporucenihPaketa=(select BrojIsporucenihPaketa from Kurir where Idkor=?)+1 where IdKor=?";
        
        try(PreparedStatement ps1 = conn.prepareStatement(sql1);
                PreparedStatement ps2 = conn.prepareStatement(sql2);
                PreparedStatement ps3 = conn.prepareStatement(sql3);
                PreparedStatement ps4 = conn.prepareStatement(sql4);
                PreparedStatement ps5 = conn.prepareStatement(sql5);
                PreparedStatement ps6 = conn.prepareStatement(sql6);
                PreparedStatement prihvatanje = conn.prepareStatement(prihvati);
                PreparedStatement isporuka = conn.prepareStatement(isporuci);
                PreparedStatement gotovo = conn.prepareStatement(zavrsiVoznju);
                
                PreparedStatement zar = conn.prepareStatement(zaradjeno);
                PreparedStatement pot = conn.prepareStatement(potroseno);
                PreparedStatement pot2 = conn.prepareStatement(potroseno2);
                
                PreparedStatement profitG = conn.prepareStatement(profitGet);
                PreparedStatement progitS = conn.prepareStatement(profitSet);
                
                PreparedStatement ps7 = conn.prepareStatement(updateKurir);){
            
            ps1.setString(1, courierUserName);
            int IdKor;
            try(ResultSet rs1 = ps1.executeQuery();){
                if(!rs1.next()){
                    return -2;
                }
                IdKor = rs1.getInt("IdKor");
            }
            
            ps2.setInt(1,IdKor);
            int IdVoznje;
            try(ResultSet rs2 = ps2.executeQuery();){
                if(!rs2.next()){
                    ps3.setInt(1, IdKor);
                    try(ResultSet rs3 = ps3.executeQuery();){
                        if(!rs3.next()){
                            return -1;
                        }
                        IdVoznje= rs3.getInt("IdVoznje");
                        
                    }
                    ps4.setInt(1, IdVoznje);
                    ps4.executeUpdate();
                    
                    int IdPak;
                    ps5.setInt(1, IdVoznje);
                    ps5.setInt(2, IdKor);
                    try(ResultSet rs4 = ps5.executeQuery();){
                        if(!rs4.next()){
                            return -2;
                        }
                        IdPak=rs4.getInt("IdPak");
                    }
                    
                    prihvatanje.setInt(1, IdPak);
                    prihvatanje.executeUpdate();
                    
                    
                    
                }
                else{
                    IdVoznje= rs2.getInt("IdVoznje");
                }
            }
            
            int IdPak;
            ps6.setInt(1, IdVoznje);
            ps6.setInt(2, IdKor);
            try(ResultSet rs5 = ps6.executeQuery();){
                if(!rs5.next()){
                    return -2;
                }
                IdPak=rs5.getInt("IdPak");
            }
            
            isporuka.setInt(1, IdPak);
            isporuka.executeUpdate();
            
            ps7.setInt(1, IdKor);
            ps7.setInt(2, IdKor);
            ps7.executeUpdate();
            
            int rez = IdPak;
            
            ps5.setInt(1, IdVoznje);
            ps5.setInt(2, IdKor);
            try(ResultSet rs6 = ps5.executeQuery();){
                if(rs6.next()){
                    IdPak=rs6.getInt("IdPak");
                    prihvatanje.setInt(1, IdPak);
                    prihvatanje.executeUpdate();
                    
                }else{
                    //racunanjeProfita
                    gotovo.setInt(1, IdVoznje);
                    gotovo.setInt(2, IdKor);
                    gotovo.executeUpdate();
                    
                    zar.setInt(1, IdVoznje);
                    pot.setInt(1, IdVoznje);
                    pot2.setInt(1, IdVoznje);
                    profitG.setInt(1, IdKor);
                    try(ResultSet rsZ = zar.executeQuery();
                            ResultSet rsP = pot.executeQuery();
                            ResultSet rsP2 = pot2.executeQuery();
                            ResultSet rsProf = profitG.executeQuery();){
                        if(rsZ.next()){
                            if(rsP.next() && rsP2.next()){
                                rsProf.next();
                                BigDecimal profit = rsProf.getBigDecimal(1);
                                progitS.setBigDecimal(1, profit.add(rsZ.getBigDecimal(1).subtract(rsP.getBigDecimal(1)).subtract(rsP2.getBigDecimal(1))));
                                System.out.println("zarada: " + rsZ.getBigDecimal(1));
                                System.out.println("potrosnja: " + rsP.getBigDecimal(1));
                                progitS.setInt(2, IdKor);
                                progitS.executeUpdate();
                                
                            }
                        }
                        
                    }
                }
                
            }
            return rez;
            
            
            
            
        } catch (SQLException ex) {
            Logger.getLogger(kn210113_Package.class.getName()).log(Level.SEVERE, null, ex);
        }
        return -1;
    }
    
    
}
