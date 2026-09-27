package catering.persistence;

import java.sql.*;

/**
 * Manages database operations such as executing queries, updates, and batch updates.
 * <p>
 * Provides methods for handling SQL operations, including query execution, batch updates,
 * and escaping strings for SQL safety. It also manages database connections and handles
 * SQL exceptions.
 * </p>
 */
public class PersistenceManager {
    public static Connection getConnection() throws SQLException {
        String url = System.getenv().getOrDefault("CATERING_DB_URL", "jdbc:mysql://localhost:3306/catering?serverTimezone=UTC");
        String username = System.getenv().getOrDefault("CATERING_DB_USER", "root");
        String password = System.getenv().getOrDefault("CATERING_DB_PASSWORD", "");
        return DriverManager.getConnection(url, username, password);
    }

    /**
     * Tests the SQL connection by querying the 'users' table and printing the results.
     * <p>
     * This method attempts to connect to the database and retrieve user information for
     * verification purposes.
     * </p>
     */
    public static void testSQLConnection() {
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM users");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("username");
                System.out.println(name + " ha id = " + id);
            }
        } catch (SQLException ex) {
            throw new PersistenceException("Database connection test failed", ex);
        }
    }

    /**
     * Executes a SQL query and processes the results using a {@link ResultHandler}.
     * <p>
     * The query results are handled by the provided {@link ResultHandler}, which processes
     * each row of the {@link ResultSet}.
     * </p>
     *
     * @param query   The SQL query to execute.
     * @param handler The handler to process the query results.
     */
    public static void executeQuery(String query, ResultHandler handler) {
        executeQuery(query, ps -> {}, handler);
    }

    public static void executeQuery(String query, UpdateHandler parameters, ResultHandler handler) {
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            parameters.handleUpdate(ps);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    handler.handle(rs);
                }
            }
        } catch (SQLException ex) {
            throw new PersistenceException("Query failed: " + query, ex);
        }

    }

    /**
     * Executes a batch update with parameterized queries and handles generated keys.
     * <p>
     * Executes multiple SQL statements in a batch and processes the generated keys using
     * a {@link BatchUpdateHandler}.
     * </p>
     *
     * @param parametrizedQuery The SQL query with parameters.
     * @param itemNumber        The number of items to be processed.
     * @param handler           The handler to process each batch item and generated keys.
     * @return An array of update counts for each command in the batch.
     */
    public static int[] executeBatchUpdate(String parametrizedQuery, int itemNumber, BatchUpdateHandler handler) {
        int[] result = new int[0];
        try (
                Connection conn = getConnection();
                PreparedStatement ps = conn.prepareStatement(parametrizedQuery, Statement.RETURN_GENERATED_KEYS);
        ) {
            for (int i = 0; i < itemNumber; i++) {
                handler.handleBatchItem(ps, i);
                ps.addBatch();
            }
            result = ps.executeBatch();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                int count = 0;
                while (keys.next()) {
                    handler.handleGeneratedIds(keys, count);
                    count++;
                }
            }

        } catch (SQLException ex) {
            throw new PersistenceException("Batch update failed: " + parametrizedQuery, ex);
        }

        return result;
    }

    /**
     * Executes a SQL update statement.
     *
     * @param update The SQL update statement to execute.
     * @return The number of rows affected by the update.
     */
    public static int executeUpdate(String update) {
        int result = 0;
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(update)) {
            result = ps.executeUpdate();
        } catch (SQLException ex) {
            throw new PersistenceException("Update failed: " + update, ex);
        }
        return result;
    }

    /**
     * Executes an update statement with a custom {@link UpdateHandler}.
     * <p>
     * Executes a SQL update statement and processes it using the provided {@link UpdateHandler}.
     * </p>
     *
     * @param query   The SQL update statement to execute.
     * @param handler The handler to process the update statement.
     * @return The number of rows affected by the update.
     */
    public static int executeUpdate(String query, UpdateHandler handler) {
        int result = 0;
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            handler.handleUpdate(ps);
            result = ps.executeUpdate();
        } catch (SQLException ex) {
            throw new PersistenceException("Update failed: " + query, ex);
        }
        return result;
    }

}
