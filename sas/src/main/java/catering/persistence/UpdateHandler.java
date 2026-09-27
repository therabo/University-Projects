package catering.persistence;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Interface for handling updates to a database using {@code PreparedStatement}.
 * <p>
 * Implementations of this interface should define how updates are executed
 * by providing specific implementations of the {@code handleUpdate} method.
 * </p>
 */
public interface UpdateHandler {
    /**
     * Handles the update operation by configuring the provided {@code PreparedStatement}.
     *
     * @param ps The {@code PreparedStatement} to be used for executing the update.
     * @throws SQLException If an SQL error occurs while configuring the {@code PreparedStatement}.
     */
    void handleUpdate(PreparedStatement ps) throws SQLException;
}