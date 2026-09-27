package catering.persistence;

import catering.businesslogic.CatERing;
import catering.businesslogic.event.EventWorkflowManager;
import catering.businesslogic.event.ServiceInfo;
import catering.businesslogic.turn.Turn;
import catering.businesslogic.user.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@EnabledIfEnvironmentVariable(named = "CATERING_INTEGRATION", matches = "true")
class EventWorkflowIntegrationTest {
    @Test
    void cancelsOwnedDraftEventInsteadOfDeletingAnApprovedOne() {
        EventWorkflowManager workflow = new EventWorkflowManager();
        User organiser = User.loadUser("Lidia");
        LocalDate date = LocalDate.now().plusDays(50);
        int eventId = workflow.create("Cancelled event", date, date, 10, "Test venue", "Test client",
                null, null, List.of(new EventWorkflowManager.ServiceDraft(
                        "Aperitif", date, LocalTime.of(18, 0), LocalTime.of(19, 0), 10, "Test venue")),
                organiser);
        try {
            workflow.cancel(eventId, "Client request", organiser);
            assertEquals("CANCELLED", workflow.status(eventId));
            assertThrows(IllegalArgumentException.class,
                    () -> workflow.deleteDraft(eventId, organiser));
        } finally {
            PersistenceManager.executeUpdate("DELETE FROM services WHERE event_id = ?",
                    statement -> statement.setInt(1, eventId));
            PersistenceManager.executeUpdate("DELETE FROM events WHERE id = ?",
                    statement -> statement.setInt(1, eventId));
        }
    }

    @Test
    void completesEventMenuStaffAndClosureLifecycle() throws Exception {
        EventWorkflowManager workflow = new EventWorkflowManager();
        User organiser = User.loadUser("Lidia");
        User serviceStaff = User.loadUser("Paola");
        LocalDate date = LocalDate.now().plusDays(40);
        int eventId = workflow.create("Demo event", date, date, 30, "Villa Test", "Demo client",
                "Initial notes", "DEMO-ANNUAL", List.of(new EventWorkflowManager.ServiceDraft(
                        "Dinner", date, LocalTime.of(19, 30), LocalTime.of(22, 30), 30, "Villa Test")),
                organiser);
        int serviceId = serviceId(eventId);
        Turn serviceTurn = null;
        try {
            workflow.assignChef(serviceId, organiser, organiser);
            workflow.proposeMenu(serviceId, 1, organiser);
            int suggestionId = workflow.suggestMenuChange(serviceId, 2, true, organiser);
            workflow.decideSuggestion(suggestionId, true, organiser);
            workflow.approveMenus(eventId, organiser);
            assertEquals("IN_PROGRESS", workflow.status(eventId));
            assertEquals(List.of(), workflow.previousApprovedMenuIds("DEMO-ANNUAL", eventId));
            assertThrows(IllegalArgumentException.class,
                    () -> workflow.updateDraft(eventId, "Changed", "Elsewhere", "Client", null, organiser));
            workflow.updateActiveDetails(eventId, "Villa Test updated", "Demo client", "Updated notes", organiser);

            CatERing.getInstance().getUserManager().fakeLogin("Lidia");
            serviceTurn = CatERing.getInstance().getTurnManager().createServiceTurn(
                    ServiceInfo.loadServiceInfoById(serviceId), 30, 30, "Villa Test",
                    LocalDateTime.now().plusDays(10));
            CatERing.getInstance().getUserManager().fakeLogin("Paola");
            CatERing.getInstance().getTurnManager().declareAvailability(serviceTurn);
            CatERing.getInstance().getUserManager().fakeLogin("Lidia");
            CatERing.getInstance().getTurnManager().assignTurn(serviceTurn, serviceStaff);
            workflow.assignServiceStaff(serviceId, serviceTurn.getId(), serviceStaff, "Dining room", organiser);

            workflow.close(eventId, "Completed successfully", List.of("archive/demo-report.pdf"), organiser);
            assertEquals("CLOSED", workflow.status(eventId));
        } finally {
            if (serviceTurn != null) new JdbcTurnRepository().delete(serviceTurn.getId());
            PersistenceManager.executeUpdate("DELETE FROM event_documents WHERE event_id = ?",
                    statement -> statement.setInt(1, eventId));
            PersistenceManager.executeUpdate(
                    "DELETE sms FROM service_menu_suggestions sms JOIN services s ON s.id = sms.service_id "
                            + "WHERE s.event_id = ?", statement -> statement.setInt(1, eventId));
            PersistenceManager.executeUpdate("DELETE FROM services WHERE event_id = ?",
                    statement -> statement.setInt(1, eventId));
            PersistenceManager.executeUpdate("DELETE FROM events WHERE id = ?",
                    statement -> statement.setInt(1, eventId));
        }
    }

    private static int serviceId(int eventId) throws Exception {
        try (Connection connection = PersistenceManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT id FROM services WHERE event_id = ?")) {
            statement.setInt(1, eventId);
            try (ResultSet row = statement.executeQuery()) {
                row.next();
                return row.getInt(1);
            }
        }
    }
}
