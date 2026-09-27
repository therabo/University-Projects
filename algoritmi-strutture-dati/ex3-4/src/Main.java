import Graph.AbstractEdge;
import Graph.Graph;
import Prim.Prim;
import Prim.PrimCsv;
import QueueClass.PriorityQueue;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Comparator;
import java.util.Scanner;


public class Main {

    public static void main(String[] args) {


        Scanner scanner = new Scanner(System.in);

        System.out.println("Selezionare l'esercizio:");
        System.out.println("0) Esercizio 3");
        System.out.println("1) Esercizio 4");
        System.out.print("Scelta: ");

        int input = scanner.nextInt();
        scanner.nextLine();

        switch (input) {
            case 0:
                System.out.println("Esercizio 3 - Code di priorità");
                System.out.println("Determinare il tipo di coda da testare:");
                System.out.println("0) Max-heap");
                System.out.println("1) Min-heap");
                System.out.print("Scelta: ");
                int heapType = scanner.nextInt();
                scanner.nextLine();

                System.out.println("Seleziona il tipo della coda da utilizzare:");
                System.out.println("0) Intero");
                System.out.println("1) Double");
                System.out.println("2) String");
                System.out.print("Scelta: ");
                int dataType = scanner.nextInt();
                scanner.nextLine();

                // Dichiarazione delle code specifiche per tipo
                PriorityQueue<Integer> queueInt = null;
                PriorityQueue<Double> queueDouble = null;
                PriorityQueue<String> queueString = null;

                switch (dataType) {
                    case 0: {
                        Comparator<Integer> comparator = (heapType == 0) ? Comparator.reverseOrder() : Comparator.naturalOrder();
                        queueInt = new PriorityQueue<>(comparator);
                        break;
                    }
                    case 1: {
                        Comparator<Double> comparator = (heapType == 0) ? Comparator.reverseOrder() : Comparator.naturalOrder();
                        queueDouble = new PriorityQueue<>(comparator);
                        break;
                    }
                    case 2: {
                        Comparator<String> comparator = (heapType == 0) ? Comparator.reverseOrder() : Comparator.naturalOrder();
                        queueString = new PriorityQueue<>(comparator);
                        break;
                    }
                    default:
                        System.out.println("Opzione non valida.");
                        return;
                }

                System.out.println("Inserisci i valori per la coda (separati dalla virgola):");
                String inputValues = scanner.nextLine();
                String[] values = inputValues.split(",");

                try {
                    if (dataType == 0) {
                        for (String val : values) {
                            queueInt.push(Integer.parseInt(val.trim()));
                        }
                        System.out.println("Coda creata!");
                    } else if (dataType == 1) {
                        for (String val : values) {
                            queueDouble.push(Double.parseDouble(val.trim()));
                        }
                        System.out.println("Coda creata!");
                    } else if (dataType == 2) {
                        for (String val : values) {
                            queueString.push(val.trim());
                        }
                        System.out.println("Coda creata!");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Errore: Assicurati di inserire valori validi per il tipo selezionato.");
                }

                boolean exit = false;

                while (!exit) {
                    System.out.println("\nOperazioni sulla coda:");
                    System.out.println("0) Controlla se è vuota");
                    System.out.println("1) Inserisci elementi");
                    System.out.println("2) Contiene gli elementi (Inserisci gli elementi da cercare).");
                    System.out.println("3) Primo elemento della coda.");
                    System.out.println("4) Rimuovi il primo elemento della coda.");
                    System.out.println("5) Rimuovi gli elementi dalla coda (Inserisci gli elementi da eliminare).");
                    System.out.println("6) Stampa l'heap.");
                    System.out.println("7) Stampa l'hashmap.");
                    System.out.println("8) Termina l'esecuzione");
                    System.out.print("Scelta: ");

                    int operation = scanner.nextInt();
                    scanner.nextLine(); // consume newline

                    switch (operation) {
                        case 0:
                            // Controlla se la coda è vuota
                            if (dataType == 0) {
                                if (queueInt.empty()) {
                                    System.out.println("La coda è vuota.");
                                } else {
                                    System.out.println("La coda non è vuota.");
                                }
                            } else if (dataType == 1) {
                                if (queueDouble.empty()) {
                                    System.out.println("La coda è vuota.");
                                } else {
                                    System.out.println("La coda non è vuota.");
                                }
                            } else if (dataType == 2) {
                                if (queueString.empty()) {
                                    System.out.println("La coda è vuota.");
                                } else {
                                    System.out.println("La coda non è vuota.");
                                }
                            }
                            break;
                        case 1:
                            // Inserisci elementi
                            System.out.println("Inserisci i valori da aggiungere (separati dalla virgola):");
                            String addValues = scanner.nextLine();
                            String[] addItems = addValues.split(",");
                            for (String val : addItems) {
                                try {
                                    if (dataType == 0) {
                                        queueInt.push(Integer.parseInt(val.trim()));
                                    } else if (dataType == 1) {
                                        queueDouble.push(Double.parseDouble(val.trim()));
                                    } else if (dataType == 2) {
                                        queueString.push(val.trim());
                                    }
                                } catch (NumberFormatException e) {
                                    System.out.println("Valore non valido: " + val.trim());
                                }
                            }
                            System.out.println("Elementi aggiunti.");
                            break;
                        case 2:
                            // Controlla se contiene elementi
                            System.out.println("Inserisci i valori da cercare (separati dalla virgola):");
                            String searchValues = scanner.nextLine();
                            String[] searchItems = searchValues.split(",");
                            for (String val : searchItems) {
                                try {
                                    boolean contains = false;
                                    if (dataType == 0) {
                                        contains = queueInt.contains(Integer.parseInt(val.trim()));
                                    } else if (dataType == 1) {
                                        contains = queueDouble.contains(Double.parseDouble(val.trim()));
                                    } else if (dataType == 2) {
                                        contains = queueString.contains(val.trim());
                                    }
                                    if (contains) {
                                        System.out.println(val.trim() + " è presente nella coda.");
                                    } else {
                                        System.out.println(val.trim() + " non è presente nella coda.");
                                    }
                                } catch (NumberFormatException e) {
                                    System.out.println("Valore non valido: " + val.trim());
                                }
                            }
                            break;
                        case 3:
                            // Primo elemento della coda
                            if (dataType == 0) {
                                if (!queueInt.empty()) {
                                    System.out.println("Il primo elemento della coda è: " + queueInt.top());
                                } else {
                                    System.out.println("La coda è vuota.");
                                }
                            } else if (dataType == 1) {
                                if (!queueDouble.empty()) {
                                    System.out.println("Il primo elemento della coda è: " + queueDouble.top());
                                } else {
                                    System.out.println("La coda è vuota.");
                                }
                            } else if (dataType == 2) {
                                if (!queueString.empty()) {
                                    System.out.println("Il primo elemento della coda è: " + queueString.top());
                                } else {
                                    System.out.println("La coda è vuota.");
                                }
                            }
                            break;
                        case 4:
                            // Rimuovi il primo elemento della coda
                            if (dataType == 0) {
                                if (!queueInt.empty()) {
                                    queueInt.pop();
                                    System.out.println("Primo elemento rimosso.");
                                } else {
                                    System.out.println("La coda è vuota.");
                                }
                            } else if (dataType == 1) {
                                if (!queueDouble.empty()) {
                                    queueDouble.pop();
                                    System.out.println("Primo elemento rimosso.");
                                } else {
                                    System.out.println("La coda è vuota.");
                                }
                            } else if (dataType == 2) {
                                if (!queueString.empty()) {
                                    queueString.pop();
                                    System.out.println("Primo elemento rimosso.");
                                } else {
                                    System.out.println("La coda è vuota.");
                                }
                            }
                            break;
                        case 5:
                            // Rimuovi elementi dalla coda
                            System.out.println("Inserisci i valori da rimuovere (separati dalla virgola):");
                            String removeValues = scanner.nextLine();
                            String[] removeItems = removeValues.split(",");
                            for (String val : removeItems) {
                                try {
                                    boolean removed = false;
                                    if (dataType == 0) {
                                        removed = queueInt.remove(Integer.parseInt(val.trim()));
                                    } else if (dataType == 1) {
                                        removed = queueDouble.remove(Double.parseDouble(val.trim()));
                                    } else if (dataType == 2) {
                                        removed = queueString.remove(val.trim());
                                    }
                                    if (removed) {
                                        System.out.println(val.trim() + " rimosso dalla coda.");
                                    } else {
                                        System.out.println(val.trim() + " non trovato nella coda.");
                                    }
                                } catch (NumberFormatException e) {
                                    System.out.println("Valore non valido: " + val.trim());
                                }
                            }
                            break;
                        case 6:
                            // Stampa l'heap
                            System.out.println("Contenuto della coda:");
                            if (dataType == 0) {
                                queueInt.printHeap();
                            } else if (dataType == 1) {
                                queueDouble.printHeap();
                            } else if (dataType == 2) {
                                queueString.printHeap();
                            }
                            break;
                        case 7:
                            // Stampa l'hashmap
                            if (dataType == 0) {
                                queueInt.printHashMap();
                            } else if (dataType == 1) {
                                queueDouble.printHashMap();
                            } else if (dataType == 2) {
                                queueString.printHashMap();
                            }
                            break;
                        case 8:
                            // Termina l'esecuzione
                            exit = true;
                            break;
                        default:
                            System.out.println("Opzione non valida.");
                            break;
                    }
                }
                break;

            case 1:

                System.out.println("Esercizio 4 - Grafo");
                System.out.println("Inserisci il percorso del file per popolare il grafo(Non diretto ed etichettato):");
                String csvFilePath = scanner.nextLine();

                if (csvFilePath.startsWith("\"") && csvFilePath.endsWith("\"")) {
                    csvFilePath = csvFilePath.substring(1, csvFilePath.length() - 1);
                }

                try {
                    Graph<String, Double> graph = PrimCsv.readGraph(Path.of(csvFilePath));
                    System.out.println("Grafo creato!");
                    System.out.println("Numero di nodi: " + graph.numNodes());
                    System.out.println("Numero di archi: " + graph.numEdges());

                    Collection<? extends AbstractEdge<String, Double>> Edges = graph.getEdges();
                    double totalWeight = 0.0;
                    for (AbstractEdge<String, Double> edge : Edges) {
                        totalWeight += edge.getLabel();
                    }
                    String newWeight = String.format("%.2f", totalWeight);
                    System.out.println("Peso complessivo degli archi:" + newWeight);

                    System.out.println("0) Applica Prim!");
                    System.out.println("1) Termina esecuzione");

                    int check = scanner.nextInt();
                    if (check == 0) {
                        Collection<? extends AbstractEdge<String, Double>> mstEdges = Prim.minimumSpanningForest(graph);
                        System.out.println("Prim applicato!");
                        System.out.println("Numeri di nodi dopo Prim: " + graph.numNodes());
                        System.out.println("Numeri di archi dopo Prim: " + mstEdges.size());
                        double totalWeightPrim = 0.0;
                        for (AbstractEdge<String, Double> edge : mstEdges) {
                            totalWeightPrim += edge.getLabel();
                        }
                        String newWeightPrim = String.format("%.2f", totalWeightPrim);
                        System.out.println("Peso complessivo degli archi dopo Prim: " + newWeightPrim);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
        }

    }
}
