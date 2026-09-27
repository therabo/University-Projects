package catering.persistence;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Handles batch updates and the processing of generated IDs.
 * <p>
 * This interface defines methods for handling individual items in a batch update and
 * processing generated IDs returned from the database.
 * </p>
 */
public interface BatchUpdateHandler {
    /**
     * Handles the preparation of a single batch item.
     * <p>
     * This method is called for each item in a batch update, allowing for the setting of
     * parameters on the {@link PreparedStatement} before adding it to the batch.
     * </p>
     *
     * @param ps         The {@link PreparedStatement} to be prepared for the batch update.
     * @param batchCount The index of the current batch item.
     * @throws SQLException If an SQL error occurs while handling the batch item.
     */
    public void handleBatchItem(PreparedStatement ps, int batchCount) throws SQLException;

    /**
     * Handles the processing of generated IDs from the batch update.
     * <p>
     * This method is called after the batch update has been executed to process any
     * generated keys returned by the database.
     * </p>
     *
     * @param rs    The {@link ResultSet} containing the generated keys.
     * @param count The index of the current generated key.
     * @throws SQLException If an SQL error occurs while handling the generated IDs.
     */
    public void handleGeneratedIds(ResultSet rs, int count) throws SQLException;
}
