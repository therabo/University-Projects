package Unity;

import Graph.AbstractEdge;
import Graph.Graph;
import Prim.Prim;
import org.junit.Test;

import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class UnityPrimTest {
    @Test
    public void testMinimumSpanningForestWithDisconnectedComponents() {
        Graph<String, Double> graph = new Graph<>(false, true);
        for (String node : new String[]{"A", "B", "C", "D", "E", "F"}) graph.addNode(node);
        graph.addEdge("A", "B", 1.0);
        graph.addEdge("B", "C", 2.0);
        graph.addEdge("A", "C", 5.0);
        graph.addEdge("D", "E", 4.0);

        Collection<? extends AbstractEdge<String, Double>> forest = Prim.minimumSpanningForest(graph);
        assertEquals(3, forest.size());
        double total = forest.stream().mapToDouble(AbstractEdge::getLabel).sum();
        assertEquals(7.0, total, 0.000001);
        assertEquals(4, graph.numEdges());
    }

    @Test
    public void testEmptyGraphHasEmptyForest() {
        Graph<String, Double> graph = new Graph<>(false, true);
        assertTrue(Prim.minimumSpanningForest(graph).isEmpty());
    }
}
