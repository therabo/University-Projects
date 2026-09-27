package catering.businesslogic.summarysheet;

import catering.businesslogic.event.ServiceInfo;
import catering.businesslogic.user.User;
import org.junit.jupiter.api.Test;

import java.sql.Date;
import java.sql.Time;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SummarySheetRulesTest {
    @Test
    void ownerIsMatchedByPersistedIdentity() {
        User owner = new User() {
            @Override public int getId() { return 2; }
        };
        ServiceInfo service = new ServiceInfo() {
            @Override public catering.businesslogic.menu.Menu getMenu(int ignored) { return null; }
        };
        SummarySheet sheet = new SummarySheet(owner, service);
        assertTrue(sheet.isOwner(owner));
        assertFalse(sheet.isOwner(new User() {
            @Override public int getId() { return 3; }
        }));
        assertFalse(sheet.isOwner(null));
        assertNotNull(sheet.getAssignments());
        assertNotNull(sheet.getExtraTask());
    }

    @Test
    void progressUsesServiceSchedule() {
        LocalDateTime start = LocalDateTime.now().minusMinutes(5);
        LocalDateTime end = LocalDateTime.now().plusMinutes(5);
        ServiceInfo active = scheduled(start, end);
        SummarySheet activeSheet = new SummarySheet(new User(), active);
        assertTrue(activeSheet.isInProgress());

        LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
        assertFalse(new SummarySheet(new User(), scheduled(yesterday, yesterday.plusHours(1))).isInProgress());
    }

    private ServiceInfo scheduled(LocalDateTime start, LocalDateTime end) {
        return new ServiceInfo() {
            @Override public catering.businesslogic.menu.Menu getMenu(int ignored) { return null; }
            @Override public Date getDate() { return Date.valueOf(start.toLocalDate()); }
            @Override public Time getTimeStart() { return Time.valueOf(start.toLocalTime()); }
            @Override public Time getTimeEnd() { return Time.valueOf(end.toLocalTime()); }
        };
    }
}
