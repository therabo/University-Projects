package catering.businesslogic.summarysheet;

import catering.businesslogic.turn.Turn;
import catering.businesslogic.recipe.Recipe;
import catering.businesslogic.user.User;

import java.util.List;

/**
 * The SheetEventReceiver interface defines the methods that handle different types of
 * updates and events related to a SummarySheet, such as creation, deletion, modifications,
 * and updates related to assignments and items (recipes).
 */
public interface SheetEventReceiver {

    /**
     * Called when a new SummarySheet is created.
     *
     * @param sheet The newly created SummarySheet.
     */
    void updateSheetCreated(SummarySheet sheet);

    /**
     * Called when a SummarySheet is deleted.
     *
     * @param sheet The deleted SummarySheet.
     */
    void updateSheetDeleted(SummarySheet sheet);

    /**
     * Called when new items (recipes) are added to a SummarySheet.
     *
     * @param sheet The SummarySheet to which the items were added.
     * @param items The list of items (recipes) added.
     */
    void updateItemAdded(SummarySheet sheet, List<Recipe> items);

    /**
     * Called when a new assignment is created for a SummarySheet.
     *
     * @param sheet      The SummarySheet containing the new assignment.
     * @param assignment The newly created assignment.
     */
    void updateAssignmentCreated(SummarySheet sheet, Assignment assignment);

    /**
     * Called when the portion for an assignment is updated.
     *
     * @param asg     The assignment for which the portion is updated.
     * @param portion The updated portion size.
     */
    void updateAssignmentPortion(Assignment asg, int portion);

    /**
     * Called when the time for an assignment is updated.
     *
     * @param asg  The assignment for which the time is updated.
     * @param time The updated time in minutes.
     */
    void updateAssignmentTime(Assignment asg, int time);

    /**
     * Called when the notes of a SummarySheet are updated.
     *
     * @param currentSheet The SummarySheet whose notes are updated.
     * @param note         The updated notes.
     */
    void updateSummarySheetNotes(SummarySheet currentSheet, String note);

    /**
     * Called when the cook (user) assigned to an assignment is updated.
     *
     * @param asg  The assignment for which the cook is updated.
     * @param cook The new cook (user) assigned.
     */
    void updateCookInAssignment(Assignment asg, User cook);

    /**
     * Called when the turn (shift) associated with an assignment is updated.
     *
     * @param asg  The assignment for which the turn is updated.
     * @param turn The updated turn (shift).
     */
    void updateTurnInAssignment(Assignment asg, Turn turn);

    /**
     * Called when the recipe (task) associated with an assignment is modified.
     *
     * @param task The recipe that has been modified.
     * @param asg  The assignment associated with the recipe.
     */
    void updateItemModified(Recipe task, Assignment asg);

    /**
     * Called when an assignment is deleted.
     *
     * @param asg The assignment that has been deleted.
     */
    void updateAssignmentDeleted(Assignment asg);

    /**
     * Called when an extra task (recipe) is deleted from a SummarySheet.
     *
     * @param sheet The SummarySheet from which the recipe was deleted.
     * @param item  The recipe that was deleted.
     */
    void updateExtraTaskDeleted(SummarySheet sheet, Recipe item);
}