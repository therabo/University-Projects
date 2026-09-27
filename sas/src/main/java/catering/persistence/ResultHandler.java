package catering.persistence;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Interface for handling {@link ResultSet} results from database queries.
 * <p>
 * Implementations of this interface are used to process the results of SQL queries executed against the
 * database. The {@link #handle(ResultSet)} method is called with a {@link ResultSet} object that contains
 * the data returned by the query.
 * </p>
 */
public interface ResultHandler {
    /**
     * Processes the given {@link ResultSet} from a database query.
     * <p>
     * Implementations of this method should extract data from the provided {@link ResultSet} and perform
     * necessary actions, such as populating objects or accumulating results.
     * </p>
     *
     * @param rs The {@link ResultSet} containing the data returned by the query.
     * @throws SQLException If an SQL error occurs while processing the {@link ResultSet}.
     */
    public void handle(ResultSet rs) throws SQLException;
}
