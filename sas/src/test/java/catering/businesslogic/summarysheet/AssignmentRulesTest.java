package catering.businesslogic.summarysheet;

import catering.businesslogic.turn.Turn;
import catering.businesslogic.event.ServiceInfo;
import catering.businesslogic.recipe.Recipe;
import catering.businesslogic.user.User;
import org.junit.jupiter.api.Test;

import java.sql.Date;
import java.sql.Time;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AssignmentRulesTest {
    private final ServiceInfo service = new ServiceInfo() {
        @Override public int getId() { return 1; }
    };
    private final SummarySheet sheet = new SummarySheet() {
        @Override public ServiceInfo getService() { return service; }
    };
    private final User cook = new User() {
        @Override public int getId() { return 7; }
        @Override public boolean isCook() { return true; }
    };
    private final Recipe recipe = new Recipe("Risotto");

    private Turn turn(String start, String end) {
        Turn turn = new Turn();
        turn.setId(3);
        turn.setService(service);
        turn.setDate(Date.valueOf("2026-09-25"));
        turn.setStart(Time.valueOf(start));
        turn.setEnd(Time.valueOf(end));
        turn.assign(cook);
        return turn;
    }

    @Test
    void rejectsWorkLongerThanTurn() {
        assertThrows(IllegalArgumentException.class,
                () -> AssignmentRules.validate(sheet, cook, turn("10:00:00", "12:00:00"), recipe, 1, 150, null));
    }

    @Test
    void accountsForExistingWorkForSameCookAndTurn() {
        Turn turn = turn("10:00:00", "12:00:00");
        Assignment existing = new Assignment(cook, turn, recipe);
        existing.setTime(100);
        sheet.getAssignments().add(existing);
        assertThrows(IllegalArgumentException.class,
                () -> AssignmentRules.validate(sheet, cook, turn, recipe, 1, 30, null));
        assertDoesNotThrow(() -> AssignmentRules.validate(sheet, cook, turn, recipe, 1, 20, null));
    }

    @Test
    void acceptsOvernightTurn() {
        assertDoesNotThrow(() -> AssignmentRules.validate(sheet, cook, turn("23:00:00", "01:00:00"), recipe, 1, 120, null));
    }

    @Test
    void rejectsWrongServiceAndNegativeValues() {
        Turn turn = turn("10:00:00", "12:00:00");
        turn.setService(new ServiceInfo() {
            @Override public int getId() { return 2; }
        });
        assertThrows(IllegalArgumentException.class,
                () -> AssignmentRules.validate(sheet, cook, turn, recipe, 1, 30, null));
        turn.setService(service);
        assertThrows(IllegalArgumentException.class,
                () -> AssignmentRules.validate(sheet, cook, turn, recipe, -1, 30, null));
    }
}
