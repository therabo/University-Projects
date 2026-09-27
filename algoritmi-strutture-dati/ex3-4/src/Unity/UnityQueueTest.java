package Unity;

import org.junit.Test;
import static org.junit.Assert.*;
import QueueClass.PriorityQueue;
import java.util.Comparator;

public class UnityQueueTest {

    Comparator<Integer> comparator = Comparator.naturalOrder();
    Comparator<Integer> reverseComparator = Comparator.reverseOrder();


    @Test
    public void testEmptyQueue() {
        PriorityQueue<Integer> pq = new PriorityQueue<>(comparator);
        assertTrue("La coda dovrebbe essere vuota all'inizio", pq.empty());
    }

    @Test
    public void testPushAndContains() {
        PriorityQueue<Integer> pq = new PriorityQueue<>(comparator);
        pq.push(5);
        assertFalse("La coda non dovrebbe essere vuota dopo aver aggiunto un elemento", pq.empty());
        assertTrue("La coda dovrebbe contenere l'elemento 5", pq.contains(5));
    }

    @Test
    public void testTop() {
        PriorityQueue<Integer> pq = new PriorityQueue<>(comparator);
        pq.push(5);
        pq.push(3);
        pq.push(8);
        assertEquals("L'elemento in cima dovrebbe essere 3 (priorità più alta)", Integer.valueOf(3), pq.top());
    }

    @Test
    public void testPop() {
        PriorityQueue<Integer> pq = new PriorityQueue<>(comparator);
        pq.push(5);
        pq.push(3);
        pq.push(8);
        pq.pop();
        assertEquals("Dopo il pop, l'elemento in cima dovrebbe essere 5", Integer.valueOf(5), pq.top());
        assertFalse("La coda non dovrebbe essere vuota", pq.empty());
    }

    @Test
    public void testRemove() {
        PriorityQueue<Integer> pq = new PriorityQueue<>(comparator);
        pq.push(5);
        pq.push(3);
        pq.push(8);
        boolean removed = pq.remove(5);
        assertTrue("L'elemento 5 dovrebbe essere rimosso", removed);
        assertFalse("La coda non dovrebbe contenere 5", pq.contains(5));
        assertEquals("L'elemento in cima dovrebbe essere ancora 3", Integer.valueOf(3), pq.top());
    }

    @Test
    public void testRemoveTop() {
        PriorityQueue<Integer> pq = new PriorityQueue<>(comparator);
        pq.push(5);
        pq.push(3);
        pq.push(8);
        boolean removed = pq.remove(3);
        assertTrue("L'elemento 3 (in cima) dovrebbe essere rimosso", removed);
        assertEquals("L'elemento in cima dovrebbe ora essere 5", Integer.valueOf(5), pq.top());
    }

    @Test
    public void testDuplicatePush() {
        PriorityQueue<Integer> pq = new PriorityQueue<>(comparator);
        boolean firstPush = pq.push(5);
        boolean secondPush = pq.push(5);
        assertTrue("Il primo push dovrebbe riuscire", firstPush);
        assertFalse("Il secondo push dovrebbe fallire perché 5 è già presente", secondPush);
    }

    @Test
    public void testOrdering() {
        PriorityQueue<Integer> pq = new PriorityQueue<>(comparator);
        pq.push(5);
        pq.push(3);
        pq.push(8);
        pq.push(1);
        assertEquals("L'elemento in cima dovrebbe essere 1", Integer.valueOf(1), pq.top());
        pq.pop();
        assertEquals("L'elemento in cima dovrebbe essere 3 dopo aver tolto 1", Integer.valueOf(3), pq.top());
        pq.pop();
        assertEquals("L'elemento in cima dovrebbe essere 5 dopo aver tolto 3", Integer.valueOf(5), pq.top());
        pq.pop();
        assertEquals("L'elemento in cima dovrebbe essere 8 dopo aver tolto 5", Integer.valueOf(8), pq.top());
        pq.pop();
        assertTrue("La coda dovrebbe essere vuota dopo aver tolto tutti gli elementi", pq.empty());
    }

    @Test(expected = NullPointerException.class)
    public void testNullElement() {
        PriorityQueue<Integer> pq = new PriorityQueue<>(comparator);
        pq.push(null);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testTopOnEmptyQueue() {
        PriorityQueue<Integer> pq = new PriorityQueue<>(comparator);
        pq.top();
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testPopOnEmptyQueue() {
        PriorityQueue<Integer> pq = new PriorityQueue<>(comparator);
        pq.pop();
    }

    @Test
    public void testRemoveNonExistentElement() {
        PriorityQueue<Integer> pq = new PriorityQueue<>(comparator);
        pq.push(5);
        boolean removed = pq.remove(10);
        assertFalse("Rimuovere un elemento non presente dovrebbe restituire false", removed);
    }

    @Test
    public void testContainsOnEmptyQueue() {
        PriorityQueue<Integer> pq = new PriorityQueue<>(comparator);
        assertFalse("Una coda vuota non dovrebbe contenere nessun elemento", pq.contains(5));
    }

    @Test
    public void testCustomComparator() {
        PriorityQueue<Integer> pq = new PriorityQueue<>(reverseComparator);
        pq.push(5);
        pq.push(3);
        pq.push(8);
        assertEquals("Con un comparatore inverso, l'elemento in cima dovrebbe essere 8", Integer.valueOf(8), pq.top());
    }
    @Test
    public void testReverseOrderTop() {
        PriorityQueue<Integer> pq = new PriorityQueue<>(reverseComparator);
        pq.push(10);
        pq.push(20);
        pq.push(5);
        pq.push(15);
        assertEquals("Con comparatore inverso, l'elemento in cima dovrebbe essere 20", Integer.valueOf(20), pq.top());
    }


    @Test
    public void testReverseOrderPop() {
        PriorityQueue<Integer> pq = new PriorityQueue<>(reverseComparator);
        pq.push(10);
        pq.push(20);
        pq.push(5);
        pq.push(15);

        pq.pop();
        assertEquals("Dopo il primo pop, l'elemento in cima dovrebbe essere 15", Integer.valueOf(15), pq.top());

        pq.pop();
        assertEquals("Dopo il secondo pop, l'elemento in cima dovrebbe essere 10", Integer.valueOf(10), pq.top());

        pq.pop();
        assertEquals("Dopo il terzo pop, l'elemento in cima dovrebbe essere 5", Integer.valueOf(5), pq.top());

        pq.pop();
        assertTrue("La coda dovrebbe essere vuota dopo aver rimosso tutti gli elementi", pq.empty());
    }


    @Test
    public void testReverseOrderRemove() {
        PriorityQueue<Integer> pq = new PriorityQueue<>(reverseComparator);
        pq.push(10);
        pq.push(20);
        pq.push(5);
        pq.push(15);
        boolean removed = pq.remove(15);
        assertTrue("L'elemento 15 dovrebbe essere rimosso", removed);
        assertFalse("La coda non dovrebbe contenere 15", pq.contains(15));
        assertEquals("L'elemento in cima dovrebbe essere 20", Integer.valueOf(20), pq.top());
    }


    @Test
    public void testReverseOrderDuplicatePush() {
        PriorityQueue<Integer> pq = new PriorityQueue<>(reverseComparator);
        boolean firstPush = pq.push(10);
        boolean secondPush = pq.push(10);
        assertTrue("Il primo push dovrebbe riuscire", firstPush);
        assertFalse("Il secondo push dovrebbe fallire perché 10 è già presente", secondPush);
    }
}