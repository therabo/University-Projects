package catering.businesslogic.menu;

import catering.persistence.BatchUpdateHandler;
import catering.persistence.PersistenceManager;
import catering.persistence.ResultHandler;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Section {
    private int id;
    private String name;
    private ArrayList<MenuItem> sectionItems;

    public Section(String name) {
        id = 0;
        this.name = name;
        sectionItems = new ArrayList<>();
    }

    public Section(Section s) {
        this.id = 0;
        this.name = s.name;
        this.sectionItems = new ArrayList<>();
        for (MenuItem original : s.sectionItems) {
            this.sectionItems.add(new MenuItem(original));
        }
    }

    /**
     * <h2>METHODS FOR MAIN OPERATIONS</h2>
     */

    public void updateItems(ArrayList<MenuItem> newItems) {
        ArrayList<MenuItem> updatedList = new ArrayList<>();
        for (int i = 0; i < newItems.size(); i++) {
            MenuItem mi = newItems.get(i);
            MenuItem prev = this.findItemById(mi.getId());
            if (prev == null) {
                updatedList.add(mi);
            } else {
                prev.setDescription(mi.getDescription());
                prev.setItemRecipe(mi.getItemRecipe());
                updatedList.add(prev);
            }
        }
        this.sectionItems.clear();
        this.sectionItems.addAll(updatedList);
    }

    private MenuItem findItemById(int id) {
        for (MenuItem mi : sectionItems) {
            if (mi.getId() == id) return mi;
        }
        return null;
    }

    public void moveItem(MenuItem mi, int position) {
        sectionItems.remove(mi);
        sectionItems.add(position, mi);
    }


    public String testString() {
        String result = name + "\n";
        for (MenuItem mi : sectionItems) {
            result += "\t" + mi.toString() + "\n";
        }
        return result;
    }


    public void addItem(MenuItem mi) {
        this.sectionItems.add(mi);
    }

    public void removeItem(MenuItem mi) {
        sectionItems.remove(mi);
    }


    /**
     * <h2>STATIC METHODS FOR PERSISTENCE</h2>
     */

    public static void saveNewSection(int menuid, Section sec, int posInMenu) {
        String secInsert = "INSERT INTO menusections (menu_id, name, position) VALUES (?, ?, ?)";
        PersistenceManager.executeBatchUpdate(secInsert, 1, new BatchUpdateHandler() {
            @Override public void handleBatchItem(PreparedStatement ps, int count) throws SQLException {
                ps.setInt(1, menuid);
                ps.setString(2, sec.name);
                ps.setInt(3, posInMenu);
            }
            @Override public void handleGeneratedIds(ResultSet rs, int count) throws SQLException {
                sec.id = rs.getInt(1);
            }
        });

        if (sec.sectionItems.size() > 0) {
            MenuItem.saveAllNewItems(menuid, sec.id, sec.sectionItems);
        }
    }

    public static void saveAllNewSections(int menuid, List<Section> sections) {
        String secInsert = "INSERT INTO catering.menusections (menu_id, name, position) VALUES (?, ?, ?);";
        PersistenceManager.executeBatchUpdate(secInsert, sections.size(), new BatchUpdateHandler() {
            @Override
            public void handleBatchItem(PreparedStatement ps, int batchCount) throws SQLException {
                ps.setInt(1, menuid);
                ps.setString(2, sections.get(batchCount).name);
                ps.setInt(3, batchCount);
            }

            @Override
            public void handleGeneratedIds(ResultSet rs, int count) throws SQLException {
                sections.get(count).id = rs.getInt(1);
            }
        });

        // salva le voci delle sezioni
        for (Section s : sections) {
            if (s.sectionItems.size() > 0) {
                MenuItem.saveAllNewItems(menuid, s.id, s.sectionItems);
            }
        }
    }


    public static ArrayList<Section> loadSectionsFor(int menu_id) {
        ArrayList<Section> result = new ArrayList<>();
        String query = "SELECT * FROM menusections WHERE menu_id = " + menu_id +
                " ORDER BY position";
        PersistenceManager.executeQuery(query, new ResultHandler() {
            @Override
            public void handle(ResultSet rs) throws SQLException {
                Section s = new Section(rs.getString("name"));
                s.id = rs.getInt("id");
                result.add(s);
            }
        });

        for (Section s : result) {
            // load items
            s.sectionItems = MenuItem.loadItemsFor(menu_id, s.id);
        }

        return result;
    }


    public static void deleteSection(int menu_id, Section s) {
        // delete items
        String itemdel = "DELETE FROM menuitems WHERE section_id = " + s.id +
                " AND menu_id = " + menu_id;
        PersistenceManager.executeUpdate(itemdel);

        String secdel = "DELETE FROM menusections WHERE id = " + s.id;
        PersistenceManager.executeUpdate(secdel);
    }


    public static void saveSectionName(Section s) {
        PersistenceManager.executeUpdate("UPDATE menusections SET name = ? WHERE id = ?", ps -> {
            ps.setString(1, s.name);
            ps.setInt(2, s.id);
        });
    }


    public static void saveItemOrder(Section s) {
        String upd = "UPDATE menuitems SET position = ? WHERE id = ?";
        PersistenceManager.executeBatchUpdate(upd, s.sectionItems.size(), new BatchUpdateHandler() {
            @Override
            public void handleBatchItem(PreparedStatement ps, int batchCount) throws SQLException {
                ps.setInt(1, batchCount);
                ps.setInt(2, s.sectionItems.get(batchCount).getId());
            }

            @Override
            public void handleGeneratedIds(ResultSet rs, int count) throws SQLException {
                // no generated ids to handle
            }
        });
    }

    /**
     * <h2>GETTER AND SETTER</h2>
     */

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<MenuItem> getItems() {
        return this.sectionItems;
    }

    public int getItemsCount() {
        return sectionItems.size();
    }

    public int getId() {
        return id;
    }

    public int getItemPosition(MenuItem mi) {
        return this.sectionItems.indexOf(mi);
    }

    public String toString() {
        return name;
    }
}
