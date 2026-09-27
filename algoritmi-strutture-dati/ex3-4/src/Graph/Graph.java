package Graph;

import java.util.*;

/**
 * Represents a graph data structure with vertices and edges.
 * Supports both directed and undirected graphs, as well as labelled and unlabelled graphs.
 *
 * @param <V> the type of the vertices
 * @param <L> the type of the labels associated with the edges
 */
public class Graph<V, L> implements AbstractGraph<V, L> {
    private final boolean directed;
    private final boolean labelled;

    private final Map<V, Map<V, Edge<V, L>>> adjacencyMap;
    private int edgeCount;

    /**
     * Constructs a new graph.
     *
     * @param directed true if the graph is directed, false otherwise
     * @param labelled true if the graph is labelled, false otherwise
     */
    public Graph(boolean directed, boolean labelled) {
        this.directed = directed;
        this.labelled = labelled;
        this.adjacencyMap = new LinkedHashMap<>();
    }

    /**
     * Checks if the graph is directed.
     *
     * @return true if the graph is directed, false otherwise
     */
    @Override
    public boolean isDirected() {
        return directed;
    }

    /**
     * Checks if the graph is labelled.
     *
     * @return true if the graph is labelled, false otherwise
     */
    @Override
    public boolean isLabelled() {
        return labelled;
    }

    /**
     * Adds a node to the graph.
     *
     * @param a the node to add
     * @return true if the node was added successfully, false id already present.
     */
    @Override
    public boolean addNode(V a) {
        if (containsNode(a)) {
            return false;
        }
        if (a == null) {
            throw new IllegalArgumentException("Null node.");
        }
        adjacencyMap.put(a, new HashMap<>());
        return true;
    }

    /**
     * Adds an edge between two nodes with an optional label.
     *
     * @param a the starting node of the edge
     * @param b the ending node of the edge
     * @param l the label associated with the edge
     * @return true if the edge was added successfully
     * @throws IllegalArgumentException if the nodes are null
     */
    @Override
    public boolean addEdge(V a, V b, L l) {
        if (a == null || b == null) {
            throw new IllegalArgumentException("Null nodes.");
        }
        if (labelled && l == null) {
            throw new IllegalArgumentException("A labelled graph requires an edge label.");
        }
        if (!adjacencyMap.containsKey(a) || !adjacencyMap.containsKey(b)) {
            return false;
        }
        if (adjacencyMap.get(a).containsKey(b)) {
            return false;
        }

        Edge<V, L> edge = new Edge<>(a, b, labelled ? l : null);
        adjacencyMap.get(a).put(b, edge);

        if (!directed) {
            adjacencyMap.get(b).put(a, edge);
        }
        edgeCount++;
        return true;
    }

    /**
     * Checks if a node is present in the graph.
     *
     * @param a the node to check
     * @return true if the node is present, false otherwise
     * @throws IllegalArgumentException if the node is null
     */
    @Override
    public boolean containsNode(V a) {
        if (a == null) {
            throw new IllegalArgumentException("Null node.");
        }
        return adjacencyMap.containsKey(a);
    }

    /**
     * Checks if an edge between two nodes is present in the graph.
     *
     * @param a the starting node of the edge
     * @param b the ending node of the edge
     * @return true if the edge is present, false otherwise
     * @throws IllegalArgumentException if the nodes are null
     */
    @Override
    public boolean containsEdge(V a, V b) {
        if (a == null || b == null) {
            throw new IllegalArgumentException("Null nodes.");
        }
        if (!adjacencyMap.containsKey(a) || !adjacencyMap.containsKey(b)) {
            return false;
        }

        return adjacencyMap.get(a).containsKey(b);
    }

    /**
     * Removes a node from the graph along with all its associated edges.
     *
     * @param a the node to remove
     * @return true if the node was removed successfully
     * @throws IllegalArgumentException if the node is null or not present in the graph
     */
    @Override
    public boolean removeNode(V a) {
        if (a == null || !adjacencyMap.containsKey(a)) {
            throw new IllegalArgumentException("Null node or not present in the graph");
        }
        if (directed) {
            edgeCount -= adjacencyMap.get(a).size();
            adjacencyMap.remove(a);
            for (Map<V, Edge<V, L>> outgoing : adjacencyMap.values()) {
                if (outgoing.remove(a) != null) {
                    edgeCount--;
                }
            }
        } else {
            for (V neighbour : new ArrayList<>(adjacencyMap.get(a).keySet())) {
                removeEdge(a, neighbour);
            }
            adjacencyMap.remove(a);
        }
        return true;
    }

    /**
     * Removes an edge between two nodes from the graph.
     *
     * @param a the starting node of the edge
     * @param b the ending node of the edge
     * @return true if the edge was removed successfully
     * @throws IllegalArgumentException if the nodes are null
     */
    @Override
    public boolean removeEdge(V a, V b) {
        if (a == null || b == null) {
            throw new IllegalArgumentException("Null nodes.");
        }
        if (!adjacencyMap.containsKey(a) || !adjacencyMap.containsKey(b)) {
            return false;
        }
        if (!adjacencyMap.get(a).containsKey(b)) return false;

        adjacencyMap.get(a).remove(b);

        if (!directed) adjacencyMap.get(b).remove(a);

        edgeCount--;
        return true;
    }

    /**
     * Returns the number of nodes in the graph.
     *
     * @return the number of nodes
     */
    @Override
    public int numNodes() {
        return adjacencyMap.size();
    }

    /**
     * Returns the number of edges in the graph.
     *
     * @return the number of edges
     */
    @Override
    public int numEdges() {
        return edgeCount;
    }

    /**
     * Returns a collection of all nodes in the graph.
     *
     * @return an unmodifiable collection of nodes
     */
    @Override
    public Collection<V> getNodes() {
        return Collections.unmodifiableSet(adjacencyMap.keySet());
    }

    /**
     * Returns a collection of all edges in the graph.
     *
     * @return an unmodifiable collection of edges
     */
    @Override
    public Collection<? extends AbstractEdge<V, L>> getEdges() {
        Set<Edge<V, L>> result = new LinkedHashSet<>();
        for (Map<V, Edge<V, L>> outgoing : adjacencyMap.values()) {
            result.addAll(outgoing.values());
        }
        return Collections.unmodifiableSet(result);
    }

    /**
     * Returns a collection of neighboring nodes adjacent to a given node.
     *
     * @param a the node whose neighbors are to be retrieved
     * @return an unmodifiable collection of neighboring nodes
     * @throws IllegalArgumentException if the node is null or not present in the graph
     */
    @Override
    public Collection<V> getNeighbours(V a) {
        if (a == null) {
            throw new IllegalArgumentException("Null node.");
        }
        if (!adjacencyMap.containsKey(a)) {
            throw new IllegalArgumentException("Node not present in the graph.");
        }

        return Collections.unmodifiableSet(adjacencyMap.get(a).keySet());
    }

    /**
     * Retrieves the label associated with the edge between two nodes.
     *
     * @param a the starting node of the edge
     * @param b the ending node of the edge
     * @return the label of the edge
     * @throws IllegalArgumentException if the nodes are null or not present in the graph
     * @throws NoSuchElementException   if there is no edge between the specified nodes
     */
    @Override
    public L getLabel(V a, V b) {
        if (a == null || b == null) {
            throw new IllegalArgumentException("Null nodes.");
        }
        if (!adjacencyMap.containsKey(a) || !adjacencyMap.containsKey(b)) {
            throw new IllegalArgumentException("One or more nodes not present in the graph.");
        }
        Edge<V, L> edge = adjacencyMap.get(a).get(b);
        if (edge == null) {
            throw new NoSuchElementException("No edge exists between the specified nodes.");
        }

        return edge.getLabel();
    }

    /**
     * Prints the adjacency map of the graph to the console.
     */
    public void printAdjacencyMap() {
        System.out.println("Adjacency Map:");
        for (Map.Entry<V, Map<V, Edge<V, L>>> entry : adjacencyMap.entrySet()) {
            V node = entry.getKey();
            Map<V, Edge<V, L>> edgesMap = entry.getValue();
            System.out.println(node + " ->");

            if (edgesMap.isEmpty()) {
                System.out.println("  None");
            } else {
                for (Map.Entry<V, Edge<V, L>> edgeEntry : edgesMap.entrySet()) {
                    V destination = edgeEntry.getKey();
                    Edge<V, L> edge = edgeEntry.getValue();

                    String description = "  Dest: " + destination;
                    if (labelled) {
                        description += ", Label: " + edge.getLabel();
                    }
                    System.out.println(description);
                }
            }
        }
    }

}
