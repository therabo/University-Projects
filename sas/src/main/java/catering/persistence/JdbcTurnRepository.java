package catering.persistence;

import catering.businesslogic.turn.Turn;
import catering.businesslogic.turn.TurnRepository;
import catering.businesslogic.event.ServiceInfo;
import catering.businesslogic.user.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class JdbcTurnRepository implements TurnRepository {
    @Override
    public Turn save(Turn turn) {
        return JdbcTransactions.execute("Could not save turn", connection -> insert(connection, turn));
    }

    private Turn insert(Connection connection, Turn turn) throws SQLException {
        if (turn.getKind() == Turn.Kind.KITCHEN) {
            lockKitchen(connection, turn.getLocation());
            ensureKitchenDoesNotOverlap(connection, turn, 0);
        }
        String sql = "INSERT INTO turn (service, date, start, end, location, kind, owner_id, "
                + "availability_deadline, group_id, series_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            if (turn.getService() == null) statement.setNull(1, java.sql.Types.INTEGER);
            else statement.setInt(1, turn.getService().getId());
            statement.setDate(2, new java.sql.Date(turn.getDate().getTime()));
            statement.setTime(3, turn.getStart());
            statement.setTime(4, turn.getEnd());
            statement.setString(5, turn.getLocation());
            statement.setString(6, turn.getKind().name());
            if (turn.getOwnerId() <= 0) statement.setNull(7, java.sql.Types.INTEGER);
            else statement.setInt(7, turn.getOwnerId());
            if (turn.getAvailabilityDeadline() == null) statement.setNull(8, java.sql.Types.TIMESTAMP);
            else statement.setTimestamp(8, Timestamp.valueOf(turn.getAvailabilityDeadline()));
            if (turn.getGroupId() <= 0) statement.setNull(9, java.sql.Types.INTEGER);
            else statement.setInt(9, turn.getGroupId());
            if (turn.getSeriesId() <= 0) statement.setNull(10, java.sql.Types.INTEGER);
            else statement.setInt(10, turn.getSeriesId());
            if (statement.executeUpdate() != 1) throw new SQLException("Turn insert did not affect one row");
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) throw new SQLException("Turn insert returned no generated ID");
                turn.setId(keys.getInt(1));
            }
            return turn;
        }
    }

    private void lockKitchen(Connection connection, String location) throws SQLException {
        try (PreparedStatement insert = connection.prepareStatement(
                "INSERT IGNORE INTO kitchen_locations (name) VALUES (?)")) {
            insert.setString(1, location);
            insert.executeUpdate();
        }
        try (PreparedStatement lock = connection.prepareStatement(
                "SELECT name FROM kitchen_locations WHERE name = ? FOR UPDATE")) {
            lock.setString(1, location);
            try (ResultSet rows = lock.executeQuery()) {
                if (!rows.next()) throw new SQLException("Kitchen location could not be locked");
            }
        }
    }

    private void ensureKitchenDoesNotOverlap(Connection connection, Turn turn, int excludedId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT id, date, start, end FROM turn WHERE kind = 'KITCHEN' AND location = ? AND id <> ? "
                        + "AND date BETWEEN DATE_SUB(?, INTERVAL 1 DAY) AND DATE_ADD(?, INTERVAL 1 DAY)")) {
            statement.setString(1, turn.getLocation());
            statement.setInt(2, excludedId);
            statement.setDate(3, new java.sql.Date(turn.getDate().getTime()));
            statement.setDate(4, new java.sql.Date(turn.getDate().getTime()));
            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) {
                    Turn other = new Turn();
                    other.setDate(rows.getDate("date"));
                    other.setStart(rows.getTime("start"));
                    other.setEnd(rows.getTime("end"));
                    if (other.overlaps(turn)) {
                        throw new IllegalArgumentException("Kitchen turns cannot overlap at the same location");
                    }
                }
            }
        }
    }

    @Override
    public Turn findById(int id) {
        List<Turn> result = find("SELECT turn.*, turnstaff.user_id AS staff_user_id FROM turn "
                + "LEFT JOIN turnstaff ON turnstaff.turn_id = turn.id WHERE turn.id = ?", id);
        return result.isEmpty() ? null : result.get(0);
    }

    @Override
    public List<Turn> findAll() {
        return find("SELECT turn.*, turnstaff.user_id AS staff_user_id FROM turn "
                + "LEFT JOIN turnstaff ON turnstaff.turn_id = turn.id ORDER BY turn.date, turn.start, turn.id", null);
    }

    @Override
    public List<Turn> findByService(int serviceId) {
        return find("SELECT turn.*, turnstaff.user_id AS staff_user_id FROM turn "
                + "LEFT JOIN turnstaff ON turnstaff.turn_id = turn.id "
                + "WHERE turn.service = ? ORDER BY turn.date, turn.start, turn.id", serviceId);
    }

    @Override
    public List<Turn> findAssignedToUser(int userId) {
        return find("SELECT turn.*, turnstaff.user_id AS staff_user_id FROM turn "
                + "LEFT JOIN turnstaff ON turnstaff.turn_id = turn.id "
                + "WHERE EXISTS (SELECT 1 FROM turnstaff AS selected WHERE selected.turn_id = turn.id "
                + "AND selected.user_id = ?) ORDER BY turn.date, turn.start, turn.id", userId);
    }

    @Override
    public List<Turn> findBySeries(int seriesId) {
        return find("SELECT turn.*, turnstaff.user_id AS staff_user_id FROM turn "
                + "LEFT JOIN turnstaff ON turnstaff.turn_id = turn.id "
                + "WHERE turn.series_id = ? ORDER BY turn.date, turn.start, turn.id", seriesId);
    }

    @Override
    public List<Turn> findByGroup(int groupId) {
        return find("SELECT turn.*, turnstaff.user_id AS staff_user_id FROM turn "
                + "LEFT JOIN turnstaff ON turnstaff.turn_id = turn.id "
                + "WHERE turn.group_id = ? ORDER BY turn.date, turn.start, turn.id", groupId);
    }

    private List<Turn> find(String sql, Integer id) {
        Map<Integer, Turn> result = new LinkedHashMap<>();
        try (Connection connection = PersistenceManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            if (id != null) {
                statement.setInt(1, id);
            }
            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) {
                    int turnId = rows.getInt("id");
                    Turn turn = result.get(turnId);
                    if (turn == null) {
                        turn = new Turn();
                        turn.setId(turnId);
                        turn.setDate(rows.getDate("date"));
                        turn.setStart(rows.getTime("start"));
                        turn.setEnd(rows.getTime("end"));
                        turn.setLocation(rows.getString("location"));
                        int serviceId = rows.getInt("service");
                        if (serviceId > 0) turn.setService(ServiceInfo.loadServiceInfoById(serviceId));
                        turn.setKind(Turn.Kind.valueOf(rows.getString("kind")));
                        turn.setOwnerId(rows.getInt("owner_id"));
                        turn.setGroupId(rows.getInt("group_id"));
                        turn.setSeriesId(rows.getInt("series_id"));
                        Timestamp deadline = rows.getTimestamp("availability_deadline");
                        if (deadline != null) turn.setAvailabilityDeadline(deadline.toLocalDateTime());
                        result.put(turnId, turn);
                    }
                    int staffId = rows.getInt("staff_user_id");
                    if (staffId > 0) {
                        turn.assign(User.loadUserById(staffId));
                    }
                }
            }
            return new ArrayList<>(result.values());
        } catch (SQLException ex) {
            throw new PersistenceException("Could not load turns", ex);
        }
    }

    @Override
    public List<Turn> saveSeries(List<Turn> turns, String frequency, int ownerId) {
        if (turns == null || turns.isEmpty()) throw new IllegalArgumentException("Recurring turns are required");
        return JdbcTransactions.execute("Could not save recurring turns", connection -> {
            int seriesId;
            try (PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO turn_series (frequency, created_by) VALUES (?, ?)", Statement.RETURN_GENERATED_KEYS)) {
                statement.setString(1, frequency);
                statement.setInt(2, ownerId);
                statement.executeUpdate();
                try (ResultSet keys = statement.getGeneratedKeys()) {
                    if (!keys.next()) throw new SQLException("Series insert returned no ID");
                    seriesId = keys.getInt(1);
                }
            }
            for (Turn turn : turns) {
                turn.setSeriesId(seriesId);
                turn.setOwnerId(ownerId);
                insert(connection, turn);
            }
            return turns;
        });
    }

    @Override
    public int createGroup(List<Integer> turnIds) {
        if (turnIds == null || turnIds.size() < 2 || turnIds.stream().distinct().count() != turnIds.size()) {
            throw new IllegalArgumentException("A group requires at least two distinct turns");
        }
        return JdbcTransactions.execute("Could not create turn group", connection -> {
            for (int id : turnIds.stream().mapToInt(Integer::intValue).sorted().toArray()) {
                try (PreparedStatement lock = connection.prepareStatement(
                        "SELECT group_id FROM turn WHERE id = ? FOR UPDATE")) {
                    lock.setInt(1, id);
                    try (ResultSet row = lock.executeQuery()) {
                        if (!row.next() || row.getInt(1) > 0 || hasAvailability(connection, id)) {
                            throw new IllegalArgumentException("Turns must exist, be ungrouped and have no availability");
                        }
                    }
                }
            }
            int groupId;
            try (PreparedStatement insert = connection.prepareStatement(
                    "INSERT INTO turn_groups (series_id) VALUES (NULL)", Statement.RETURN_GENERATED_KEYS)) {
                insert.executeUpdate();
                try (ResultSet keys = insert.getGeneratedKeys()) {
                    if (!keys.next()) throw new SQLException("Group insert returned no ID");
                    groupId = keys.getInt(1);
                }
            }
            try (PreparedStatement update = connection.prepareStatement("UPDATE turn SET group_id = ? WHERE id = ?")) {
                for (int id : turnIds) {
                    update.setInt(1, groupId);
                    update.setInt(2, id);
                    update.addBatch();
                }
                update.executeBatch();
            }
            return groupId;
        });
    }

    @Override
    public List<Integer> createGroupSeries(List<List<Integer>> groups, int ownerId) {
        if (groups == null || groups.size() < 2 || ownerId <= 0
                || groups.stream().anyMatch(group -> group == null || group.size() < 2)
                || groups.stream().flatMap(List::stream).distinct().count()
                != groups.stream().mapToLong(List::size).sum()) {
            throw new IllegalArgumentException("Recurring groups need distinct turns in at least two groups");
        }
        return JdbcTransactions.execute("Could not create recurring turn groups", connection -> {
            int[] allIds = groups.stream().flatMap(List::stream).mapToInt(Integer::intValue).sorted().toArray();
            for (int id : allIds) {
                try (PreparedStatement lock = connection.prepareStatement(
                        "SELECT group_id FROM turn WHERE id = ? FOR UPDATE")) {
                    lock.setInt(1, id);
                    try (ResultSet row = lock.executeQuery()) {
                        if (!row.next() || row.getInt(1) > 0 || hasAvailability(connection, id)) {
                            throw new IllegalArgumentException("Turns must exist, be ungrouped and have no availability");
                        }
                    }
                }
            }
            int seriesId;
            try (PreparedStatement insert = connection.prepareStatement(
                    "INSERT INTO turn_series (frequency, created_by) VALUES ('GROUP', ?)",
                    Statement.RETURN_GENERATED_KEYS)) {
                insert.setInt(1, ownerId);
                insert.executeUpdate();
                try (ResultSet keys = insert.getGeneratedKeys()) {
                    if (!keys.next()) throw new SQLException("Group series ID was not generated");
                    seriesId = keys.getInt(1);
                }
            }
            List<Integer> groupIds = new ArrayList<>();
            try (PreparedStatement insert = connection.prepareStatement(
                    "INSERT INTO turn_groups (series_id) VALUES (?)", Statement.RETURN_GENERATED_KEYS);
                 PreparedStatement update = connection.prepareStatement(
                         "UPDATE turn SET group_id = ? WHERE id = ?")) {
                for (List<Integer> group : groups) {
                    insert.setInt(1, seriesId);
                    insert.executeUpdate();
                    int groupId;
                    try (ResultSet keys = insert.getGeneratedKeys()) {
                        if (!keys.next()) throw new SQLException("Group ID was not generated");
                        groupId = keys.getInt(1);
                    }
                    groupIds.add(groupId);
                    for (int id : group) {
                        update.setInt(1, groupId);
                        update.setInt(2, id);
                        update.addBatch();
                    }
                }
                update.executeBatch();
            }
            return groupIds;
        });
    }

    @Override
    public void dissolveGroup(int groupId) {
        JdbcTransactions.execute("Could not dissolve turn group", connection -> {
            try (PreparedStatement lock = connection.prepareStatement(
                    "SELECT id FROM turn WHERE group_id = ? FOR UPDATE")) {
                lock.setInt(1, groupId);
                try (ResultSet rows = lock.executeQuery()) {
                    while (rows.next()) {
                        if (hasAvailability(connection, rows.getInt(1))) {
                            throw new IllegalArgumentException("A group with declared availability is locked");
                        }
                    }
                }
            }
            try (PreparedStatement update = connection.prepareStatement("UPDATE turn SET group_id = NULL WHERE group_id = ?")) {
                update.setInt(1, groupId);
                update.executeUpdate();
            }
            try (PreparedStatement delete = connection.prepareStatement("DELETE FROM turn_groups WHERE id = ?")) {
                delete.setInt(1, groupId);
                delete.executeUpdate();
            }
            return null;
        });
    }

    @Override
    public void dissolveGroupSeries(int seriesId) {
        JdbcTransactions.execute("Could not dissolve recurring turn groups", connection -> {
            try (PreparedStatement lock = connection.prepareStatement(
                    "SELECT t.id FROM turn t JOIN turn_groups g ON g.id = t.group_id "
                            + "WHERE g.series_id = ? ORDER BY t.id FOR UPDATE")) {
                lock.setInt(1, seriesId);
                boolean found = false;
                try (ResultSet rows = lock.executeQuery()) {
                    while (rows.next()) {
                        found = true;
                        if (hasAvailability(connection, rows.getInt(1))) {
                            throw new IllegalArgumentException("Recurring groups with availability are locked");
                        }
                    }
                }
                if (!found) throw new IllegalArgumentException("Recurring group series not found");
            }
            try (PreparedStatement update = connection.prepareStatement(
                    "UPDATE turn t JOIN turn_groups g ON g.id = t.group_id SET t.group_id = NULL "
                            + "WHERE g.series_id = ?")) {
                update.setInt(1, seriesId);
                update.executeUpdate();
            }
            try (PreparedStatement deleteGroups = connection.prepareStatement(
                    "DELETE FROM turn_groups WHERE series_id = ?")) {
                deleteGroups.setInt(1, seriesId);
                deleteGroups.executeUpdate();
            }
            try (PreparedStatement deleteSeries = connection.prepareStatement(
                    "DELETE FROM turn_series WHERE id = ? AND frequency = 'GROUP'")) {
                deleteSeries.setInt(1, seriesId);
                deleteSeries.executeUpdate();
            }
            return null;
        });
    }

    @Override
    public int findGroupSeriesId(int groupId) {
        try (Connection connection = PersistenceManager.getConnection();
             PreparedStatement select = connection.prepareStatement(
                     "SELECT series_id FROM turn_groups WHERE id = ?")) {
            select.setInt(1, groupId);
            try (ResultSet row = select.executeQuery()) {
                return row.next() ? row.getInt(1) : 0;
            }
        } catch (SQLException failure) {
            throw new PersistenceException("Could not load recurring group series", failure);
        }
    }

    @Override
    public void updateSchedule(Turn turn) {
        JdbcTransactions.execute("Could not update turn", connection -> {
            try (PreparedStatement lock = connection.prepareStatement("SELECT id FROM turn WHERE id = ? FOR UPDATE")) {
                lock.setInt(1, turn.getId());
                try (ResultSet row = lock.executeQuery()) {
                    if (!row.next()) throw new IllegalArgumentException("Turn not found");
                }
            }
            if (hasAvailability(connection, turn.getId())) {
                throw new IllegalArgumentException("Turn with declared availability is locked");
            }
            if (turn.getKind() == Turn.Kind.KITCHEN) {
                lockKitchen(connection, turn.getLocation());
                ensureKitchenDoesNotOverlap(connection, turn, turn.getId());
            }
            try (PreparedStatement statement = connection.prepareStatement(
                    "UPDATE turn SET date = ?, start = ?, end = ?, location = ?, availability_deadline = ? WHERE id = ?")) {
                statement.setDate(1, new java.sql.Date(turn.getDate().getTime()));
                statement.setTime(2, turn.getStart());
                statement.setTime(3, turn.getEnd());
                statement.setString(4, turn.getLocation());
                if (turn.getAvailabilityDeadline() == null) statement.setNull(5, java.sql.Types.TIMESTAMP);
                else statement.setTimestamp(5, Timestamp.valueOf(turn.getAvailabilityDeadline()));
                statement.setInt(6, turn.getId());
                statement.executeUpdate();
            }
            return null;
        });
    }

    @Override
    public void updateSchedules(List<Turn> turns) {
        if (turns == null || turns.isEmpty()
                || turns.stream().map(Turn::getId).distinct().count() != turns.size()) {
            throw new IllegalArgumentException("Distinct persisted turns are required");
        }
        JdbcTransactions.execute("Could not update turns", connection -> {
            List<Turn> ordered = new ArrayList<>(turns);
            ordered.sort(java.util.Comparator.comparingInt(Turn::getId));
            for (Turn turn : ordered) {
                try (PreparedStatement lock = connection.prepareStatement(
                        "SELECT id FROM turn WHERE id = ? FOR UPDATE")) {
                    lock.setInt(1, turn.getId());
                    try (ResultSet row = lock.executeQuery()) {
                        if (!row.next() || hasAvailability(connection, turn.getId())) {
                            throw new IllegalArgumentException("All turns must exist and have no availability");
                        }
                    }
                }
                if (turn.getKind() == Turn.Kind.KITCHEN) lockKitchen(connection, turn.getLocation());
            }
            try (PreparedStatement update = connection.prepareStatement(
                    "UPDATE turn SET date = ?, start = ?, end = ?, location = ?, availability_deadline = ? WHERE id = ?")) {
                for (Turn turn : ordered) {
                    update.setDate(1, new java.sql.Date(turn.getDate().getTime()));
                    update.setTime(2, turn.getStart());
                    update.setTime(3, turn.getEnd());
                    update.setString(4, turn.getLocation());
                    if (turn.getAvailabilityDeadline() == null) update.setNull(5, java.sql.Types.TIMESTAMP);
                    else update.setTimestamp(5, Timestamp.valueOf(turn.getAvailabilityDeadline()));
                    update.setInt(6, turn.getId());
                    update.addBatch();
                }
                update.executeBatch();
            }
            for (Turn turn : ordered) {
                if (turn.getKind() == Turn.Kind.KITCHEN) {
                    ensureKitchenDoesNotOverlap(connection, turn, turn.getId());
                }
            }
            return null;
        });
    }

    @Override
    public void setAvailability(int turnId, int userId, boolean available) {
        JdbcTransactions.execute("Could not update availability", connection -> {
            try (PreparedStatement userLock = connection.prepareStatement("SELECT id FROM users WHERE id = ? FOR UPDATE")) {
                userLock.setInt(1, userId);
                try (ResultSet row = userLock.executeQuery()) {
                    if (!row.next()) throw new IllegalArgumentException("User not found");
                }
            }
            int groupId;
            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT group_id FROM turn WHERE id = ? FOR UPDATE")) {
                statement.setInt(1, turnId);
                try (ResultSet row = statement.executeQuery()) {
                    if (!row.next()) throw new IllegalArgumentException("Turn not found");
                    groupId = row.getInt(1);
                }
            }
            List<Turn> members = new ArrayList<>();
            String sql = groupId > 0
                    ? "SELECT id, date, start, end, availability_deadline FROM turn WHERE group_id = ? ORDER BY id FOR UPDATE"
                    : "SELECT id, date, start, end, availability_deadline FROM turn WHERE id = ? FOR UPDATE";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, groupId > 0 ? groupId : turnId);
                try (ResultSet rows = statement.executeQuery()) {
                    while (rows.next()) {
                        Turn member = new Turn();
                        member.setId(rows.getInt("id"));
                        member.setDate(rows.getDate("date"));
                        member.setStart(rows.getTime("start"));
                        member.setEnd(rows.getTime("end"));
                        Timestamp deadline = rows.getTimestamp("availability_deadline");
                        if (deadline != null) member.setAvailabilityDeadline(deadline.toLocalDateTime());
                        members.add(member);
                    }
                }
            }
            java.time.LocalDateTime now = java.time.LocalDateTime.now();
            for (Turn member : members) {
                if (!member.startsAt().isAfter(now)
                        || member.getAvailabilityDeadline() != null
                        && !member.getAvailabilityDeadline().isAfter(now)) {
                    throw new IllegalArgumentException("Availability period is closed");
                }
                if (!available && isAssigned(connection, member.getId(), userId)) {
                    throw new IllegalArgumentException("Assigned staff cannot withdraw availability");
                }
            }
            String change = available
                    ? "INSERT IGNORE INTO turn_availability (turn_id, user_id) VALUES (?, ?)"
                    : "DELETE FROM turn_availability WHERE turn_id = ? AND user_id = ?";
            try (PreparedStatement statement = connection.prepareStatement(change)) {
                for (Turn member : members) {
                    statement.setInt(1, member.getId());
                    statement.setInt(2, userId);
                    statement.addBatch();
                }
                statement.executeBatch();
            }
            return null;
        });
    }

    @Override
    public boolean hasAvailability(int turnId) {
        try (Connection connection = PersistenceManager.getConnection()) {
            return hasAvailability(connection, turnId);
        } catch (SQLException ex) {
            throw new PersistenceException("Could not check availability", ex);
        }
    }

    private boolean hasAvailability(Connection connection, int turnId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT 1 FROM turn_availability WHERE turn_id = ? LIMIT 1")) {
            statement.setInt(1, turnId);
            try (ResultSet rows = statement.executeQuery()) {
                return rows.next();
            }
        }
    }

    @Override
    public boolean isAvailable(int turnId, int userId) {
        try (Connection connection = PersistenceManager.getConnection()) {
            return isAvailable(connection, turnId, userId);
        } catch (SQLException ex) {
            throw new PersistenceException("Could not check staff availability", ex);
        }
    }

    private boolean isAvailable(Connection connection, int turnId, int userId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT 1 FROM turn_availability WHERE turn_id = ? AND user_id = ?")) {
            statement.setInt(1, turnId);
            statement.setInt(2, userId);
            try (ResultSet rows = statement.executeQuery()) {
                return rows.next();
            }
        }
    }

    private boolean isAssigned(Connection connection, int turnId, int userId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT 1 FROM turnstaff WHERE turn_id = ? AND user_id = ?")) {
            statement.setInt(1, turnId);
            statement.setInt(2, userId);
            try (ResultSet rows = statement.executeQuery()) {
                return rows.next();
            }
        }
    }

    @Override
    public boolean delete(int id) {
        try (Connection connection = PersistenceManager.getConnection();
             PreparedStatement statement = connection.prepareStatement("DELETE FROM turn WHERE id = ?")) {
            statement.setInt(1, id);
            return statement.executeUpdate() == 1;
        } catch (SQLException ex) {
            throw new PersistenceException("Could not delete turn", ex);
        }
    }

    @Override
    public int deleteAll(List<Integer> ids) {
        if (ids == null || ids.isEmpty() || ids.stream().distinct().count() != ids.size()) {
            throw new IllegalArgumentException("Distinct turn IDs are required");
        }
        return JdbcTransactions.execute("Could not delete turns", connection -> {
            List<Integer> ordered = new ArrayList<>(ids);
            ordered.sort(Integer::compareTo);
            for (int id : ordered) {
                try (PreparedStatement lock = connection.prepareStatement(
                        "SELECT id FROM turn WHERE id = ? FOR UPDATE")) {
                    lock.setInt(1, id);
                    try (ResultSet row = lock.executeQuery()) {
                        if (!row.next() || hasAvailability(connection, id)) {
                            throw new IllegalArgumentException("All turns must exist and have no availability");
                        }
                    }
                }
            }
            try (PreparedStatement delete = connection.prepareStatement("DELETE FROM turn WHERE id = ?")) {
                for (int id : ordered) {
                    delete.setInt(1, id);
                    delete.addBatch();
                }
                int total = 0;
                for (int count : delete.executeBatch()) if (count > 0) total += count;
                return total;
            }
        });
    }

    @Override
    public void updateLocation(Turn turn) {
        updateSchedule(turn);
    }

    @Override
    public void assign(Turn turn, User user) {
        JdbcTransactions.execute("Could not assign user to turn", connection -> {
            try (PreparedStatement lock = connection.prepareStatement("SELECT id FROM users WHERE id = ? FOR UPDATE")) {
                lock.setInt(1, user.getId());
                try (ResultSet row = lock.executeQuery()) {
                    if (!row.next()) throw new IllegalArgumentException("User not found");
                }
            }
            Turn current = null;
            try (PreparedStatement lock = connection.prepareStatement(
                    "SELECT date, start, end FROM turn WHERE id = ? FOR UPDATE")) {
                lock.setInt(1, turn.getId());
                try (ResultSet row = lock.executeQuery()) {
                    if (row.next()) {
                        current = new Turn();
                        current.setDate(row.getDate("date"));
                        current.setStart(row.getTime("start"));
                        current.setEnd(row.getTime("end"));
                    }
                }
            }
            if (current == null || !isAvailable(connection, turn.getId(), user.getId())) {
                throw new IllegalArgumentException("Staff must declare availability before assignment");
            }
            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT turn.id, turn.date, turn.start, turn.end FROM turn "
                            + "JOIN turnstaff ON turnstaff.turn_id = turn.id "
                            + "WHERE turnstaff.user_id = ? AND turn.id <> ? FOR UPDATE")) {
                statement.setInt(1, user.getId());
                statement.setInt(2, turn.getId());
                try (ResultSet rows = statement.executeQuery()) {
                    while (rows.next()) {
                        Turn assigned = new Turn();
                        assigned.setDate(rows.getDate("date"));
                        assigned.setStart(rows.getTime("start"));
                        assigned.setEnd(rows.getTime("end"));
                        if (assigned.overlaps(current)) {
                            throw new IllegalArgumentException("User is already assigned to an overlapping turn");
                        }
                    }
                }
            }
            try (PreparedStatement statement = connection.prepareStatement(
                    "INSERT IGNORE INTO turnstaff (turn_id, user_id) VALUES (?, ?)")) {
                statement.setInt(1, turn.getId());
                statement.setInt(2, user.getId());
                statement.executeUpdate();
            }
            return null;
        });
    }
}
