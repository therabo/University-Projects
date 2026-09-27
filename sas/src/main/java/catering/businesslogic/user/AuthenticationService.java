package catering.businesslogic.user;

import catering.persistence.JdbcTransactions;
import catering.persistence.PersistenceException;
import catering.persistence.PersistenceManager;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

public final class AuthenticationService {
    public static final class Session {
        private final User user;

        private Session(User user) {
            this.user = user;
        }

        public User getUser() {
            return user;
        }
    }

    private static final int ITERATIONS = 120000;
    private static final int KEY_BITS = 256;
    private final SecureRandom random = new SecureRandom();

    public void provisionCredential(User target, char[] password, User organiser) {
        if (target == null || target.getId() <= 0 || organiser == null || !organiser.isOrganiser()) {
            throw new IllegalArgumentException("An organiser must provision an existing user");
        }
        validatePassword(password);
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        byte[] hash = derive(password, salt, ITERATIONS);
        try {
            JdbcTransactions.execute("Cannot provision user credential", connection -> {
                try (PreparedStatement update = connection.prepareStatement(
                        "INSERT INTO user_credentials (user_id, salt, password_hash, iterations) VALUES (?, ?, ?, ?) "
                                + "ON DUPLICATE KEY UPDATE salt = VALUES(salt), password_hash = VALUES(password_hash), "
                                + "iterations = VALUES(iterations)")) {
                    update.setInt(1, target.getId());
                    update.setBytes(2, salt);
                    update.setBytes(3, hash);
                    update.setInt(4, ITERATIONS);
                    update.executeUpdate();
                }
                return null;
            });
        } finally {
            Arrays.fill(password, '\0');
            Arrays.fill(hash, (byte) 0);
        }
    }

    public Session authenticate(String username, char[] password) {
        if (username == null || username.isBlank() || password == null) {
            throw new IllegalArgumentException("Username and password are required");
        }
        try (Connection connection = PersistenceManager.getConnection();
             PreparedStatement select = connection.prepareStatement(
                     "SELECT u.id, uc.salt, uc.password_hash, uc.iterations FROM users u "
                             + "JOIN user_credentials uc ON uc.user_id = u.id WHERE u.username = ?")) {
            select.setString(1, username);
            try (ResultSet row = select.executeQuery()) {
                if (!row.next()) throw new IllegalArgumentException("Invalid credentials");
                byte[] expected = row.getBytes(3);
                byte[] actual = derive(password, row.getBytes(2), row.getInt(4));
                boolean valid = MessageDigest.isEqual(expected, actual);
                Arrays.fill(actual, (byte) 0);
                if (!valid) throw new IllegalArgumentException("Invalid credentials");
                return new Session(User.loadUserById(row.getInt(1)));
            }
        } catch (SQLException failure) {
            throw new PersistenceException("Cannot authenticate user", failure);
        } finally {
            Arrays.fill(password, '\0');
        }
    }

    private static void validatePassword(char[] password) {
        if (password == null || password.length < 10) {
            throw new IllegalArgumentException("Password must contain at least 10 characters");
        }
    }

    private static byte[] derive(char[] password, byte[] salt, int iterations) {
        try {
            PBEKeySpec specification = new PBEKeySpec(password, salt, iterations, KEY_BITS);
            try {
                return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                        .generateSecret(specification).getEncoded();
            } finally {
                specification.clearPassword();
            }
        } catch (GeneralSecurityException failure) {
            throw new IllegalStateException("Password hashing is not available", failure);
        }
    }
}
