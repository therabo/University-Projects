package catering.businesslogic.event;

import catering.businesslogic.summarysheet.SummarySheet;
import catering.businesslogic.menu.Menu;
import catering.businesslogic.user.User;
import catering.persistence.PersistenceManager;
import catering.persistence.ResultHandler;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.util.ArrayList;

public class ServiceInfo implements EventItemInfo {
    private int id;
    private String name;
    private Date date;
    private Time timeStart;
    private Time timeEnd;
    private int participants;
    private int chefId;

    private Menu menu;
    private SummarySheet sheet;

    public ServiceInfo() {
    }

    /**
     * <h2>METHODS FOR MAIN OPERATIONS</h2>
     */


    public String toString() {
        return name + ": " + date + " (" + timeStart + "-" + timeEnd + "), " + participants + " pp.";
    }

    public boolean isAssignedChef(User user) {
        return user != null && user.isChef() && chefId > 0 && user.getId() == chefId;
    }

    public boolean isAssignedMenu() {
        return getMenu(id) != null;
    }


    /**
     * <h2>STATIC METHODS FOR PERSISTENCE</h2>
     */
    public static ArrayList<ServiceInfo> loadServiceInfoForEvent(int event_id) {
        ArrayList<ServiceInfo> result = new ArrayList<>();
        String query = "SELECT id, name, service_date, time_start, time_end, expected_participants, proposed_menu_id, chef_id " +
                "FROM services WHERE event_id = " + event_id;
        PersistenceManager.executeQuery(query, new ResultHandler() {
            @Override
            public void handle(ResultSet rs) throws SQLException {

                ServiceInfo serv = new ServiceInfo();
                serv.id = rs.getInt("id");
                serv.name = rs.getString("name");
                serv.date = rs.getDate("service_date");
                serv.timeStart = rs.getTime("time_start");
                serv.timeEnd = rs.getTime("time_end");
                serv.participants = rs.getInt("expected_participants");
                serv.chefId = rs.getInt("chef_id");

                // Carica il menu associato se presente
                int menuId = rs.getInt("proposed_menu_id");
                if (menuId > 0) {
                    serv.menu = Menu.loadMenuById(menuId);
                }

                result.add(serv);
            }
        });

        return result;
    }

    public Menu getMenu(int serviceId) {
        if (this.menu == null) {
            // Carica il menu dal database se non è già caricato
            String query = "SELECT proposed_menu_id FROM services WHERE id = " + serviceId;
            PersistenceManager.executeQuery(query, rs -> {
                int menuId = rs.getInt("proposed_menu_id");
                if (menuId > 0) {
                    menu = Menu.loadMenuById(menuId);
                }
            });
        }
        return this.menu;
    }

    public static ServiceInfo loadServiceInfoById(int serviceId) {
        final ServiceInfo service = new ServiceInfo();
        String query = "SELECT * FROM services WHERE id = " + serviceId;
        PersistenceManager.executeQuery(query, new ResultHandler() {
            @Override
            public void handle(ResultSet rs) throws SQLException {
                service.id = rs.getInt("id");
                service.name = rs.getString("name");
                service.date = rs.getDate("service_date");
                service.timeStart = rs.getTime("time_start");
                service.timeEnd = rs.getTime("time_end");
                service.participants = rs.getInt("expected_participants");
                service.chefId = rs.getInt("chef_id");

                int menuId = rs.getInt("proposed_menu_id");
                if (menuId > 0) {
                    service.menu = Menu.loadMenuById(menuId);
                }
            }
        });
        return service.id != 0 ? service : null;
    }

    /**
     * <h2>GETTER AND SETTER</h2>
     **/
    public SummarySheet getSheet() {
        return sheet;
    }

    public void setSheet(SummarySheet sheet) {
        this.sheet = sheet;
    }

    public void setMenu(Menu menuDiEsempio) {
        this.menu = menuDiEsempio;
    }

    public int getId() {
        return id;
    }

    public Date getDate() {
        return date;
    }

    public Time getTimeStart() {
        return timeStart;
    }

    public Time getTimeEnd() {
        return timeEnd;
    }
}
