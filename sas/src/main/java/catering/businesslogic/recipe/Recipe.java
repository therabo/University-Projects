package catering.businesslogic.recipe;

import catering.persistence.PersistenceManager;
import catering.persistence.ResultHandler;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * Represents a recipe with an ID and name. Provides methods to load recipes from the database and manage them.
 */
public class Recipe {
    private static Map<Integer, Recipe> all = new HashMap<>();

    private int id;
    private String name;


    /**
     * Constructs a new {@code Recipe} with no initial values.
     */
    private Recipe() {
    }

    /**
     * Constructs a new {@code Recipe} with the specified name.
     *
     * @param name The name of the recipe.
     */
    public Recipe(String name) {
        id = 0;
        this.name = name;
    }


    /**
     * <h2>STATIC METHODS FOR PERSISTENCE</h2>
     */

    /**
     * Loads all recipes from the database.
     *
     * @return A list of all {@code Recipe} instances, sorted by name.
     */
    public static ArrayList<Recipe> loadAllRecipes() {
        String query = "SELECT * FROM recipes";
        PersistenceManager.executeQuery(query, new ResultHandler() {
            @Override
            public void handle(ResultSet rs) throws SQLException {
                int id = rs.getInt("id");
                if (all.containsKey(id)) {
                    Recipe rec = all.get(id);
                    rec.name = rs.getString("name");
                } else {
                    Recipe rec = new Recipe(rs.getString("name"));
                    rec.id = id;
                    all.put(rec.id, rec);
                }
            }
        });
        ArrayList<Recipe> ret = new ArrayList<Recipe>(all.values());
        Collections.sort(ret, new Comparator<Recipe>() {
            @Override
            public int compare(Recipe o1, Recipe o2) {
                return (o1.getName().compareTo(o2.getName()));
            }
        });
        return ret;
    }

    /**
     * Loads a specific recipe by its ID.
     *
     * @param id The ID of the recipe to load.
     * @return The {@code Recipe} with the specified ID, or {@code null} if not found.
     */
    public static Recipe loadRecipeById(int id) {
        Recipe recipe = new Recipe();
        String query = "SELECT * FROM recipes WHERE id = " + id;
        PersistenceManager.executeQuery(query, new ResultHandler() {
            @Override
            public void handle(ResultSet rs) throws SQLException {
                recipe.id = rs.getInt("id");
                recipe.name = rs.getString("name");
            }
        });
        return recipe.id != 0 ? recipe : null;
    }

    /**
     * <h2>GETTER AND SETTER</h2>
     */

    public void setId(int id) {
        this.id = id;
    }

    public static ArrayList<Recipe> getAllRecipes() {
        return new ArrayList<Recipe>(all.values());
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    public String toString() {
        return name;
    }
}
