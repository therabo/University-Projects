package Graph;

public interface AbstractEdge<V, L> {
    public V getStart(); // il nodo di partenza dell'arco

    public V getEnd(); // il nodo di arrivo dell'arco

    public L getLabel(); // l'etichetta dell'arco
}
