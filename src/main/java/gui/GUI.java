/*
 * 
 *  Somado (System Optymalizacji Małych Dostaw)
 *  Optymalizacja dostaw towarów, dane OSM, problem VRP
 * 
 *  Autor: Maciej Kawecki 2016 (praca inż. EE PW)
 * 
 */
package gui;
 
import addon.carbon.VerticalLabelUI;
import gui.menus.*;
import gui.tablepanels.TableDeliveriesPanel;
import gui.tablepanels.TableDriversPanel;
import gui.tablepanels.TableOrdersPanel;
import gui.tablepanels.TableVehiclesPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.sql.SQLException;
import java.util.Observable;
import java.util.Observer;
import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.plaf.metal.OceanTheme;
import somado.AppObserver;
import somado.Database;
import somado.IConf;
import somado.Lang;
import somado.Somado;
import somado.User;


/**
 *
 * Szablon obiektu budującego graficzny interfejs użytkownika
 * 
 * @author Maciej Kawecki
 * @version 1.0
 * 
 */
@SuppressWarnings("serial")
public class GUI extends JFrame implements Observer {
  
  
  /** Szerokość okna aplikacji w pikselach */
  public static final int FRAME_WIDTH = 1000;
  /** Wysokość okna aplikacji w pikselach */
  protected static final int FRAME_HEIGHT = 700;  
  /** Czcionka do uzytku w formularzach, oknach */  
  public static final Font BASE_FONT = new Font(Font.SANS_SERIF, Font.PLAIN, 12);
  
  /** Zakładka "Pojazdy" */
  public static final int TAB_VEHICLES = 0;
  /** Zakładka "Kierowcy" */
  public static final int TAB_DRIVERS = 1;
  /** Zakładka "Zamówienia" */
  public static final int TAB_ORDERS = 2;
  /** Zakładka "Dostawy:" */
  public static final int TAB_DELIVERIES = 3;
  
  /** Liczba bocznych zakładek */
  private static final int TABS_COUNT = 4;
  
  /** Boczne etykiety */
  private JLabel labTab0, labTab1, labTab2, labTab3;
  
  /** Górny pasek narzędziowy */
  private TopToolBar topToolBar;
  /** Referencja do obiektu użytkownika */
  private User user;
  /** Referencja do obserwatora z głównej klasy */
  private final AppObserver appObserver;
  /** Referencja do obiektu zdalnej, globalnej bazy danych */
  private Database database;
  /** Panel początkowy (pusty) */
  private JPanel startPanel;
  /** Panel z zakładkami (po autoryzacji) */
  private JTabbedPane tabPane;
  /** Belka glownego menu aplikacji */
  private JMenuBar menu;
  /** Czy interfejs juz zostal zbudowany */
  private boolean built = false;
  /** Watek do uruchomienia w momencie zakonczenia dzialania aplikacji */
  private Thread onQuitHook;

  
  /**
   * Konstruktor tworzący główne okno aplikacji
   * @param appObserver Referencja do obiektu obserwatora
   */  
  public GUI(AppObserver appObserver) {
      
    super(IConf.APP_NAME);  

    
    setIconImage(ImageRes.getImage("icon.png"));  
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    
    this.appObserver = appObserver;
   
    
    // umieszczenie okna programu na środku ekranu
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    setBounds((int)((screenSize.getWidth() - FRAME_WIDTH)/2), 
              (int)((screenSize.getHeight() - FRAME_HEIGHT)/2), FRAME_WIDTH, FRAME_HEIGHT);


  } 
  
  
  /**
   * Metoda buduje podstawowe elementy glownego okna
   */
  public void build() {
       
    getContentPane().setBackground(new Color(0xed, 0xf4, 0xff));
    getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));      
      
    // stworzenie glownego menu aplikacji
    menu = new JMenuBar();
    menu.add(new MenuApplication(this));
    menu.add(new MenuDeliveries(this));
    menu.add(new MenuHelp(this));
  
    // inicjalizacja i dodanie górnego paska narzędziowego
    topToolBar = new TopToolBar(this);
    topToolBar.setVisible(false);
    add(topToolBar);       
    
    startPanel = new EmptyPanel();
    add(startPanel);

    
    pack();
    setMinimumSize(new Dimension(FRAME_WIDTH, FRAME_HEIGHT));
    setSize(FRAME_WIDTH, FRAME_HEIGHT);
    
    // blokowanie minimalnego rozmiaru okna
    final GUI frame = this;
    addComponentListener(new ComponentAdapter() { 
        
       @Override
       public void componentResized(ComponentEvent e) { 
         Dimension d=frame.getSize(); 
         Dimension minD=frame.getMinimumSize(); 
         if(d.width<minD.width) d.width=minD.width; 
         if(d.height<minD.height) d.height=minD.height; 
         frame.setSize(d); 
       } 
    });

    setResizable(true);
    setVisible(true);
    setCursor(null);

  }


  
  /**
   * Metoda buduje menu zakładek bocznych z przypisanymi panelami
  */
  private void buildPanels() {
      
    // boczne zakładki
    tabPane = new JTabbedPane(JTabbedPane.LEFT);
    // maksymalny możliwy rozmiar w oknie programu
    tabPane.setPreferredSize(Toolkit.getDefaultToolkit().getScreenSize());
    
    for (int i=0;i<TABS_COUNT;i++)  tabPane.addTab(null, new EmptyPanel());

    // Boczne zakładki
    labTab0 = new JLabel(Lang.get("GUI.Cars"), JLabel.CENTER);
    labTab0.setUI(new VerticalLabelUI(false));
    tabPane.setTabComponentAt(TAB_VEHICLES, labTab0); 
    labTab0.setPreferredSize(new Dimension(11,105));    
    
    labTab1 = new JLabel(Lang.get("GUI.Drivers"),  JLabel.CENTER);
    labTab1.setUI(new VerticalLabelUI(false)); 
    tabPane.setTabComponentAt(TAB_DRIVERS, labTab1); 
    labTab1.setPreferredSize(new Dimension(11,105));

    labTab2 = new JLabel(Lang.get("GUI.Orders"), JLabel.CENTER);
    labTab2.setUI(new VerticalLabelUI(false));
    tabPane.setTabComponentAt(TAB_ORDERS, labTab2); 
    labTab2.setPreferredSize(new Dimension(11,105));

    labTab3 = new JLabel(Lang.get("GUI.Deliveries"), JLabel.CENTER);
    labTab3.setUI(new VerticalLabelUI(false));
    tabPane.setTabComponentAt(TAB_DELIVERIES, labTab3); 
    labTab3.setPreferredSize(new Dimension(11,105));    
    
    tabPane.setPreferredSize(new Dimension(FRAME_WIDTH, FRAME_HEIGHT));
    
    // dodanie paneli z bocznymi zakładkami
    add(tabPane);
    
    
    JScrollPane scrollPane = new JScrollPane(tabPane);
    add(scrollPane);    
    
    
    // panele do zakładek - wypelnianie danymi po przelaczeniu, odswiezanie
    tabPane.addChangeListener(new ChangeListener() {

        @Override
        public void stateChanged(ChangeEvent e) {
  
          if (!(tabPane.getSelectedComponent() instanceof TablePanel) 
                  || ((TablePanel)(tabPane.getSelectedComponent())).isChanged())
          switch (tabPane.getSelectedIndex()) {
              
              case TAB_VEHICLES:  
                  
                tabPane.setComponentAt(TAB_VEHICLES, new TableVehiclesPanel(Lang.get("GUI.CarsList"), GUI.this));  
                break;
                  
              case TAB_DRIVERS:  
                  
                tabPane.setComponentAt(TAB_DRIVERS, new TableDriversPanel(Lang.get("GUI.DriversList"), GUI.this));                
                break;
                  
              case TAB_ORDERS:  
                  
                tabPane.setComponentAt(TAB_ORDERS, new TableOrdersPanel(Lang.get("GUI.OrdersList"), GUI.this));
                break;  
                  
              case TAB_DELIVERIES: 
                  
                tabPane.setComponentAt(TAB_DELIVERIES, new TableDeliveriesPanel(Lang.get("GUI.DeliveriesList"), GUI.this));
                break;                                        
    
              
          }
            
            
        }
    
    
    
    });    
    
        
    tabPane.setSelectedIndex(TAB_ORDERS);
    
    pack();        
    
    setResizable(true);
    setVisible(true);      
      

  }
  
  
  /**
   * Odswiezenie zawartosci wszystkich zakladek bocznych
   */
  public void refreshTabPanels() {      	  
	    
    getActiveDataPanel().setChanged(true);
    int sel = getSelectedDataPanel();
    setSelectedDataPanel(sel > 0 ? 0 : 1);                 
    for (int i=0;i<TABS_COUNT;i++) {
    	
    	getDataPanel(i).setChanged(true);      
    }
    setSelectedDataPanel(sel);            
      
  }
  
  
  public void refreshAfterLangChange() {

	labTab0.setText(Lang.get("GUI.Cars"));
	labTab1.setText(Lang.get("GUI.Drivers"));
	labTab2.setText(Lang.get("GUI.Orders"));
	labTab3.setText(Lang.get("GUI.Deliveries"));
		
	menu.removeAll();
	menu.add(new MenuApplication(this));
	menu.add(new MenuDeliveries(this));
	menu.add(new MenuHelp(this));
	setJMenuBar(menu);   
	
	topToolBar.setBtnCaptions();
	
	refreshTabPanels();
	
  }
  
  
  /**
   * Metoda ustawia referencję do użytkownika i wlacza etykiety user/db
   * @param user Obiekt użytkownika
   */
  private void setUser(User user) {
      
    this.user = user;
    topToolBar.lockButtons(user);
    
  }  
  
  
  
  /**
   * Metoda zwraca referencję do obiektu użytkownika
   * @return Referencja do obiektu użytkownika
   */
  public User getUser() {
      
    return user;  
      
  }
  
  
  /**
   * Metoda zwraca referencję do wewnętrznego obiektu obserwatora
   * @return Referencja do obiektu obserwatora
   */
  public AppObserver getAppObserver() {
      
    return appObserver;  
      
  }
  
  

  public Database getDatabase() {
      
    return database;  
      
  }  
    
  
  
  
  /**
   * Metoda ustawia referencje przekazane przez obserwatora
   * @param o Obserwowany obiekt 
   * @param object Przekazany obiekt
   */
  @Override
  public void update(Observable o, Object object) {
       
     AppObserver obs = (AppObserver)object;
     
     switch (obs.getKey()) {
         
        case "db_shared":
            {

            	this.database = (Database)(obs.getObject());                
                setShutdownHook();
                
                break;
                
            }

        
        case "user":
            {
                setUser((User)(obs.getObject()));

                // po autoryzacji zamiana pustego startowego panelu na zakladki z danymi
                if (this.user.isAuthorized() && !built) {
                      
                    remove(startPanel);
                    buildPanels();
                    setJMenuBar(menu);
                    topToolBar.setVisible(true);
                    built = true;
                      
                }         
                break;
            }     

          
      }
           
  }
  
  
  /**
   * Zdjecie blokad w globalnej BD i zamknięcie połączenia z lokalną BD przy zamykaniu aplikacji
   */
  private void setShutdownHook() {
      
    if (database == null) return;  
    if (onQuitHook != null) Runtime.getRuntime().removeShutdownHook(onQuitHook);
    onQuitHook = new Thread() {
       @Override
       public void run() {
          
          try {
            database.close();
          }
          catch (SQLException e) {}
       }
    };
    Runtime.getRuntime().addShutdownHook(onQuitHook);            
      
  }
      
  
  /**
   * Wywolanie zakonczenia wykonywania programu
   */
  public void quitApp() {
      
     Somado.quitApp();
      
  }
 

  /**
   * Metoda zwraca ef. do panela danych aktywnej zakladki
   * @return Ref. panela danych z aktywnej zakladki
   */
  public EmptyPanel getActiveDataPanel() {  
  
   return (EmptyPanel) tabPane.getComponentAt(tabPane.getSelectedIndex());  
      
  }
  
  
 /** 
  * Metoda zwraca nr aktywnej bocznej zakladki
  * @return Nr aktywnej bocznej zakladki
  */  
  public int getSelectedDataPanel() { 
      
    return tabPane.getSelectedIndex();

  }  
  

  
  /** Metoda przelacza boczne zakladki
   * @param index Indeks bocznej zakladki
   */  
  public void setSelectedDataPanel(int index) {
      
    tabPane.setSelectedIndex(index);

  }
  
  
  /** Metoda zwraca panel bocznej zakladki
   * @param index Indeks bocznej zakladki
   * @return  Panel bocznej zakladki
   */  
  public EmptyPanel getDataPanel(int index) {
      
    return (EmptyPanel) tabPane.getComponentAt(index);  

  }  
  
  
  
  /**
   * Statyczna metoda ustawiająca temat(Ocean) LookAndFeel 
   */
  public static void setLookAndFeel() {
      
    MetalLookAndFeel.setCurrentTheme(new OceanTheme());
    try {
      UIManager.setLookAndFeel(new MetalLookAndFeel());
    }
    catch(Exception e) {
      System.err.println(e);
    }
    
    JFrame.setDefaultLookAndFeelDecorated(true);   
    JDialog.setDefaultLookAndFeelDecorated(true); 
      
    
  }  

    
}
  
