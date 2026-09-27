package catering.businesslogic.summarysheet;

import catering.businesslogic.user.User;
import catering.persistence.JdbcTransactions;
import catering.persistence.PersistenceException;
import catering.persistence.PersistenceManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class KitchenTaskManager {
    public static final class TaskProgress {
        private final int assignmentId;
        private final int cookId;
        private final String recipeName;
        private final LocalDateTime completedAt;
        private final String issueNote;

        private TaskProgress(int assignmentId, int cookId, String recipeName,
                             LocalDateTime completedAt, String issueNote) {
            this.assignmentId = assignmentId;
            this.cookId = cookId;
            this.recipeName = recipeName;
            this.completedAt = completedAt;
            this.issueNote = issueNote;
        }

        public int getAssignmentId() { return assignmentId; }
        public int getCookId() { return cookId; }
        public String getRecipeName() { return recipeName; }
        public LocalDateTime getCompletedAt() { return completedAt; }
        public String getIssueNote() { return issueNote; }
    }

    public void complete(int assignmentId, User actor) {
        updateProgress(assignmentId, actor, true, null);
    }

    public void reportIssue(int assignmentId, User actor, String issue) {
        if (issue == null || issue.isBlank()) throw new IllegalArgumentException("Issue text is required");
        updateProgress(assignmentId, actor, false, issue.trim());
    }

    public List<TaskProgress> forSheet(int sheetId, User actor) {
        if (sheetId <= 0 || actor == null || !actor.isChef() && !actor.isOrganiser()) {
            throw new IllegalArgumentException("A chef or organiser and persisted sheet are required");
        }
        String sql = "SELECT a.id, a.cook, r.name, a.completed_at, a.issue_note "
                + "FROM assignment a JOIN summarysheet s ON s.id = a.sheet "
                + "JOIN services sv ON sv.id = s.service_id JOIN events e ON e.id = sv.event_id "
                + "JOIN recipes r ON r.id = a.recipe WHERE s.id = ? "
                + "AND ((? = 1 AND s.owner = ?) OR (? = 1 AND e.organizer_id = ?)) ORDER BY a.id";
        List<TaskProgress> result = new ArrayList<>();
        try (Connection connection = PersistenceManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, sheetId);
            statement.setInt(2, actor.isChef() ? 1 : 0);
            statement.setInt(3, actor.getId());
            statement.setInt(4, actor.isOrganiser() ? 1 : 0);
            statement.setInt(5, actor.getId());
            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) {
                    java.sql.Timestamp completed = rows.getTimestamp(4);
                    result.add(new TaskProgress(rows.getInt(1), rows.getInt(2), rows.getString(3),
                            completed == null ? null : completed.toLocalDateTime(), rows.getString(5)));
                }
            }
        } catch (SQLException failure) {
            throw new PersistenceException("Cannot read kitchen task progress", failure);
        }
        return result;
    }

    private void updateProgress(int assignmentId, User actor, boolean complete, String issue) {
        if (assignmentId <= 0 || actor == null || !actor.isCook()) {
            throw new IllegalArgumentException("An assigned cook and persisted task are required");
        }
        JdbcTransactions.execute("Cannot update kitchen task", connection -> {
            try (PreparedStatement lock = connection.prepareStatement(
                    "SELECT cook, completed_at FROM assignment WHERE id = ? FOR UPDATE")) {
                lock.setInt(1, assignmentId);
                try (ResultSet row = lock.executeQuery()) {
                    if (!row.next() || row.getInt(1) != actor.getId()) {
                        throw new IllegalArgumentException("Task is not assigned to this cook");
                    }
                    if (row.getTimestamp(2) != null) {
                        throw new IllegalArgumentException("Completed task cannot be modified");
                    }
                }
            }
            String sql = complete
                    ? "UPDATE assignment SET completed_at = CURRENT_TIMESTAMP WHERE id = ?"
                    : "UPDATE assignment SET issue_note = ? WHERE id = ?";
            try (PreparedStatement update = connection.prepareStatement(sql)) {
                if (complete) update.setInt(1, assignmentId);
                else {
                    update.setString(1, Objects.requireNonNull(issue));
                    update.setInt(2, assignmentId);
                }
                update.executeUpdate();
            }
            return null;
        });
    }
}
