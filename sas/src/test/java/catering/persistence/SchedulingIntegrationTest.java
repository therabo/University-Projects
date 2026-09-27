package catering.persistence;

import catering.businesslogic.CatERing;
import catering.businesslogic.turn.Turn;
import catering.businesslogic.turn.TurnManager;
import catering.businesslogic.user.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@EnabledIfEnvironmentVariable(named = "CATERING_INTEGRATION", matches = "true")
class SchedulingIntegrationTest {
    private final JdbcTurnRepository repository = new JdbcTurnRepository();

    @Test
    void groupAvailabilityIsAllOrNoneAndLocksEdits() {
        TurnManager manager = CatERing.getInstance().getTurnManager();
        CatERing.getInstance().getUserManager().fakeLogin("Lidia");
        LocalDate day = LocalDate.now().plusDays(21);
        Turn first = manager.createKitchenTurn(Date.valueOf(day), Time.valueOf("09:00:00"),
                Time.valueOf("11:00:00"), "Group test kitchen", null);
        Turn second = manager.createKitchenTurn(Date.valueOf(day.plusDays(1)), Time.valueOf("09:00:00"),
                Time.valueOf("11:00:00"), "Group test kitchen", null);
        try {
            int groupId = manager.groupKitchenTurns(List.of(first, second));
            assertEquals(2, manager.findByGroup(groupId).size());
            User cook = User.loadUser("Guido");
            CatERing.getInstance().getUserManager().fakeLogin("Guido");
            manager.declareAvailability(first);
            assertTrue(repository.isAvailable(first.getId(), cook.getId()));
            assertTrue(repository.isAvailable(second.getId(), cook.getId()));

            CatERing.getInstance().getUserManager().fakeLogin("Lidia");
            assertThrows(IllegalArgumentException.class, () -> manager.addLocationToTurn(second, "Other kitchen"));
            assertThrows(IllegalArgumentException.class, () -> manager.dissolveGroup(groupId));

            CatERing.getInstance().getUserManager().fakeLogin("Guido");
            manager.withdrawAvailability(second);
            assertFalse(repository.isAvailable(first.getId(), cook.getId()));
            assertFalse(repository.isAvailable(second.getId(), cook.getId()));
            CatERing.getInstance().getUserManager().fakeLogin("Lidia");
            manager.dissolveGroup(groupId);
            assertTrue(manager.findByGroup(groupId).isEmpty());
        } finally {
            repository.delete(second.getId());
            repository.delete(first.getId());
        }
    }

    @Test
    void recurringKitchenTurnsPersistAsOneSeries() {
        TurnManager manager = CatERing.getInstance().getTurnManager();
        CatERing.getInstance().getUserManager().fakeLogin("Lidia");
        List<Turn> turns = manager.createRecurringKitchenTurns(Date.valueOf(LocalDate.now().plusDays(28)),
                Time.valueOf("08:00:00"), Time.valueOf("10:00:00"),
                "Series test kitchen", TurnManager.Frequency.WEEKLY, 3);
        try {
            assertEquals(3, repository.findBySeries(turns.get(0).getSeriesId()).size());
            assertTrue(turns.stream().allMatch(turn -> turn.getService() == null));
            int updated = manager.updateSeriesFrom(turns.get(0).getSeriesId(),
                    new java.sql.Date(turns.get(1).getDate().getTime()).toLocalDate(),
                    Time.valueOf("08:30:00"), Time.valueOf("10:30:00"), "Series updated kitchen");
            assertEquals(2, updated);
            assertEquals("Series test kitchen", repository.findById(turns.get(0).getId()).getLocation());
            assertEquals("Series updated kitchen", repository.findById(turns.get(1).getId()).getLocation());
            assertEquals(2, manager.deleteSeriesFrom(turns.get(0).getSeriesId(),
                    new java.sql.Date(turns.get(1).getDate().getTime()).toLocalDate()));
            assertEquals(1, repository.findBySeries(turns.get(0).getSeriesId()).size());
        } finally {
            turns.forEach(turn -> repository.delete(turn.getId()));
        }
    }

    @Test
    void recurringGroupsCanBeManagedTogetherUntilAvailabilityExists() {
        TurnManager manager = CatERing.getInstance().getTurnManager();
        CatERing.getInstance().getUserManager().fakeLogin("Lidia");
        LocalDate day = LocalDate.now().plusDays(60);
        List<Turn> turns = new java.util.ArrayList<>();
        for (int index = 0; index < 4; index++) {
            turns.add(manager.createKitchenTurn(Date.valueOf(day.plusDays(index)), Time.valueOf("14:00:00"),
                    Time.valueOf("16:00:00"), "Recurring group kitchen", null));
        }
        try {
            List<Integer> groupIds = manager.groupKitchenTurnsRecurring(List.of(
                    List.of(turns.get(0), turns.get(1)), List.of(turns.get(2), turns.get(3))));
            assertEquals(2, groupIds.size());
            CatERing.getInstance().getUserManager().fakeLogin("Guido");
            manager.declareAvailability(turns.get(0));
            CatERing.getInstance().getUserManager().fakeLogin("Lidia");
            assertThrows(IllegalArgumentException.class,
                    () -> manager.dissolveRecurringGroups(groupIds.get(0)));
            CatERing.getInstance().getUserManager().fakeLogin("Guido");
            manager.withdrawAvailability(turns.get(1));
            CatERing.getInstance().getUserManager().fakeLogin("Lidia");
            manager.dissolveRecurringGroups(groupIds.get(0));
            assertTrue(groupIds.stream().allMatch(id -> manager.findByGroup(id).isEmpty()));
        } finally {
            turns.forEach(turn -> repository.delete(turn.getId()));
        }
    }
}
