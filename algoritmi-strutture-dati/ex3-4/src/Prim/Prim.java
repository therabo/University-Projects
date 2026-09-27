package Prim;

import Graph.AbstractEdge;
import Graph.Edge;
import Graph.Graph;
import QueueClass.PriorityQueue;

import java.util.*;

public class Prim<V, L extends Number> {

    public static void main(String[] args) {
        PrimCsv.run(args);
    }

    /**
     * Computes the Minimum Spanning Forest of a given graph using Prim's algorithm.
     *
     * @param graph the input graph
     * @param <V>   the type of the vertices
     * @param <L>   the type of the labels associated with the edges (must extend Number)
     * @return a collection of edges that form the Minimum Spanning Forest of the graph
     */
    public static <V, L extends Number> Collection<? extends AbstractEdge<V, L>> minimumSpanningForest(Graph<V, L> graph) {
        Map<V, Double> keys = new HashMap<>();
        Map<V, V> parents = new HashMap<>();
        Set<Edge<V, L>> mstEdges = new HashSet<>();

        for (V node : graph.getNodes()) {
            keys.put(node, Double.POSITIVE_INFINITY);
            parents.put(node, null);
        }

        Comparator<V> comparator = Comparator.comparingDouble(keys::get);
        Set<V> visited = new HashSet<>();

        for (V startNode : graph.getNodes()) {
            if (!visited.contains(startNode)) {
                keys.put(startNode, 0.0);
                PriorityQueue<V> queue = new PriorityQueue<>(comparator);
                queue.push(startNode);

                while (!queue.empty()) {
                    V u = queue.top();
                    queue.pop();
                    visited.add(u);

                    if (parents.get(u) != null) {
                        L label = graph.getLabel(u, parents.get(u));
                        V node1 = u;
                        V node2 = parents.get(u);
                        if (node1.toString().compareTo(node2.toString()) > 0) {
                            V temp = node1;
                            node1 = node2;
                            node2 = temp;
                        }
                        Edge<V, L> edge = new Edge<>(node1, node2, label);
                        mstEdges.add(edge);
                    }

                    for (V v : graph.getNeighbours(u)) {
                        L edgeLabel = graph.getLabel(u, v);
                        double weight = edgeLabel.doubleValue();
                        if (!visited.contains(v) && keys.get(v) > weight) {
                            keys.put(v, weight);
                            parents.put(v, u);
                            if (queue.contains(v)) {
                                queue.remove(v);
                            }
                            queue.push(v);
                        }
                    }
                }
            }
        }
        return mstEdges;
    }
}
