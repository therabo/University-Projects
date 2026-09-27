package catering.persistence;

import catering.businesslogic.CatERing;
import catering.businesslogic.summarysheet.SummarySheet;
import catering.businesslogic.summarysheet.SummarySheetManager;
import catering.businesslogic.UseCaseLogicException;
import catering.businesslogic.turn.Turn;
import catering.businesslogic.event.EventInfo;
import catering.businesslogic.event.ServiceInfo;
import catering.businesslogic.menu.Menu;
import catering.businesslogic.recipe.Recipe;
import catering.businesslogic.user.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.sql.Date;
import java.sql.Time;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@EnabledIfEnvironmentVariable(named = "CATERING_INTEGRATION", matches = "true")
class DatabaseIntegrationTest {
    @Test
    void loadsSeedDataOnCaseSensitiveMysql() {
        assertEquals(10, User.loadAllUsers().size());
        assertTrue(User.loadUser("Lidia").isChef());
        assertTrue(Recipe.loadAllRecipes().size() > 0);
        assertTrue(Menu.loadAllMenus().size() > 0);
        assertTrue(EventInfo.loadAllEventInfo().size() > 0);
        assertTrue(new JdbcTurnRepository().findAll().size() > 0);
        ServiceInfo service = ServiceInfo.loadServiceInfoById(1);
        assertTrue(service.isAssignedChef(User.loadUser("Lidia")));
        assertTrue(!service.isAssignedChef(User.loadUser("Tony")));
    }

    @Test
    void savesSheetAndEnforcesAssignmentRules() throws Exception {
        ServiceInfo service = ServiceInfo.loadServiceInfoById(1);
        User owner = User.loadUser("Lidia");
        SummarySheet sheet = new SummarySheet(owner, service);
        SummarySheet.saveNewSummarySheet(sheet);
        JdbcTurnRepository turnRepository = new JdbcTurnRepository();
        Turn turn = new Turn();
        turn.setDate(Date.valueOf(LocalDate.now().plusDays(14)));
        turn.setStart(Time.valueOf("10:00:00"));
        turn.setEnd(Time.valueOf("12:00:00"));
        turn.setLocation("Sheet test kitchen");
        turn.setKind(Turn.Kind.KITCHEN);
        turn.setOwnerId(owner.getId());
        turnRepository.save(turn);
        try {
            assertTrue(sheet.getId() > 0);
            SummarySheetManager manager = CatERing.getInstance().getSummarySheetManager();
            manager.setCurrentSheet(sheet);
            User cook = User.loadUser("Guido");
            Recipe recipe = Recipe.loadRecipeById(1);
            CatERing.getInstance().getUserManager().fakeLogin("Guido");
            CatERing.getInstance().getTurnManager().declareAvailability(turn);
            CatERing.getInstance().getUserManager().fakeLogin("Lidia");
            CatERing.getInstance().getTurnManager().assignTurn(turn, cook);

            CatERing.getInstance().getUserManager().fakeLogin("Tony");
            assertThrows(UseCaseLogicException.class,
                    () -> manager.createAssignment(sheet, cook, turn, recipe, 1, 30));

            CatERing.getInstance().getUserManager().fakeLogin("Lidia");
            assertThrows(IllegalArgumentException.class,
                    () -> manager.createAssignment(sheet, cook, turn, recipe, 1, 150));
            manager.createAssignment(sheet, cook, turn, recipe, 1, 90);
            assertEquals(1, sheet.getAssignments().size());
            int assignmentId = sheet.getAssignments().get(0).getId();
            assertTrue(assignmentId > 0);
            CatERing.getInstance().getKitchenTaskManager().reportIssue(assignmentId, cook, "Missing ingredient");
            assertEquals("Missing ingredient", CatERing.getInstance().getKitchenTaskManager()
                    .forSheet(sheet.getId(), owner).get(0).getIssueNote());
            assertThrows(IllegalArgumentException.class,
                    () -> CatERing.getInstance().getKitchenTaskManager()
                            .complete(assignmentId, User.loadUser("Tony")));
            CatERing.getInstance().getKitchenTaskManager().complete(assignmentId, cook);
            assertNotNull(CatERing.getInstance().getKitchenTaskManager()
                    .forSheet(sheet.getId(), owner).get(0).getCompletedAt());
        } finally {
            SummarySheet.deleteSheet(sheet);
            turnRepository.delete(turn.getId());
        }
    }

    @Test
    void persistsTurnLifecycleAndStaffAssignment() {
        JdbcTurnRepository repository = new JdbcTurnRepository();
        Turn turn = new Turn();
        turn.setService(ServiceInfo.loadServiceInfoById(1));
        turn.setDate(Date.valueOf(LocalDate.now().plusDays(7)));
        turn.setStart(Time.valueOf("15:00:00"));
        turn.setEnd(Time.valueOf("17:00:00"));
        turn.setLocation("Kitchen");
        repository.save(turn);
        Turn overlapping = new Turn();
        overlapping.setService(turn.getService());
        overlapping.setDate(Date.valueOf(LocalDate.now().plusDays(7)));
        overlapping.setStart(Time.valueOf("16:00:00"));
        overlapping.setEnd(Time.valueOf("18:00:00"));
        overlapping.setLocation("Other kitchen");
        repository.save(overlapping);
        try {
            assertTrue(turn.getId() > 0);
            assertNotNull(repository.findById(turn.getId()));
            turn.setLocation("Main kitchen");
            repository.updateLocation(turn);
            assertEquals("Main kitchen", repository.findById(turn.getId()).getLocation());
            User cook = User.loadUser("Guido");
            CatERing.getInstance().getUserManager().fakeLogin("Guido");
            CatERing.getInstance().getTurnManager().declareAvailability(turn);
            CatERing.getInstance().getTurnManager().declareAvailability(overlapping);
            CatERing.getInstance().getUserManager().fakeLogin("Lidia");
            CatERing.getInstance().getTurnManager().assignTurn(turn, cook);
            assertEquals(1, repository.findById(turn.getId()).getAssignedUsers().size());
            assertThrows(IllegalArgumentException.class,
                    () -> CatERing.getInstance().getTurnManager().assignTurn(overlapping, cook));
            turn.setLocation("Changed after availability");
            assertThrows(IllegalArgumentException.class, () -> repository.updateLocation(turn));
        } finally {
            repository.delete(overlapping.getId());
            repository.delete(turn.getId());
        }
    }

    @Test
    void preservesQuotedMenuTextAndDeletesSectionsInDependencyOrder() throws Exception {
        Menu menu = new Menu(User.loadUser("Lidia"), "L'équipe", new String[] {"Chef's choice"});
        menu.addSection("D'entrata");
        Menu.saveNewMenu(menu);
        try {
            assertTrue(menu.getId() > 0);
            try (Connection connection = PersistenceManager.getConnection();
                 PreparedStatement statement = connection.prepareStatement("SELECT title FROM menus WHERE id = ?")) {
                statement.setInt(1, menu.getId());
                try (ResultSet rows = statement.executeQuery()) {
                    assertTrue(rows.next());
                    assertEquals("L'équipe", rows.getString(1));
                }
            }
        } finally {
            Menu.deleteMenu(menu);
        }
    }
}
