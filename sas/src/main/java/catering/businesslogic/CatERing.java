package catering.businesslogic;

import catering.businesslogic.summarysheet.SummarySheet;
import catering.businesslogic.summarysheet.SummarySheetManager;
import catering.businesslogic.summarysheet.KitchenTaskManager;
import catering.businesslogic.turn.Turn;
import catering.businesslogic.turn.TurnManager;
import catering.businesslogic.event.EventManager;
import catering.businesslogic.event.EventWorkflowManager;
import catering.businesslogic.menu.MenuManager;
import catering.businesslogic.recipe.Recipe;
import catering.businesslogic.recipe.RecipeManager;
import catering.businesslogic.recipe.RecipeBookManager;
import catering.businesslogic.user.User;
import catering.businesslogic.user.UserManager;
import catering.persistence.MenuPersistence;
import catering.persistence.JdbcTurnRepository;
import catering.persistence.SheetPersistence;

import java.util.List;

/**
 * The central class of the catering business logic layer. It acts as a singleton
 * providing access to various managers and persistence operations.
 * <p>
 * This class initializes and manages instances of {@link MenuManager}, {@link RecipeManager},
 * {@link UserManager}, {@link EventManager}, and {@link SummarySheetManager}. It also handles
 * the persistence layers for menus and summary sheets.
 * </p>
 */
public class CatERing {
    private static CatERing singleInstance;
    private MenuManager menuMgr;
    private RecipeManager recipeMgr;
    private RecipeBookManager recipeBookMgr;
    private UserManager userMgr;
    private EventManager eventMgr;
    private EventWorkflowManager eventWorkflowMgr;
    private SummarySheetManager sheetMgr;
    private TurnManager turnMgr;
    private KitchenTaskManager kitchenTaskMgr;
    private MenuPersistence menuPersistence;
    private SheetPersistence sheetPersistence;

    private CatERing() {
        userMgr = new UserManager();
        menuMgr = new MenuManager(userMgr::getCurrentUser);
        recipeMgr = new RecipeManager();
        recipeBookMgr = new RecipeBookManager();
        eventMgr = new EventManager();
        eventWorkflowMgr = new EventWorkflowManager();
        menuPersistence = new MenuPersistence();
        sheetMgr = new SummarySheetManager(userMgr::getCurrentUser);
        kitchenTaskMgr = new KitchenTaskManager();
        turnMgr = new TurnManager(new JdbcTurnRepository(), userMgr::getCurrentUser);
        sheetPersistence = new SheetPersistence();
        menuMgr.addEventReceiver(menuPersistence);
        sheetMgr.addReceiver(sheetPersistence);
    }

    public static CatERing getInstance() {
        if (singleInstance == null) {
            singleInstance = new CatERing();
        }
        return singleInstance;
    }

    public MenuManager getMenuManager() {
        return menuMgr;
    }

    public RecipeManager getRecipeManager() {
        return recipeMgr;
    }

    public RecipeBookManager getRecipeBookManager() {
        return recipeBookMgr;
    }

    public UserManager getUserManager() {
        return userMgr;
    }

    public EventManager getEventManager() {
        return eventMgr;
    }

    public EventWorkflowManager getEventWorkflowManager() {
        return eventWorkflowMgr;
    }

    public SummarySheetManager getSummarySheetManager() {
        return sheetMgr;
    }

    public TurnManager getTurnManager() {
        return turnMgr;
    }

    public KitchenTaskManager getKitchenTaskManager() {
        return kitchenTaskMgr;
    }

    public List<SummarySheet> loadAllSummarySheets() {
        return SummarySheet.loadAllSummarySheets();
    }

    public List<SummarySheet> loadAllSummarySheetsForService(int serviceId) {
        return SummarySheet.loadAllSummarySheetsForService(serviceId);
    }

    public List<User> loadAllUsers() {
        return User.loadAllUsers();
    }


    public List<Turn> loadAllTurn() {
        return turnMgr.findAll();
    }

    public List<Turn> loadAllTurnForService(int service_id) {
        return turnMgr.findByService(service_id);
    }


    public List<Recipe> loadAllRecipes() {
        return Recipe.loadAllRecipes();
    }
}
