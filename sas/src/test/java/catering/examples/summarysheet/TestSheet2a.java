package catering.examples.summarysheet;


import catering.businesslogic.CatERing;
import catering.businesslogic.summarysheet.SummarySheet;
import catering.businesslogic.UseCaseLogicException;
import catering.businesslogic.event.EventInfo;
import catering.businesslogic.event.ServiceInfo;
import catering.businesslogic.recipe.Recipe;
import catering.businesslogic.user.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class TestSheet2a {
    /**
     * OP.: REMOVE EXTRA_PREPARATION_RECIPE
     */
    public static void main(String[] args) {
        try {
            // Effettua il login
            System.out.println("LOGIN");
            CatERing.getInstance().getUserManager().fakeLogin("Lidia");
            User currentUser = CatERing.getInstance().getUserManager().getCurrentUser();
            System.out.println("Utente loggato: " + currentUser.getUserName());

            // Ottiene le informazioni sugli eventi
            System.out.println("\nGET EVENT INFO");
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

            // Carica i fogli riepilogativi per il servizio selezionato
            System.out.println("\nSummarySheets trovati per il servizio selezionato:");
            List<SummarySheet> sheets = CatERing.getInstance().loadAllSummarySheetsForService(selectedService.getId());
            for (int i = 0; i < sheets.size(); i++) {
                System.out.println((i + 1) + ": SummarySheet ID: " + sheets.get(i).getId());
            }

            System.out.print("Seleziona il numero del SummarySheet desiderato: ");
            int sheetIndex = scanner.nextInt() - 1;
            SummarySheet selectedSheet = sheets.get(sheetIndex);
            CatERing.getInstance().getSummarySheetManager().setCurrentSheet(selectedSheet); // Imposta il foglio corrente

            // Stampa il foglio riepilogativo con tutti i suoi oggetti associati
            System.out.println("\nDettagli del Summary Sheet:");
            System.out.println(selectedSheet.testString());

            // Selezione dell'extra task da rimuovere
            System.out.println("\nExtra Tasks:");
            List<Recipe> extraTasks = selectedSheet.getExtraTask();
            for (int i = 0; i < extraTasks.size(); i++) {
                System.out.println((i + 1) + ": " + extraTasks.get(i).getName());
            }

            System.out.print("Seleziona il numero dell'extra task da rimuovere: ");
            int taskIndex = scanner.nextInt() - 1;
            Recipe selectedTask = extraTasks.get(taskIndex);

            // Rimuove l'extra task selezionato
            CatERing.getInstance().getSummarySheetManager().removeRecipe(selectedTask);

            // Stampa il foglio riepilogativo aggiornato
            System.out.println("\nDettagli del Summary Sheet aggiornato:");
            System.out.println(selectedSheet.testString());

        } catch (UseCaseLogicException e) {
            System.out.println("Errore di logica nello use case");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
