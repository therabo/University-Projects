package Graph;

/**
 * Represents an edge in a graph, connecting two vertices with an optional label.
 *
 * @param <V> the type of the vertices
 * @param <L> the type of the labels associated with the edges
 */

public class Edge<V, L> implements AbstractEdge<V, L> {
    private final V start;

    private final V end;

    private final L label;

    /**
     * Constructs an edge connecting two vertices with an optional label.
     *
     * @param start the starting vertex of the edge
     * @param end   the ending vertex of the edge
     * @param label the label associated with the edge; can be null if the graph is unlabelled
     */
    public Edge(V start, V end, L label) {

        this.start = start;
        this.end = end;
        this.label = label;
    }

    /**
     * Returns the starting vertex of the edge.
     *
     * @return the starting vertex
     */
    @Override
    public V getStart() {
        return start;
    }

    /**
     * Returns the ending vertex of the edge.
     *
     * @return the ending vertex
     */
    @Override
    public V getEnd() {
        return end;
    }

    /**
     * Returns the label associated with the edge.
     *
     * @return the label of the edge, or null if the graph is unlabelled
     */
    @Override
    public L getLabel() {
        return label;
    }
}
