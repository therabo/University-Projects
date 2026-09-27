package catering.businesslogic.menu;

import catering.businesslogic.recipe.Recipe;
import catering.persistence.BatchUpdateHandler;
import catering.persistence.PersistenceManager;
import catering.persistence.ResultHandler;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MenuItem {
    private int id;
    private String description;
    private Recipe itemRecipe;

    private MenuItem() {

    }

    public MenuItem(Recipe rec) {
        this(rec, rec.getName());
    }

    public MenuItem(Recipe rec, String desc) {
        id = 0;
        itemRecipe = rec;
        description = desc;
    }

    public MenuItem(MenuItem mi) {
        this.id = 0;
        this.description = mi.description;
        this.itemRecipe = mi.itemRecipe;
    }


    /**
     * <h2>STATIC METHODS FOR PERSISTENCE</h2>
     */

    public static void saveAllNewItems(int menuid, int sectionid, List<MenuItem> items) {
        String itemInsert = "INSERT INTO catering.menuitems (menu_id, section_id, description, recipe_id, position) VALUES (?, ?, ?, ?, ?);";
        PersistenceManager.executeBatchUpdate(itemInsert, items.size(), new BatchUpdateHandler() {
            @Override
            public void handleBatchItem(PreparedStatement ps, int batchCount) throws SQLException {
                ps.setInt(1, menuid);
                ps.setInt(2, sectionid);
                ps.setString(3, items.get(batchCount).description);
                ps.setInt(4, items.get(batchCount).itemRecipe.getId());
                ps.setInt(5, batchCount);
            }

            @Override
            public void handleGeneratedIds(ResultSet rs, int count) throws SQLException {
                items.get(count).id = rs.getInt(1);
            }
        });
    }

    public static void saveNewItem(int menuid, int sectionid, MenuItem mi, int pos) {
        String itemInsert = "INSERT INTO menuitems (menu_id, section_id, description, recipe_id, position) VALUES (?, ?, ?, ?, ?)";
        PersistenceManager.executeBatchUpdate(itemInsert, 1, new BatchUpdateHandler() {
            @Override public void handleBatchItem(PreparedStatement ps, int count) throws SQLException {
                ps.setInt(1, menuid);
                ps.setInt(2, sectionid);
                ps.setString(3, mi.description);
                ps.setInt(4, mi.itemRecipe.getId());
                ps.setInt(5, pos);
            }
            @Override public void handleGeneratedIds(ResultSet rs, int count) throws SQLException {
                mi.id = rs.getInt(1);
            }
        });
    }

    public static ArrayList<MenuItem> loadItemsFor(int menu_id, int sec_id) {
        ArrayList<MenuItem> result = new ArrayList<>();
        ArrayList<Integer> recids = new ArrayList<>();
        String query = "SELECT * FROM menuitems WHERE menu_id = " + menu_id +
                " AND " +
                "section_id = " + sec_id +
                " ORDER BY position";
        PersistenceManager.executeQuery(query, new ResultHandler() {
            @Override
            public void handle(ResultSet rs) throws SQLException {
                MenuItem mi = new MenuItem();
                mi.description = rs.getString("description");
                result.add(mi);
                recids.add(rs.getInt("recipe_id"));
            }
        });

        // carico qui le ricette perché non posso innestare due connessioni al DB
        for (int i = 0; i < result.size(); i++) {
            result.get(i).itemRecipe = Recipe.loadRecipeById(recids.get(i));
        }

        return result;
    }

    public static void saveSection(int sec_id, MenuItem mi) {
        String upd = "UPDATE menuitems SET section_id = " + sec_id +
                " WHERE id = " + mi.id;
        PersistenceManager.executeUpdate(upd);
    }

    public static void saveDescription(MenuItem mi) {
        PersistenceManager.executeUpdate("UPDATE menuitems SET description = ? WHERE id = ?", ps -> {
            ps.setString(1, mi.getDescription());
            ps.setInt(2, mi.id);
        });
    }

    public static void removeItem(MenuItem mi) {
        String rem = "DELETE FROM menuitems WHERE id = " + mi.getId();
        PersistenceManager.executeUpdate(rem);
    }


    /**
     * GETTER AND SETTER
     */

    public int getId() {
        return id;
    }


    public String toString() {
        return description;
    }


    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Recipe getItemRecipe() {
        return itemRecipe;
    }

    public void setItemRecipe(Recipe itemRecipe) {
        this.itemRecipe = itemRecipe;
    }
}
