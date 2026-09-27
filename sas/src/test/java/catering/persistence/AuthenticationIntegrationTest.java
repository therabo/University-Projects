package catering.persistence;

import catering.businesslogic.user.AuthenticationService;
import catering.businesslogic.user.User;
import catering.businesslogic.user.UserManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@EnabledIfEnvironmentVariable(named = "CATERING_INTEGRATION", matches = "true")
class AuthenticationIntegrationTest {
    @Test
    void authenticatesHashedCredentialsAndIsolatesThreadSession() throws Exception {
        User target = User.loadUser("Guido");
        User organiser = User.loadUser("Lidia");
        AuthenticationService authentication = new AuthenticationService();
        try {
            authentication.provisionCredential(target, "strong-demo-password".toCharArray(), organiser);
            assertThrows(IllegalArgumentException.class,
                    () -> authentication.authenticate("Guido", "wrong-password".toCharArray()));
            assertEquals(target.getId(), authentication.authenticate(
                    "Guido", "strong-demo-password".toCharArray()).getUser().getId());

            UserManager sessions = new UserManager();
            sessions.login("Guido", "strong-demo-password".toCharArray());
            assertEquals(target.getId(), sessions.getCurrentUser().getId());
            AtomicReference<User> otherThread = new AtomicReference<>();
            Thread thread = new Thread(() -> otherThread.set(sessions.getCurrentUser()));
            thread.start();
            thread.join();
            assertNull(otherThread.get());
            sessions.logout();
            assertNull(sessions.getCurrentUser());
        } finally {
            PersistenceManager.executeUpdate("DELETE FROM user_credentials WHERE user_id = ?",
                    statement -> statement.setInt(1, target.getId()));
        }
    }
}
