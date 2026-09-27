package Unity;

import org.junit.Test;
import static org.junit.Assert.*;
import Graph.Graph;
import Graph.AbstractEdge;
import java.util.Collection;
import java.util.NoSuchElementException;

public class UnityGraphTest {

    @Test
    public void testUndirectedReverseRemovalKeepsEdgesConsistent() {
        Graph<String, Integer> graph = new Graph<>(false, false);
        graph.addNode("A");
        graph.addNode("B");
        graph.addEdge("A", "B", null);

        assertTrue(graph.removeEdge("B", "A"));
        assertEquals(0, graph.numEdges());
        assertTrue(graph.getEdges().isEmpty());
        assertFalse(graph.containsEdge("A", "B"));
        assertFalse(graph.containsEdge("B", "A"));
    }

    @Test
    public void testUndirectedNodeRemovalKeepsEdgesConsistent() {
        Graph<String, Integer> graph = new Graph<>(false, false);
        graph.addNode("A");
        graph.addNode("B");
        graph.addNode("C");
        graph.addEdge("A", "B", null);
        graph.addEdge("C", "B", null);

        graph.removeNode("B");
        assertEquals(0, graph.numEdges());
        assertEquals(0, graph.getEdges().size());
        assertEquals(2, graph.numNodes());
    }

    @Test
    public void testDirectedNodeRemovalRemovesIncomingAndOutgoingEdges() {
        Graph<String, Integer> graph = new Graph<>(true, false);
        graph.addNode("A");
        graph.addNode("B");
        graph.addNode("C");
        graph.addEdge("A", "B", null);
        graph.addEdge("B", "C", null);
        graph.addEdge("C", "A", null);

        graph.removeNode("B");
        assertEquals(1, graph.numEdges());
        assertEquals(1, graph.getEdges().size());
        assertTrue(graph.containsEdge("C", "A"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testLabelledGraphRejectsNullLabel() {
        Graph<String, Integer> graph = new Graph<>(false, true);
        graph.addNode("A");
        graph.addNode("B");
        graph.addEdge("A", "B", null);
    }

    @Test
    public void testSelfLoopRemovalKeepsCountConsistent() {
        Graph<String, Integer> graph = new Graph<>(false, false);
        graph.addNode("A");
        graph.addEdge("A", "A", null);
        assertEquals(1, graph.numEdges());
        assertTrue(graph.removeEdge("A", "A"));
        assertEquals(0, graph.numEdges());
        assertTrue(graph.getEdges().isEmpty());
    }

    /**
     * Test che verifica l'aggiunta di un nodo al grafo.
     */
    @Test
    public void testAddNode() {
        Graph<String, Integer> graph = new Graph<>(false, false);
        boolean added = graph.addNode("A");
        assertTrue("Il nodo 'A' dovrebbe essere aggiunto", added);
        assertTrue("Il grafo dovrebbe contenere il nodo 'A'", graph.containsNode("A"));
    }

    /**
     * Test che verifica l'aggiunta di un nodo già esistente al grafo.
     */
    @Test
    public void testAddExistingNode() {
        Graph<String, Integer> graph = new Graph<>(false, false);
        graph.addNode("A");
        boolean addedAgain = graph.addNode("A");
        assertFalse("Aggiungere nuovamente il nodo 'A' dovrebbe restituire false", addedAgain);
    }

    /**
     * Test che verifica l'aggiunta di un nodo null al grafo.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testAddNullNode() {
        Graph<String, Integer> graph = new Graph<>(false, false);
        graph.addNode(null);
    }

    /**
     * Test che verifica l'aggiunta di un arco tra due nodi.
     */
    @Test
    public void testAddEdge() {
        Graph<String, Integer> graph = new Graph<>(false, false);
        graph.addNode("A");
        graph.addNode("B");
        boolean edgeAdded = graph.addEdge("A", "B", null);
        assertTrue("L'arco da 'A' a 'B' dovrebbe essere aggiunto", edgeAdded);
        assertTrue("Il grafo dovrebbe contenere l'arco da 'A' a 'B'", graph.containsEdge("A", "B"));
    }

    /**
     * Test che verifica l'aggiunta di un arco dove uno dei nodi non esiste.
     */
    @Test
    public void testAddEdgeNonExistingNode() {
        Graph<String, Integer> graph = new Graph<>(false, false);
        graph.addNode("A");
        boolean edgeAdded = graph.addEdge("A", "B", null);
        assertFalse("L'arco non dovrebbe essere aggiunto se il nodo 'B' non esiste", edgeAdded);
    }

    /**
     * Test che verifica l'aggiunta di un arco già esistente.
     */
    @Test
    public void testAddExistingEdge() {
        Graph<String, Integer> graph = new Graph<>(false, false);
        graph.addNode("A");
        graph.addNode("B");
        graph.addEdge("A", "B", null);
        boolean edgeAddedAgain = graph.addEdge("A", "B", null);
        assertFalse("Aggiungere lo stesso arco dovrebbe restituire false", edgeAddedAgain);
    }

    /**
     * Test che verifica l'aggiunta di un arco con nodi null.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testAddEdgeNullNodes() {
        Graph<String, Integer> graph = new Graph<>(false, false);
        graph.addEdge(null, null, null);
    }

    /**
     * Test che verifica la rimozione di un nodo dal grafo.
     */
    @Test
    public void testRemoveNode() {
        Graph<String, Integer> graph = new Graph<>(false, false);
        graph.addNode("A");
        boolean removed = graph.removeNode("A");
        assertTrue("Il nodo 'A' dovrebbe essere rimosso", removed);
        assertFalse("Il grafo non dovrebbe contenere il nodo 'A'", graph.containsNode("A"));
    }

    /**
     * Test che verifica la rimozione di un nodo che non esiste.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testRemoveNonExistingNode() {
        Graph<String, Integer> graph = new Graph<>(false, false);
        graph.removeNode("A");
    }

    /**
     * Test che verifica la rimozione di un arco dal grafo.
     */
    @Test
    public void testRemoveEdge() {
        Graph<String, Integer> graph = new Graph<>(false, false);
        graph.addNode("A");
        graph.addNode("B");
        graph.addEdge("A", "B", null);
        boolean removed = graph.removeEdge("A", "B");
        assertTrue("L'arco da 'A' a 'B' dovrebbe essere rimosso", removed);
        assertFalse("Il grafo non dovrebbe contenere l'arco da 'A' a 'B'", graph.containsEdge("A", "B"));
    }

    /**
     * Test che verifica la rimozione di un arco che non esiste.
     */
    @Test
    public void testRemoveNonExistingEdge() {
        Graph<String, Integer> graph = new Graph<>(false, false);
        graph.addNode("A");
        graph.addNode("B");
        boolean removed = graph.removeEdge("A", "B");
        assertFalse("Rimuovere un arco non esistente dovrebbe restituire false", removed);
    }

    /**
     * Test che verifica se il grafo contiene un nodo.
     */
    @Test
    public void testContainsNode() {
        Graph<String, Integer> graph = new Graph<>(false, false);
        graph.addNode("A");
        assertTrue("Il grafo dovrebbe contenere il nodo 'A'", graph.containsNode("A"));
        assertFalse("Il grafo non dovrebbe contenere il nodo 'B'", graph.containsNode("B"));
    }

    /**
     * Test che verifica se il grafo contiene un arco.
     */
    @Test
    public void testContainsEdge() {
        Graph<String, Integer> graph = new Graph<>(false, false);
        graph.addNode("A");
        graph.addNode("B");
        graph.addEdge("A", "B", null);
        assertTrue("Il grafo dovrebbe contenere l'arco da 'A' a 'B'", graph.containsEdge("A", "B"));
        assertTrue("In un grafo non diretto, dovrebbe contenere l'arco da 'B' a 'A'", graph.containsEdge("B", "A"));
    }

    /**
     * Test che verifica il recupero dell'etichetta di un arco.
     */
    @Test
    public void testGetLabel() {
        Graph<String, String> graph = new Graph<>(false, true);
        graph.addNode("A");
        graph.addNode("B");
        graph.addEdge("A", "B", "etichetta");
        String label = graph.getLabel("A", "B");
        assertEquals("L'etichetta dovrebbe essere 'etichetta'", "etichetta", label);
    }

    /**
     * Test che verifica il recupero dell'etichetta di un arco non esistente.
     */
    @Test(expected = NoSuchElementException.class)
    public void testGetLabelNonExistingEdge() {
        Graph<String, String> graph = new Graph<>(false, true);
        graph.addNode("A");
        graph.addNode("B");
        graph.getLabel("A", "B");
    }

    /**
     * Test che verifica il recupero dei vicini di un nodo.
     */
    @Test
    public void testGetNeighbours() {
        Graph<String, Integer> graph = new Graph<>(false, false);
        graph.addNode("A");
        graph.addNode("B");
        graph.addNode("C");
        graph.addEdge("A", "B", null);
        graph.addEdge("A", "C", null);
        Collection<String> neighbours = graph.getNeighbours("A");
        assertEquals("Il nodo 'A' dovrebbe avere 2 vicini", 2, neighbours.size());
        assertTrue("I vicini dovrebbero contenere 'B'", neighbours.contains("B"));
        assertTrue("I vicini dovrebbero contenere 'C'", neighbours.contains("C"));
    }

    /**
     * Test che verifica il recupero dei vicini di un nodo non esistente.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testGetNeighboursNonExistingNode() {
        Graph<String, Integer> graph = new Graph<>(false, false);
        graph.getNeighbours("A");
    }

    /**
     * Test che verifica il numero di nodi nel grafo.
     */
    @Test
    public void testNumNodes() {
        Graph<String, Integer> graph = new Graph<>(false, false);
        graph.addNode("A");
        graph.addNode("B");
        assertEquals("Il grafo dovrebbe avere 2 nodi", 2, graph.numNodes());
    }

    /**
     * Test che verifica il numero di archi nel grafo.
     */
    @Test
    public void testNumEdges() {
        Graph<String, Integer> graph = new Graph<>(false, false);
        graph.addNode("A");
        graph.addNode("B");
        graph.addEdge("A", "B", null);
        assertEquals("Il grafo dovrebbe avere 1 arco", 1, graph.numEdges());
    }

    /**
     * Test che verifica il recupero di tutti i nodi dal grafo.
     */
    @Test
    public void testGetNodes() {
        Graph<String, Integer> graph = new Graph<>(false, false);
        graph.addNode("A");
        graph.addNode("B");
        Collection<String> nodes = graph.getNodes();
        assertEquals("Il grafo dovrebbe avere 2 nodi", 2, nodes.size());
        assertTrue("I nodi dovrebbero contenere 'A'", nodes.contains("A"));
        assertTrue("I nodi dovrebbero contenere 'B'", nodes.contains("B"));
    }

    /**
     * Test che verifica il recupero di tutti gli archi dal grafo.

     */

    @Test

    public void testGetEdges() {
        Graph<String, String> graph = new Graph<>(false, true);
        graph.addNode("A");
        graph.addNode("B");
        graph.addEdge("A", "B", "etichetta1");
        graph.addEdge("B", "A", "etichetta2");
        Collection<? extends AbstractEdge<String, String>> edges = graph.getEdges();
        assertEquals("Il grafo dovrebbe avere un arco", 1, edges.size());
    }

    /**
     * Test che verifica il comportamento di un grafo diretto.
     */
    @Test
    public void testDirectedGraph() {
        Graph<String, Integer> graph = new Graph<>(true, false);
        graph.addNode("A");
        graph.addNode("B");
        graph.addEdge("A", "B", null);
        assertTrue("Il grafo dovrebbe contenere l'arco da 'A' a 'B'", graph.containsEdge("A", "B"));
        assertFalse("Il grafo non dovrebbe contenere l'arco da 'B' a 'A'", graph.containsEdge("B", "A"));
    }

    /**
     * Test che verifica il comportamento di un grafo non diretto.
     */
    @Test
    public void testUndirectedGraph() {
        Graph<String, Integer> graph = new Graph<>(false, false);
        graph.addNode("A");
        graph.addNode("B");
        graph.addEdge("A", "B", null);
        assertTrue("Il grafo dovrebbe contenere l'arco da 'A' a 'B'", graph.containsEdge("A", "B"));
        assertTrue("In un grafo non diretto, dovrebbe contenere l'arco da 'B' a 'A'", graph.containsEdge("B", "A"));
    }

    /**
     * Test che verifica il comportamento di un grafo etichettato.
     */
    @Test
    public void testLabelledGraph() {
        Graph<String, String> graph = new Graph<>(false, true);
        graph.addNode("A");
        graph.addNode("B");
        graph.addEdge("A", "B", "EtichettaArco");
        String label = graph.getLabel("A", "B");
        assertEquals("L'etichetta dell'arco dovrebbe essere 'EtichettaArco'", "EtichettaArco", label);
    }

    /**
     * Test che verifica il comportamento di un grafo non etichettato.
     */
    @Test
    public void testUnlabelledGraph() {
        Graph<String, String> graph = new Graph<>(false, false);
        graph.addNode("A");
        graph.addNode("B");
        graph.addEdge("A", "B", "DovrebbeEssereNull");
        Object label = graph.getLabel("A", "B");
        assertNull("L'etichetta dell'arco dovrebbe essere null in un grafo non etichettato", label);
    }

    /**
     * Test che verifica l'aggiunta di un arco ad anello (self-loop).
     */
    @Test
    public void testSelfLoopEdge() {
        Graph<String, Integer> graph = new Graph<>(false, false);
        graph.addNode("A");
        boolean edgeAdded = graph.addEdge("A", "A", null);
        assertTrue("Dovrebbe permettere l'aggiunta di un arco ad anello", edgeAdded);
        assertTrue("Il grafo dovrebbe contenere l'arco da 'A' a 'A'", graph.containsEdge("A", "A"));
    }

    /**
     * Test che verifica la rimozione di un nodo e l'eliminazione dei relativi archi.
     */
    @Test
    public void testRemoveNodeEdges() {
        Graph<String, Integer> graph = new Graph<>(false, false);
        graph.addNode("A");
        graph.addNode("B");
        graph.addEdge("A", "B", null);
        graph.removeNode("A");
        assertFalse("Il grafo non dovrebbe contenere il nodo 'A'", graph.containsNode("A"));
        assertFalse("Il grafo non dovrebbe contenere l'arco da 'A' a 'B'", graph.containsEdge("A", "B"));
        assertFalse("Il grafo non dovrebbe contenere l'arco da 'B' a 'A'", graph.containsEdge("B", "A"));
    }

    /**
     * Test che verifica l'eccezione quando si aggiunge un arco con nodo di partenza null.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testAddEdgeNullStartNode() {
        Graph<String, Integer> graph = new Graph<>(false, false);
        graph.addNode("B");
        graph.addEdge(null, "B", null);
    }

    /**
     * Test che verifica l'eccezione quando si aggiunge un arco con nodo di arrivo null.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testAddEdgeNullEndNode() {
        Graph<String, Integer> graph = new Graph<>(false, false);
        graph.addNode("A");
        graph.addEdge("A", null, null);
    }

    /**
     * Test che verifica l'eccezione quando si controlla se il grafo contiene un nodo null.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testContainsNullNode() {
        Graph<String, Integer> graph = new Graph<>(false, false);
        graph.containsNode(null);
    }

    /**
     * Test che verifica l'eccezione quando si controlla se il grafo contiene un arco con nodi null.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testContainsEdgeNullNodes() {
        Graph<String, Integer> graph = new Graph<>(false, false);
        graph.containsEdge(null, null);
    }

    /**
     * Test che verifica l'eccezione quando si rimuove un arco con nodi null.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testRemoveEdgeNullNodes() {
        Graph<String, Integer> graph = new Graph<>(false, false);
        graph.removeEdge(null, null);
    }

    /**
     * Test che verifica l'aggiunta e la rimozione di più archi.
     */
    @Test
    public void testMultipleEdges() {
        Graph<String, Integer> graph = new Graph<>(false, false);
        graph.addNode("A");
        graph.addNode("B");
        graph.addNode("C");
        graph.addEdge("A", "B", null);
        graph.addEdge("B", "C", null);
        graph.addEdge("C", "A", null);
        assertEquals("Il grafo dovrebbe avere 3 archi", 3, graph.numEdges());
        graph.removeEdge("B", "C");
        assertEquals("Il grafo dovrebbe avere 2 archi dopo la rimozione", 2, graph.numEdges());
    }

    /**
     * Test che verifica il conteggio degli archi in un grafo diretto.
     */
    @Test
    public void testEdgeCountDirectedGraph() {
        Graph<String, Integer> graph = new Graph<>(true, false);
        graph.addNode("A");
        graph.addNode("B");
        graph.addEdge("A", "B", null);
        assertEquals("Il grafo diretto dovrebbe avere 1 arco", 1, graph.numEdges());
    }

    /**
     * Test che verifica il conteggio degli archi in un grafo non diretto.
     */
    @Test
    public void testEdgeCountUndirectedGraph() {
        Graph<String, Integer> graph = new Graph<>(false, false);
        graph.addNode("A");
        graph.addNode("B");
        graph.addEdge("A", "B", null);
        assertEquals("Il grafo non diretto dovrebbe avere 1 arco", 1, graph.numEdges());
    }

    /**
     * Test che verifica che la rimozione di un arco in un grafo non diretto rimuova entrambe le direzioni.
     */
    @Test
    public void testRemoveEdgeUndirectedGraph() {
        Graph<String, Integer> graph = new Graph<>(false, false);
        graph.addNode("A");
        graph.addNode("B");
        graph.addEdge("A", "B", null);
        graph.removeEdge("A", "B");
        assertFalse("Il grafo non dovrebbe contenere l'arco da 'A' a 'B'", graph.containsEdge("A", "B"));
        assertFalse("Il grafo non dovrebbe contenere l'arco da 'B' a 'A'", graph.containsEdge("B", "A"));
    }
}
