package catering.businesslogic.turn;

import catering.businesslogic.event.ServiceInfo;
import catering.businesslogic.user.User;
import org.junit.jupiter.api.Test;

import java.sql.Date;
import java.sql.Time;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TurnManagerTest {
    private final MemoryRepository repository = new MemoryRepository();
    private final User organiser = new User() {
        @Override public int getId() { return 1; }
        @Override public boolean isOrganiser() { return true; }
    };
    private final User chef = new User() {
        @Override public int getId() { return 2; }
        @Override public boolean isChef() { return true; }
    };
    private final AtomicReference<User> currentUser = new AtomicReference<>(organiser);
    private final TurnManager manager = new TurnManager(repository, currentUser::get,
            Clock.fixed(Instant.parse("2026-09-24T10:00:00Z"), ZoneOffset.UTC));

    private ServiceInfo service() {
        return new ServiceInfo() {
            @Override public int getId() { return 1; }
            @Override public Date getDate() { return Date.valueOf("2026-09-25"); }
            @Override public Time getTimeStart() { return Time.valueOf("10:00:00"); }
            @Override public Time getTimeEnd() { return Time.valueOf("12:00:00"); }
        };
    }

    @Test
    void createsPersistsAndDeletesTurn() {
        Turn turn = manager.createTurn(service());
        assertEquals(1, turn.getId());
        assertEquals(1, turn.getService().getId());
        assertEquals("SEDE", turn.getLocation());
        assertEquals(1, manager.findAll().size());
        assertTrue(manager.deleteTurn(turn));
        assertFalse(manager.deleteTurn(turn));
    }

    @Test
    void assignsOneUserOnlyOnce() {
        Turn turn = manager.createTurn(service());
        User user = new User() {
            @Override public int getId() { return 7; }
            @Override public boolean isCook() { return true; }
        };
        currentUser.set(chef);
        manager.assignTurn(turn, user);
        manager.assignTurn(turn, user);
        assertEquals(1, turn.getAssignedUsers().size());
        assertEquals(2, repository.assignmentCalls);
    }

    @Test
    void rejectsInvalidScheduleBeforePersistence() {
        assertThrows(IllegalArgumentException.class,
                () -> manager.createTurn(service(), Date.valueOf("2026-09-25"),
                        Time.valueOf("10:00:00"), Time.valueOf("10:00:00"), "SEDE"));
        assertTrue(repository.turns.isEmpty());
    }

    @Test
    void createsIndependentRecurringKitchenTurns() {
        List<Turn> turns = manager.createRecurringKitchenTurns(Date.valueOf("2026-09-25"),
                Time.valueOf("09:00:00"), Time.valueOf("11:00:00"), "Main kitchen",
                TurnManager.Frequency.WEEKLY, 3);
        assertEquals(3, turns.size());
        assertEquals(Date.valueOf("2026-10-02"), turns.get(1).getDate());
        assertEquals(1, turns.get(0).getSeriesId());
        assertTrue(turns.stream().allMatch(turn -> turn.getService() == null));
    }

    @Test
    void groupsKitchenTurnsAndRejectsNonOwner() {
        Turn first = manager.createKitchenTurn(Date.valueOf("2026-09-25"), Time.valueOf("09:00:00"),
                Time.valueOf("11:00:00"), "Main kitchen", null);
        Turn second = manager.createKitchenTurn(Date.valueOf("2026-09-26"), Time.valueOf("09:00:00"),
                Time.valueOf("11:00:00"), "Main kitchen", null);
        int groupId = manager.groupKitchenTurns(List.of(first, second));
        assertEquals(2, manager.findByGroup(groupId).size());
        currentUser.set(chef);
        assertThrows(IllegalArgumentException.class, () -> manager.dissolveGroup(groupId));
        currentUser.set(organiser);
        manager.dissolveGroup(groupId);
        assertTrue(manager.findByGroup(groupId).isEmpty());
    }

    @Test
    void serviceTurnIncludesSetupAndCleanup() {
        Turn turn = manager.createServiceTurn(service(), 30, 45, "Event venue", null);
        assertEquals(Turn.Kind.SERVICE, turn.getKind());
        assertEquals(Time.valueOf("09:30:00"), turn.getStart());
        assertEquals(Time.valueOf("12:45:00"), turn.getEnd());
    }

    private static class MemoryRepository implements TurnRepository {
        private final List<Turn> turns = new ArrayList<>();
        private final Map<Integer, List<Integer>> groups = new HashMap<>();
        private final Map<Integer, List<Integer>> availability = new HashMap<>();
        private int assignmentCalls;

        @Override public Turn save(Turn turn) {
            turn.setId(turns.size() + 1);
            turns.add(turn);
            return turn;
        }
        @Override public Turn findById(int id) {
            return turns.stream().filter(turn -> turn.getId() == id).findFirst().orElse(null);
        }
        @Override public List<Turn> findAll() { return new ArrayList<>(turns); }
        @Override public List<Turn> findByService(int id) {
            List<Turn> result = new ArrayList<>();
            for (Turn turn : turns) {
                if (turn.getService().getId() == id) result.add(turn);
            }
            return result;
        }
        @Override public List<Turn> findAssignedToUser(int userId) { return new ArrayList<>(); }
        @Override public List<Turn> findBySeries(int id) {
            List<Turn> result = new ArrayList<>();
            for (Turn turn : turns) if (turn.getSeriesId() == id) result.add(turn);
            return result;
        }
        @Override public List<Turn> findByGroup(int id) {
            List<Turn> result = new ArrayList<>();
            for (Turn turn : turns) if (turn.getGroupId() == id) result.add(turn);
            return result;
        }
        @Override public List<Turn> saveSeries(List<Turn> series, String frequency, int ownerId) {
            for (Turn turn : series) {
                turn.setSeriesId(1);
                save(turn);
            }
            return series;
        }
        @Override public int createGroup(List<Integer> ids) {
            int id = groups.size() + 1;
            groups.put(id, ids);
            for (Turn turn : turns) if (ids.contains(turn.getId())) turn.setGroupId(id);
            return id;
        }
        @Override public void dissolveGroup(int id) {
            groups.remove(id);
            for (Turn turn : turns) if (turn.getGroupId() == id) turn.setGroupId(0);
        }
        @Override public void updateSchedule(Turn turn) { }
        @Override public void setAvailability(int turnId, int userId, boolean available) {
            List<Integer> users = availability.computeIfAbsent(turnId, ignored -> new ArrayList<>());
            if (available && !users.contains(userId)) users.add(userId);
            if (!available) users.remove(Integer.valueOf(userId));
        }
        @Override public boolean hasAvailability(int turnId) {
            return !availability.getOrDefault(turnId, new ArrayList<>()).isEmpty();
        }
        @Override public boolean isAvailable(int turnId, int userId) { return true; }
        @Override public boolean delete(int id) { return turns.removeIf(turn -> turn.getId() == id); }
        @Override public void updateLocation(Turn turn) { }
        @Override public void assign(Turn turn, User user) { assignmentCalls++; }
    }
}
