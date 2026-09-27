package catering.businesslogic.user;

import catering.persistence.PersistenceManager;
import catering.persistence.ResultHandler;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * Represents a user in the system with associated roles.
 * <p>
 * This class provides methods to load user data from the database and manage user roles.
 * </p>
 */
public class User {

    private static Map<Integer, User> loadedUsers = new HashMap<Integer, User>();
    private int id;
    private String username;
    private Set<Role> roles;

    public User() {
        id = 0;
        username = "";
        this.roles = new HashSet<>();
    }

    /**
     * <h2>STATIC METHODS FOR PERSISTENCE</h2>
     */

    /**
     * Loads a user from the database based on their ID.
     *
     * @param uid The ID of the user to load.
     * @return The user with the specified ID.
     */
    public static User loadUserById(int uid) {
        if (loadedUsers.containsKey(uid)) return loadedUsers.get(uid);

        User load = new User();
        String userQuery = "SELECT * FROM users WHERE id = ?";
        PersistenceManager.executeQuery(userQuery, ps -> ps.setInt(1, uid), new ResultHandler() {
            @Override
            public void handle(ResultSet rs) throws SQLException {
                load.id = rs.getInt("id");
                load.username = rs.getString("username");
            }
        });
        if (load.id > 0) {
            loadedUsers.put(load.id, load);
            String roleQuery = "SELECT * FROM userroles WHERE user_id=" + load.id;
            PersistenceManager.executeQuery(roleQuery, new ResultHandler() {
                @Override
                public void handle(ResultSet rs) throws SQLException {
                    String role = rs.getString("role_id");
                    switch (role.charAt(0)) {
                        case 'c':
                            load.roles.add(User.Role.CUOCO);
                            break;
                        case 'h':
                            load.roles.add(User.Role.CHEF);
                            break;
                        case 'o':
                            load.roles.add(User.Role.ORGANIZZATORE);
                            break;
                        case 's':
                            load.roles.add(User.Role.SERVIZIO);
                    }
                }
            });
        }
        return load;
    }

    /**
     * Loads a user from the database based on their username.
     *
     * @param username The username of the user to load.
     * @return The user with the specified username.
     */
    public static User loadUser(String username) {
        User u = new User();
        String userQuery = "SELECT * FROM users WHERE username = ?";
        PersistenceManager.executeQuery(userQuery, ps -> ps.setString(1, username), new ResultHandler() {
            @Override
            public void handle(ResultSet rs) throws SQLException {
                u.id = rs.getInt("id");
                u.username = rs.getString("username");
            }
        });
        if (u.id > 0) {
            loadedUsers.put(u.id, u);
            String roleQuery = "SELECT * FROM userroles WHERE user_id=" + u.id;
            PersistenceManager.executeQuery(roleQuery, new ResultHandler() {
                @Override
                public void handle(ResultSet rs) throws SQLException {
                    String role = rs.getString("role_id");
                    switch (role.charAt(0)) {
                        case 'c':
                            u.roles.add(User.Role.CUOCO);
                            break;
                        case 'h':
                            u.roles.add(User.Role.CHEF);
                            break;
                        case 'o':
                            u.roles.add(User.Role.ORGANIZZATORE);
                            break;
                        case 's':
                            u.roles.add(User.Role.SERVIZIO);
                    }
                }
            });
        }
        return u;
    }

    /**
     * Loads all users from the database.
     *
     * @return A list of all users.
     */
    public static List<User> loadAllUsers() {
        List<User> users = new ArrayList<>();
        String query = "SELECT * FROM users";

        PersistenceManager.executeQuery(query, new ResultHandler() {
            @Override
            public void handle(ResultSet rs) throws SQLException {
                User user = new User();
                user.id = rs.getInt("id");
                user.username = rs.getString("username");
                users.add(user);
                loadedUsers.put(user.id, user);
            }
        });

        for (User user : users) {
            String roleQuery = "SELECT * FROM userroles WHERE user_id=" + user.id;
            PersistenceManager.executeQuery(roleQuery, new ResultHandler() {
                @Override
                public void handle(ResultSet rs) throws SQLException {
                    String role = rs.getString("role_id");
                    switch (role.charAt(0)) {
                        case 'c':
                            user.roles.add(User.Role.CUOCO);
                            break;
                        case 'h':
                            user.roles.add(User.Role.CHEF);
                            break;
                        case 'o':
                            user.roles.add(User.Role.ORGANIZZATORE);
                            break;
                        case 's':
                            user.roles.add(User.Role.SERVIZIO);
                    }
                }
            });
        }

        return users;
    }

    /**
     * <h2>METHODS FOR MAIN OPERATIONS</h2>
     */

    /**
     * Checks if the user has the role of CHEF.
     *
     * @return true if the user has the role of CHEF, false otherwise.
     */
    public boolean isChef() {
        return roles.contains(Role.CHEF);
    }

    /**
     * Checks if the user has the role of ORGANIZZATORE.
     *
     * @return true if the user has the role of ORGANIZZATORE, false otherwise.
     */
    public boolean isOrganiser() {
        return roles.contains(Role.ORGANIZZATORE);
    }

    /**
     * Checks if the user has the role of CUOCO.
     *
     * @return true if the user has the role of CUOCO, false otherwise.
     */
    public boolean isCook() {
        return roles.contains(Role.CUOCO);
    }

    public boolean isService() {
        return roles.contains(Role.SERVIZIO);
    }


    /**
     * Returns a string representation of the user including their roles.
     *
     * @return A string representation of the user with their roles.
     */
    public String toString() {
        String result = username;
        if (roles.size() > 0) {
            result += ": ";

            for (User.Role r : roles) {
                result += r.toString() + " ";
            }
        }
        return result;
    }

    /**
     * <h2>GETTER AND SETTER</h2>
     */

    /**
     * Returns the username of the user.
     *
     * @return The username of the user.
     */
    public String getUserName() {
        return username;
    }

    /**
     * Returns the ID of the user.
     *
     * @return The ID of the user.
     */
    public int getId() {
        return this.id;
    }

    public static enum Role {SERVIZIO, CUOCO, CHEF, ORGANIZZATORE}
}
