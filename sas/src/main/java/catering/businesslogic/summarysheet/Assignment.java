package catering.businesslogic.summarysheet;

import catering.businesslogic.turn.Turn;
import catering.businesslogic.CatERing;
import catering.businesslogic.recipe.Recipe;
import catering.businesslogic.user.User;
import catering.persistence.PersistenceManager;
import catering.persistence.ResultHandler;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * This class represents an Assignment, which ties together a cook (User), a specific turn,
 * and a recipe that the cook is assigned to prepare during that turn. Each assignment
 * also tracks the portion of the recipe and the time associated with the task.
 */
public class Assignment {
    private int id;              // Unique identifier for the assignment
    private User cook;           // The cook (user) assigned to this task
    private Turn turn;           // The turn (shift) during which the assignment is scheduled
    private Recipe item;         // The recipe assigned to the cook
    private int portion;         // The number of portions to prepare
    private int time;            // The time allocated for the task in minutes

    /**
     * Constructor to initialize an Assignment with a cook, turn, and recipe.
     *
     * @param cook The cook assigned to the task
     * @param turn The turn in which the task takes place
     * @param item The recipe assigned for preparation
     */
    public Assignment(User cook, Turn turn, Recipe item) {
        this.cook = cook;
        this.turn = turn;
        this.item = item;
        this.portion = 0;   // Default portion set to 0
        this.time = 0;      // Default time set to 0
    }

    /**
     * Loads all assignments for a given summary sheet by the sheet's ID.
     * Populates the provided list with Assignment objects.
     *
     * @param sheetId     The ID of the summary sheet.
     * @param assignments The list of assignments to populate.
     */
    public static void loadAssignments(int sheetId, List<Assignment> assignments) {
        String query = "SELECT * FROM assignment WHERE sheet = " + sheetId;
        PersistenceManager.executeQuery(query, new ResultHandler() {
            @Override
            public void handle(ResultSet rs) throws SQLException {
                Turn turn = CatERing.getInstance().getTurnManager().findById(rs.getInt("turn"));
                User cook = User.loadUserById(rs.getInt("cook"));      // Load cook by their ID
                Recipe recipe = Recipe.loadRecipeById(rs.getInt("recipe"));  // Load recipe by its ID
                int portion = rs.getInt("portion");    // Retrieve portion size
                int time = rs.getInt("time");          // Retrieve time for the task

                // Only create and add assignment if all fields are valid
                if (turn != null && cook != null && recipe != null) {
                    Assignment assignment = new Assignment(cook, turn, recipe);
                    assignment.setPortion(portion);
                    assignment.setTime(time);
                    assignment.setId(rs.getInt("id"));
                    assignments.add(assignment);
                }
            }
        });
    }

    /**
     * Provides a string representation of the Assignment, including turn, cook, recipe, portion, and time.
     *
     * @return A string describing the assignment details.
     */
    @Override
    public String toString() {
        return "Assignment: " +
                "Turn: " + (turn != null ? turn.toString() : "N/A") +
                ", Cook: " + (cook != null ? cook.getUserName() : "N/A") +
                ", Recipe: " + (item != null ? item.getName() : "N/A") +
                ", Portion: " + portion +
                ", Time: " + time;
    }

    /**
     * Sets the recipe (task) for this assignment.
     *
     * @param item The recipe to assign.
     */
    public void setTask(Recipe item) {
        this.item = item;
    }

    /**
     * Gets the ID of this assignment.
     *
     * @return The unique identifier of the assignment.
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the ID for this assignment.
     *
     * @param id The unique identifier to set.
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Gets the cook assigned to this assignment.
     *
     * @return The cook (User) assigned.
     */
    public User getCook() {
        return cook;
    }

    /**
     * Sets the cook for this assignment.
     *
     * @param cook The cook (User) to assign.
     */
    public void setCook(User cook) {
        this.cook = cook;
    }

    /**
     * Gets the turn associated with this assignment.
     *
     * @return The turn (shift) assigned.
     */
    public Turn getTurn() {
        return turn;
    }

    /**
     * Sets the turn (shift) for this assignment.
     *
     * @param turn The turn (shift) to assign.
     */
    public void setTurn(Turn turn) {
        this.turn = turn;
    }

    /**
     * Gets the recipe assigned to this assignment.
     *
     * @return The recipe assigned.
     */
    public Recipe getRecipe() {
        return item;
    }

    /**
     * Gets the portion size assigned for the task.
     *
     * @return The portion size.
     */
    public int getPortion() {
        return portion;
    }

    /**
     * Sets the portion size for this assignment.
     *
     * @param portion The portion size to set.
     */
    public void setPortion(int portion) {
        this.portion = portion;
    }

    /**
     * Gets the time allocated for the task.
     *
     * @return The time in minutes.
     */
    public int getTime() {
        return time;
    }

    /**
     * Sets the time allocated for this assignment.
     *
     * @param time The time in minutes.
     */
    public void setTime(int time) {
        this.time = time;
    }
}
