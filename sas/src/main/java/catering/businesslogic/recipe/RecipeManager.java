package catering.businesslogic.recipe;

import java.util.ArrayList;

/**
 * Manages the recipes, including loading and retrieving them.
 */
public class RecipeManager {

    /**
     * Constructs a new {@code RecipeManager} and loads all recipes.
     */
    public RecipeManager() {
        Recipe.loadAllRecipes();
    }

    /**
     * Retrieves a list of all recipes.
     *
     * @return An {@code ArrayList} containing all recipes.
     */
    public ArrayList<Recipe> getRecipes() {
        return Recipe.getAllRecipes();
    }
}
