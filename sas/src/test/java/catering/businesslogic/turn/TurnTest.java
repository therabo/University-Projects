package catering.businesslogic.turn;

import org.junit.jupiter.api.Test;

import java.sql.Date;
import java.sql.Time;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TurnTest {
    private Turn turn(String date, String start, String end) {
        Turn turn = new Turn();
        turn.setDate(Date.valueOf(date));
        turn.setStart(Time.valueOf(start));
        turn.setEnd(Time.valueOf(end));
        return turn;
    }

    @Test
    void detectsOverlapAndAllowsAdjacentTurns() {
        Turn morning = turn("2026-09-25", "10:00:00", "12:00:00");
        assertTrue(morning.overlaps(turn("2026-09-25", "11:00:00", "13:00:00")));
        assertFalse(morning.overlaps(turn("2026-09-25", "12:00:00", "14:00:00")));
        assertEquals(120, morning.durationMinutes());
    }

    @Test
    void comparesOvernightTurnsAcrossDates() {
        Turn night = turn("2026-09-25", "23:00:00", "01:00:00");
        assertTrue(night.overlaps(turn("2026-09-26", "00:30:00", "02:00:00")));
        assertFalse(night.overlaps(turn("2026-09-26", "01:00:00", "02:00:00")));
        assertEquals(120, night.durationMinutes());
    }

    @Test
    void rejectsIncompleteAndEmptySchedules() {
        assertThrows(IllegalArgumentException.class, new Turn()::durationMinutes);
        assertThrows(IllegalArgumentException.class,
                () -> turn("2026-09-25", "10:00:00", "10:00:00").durationMinutes());
    }
}
