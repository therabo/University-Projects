package catering.examples.summarysheet;

import catering.businesslogic.CatERing;
import catering.businesslogic.summarysheet.SummarySheet;
import catering.businesslogic.turn.Turn;
import catering.businesslogic.UseCaseLogicException;
import catering.businesslogic.event.EventInfo;
import catering.businesslogic.event.ServiceInfo;
import catering.businesslogic.recipe.Recipe;
import catering.businesslogic.user.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class TestSheet4 {
    /**
     * OP.: CREATE_ASSIGNMENT
     */
    public static void main(String[] args) {
        try {
            System.out.println("LOGIN");
            CatERing.getInstance().getUserManager().fakeLogin("Lidia");
            User currentUser = CatERing.getInstance().getUserManager().getCurrentUser();
            System.out.println("Utente loggato: " + currentUser.getUserName());
            System.out.println("------------------------------------");

            // Ottiene le informazioni sugli eventi
            System.out.println("GET EVENT INFO");
            ArrayList<EventInfo> events = CatERing.getInstance().getEventManager().getEventInfo();
            for (int i = 0; i < events.size(); i++) {
                System.out.println((i + 1) + ": " + events.get(i));
            }

            // Seleziona un evento
            Scanner scanner = new Scanner(System.in);
            System.out.print("Seleziona il numero dell'evento desiderato: ");
            int eventIndex = scanner.nextInt() - 1;
            EventInfo selectedEvent = events.get(eventIndex);
            System.out.println("Evento selezionato:");
            System.out.println(selectedEvent);
            System.out.println("------------------------------------");

            // Seleziona un servizio per l'evento
            List<ServiceInfo> services = selectedEvent.getServices();
            for (int i = 0; i < services.size(); i++) {
                System.out.println((i + 1) + ": " + services.get(i));
            }

            System.out.print("Seleziona il numero del servizio desiderato: ");
            int serviceIndex = scanner.nextInt() - 1;
            ServiceInfo selectedService = services.get(serviceIndex);
            System.out.println("Servizio selezionato:");
            System.out.println(selectedService);
            System.out.println("------------------------------------");

            // Carica i fogli riepilogativi per il servizio selezionato
            System.out.println("SUMMARY SHEETS FOR SELECTED SERVICE");
            List<SummarySheet> summarySheets = CatERing.getInstance().loadAllSummarySheetsForService(selectedService.getId());
            for (int i = 0; i < summarySheets.size(); i++) {
                System.out.println((i + 1) + ": SummarySheet ID: " + summarySheets.get(i).getId());
            }

            // Seleziona un foglio riepilogativo
            System.out.print("Seleziona il numero del SummarySheet desiderato: ");
            int sheetIndex = scanner.nextInt() - 1;
            SummarySheet summarySheet;
            if (sheetIndex >= 0 && sheetIndex < summarySheets.size()) {
                summarySheet = summarySheets.get(sheetIndex);
            } else {
                summarySheet = CatERing.getInstance().getSummarySheetManager().createSummarySheet(selectedService);
                summarySheet.setNote("Nota di esempio per il foglio riepilogativo");
            }
            System.out.println(summarySheet.testString());
            System.out.println("------------------------------------");

            // Verifica che il SummarySheet abbia un ID valido
            if (summarySheet.getId() == 0) {
                throw new IllegalArgumentException("SummarySheet ID non valido");
            }
            List<User> allUsers = CatERing.getInstance().loadAllUsers();

            // Creare n assegnamenti in base alle necessità
            System.out.println("CREATE ASSIGNMENTS");
            List<Recipe> recipes = CatERing.getInstance().getRecipeManager().getRecipes();
            List<Turn> turns = CatERing.getInstance().loadAllTurnForService(selectedService.getId());

            // Verifica se i turni sono null o vuoti prima di entrare nel ciclo while
            if (turns == null || turns.isEmpty()) {
                System.out.println("I turni per questo servizio non sono ancora disponibili, non è possibile effettuare gli assegnamenti!");
            } else {
                boolean createMoreAssignments = true;

                while (createMoreAssignments && !allUsers.isEmpty() && !recipes.isEmpty()) {

                    System.out.println("Seleziona un utente:");
                    for (int j = 0; j < allUsers.size(); j++) {
                        System.out.println((j + 1) + ". " + allUsers.get(j).getUserName());
                    }
                    int userIndex = scanner.nextInt() - 1;

                    System.out.println("Seleziona una ricetta:");
                    for (int j = 0; j < recipes.size(); j++) {
                        System.out.println((j + 1) + ". " + recipes.get(j).getName());
                    }
                    int recipeIndex = scanner.nextInt() - 1;

                    System.out.println("Seleziona un turno:");
                    for (int j = 0; j < turns.size(); j++) {
                        System.out.println((j + 1) + ". " + turns.get(j).toString());
                    }
                    int turnIndex = scanner.nextInt() - 1;

                    System.out.println("Vuoi inserire la porzione? (true/false)");
                    boolean insertPortion = scanner.nextBoolean();
                    Integer portion = null;
                    if (insertPortion) {
                        System.out.println("Inserisci la porzione:");
                        portion = scanner.nextInt();
                    }

                    System.out.println("Vuoi inserire il tempo? (true/false)");
                    boolean insertTime = scanner.nextBoolean();
                    Integer time = null;
                    if (insertTime) {
                        System.out.println("Inserisci il tempo:");
                        time = scanner.nextInt();
                    }

                    CatERing.getInstance().getSummarySheetManager().createAssignment(
                            summarySheet,
                            allUsers.get(userIndex),
                            turns.get(turnIndex),
                            recipes.get(recipeIndex),
                            portion,
                            time
                    );

                    System.out.println("Vuoi creare un altro assegnamento? (true/false)");
                    createMoreAssignments = scanner.nextBoolean();
                }
                System.out.println("------------------------------------");

                // Stampa il foglio riepilogativo aggiornato
                System.out.println("FOGLIO RIEPILOGATIVO AGGIORNATO:");
                System.out.println(summarySheet.testString());
                System.out.println("------------------------------------");
            }

        } catch (UseCaseLogicException e) {
            System.out.println("Errore di logica nello use case");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
