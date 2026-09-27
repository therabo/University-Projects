package catering.examples.summarysheet;

import catering.businesslogic.CatERing;
import catering.businesslogic.turn.Turn;
import catering.businesslogic.user.User;

import java.util.List;

public class TestSheet8 {
    /**
     * OP.: CONSULT_TURNS
     */
    public static void main(String[] args) {
        try {
            System.out.println("LOGIN");
            CatERing.getInstance().getUserManager().fakeLogin("Lidia");
            User currentUser = CatERing.getInstance().getUserManager().getCurrentUser();
            System.out.println("Utente loggato: " + currentUser.getUserName());
            System.out.println("------------------------------------");

            System.out.println("CARICAMENTO DI TUTTI I TURNI");
            List<Turn> turns = CatERing.getInstance().loadAllTurn();
            for (Turn turn : turns) {
                System.out.println(turn);
            }
            System.out.println("------------------------------------");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
