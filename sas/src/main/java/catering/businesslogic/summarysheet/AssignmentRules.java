package catering.businesslogic.summarysheet;

import catering.businesslogic.turn.Turn;
import catering.businesslogic.recipe.Recipe;
import catering.businesslogic.user.User;

public final class AssignmentRules {
    private AssignmentRules() {
    }

    public static void validate(SummarySheet sheet, User cook, Turn turn, Recipe recipe,
                                int portion, int minutes, Assignment excluded) {
        if (sheet == null || sheet.getService() == null || cook == null || !cook.isCook()
                || turn == null || turn.getKind() != Turn.Kind.KITCHEN || recipe == null
                || turn.getService() != null && turn.getService().getId() != sheet.getService().getId()) {
            throw new IllegalArgumentException("Assignment requires a cook, a recipe and a turn for the sheet's service");
        }
        if (portion < 0 || minutes < 0) {
            throw new IllegalArgumentException("Portions and minutes cannot be negative");
        }
        if (turn.getAssignedUsers().stream().noneMatch(user -> user.getId() == cook.getId())) {
            throw new IllegalArgumentException("Cook must be assigned to the kitchen turn");
        }
        long duration = turn.durationMinutes();
        long allocated = sheet.getAssignments().stream()
                .filter(asg -> asg != excluded && asg.getCook() != null && asg.getTurn() != null
                        && asg.getCook().getId() == cook.getId() && asg.getTurn().getId() == turn.getId())
                .mapToLong(Assignment::getTime)
                .sum();
        if (allocated + minutes > duration) {
            throw new IllegalArgumentException("Allocated work exceeds the cook's turn duration");
        }
    }
}
