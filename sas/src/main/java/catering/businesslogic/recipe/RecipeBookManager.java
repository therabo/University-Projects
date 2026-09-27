package catering.businesslogic.recipe;

import catering.businesslogic.user.User;
import catering.persistence.JdbcTransactions;
import catering.persistence.PersistenceException;
import catering.persistence.PersistenceManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public final class RecipeBookManager {
    public int create(RecipeBookEntry entry, User actor) {
        requireAuthor(actor);
        if (entry.getId() != 0 || entry.getStatus() != RecipeBookEntry.Status.DRAFT) {
            throw new IllegalArgumentException("New entries must be drafts");
        }
        return JdbcTransactions.execute("Cannot create recipe-book entry", connection -> {
            int id;
            try (PreparedStatement insert = connection.prepareStatement(
                    "INSERT INTO recipes (name, kind, owner_id, author_name, description, yield_amount, "
                            + "yield_unit, estimated_minutes, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, 'DRAFT')",
                    Statement.RETURN_GENERATED_KEYS)) {
                bindEntry(insert, entry, actor.getId());
                insert.executeUpdate();
                try (ResultSet keys = insert.getGeneratedKeys()) {
                    if (!keys.next()) throw new SQLException("Recipe ID was not generated");
                    id = keys.getInt(1);
                }
            }
            saveParts(connection, id, entry);
            return id;
        });
    }

    public void update(int id, RecipeBookEntry entry, User actor) {
        requireAuthor(actor);
        if (id <= 0 || entry.getStatus() != RecipeBookEntry.Status.DRAFT) {
            throw new IllegalArgumentException("Only draft content can be edited");
        }
        JdbcTransactions.execute("Cannot update recipe-book entry", connection -> {
            requireOwnedDraft(connection, id, actor.getId());
            try (PreparedStatement update = connection.prepareStatement(
                    "UPDATE recipes SET name = ?, kind = ?, owner_id = ?, author_name = ?, "
                            + "description = ?, yield_amount = ?, yield_unit = ?, estimated_minutes = ? WHERE id = ?")) {
                bindEntry(update, entry, actor.getId());
                update.setInt(9, id);
                update.executeUpdate();
            }
            clearParts(connection, id);
            saveParts(connection, id, entry);
            return null;
        });
    }

    public void publish(int id, User actor) {
        requireAuthor(actor);
        JdbcTransactions.execute("Cannot publish recipe-book entry", connection -> {
            requireOwnedDraft(connection, id, actor.getId());
            if (count(connection, "SELECT COUNT(*) FROM recipe_ingredients WHERE recipe_id = ?", id) == 0
                    || count(connection, "SELECT COUNT(*) FROM recipe_steps WHERE recipe_id = ?", id) == 0) {
                throw new IllegalArgumentException("Published entries need ingredients and instructions");
            }
            try (PreparedStatement update = connection.prepareStatement(
                    "UPDATE recipes SET status = 'PUBLISHED' WHERE id = ?")) {
                update.setInt(1, id);
                update.executeUpdate();
            }
            return null;
        });
    }

    public void deleteDraft(int id, User actor) {
        requireAuthor(actor);
        JdbcTransactions.execute("Cannot delete recipe-book entry", connection -> {
            requireOwnedDraft(connection, id, actor.getId());
            try (PreparedStatement delete = connection.prepareStatement("DELETE FROM recipes WHERE id = ?")) {
                delete.setInt(1, id);
                delete.executeUpdate();
            }
            return null;
        });
    }

    public void withdraw(int id, User actor) {
        requireAuthor(actor);
        JdbcTransactions.execute("Cannot withdraw recipe-book entry", connection -> {
            try (PreparedStatement select = connection.prepareStatement(
                    "SELECT owner_id, status FROM recipes WHERE id = ? FOR UPDATE")) {
                select.setInt(1, id);
                try (ResultSet row = select.executeQuery()) {
                    if (!row.next() || row.getInt(1) != actor.getId()
                            || !"PUBLISHED".equals(row.getString(2))) {
                        throw new IllegalArgumentException("Only the owner can withdraw a published entry");
                    }
                }
            }
            for (String query : List.of(
                    "SELECT COUNT(*) FROM menuitems WHERE recipe_id = ?",
                    "SELECT COUNT(*) FROM assignment WHERE recipe = ?",
                    "SELECT COUNT(*) FROM recipe_ingredients WHERE preparation_id = ?")) {
                if (count(connection, query, id) > 0) {
                    throw new IllegalArgumentException("Entry is in use and cannot be withdrawn");
                }
            }
            try (PreparedStatement update = connection.prepareStatement(
                    "UPDATE recipes SET status = 'DRAFT' WHERE id = ?")) {
                update.setInt(1, id);
                update.executeUpdate();
            }
            return null;
        });
    }

    public int copy(int id, User actor) {
        requireAuthor(actor);
        RecipeBookEntry source = findById(id, actor);
        if (source == null) throw new IllegalArgumentException("Entry is not visible to this user");
        RecipeBookEntry copy = new RecipeBookEntry(0, actor.getId(), source.getName() + " (copy)",
                source.getKind(), source.getAuthor(), source.getDescription(), source.getYieldAmount(),
                source.getYieldUnit(), source.getEstimatedMinutes(), RecipeBookEntry.Status.DRAFT,
                source.getIngredients(), source.getPreparationSteps(), source.getServiceSteps(), source.getTags());
        return create(copy, actor);
    }

    public RecipeBookEntry findById(int id, User actor) {
        try (Connection connection = PersistenceManager.getConnection()) {
            try (PreparedStatement select = connection.prepareStatement(
                    "SELECT id, owner_id, name, kind, author_name, description, yield_amount, "
                            + "yield_unit, estimated_minutes, status FROM recipes WHERE id = ?")) {
                select.setInt(1, id);
                try (ResultSet row = select.executeQuery()) {
                    if (!row.next()) return null;
                    int ownerId = row.getInt("owner_id");
                    String name = row.getString("name");
                    RecipeBookEntry.Kind kind = RecipeBookEntry.Kind.valueOf(row.getString("kind"));
                    String author = row.getString("author_name");
                    String description = row.getString("description");
                    java.math.BigDecimal yield = row.getBigDecimal("yield_amount");
                    String unit = row.getString("yield_unit");
                    int minutes = row.getInt("estimated_minutes");
                    Integer estimated = row.wasNull() ? null : minutes;
                    RecipeBookEntry.Status status = RecipeBookEntry.Status.valueOf(row.getString("status"));
                    if (status == RecipeBookEntry.Status.DRAFT
                            && (actor == null || actor.getId() != ownerId)) return null;
                    List<RecipeBookEntry.Ingredient> ingredients = loadIngredients(connection, id);
                    List<String> preparation = loadSteps(connection, id, "PREPARATION");
                    List<String> service = loadSteps(connection, id, "SERVICE");
                    List<String> tags = loadTags(connection, id);
                    return new RecipeBookEntry(id, ownerId, name, kind, author, description, yield, unit,
                            estimated, status, ingredients, preparation, service, tags);
                }
            }
        } catch (SQLException failure) {
            throw new PersistenceException("Cannot load recipe-book entry", failure);
        }
    }

    public List<RecipeBookEntry> published() {
        List<Integer> ids = new ArrayList<>();
        try (Connection connection = PersistenceManager.getConnection();
             PreparedStatement select = connection.prepareStatement(
                     "SELECT id FROM recipes WHERE status = 'PUBLISHED' ORDER BY name, id");
             ResultSet rows = select.executeQuery()) {
            while (rows.next()) ids.add(rows.getInt(1));
        } catch (SQLException failure) {
            throw new PersistenceException("Cannot list recipe-book entries", failure);
        }
        List<RecipeBookEntry> result = new ArrayList<>();
        for (int id : ids) result.add(findById(id, null));
        return result;
    }

    private static void requireAuthor(User actor) {
        if (actor == null || !actor.isChef() && !actor.isCook()) {
            throw new IllegalArgumentException("Chef or cook role is required");
        }
    }

    private static void requireOwnedDraft(Connection connection, int id, int actorId) throws SQLException {
        try (PreparedStatement select = connection.prepareStatement(
                "SELECT owner_id, status FROM recipes WHERE id = ? FOR UPDATE")) {
            select.setInt(1, id);
            try (ResultSet row = select.executeQuery()) {
                if (!row.next() || row.getInt(1) != actorId || !"DRAFT".equals(row.getString(2))) {
                    throw new IllegalArgumentException("Only the owner may change a draft");
                }
            }
        }
    }

    private static void bindEntry(PreparedStatement statement, RecipeBookEntry entry, int ownerId)
            throws SQLException {
        statement.setString(1, entry.getName());
        statement.setString(2, entry.getKind().name());
        statement.setInt(3, ownerId);
        statement.setString(4, entry.getAuthor());
        statement.setString(5, entry.getDescription());
        statement.setBigDecimal(6, entry.getYieldAmount());
        statement.setString(7, entry.getYieldUnit());
        if (entry.getEstimatedMinutes() == null) statement.setNull(8, java.sql.Types.INTEGER);
        else statement.setInt(8, entry.getEstimatedMinutes());
    }

    private static void saveParts(Connection connection, int id, RecipeBookEntry entry) throws SQLException {
        try (PreparedStatement insert = connection.prepareStatement(
                "INSERT INTO recipe_ingredients (recipe_id, ingredient_name, preparation_id, amount, unit) "
                        + "VALUES (?, ?, ?, ?, ?)")) {
            for (RecipeBookEntry.Ingredient ingredient : entry.getIngredients()) {
                if (ingredient.getPreparationId() != null) {
                    try (PreparedStatement check = connection.prepareStatement(
                            "SELECT kind, status FROM recipes WHERE id = ?")) {
                        check.setInt(1, ingredient.getPreparationId());
                        try (ResultSet row = check.executeQuery()) {
                            if (!row.next() || !"PREPARATION".equals(row.getString(1))
                                    || !"PUBLISHED".equals(row.getString(2))
                                    || ingredient.getPreparationId() == id) {
                                throw new IllegalArgumentException("Referenced preparation must be published");
                            }
                        }
                    }
                }
                insert.setInt(1, id);
                insert.setString(2, ingredient.getName());
                if (ingredient.getPreparationId() == null) insert.setNull(3, java.sql.Types.INTEGER);
                else insert.setInt(3, ingredient.getPreparationId());
                insert.setBigDecimal(4, ingredient.getAmount());
                insert.setString(5, ingredient.getUnit());
                insert.addBatch();
            }
            insert.executeBatch();
        }
        saveSteps(connection, id, "PREPARATION", entry.getPreparationSteps());
        saveSteps(connection, id, "SERVICE", entry.getServiceSteps());
        try (PreparedStatement insert = connection.prepareStatement(
                "INSERT INTO recipe_tags (recipe_id, tag) VALUES (?, ?)")) {
            for (String tag : entry.getTags()) {
                if (tag == null || tag.isBlank()) throw new IllegalArgumentException("Tags cannot be blank");
                insert.setInt(1, id);
                insert.setString(2, tag.trim());
                insert.addBatch();
            }
            insert.executeBatch();
        }
    }

    private static void saveSteps(Connection connection, int id, String phase, List<String> steps)
            throws SQLException {
        try (PreparedStatement insert = connection.prepareStatement(
                "INSERT INTO recipe_steps (recipe_id, phase, position, instruction) VALUES (?, ?, ?, ?)")) {
            for (int index = 0; index < steps.size(); index++) {
                String instruction = steps.get(index);
                if (instruction == null || instruction.isBlank()) {
                    throw new IllegalArgumentException("Instructions cannot be blank");
                }
                insert.setInt(1, id);
                insert.setString(2, phase);
                insert.setInt(3, index + 1);
                insert.setString(4, instruction.trim());
                insert.addBatch();
            }
            insert.executeBatch();
        }
    }

    private static void clearParts(Connection connection, int id) throws SQLException {
        for (String table : List.of("recipe_ingredients", "recipe_steps", "recipe_tags")) {
            try (PreparedStatement delete = connection.prepareStatement(
                    "DELETE FROM " + table + " WHERE recipe_id = ?")) {
                delete.setInt(1, id);
                delete.executeUpdate();
            }
        }
    }

    private static int count(Connection connection, String sql, int id) throws SQLException {
        try (PreparedStatement select = connection.prepareStatement(sql)) {
            select.setInt(1, id);
            try (ResultSet row = select.executeQuery()) {
                row.next();
                return row.getInt(1);
            }
        }
    }

    private static List<RecipeBookEntry.Ingredient> loadIngredients(Connection connection, int id)
            throws SQLException {
        List<RecipeBookEntry.Ingredient> result = new ArrayList<>();
        try (PreparedStatement select = connection.prepareStatement(
                "SELECT ingredient_name, preparation_id, amount, unit FROM recipe_ingredients "
                        + "WHERE recipe_id = ? ORDER BY id")) {
            select.setInt(1, id);
            try (ResultSet row = select.executeQuery()) {
                while (row.next()) {
                    int preparationId = row.getInt(2);
                    Integer preparation = row.wasNull() ? null : preparationId;
                    result.add(new RecipeBookEntry.Ingredient(row.getString(1),
                            preparation, row.getBigDecimal(3), row.getString(4)));
                }
            }
        }
        return result;
    }

    private static List<String> loadSteps(Connection connection, int id, String phase) throws SQLException {
        List<String> result = new ArrayList<>();
        try (PreparedStatement select = connection.prepareStatement(
                "SELECT instruction FROM recipe_steps WHERE recipe_id = ? AND phase = ? ORDER BY position")) {
            select.setInt(1, id);
            select.setString(2, phase);
            try (ResultSet row = select.executeQuery()) {
                while (row.next()) result.add(row.getString(1));
            }
        }
        return result;
    }

    private static List<String> loadTags(Connection connection, int id) throws SQLException {
        List<String> result = new ArrayList<>();
        try (PreparedStatement select = connection.prepareStatement(
                "SELECT tag FROM recipe_tags WHERE recipe_id = ? ORDER BY tag")) {
            select.setInt(1, id);
            try (ResultSet row = select.executeQuery()) {
                while (row.next()) result.add(row.getString(1));
            }
        }
        return result;
    }
}
