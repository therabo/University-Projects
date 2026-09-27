package catering.businesslogic.summarysheet;

import catering.businesslogic.turn.Turn;
import catering.businesslogic.UseCaseLogicException;
import catering.businesslogic.event.ServiceInfo;
import catering.businesslogic.recipe.Recipe;
import catering.businesslogic.user.User;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Manages operations related to {@code SummarySheet} objects and notifies registered event receivers of changes.
 */
public class SummarySheetManager {
    private SummarySheet currentSheet;
    private final Supplier<User> currentUser;

    private final List<SheetEventReceiver> eventReceivers = new ArrayList<>();

    public SummarySheetManager(Supplier<User> currentUser) {
        this.currentUser = Objects.requireNonNull(currentUser);
    }


    /**
     * <h2>ALL NOTIFY METHODS</h2>
     */

    /**
     * Notifies all registered event receivers that a {@code SummarySheet} has been created.
     *
     * @param sheet The {@code SummarySheet} that was created.
     * @throws SQLException If a database access error occurs.
     */
    private void notifySheetCreated(SummarySheet sheet) throws SQLException {
        for (SheetEventReceiver receiver : eventReceivers) {
            receiver.updateSheetCreated(sheet);
        }
    }

    /**
     * Notifies all registered event receivers that a {@code SummarySheet} has been deleted.
     *
     * @param sheet The {@code SummarySheet} that was deleted.
     */
    private void notifySheetDeleted(SummarySheet sheet) {
        for (SheetEventReceiver receiver : eventReceivers) {
            receiver.updateSheetDeleted(sheet);
        }
    }

    /**
     * Notifies all registered event receivers that an {@code Assignment} has been deleted.
     *
     * @param asg The {@code Assignment} that was deleted.
     */
    private void notifyAssignmentDeleted(Assignment asg) {
        for (SheetEventReceiver receiver : eventReceivers) {
            receiver.updateAssignmentDeleted(asg);
        }
    }

    /**
     * Notifies all registered event receivers that items have been added to the {@code SummarySheet}.
     *
     * @param items The list of {@code Recipe} items that were added.
     */
    private void notifyItemAdded(List<Recipe> items) {
        for (SheetEventReceiver receiver : eventReceivers) {
            receiver.updateItemAdded(currentSheet, items);
        }
    }

    /**
     * Notifies all registered event receivers that an {@code Assignment} has been created for a {@code SummarySheet}.
     *
     * @param sheet The {@code SummarySheet} where the assignment was created.
     * @param asg   The {@code Assignment} that was created.
     */
    private void notifyAssignmentCreated(SummarySheet sheet, Assignment asg) {
        for (SheetEventReceiver receiver : eventReceivers) {
            receiver.updateAssignmentCreated(sheet, asg);
        }
    }

    /**
     * Notifies all registered event receivers that a portion has been added to an {@code Assignment}.
     *
     * @param asg     The {@code Assignment} where the portion was added.
     * @param portion The amount of the portion added.
     */
    private void notifyPortionAdded(Assignment asg, int portion) {
        for (SheetEventReceiver receiver : eventReceivers) {
            receiver.updateAssignmentPortion(asg, portion);
        }
    }

    /**
     * Notifies all registered event receivers that a time has been added to an {@code Assignment}.
     *
     * @param asg  The {@code Assignment} where the time was added.
     * @param time The time added.
     */
    private void notifyTimeAdded(Assignment asg, int time) {
        for (SheetEventReceiver receiver : eventReceivers) {
            receiver.updateAssignmentTime(asg, time);
        }
    }

    /**
     * Notifies all registered event receivers that a note has been added to the {@code SummarySheet}.
     *
     * @param note The note added.
     */
    private void notifyNoteAdded(String note) {
        for (SheetEventReceiver receiver : eventReceivers) {
            receiver.updateSummarySheetNotes(currentSheet, note);
        }
    }

    /**
     * Notifies all registered event receivers that the cook in an {@code Assignment} has been modified.
     *
     * @param asg  The {@code Assignment} where the cook was modified.
     * @param cook The new {@code User} assigned as cook.
     */
    private void notifyCookModified(Assignment asg, User cook) {
        for (SheetEventReceiver receiver : eventReceivers) {
            receiver.updateCookInAssignment(asg, cook);
        }
    }

    /**
     * Notifies all registered event receivers that a new turn has been assigned to an {@code Assignment}.
     *
     * @param asg  The {@code Assignment} where the turn was assigned.
     * @param turn The {@code Turn} assigned.
     */
    private void notifyNewTurnInAssignment(Assignment asg, Turn turn) {
        for (SheetEventReceiver receiver : eventReceivers) {
            receiver.updateTurnInAssignment(asg, turn);
        }
    }

    /**
     * Notifies all registered event receivers that the item in an {@code Assignment} has been modified.
     *
     * @param task The {@code Recipe} that was modified.
     * @param asg  The {@code Assignment} where the item was modified.
     */
    private void notifyItemModified(Recipe task, Assignment asg) {
        for (SheetEventReceiver receiver : eventReceivers) {
            receiver.updateItemModified(task, asg);
        }
    }

    /**
     * Notifies all registered event receivers that an extra task has been deleted from the {@code SummarySheet}.
     *
     * @param item The {@code Recipe} that was deleted.
     */
    private void notifyExtraTaskDeleted(Recipe item) {
        for (SheetEventReceiver receiver : eventReceivers) {
            receiver.updateExtraTaskDeleted(currentSheet, item);
        }
    }

    /**
     * <h2>METHODS FOR MAIN OPERATIONS</h2>
     */

    /**
     * Adds a {@code SheetEventReceiver} to receive notifications.
     *
     * @param receiver The {@code SheetEventReceiver} to add.
     */
    public void addReceiver(SheetEventReceiver receiver) {
        eventReceivers.add(Objects.requireNonNull(receiver));
    }

    /**
     * Removes a {@code SheetEventReceiver} from receiving notifications.
     *
     * @param receiver The {@code SheetEventReceiver} to remove.
     */
    public void removeReceiver(SheetEventReceiver receiver) {
        eventReceivers.remove(receiver);
    }

    /**
     * Creates a new {@code SummarySheet} based on the provided {@code ServiceInfo}.
     *
     * @param service The {@code ServiceInfo} used to create the {@code SummarySheet}.
     * @return The created {@code SummarySheet}.
     * @throws SQLException          If a database access error occurs.
     * @throws UseCaseLogicException If the current user is not authorized to create the sheet.
     */
    public SummarySheet createSummarySheet(ServiceInfo service) throws SQLException, UseCaseLogicException {
        User user = currentUser.get();
        if (service != null && user != null && service.isAssignedChef(user) && service.isAssignedMenu()) {
            SummarySheet sheet = new SummarySheet(user, service);
            notifySheetCreated(sheet);
            service.setSheet(sheet);
            setCurrentSheet(sheet);
            return sheet;
        } else {
            throw new UseCaseLogicException();
        }
    }

    /**
     * Deletes the specified {@code SummarySheet}.
     *
     * @param sheet The {@code SummarySheet} to delete.
     */
    public void deleteSheet(SummarySheet sheet) throws UseCaseLogicException {
        User user = currentUser.get();
        if (sheet == null || !sheet.isOwner(user) || sheet.isInProgress()) {
            throw new UseCaseLogicException("Only the sheet owner can delete a sheet outside an active service");
        }
        notifySheetDeleted(sheet);
    }

    /**
     * Modifies the specified {@code SummarySheet}.
     *
     * @param sheet The {@code SummarySheet} to modify.
     * @return {@code true} if the modification was successful; {@code false} otherwise.
     * @throws UseCaseLogicException If the current user is not authorized to modify the sheet.
     */
    public boolean modifySheet(SummarySheet sheet) throws UseCaseLogicException {
        User user = currentUser.get();
        if (sheet != null && sheet.isOwner(user) && !sheet.isInProgress()) {
            return true;
        } else {
            throw new UseCaseLogicException("Only the sheet owner can modify a sheet outside an active service");
        }
    }

    /**
     * Adds a recipe to the current {@code SummarySheet}.
     *
     * @param item The {@code Recipe} to add.
     * @throws UseCaseLogicException If the current user is not authorized to add the item.
     */
    public void addRecipe(Recipe item) throws UseCaseLogicException {
        modifySheet(currentSheet);
        List<Recipe> items = new ArrayList<>();
        items.add(item);
        notifyItemAdded(items);
        currentSheet.addExtraTask(item);
    }

    /**
     * Removes a recipe from the current {@code SummarySheet}.
     *
     * @param item The {@code Recipe} to remove.
     * @throws UseCaseLogicException If the current user is not authorized to remove the item.
     */
    public void removeRecipe(Recipe item) throws UseCaseLogicException {
        modifySheet(currentSheet);
        notifyExtraTaskDeleted(item);
        currentSheet.deleteExtraTask(item);
    }

    /**
     * Moves a recipe to a new position in the current {@code SummarySheet}.
     *
     * @param item The {@code Recipe} to move.
     * @param pos  The new position for the {@code Recipe}.
     * @throws UseCaseLogicException If the current user is not authorized to move the item.
     */
    public void moveRecipe(Recipe item, int pos) throws UseCaseLogicException {
        modifySheet(currentSheet);
        currentSheet.moveRecipe(item, pos);
    }

    /**
     * Creates an assignment for a recipe in the specified {@code SummarySheet}.
     *
     * @param sheet   The {@code SummarySheet} where the assignment is created.
     * @param cook    The {@code User} assigned to the task.
     * @param turn    The {@code Turn} assigned to the task.
     * @param item    The {@code Recipe} assigned.
     * @param portion The portion assigned.
     * @param time    The time allocated.
     * @throws UseCaseLogicException If the current user is not authorized to create the assignment.
     */
    public void createAssignment(SummarySheet sheet, User cook, Turn turn, Recipe item, Integer portion, Integer time) throws UseCaseLogicException {
        if (sheet == null || sheet.getId() == 0) {
            throw new IllegalArgumentException("SummarySheet cannot be null and must have a valid ID");
        }
        modifySheet(sheet);
        if (portion == null) {
            portion = 0;
        }
        if (time == null) {
            time = 0;
        }
        AssignmentRules.validate(sheet, cook, turn, item, portion, time, null);
        Assignment assignment = new Assignment(cook, turn, item);
        assignment.setPortion(portion);
        assignment.setTime(time);
        notifyAssignmentCreated(sheet, assignment);
        sheet.getAssignments().add(assignment);
    }

    /**
     * Deletes the specified {@code Assignment} from the current {@code SummarySheet}.
     *
     * @param asg The {@code Assignment} to delete.
     */
    public void deleteAssignment(Assignment asg) throws UseCaseLogicException {
        modifySheet(currentSheet);
        if (asg == null || !currentSheet.getAssignments().contains(asg)) {
            throw new IllegalArgumentException("Assignment does not belong to the current sheet");
        }
        notifyAssignmentDeleted(asg);
        currentSheet.getAssignments().remove(asg);
    }

    /**
     * Modifies the cook assigned to an {@code Assignment}.
     *
     * @param cook The new {@code User} assigned as cook.
     * @param asg  The {@code Assignment} to modify.
     * @throws UseCaseLogicException If the current user is not authorized to modify the cook.
     */
    public void modifyCook(User cook, Assignment asg) throws UseCaseLogicException {
        modifySheet(currentSheet);
        AssignmentRules.validate(currentSheet, cook, asg.getTurn(), asg.getRecipe(), asg.getPortion(), asg.getTime(), asg);
        notifyCookModified(asg, cook);
        currentSheet.setNewCook(cook, asg);
    }

    /**
     * Modifies the turn assigned to an {@code Assignment}.
     *
     * @param turn The new {@code Turn} assigned.
     * @param asg  The {@code Assignment} to modify.
     * @throws UseCaseLogicException If the current user is not authorized to modify the turn.
     */
    public void modifyTurn(Turn turn, Assignment asg) throws UseCaseLogicException {
        modifySheet(currentSheet);
        AssignmentRules.validate(currentSheet, asg.getCook(), turn, asg.getRecipe(), asg.getPortion(), asg.getTime(), asg);
        notifyNewTurnInAssignment(asg, turn);
        currentSheet.setNewTurn(turn, asg);
    }

    /**
     * Modifies the task assigned to an {@code Assignment}.
     *
     * @param task The new {@code Recipe} assigned.
     * @param asg  The {@code Assignment} to modify.
     * @throws UseCaseLogicException If the current user is not authorized to modify the task.
     */
    public void modifyTask(Recipe task, Assignment asg) throws UseCaseLogicException {
        modifySheet(currentSheet);
        notifyItemModified(task, asg);
        currentSheet.setNewItem(task, asg);
    }

    /**
     * Assigns a portion to an {@code Assignment}.
     *
     * @param portion The portion to assign.
     * @param asg     The {@code Assignment} to update.
     * @throws UseCaseLogicException If the current user is not authorized to assign the portion.
     */
    public void assignPortion(int portion, Assignment asg) throws UseCaseLogicException {
        modifySheet(currentSheet);
        AssignmentRules.validate(currentSheet, asg.getCook(), asg.getTurn(), asg.getRecipe(), portion, asg.getTime(), asg);
        notifyPortionAdded(asg, portion);
        currentSheet.assignPortion(portion, asg);
    }

    /**
     * Modifies the portion assigned to an {@code Assignment}.
     *
     * @param newPortion The new portion to assign.
     * @param asg        The {@code Assignment} to update.
     * @throws UseCaseLogicException If the current user is not authorized to modify the portion.
     */
    public void modifyPortion(int newPortion, Assignment asg) throws UseCaseLogicException {
        assignPortion(newPortion, asg);
    }

    /**
     * Assigns time to an {@code Assignment}.
     *
     * @param time The time to assign.
     * @param asg  The {@code Assignment} to update.
     * @throws UseCaseLogicException If the current user is not authorized to assign the time.
     */
    public void assignTime(int time, Assignment asg) throws UseCaseLogicException {
        modifySheet(currentSheet);
        AssignmentRules.validate(currentSheet, asg.getCook(), asg.getTurn(), asg.getRecipe(), asg.getPortion(), time, asg);
        notifyTimeAdded(asg, time);
        currentSheet.assignTime(time, asg);
    }

    /**
     * Adds a note to the current {@code SummarySheet}.
     *
     * @param note The note to add.
     * @throws UseCaseLogicException If the current user is not authorized to add the note.
     */
    public void writeNote(String note) throws UseCaseLogicException {
        modifySheet(currentSheet);
        notifyNoteAdded(note);
        currentSheet.setNote(note);
    }

    /**
     * <h2>GETTER AND SETTER</h2>
     */

    /**
     * Returns the current {@code SummarySheet}.
     *
     * @return The current {@code SummarySheet}.
     */
    public SummarySheet getCurrentSheet() {
        return currentSheet;
    }

    /**
     * Sets the current {@code SummarySheet}.
     *
     * @param currentSheet The {@code SummarySheet} to set as current.
     */
    public void setCurrentSheet(SummarySheet currentSheet) {
        this.currentSheet = currentSheet;
    }

}
