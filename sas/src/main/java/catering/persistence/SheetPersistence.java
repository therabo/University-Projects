package catering.persistence;

import catering.businesslogic.summarysheet.Assignment;
import catering.businesslogic.summarysheet.SheetEventReceiver;
import catering.businesslogic.summarysheet.SummarySheet;
import catering.businesslogic.turn.Turn;
import catering.businesslogic.recipe.Recipe;
import catering.businesslogic.user.User;

import java.util.List;

/**
 * Implementation of the {@link SheetEventReceiver} interface for handling persistence operations.
 * <p>
 * This class provides concrete implementations for the methods defined in the {@link SheetEventReceiver}
 * interface to interact with the {@link SummarySheet} class for database operations.
 * </p>
 */
public class SheetPersistence implements SheetEventReceiver {

    @Override
    public void updateSheetCreated(SummarySheet sheet) {
        SummarySheet.saveNewSummarySheet(sheet);
    }

    @Override
    public void updateSheetDeleted(SummarySheet sheet) {
        SummarySheet.deleteSheet(sheet);
    }

    @Override
    public void updateItemAdded(SummarySheet sheet, List<Recipe> items) {
        SummarySheet.addItem(sheet, items);
    }

    @Override
    public void updateAssignmentCreated(SummarySheet sheet, Assignment assignment) {
        SummarySheet.saveNewAssignment(sheet, assignment);
    }

    @Override
    public void updateAssignmentPortion(Assignment asg, int portion) {
        SummarySheet.addPortion(asg, portion);
    }

    @Override
    public void updateAssignmentTime(Assignment asg, int time) {
        SummarySheet.addTime(asg, time);
    }

    @Override
    public void updateSummarySheetNotes(SummarySheet sheet, String note) {
        SummarySheet.addNote(sheet, note);
    }

    @Override
    public void updateCookInAssignment(Assignment asg, User cook) {
        SummarySheet.modifyCookInAssignment(asg, cook);
    }

    @Override
    public void updateTurnInAssignment(Assignment asg, Turn turn) {
        SummarySheet.modifyTurnInAssignment(asg, turn);
    }

    @Override
    public void updateItemModified(Recipe task, Assignment asg) {
        SummarySheet.modifyTaskInAssignment(asg, task);
    }

    @Override
    public void updateAssignmentDeleted(Assignment asg) {
        SummarySheet.deleteAssignment(asg);
    }

    @Override
    public void updateExtraTaskDeleted(SummarySheet sheet, Recipe item) {
        SummarySheet.removeItem(sheet, item);
    }

}