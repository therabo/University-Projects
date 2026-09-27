package catering.examples.summarysheet;

import catering.businesslogic.CatERing;
import catering.businesslogic.summarysheet.SummarySheet;
import catering.businesslogic.UseCaseLogicException;
import catering.businesslogic.event.EventInfo;
import catering.businesslogic.event.ServiceInfo;
import catering.businesslogic.user.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class TestSheet1 {
    /**
     * OP.: CREATE SHEET
     */
    public static void main(String[] args) {
        try {
            System.out.println("LOGIN");
            CatERing.getInstance().getUserManager().fakeLogin("Lidia");
            User currentUser = CatERing.getInstance().getUserManager().getCurrentUser();
            System.out.println(currentUser);

            System.out.println("\nGET EVENT INFO");
            ArrayList<EventInfo> events = CatERing.getInstance().getEventManager().getEventInfo();
            for (int i = 0; i < events.size(); i++) {
                System.out.println((i + 1) + ": " + events.get(i));
            }

            Scanner scanner = new Scanner(System.in);
            System.out.print("Seleziona il numero dell'evento desiderato: ");
            int eventIndex = scanner.nextInt() - 1;
            EventInfo selectedEvent = events.get(eventIndex);
            System.out.println("Evento selezionato:");
            System.out.println(selectedEvent);

            List<ServiceInfo> services = selectedEvent.getServices();
            for (int i = 0; i < services.size(); i++) {
                System.out.println((i + 1) + ": " + services.get(i));
            }

            System.out.print("Seleziona il numero del servizio desiderato: ");
            int serviceIndex = scanner.nextInt() - 1;
            ServiceInfo selectedService = services.get(serviceIndex);
            System.out.println("Servizio selezionato:");
            System.out.println(selectedService);

            System.out.println("\nCREATE SUMMARY SHEET");
            SummarySheet summarySheet = CatERing.getInstance().getSummarySheetManager().createSummarySheet(selectedService);
            if (summarySheet != null) {
                // Stampa il foglio riepilogativo con tutti i suoi oggetti associati
                System.out.println("\nDettagli del Summary Sheet:");
                System.out.println(summarySheet.testString());
            } else {
                System.out.println("Errore: il foglio riepilogativo non è stato creato.");
            }

        } catch (UseCaseLogicException e) {
            System.out.println("Errore di logica nello use case");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
