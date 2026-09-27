package Prim;

import Graph.AbstractEdge;
import Graph.Graph;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Locale;

public final class PrimCsv {
    private PrimCsv() {
    }

    static void run(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: java Prim.Prim <italian_dist_graph.csv>");
            System.exit(2);
        }

        try {
            Graph<String, Double> graph = readGraph(Path.of(args[0]));
            Collection<? extends AbstractEdge<String, Double>> forest = Prim.minimumSpanningForest(graph);
            double totalWeight = 0.0;
            for (AbstractEdge<String, Double> edge : forest) {
                System.out.printf(Locale.ROOT, "%s,%s,%s%n", edge.getStart(), edge.getEnd(), edge.getLabel());
                totalWeight += edge.getLabel();
            }
            System.err.printf(Locale.ROOT, "Nodes: %d, edges: %d, weight: %.3f km%n",
                    graph.numNodes(), forest.size(), totalWeight / 1000.0);
        } catch (IOException | IllegalArgumentException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }

    public static Graph<String, Double> readGraph(Path path) throws IOException {
        Graph<String, Double> graph = new Graph<>(false, true);
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                String[] fields = line.split(",", -1);
                if (fields.length != 3 || fields[0].isBlank() || fields[1].isBlank()) {
                    throw new IOException("Invalid CSV at line " + lineNumber);
                }
                String source = fields[0].trim();
                String destination = fields[1].trim();
                double distance;
                try {
                    distance = Double.parseDouble(fields[2].trim());
                } catch (NumberFormatException e) {
                    throw new IOException("Invalid distance at line " + lineNumber, e);
                }
                if (!Double.isFinite(distance) || distance < 0) {
                    throw new IOException("Invalid distance at line " + lineNumber);
                }
                graph.addNode(source);
                graph.addNode(destination);
                graph.addEdge(source, destination, distance);
            }
        }
        return graph;
    }
}
