package catering.businesslogic.user;

/**
 * Manages user-related operations in the business logic layer.
 * <p>
 * This class handles user login and provides access to the currently logged-in user.
 * </p>
 */
public class UserManager {
    private final ThreadLocal<User> currentUser = new ThreadLocal<>();
    private final AuthenticationService authentication = new AuthenticationService();

    /**
     * Simulates a user login by loading a user based on the provided username.
     * <p>
     * This method is intended for testing purposes and sets the current user
     * based on the given username.
     * </p>
     *
     * @param username The username of the user to be loaded and set as current user.
     */
    public void fakeLogin(String username) {
        this.currentUser.set(User.loadUser(username));
    }

    public AuthenticationService.Session login(String username, char[] password) {
        AuthenticationService.Session session = authentication.authenticate(username, password);
        currentUser.set(session.getUser());
        return session;
    }

    public void logout() {
        currentUser.remove();
    }

    public AuthenticationService getAuthenticationService() {
        return authentication;
    }

    /**
     * Returns the currently logged-in user.
     * <p>
     * This method provides access to the user who is currently logged in.
     * </p>
     *
     * @return The currently logged-in user.
     */
    public User getCurrentUser() {
        return this.currentUser.get();
    }
}
