package catering.businesslogic.summarysheet;

import catering.businesslogic.UseCaseLogicException;
import catering.businesslogic.event.ServiceInfo;
import catering.businesslogic.user.User;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SummarySheetManagerTest {
    @Test
    void usesInjectedUserForSheetAuthorization() throws Exception {
        User chef = new User() {
            @Override public int getId() { return 7; }
            @Override public boolean isChef() { return true; }
        };
        ServiceInfo service = new ServiceInfo() {
            @Override public boolean isAssignedChef(User user) { return user != null && user.getId() == 7; }
            @Override public boolean isAssignedMenu() { return true; }
            @Override public catering.businesslogic.menu.Menu getMenu(int ignored) { return null; }
        };
        AtomicReference<User> currentUser = new AtomicReference<>();
        SummarySheetManager manager = new SummarySheetManager(currentUser::get);

        assertThrows(UseCaseLogicException.class, () -> manager.createSummarySheet(service));
        currentUser.set(chef);
        SummarySheet sheet = manager.createSummarySheet(service);
        assertEquals(sheet, manager.getCurrentSheet());
        assertEquals(sheet, service.getSheet());
        assertTrue(manager.modifySheet(sheet));

        currentUser.set(new User() {
            @Override public int getId() { return 8; }
        });
        assertThrows(UseCaseLogicException.class, () -> manager.modifySheet(sheet));
    }
}
