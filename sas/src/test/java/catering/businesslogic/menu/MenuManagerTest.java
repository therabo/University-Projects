package catering.businesslogic.menu;

import catering.businesslogic.UseCaseLogicException;
import catering.businesslogic.user.User;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MenuManagerTest {
    @Test
    void usesInjectedUserForMenuAuthorization() throws Exception {
        AtomicReference<User> currentUser = new AtomicReference<>();
        MenuManager manager = new MenuManager(currentUser::get);

        assertThrows(UseCaseLogicException.class, () -> manager.createMenu("Demo"));
        currentUser.set(new User());
        assertThrows(UseCaseLogicException.class, () -> manager.createMenu("Demo"));

        User chef = new User() {
            @Override public int getId() { return 7; }
            @Override public boolean isChef() { return true; }
        };
        currentUser.set(chef);
        Menu menu = manager.createMenu("Demo");
        assertEquals(menu, manager.getCurrentMenu());

        currentUser.set(new User());
        assertThrows(UseCaseLogicException.class, () -> manager.copyMenu(menu));
        currentUser.set(chef);
        assertEquals(manager.copyMenu(menu), manager.getCurrentMenu());
    }
}
