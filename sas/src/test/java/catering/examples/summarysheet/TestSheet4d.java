package catering.examples.summarysheet;

import catering.businesslogic.CatERing;
import catering.businesslogic.summarysheet.Assignment;
import catering.businesslogic.summarysheet.SummarySheet;
import catering.businesslogic.event.EventInfo;
import catering.businesslogic.event.ServiceInfo;
import catering.businesslogic.user.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class TestSheet4d {
    /**
     * OP.: DELETE_ASSIGNMENT
     */
    public static void main(String[] args) {
        try {
            System.out.println("LOGIN");
            CatERing.getInstance().getUserManager().fakeLogin("Lidia");
            User currentUser = CatERing.getInstance().getUserManager().getCurrentUser();
            System.out.println(currentUser);
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
            SummarySheet selectedSheet = summarySheets.get(sheetIndex);
            CatERing.getInstance().getSummarySheetManager().setCurrentSheet(selectedSheet); // Imposta il foglio corrente

            // Stampa il foglio riepilogativo con tutti i suoi oggetti associati
            System.out.println("\nDettagli del Summary Sheet:");
            System.out.println(selectedSheet.testString());

            // Selezione dell'assignment da eliminare
            System.out.println("\nAssignments:");
            List<Assignment> assignments = selectedSheet.getAssignments();
            for (int i = 0; i < assignments.size(); i++) {
                System.out.println((i + 1) + ": " + assignments.get(i).toString());
            }

            System.out.print("Seleziona il numero dell'assignment da eliminare: ");
            int assignmentIndex = scanner.nextInt() - 1;
            Assignment selectedAssignment = assignments.get(assignmentIndex);

            // Elimina l'assegnamento selezionato
            CatERing.getInstance().getSummarySheetManager().deleteAssignment(selectedAssignment);

            // Stampa il foglio riepilogativo aggiornato
            System.out.println("\nDettagli del Summary Sheet aggiornato:");
            System.out.println(selectedSheet.testString());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
