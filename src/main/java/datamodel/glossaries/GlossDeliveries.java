/*
 * 
 *  Somado (System Optymalizacji Małych Dostaw)
 *  Optymalizacja dostaw towarów, dane OSM, problem VRP
 * 
 *  Autor: Maciej Kawecki 2016 (praca inż. EE PW)
 * 
 */
package datamodel.glossaries;

import datamodel.Delivery;
import datamodel.OrderState;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import somado.Database;
import somado.User;

/**
 *
 * Szablon obiektu "słownika" dostaw - dodatkowe operacje BD na zapisanych dostawach
 * 
 * @author Maciej Kawecki
 * @version 1.0
 * 
 */
public class GlossDeliveries extends GlossaryAdapter<Delivery> {
     
  
  /**
   * Konstruktor
   * @param database Ref. do DB
   */
  public GlossDeliveries(Database database) {
      
    super(database);
           
  }
      
  
  /**
   * Metoda "usuwa" element ze słownika, czyli zamyka dostawę
   * @param delivery Obiekt dostawy
   * @param user Ref. do obiektu zalogowanego użytkownika
   * @return true jezeli OK
   */  
  @Override
  public boolean deleteItem(Delivery delivery, User user) {      
      
    if (delivery == null || !delivery.isActive()) return false;
    
    try {
           
      database.begin();
    
      PreparedStatement ps = database.prepareQuery("UPDATE dat_deliveries SET active = 0, date_end = DATETIME('now')"
              + " WHERE id = ?;");
      ps.setInt(1, delivery.getId());
      ps.executeUpdate();
    
      ps = database.prepareQuery("SELECT id FROM dat_orders WHERE delivery_id = ? AND state_id = ?");
      ps.setInt(1, delivery.getId());
      ps.setInt(2, OrderState.DELIVERY.getId());
      ResultSet rs = ps.executeQuery();
    
      while (rs.next()) {
        
        ps = database.prepareQuery("UPDATE dat_orders SET delivery_id=0, state_id = ? WHERE id = ?;");
        ps.setInt(1, OrderState.DONE.getId());
        ps.setInt(2, rs.getInt("id"));
        ps.executeUpdate();
        
      }
      
      rs.close();
      
      database.commit();       
      
        
    }
    
    catch (SQLException ex) {
        
      try {  
         database.rollback();
      }
      catch (SQLException ex2) {  System.err.println(ex2); }
      
      lastError = ex.getMessage();
      return false;
        
    }
      
     
    return true;
    
      
  }
  
  
    
}
