package catering.persistence;

import java.sql.Connection;
import java.sql.SQLException;

public final class JdbcTransactions {
    @FunctionalInterface
    public interface Work<T> {
        T execute(Connection connection) throws SQLException;
    }

    private JdbcTransactions() {
    }

    public static <T> T execute(String operation, Work<T> work) {
        try (Connection connection = PersistenceManager.getConnection()) {
            connection.setAutoCommit(false);
            try {
                T result = work.execute(connection);
                connection.commit();
                return result;
            } catch (SQLException | RuntimeException failure) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackFailure) {
                    failure.addSuppressed(rollbackFailure);
                }
                throw failure;
            }
        } catch (SQLException failure) {
            throw new PersistenceException(operation, failure);
        }
    }
}
