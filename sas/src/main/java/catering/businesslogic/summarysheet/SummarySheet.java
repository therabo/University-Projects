package catering.businesslogic.summarysheet;

import catering.businesslogic.turn.Turn;
import catering.businesslogic.event.ServiceInfo;
import catering.businesslogic.menu.Menu;
import catering.businesslogic.recipe.Recipe;
import catering.businesslogic.user.User;
import catering.persistence.BatchUpdateHandler;
import catering.persistence.PersistenceManager;
import catering.persistence.ResultHandler;
import catering.persistence.UpdateHandler;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SummarySheet {
    private int id;
    private String note;
    private User owner;
    private Menu menu;
    private ServiceInfo service;
    private List<Assignment> assignments;
    private List<Recipe> extraTask;

    /**
     * Constructs a new {@code SummarySheet} with default values.
     * Initializes {@code assignments} and {@code extraTask} as empty {@code ArrayList}.
     */
    public SummarySheet() {
        this.assignments = new ArrayList<>();
        this.extraTask = new ArrayList<>();
    }

    /**
     * Constructs a new {@code SummarySheet} with the specified owner and service.
     * Initializes the menu associated with the provided service.
     *
     * @param owner   The owner of the summary sheet.
     * @param service The service associated with the summary sheet.
     */
    public SummarySheet(User owner, ServiceInfo service) {
        this();
        this.owner = owner;
        this.service = service;
        this.menu = service.getMenu(service.getId()); // Carica il menu associato al servizio
    }

    /**
     * <h2>STATIC METHODS FOR PERSISTENCE</h2>
     */

    /**
     * Saves a new {@code SummarySheet} to the database.
     * Inserts the summary sheet's note, owner, service ID, and menu ID into the {@code summarysheet} table.
     *
     * @param sheet The {@code SummarySheet} to be saved.
     */
    public static void saveNewSummarySheet(SummarySheet sheet) {
        String query = "INSERT INTO summarysheet (note, owner, service_id, menu) VALUES (?, ?, ?, ?)";
        int[] result = PersistenceManager.executeBatchUpdate(query, 1, new BatchUpdateHandler() {
            @Override
            public void handleBatchItem(PreparedStatement ps, int batchCount) throws SQLException {
                ps.setString(1, sheet.note != null ? sheet.note : "");
                ps.setInt(2, sheet.owner.getId());
                ps.setInt(3, sheet.service.getId());
                ps.setInt(4, sheet.menu != null ? sheet.menu.getId() : 0);
            }

            @Override
            public void handleGeneratedIds(ResultSet rs, int count) throws SQLException {
                sheet.id = rs.getInt(1);
            }
        });

        if (result.length != 1 || result[0] <= 0 || sheet.id <= 0) {
            throw new IllegalStateException("Summary sheet was not persisted");
        }
    }

    /**
     * Deletes the specified {@code SummarySheet} from the database.
     * Also deletes related assignments and extra tasks.
     *
     * @param sheet The {@code SummarySheet} to be deleted.
     */
    public static void deleteSheet(SummarySheet sheet) {
        deleteAssignmentsBySheet(sheet);
        deleteExtraTasks(sheet);
        String query = "DELETE FROM summarysheet WHERE id = ?";
        PersistenceManager.executeUpdate(query, new UpdateHandler() {
            @Override
            public void handleUpdate(PreparedStatement ps) throws SQLException {
                ps.setInt(1, sheet.getId());
            }
        });
    }

    /**
     * Deletes all extra tasks associated with the specified {@code SummarySheet}.
     *
     * @param sheet The {@code SummarySheet} whose extra tasks are to be deleted.
     */
    private static void deleteExtraTasks(SummarySheet sheet) {
        String query = "DELETE FROM extratask WHERE sheet = ?";
        PersistenceManager.executeUpdate(query, new UpdateHandler() {
            @Override
            public void handleUpdate(PreparedStatement ps) throws SQLException {
                ps.setInt(1, sheet.getId());
            }
        });
    }

    /**
     * Deletes all assignments associated with the specified {@code SummarySheet}.
     *
     * @param sheet The {@code SummarySheet} whose assignments are to be deleted.
     */
    private static void deleteAssignmentsBySheet(SummarySheet sheet) {
        String query = "DELETE FROM assignment WHERE sheet = ?";
        PersistenceManager.executeUpdate(query, new UpdateHandler() {
            @Override
            public void handleUpdate(PreparedStatement ps) throws SQLException {
                ps.setInt(1, sheet.getId());
            }
        });
    }

    /**
     * Adds a list of {@code Recipe} items to the extra tasks of the specified {@code SummarySheet}.
     *
     * @param sheet The {@code SummarySheet} to which items are added.
     * @param items The list of {@code Recipe} items to be added.
     */
    public static void addItem(SummarySheet sheet, List<Recipe> items) {
        String query = "INSERT INTO extratask (task, sheet) VALUES (?, ?)";
        int[] result = PersistenceManager.executeBatchUpdate(query, items.size(), new BatchUpdateHandler() {
            @Override
            public void handleBatchItem(PreparedStatement ps, int batchCount) throws SQLException {
                Recipe item = items.get(batchCount);
                ps.setInt(1, item.getId());
                ps.setInt(2, sheet.getId());
            }

            @Override
            public void handleGeneratedIds(ResultSet rs, int count) throws SQLException {
            }
        });

        if (result.length == items.size()) {
            for (Recipe item : items) {
                sheet.addExtraTask(item); // Aggiunge l'elemento alla lista extraTask del foglio riepilogativo
            }
            System.out.println("Tutti gli extra task sono stati aggiunti al SummarySheet con ID: " + sheet.getId());
        } else {
            System.out.println("Errore durante l'aggiunta degli extra task al SummarySheet.");
        }
    }

    /**
     * Removes a specific {@code Recipe} item from the extra tasks of the specified {@code SummarySheet}.
     *
     * @param sheet The {@code SummarySheet} from which the item is removed.
     * @param item  The {@code Recipe} item to be removed.
     */
    public static void removeItem(SummarySheet sheet, Recipe item) {
        String query = "DELETE FROM extratask WHERE sheet = ? AND task = ?";
        PersistenceManager.executeUpdate(query, new UpdateHandler() {
            @Override
            public void handleUpdate(PreparedStatement ps) throws SQLException {
                ps.setInt(1, sheet.getId());
                ps.setInt(2, item.getId());
            }
        });
    }

    /**
     * Saves a new {@code Assignment} to the database.
     * Inserts the assignment details into the {@code assignment} table.
     *
     * @param sheet      The {@code SummarySheet} to which the assignment belongs.
     * @param assignment The {@code Assignment} to be saved.
     * @throws IllegalArgumentException if the {@code SummarySheet} is null or has an invalid ID.
     */
    public static void saveNewAssignment(SummarySheet sheet, Assignment assignment) {
        if (sheet == null || sheet.getId() == 0) {
            throw new IllegalArgumentException("SummarySheet cannot be null and must have a valid ID");
        }

        String query = "INSERT INTO assignment (turn, cook, recipe, sheet, portion, time) VALUES (?, ?, ?, ?, ?, ?)";
        int[] result = PersistenceManager.executeBatchUpdate(query, 1, new BatchUpdateHandler() {
            @Override
            public void handleBatchItem(PreparedStatement ps, int batchCount) throws SQLException {
                ps.setInt(1, assignment.getTurn().getId());
                ps.setInt(2, assignment.getCook().getId());
                ps.setInt(3, assignment.getRecipe().getId());
                ps.setInt(4, sheet.getId());
                ps.setInt(5, assignment.getPortion());
                ps.setInt(6, assignment.getTime());
            }

            @Override
            public void handleGeneratedIds(ResultSet rs, int count) throws SQLException {
                assignment.setId(rs.getInt(1));
            }
        });

        if (result.length != 1 || result[0] <= 0 || assignment.getId() <= 0) {
            throw new IllegalStateException("Assignment was not persisted");
        }
    }

    /**
     * Updates the portion size of a specific {@code Assignment} in the database.
     *
     * @param asg     The {@code Assignment} to be updated.
     * @param portion The new portion size.
     */
    public static void addPortion(Assignment asg, int portion) {
        String query = "UPDATE assignment SET portion = ? WHERE id = ?";
        PersistenceManager.executeUpdate(query, new UpdateHandler() {
            @Override
            public void handleUpdate(PreparedStatement ps) throws SQLException {
                ps.setInt(1, portion);
                ps.setInt(2, asg.getId());
            }
        });
        asg.setPortion(portion);
    }

    /**
     * Updates the time of a specific {@code Assignment} in the database.
     *
     * @param asg  The {@code Assignment} to be updated.
     * @param time The new time.
     */
    public static void addTime(Assignment asg, int time) {
        String query = "UPDATE assignment SET time = ? WHERE id = ?";
        PersistenceManager.executeUpdate(query, new UpdateHandler() {
            @Override
            public void handleUpdate(PreparedStatement ps) throws SQLException {
                ps.setInt(1, time);
                ps.setInt(2, asg.getId());
            }
        });
        asg.setTime(time);
    }

    /**
     * Updates the note of the specified {@code SummarySheet} in the database.
     *
     * @param sheet The {@code SummarySheet} to be updated.
     * @param note  The new note.
     */
    public static void addNote(SummarySheet sheet, String note) {
        String query = "UPDATE summarysheet SET note = ? WHERE id = ?";
        PersistenceManager.executeUpdate(query, new UpdateHandler() {
            @Override
            public void handleUpdate(PreparedStatement ps) throws SQLException {
                ps.setString(1, note);
                ps.setInt(2, sheet.getId());
            }
        });
        sheet.setNote(note);
    }

    /**
     * Updates the cook assigned to a specific {@code Assignment} in the database.
     *
     * @param asg  The {@code Assignment} to be updated.
     * @param cook The new cook.
     */
    public static void modifyCookInAssignment(Assignment asg, User cook) {
        String query = "UPDATE assignment SET cook = ? WHERE id = ?";
        PersistenceManager.executeUpdate(query, new UpdateHandler() {
            @Override
            public void handleUpdate(PreparedStatement ps) throws SQLException {
                ps.setInt(1, cook.getId());
                ps.setInt(2, asg.getId());
            }
        });
        asg.setCook(cook);
    }

    /**
     * Updates the turn assigned to a specific {@code Assignment} in the database.
     *
     * @param asg  The {@code Assignment} to be updated.
     * @param turn The new turn.
     */
    public static void modifyTurnInAssignment(Assignment asg, Turn turn) {
        String query = "UPDATE assignment SET turn = ? WHERE id = ?";
        PersistenceManager.executeUpdate(query, new UpdateHandler() {
            @Override
            public void handleUpdate(PreparedStatement ps) throws SQLException {
                ps.setInt(1, turn.getId());
                ps.setInt(2, asg.getId());
            }
        });
        asg.setTurn(turn);
    }

    /**
     * Updates the task assigned to a specific {@code Assignment} in the database.
     *
     * @param asg  The {@code Assignment} to be updated.
     * @param task The new task.
     */
    public static void modifyTaskInAssignment(Assignment asg, Recipe task) {
        String query = "UPDATE assignment SET recipe = ? WHERE id = ?";
        PersistenceManager.executeUpdate(query, new UpdateHandler() {
            @Override
            public void handleUpdate(PreparedStatement ps) throws SQLException {
                ps.setInt(1, task.getId());
                ps.setInt(2, asg.getId());
            }
        });
        asg.setTask(task);
    }

    /**
     * Deletes a specific {@code Assignment} from the database.
     *
     * @param asg The {@code Assignment} to be deleted.
     */
    public static void deleteAssignment(Assignment asg) {
        String query = "DELETE FROM assignment WHERE id = ?";
        PersistenceManager.executeUpdate(query, new UpdateHandler() {
            @Override
            public void handleUpdate(PreparedStatement ps) throws SQLException {
                ps.setInt(1, asg.getId());
            }
        });
    }

    /**
     * Loads all {@code SummarySheet} records from the database.
     *
     * @return A list of all {@code SummarySheet} objects.
     */
    public static List<SummarySheet> loadAllSummarySheets() {
        List<SummarySheet> sheets = new ArrayList<>();
        String query = "SELECT * FROM summarysheet";
        PersistenceManager.executeQuery(query, new ResultHandler() {
            @Override
            public void handle(ResultSet rs) throws SQLException {
                SummarySheet sheet = new SummarySheet();
                sheet.id = rs.getInt("id");
                sheet.note = rs.getString("note");
                sheet.owner = User.loadUserById(rs.getInt("owner"));
                sheet.service = ServiceInfo.loadServiceInfoById(rs.getInt("service_id"));
                sheet.menu = Menu.loadMenuById(rs.getInt("menu"));
                sheet.loadExtraTasks();
                sheet.loadAssignments();
                sheets.add(sheet);
            }
        });
        return sheets;
    }

    /**
     * Loads all {@code SummarySheet} records for a specific service from the database.
     *
     * @param serviceId The ID of the service for which summary sheets are to be loaded.
     * @return A list of {@code SummarySheet} objects associated with the specified service.
     */
    public static List<SummarySheet> loadAllSummarySheetsForService(int serviceId) {
        List<SummarySheet> sheets = new ArrayList<>();
        String query = "SELECT * FROM summarysheet WHERE service_id = " + serviceId;
        PersistenceManager.executeQuery(query, new ResultHandler() {
            @Override
            public void handle(ResultSet rs) throws SQLException {
                SummarySheet sheet = new SummarySheet();
                sheet.id = rs.getInt("id");
                sheet.note = rs.getString("note");
                sheet.owner = User.loadUserById(rs.getInt("owner"));
                sheet.service = ServiceInfo.loadServiceInfoById(rs.getInt("service_id"));
                sheet.menu = Menu.loadMenuById(rs.getInt("menu"));
                sheet.loadExtraTasks(); // Carica gli extra tasks
                sheet.loadAssignments();
                sheets.add(sheet);
            }
        });
        return sheets;
    }

    /**
     * <h2>METHODS FOR MAIN OPERATIONS</h2>
     */

    /**
     * Adds a {@code Recipe} item to the list of extra tasks for this {@code SummarySheet}.
     * If the item is already in the list, it will not be added again.
     *
     * @param item The {@code Recipe} item to be added to the extra tasks.
     */
    public void addExtraTask(Recipe item) {
        if (!this.extraTask.contains(item)) {
            this.extraTask.add(item);
        }
    }

    /**
     * Removes a {@code Recipe} item from the list of extra tasks for this {@code SummarySheet}.
     * If the item is not in the list, no action is taken.
     *
     * @param item The {@code Recipe} item to be removed from the extra tasks.
     */
    public void deleteExtraTask(Recipe item) {
        if (this.extraTask.contains(item)) {
            this.extraTask.remove(item);
        }
    }

    /**
     * Moves a {@code Recipe} item within the list of extra tasks to a new position.
     *
     * @param item The {@code Recipe} item to be moved.
     * @param pos  The new position of the item in the list.
     */
    public void moveRecipe(Recipe item, int pos) {
        extraTask.remove(item);
        extraTask.add(pos, item);
    }

    /**
     * Creates a new {@code Assignment} with the specified cook, turn, and recipe.
     * Adds the newly created assignment to the list of assignments for this {@code SummarySheet}.
     *
     * @param cook The {@code User} assigned to the task.
     * @param turn The {@code Turn} associated with the assignment.
     * @param item The {@code Recipe} assigned.
     * @return The newly created {@code Assignment}.
     */
    public Assignment createAssignment(User cook, Turn turn, Recipe item) {
        Assignment asg = new Assignment(cook, turn, item);
        assignments.add(asg);
        return asg;
    }

    /**
     * Assigns a portion size to the specified {@code Assignment}.
     *
     * @param portion The portion size to be assigned.
     * @param asg     The {@code Assignment} to be updated.
     */
    public void assignPortion(int portion, Assignment asg) {
        asg.setPortion(portion);
    }

    /**
     * Assigns a time duration to the specified {@code Assignment}.
     *
     * @param time The time duration to be assigned.
     * @param asg  The {@code Assignment} to be updated.
     */
    public void assignTime(int time, Assignment asg) {
        asg.setTime(time);
    }

    /**
     * Sets a new cook for the specified {@code Assignment}.
     *
     * @param cook The new {@code User} assigned to the task.
     * @param asg  The {@code Assignment} to be updated.
     */
    public void setNewCook(User cook, Assignment asg) {
        asg.setCook(cook);
    }

    /**
     * Sets a new turn for the specified {@code Assignment}.
     *
     * @param turn The new {@code Turn} for the assignment.
     * @param asg  The {@code Assignment} to be updated.
     */
    public void setNewTurn(Turn turn, Assignment asg) {
        asg.setTurn(turn);
    }

    /**
     * Sets a new recipe for the specified {@code Assignment}.
     *
     * @param task The new {@code Recipe} assigned to the task.
     * @param asg  The {@code Assignment} to be updated.
     */
    public void setNewItem(Recipe task, Assignment asg) {
        asg.setTask(task);
    }

    /**
     * Checks if the {@code SummarySheet} is in progress.
     *
     * @return {@code false} if the summary sheet is not in progress.
     */
    public boolean isInProgress() {
        if (service == null || service.getDate() == null || service.getTimeStart() == null || service.getTimeEnd() == null) {
            return false;
        }
        java.time.LocalDateTime start = java.time.LocalDateTime.of(service.getDate().toLocalDate(), service.getTimeStart().toLocalTime());
        java.time.LocalDateTime end = java.time.LocalDateTime.of(service.getDate().toLocalDate(), service.getTimeEnd().toLocalTime());
        if (!end.isAfter(start)) {
            end = end.plusDays(1);
        }
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        return !now.isBefore(start) && now.isBefore(end);
    }

    /**
     * Checks if the specified {@code User} is the owner of the {@code SummarySheet}.
     *
     * @param user The {@code User} to be checked.
     * @return {@code true} if the user is the owner, {@code false} otherwise.
     */
    public boolean isOwner(User user) {
        return owner != null && user != null && owner.getId() > 0 && owner.getId() == user.getId();
    }

    /**
     * Returns a string representation of the {@code SummarySheet}.
     * Includes the ID, owner, note, menu, assignments, and extra tasks.
     *
     * @return A string representation of the {@code SummarySheet}.
     */
    public String testString() {
        StringBuilder result = new StringBuilder();
        result.append("SummarySheet ID: ").append(id).append("\n");
        result.append("Owner: ").append(owner != null ? owner.getUserName() : "N/A").append("\n");
        result.append("Note: ").append(note != null ? note : "N/A").append("\n");

        result.append("Menu:\n");
        if (menu != null) {
            result.append(menu.testString()).append("\n");
        } else {
            result.append("N/A\n");
        }

        result.append("Assignments:\n");
        if (assignments != null) {
            for (Assignment a : assignments) {
                result.append(a != null ? a.toString() : "N/A").append("\n");
            }
        } else {
            result.append("N/A\n");
        }

        result.append("Extra Tasks:\n");
        if (extraTask != null) {
            for (Recipe ib : extraTask) {
                result.append(ib != null ? ib.toString() : "N/A").append("\n");
            }
        } else {
            result.append("N/A\n");
        }

        return result.toString();
    }

    /**
     * Loads extra tasks from the database into the list of extra tasks for this {@code SummarySheet}.
     */
    public void loadExtraTasks() {
        String query = "SELECT * FROM extratask WHERE sheet = " + this.id;
        PersistenceManager.executeQuery(query, rs -> {
            Recipe recipe = Recipe.loadRecipeById(rs.getInt("task"));
            extraTask.add(recipe);
        });
    }

    /**
     * Loads assignments from the database into the list of assignments for this {@code SummarySheet}.
     */
    public void loadAssignments() {
        Assignment.loadAssignments(this.id, this.assignments);
    }

    /**
     * <h2>GETTER AND SETTER</h2>
     */

    /**
     * Returns the ID of this {@code SummarySheet}.
     *
     * @return The ID of the {@code SummarySheet}.
     */
    public int getId() {
        return this.id;
    }

    /**
     * Returns the list of assignments associated with this {@code SummarySheet}.
     *
     * @return A {@code List} of {@code Assignment} objects.
     */
    public List<Assignment> getAssignments() {
        return this.assignments;
    }

    /**
     * Returns the list of extra tasks associated with this {@code SummarySheet}.
     *
     * @return A {@code List} of {@code Recipe} objects.
     */
    public List<Recipe> getExtraTask() {
        return this.extraTask;
    }

    /**
     * Returns the {@code Menu} associated with this {@code SummarySheet}.
     *
     * @return The {@code Menu} associated with the {@code SummarySheet}.
     */
    public Menu getMenu() {
        return this.menu;
    }

    public ServiceInfo getService() {
        return service;
    }

    /**
     * Sets a note for this {@code SummarySheet}.
     *
     * @param note The note to be set for the {@code SummarySheet}.
     */
    public void setNote(String note) {
        this.note = note;
    }

}
