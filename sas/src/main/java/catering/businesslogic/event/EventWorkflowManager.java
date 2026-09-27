package catering.businesslogic.event;

import catering.businesslogic.user.User;
import catering.persistence.JdbcTransactions;
import catering.persistence.PersistenceException;
import catering.persistence.PersistenceManager;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;

public final class EventWorkflowManager {
    public static final class ServiceDraft {
        private final String name;
        private final LocalDate date;
        private final java.time.LocalTime start;
        private final java.time.LocalTime end;
        private final int participants;
        private final String venue;

        public ServiceDraft(String name, LocalDate date, java.time.LocalTime start,
                            java.time.LocalTime end, int participants, String venue) {
            if (name == null || name.isBlank() || date == null || start == null || end == null
                    || start.equals(end) || participants <= 0 || venue == null || venue.isBlank()) {
                throw new IllegalArgumentException("Complete service details are required");
            }
            this.name = name.trim();
            this.date = date;
            this.start = start;
            this.end = end;
            this.participants = participants;
            this.venue = venue.trim();
        }
    }

    public int create(String name, LocalDate start, LocalDate end, int participants,
                      String venue, String client, String notes, String recurrenceKey,
                      List<ServiceDraft> services, User organiser) {
        requireOrganiser(organiser);
        if (name == null || name.isBlank() || start == null || end == null || end.isBefore(start)
                || participants <= 0 || venue == null || venue.isBlank()
                || client == null || client.isBlank() || services == null || services.isEmpty()) {
            throw new IllegalArgumentException("Complete event and service details are required");
        }
        for (ServiceDraft service : services) {
            if (service.date.isBefore(start) || service.date.isAfter(end)) {
                throw new IllegalArgumentException("Service date must be inside the event period");
            }
        }
        return JdbcTransactions.execute("Cannot create event", connection -> {
            int eventId;
            try (PreparedStatement insert = connection.prepareStatement(
                    "INSERT INTO events (name, date_start, date_end, expected_participants, organizer_id, "
                            + "venue, client_name, notes, status, recurrence_key) "
                            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, 'DRAFT', ?)", Statement.RETURN_GENERATED_KEYS)) {
                insert.setString(1, name.trim());
                insert.setDate(2, Date.valueOf(start));
                insert.setDate(3, Date.valueOf(end));
                insert.setInt(4, participants);
                insert.setInt(5, organiser.getId());
                insert.setString(6, venue.trim());
                insert.setString(7, client.trim());
                insert.setString(8, notes);
                insert.setString(9, recurrenceKey);
                insert.executeUpdate();
                try (ResultSet keys = insert.getGeneratedKeys()) {
                    if (!keys.next()) throw new SQLException("Event ID was not generated");
                    eventId = keys.getInt(1);
                }
            }
            try (PreparedStatement insert = connection.prepareStatement(
                    "INSERT INTO services (event_id, name, service_date, time_start, time_end, "
                            + "expected_participants, venue) VALUES (?, ?, ?, ?, ?, ?, ?)")) {
                for (ServiceDraft service : services) {
                    insert.setInt(1, eventId);
                    insert.setString(2, service.name);
                    insert.setDate(3, Date.valueOf(service.date));
                    insert.setTime(4, Time.valueOf(service.start));
                    insert.setTime(5, Time.valueOf(service.end));
                    insert.setInt(6, service.participants);
                    insert.setString(7, service.venue);
                    insert.addBatch();
                }
                insert.executeBatch();
            }
            return eventId;
        });
    }

    public void updateDraft(int eventId, String name, String venue, String client, String notes,
                            User organiser) {
        requireOrganiser(organiser);
        if (name == null || name.isBlank() || venue == null || venue.isBlank()
                || client == null || client.isBlank()) {
            throw new IllegalArgumentException("Name, venue and client are required");
        }
        JdbcTransactions.execute("Cannot update event", connection -> {
            lockOwnedEvent(connection, eventId, organiser.getId(), "DRAFT");
            try (PreparedStatement update = connection.prepareStatement(
                    "UPDATE events SET name = ?, venue = ?, client_name = ?, notes = ? WHERE id = ?")) {
                update.setString(1, name.trim());
                update.setString(2, venue.trim());
                update.setString(3, client.trim());
                update.setString(4, notes);
                update.setInt(5, eventId);
                update.executeUpdate();
            }
            return null;
        });
    }

    public void updateActiveDetails(int eventId, String venue, String client, String notes,
                                    User organiser) {
        requireOrganiser(organiser);
        if (venue == null || venue.isBlank() || client == null || client.isBlank()) {
            throw new IllegalArgumentException("Venue and client are required");
        }
        JdbcTransactions.execute("Cannot update active event", connection -> {
            lockOwnedEvent(connection, eventId, organiser.getId(), "IN_PROGRESS");
            try (PreparedStatement update = connection.prepareStatement(
                    "UPDATE events SET venue = ?, client_name = ?, notes = ? WHERE id = ?")) {
                update.setString(1, venue.trim());
                update.setString(2, client.trim());
                update.setString(3, notes);
                update.setInt(4, eventId);
                update.executeUpdate();
            }
            return null;
        });
    }

    public void assignChef(int serviceId, User chef, User organiser) {
        requireOrganiser(organiser);
        if (chef == null || !chef.isChef()) throw new IllegalArgumentException("Chef role is required");
        updateOwnedDraftService(serviceId, organiser, "chef_id", chef.getId());
    }

    public void proposeMenu(int serviceId, int menuId, User chef) {
        if (chef == null || !chef.isChef() || menuId <= 0) {
            throw new IllegalArgumentException("Chef and persisted menu are required");
        }
        JdbcTransactions.execute("Cannot propose menu", connection -> {
            try (PreparedStatement lock = connection.prepareStatement(
                    "SELECT s.chef_id, e.status FROM services s JOIN events e ON e.id = s.event_id "
                            + "WHERE s.id = ? FOR UPDATE")) {
                lock.setInt(1, serviceId);
                try (ResultSet row = lock.executeQuery()) {
                    if (!row.next() || row.getInt(1) != chef.getId() || !"DRAFT".equals(row.getString(2))) {
                        throw new IllegalArgumentException("Only the assigned chef may propose a menu");
                    }
                }
            }
            if (count(connection, "SELECT COUNT(*) FROM menus WHERE id = ? AND owner_id = ?", menuId,
                    chef.getId()) == 0) {
                throw new IllegalArgumentException("Chef can only propose an owned menu");
            }
            try (PreparedStatement update = connection.prepareStatement(
                    "UPDATE services SET proposed_menu_id = ? WHERE id = ?")) {
                update.setInt(1, menuId);
                update.setInt(2, serviceId);
                update.executeUpdate();
            }
            return null;
        });
    }

    public int suggestMenuChange(int serviceId, int recipeId, boolean add, User organiser) {
        requireOrganiser(organiser);
        return JdbcTransactions.execute("Cannot suggest menu change", connection -> {
            requireOwnedService(connection, serviceId, organiser.getId(), "DRAFT");
            try (PreparedStatement insert = connection.prepareStatement(
                    "INSERT INTO service_menu_suggestions (service_id, recipe_id, action) VALUES (?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS)) {
                insert.setInt(1, serviceId);
                insert.setInt(2, recipeId);
                insert.setString(3, add ? "ADD" : "REMOVE");
                insert.executeUpdate();
                try (ResultSet keys = insert.getGeneratedKeys()) {
                    if (!keys.next()) throw new SQLException("Suggestion ID was not generated");
                    return keys.getInt(1);
                }
            }
        });
    }

    public void decideSuggestion(int suggestionId, boolean accept, User chef) {
        if (chef == null || !chef.isChef()) throw new IllegalArgumentException("Chef role is required");
        JdbcTransactions.execute("Cannot decide menu suggestion", connection -> {
            try (PreparedStatement update = connection.prepareStatement(
                    "UPDATE service_menu_suggestions sms JOIN services s ON s.id = sms.service_id "
                            + "JOIN events e ON e.id = s.event_id SET sms.accepted = ? "
                            + "WHERE sms.id = ? AND s.chef_id = ? AND e.status = 'DRAFT' AND sms.accepted IS NULL")) {
                update.setBoolean(1, accept);
                update.setInt(2, suggestionId);
                update.setInt(3, chef.getId());
                if (update.executeUpdate() != 1) {
                    throw new IllegalArgumentException("Suggestion is not pending for this chef");
                }
            }
            return null;
        });
    }

    public void approveMenus(int eventId, User organiser) {
        requireOrganiser(organiser);
        JdbcTransactions.execute("Cannot approve event menus", connection -> {
            lockOwnedEvent(connection, eventId, organiser.getId(), "DRAFT");
            if (count(connection, "SELECT COUNT(*) FROM services WHERE event_id = ? AND "
                    + "(chef_id IS NULL OR proposed_menu_id = 0)", eventId) > 0) {
                throw new IllegalArgumentException("Every service needs a chef and proposed menu");
            }
            try (PreparedStatement services = connection.prepareStatement(
                    "UPDATE services SET approved_menu_id = proposed_menu_id WHERE event_id = ?")) {
                services.setInt(1, eventId);
                services.executeUpdate();
            }
            try (PreparedStatement event = connection.prepareStatement(
                    "UPDATE events SET status = 'IN_PROGRESS' WHERE id = ?")) {
                event.setInt(1, eventId);
                event.executeUpdate();
            }
            return null;
        });
    }

    public void assignServiceStaff(int serviceId, int turnId, User staff, String role, User organiser) {
        requireOrganiser(organiser);
        if (staff == null || !staff.isService() && !staff.isCook()
                || role == null || role.isBlank()) {
            throw new IllegalArgumentException("Service staff and role are required");
        }
        JdbcTransactions.execute("Cannot assign service staff", connection -> {
            requireOwnedService(connection, serviceId, organiser.getId(), "IN_PROGRESS");
            if (count(connection, "SELECT COUNT(*) FROM turn t JOIN turnstaff ts ON ts.turn_id = t.id "
                    + "WHERE t.id = ? AND t.service = ? AND t.kind = 'SERVICE' AND ts.user_id = ?",
                    turnId, serviceId, staff.getId()) != 1) {
                throw new IllegalArgumentException("Staff must already be assigned to the service turn");
            }
            try (PreparedStatement insert = connection.prepareStatement(
                    "INSERT INTO service_staff (service_id, turn_id, user_id, role) VALUES (?, ?, ?, ?) "
                            + "ON DUPLICATE KEY UPDATE role = VALUES(role)")) {
                insert.setInt(1, serviceId);
                insert.setInt(2, turnId);
                insert.setInt(3, staff.getId());
                insert.setString(4, role.trim());
                insert.executeUpdate();
            }
            return null;
        });
    }

    public void close(int eventId, String closingNotes, List<String> documentPaths, User organiser) {
        requireOrganiser(organiser);
        if (closingNotes == null || closingNotes.isBlank()) {
            throw new IllegalArgumentException("Closing notes are required");
        }
        JdbcTransactions.execute("Cannot close event", connection -> {
            lockOwnedEvent(connection, eventId, organiser.getId(), "IN_PROGRESS");
            try (PreparedStatement update = connection.prepareStatement(
                    "UPDATE events SET status = 'CLOSED', closing_notes = ? WHERE id = ?")) {
                update.setString(1, closingNotes.trim());
                update.setInt(2, eventId);
                update.executeUpdate();
            }
            if (documentPaths != null) {
                try (PreparedStatement insert = connection.prepareStatement(
                        "INSERT INTO event_documents (event_id, name, storage_path) VALUES (?, ?, ?)")) {
                    for (String path : documentPaths) {
                        if (path == null || path.isBlank()) continue;
                        String normalized = path.trim();
                        int separator = Math.max(normalized.lastIndexOf('/'), normalized.lastIndexOf('\\'));
                        insert.setInt(1, eventId);
                        insert.setString(2, separator >= 0 ? normalized.substring(separator + 1) : normalized);
                        insert.setString(3, normalized);
                        insert.addBatch();
                    }
                    insert.executeBatch();
                }
            }
            return null;
        });
    }

    public void cancel(int eventId, String reason, User organiser) {
        requireOrganiser(organiser);
        if (reason == null || reason.isBlank()) throw new IllegalArgumentException("Cancellation reason is required");
        JdbcTransactions.execute("Cannot cancel event", connection -> {
            try (PreparedStatement lock = connection.prepareStatement(
                    "SELECT organizer_id, status FROM events WHERE id = ? FOR UPDATE")) {
                lock.setInt(1, eventId);
                try (ResultSet row = lock.executeQuery()) {
                    if (!row.next() || row.getInt(1) != organiser.getId()
                            || !List.of("DRAFT", "IN_PROGRESS").contains(row.getString(2))) {
                        throw new IllegalArgumentException("Event cannot be cancelled by this organiser");
                    }
                }
            }
            try (PreparedStatement update = connection.prepareStatement(
                    "UPDATE events SET status = 'CANCELLED', closing_notes = ? WHERE id = ?")) {
                update.setString(1, reason.trim());
                update.setInt(2, eventId);
                update.executeUpdate();
            }
            return null;
        });
    }

    public List<Integer> previousApprovedMenuIds(String recurrenceKey, int excludingEventId) {
        if (recurrenceKey == null || recurrenceKey.isBlank()) return List.of();
        List<Integer> menus = new ArrayList<>();
        try (Connection connection = PersistenceManager.getConnection();
             PreparedStatement select = connection.prepareStatement(
                     "SELECT DISTINCT s.approved_menu_id FROM events e JOIN services s ON s.event_id = e.id "
                             + "WHERE e.recurrence_key = ? AND e.id <> ? AND s.approved_menu_id > 0 "
                             + "ORDER BY s.approved_menu_id")) {
            select.setString(1, recurrenceKey);
            select.setInt(2, excludingEventId);
            try (ResultSet rows = select.executeQuery()) {
                while (rows.next()) menus.add(rows.getInt(1));
            }
            return menus;
        } catch (SQLException failure) {
            throw new PersistenceException("Cannot read recurring event menu history", failure);
        }
    }

    public void deleteDraft(int eventId, User organiser) {
        requireOrganiser(organiser);
        JdbcTransactions.execute("Cannot delete event", connection -> {
            lockOwnedEvent(connection, eventId, organiser.getId(), "DRAFT");
            try (PreparedStatement deleteServices = connection.prepareStatement(
                    "DELETE FROM services WHERE event_id = ?")) {
                deleteServices.setInt(1, eventId);
                deleteServices.executeUpdate();
            }
            try (PreparedStatement deleteEvent = connection.prepareStatement("DELETE FROM events WHERE id = ?")) {
                deleteEvent.setInt(1, eventId);
                deleteEvent.executeUpdate();
            }
            return null;
        });
    }

    public String status(int eventId) {
        try (Connection connection = PersistenceManager.getConnection();
             PreparedStatement select = connection.prepareStatement("SELECT status FROM events WHERE id = ?")) {
            select.setInt(1, eventId);
            try (ResultSet row = select.executeQuery()) {
                return row.next() ? row.getString(1) : null;
            }
        } catch (SQLException failure) {
            throw new PersistenceException("Cannot read event status", failure);
        }
    }

    private static void updateOwnedDraftService(int serviceId, User organiser, String column, int value) {
        JdbcTransactions.execute("Cannot update service", connection -> {
            requireOwnedService(connection, serviceId, organiser.getId(), "DRAFT");
            try (PreparedStatement update = connection.prepareStatement(
                    "UPDATE services SET " + column + " = ? WHERE id = ?")) {
                update.setInt(1, value);
                update.setInt(2, serviceId);
                update.executeUpdate();
            }
            return null;
        });
    }

    private static void requireOwnedService(Connection connection, int serviceId, int organiserId,
                                            String status) throws SQLException {
        try (PreparedStatement select = connection.prepareStatement(
                "SELECT e.organizer_id, e.status FROM services s JOIN events e ON e.id = s.event_id "
                        + "WHERE s.id = ? FOR UPDATE")) {
            select.setInt(1, serviceId);
            try (ResultSet row = select.executeQuery()) {
                if (!row.next() || row.getInt(1) != organiserId || !status.equals(row.getString(2))) {
                    throw new IllegalArgumentException("Service is not editable by this organiser");
                }
            }
        }
    }

    private static void lockOwnedEvent(Connection connection, int eventId, int organiserId,
                                       String status) throws SQLException {
        try (PreparedStatement select = connection.prepareStatement(
                "SELECT organizer_id, status FROM events WHERE id = ? FOR UPDATE")) {
            select.setInt(1, eventId);
            try (ResultSet row = select.executeQuery()) {
                if (!row.next() || row.getInt(1) != organiserId || !status.equals(row.getString(2))) {
                    throw new IllegalArgumentException("Event is not editable in its current state");
                }
            }
        }
    }

    private static int count(Connection connection, String sql, int... parameters) throws SQLException {
        try (PreparedStatement select = connection.prepareStatement(sql)) {
            for (int index = 0; index < parameters.length; index++) select.setInt(index + 1, parameters[index]);
            try (ResultSet row = select.executeQuery()) {
                row.next();
                return row.getInt(1);
            }
        }
    }

    private static void requireOrganiser(User organiser) {
        if (organiser == null || !organiser.isOrganiser()) {
            throw new IllegalArgumentException("Organiser role is required");
        }
    }
}
