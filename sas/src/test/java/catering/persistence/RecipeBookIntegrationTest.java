package catering.persistence;

import catering.businesslogic.recipe.RecipeBookEntry;
import catering.businesslogic.recipe.RecipeBookManager;
import catering.businesslogic.user.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@EnabledIfEnvironmentVariable(named = "CATERING_INTEGRATION", matches = "true")
class RecipeBookIntegrationTest {
    @Test
    void createsEditsAndPublishesAnOwnedRecipe() {
        RecipeBookManager manager = new RecipeBookManager();
        User chef = User.loadUser("Lidia");
        RecipeBookEntry draft = new RecipeBookEntry(0, 0, "Demo focaccia", RecipeBookEntry.Kind.RECIPE,
                "Lidia", "Test recipe", BigDecimal.valueOf(4), "portions", 60,
                RecipeBookEntry.Status.DRAFT, List.of(), List.of(), List.of(), List.of("bakery"));
        int id = manager.create(draft, chef);
        try {
            assertEquals(RecipeBookEntry.Status.DRAFT, manager.findById(id, chef).getStatus());
            assertEquals(null, manager.findById(id, User.loadUser("Guido")));
            assertThrows(IllegalArgumentException.class, () -> manager.publish(id, chef));
            RecipeBookEntry complete = new RecipeBookEntry(id, chef.getId(), "Demo focaccia",
                    RecipeBookEntry.Kind.RECIPE, "Lidia", "Test recipe", BigDecimal.valueOf(4),
                    "portions", 60, RecipeBookEntry.Status.DRAFT,
                    List.of(new RecipeBookEntry.Ingredient("Flour", null, BigDecimal.valueOf(500), "g")),
                    List.of("Knead the dough", "Bake"), List.of("Serve warm"), List.of("bakery"));
            assertThrows(IllegalArgumentException.class,
                    () -> manager.update(id, complete, User.loadUser("Tony")));
            manager.update(id, complete, chef);
            manager.publish(id, chef);
            assertEquals(RecipeBookEntry.Status.PUBLISHED, manager.findById(id, null).getStatus());
            assertEquals(2, manager.findById(id, null).getPreparationSteps().size());
            assertTrue(manager.published().stream().anyMatch(entry -> entry.getId() == id));
            assertThrows(IllegalArgumentException.class, () -> manager.update(id, complete, chef));
            int copyId = manager.copy(id, User.loadUser("Guido"));
            manager.deleteDraft(copyId, User.loadUser("Guido"));
            manager.withdraw(id, chef);
            assertEquals(RecipeBookEntry.Status.DRAFT, manager.findById(id, chef).getStatus());
        } finally {
            PersistenceManager.executeUpdate("DELETE FROM recipes WHERE id = ?", ps -> ps.setInt(1, id));
        }
    }
}
