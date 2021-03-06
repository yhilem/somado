/*
 * 
 *  Somado (System Optymalizacji Małych Dostaw)
 *  Optymalizacja dostaw towarów, dane OSM, problem VRP
 * 
 *  Autor: Maciej Kawecki 2016 (praca inż. EE PW)
 * 
 */
package gui.dialogs.gloss;

import datamodel.VehicleModel;
import gui.GUI;
import gui.TablePanel;
import gui.dialogs.GlossDialog;
import somado.Lang;


/**
 *
 * Szablon okienka do modyfikacji elementow slownika
 * 
 * @author Maciej Kawecki
 * @version 1.0
 * 
 */
@SuppressWarnings("serial")
public class GlossVehicleModelEditModDialog extends GlossVehicleModelEditDialog {
  
  /** Zmienione dane */
  private VehicleModel updatedItem;  
    
  
  /**
   * Konstruktor: istniejacy element slownika
   * @param frame Referencja do GUI
   * @param parentDialog Obiekt nadrzednego okienka
   * @param vmIndex Indeks slownikowy elementu
   */  
  public GlossVehicleModelEditModDialog(GUI frame, GlossDialog<VehicleModel> parentDialog, int vmIndex) {
        
    super(frame, parentDialog, Lang.get("Gloss.VehicleModelsGloss") + " - " +  Lang.get("Gloss.EditVehicleModel"), vmIndex);
         
  }
  
  
  /**
   * Metoda zapisujaca do BD
   * @param vehicleModel Dane do zapisania
   * @return true jezeli OK
   */  
  @Override
  protected boolean saveItem(VehicleModel vehicleModel) {
      
     updatedItem = vehicleModel;
     boolean saved = glossVehicleModels.updateItem(vehicleModel, frame.getUser());
     
     
     if (saved) 
       try {
        ((TablePanel)(frame.getDataPanel(GUI.TAB_VEHICLES))).refreshTable();
        ((TablePanel)(frame.getDataPanel(GUI.TAB_DRIVERS))).refreshTable();
       }
       catch (ClassCastException e) {}
     
     return saved;
     
  }
  
  
  /**
   * Metoda odswieza liste po zapisie do BD
   */  
  @Override
  protected void refreshItemsList() {
      
    getParentDialog().getFilters().doUpdate(updatedItem.getId());
      
  }
  
    
}
